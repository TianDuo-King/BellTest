package com.example.belltest.player

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.RemoteCallbackList
import android.util.Log
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.belltest.ipc.IVideoPlayerCallback
import com.example.belltest.ipc.IVideoPlayerService
import com.example.belltest.ipc.PlaybackContract
import com.example.belltest.ipc.PlaybackSnapshot
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@UnstableApi
class RemoteVideoPlayerService : Service() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val callbacks = RemoteCallbackList<IVideoPlayerCallback>()

    @Volatile
    private var latestSnapshot = PlaybackSnapshot()

    private var player: ExoPlayer? = null
    private var currentRequestId = 0L
    private var currentMediaId = ""
    private var currentSurfaceToken = 0L
    private var currentSurface: Surface? = null
    private var lastError: PlaybackException? = null
    private lateinit var videoCache: SimpleCache
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private val preloadExecutor = Executors.newSingleThreadExecutor()

    @Volatile
    private var preloadWriter: CacheWriter? = null

    @Volatile
    private var preloadUrl: String? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.i(TAG, "state=$playbackState media=$currentMediaId pid=${Process.myPid()}")
            publishSnapshot()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) = publishSnapshot()

        override fun onPlayerError(error: PlaybackException) {
            lastError = error
            Log.e(TAG, "Playback failed media=$currentMediaId code=${error.errorCodeName}", error)
            publishSnapshot()
        }

        override fun onRenderedFirstFrame() {
            Log.i(TAG, "Rendered first frame media=$currentMediaId pid=${Process.myPid()}")
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            val count = callbacks.beginBroadcast()
            try {
                for (index in 0 until count) {
                    runCatching {
                        callbacks.getBroadcastItem(index).onVideoSizeChanged(
                            currentRequestId,
                            currentMediaId,
                            videoSize.width,
                            videoSize.height,
                            videoSize.pixelWidthHeightRatio,
                        )
                    }
                }
            } finally {
                callbacks.finishBroadcast()
            }
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (player != null) {
                publishSnapshot()
                mainHandler.postDelayed(this, PROGRESS_INTERVAL_MS)
            }
        }
    }

    private val binder = object : IVideoPlayerService.Stub() {
        override fun attachSurface(surfaceToken: Long, surface: Surface?) {
            if (surface == null) return
            mainHandler.post { attachSurfaceOnMain(surfaceToken, surface) }
        }

        override fun clearSurface(surfaceToken: Long) {
            mainHandler.post {
                if (surfaceToken == currentSurfaceToken || surfaceToken == Long.MAX_VALUE) {
                    clearSurfaceOnMain()
                }
            }
        }

        override fun prepare(
            requestId: Long,
            mediaId: String?,
            url: String?,
            playWhenReady: Boolean,
        ) {
            if (mediaId.isNullOrBlank() || url.isNullOrBlank()) return
            mainHandler.post {
                val remotePlayer = ensurePlayer()
                currentRequestId = requestId
                currentMediaId = mediaId
                lastError = null
                latestSnapshot = PlaybackSnapshot(requestId = requestId, mediaId = mediaId)
                remotePlayer.setMediaItem(MediaItem.fromUri(url))
                remotePlayer.playWhenReady = playWhenReady
                remotePlayer.prepare()
                publishSnapshot()
            }
        }

        override fun play() {
            mainHandler.post { player?.play() }
        }

        override fun pause() {
            mainHandler.post { player?.pause() }
        }

        override fun seekTo(positionMs: Long) {
            mainHandler.post { player?.seekTo(positionMs.coerceAtLeast(0L)) }
        }

        override fun preload(url: String?) {
            if (url.isNullOrBlank() || url == preloadUrl) return
            preloadUrl = url
            preloadWriter?.cancel()
            preloadExecutor.execute {
                if (url != preloadUrl) return@execute
                val writer = CacheWriter(
                    cacheDataSourceFactory.createDataSource(),
                    DataSpec.Builder()
                        .setUri(url)
                        .setLength(PRELOAD_BYTES)
                        .build(),
                    null,
                    null,
                )
                preloadWriter = writer
                runCatching { writer.cache() }
                    .onFailure { error ->
                        if (url == preloadUrl) {
                            Log.w(TAG, "Preload skipped url=$url", error)
                        }
                    }
                if (preloadWriter === writer) preloadWriter = null
            }
        }

        override fun registerCallback(callback: IVideoPlayerCallback?) {
            if (callback == null) return
            mainHandler.post {
                callbacks.register(callback)
                runCatching { callback.onPlaybackStateChanged(latestSnapshot) }
            }
        }

        override fun unregisterCallback(callback: IVideoPlayerCallback?) {
            if (callback == null) return
            mainHandler.post { callbacks.unregister(callback) }
        }

        override fun releasePlayback() {
            mainHandler.post { releasePlayerOnMain() }
        }

        override fun getSnapshot(): PlaybackSnapshot = latestSnapshot

        override fun getServicePid(): Int = Process.myPid()
    }

    override fun onCreate() {
        super.onCreate()

        videoCache = SimpleCache(
            File(cacheDir, "video_cache"),
            LeastRecentlyUsedCacheEvictor(CACHE_MAX_BYTES),
            StandaloneDatabaseProvider(this),
        )

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
            .setReadTimeoutMs(READ_TIMEOUT_MS)
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("BellTest/1.0")

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(videoCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        Log.i(TAG, "Remote player service created in pid=${Process.myPid()}")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        releasePlayerOnMain()
        callbacks.kill()
        preloadWriter?.cancel()
        preloadExecutor.shutdownNow()
        runCatching { preloadExecutor.awaitTermination(300L, TimeUnit.MILLISECONDS) }
        videoCache.release()

        Log.i(TAG, "Remote player service destroyed in pid=${Process.myPid()}")
        super.onDestroy()
    }

    private fun ensurePlayer(): ExoPlayer {
        player?.let { return it }

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(cacheDataSourceFactory)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMsForStreaming(
                MIN_BUFFER_MS,
                MAX_BUFFER_MS,
                BUFFER_FOR_PLAYBACK_MS,
                BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
            )
            .setPrioritizeTimeOverSizeThresholdsForStreaming(true)
            .build()

        return ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .also { newPlayer ->
                player = newPlayer
                newPlayer.addListener(playerListener)
                newPlayer.repeatMode = Player.REPEAT_MODE_ONE
                newPlayer.setHandleAudioBecomingNoisy(true)
                currentSurface?.let(newPlayer::setVideoSurface)

                mainHandler.removeCallbacks(progressRunnable)
                mainHandler.post(progressRunnable)
            }
    }

    private fun attachSurfaceOnMain(surfaceToken: Long, surface: Surface) {
        if (surfaceToken < currentSurfaceToken) {
            surface.release()
            return
        }
        clearSurfaceOnMain()
        currentSurfaceToken = surfaceToken
        currentSurface = surface
        player?.setVideoSurface(surface)
    }

    private fun clearSurfaceOnMain() {
        player?.clearVideoSurface()
        currentSurface?.release()
        currentSurface = null
        currentSurfaceToken = 0L
    }

    private fun publishSnapshot() {
        val remotePlayer = player
        val error = lastError
        latestSnapshot = if (remotePlayer == null) {
            PlaybackSnapshot(requestId = currentRequestId, mediaId = currentMediaId)
        } else {
            PlaybackSnapshot(
                requestId = currentRequestId,
                mediaId = currentMediaId,
                playbackState = Media3PlaybackStateMapper.toRemoteState(
                    remotePlayer.playbackState,
                    error != null,
                ),
                isPlaying = remotePlayer.isPlaying,
                positionMs = remotePlayer.currentPosition.coerceAtLeast(0L),
                durationMs = remotePlayer.duration.validDuration(),
                bufferedPositionMs = remotePlayer.bufferedPosition.coerceAtLeast(0L),
                errorCode = error?.toRemoteErrorCode() ?: PlaybackContract.ERROR_NONE,
                errorMessage = error?.message,
            )
        }

        val count = callbacks.beginBroadcast()
        try {
            for (index in 0 until count) {
                runCatching { callbacks.getBroadcastItem(index).onPlaybackStateChanged(latestSnapshot) }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    private fun releasePlayerOnMain() {
        mainHandler.removeCallbacks(progressRunnable)
        clearSurfaceOnMain()
        player?.removeListener(playerListener)
        player?.release()
        player = null
        lastError = null
        latestSnapshot = PlaybackSnapshot(requestId = currentRequestId, mediaId = currentMediaId)
        Log.i(TAG, "Released ExoPlayer in pid=${Process.myPid()}")
    }

    private fun Long.validDuration(): Long = if (this == C.TIME_UNSET || this < 0L) 0L else this

    private fun PlaybackException.toRemoteErrorCode(): Int = when (errorCode) {
        in 2_000..3_999 -> PlaybackContract.ERROR_NETWORK_OR_SOURCE
        in 4_000..4_999 -> PlaybackContract.ERROR_DECODER
        else -> PlaybackContract.ERROR_UNEXPECTED
    }

    private companion object {
        const val TAG = "RemoteVideoPlayer"
        // The UI only displays whole seconds, so a 1 Hz cross-process update avoids redundant
        // Binder traffic and TextView layout work without making the progress label feel stale.
        const val PROGRESS_INTERVAL_MS = 1_000L
        const val CACHE_MAX_BYTES = 512L * 1024L * 1024L
        const val PRELOAD_BYTES = 8L * 1024L * 1024L
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 15_000
        const val MIN_BUFFER_MS = 12_000
        const val MAX_BUFFER_MS = 60_000
        const val BUFFER_FOR_PLAYBACK_MS = 800
        const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2_000
    }
}
