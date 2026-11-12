package com.example.belltest.base

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach

fun View.applySystemBarsPadding(includeTop: Boolean = true, includeBottom: Boolean = false) {
    val initialLeft = paddingLeft//verify soso
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val safeArea = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        view.setPadding(
            initialLeft + safeArea.left,
            initialTop + if (includeTop) safeArea.top else 0,
            initialRight + safeArea.right,
            initialBottom + if (includeBottom) safeArea.bottom else 0,
        )
        insets
    }
    doOnAttach(ViewCompat::requestApplyInsets)
}
