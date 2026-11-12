package com.example.belltest.feed

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.belltest.base.applySystemBarsPadding
import com.example.belltest.feed.databinding.FeedItemVideoBinding
import com.example.belltest.ipc.PlaybackContract
import com.example.belltest.ipc.PlaybackSnapshot
import com.example.belltest.model.VideoItem

internal class FeedPagerAdapter(
    private val items: List<VideoItem>,
    private val surfaceListener: SurfaceListener,
    private val retryListener: (Int) -> Unit,
) : RecyclerView.Adapter<FeedPagerAdapter.VideoViewHolder>() {
    interface SurfaceListener {
        fun onSurfaceAvailable(position: Int, surface: Surface)
        fun onSurfaceDestroyed(position: Int)
    }

    private val holders = mutableMapOf<Int, VideoViewHolder>()
    private val renderState = FeedRenderState()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = FeedItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holders.entries.removeAll { it.value === holder }
        holders[position] = holder
        holder.bind(
            item = items[position],
            position = position,
            isCurrent = position == renderState.position,
            playbackSnapshot = renderState.snapshot,
            isConnected = renderState.connected,
        )
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        holders.entries.removeAll { it.value === holder }
        holder.recycle()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = items.size

    fun surfaceAt(position: Int): Surface? = holders[position]?.validSurface()

    fun render(position: Int, newSnapshot: PlaybackSnapshot?, isConnected: Boolean) {
        val previousPosition = renderState.position
        val previousSnapshot = renderState.snapshot
        val previousConnected = renderState.connected
        val changedPositions = renderState.update(position, newSnapshot, isConnected)
        val presentationChanged = previousPosition != position ||
            previousConnected != isConnected ||
            previousSnapshot.presentationKey() != newSnapshot.presentationKey()
        if (!presentationChanged) return

        changedPositions.forEach { changedPosition ->
            holders[changedPosition]?.updatePlaybackState(
                isCurrent = changedPosition == renderState.position,
                playbackSnapshot = renderState.snapshot,
                isConnected = renderState.connected,
            )
        }
    }

    private fun PlaybackSnapshot?.presentationKey(): List<Any?> {
        val snapshot = this
        val bufferedPercentage = if (
            snapshot != null &&
            snapshot.playbackState == PlaybackContract.STATE_BUFFERING &&
            snapshot.durationMs > 0L
        ) {
            (snapshot.bufferedPositionMs * 100L / snapshot.durationMs).coerceIn(0L, 100L)
        } else {
            null
        }
        return listOf(
            snapshot?.requestId,
            snapshot?.mediaId,
            snapshot?.playbackState,
            snapshot?.isPlaying,
            snapshot?.errorMessage,
            bufferedPercentage,
        )
    }

    fun applyVideoSize(position: Int, width: Int, height: Int, pixelRatio: Float) {
        holders[position]?.applyVideoSize(width, height, pixelRatio)
    }

    inner class VideoViewHolder(
        private val binding: FeedItemVideoBinding,
    ) : RecyclerView.ViewHolder(binding.root), SurfaceHolder.Callback {
        private var boundPosition = RecyclerView.NO_POSITION

        init {
            binding.surfaceView.holder.addCallback(this)
            binding.topBar.applySystemBarsPadding(includeTop = true)
        }

        fun bind(
            item: VideoItem,
            position: Int,
            isCurrent: Boolean,
            playbackSnapshot: PlaybackSnapshot?,
            isConnected: Boolean,
        ) {
            boundPosition = position
            binding.author.text = item.author
            binding.caption.text = item.caption
            binding.avatarText.text = item.avatarText
            binding.videoTags.text = item.tags.joinToString(separator = "  ") { "#$it" }
            binding.videoMeta.text = "${item.qualityLabel}  ·  ${item.durationLabel}  ·  磁盘缓存"
            binding.likeCount.text = "♥\n${item.likeCount}"
            binding.commentCount.text = "●\n${item.commentCount}"
            binding.shareCount.text = "↗\n${item.shareCount}"
            binding.retryButton.setOnClickListener { retryListener(position) }
            binding.playbackStatus.setTextColor(Color.WHITE)
            updatePlaybackState(isCurrent, playbackSnapshot, isConnected)
        }

        fun updatePlaybackState(
            isCurrent: Boolean,
            playbackSnapshot: PlaybackSnapshot?,
            isConnected: Boolean,
        ) {
            binding.retryButton.visibility = View.GONE
            binding.loading.visibility = if (isCurrent) View.VISIBLE else View.GONE
            binding.playbackStatus.visibility = if (isCurrent) View.VISIBLE else View.GONE
            if (!isCurrent) {
                return
            }
            if (!isConnected) {
                binding.playbackStatus.text = "正在连接播放器…"
                return
            }
            val state = playbackSnapshot?.playbackState ?: PlaybackContract.STATE_IDLE
            binding.playbackStatus.text = when (state) {
                PlaybackContract.STATE_BUFFERING -> {
                    val duration = playbackSnapshot?.durationMs ?: 0L
                    val buffered = playbackSnapshot?.bufferedPositionMs ?: 0L
                    val percentage = if (duration > 0L) {
                        (buffered * 100L / duration).coerceIn(0L, 100L)
                    } else {
                        0L
                    }
                    if (percentage > 0L) "缓冲中 $percentage%" else "正在加载高清画面…"
                }
                PlaybackContract.STATE_READY -> if (playbackSnapshot?.isPlaying == true) {
                    ""
                } else {
                    "已暂停"
                }
                PlaybackContract.STATE_ENDED -> "播放结束"
                PlaybackContract.STATE_ERROR -> playbackSnapshot?.errorMessage ?: "播放失败"
                else -> "正在准备视频…"
            }
            val isError = state == PlaybackContract.STATE_ERROR
            val isPlaying = state == PlaybackContract.STATE_READY && playbackSnapshot?.isPlaying == true
            binding.playbackStatus.visibility = if (isPlaying) View.GONE else View.VISIBLE
            binding.loading.visibility = if (
                state == PlaybackContract.STATE_READY || state == PlaybackContract.STATE_ENDED || isError
            ) View.GONE else View.VISIBLE
            binding.retryButton.visibility = if (isError) View.VISIBLE else View.GONE
        }

        fun validSurface(): Surface? = binding.surfaceView.holder.surface.takeIf { it.isValid }

        fun applyVideoSize(width: Int, height: Int, pixelRatio: Float) {
            if (width <= 0 || height <= 0) return
            binding.surfaceView.post {
                val viewWidth = binding.pageRoot.width.toFloat()
                val viewHeight = binding.pageRoot.height.toFloat()
                if (viewWidth <= 0f || viewHeight <= 0f) return@post
                val safePixelRatio = pixelRatio.takeIf { it > 0f } ?: 1f
                val videoRatio = width * safePixelRatio / height
                val viewRatio = viewWidth / viewHeight
                val targetWidth: Int
                val targetHeight: Int
                if (videoRatio > viewRatio) {
                    targetHeight = viewHeight.toInt()
                    targetWidth = (viewHeight * videoRatio).toInt()
                } else {
                    targetWidth = viewWidth.toInt()
                    targetHeight = (viewWidth / videoRatio).toInt()
                }
                binding.surfaceView.layoutParams = FrameLayout.LayoutParams(
                    targetWidth,
                    targetHeight,
                    Gravity.CENTER,
                )
            }
        }

        fun recycle() {
            boundPosition = RecyclerView.NO_POSITION
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            if (boundPosition != RecyclerView.NO_POSITION) {
                surfaceListener.onSurfaceAvailable(boundPosition, holder.surface)
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (boundPosition != RecyclerView.NO_POSITION) {
                surfaceListener.onSurfaceDestroyed(boundPosition)
            }
        }
    }
}
