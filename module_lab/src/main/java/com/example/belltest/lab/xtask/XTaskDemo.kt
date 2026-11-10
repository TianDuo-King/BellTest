package com.example.belltest.lab.xtask

import com.example.belltest.lab.DemoRunner
import com.example.belltest.lab.DemoScenario
import com.xuexiang.xtask.XTask
import com.xuexiang.xtask.api.TaskChainEngine
import com.xuexiang.xtask.core.ITaskChainEngine
import com.xuexiang.xtask.core.ThreadType
import com.xuexiang.xtask.core.param.ITaskResult
import com.xuexiang.xtask.core.step.impl.TaskChainCallbackAdapter
import com.xuexiang.xtask.core.step.impl.TaskCommand

class XTaskDemo : DemoRunner {
    override val description = "任务链：后台步骤执行加载，主线程步骤更新页面；链回调处理错误。"
    private var chain: TaskChainEngine? = null

    override fun start(shouldFail: Boolean, onResult: (Result<String>) -> Unit) {
        cancel()
        val engine = XTask.getTaskChain()
        chain = engine
        engine.addTask(XTask.getTask(object : TaskCommand() {
            override fun run() {
                Thread.sleep(DemoScenario.DELAY_MS)
                getTaskParam().put("result", DemoScenario.result(shouldFail))
            }
        }, ThreadType.ASYNC_IO))
        engine.addTask(XTask.getTask(object : TaskCommand() {
            override fun run() {
                onResult(Result.success(getTaskParam().get("result") as String))
            }
        }, ThreadType.MAIN))
        engine.setTaskChainCallback(object : TaskChainCallbackAdapter() {
            override fun onTaskChainCompleted(engine: ITaskChainEngine, result: ITaskResult) {
                if (chain === engine) chain = null
            }

            override fun onTaskChainError(engine: ITaskChainEngine, result: ITaskResult) {
                if (chain === engine) {
                    chain = null
                    onResult(Result.failure(IllegalStateException(result.message ?: "XTask 任务失败")))
                }
            }

            override fun onTaskChainCancelled(engine: ITaskChainEngine) {
                if (chain === engine) chain = null
            }
        }).start(false)
    }

    override fun cancel() {
        chain?.cancel()
        chain = null
    }
}
