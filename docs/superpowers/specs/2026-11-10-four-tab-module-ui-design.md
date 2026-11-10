# BellTest 四页模块 UI 调整设计

日期：2026-11-10

## 意图

把当前五入口原型收敛为用户指定的四个顶层页面：`首页 / 精选 / 消息 / 我的`。页面既要能直观看到 UI，也要能看懂它属于哪个 Gradle 模块、调用了哪些下层模块；首页继续优先保证实际网络视频通过 AIDL 在 `:player` 进程播放。

## 目标与约束

- 新建语义清晰的 `module_featured`，不把旧 `module_social` 只做文字改名。
- `首页`：`app → module_feed → lib_ipc → service_player(:player)`，保留竖向真实视频流并显示播放状态、进程名和 PID。
- `精选`：`app → module_featured → lib_model`，以卡片列表展示同一视频目录、封面色块、作者、标题和媒体来源；第一版不在精选页创建第二个播放器。
- `消息`：`app → module_message → lib_resources/lib_base`，展示消息卡片与模块调用说明。
- `我的`：`app → module_user → module_lab`，展示模块总览以及 XTask/RxJava/协程、Profiler、内存泄漏入口。
- 四页都使用 ViewBinding，不使用 `findViewById`。
- 视频首页内容延伸到状态栏后方，但顶部 UI 消费 status bar/display cutout inset；其余三页根容器消费顶部 inset。
- 应用底部导航统一消费 navigation bar/display cutout inset，四页内容始终位于其上方。
- 隐藏页最大生命周期降到 `CREATED`；离开首页释放远端 ExoPlayer 和 Surface。

## 方案选择

考虑过三种方式：

1. 直接把 `module_social` 政名为“精选”：修改最少，但目录、包名和学习语义不一致。
2. 在 `module_feed` 内增加精选 Fragment：代码少，但首页和精选无法体现独立功能模块边界。
3. 新建 `module_featured`：多一个小模块，但结构最清晰，能直接展示 `app → feature → lib_model` 的调用关系。

采用方案 3。旧 `module_social`、`module_publish` 从 active build 中移除，避免 Android Studio 项目树出现不再使用的业务模块。

## 验收

- 底部导航只显示首页、精选、消息、我的。
- 每页 UI 明确显示模块名和调用链。
- 首页能看到视频画面；远端 badge 显示 `:player` PID，播放状态进入 READY/playing。
- Pixel 8 上四页顶部不与状态栏/挖孔重叠，底部导航不进入手势区。
- 切到精选、消息或我的时播放器释放；返回首页重新绑定并恢复播放。
- 单元测试、debug assemble、两个现有仪器测试和新增四页导航测试通过。
