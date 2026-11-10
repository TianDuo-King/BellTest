package com.example.belltest.base

import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun View.applySystemBarsPadding(includeTop: Boolean = true, includeBottom: Boolean = false) {
    val initialLeft = paddingLeft
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
    ViewCompat.requestApplyInsets(this)
}

fun AppCompatActivity.enableEdgeToEdgeSystemBarPadding(root: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    root.applySystemBarsPadding(includeTop = true, includeBottom = true)
    val darkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
        Configuration.UI_MODE_NIGHT_YES
    WindowCompat.getInsetsController(window, root).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
    }
}
