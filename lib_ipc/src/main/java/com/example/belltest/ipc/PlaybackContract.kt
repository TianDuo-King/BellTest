package com.example.belltest.ipc

object PlaybackContract {
    const val SERVICE_CLASS_NAME = "com.example.belltest.player.RemoteVideoPlayerService"

    const val STATE_IDLE = 0
    const val STATE_BUFFERING = 1
    const val STATE_READY = 2
    const val STATE_ENDED = 3
    const val STATE_ERROR = 4

    const val ERROR_NONE = 0
    const val ERROR_NETWORK_OR_SOURCE = 1
    const val ERROR_DECODER = 2
    const val ERROR_UNEXPECTED = 3
}
