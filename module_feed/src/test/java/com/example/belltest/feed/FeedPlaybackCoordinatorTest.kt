package com.example.belltest.feed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedPlaybackCoordinatorTest {
    @Test
    fun changingPageStartsTheNewMediaFromTheBeginning() {
        val coordinator = FeedPlaybackCoordinator(listOf("one", "two"))
        coordinator.rememberPosition("one", 3_000L)

        val selection = coordinator.select(1)

        assertEquals("two", selection.mediaId)
        assertEquals(0L, selection.startPositionMs)
    }

    @Test
    fun reenteringFeedResumesTheCurrentMedia() {
        val coordinator = FeedPlaybackCoordinator(listOf("one", "two"))
        coordinator.select(1)
        coordinator.rememberPosition("two", 7_500L)

        val selection = coordinator.restoreCurrent()

        assertEquals("two", selection.mediaId)
        assertEquals(7_500L, selection.startPositionMs)
    }

    @Test
    fun onlyCurrentMediaCallbacksAreAccepted() {
        val coordinator = FeedPlaybackCoordinator(listOf("one", "two"))
        val first = coordinator.select(0)
        val second = coordinator.select(1)

        assertFalse(coordinator.accepts(first.requestId, first.mediaId))
        assertTrue(coordinator.accepts(second.requestId, second.mediaId))
    }
}
