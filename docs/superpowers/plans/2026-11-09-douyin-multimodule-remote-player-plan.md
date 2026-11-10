# BellTest 多模块与远端播放器实施计划

> 设计依据：`docs/superpowers/specs/2026-11-09-douyin-multimodule-remote-player-design.md`

目标：把现有单模块 BellTest 改造成可运行的抖音式多模块骨架；主进程只负责 ViewBinding UI 和 Surface，真实网络加载、Media3 ExoPlayer、MediaCodec 解码和音频/视频输出在 `:player` 进程。

技术基线：AGP 9.3.3、compile/target SDK 37、min SDK 24、XML + ViewBinding、Media3 1.11.1。

## Task 1：建立模块和版本目录

涉及文件：

- 修改 `settings.gradle.kts`
- 修改根 `build.gradle.kts`
- 修改 `gradle/libs.versions.toml`
- 新建各模块的 `build.gradle.kts`、Manifest 和基础资源

步骤：

1. 注册 `lib_base`、`lib_resources`、`lib_model`、`lib_ipc`、`service_player`、`module_feed`、`module_social`、`module_publish`、`module_message`、`module_user`、`module_lab`。
2. 增加 Android library 插件、Fragment、ViewPager2、Lifecycle、Media3 等版本别名。
3. 所有 UI 模块打开 ViewBinding，统一 Java 11、minSdk 24 和 compileSdk 37。
4. 执行 Gradle projects/配置检查，先解决模块脚本问题。

## Task 2：先写公共纯逻辑测试，再实现模型和 IPC 状态

涉及文件：

- 新建 `lib_ipc/src/test/.../PlaybackStateMapperTest.kt`
- 新建 `lib_ipc/src/main/.../PlaybackContract.kt`
- 新建 `lib_ipc/src/main/.../PlaybackSnapshot.kt`
- 新建 AIDL 文件 `IVideoPlayerService.aidl`、`IVideoPlayerCallback.aidl`、`PlaybackSnapshot.aidl`
- 新建 `lib_model/src/main/.../VideoItem.kt` 和 demo catalog

步骤：

1. 写失败测试，约束 IPC 状态是自有常量并能描述 idle/buffering/ready/ended/error。
2. 实现稳定 IPC 常量和 Parcelable 快照。
3. 定义带 `requestId`、`surfaceToken` 的 AIDL 合同。
4. 建立三条集中维护的官方公开测试媒体数据。
5. 运行 `lib_ipc` 单元测试。

## Task 3：实现远端播放器客户端状态机

涉及文件：

- 新建 `lib_ipc/src/test/.../PlaybackRequestTrackerTest.kt`
- 新建 `lib_ipc/src/main/.../PlaybackRequestTracker.kt`
- 新建 `lib_ipc/src/main/.../RemotePlayerClient.kt`

步骤：

1. 先写失败测试，覆盖递增 request/surface token、过期回调过滤和最近位置保存。
2. 实现纯 Kotlin tracker。
3. 实现 ServiceConnection、AIDL callback、DeathRecipient、显式 ComponentName 和主线程事件分发。
4. 功能层只接收连接状态与 PlaybackSnapshot，不操作裸 Binder。
5. 运行 `lib_ipc` 测试。

## Task 4：实现 `:player` 远端 Service

涉及文件：

- 新建 `service_player/src/test/.../Media3PlaybackStateMapperTest.kt`
- 新建 `service_player/src/main/.../Media3PlaybackStateMapper.kt`
- 新建 `service_player/src/main/.../RemoteVideoPlayerService.kt`
- 新建 `service_player/src/main/AndroidManifest.xml`

步骤：

1. 先写 Media3 状态映射失败测试。
2. 实现映射器，不把 Media3 常量泄漏到 IPC。
3. 在 `android:process=":player"`、`exported=false` 的 bound service 中创建唯一 ExoPlayer。
4. 所有 Binder 命令投递到远端主 Handler；用 `RemoteCallbackList` 分发快照。
5. 使用 token 防止过期 clearSurface；每 500ms 更新进度。
6. `releasePlayback` 与 `onDestroy` 幂等释放播放器、Surface 引用和回调。
7. 编译并运行 service 单元测试。

## Task 5：迁移现有学习实验到 `module_lab`

涉及文件：

- 从 `app/src/main/java` 迁移 DemoRunner、DemoScenario、异步实现、Profiler 与泄漏实验
- 从 `app/src/main/res/layout` 迁移两个实验布局
- 新建 `module_lab` Manifest

步骤：

1. 将原 MainActivity 改名为 `AsyncComparisonActivity`，更新包名和 binding。
2. 保留 XTask、RxJava2/3、协程依赖在 `module_lab`。
3. 把程序化创建 UI 的 LeakDemoActivity 改成 ViewBinding 布局，消除 `findViewById`/裸 View 组装。
4. 确保实验 Activity 能由 `module_user` 打开。
5. 编译 `module_lab`。

## Task 6：实现短视频 Feed

涉及文件：

- 新建 `module_feed/src/test/.../FeedPlaybackCoordinatorTest.kt`
- 新建 `FeedPlaybackCoordinator.kt`
- 新建 `FeedFragment.kt`、`FeedPagerAdapter.kt`
- 新建 feed Fragment/item XML 和资源

步骤：

1. 先写失败测试，覆盖当前条目、requestId 过滤、最近位置恢复和切页从头播放。
2. 实现 coordinator。
3. 用竖向 ViewPager2 + 每页 SurfaceView 展示 demo catalog。
4. SurfaceHolder 回调生成 token，attach/clear 只作用于当前页。
5. Feed `onStart` 绑定，`onStop` 保存位置并 release/unbind。
6. 展示连接中、缓冲、播放、错误和重试状态。
7. 运行 feed 单元测试并编译模块。

## Task 7：实现占位功能、用户入口和应用壳

涉及文件：

- 新建 `module_social`、`module_publish`、`module_message`、`module_user` Fragment 与布局
- 重写 `app/src/main/java/com/example/belltest/MainActivity.kt`
- 重写 `app/src/main/res/layout/activity_main.xml`
- 更新 `app/src/main/AndroidManifest.xml`、主题、颜色、菜单和 strings

步骤：

1. 建立五个入口的暗色抖音式底部导航。
2. Fragment show/hide 时把隐藏项最大生命周期降为 `CREATED`，保证 Feed `onStop`。
3. 使用 WindowInsetsCompat 适配 status bar、display cutout 和 gesture navigation。
4. “我”页面提供异步对比、RxJava 和内存泄漏实验入口。
5. App 只保留壳依赖，三方实验依赖从 app 移除。

## Task 8：跨进程测试和整体验证

涉及文件：

- 更新 `app/src/androidTest/.../RemotePlayerProcessTest.kt`
- 必要时补充测试专用等待工具

步骤：

1. 仪器测试绑定 Service 并断言远端 PID 与主进程 PID 不同。
2. 执行全工程 unit test 和 debug assemble。
3. 安装到连接设备，启动首页，确认真实 HTTPS 视频能显示和播放。
4. 用 adb 查看 `com.example.belltest:player` PID；切 Tab 后确认播放器释放日志。
5. 验证顶部/底部系统栏、快速翻页、后台返回和实验入口。
6. 记录无法自动化的外网依赖或设备差异，不把网络偶发错误伪报成通过。
