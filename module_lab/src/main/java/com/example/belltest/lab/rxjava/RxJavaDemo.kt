package com.example.belltest.lab.rxjava

import com.example.belltest.lab.DemoRunner
import com.example.belltest.lab.DemoScenario
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RxJavaDemo : DemoRunner {
    override val description = "Single：IO 调度器执行加载，observeOn 切回主线程；dispose 取消订阅。"
    private var disposable: Disposable? = null

    override fun start(shouldFail: Boolean, onResult: (Result<String>) -> Unit) {
        cancel()
        disposable = Single.timer(DemoScenario.DELAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
            .map { DemoScenario.result(shouldFail) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { value ->
                    disposable = null
                    onResult(Result.success(value))
                },
                { error ->
                    disposable = null
                    onResult(Result.failure(error))
                },
            )
    }

    override fun cancel() {
        disposable?.dispose()
        disposable = null
    }
}
