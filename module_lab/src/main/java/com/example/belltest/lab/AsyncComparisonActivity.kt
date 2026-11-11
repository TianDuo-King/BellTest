package com.example.belltest.lab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.belltest.base.BaseViewBindingActivity
import com.example.belltest.lab.coroutines.CoroutineDemo
import com.example.belltest.lab.databinding.LabActivityAsyncComparisonBinding
import com.example.belltest.lab.rxjava.RxJavaDemo
import com.example.belltest.lab.xtask.XTaskDemo

class AsyncComparisonActivity : BaseViewBindingActivity<LabActivityAsyncComparisonBinding>() {
    private lateinit var runners: Map<Int, DemoRunner>
    private lateinit var currentRunner: DemoRunner
    private lateinit var profilerLab: ProfilerLab
    private var requestId = 0
    private var loading = false

    override fun inflateBinding(inflater: LayoutInflater): LabActivityAsyncComparisonBinding =
        LabActivityAsyncComparisonBinding.inflate(inflater)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        runners = mapOf(
            binding.optionXtask.id to XTaskDemo(),
            binding.optionRxjava.id to RxJavaDemo(),
            binding.optionCoroutines.id to CoroutineDemo(lifecycleScope),
        )
        currentRunner = runners.getValue(binding.optionXtask.id)
        profilerLab = ProfilerLab(lifecycleScope)
        showSelectedRunner()

        binding.implementationGroup.setOnCheckedChangeListener { _, checkedId ->
            currentRunner.cancel()
            requestId++
            currentRunner = runners.getValue(checkedId)
            loading = false
            binding.progress.visibility = View.GONE
            binding.status.text = "状态：未开始"
            binding.result.text = "点击开始加载或模拟失败"
            binding.callbackThread.text = "页面回调线程：—"
            showSelectedRunner()
        }
        binding.startButton.setOnClickListener { startDemo(shouldFail = false) }
        binding.failButton.setOnClickListener { startDemo(shouldFail = true) }
        binding.cancelButton.setOnClickListener { cancelDemo() }

        binding.startButton.setOnLongClickListener {
            showProfilerActions()
            true
        }
        binding.failButton.setOnLongClickListener {
            startActivity(Intent(this, LeakDemoActivity::class.java))
            true
        }
        binding.cancelButton.setOnLongClickListener {
            val count = MemoryLeakStore.leakedActivities.size
            MemoryLeakStore.clear()
            Toast.makeText(this, "已清理 $count 个 Activity 引用", Toast.LENGTH_SHORT).show()
            true
        }
        binding.optionRxjava.setOnLongClickListener {
            startActivity(Intent(this, RxLearningActivity::class.java))
            true
        }
    }

    private fun showSelectedRunner() {
        binding.implementationDescription.text = currentRunner.description
    }

    private fun startDemo(shouldFail: Boolean) {
        currentRunner.cancel()
        val id = ++requestId
        loading = true
        binding.progress.visibility = View.VISIBLE
        binding.status.text = "状态：加载中"
        binding.result.text = "正在模拟耗时操作…"
        binding.callbackThread.text = "页面回调线程：—"
        currentRunner.start(shouldFail) { outcome ->
            if (id != requestId || isDestroyed) return@start
            loading = false
            binding.progress.visibility = View.GONE
            binding.status.text = if (outcome.isSuccess) "状态：成功" else "状态：失败"
            binding.result.text = outcome.getOrElse { it.message ?: "未知错误" }
            binding.callbackThread.text = "页面回调线程：${Thread.currentThread().name}"
        }
    }

    private fun cancelDemo() {
        if (!loading) return
        currentRunner.cancel()
        requestId++
        loading = false
        binding.progress.visibility = View.GONE
        binding.status.text = "状态：已取消"
        binding.result.text = "旧任务的结果不会再更新页面"
        binding.callbackThread.text = "页面回调线程：${Thread.currentThread().name}"
    }

    private fun showProfilerActions() {
        val actions = arrayOf(
            "CPU 压力测试（12秒）",
            "内存分配测试（24MB）",
            "线程压力测试（16个线程）",
            "停止任务并释放内存",
            "Trace：主线程阻塞 200ms",
            "Trace：后台 CPU 计算 3秒",
        )
        AlertDialog.Builder(this)
            .setTitle("Profiler 性能实验")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> profilerLab.startCpuPressure()
                    1 -> profilerLab.startMemoryPressure()
                    2 -> profilerLab.startThreadPressure()
                    3 -> profilerLab.stopAll()
                    4 -> {
                        profilerLab.simulateMainThreadBlock()
                        binding.status.text = "状态：主线程阻塞实验完成"
                    }
                    5 -> {
                        profilerLab.startTracedCpuPressure()
                        binding.status.text = "状态：后台 CPU Trace 运行中"
                    }
                }
            }
            .show()
    }

    override fun onDestroy() {
        profilerLab.stopAll()
        requestId++
        currentRunner.cancel()
        super.onDestroy()
    }
}
