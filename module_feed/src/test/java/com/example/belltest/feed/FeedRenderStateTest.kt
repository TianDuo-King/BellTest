package com.example.belltest.feed

import com.example.belltest.ipc.PlaybackSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedRenderStateTest {
    @Test
    fun progressUpdateOnlyTouchesTheCurrentPage() {
        val state = FeedRenderState(initialPosition = 1)

        val changed = state.update(
            position = 1,
            snapshot = PlaybackSnapshot(mediaId = "video-b", positionMs = 500L),
            connected = true,
        )

        assertEquals(listOf(1), changed)
    }

    @Test
    fun pageChangeTouchesOldAndNewPagesWithoutRebindingTheList() {
        val state = FeedRenderState(initialPosition = 0)

        val changed = state.update(
            position = 1,
            snapshot = null,
            connected = true,
        )

        assertEquals(listOf(0, 1), changed)
        assertEquals(1, state.position)
    }
}
