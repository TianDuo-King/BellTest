package com.example.belltest.base

import android.view.View

data class SystemBarInsetTarget(
    val view: View,
    val includeTop: Boolean = true,
    val includeBottom: Boolean = false,
)
