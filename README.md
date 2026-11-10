# XTask、RxJava、协程页面示例

这是一个 Android 单页面对照示例。页面上的 XTask、RxJava、协程三个选项执行相同的模拟加载：等待约 1.5 秒，返回结果或抛出同一条错误。你可以在等待期间点“取消当前任务”，或立即切换方案。页面会显示后台执行线程和页面回调线程。

## 代码位置

| 方案 | 实现 | 核心写法 |
| --- | --- | --- |
| XTask | `app/src/main/java/com/example/belltest/xtask/XTaskDemo.kt` | `TaskChainEngine` + 后台任务步骤 + 主线程任务步骤 |
| RxJava | `app/src/main/java/com/example/belltest/rxjava/RxJavaDemo.kt` | `Single.timer` + `map` + `observeOn` + `Disposable` |
| 协程 | `app/src/main/java/com/example/belltest/coroutines/CoroutineDemo.kt` | `lifecycleScope.launch` + `delay` + `withContext` + `Job` |

`MainActivity.kt` 只负责同一套页面状态更新。`DemoScenario.kt` 定义共用的等待时长、成功结果和失败条件。这样切换方案时，比较的是异步写法，而不是不同业务逻辑。页面用请求编号拦截取消后或切换后到达的旧回调。

## 如何比较

1. 依次选择三个方案，点击“开始加载”，观察加载、结果及线程信息。
2. 点击“模拟失败”，看各方案如何把错误传回主线程。
3. 在加载完成前点“取消当前任务”，等超过 1.5 秒，确认旧结果没有覆盖“已取消”。
4. 在加载中切换方案并重新开始，确认页面只显示最新一次的结果。

| 关注点 | XTask | RxJava | 协程 |
| --- | --- | --- | --- |
| 主线程切换 | 指定 `ThreadType.MAIN` 步骤 | `observeOn(AndroidSchedulers.mainThread())` | `launch` 从 `lifecycleScope` 开始，`withContext` 返回后恢复主线程 |
| 取消 | `TaskChainEngine.cancel()` | `Disposable.dispose()` | `Job.cancel()`，Activity 销毁时 `lifecycleScope` 也会取消 |
| 错误 | 任务链错误回调 | `subscribe` 的错误回调 | `try/catch`，单独保留 `CancellationException` |
| 适合的场景 | 多步骤、有依赖关系的任务流程 | 已使用响应式流，或需要丰富的流组合操作 | Kotlin 页面上的一次性异步操作 |

**对这个单页面加载例子，我会选协程。** 顺序代码较短，取消和 Activity 生命周期关系清楚。已有 RxJava 数据流的项目可以沿用 RxJava；多步骤初始化或需要重排任务依赖时，XTask 的任务链更有表达力。这个示例只比较代码结构和行为，1.5 秒是人为等待，不能用它判断真实性能。

依赖为 XTask 1.0.2、RxJava 3.1.12、RxAndroid 3.0.2 和 kotlinx.coroutines Android 1.7.3。XTask 用法参考[官方说明](https://github.com/xuexiangjys/XTask)，RxAndroid 主线程调度参考[官方仓库](https://github.com/ReactiveX/RxAndroid)。
