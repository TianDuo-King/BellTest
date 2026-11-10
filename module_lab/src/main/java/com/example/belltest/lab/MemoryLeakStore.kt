package com.example.belltest.lab

object MemoryLeakStore {
    val leakedActivities = mutableListOf<LeakDemoActivity>()

    fun clear() {
        leakedActivities.clear()
    }
}
