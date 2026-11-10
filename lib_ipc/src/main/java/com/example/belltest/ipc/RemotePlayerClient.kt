package com.example.belltest.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.view.Surface

class RemotePlayerClient(
    context: Context,
    private val listener: Listener,
) {
    interface Listener {
        fun onConnectionChanged(connected: Boolean, remotePid: Int?)
        fun onPlaybackSnapshot(snapshot: PlaybackSnapshot)
        fun onVideoSizeChanged(mediaId: String, width: Int, height: Int, pixelRatio: Float)
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val tracker = PlaybackRequestTracker()
    private var service: IVideoPlayerService? = null
    private var serviceBinder: IBinder? = null
    private var bindRequested = false
    private var isBound = false

    private val callback = object : IVideoPlayerCallback.Stub() {
        override fun onPlaybackStateChanged(snapshot: PlaybackSnapshot?) {
            if (snapshot == null) return
            mainHandler.post {
                if (tracker.isCurrent(snapshot.requestId, snapshot.mediaId)) {
                    tracker.rememberPosition(snapshot.requestId, snapshot.mediaId, snapshot.positionMs)
                    listener.onPlaybackSnapshot(snapshot)
                }
            }
        }

        override fun onVideoSizeChanged(
            requestId: Long,
            mediaId: String?,
            width: Int,
            height: Int,
            pixelWidthHeightRatio: Float,
        ) {
            val safeMediaId = mediaId.orEmpty()
            mainHandler.post {
                if (tracker.isCurrent(requestId, safeMediaId)) {
                    listener.onVideoSizeChanged(
                        safeMediaId,
                        width,
                        height,
                        pixelWidthHeightRatio,
                    )
                }
            }
        }
    }

    private val deathRecipient = IBinder.DeathRecipient {
        mainHandler.post { handleConnectionLost(rebind = true) }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder == null) {
                handleConnectionLost(rebind = true)
                return
            }
            serviceBinder = binder
            service = IVideoPlayerService.Stub.asInterface(binder)
            try {
                binder.linkToDeath(deathRecipient, 0)
                service?.registerCallback(callback)
                listener.onConnectionChanged(true, service?.servicePid)
            } catch (_: RemoteException) {
                handleConnectionLost(rebind = true)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            handleConnectionLost(rebind = true)
        }

        override fun onBindingDied(name: ComponentName?) {
            handleConnectionLost(rebind = true)
        }

        override fun onNullBinding(name: ComponentName?) {
            handleConnectionLost(rebind = false)
        }
    }

    fun bind() {
        if (bindRequested) return
        bindRequested = true
        bindInternal()
    }

    private fun bindInternal() {
        val intent = Intent().setComponent(
            ComponentName(appContext.packageName, PlaybackContract.SERVICE_CLASS_NAME),
        )
        isBound = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!isBound) {
            listener.onConnectionChanged(false, null)
        }
    }

    fun prepare(mediaId: String, url: String, startPositionMs: Long, playWhenReady: Boolean): Long {
        val requestId = tracker.startRequest(mediaId)
        try {
            service?.prepare(requestId, mediaId, url, playWhenReady)
            if (startPositionMs > 0L) service?.seekTo(startPositionMs)
        } catch (_: RemoteException) {
            handleConnectionLost(rebind = true)
        }
        return requestId
    }

    fun attachSurface(surface: Surface): Long {
        val surfaceToken = tracker.startSurfaceSession()
        try {
            service?.attachSurface(surfaceToken, surface)
        } catch (_: RemoteException) {
            handleConnectionLost(rebind = true)
        }
        return surfaceToken
    }

    fun clearSurface(surfaceToken: Long) {
        if (!tracker.canClearSurface(surfaceToken)) return
        try {
            service?.clearSurface(surfaceToken)
        } catch (_: RemoteException) {
            handleConnectionLost(rebind = true)
        }
    }

    fun play() = callRemote { it.play() }

    fun pause() = callRemote { it.pause() }

    fun seekTo(positionMs: Long) = callRemote { it.seekTo(positionMs.coerceAtLeast(0L)) }

    fun resumePosition(mediaId: String): Long = tracker.resumePosition(mediaId)

    fun releaseAndUnbind() {
        bindRequested = false
        try {
            service?.clearSurface(Long.MAX_VALUE)
            service?.unregisterCallback(callback)
            service?.releasePlayback()
        } catch (_: RemoteException) {
            // The remote process is already gone, so its resources are gone too.
        }
        unlinkDeathRecipient()
        service = null
        if (isBound) {
            runCatching { appContext.unbindService(connection) }
            isBound = false
        }
        listener.onConnectionChanged(false, null)
    }

    private inline fun callRemote(block: (IVideoPlayerService) -> Unit) {
        try {
            service?.let(block)
        } catch (_: RemoteException) {
            handleConnectionLost(rebind = true)
        }
    }

    private fun handleConnectionLost(rebind: Boolean) {
        unlinkDeathRecipient()
        service = null
        listener.onConnectionChanged(false, null)
        if (rebind && bindRequested) {
            if (isBound) runCatching { appContext.unbindService(connection) }
            isBound = false
            mainHandler.removeCallbacks(rebindRunnable)
            mainHandler.postDelayed(rebindRunnable, REBIND_DELAY_MS)
        }
    }

    private val rebindRunnable = Runnable {
        if (bindRequested && !isBound) bindInternal()
    }

    private fun unlinkDeathRecipient() {
        serviceBinder?.let { binder ->
            runCatching { binder.unlinkToDeath(deathRecipient, 0) }
        }
        serviceBinder = null
    }

    private companion object {
        const val REBIND_DELAY_MS = 300L
    }
}
