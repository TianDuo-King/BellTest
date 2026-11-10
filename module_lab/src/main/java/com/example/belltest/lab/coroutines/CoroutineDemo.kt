package com.example.belltest.lab.coroutines

import com.example.belltest.lab.DemoRunner
import com.example.belltest.lab.DemoScenario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineDemo(private val scope: CoroutineScope) : DemoRunner {
    override val description = "lifecycleScope：delay 可取消，withContext 在后台生成数据，随后回到主线程。"
    private var job: Job? = null

    override fun start(shouldFail: Boolean, onResult: (Result<String>) -> Unit) {
        cancel()
        job = scope.launch {
            try {
                delay(DemoScenario.DELAY_MS)
                val value = withContext(Dispatchers.Default) { DemoScenario.result(shouldFail) }
                onResult(Result.success(value))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                onResult(Result.failure(error))
            } finally {
                if (job === currentCoroutineContext()[Job]) job = null
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        job = null
    }
}
