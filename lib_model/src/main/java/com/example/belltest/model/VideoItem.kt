package com.example.belltest.model

data class VideoItem(
    val id: String,
    val author: String,
    val caption: String,
    val mediaUrl: String,
    val accentColor: Long,
    val avatarText: String = "B",
    val tags: List<String> = emptyList(),
    val qualityLabel: String = "HD",
    val durationLabel: String = "精选片段",
    val likeCount: String = "1.2w",
    val commentCount: String = "328",
    val shareCount: String = "分享",
)
