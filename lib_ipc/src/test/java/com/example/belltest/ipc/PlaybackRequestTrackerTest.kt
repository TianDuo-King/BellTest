package com.example.belltest.ipc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackRequestTrackerTest {
    @Test
    fun requestIdsIncreaseAndOldSnapshotsAreRejected() {
        val tracker = PlaybackRequestTracker()

        val first = tracker.startRequest("video-a")
        val second = tracker.startRequest("video-b")

        assertTrue(second > first)
        assertFalse(tracker.isCurrent(first, "video-a"))
        assertTrue(tracker.isCurrent(second, "video-b"))
    }

    @Test
    fun surfaceTokenOnlyClearsTheSurfaceThatCreatedIt() {
        val tracker = PlaybackRequestTracker()

        val oldToken = tracker.startSurfaceSession()
        val currentToken = tracker.startSurfaceSession()

        assertFalse(tracker.canClearSurface(oldToken))
        assertTrue(tracker.canClearSurface(currentToken))
    }

    @Test
    fun latestPositionIsRememberedPerMedia() {
        val tracker = PlaybackRequestTracker()
        val requestId = tracker.startRequest("video-a")

        tracker.rememberPosition(requestId, "video-a", 4_200L)

        assertEquals(4_200L, tracker.resumePosition("video-a"))
        assertEquals(0L, tracker.resumePosition("video-b"))
    }
}
