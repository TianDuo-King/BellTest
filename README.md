# BellTest 模块协作链路与思维导图

> 分析对象：GitHub `TianDuo-King/BellTest`，`main` 分支，2026-11-10。依据 `settings.gradle.kts`、各模块 `build.gradle.kts` 与关键 Kotlin/AIDL 源码。

## 一、实际运行链路图

```mermaid
flowchart TB
    U[用户] --> APP[app / MainActivity]
    APP --> NAV{底部导航 / 四个 Tab}

    subgraph MAIN[主进程]
      NAV -->|首页| FEED[module_feed / FeedFragment]
      NAV -->|精选| FEATURED[module_featured / FeaturedFragment]
      NAV -->|消息| MSG[module_message / MessageFragment]
      NAV -->|我的| USER[module_user / UserFragment]

      MODEL[lib_model / DemoVideoCatalog + VideoItem]
      UI[FeedPagerAdapter / ViewPager2 + SurfaceView]
      IPC[lib_ipc / RemotePlayerClient]
      LAB[module_lab / 异步、Rx、Profiler、泄漏实验]

      FEED -->|读取视频数据| MODEL
      FEATURED -->|读取同一目录| MODEL
      FEED -->|构建竖向视频列表| UI
      UI -->|Surface 创建、销毁| FEED
      FEED -->|bind、prepare、attachSurface| IPC
      IPC -.->|PlaybackSnapshot、视频尺寸回调| FEED
      FEED -.->|更新播放状态| UI
      FEATURED -.->|FragmentResult：请求切回首页| APP
      USER -->|Intent 打开实验 Activity| LAB
    end

    subgraph REMOTE[独立进程 :player]
      SERVICE[service_player / RemoteVideoPlayerService]
      PLAYER[Media3 / ExoPlayer]
      SERVICE --> PLAYER
    end

    IPC -->|Binder / AIDL：IVideoPlayerService| SERVICE
    SERVICE -.->|AIDL：IVideoPlayerCallback| IPC
    UI -.->|Surface 经 IPC 交付| IPC
    PLAYER -.->|视频渲染到传入的 Surface| UI
```

**说明**：`module_feed` 通过 `lib_ipc` 而非直接访问 `ExoPlayer`。`service_player` 通过 Gradle 依赖被打包到 `app`，通过 AndroidManifest 声明为 `android:process=":player"` 的 Service。主进程与远程播放器经 Binder/AIDL 双向通信。首页离开 `onStop` 时解绑并释放远程播放资源；返回时重新绑定、尝试恢复播放。

## 二、Gradle 依赖关系图（箭头表示 implementation 依赖）

```mermaid
flowchart TB
    APP[app] --> BASE[lib_base]
    APP --> RES[lib_resources]
    APP --> FEED[module_feed]
    APP --> FEATURED[module_featured]
    APP --> MESSAGE[module_message]
    APP --> USER[module_user]
    APP --> PLAYER[service_player]

    FEED --> BASE
    FEED --> RES
    FEED --> MODEL[lib_model]
    FEED --> IPC[lib_ipc]

    FEATURED --> BASE
    FEATURED --> RES
    FEATURED --> MODEL

    MESSAGE --> BASE
    MESSAGE --> RES
    USER --> BASE
    USER --> RES
    USER --> LAB[module_lab]
    LAB --> BASE
    LAB --> RES
    PLAYER --> IPC
```

`lib_base` 的 `api` 暴露 AndroidX Core / AppCompat / Fragment 依赖；`lib_resources` 的 `api` 暴露 Material。`app` 对 `lib_ipc` 另有 `androidTestImplementation`，未在正式运行依赖箭头中展示。`module_publish`、`module_social` 有仓库目录及 Gradle 文件，但没有加入当前 `settings.gradle.kts`。

## 三、模块思维导图

```mermaid
mindmap
  root((BellTest))
    应用壳
      app
        MainActivity
        四个底部 Tab
        Fragment hide/show
    业务功能模块
      module_feed 首页
        竖向 ViewPager2
        FeedPlaybackCoordinator
        RemotePlayerClient
      module_featured 精选
        视频卡片 RecyclerView
        FragmentResult 返回首页
      module_message 消息
        消息展示页
      module_user 我的
        实验入口
      module_lab 技术实验
        XTask、RxJava、协程
        Profiler 与内存泄漏
    共享基础模块
      lib_base
        Insets 工具与 AndroidX 基础依赖
      lib_resources
        共享资源与 Material
      lib_model
        VideoItem
        DemoVideoCatalog
    播放与进程间通信
      lib_ipc
        AIDL 接口
        RemotePlayerClient
        PlaybackSnapshot
        请求与 Surface Token
      service_player
        独立 player 进程
        RemoteVideoPlayerService
        Media3 ExoPlayer
    已存在但未纳入构建
      module_publish
      module_social
```

## 四、当前链路中值得注意的细节

1. 精选卡片点击时通过 `setFragmentResult()` 向 `MainActivity` 传递视频 `mediaId`，但 `MainActivity` 目前仅切回首页，**没有消费 `mediaId` 并定位到对应视频**。
2. `app` 通过直接实现依赖引入 `service_player`，但真正的播放调用链是 `module_feed → lib_ipc → service_player`，不能把 Gradle 打包依赖与运行时方法调用混为一谈。
3. `module_publish`、`module_social` 有实际源代码目录，但不属于当前 Gradle 构建包含模块。
4. 主进程与远程进程传递的播放状态为 `PlaybackSnapshot`（Parcelable），跨进程视频输出使用 `Surface`。
5. `module_user → module_lab` 是直接依赖：由 `UserFragment` 使用显式 Intent 打开实验 Activity，而不是通过独立导航协议解耦。

## 五、代码依据

- [模块清单 settings.gradle.kts](https://github.com/TianDuo-King/BellTest/blob/main/settings.gradle.kts)
- [app 依赖](https://github.com/TianDuo-King/BellTest/blob/main/app/build.gradle.kts)
- [主导航 MainActivity.kt](https://github.com/TianDuo-King/BellTest/blob/main/app/src/main/java/com/example/belltest/MainActivity.kt)
- [首页 FeedFragment.kt](https://github.com/TianDuo-King/BellTest/blob/main/module_feed/src/main/java/com/example/belltest/feed/FeedFragment.kt)
- [精选 FeaturedFragment.kt](https://github.com/TianDuo-King/BellTest/blob/main/module_featured/src/main/java/com/example/belltest/featured/FeaturedFragment.kt)
- [IPC RemotePlayerClient.kt](https://github.com/TianDuo-King/BellTest/blob/main/lib_ipc/src/main/java/com/example/belltest/ipc/RemotePlayerClient.kt)
- [播放器 RemoteVideoPlayerService.kt](https://github.com/TianDuo-King/BellTest/blob/main/service_player/src/main/java/com/example/belltest/player/RemoteVideoPlayerService.kt)
- [播放器独立进程声明](https://github.com/TianDuo-King/BellTest/blob/main/service_player/src/main/AndroidManifest.xml)
- [我的 UserFragment.kt](https://github.com/TianDuo-King/BellTest/blob/main/module_user/src/main/java/com/example/belltest/user/UserFragment.kt)
