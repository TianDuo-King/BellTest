package com.example.belltest.lab

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.belltest.base.enableEdgeToEdgeSystemBarPadding
import com.example.belltest.lab.databinding.LabActivityLeakDemoBinding

class LeakDemoActivity : AppCompatActivity() {
    private val memoryPayload = ByteArray(4 * 1024 * 1024).apply { fill(1) }
    private lateinit var binding: LabActivityLeakDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LabActivityLeakDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeSystemBarPadding(binding.root)
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
