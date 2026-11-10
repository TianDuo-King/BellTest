package com.example.belltest.ipc

class PlaybackRequestTracker {
    private var requestSequence = 0L
    private var surfaceSequence = 0L
    private var currentRequestId = 0L
    private var currentMediaId = ""
    private var currentSurfaceToken = 0L
    private val positions = mutableMapOf<String, Long>()

    fun startRequest(mediaId: String): Long {
        currentRequestId = ++requestSequence
        currentMediaId = mediaId
        return currentRequestId
    }

    fun isCurrent(requestId: Long, mediaId: String): Boolean =
        requestId == currentRequestId && mediaId == currentMediaId

    fun startSurfaceSession(): Long {
        currentSurfaceToken = ++surfaceSequence
        return currentSurfaceToken
    }

    fun canClearSurface(surfaceToken: Long): Boolean =
        surfaceToken != 0L && surfaceToken == currentSurfaceToken

    fun rememberPosition(requestId: Long, mediaId: String, positionMs: Long) {
        if (isCurrent(requestId, mediaId)) {
            positions[mediaId] = positionMs.coerceAtLeast(0L)
        }
    }

    fun resumePosition(mediaId: String): Long = positions[mediaId] ?: 0L
}
