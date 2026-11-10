package com.example.belltest.lab

object DemoScenario {
    const val DELAY_MS = 1_500L

    fun result(shouldFail: Boolean): String {
        if (shouldFail) throw IllegalStateException("模拟请求失败，请重试")
        return "加载完成：3 条示例数据\n后台线程：${Thread.currentThread().name}"
    }
}
