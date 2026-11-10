# BellTest 抖音式多模块与跨进程播放器设计

日期：2026-11-09

## 1. 背景与目标

BellTest 当前主要由 `app` 单模块承载页面、异步实验和内存实验。下一阶段要在同一个大项目中搭建一个便于继续学习和扩展的抖音式应用骨架，并把视频播放能力隔离到独立进程。

本次设计目标：

- 采用与参考项目相近的扁平化 `lib_*`、`module_*` 模块命名，但建立单向、可解释的依赖关系。
- `app` 只承担应用壳、底部导航和模块装配。
- 建立首页短视频流、朋友、发布、消息、我的五个产品入口；第一版只完整实现首页播放链路，其余入口提供可运行的占位页。
- 保留现有 XTask、RxJava、协程、内存泄漏与 Profiler 学习代码，并迁移到独立的 `module_lab`。
- 使用 ViewBinding，不使用 `findViewById`。
- 适配状态栏、刘海/挖孔屏和底部手势导航区域。
- 通过自定义 AIDL 控制独立 `:player` 进程中的 Media3 ExoPlayer。
- 网络请求、容器解析、MediaCodec 解码、音频输出以及向主进程 `Surface` 渲染视频，全部由 `:player` 进程中的播放器完成。
- 离开视频流页面时释放远端 ExoPlayer、Surface 和网络资源，并解除绑定。

## 2. 本期不做

- 登录、真实用户体系、真实业务后端、上传和发布流程。
- 点赞、关注、评论、私信的持久化业务实现。
- 后台播放、锁屏控制、系统媒体通知、车机或蓝牙媒体控制。
- DRM、离线下载、多清晰度切换、多播放器并行解码。
- 生产级预加载、磁盘缓存和弱网调度；本期先保证跨进程播放边界正确。
- Jetpack Compose；现有工程继续使用 XML + ViewBinding。
- 为了“看起来模块化”而把所有三方依赖和工具类塞进 `lib_base`。

## 3. 方案选择

### 3.1 采用：自定义 AIDL + 远端 ExoPlayer

主进程拥有页面和 `SurfaceView`，远端进程拥有唯一的 ExoPlayer。主进程通过 AIDL 发送播放命令和 `Surface`，远端通过回调返回播放状态。

该方案直接展示本项目希望学习的内容：Binder、AIDL、跨进程 Parcelable、Surface 跨进程传递、服务生命周期和进程死亡恢复。它也能明确保证“解码和播放不在主进程”。

### 3.2 暂不采用：MediaSessionService + 自定义 Surface 通道

MediaSession 很适合系统媒体控制和后台播放，但 Media3 的跨进程 `MediaController` 不负责远端视频 Surface 操作。本期没有后台媒体控制需求，如果同时引入 MediaSession 和自定义 Surface AIDL，会形成两套控制通道，增加学习噪声。以后需要后台播放或系统控制时，再把 MediaSession 接到同一个远端播放器上。

### 3.3 不采用：远端 Activity 或远端窗口承载视频 UI

让远端进程同时拥有 UI 可以减少 Surface IPC，但页面导航、手势、列表复用和主应用主题会被拆散，不适合短视频主界面的模块边界。

## 4. 模块结构

```text
BellTest
├── app                  # 应用壳、启动页、五栏底部导航、模块装配
├── lib_base             # 少量通用 UI/lifecycle 基础设施，不承载业务
├── lib_resources        # 公共主题、颜色、尺寸、字符串和通用 drawable
├── lib_model            # VideoItem 等跨功能数据模型
├── lib_ipc              # AIDL、Parcelable、远端播放器客户端
├── service_player       # :player 进程的 Service 与 Media3 ExoPlayer
├── module_feed          # 竖向短视频流与播放器 UI
├── module_social        # “朋友”占位功能
├── module_publish       # 中间“发布”占位功能
├── module_message       # “消息”占位功能
├── module_user          # “我”页面及学习实验入口
└── module_lab           # 现有 XTask/RxJava/协程/内存实验
```

所有模块保持在项目根目录，便于在 Android Studio 的 Project 视图中直接观察，结构风格与参考项目一致。

### 4.1 模块职责与依赖

| 模块 | 类型 | 直接依赖 | 责任边界 |
| --- | --- | --- | --- |
| `app` | application | 各 `module_*`、`service_player`、`lib_resources` | Application、MainActivity、底部导航、打包远端 Service |
| `lib_base` | Android library | AndroidX lifecycle/appcompat | 通用 Insets、结果封装等小型基础设施 |
| `lib_resources` | Android library | 无业务模块 | 公共主题和资源 |
| `lib_model` | Android library | 无业务模块 | `VideoItem` 等稳定模型 |
| `lib_ipc` | Android library | Android binder APIs | AIDL 合同、`PlaybackSnapshot`、绑定客户端 |
| `service_player` | Android library | `lib_ipc`、Media3 ExoPlayer | 远端服务、真实网络媒体源、解码、音视频输出 |
| `module_feed` | Android library | `lib_base`、`lib_resources`、`lib_model`、`lib_ipc` | ViewPager2 视频流、Surface 生命周期、播放交互 |
| `module_social` | Android library | `lib_base`、`lib_resources` | 朋友页占位 |
| `module_publish` | Android library | `lib_base`、`lib_resources` | 发布页占位 |
| `module_message` | Android library | `lib_base`、`lib_resources` | 消息页占位 |
| `module_user` | Android library | `lib_base`、`lib_resources`、`module_lab` | 我的页和学习入口 |
| `module_lab` | Android library | `lib_base`、`lib_resources`、XTask/RxJava/协程/lifecycle | 迁入现有实验页面与实现 |

依赖规则：

- 功能模块之间不互相依赖，唯一例外是 `module_user` 需要打开学习页，因此直接依赖 `module_lab` 的公开入口。
- `module_feed` 只依赖 `lib_ipc` 合同，不依赖 `service_player` 实现。
- `service_player` 不依赖任何功能模块，也不访问页面对象。
- `app` 通过依赖 `service_player` 让其 Manifest 和实现进入最终 APK。
- 默认使用 `implementation`；只有确实需要向上暴露的公开类型才使用 `api`。
- 各功能模块使用资源前缀，如 `feed_`、`social_`、`publish_`、`message_`、`user_`、`lab_`。

```text
module_feed ───────> lib_ipc <────── service_player
     │                  │
     ├──> lib_model     └── AIDL contract
     ├──> lib_base
     └──> lib_resources

app ──> module_* + service_player + lib_resources
```

## 5. 应用壳与页面

`app` 新建唯一 Launcher `MainActivity`，使用 ViewBinding 和 FragmentManager 承载五个顶层 Fragment：

1. 首页：`module_feed`
2. 朋友：`module_social`
3. 发布：`module_publish`
4. 消息：`module_message`
5. 我：`module_user`

底部导航在首次访问时创建页面，之后以 show/hide 保留状态。选中页的最大生命周期设为 `RESUMED`，所有隐藏页降到 `CREATED`，确保 Feed 会触发 `onStop` 并释放远端播放器。重新创建 Activity 时使用 FragmentManager 已恢复的实例，避免重复 Fragment。

窗口采用 edge-to-edge：

- 视频首页允许画面延伸到系统栏后方，首页顶部操作区应用 status bar/cutout 的 top inset，底部交互区叠加 navigation bar inset。
- 普通页面的内容容器直接消费上下系统栏 inset。
- 底部导航高度加上 navigation bar/gesture inset，避免按钮贴住或落入手势区域。
- 不硬编码某台手机的状态栏高度。

## 6. 短视频流

`module_feed` 使用竖向 ViewPager2 展示少量 `VideoItem`。每个页面持有自己的 ViewBinding 和 `SurfaceView`，但整个应用只复用远端进程中的一个 ExoPlayer。

页面切换顺序：

1. 清除上一页 Surface。
2. 当前页 Surface 可用后，将 `Surface` 传给远端服务。
3. 对新 `VideoItem` 调用 `prepare(mediaId, url, playWhenReady = true)`。
4. 远端 READY 后隐藏封面/加载态；失败时显示可重试错误态。

首版内置一个集中管理的演示视频清单，使用可公开访问的 HTTPS 测试媒体。清单只在 `lib_model` 或 feed 的 demo repository 中维护，页面不散落 URL。实现计划阶段会从官方公开测试媒体中确定具体 URL 和当前稳定 Media3 版本。网络失败必须显示错误和重试，不伪装成播放成功。

## 7. AIDL 合同

AIDL 位于 `lib_ipc/src/main/aidl/com/example/belltest/ipc/`，Java/Kotlin 包名和 AIDL 目录严格一致。

### 7.1 服务接口

```aidl
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
```

`getServicePid()` 是明确的诊断能力和仪器测试断言点，用于证明服务不在主进程。

### 7.2 回调接口

```aidl
oneway interface IVideoPlayerCallback {
    void onPlaybackStateChanged(in PlaybackSnapshot snapshot);
    void onVideoSizeChanged(long requestId, String mediaId, int width, int height,
            float pixelWidthHeightRatio);
}
```

`PlaybackSnapshot` 为手写 Parcelable，包含：

- `requestId`
- `mediaId`
- `playbackState`
- `isPlaying`
- `positionMs`
- `durationMs`
- `bufferedPositionMs`
- `errorCode`
- `errorMessage`

回调在远端使用 `RemoteCallbackList` 管理，客户端必须把 Binder 回调切换回主线程后再更新 UI。

`surfaceToken` 和 `requestId` 都由客户端单调递增生成：远端只允许当前 token 清除当前 Surface，UI 只消费当前 request 的状态和尺寸回调。这避免快速翻页时旧页面的延迟事件破坏新页面。`playbackState` 和 `errorCode` 使用 `lib_ipc` 自己定义的稳定常量，`service_player` 负责把 Media3 状态映射到 IPC 状态，避免合同泄漏播放器实现细节。

## 8. 远端播放器实现

`service_player` 的 Manifest 声明：

```xml
<service
    android:name=".RemoteVideoPlayerService"
    android:exported="false"
    android:process=":player" />
```

服务是纯 bound service，不调用 `startForegroundService`。原因是本期只允许前台页面播放，离开视频页立即释放，不需要通知权限或后台保活。

实现约束：

- `onCreate()` 在 `:player` 进程主线程创建单个 ExoPlayer。
- URL 交给远端 ExoPlayer/Media3 DataSource；主进程不下载视频字节。
- 容器解析、MediaCodec 解码、音频输出和视频渲染均发生在 `:player` 进程。
- Binder 方法可能运行在 Binder 线程池，所有 ExoPlayer 操作统一投递到远端主线程 Handler。
- 同步 `getSnapshot()` 只读取线程安全的不可变快照，不跨线程直接访问 ExoPlayer。
- 播放期间每约 500ms 产生一次位置快照；状态变化立即回调。
- 新 Surface 到达时先清除旧输出，再设置新 Surface；服务只释放自己持有的跨进程 Surface 引用，不销毁主进程的 SurfaceView。
- `releasePlayback()` 停止并释放 ExoPlayer、清除 Handler 回调、清除 Surface 和回调列表。
- `onDestroy()` 再做幂等释放，防止异常解绑留下解码器。

不使用 `Process.killProcess()` 强杀远端进程。释放播放器并解除最后一个绑定后，系统可以回收空的 `:player` 进程；即使它暂时以 cached process 存活，也不再持有播放器资源。

## 9. 客户端与生命周期

`lib_ipc` 提供 `RemotePlayerClient`，封装 `ServiceConnection`、AIDL proxy、callback、`IBinder.DeathRecipient` 和重连状态，功能页不直接处理裸 Binder。合同中固定远端 Service 的完整类名，客户端以 `ComponentName(context.packageName, serviceClassName)` 构造显式 Intent，因此 `lib_ipc` 无需依赖 `service_player`。

Feed 生命周期：

- `onStart`：绑定服务并注册回调。
- 连接成功：校验当前可见条目和 Surface，执行 attach + prepare。
- `surfaceCreated`：attach 当前 Surface。
- `surfaceDestroyed`：clear Surface。
- `onStop`：pause/clear/release，注销回调并解除绑定。
- Binder 死亡：标记 disconnected、显示恢复状态；如果 Feed 仍 STARTED，则重新绑定并恢复当前条目与位置。

用户切换到底部其他 Tab 即视为离开视频流，执行相同释放流程。该选择优先降低资源占用，也让独立进程生命周期更容易观察。

主进程保留当前媒体最近一次快照中的位置。正常切换 Tab、应用进入后台或配置变化导致释放后，重新绑定时重新 prepare 并 seek 到该位置；切换到另一条视频则从头播放。Binder 死亡使用相同恢复逻辑。保留位置不等于保留播放器资源。

## 10. 现有实验代码迁移

现有首页异步对比代码不丢弃：

- 原 `MainActivity` 的实验 UI 改为 `module_lab` 中的 `AsyncComparisonActivity`（或同职责 Fragment）。
- `DemoRunner`、`DemoScenario`、XTask/RxJava/协程实现迁入 `module_lab`。
- `RxLearningActivity`、`LeakDemoActivity`、`MemoryLeakStore`、`ProfilerLab` 一并迁入 `module_lab`。
- XTask、RxJava2/3、RxAndroid、协程和相关 lifecycle 依赖只留在 `module_lab`，不污染 `app` 和播放器模块。
- “我”页面提供明确的“异步方案对比”和“内存/Profiler 实验”入口。

迁移时保持已有 ViewBinding 和系统栏适配行为，不改实验的教学含义。

## 11. 错误处理

- 服务未连接：页面显示“正在连接播放器”，连接后自动恢复。
- Binder 死亡：客户端清空旧 proxy，避免 `DeadObjectException` 循环；页面仍可见时重绑一次。
- 网络/解析/解码错误：远端转换成快照错误字段，Feed 展示错误原因摘要与重试按钮。
- Surface 暂不可用：保留目标媒体但不输出画面；Surface 重建后重新 attach。
- 快速连续滑动：使用 `mediaId` 过滤过期回调，旧视频状态不能覆盖当前页。
- URL 和错误消息不进入通用日志时携带隐私信息；本期演示 URL 不包含令牌。

## 12. 测试与验收

### 12.1 自动化测试

- `module_feed` 单元测试：当前页变化、过期 `mediaId` 回调过滤、连接恢复和生命周期命令序列。
- `service_player` 单元测试：Media3 状态到 `PlaybackSnapshot` 的映射、错误映射、幂等释放。
- 仪器测试：绑定服务后断言 `getServicePid() != Process.myPid()`。
- 仪器测试：注册回调、发送基本命令、解绑后无崩溃。
- 全工程执行各模块 unit test、lint/assemble 中与当前环境兼容的任务。

真实网络视频 READY 受外网状态影响，不作为唯一的 CI 成败条件；它作为设备集成验收项，并保留明确的失败 UI。

### 12.2 设备验收

- 启动进入抖音式首页，视频通过 HTTPS 实际加载并能播放声音和画面。
- 上下滑动切换条目，旧画面不残留，当前条目自动播放。
- 通过 Android Studio/adb 观察到 `com.example.belltest:player` 独立 PID。
- 播放时主进程没有创建 ExoPlayer/MediaCodec 实例；相关解码日志和资源位于远端 PID。
- 切换到其他底部 Tab 后，远端播放器释放；返回 Feed 后重新绑定和播放。
- 旋转/重建、后台再返回、Surface 销毁重建不崩溃。
- Pixel 8 等全面屏设备上，顶部控件和底部导航不与系统栏重叠。
- “我”页面能进入迁移后的 XTask/RxJava/协程与内存实验。

## 13. 实施边界

实现应按以下顺序推进：

1. 建立公共模块、功能占位模块和依赖规则。
2. 迁移现有实验，确保旧功能仍可编译运行。
3. 以测试驱动建立 AIDL 合同与客户端状态机。
4. 实现远端服务和 Media3 播放器。
5. 实现 Feed、Surface 生命周期与竖向切换。
6. 接入应用壳、系统栏适配并做设备验证。

Media3 的精确版本、演示媒体 URL、颜色和文案属于实现计划中的可替换细节，不改变上述进程与模块边界。
