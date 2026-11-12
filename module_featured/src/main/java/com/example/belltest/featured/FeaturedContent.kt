package com.example.belltest.featured

import com.example.belltest.model.VideoItem
import java.net.URI

data class FeaturedCard(
    val id: String,
    val title: String,
    val author: String,
    val sourceHost: String,
    val accentColor: Long,
    val tags: List<String>,
    val qualityLabel: String,
    val durationLabel: String,
    val popularity: String,
)

object FeaturedContent {
    fun from(videos: List<VideoItem>): List<FeaturedCard> = videos.map { video ->
        FeaturedCard(
            id = video.id,
            title = video.caption,
            author = video.author,
            sourceHost = runCatching { URI(video.mediaUrl).host }.getOrNull().orEmpty()
                .ifBlank { "未知媒体源" },
            accentColor = video.accentColor,
            tags = video.tags,
            qualityLabel = video.qualityLabel,
            durationLabel = video.durationLabel,
            popularity = "${video.likeCount} 喜欢 · ${video.commentCount} 评论",
        )
    }
}
