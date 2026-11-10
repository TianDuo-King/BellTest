package com.example.belltest.player

import androidx.media3.common.Player
import com.example.belltest.ipc.PlaybackContract
import org.junit.Assert.assertEquals
import org.junit.Test

class Media3PlaybackStateMapperTest {
    @Test
    fun mapsMedia3StatesToStableIpcStates() {
        assertEquals(
            PlaybackContract.STATE_IDLE,
            Media3PlaybackStateMapper.toRemoteState(Player.STATE_IDLE, hasError = false),
        )
        assertEquals(
            PlaybackContract.STATE_BUFFERING,
            Media3PlaybackStateMapper.toRemoteState(Player.STATE_BUFFERING, hasError = false),
        )
        assertEquals(
            PlaybackContract.STATE_READY,
            Media3PlaybackStateMapper.toRemoteState(Player.STATE_READY, hasError = false),
        )
        assertEquals(
            PlaybackContract.STATE_ENDED,
            Media3PlaybackStateMapper.toRemoteState(Player.STATE_ENDED, hasError = false),
        )
    }

    @Test
    fun errorOverridesPlayerState() {
        assertEquals(
            PlaybackContract.STATE_ERROR,
            Media3PlaybackStateMapper.toRemoteState(Player.STATE_READY, hasError = true),
        )
    }
}
