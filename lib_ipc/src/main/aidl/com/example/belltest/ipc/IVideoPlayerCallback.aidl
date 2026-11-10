package com.example.belltest.ipc;

import com.example.belltest.ipc.PlaybackSnapshot;

oneway interface IVideoPlayerCallback {
    void onPlaybackStateChanged(in PlaybackSnapshot snapshot);
    void onVideoSizeChanged(long requestId, String mediaId, int width, int height,
            float pixelWidthHeightRatio);
}
