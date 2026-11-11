package com.example.belltest.lab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.belltest.base.BaseViewBindingActivity
import com.example.belltest.lab.databinding.LabActivityLeakDemoBinding

class LeakDemoActivity : BaseViewBindingActivity<LabActivityLeakDemoBinding>() {
    private val memoryPayload = ByteArray(4 * 1024 * 1024).apply { fill(1) }

    override fun inflateBinding(inflater: LayoutInflater): LabActivityLeakDemoBinding =
        LabActivityLeakDemoBinding.inflate(inflater)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.closeButton.setOnClickListener { finish() }
        Log.d(TAG, "创建 Activity，数组大小=${memoryPayload.size}")
    }

    override fun onDestroy() {
        MemoryLeakStore.leakedActivities += this
        Log.d(TAG, "Activity 已销毁，泄漏数量=${MemoryLeakStore.leakedActivities.size}")
        super.onDestroy()
    }

    private companion object {
        const val TAG = "HeapLeakDemo"
    }
}
