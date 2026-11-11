package com.example.belltest.lab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.belltest.base.BaseViewBindingActivity
import com.example.belltest.lab.databinding.LabActivityRxLearningBinding
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class RxLearningActivity : BaseViewBindingActivity<LabActivityRxLearningBinding>() {
    private var singleDisposable: Disposable? = null
    private var compositeDisposable = CompositeDisposable()

    data class Tick(val number: Int, val sourceThread: String)

    override fun inflateBinding(inflater: LayoutInflater): LabActivityRxLearningBinding =
        LabActivityRxLearningBinding.inflate(inflater)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.btnStartSingle.setOnClickListener { startSingleTask(failAt = null) }
        binding.btnStartError.setOnClickListener { startSingleTask(failAt = 4) }
        binding.btnStopSingle.setOnClickListener {
            val disposable = singleDisposable
            if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
                binding.tvSingleStatus.text = "单任务：已取消"
            } else {
                appendLog("单任务 → 没有正在运行的订阅")
            }
        }
        binding.btnStartGroup.setOnClickListener { startGroupTasks() }
        binding.btnClearGroup.setOnClickListener {
            compositeDisposable.clear()
            updateGroupStatus("clear() 完成，可再次添加订阅")
            appendLog("CompositeDisposable → clear()")
        }
        binding.btnDisposeGroup.setOnClickListener {
            compositeDisposable.dispose()
            updateGroupStatus("dispose() 完成，容器已终止")
            appendLog("CompositeDisposable → dispose()")
        }
        binding.btnResetGroup.setOnClickListener {
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            updateGroupStatus("已创建新容器")
            appendLog("CompositeDisposable → 重新创建")
        }
        binding.btnClearLog.setOnClickListener { binding.tvLog.text = "" }
        updateGroupStatus("等待操作")
    }

    private fun createObservable(name: String, delayMs: Long, failAt: Int?): Observable<Tick> =
        Observable.create<Tick> { emitter ->
            for (index in 1..8) {
                if (emitter.isDisposed) return@create
                try {
                    Thread.sleep(delayMs)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return@create
                }
                if (emitter.isDisposed) return@create
                if (failAt == index) {
                    emitter.onError(IllegalStateException("$name 在第 $index 次模拟失败"))
                    return@create
                }
                emitter.onNext(Tick(index, Thread.currentThread().name))
            }
            if (!emitter.isDisposed) emitter.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    private fun subscribeTask(
        name: String,
        delayMs: Long,
        failAt: Int? = null,
        register: (Disposable) -> Unit,
    ) {
        createObservable(name, delayMs, failAt).subscribe(object : Observer<Tick> {
            override fun onSubscribe(disposable: Disposable) {
                register(disposable)
                appendLog("$name → onSubscribe()")
            }

            override fun onNext(tick: Tick) {
                appendLog(
                    "$name → onNext(${tick.number})\n" +
                        "生产线程：${tick.sourceThread}\n接收线程：${Thread.currentThread().name}",
                )
                if (name == "单任务") {
                    binding.tvSingleStatus.text = "单任务：收到第 ${tick.number} 次数据"
                } else {
                    updateGroupStatus("$name 收到第 ${tick.number} 次数据")
                }
            }

            override fun onError(error: Throwable) {
                appendLog("$name → onError(${error.message})")
                if (name == "单任务") binding.tvSingleStatus.text = "单任务：发生错误"
                else updateGroupStatus("$name 发生错误")
            }

            override fun onComplete() {
                appendLog("$name → onComplete()")
                if (name == "单任务") binding.tvSingleStatus.text = "单任务：正常完成"
                else updateGroupStatus("$name 正常完成")
            }
        })
    }

    private fun startSingleTask(failAt: Int?) {
        singleDisposable?.dispose()
        binding.tvSingleStatus.text = "单任务：运行中"
        subscribeTask("单任务", 1_000L, failAt) { singleDisposable = it }
    }

    private fun startGroupTasks() {
        if (compositeDisposable.isDisposed) {
            appendLog("容器已 dispose，请先点击重建容器")
            return
        }
        compositeDisposable.clear()
        for (index in 1..3) {
            subscribeTask("任务$index", 500L + index * 300L) { compositeDisposable.add(it) }
        }
        updateGroupStatus("3 个任务已启动")
    }

    private fun updateGroupStatus(message: String) {
        binding.tvGroupStatus.text =
            "$message\n已登记订阅：${compositeDisposable.size()}\nisDisposed：${compositeDisposable.isDisposed}"
    }

    private fun appendLog(message: String) {
        Log.d(TAG, message)
        binding.tvLog.append("\n$message\n")
    }

    override fun onDestroy() {
        singleDisposable?.dispose()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "RxObserverLab"
    }
}
