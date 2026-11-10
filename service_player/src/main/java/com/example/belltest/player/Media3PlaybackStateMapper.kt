package com.example.belltest.player

import androidx.media3.common.Player
import com.example.belltest.ipc.PlaybackContract

internal object Media3PlaybackStateMapper {
    fun toRemoteState(playerState: Int, hasError: Boolean): Int {
        if (hasError) return PlaybackContract.STATE_ERROR
        return when (playerState) {
            Player.STATE_BUFFERING -> PlaybackContract.STATE_BUFFERING
            Player.STATE_READY -> PlaybackContract.STATE_READY
            Player.STATE_ENDED -> PlaybackContract.STATE_ENDED
            else -> PlaybackContract.STATE_IDLE
        }
    }
}
