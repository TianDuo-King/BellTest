package com.example.belltest.feed

data class FeedSelection(
    val requestId: Long,
    val mediaId: String,
    val startPositionMs: Long,
)

class FeedPlaybackCoordinator(private val mediaIds: List<String>) {
    init {
        require(mediaIds.isNotEmpty())
    }

    private var requestSequence = 0L
    private var currentIndex = 0
    private var currentRequestId = 0L
    private val positions = mutableMapOf<String, Long>()

    fun select(index: Int): FeedSelection {
        require(index in mediaIds.indices)
        val changed = index != currentIndex || currentRequestId == 0L
        currentIndex = index
        val mediaId = mediaIds[index]
        if (changed) {
            positions[mediaId] = 0L
        }
        currentRequestId = ++requestSequence
        return FeedSelection(currentRequestId, mediaId, if (changed) 0L else resumePosition(mediaId))
    }

    fun restoreCurrent(): FeedSelection {
        val mediaId = mediaIds[currentIndex]
        currentRequestId = ++requestSequence
        return FeedSelection(currentRequestId, mediaId, resumePosition(mediaId))
    }

    fun accepts(requestId: Long, mediaId: String): Boolean =
        requestId == currentRequestId && mediaId == mediaIds[currentIndex]

    fun rememberPosition(mediaId: String, positionMs: Long) {
        if (mediaId == mediaIds[currentIndex]) {
            positions[mediaId] = positionMs.coerceAtLeast(0L)
        }
    }

    private fun resumePosition(mediaId: String): Long = positions[mediaId] ?: 0L
}
