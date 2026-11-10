package com.example.belltest.ipc;

import android.view.Surface;
import com.example.belltest.ipc.IVideoPlayerCallback;
import com.example.belltest.ipc.PlaybackSnapshot;

interface IVideoPlayerService {
    oneway void attachSurface(long surfaceToken, in Surface surface);
    oneway void clearSurface(long surfaceToken);
    oneway void prepare(long requestId, String mediaId, String url, boolean playWhenReady);
    oneway void play();
    oneway void pause();
    oneway void seekTo(long positionMs);
    oneway void registerCallback(IVideoPlayerCallback callback);
    oneway void unregisterCallback(IVideoPlayerCallback callback);
    oneway void releasePlayback();

    PlaybackSnapshot getSnapshot();
    int getServicePid();
}
