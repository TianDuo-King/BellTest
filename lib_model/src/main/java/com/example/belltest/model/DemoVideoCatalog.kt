package com.example.belltest.model

object DemoVideoCatalog {
    val items = listOf(
        VideoItem(
            id = "big-buck-bunny",
            author = "@BellTest · Remote Player",
            caption = "Big Buck Bunny · 网络加载、解码与播放都在 :player 进程",
            mediaUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            accentColor = 0xFFED2B54,
        ),
        VideoItem(
            id = "android-screens-10s",
            author = "@AndroidX Media3",
            caption = "向上滑动切换视频 · Surface 通过 AIDL 交给远端播放器",
            mediaUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
            accentColor = 0xFF25F4EE,
        ),
        VideoItem(
            id = "android-screens-25s",
            author = "@Binder Lab",
            caption = "离开首页立即释放 ExoPlayer，返回后从最近进度恢复",
            mediaUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-25s.mp4",
            accentColor = 0xFFFE2C55,
        ),
    )
}
