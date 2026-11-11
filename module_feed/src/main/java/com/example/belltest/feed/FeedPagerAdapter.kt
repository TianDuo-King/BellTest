package com.example.belltest.feed

import android.graphics.Color
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.ViewGroup
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
        renderState.update(position, newSnapshot, isConnected).forEach { changedPosition ->
            holders[changedPosition]?.updatePlaybackState(
                isCurrent = changedPosition == renderState.position,
                playbackSnapshot = renderState.snapshot,
                isConnected = renderState.connected,
            )
        }
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
            binding.retryButton.setOnClickListener { retryListener(position) }
            binding.playbackStatus.setTextColor(Color.WHITE)
            updatePlaybackState(isCurrent, playbackSnapshot, isConnected)
        }

        fun updatePlaybackState(
            isCurrent: Boolean,
            playbackSnapshot: PlaybackSnapshot?,
            isConnected: Boolean,
        ) {
            binding.retryButton.visibility = android.view.View.GONE
            binding.loading.visibility = if (isCurrent) android.view.View.VISIBLE else android.view.View.GONE
            if (!isCurrent) {
                binding.playbackStatus.text = "向上滑动播放"
                return
            }
            if (!isConnected) {
                binding.playbackStatus.text = "正在连接 :player 进程…"
                return
            }
            val state = playbackSnapshot?.playbackState ?: PlaybackContract.STATE_IDLE
            binding.playbackStatus.text = when (state) {
                PlaybackContract.STATE_BUFFERING -> "远端缓冲中"
                PlaybackContract.STATE_READY -> if (playbackSnapshot?.isPlaying == true) {
                    "远端播放中 · ${playbackSnapshot.positionMs / 1_000}s"
                } else {
                    "远端已就绪"
                }
                PlaybackContract.STATE_ENDED -> "播放结束"
                PlaybackContract.STATE_ERROR -> playbackSnapshot?.errorMessage ?: "播放失败"
                else -> "远端播放器准备中"
            }
            val isError = state == PlaybackContract.STATE_ERROR
            binding.loading.visibility = if (
                state == PlaybackContract.STATE_READY || state == PlaybackContract.STATE_ENDED || isError
            ) android.view.View.GONE else android.view.View.VISIBLE
            binding.retryButton.visibility = if (isError) android.view.View.VISIBLE else android.view.View.GONE
        }

        fun validSurface(): Surface? = binding.surfaceView.holder.surface.takeIf { it.isValid }

        fun applyVideoSize(width: Int, height: Int, pixelRatio: Float) {
            if (width <= 0 || height <= 0) return
            binding.surfaceView.post {
                val viewWidth = binding.pageRoot.width.toFloat()
                val viewHeight = binding.pageRoot.height.toFloat()
                if (viewWidth <= 0f || viewHeight <= 0f) return@post
                val videoRatio = width * pixelRatio / height
                val viewRatio = viewWidth / viewHeight
                binding.surfaceView.scaleX = if (videoRatio > viewRatio) videoRatio / viewRatio else 1f
                binding.surfaceView.scaleY = if (videoRatio < viewRatio) viewRatio / videoRatio else 1f
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
