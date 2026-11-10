package com.example.belltest.lab

interface DemoRunner {
    val description: String
    fun start(shouldFail: Boolean, onResult: (Result<String>) -> Unit)
    fun cancel()
}
