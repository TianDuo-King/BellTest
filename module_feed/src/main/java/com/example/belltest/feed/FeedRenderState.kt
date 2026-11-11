package com.example.belltest.feed

import com.example.belltest.ipc.PlaybackSnapshot

internal class FeedRenderState(initialPosition: Int = 0) {
    var position: Int = initialPosition
        private set

    var snapshot: PlaybackSnapshot? = null
        private set

    var connected: Boolean = false
        private set

    fun update(
        position: Int,
        snapshot: PlaybackSnapshot?,
        connected: Boolean,
    ): List<Int> {
        val previousPosition = this.position
        this.position = position
        this.snapshot = snapshot
        this.connected = connected
        return if (previousPosition == position) {
            listOf(position)
        } else {
            listOf(previousPosition, position)
        }
    }
}
