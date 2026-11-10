package com.example.belltest.lab

import android.os.SystemClock
import android.os.Trace
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProfilerLab(private val scope: CoroutineScope) {
    private var cpuJob: Job? = null
    private var memoryJob: Job? = null
    private val memoryBlocks = mutableListOf<ByteArray>()
    private val workerThreads = mutableListOf<Thread>()

    fun startCpuPressure() {
        cpuJob?.cancel()
        cpuJob = scope.launch {
            repeat(2) { worker ->
                launch(Dispatchers.Default) {
                    val endTime = SystemClock.elapsedRealtime() + 12_000L
                    var checksum = worker.toLong()
                    while (isActive && SystemClock.elapsedRealtime() < endTime) {
                        for (index in 1..50_000) checksum = checksum * 31 + index
                    }
                    Log.d(TAG, "CPU worker=$worker result=$checksum")
                }
            }
        }
    }

    fun startMemoryPressure() {
        memoryJob?.cancel()
        memoryBlocks.clear()
        memoryJob = scope.launch {
            repeat(6) { index ->
                memoryBlocks += ByteArray(4 * 1024 * 1024).apply { fill((index + 1).toByte()) }
                Log.d(TAG, "已分配 ${(index + 1) * 4} MB")
                delay(800)
            }
        }
    }

    fun startThreadPressure() {
        workerThreads.forEach(Thread::interrupt)
        workerThreads.clear()
        repeat(16) { index ->
            Thread(
                {
                    try {
                        Thread.sleep(15_000)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                },
                "Telemetry-Sleep-$index",
            ).also {
                workerThreads += it
                it.start()
            }
        }
    }

    fun simulateMainThreadBlock() {
        Trace.beginSection("BellTest_Main_Block_200ms")
        try {
            SystemClock.sleep(200)
        } finally {
            Trace.endSection()
        }
    }

    fun startTracedCpuPressure() {
        cpuJob?.cancel()
        cpuJob = scope.launch(Dispatchers.Default) {
            Trace.beginSection("BellTest_BG_CPU_3000ms")
            try {
                val endTime = SystemClock.elapsedRealtime() + 3_000L
                var checksum = 0L
                while (isActive && SystemClock.elapsedRealtime() < endTime) {
                    for (index in 1..50_000) checksum = checksum * 31 + index
                }
                Log.d(TAG, "CPU trace finished: $checksum")
            } finally {
                Trace.endSection()
            }
        }
    }

    fun stopAll() {
        cpuJob?.cancel()
        memoryJob?.cancel()
        workerThreads.forEach(Thread::interrupt)
        workerThreads.clear()
        memoryBlocks.clear()
        Log.d(TAG, "已停止任务并释放内存引用")
    }

    private companion object {
        const val TAG = "ProfilerLab"
    }
}
