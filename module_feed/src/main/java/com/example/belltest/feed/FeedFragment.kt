package com.example.belltest.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.belltest.base.BaseViewBindingFragment
import com.example.belltest.base.SystemBarInsetTarget
import com.example.belltest.feed.databinding.FeedFragmentBinding
import com.example.belltest.ipc.PlaybackContract
import com.example.belltest.ipc.PlaybackSnapshot
import com.example.belltest.ipc.RemotePlayerClient
import com.example.belltest.model.DemoVideoCatalog

class FeedFragment : BaseViewBindingFragment<FeedFragmentBinding>() {

    companion object {
        const val REQUEST_OPEN_MEDIA = "feed:open-media"
        const val KEY_MEDIA_ID = "mediaId"
        private const val PRELOAD_AFTER_BUFFERED_MS = 5_000L
    }
    private val items = DemoVideoCatalog.items
    private val coordinator = FeedPlaybackCoordinator(items.map { it.id })

    private lateinit var playerClient: RemotePlayerClient
    private lateinit var pagerAdapter: FeedPagerAdapter
    private var currentPosition = 0
    private var connected = false
    private var started = false
    private var activeSurfaceToken = 0L
    private var activeSurfacePosition = -1
    private var currentSnapshot: PlaybackSnapshot? = null
    private var preloadedAfterMediaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerClient = RemotePlayerClient(
            requireContext(),
            playerListener
        )

        // 监听其他模块传递过来的视频 ID
        parentFragmentManager.setFragmentResultListener(
            REQUEST_OPEN_MEDIA,
            this
        ) { _, result ->

            val mediaId = result.getString(KEY_MEDIA_ID)
                ?: return@setFragmentResultListener

            // 在视频列表中查找指定视频
            val targetPosition = items.indexOfFirst {
                it.id == mediaId
            }

            if (targetPosition == -1) {
                return@setFragmentResultListener
            }

            // 跳转到对应的视频位置
            bindingOrNull?.videoPager?.setCurrentItem(
                targetPosition,
                false
            )
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FeedFragmentBinding =
        FeedFragmentBinding.inflate(inflater, container, false)

    override fun systemBarInsetTargets(binding: FeedFragmentBinding) = emptyList<SystemBarInsetTarget>()

    override fun onBindingCreated(binding: FeedFragmentBinding, savedInstanceState: Bundle?) {
        pagerAdapter = FeedPagerAdapter(items, surfaceListener) { position ->
            if (position == currentPosition) startCurrentPlayback(resume = true)
        }
        binding.videoPager.adapter = pagerAdapter
        binding.videoPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.videoPager.offscreenPageLimit = 2
        (binding.videoPager.getChildAt(0) as? RecyclerView)?.apply {
            itemAnimator = null
            setItemViewCacheSize(3)
        }
        binding.videoPager.registerOnPageChangeCallback(pageChangeCallback)
        pagerAdapter.render(currentPosition, currentSnapshot, connected)
    }

    override fun onStart() {
        super.onStart()
        started = true
        playerClient.bind()
    }

    override fun onStop() {
        started = false
        currentSnapshot?.let { coordinator.rememberPosition(it.mediaId, it.positionMs) }
        clearActiveSurface()
        playerClient.releaseAndUnbind()
        connected = false
        bindingOrNull?.let { pagerAdapter.render(currentPosition, currentSnapshot, false) }
        super.onStop()
    }

    override fun onBindingDestroyed(binding: FeedFragmentBinding) {
        binding.videoPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.videoPager.adapter = null
    }

    private fun handleConnectionChanged(connected: Boolean) {
        this.connected = connected
        bindingOrNull ?: return
        pagerAdapter.render(currentPosition, currentSnapshot, connected)
        if (connected && started) startCurrentPlayback(resume = true)
    }

    private fun handlePlaybackSnapshot(snapshot: PlaybackSnapshot) {
        currentSnapshot = snapshot
        coordinator.rememberPosition(snapshot.mediaId, snapshot.positionMs)
        val bufferedAheadMs = snapshot.bufferedPositionMs - snapshot.positionMs
        if (
            snapshot.playbackState == PlaybackContract.STATE_READY &&
            bufferedAheadMs >= PRELOAD_AFTER_BUFFERED_MS &&
            preloadedAfterMediaId != snapshot.mediaId
        ) {
            preloadedAfterMediaId = snapshot.mediaId
            items.getOrNull((currentPosition + 1) % items.size)?.let { nextItem ->
                playerClient.preload(nextItem.mediaUrl)
            }
        }
        bindingOrNull ?: return
        pagerAdapter.render(currentPosition, snapshot, connected)
    }

    private fun handleVideoSizeChanged(
        mediaId: String,
        width: Int,
        height: Int,
        pixelRatio: Float,
    ) {
        bindingOrNull ?: return
        pagerAdapter.applyVideoSize(currentPosition, width, height, pixelRatio)
    }

    private fun handleSurfaceAvailable(position: Int, surface: Surface) {
        if (position != currentPosition || !connected || !started) return
        attachSurface(position, surface)
    }

    private fun handleSurfaceDestroyed(position: Int) {
        if (position == activeSurfacePosition) clearActiveSurface()
    }

    private fun startCurrentPlayback(resume: Boolean) {
        if (!connected || !started) return
        clearActiveSurface()
        pagerAdapter.surfaceAt(currentPosition)?.let { attachSurface(currentPosition, it) }
        val item = items[currentPosition]
        val startPosition = if (resume) playerClient.resumePosition(item.id).takeIf { it > 0L }
            ?: coordinator.restoreCurrent().startPositionMs else 0L
        playerClient.prepare(item.id, item.mediaUrl, startPosition, playWhenReady = true)
        currentSnapshot = null
        pagerAdapter.render(currentPosition, null, connected)
    }

    private fun attachSurface(position: Int, surface: Surface) {
        clearActiveSurface()
        activeSurfaceToken = playerClient.attachSurface(surface)
        activeSurfacePosition = position
    }

    private fun clearActiveSurface() {
        if (activeSurfaceToken != 0L) playerClient.clearSurface(activeSurfaceToken)
        activeSurfaceToken = 0L
        activeSurfacePosition = -1
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (position == currentPosition && currentSnapshot != null) return
            currentPosition = position
            currentSnapshot = null
            coordinator.select(position)
            bindingOrNull?.videoPager?.post {
                if (bindingOrNull != null && currentPosition == position) {
                    pagerAdapter.render(position, null, connected)
                    startCurrentPlayback(resume = false)
                }
            }
        }
    }

    private val playerListener = object : RemotePlayerClient.Listener {
        override fun onConnectionChanged(connected: Boolean, remotePid: Int?) {
            handleConnectionChanged(connected)
        }

        override fun onPlaybackSnapshot(snapshot: PlaybackSnapshot) {
            handlePlaybackSnapshot(snapshot)
        }

        override fun onVideoSizeChanged(
            mediaId: String,
            width: Int,
            height: Int,
            pixelRatio: Float,
        ) {
            handleVideoSizeChanged(mediaId, width, height, pixelRatio)
        }
    }

    private val surfaceListener = object : FeedPagerAdapter.SurfaceListener {
        override fun onSurfaceAvailable(position: Int, surface: Surface) {
            handleSurfaceAvailable(position, surface)
        }

        override fun onSurfaceDestroyed(position: Int) {
            handleSurfaceDestroyed(position)
        }
    }

}
