package com.example.belltest.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding

abstract class BaseViewBindingActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VB
        private set

    protected abstract fun inflateBinding(inflater: LayoutInflater): VB

    protected open fun systemBarInsetTargets(binding: VB): List<SystemBarInsetTarget> =
        listOf(SystemBarInsetTarget(binding.root, includeTop = true, includeBottom = true))

    protected open fun onBindingCreated(savedInstanceState: Bundle?) = Unit

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        systemBarInsetTargets(binding).forEach { target ->
            target.view.applySystemBarsPadding(
                includeTop = target.includeTop,
                includeBottom = target.includeBottom,
            )
        }
        configureSystemBarIcons()
        onBindingCreated(savedInstanceState)
    }

    private fun configureSystemBarIcons() {
        val darkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
