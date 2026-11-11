package com.example.belltest.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseViewBindingFragment<VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null

    protected val binding: VB
        get() = requireNotNull(_binding) { "Binding is only available between onCreateView and onDestroyView" }

    protected val bindingOrNull: VB?
        get() = _binding

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected open fun systemBarInsetTargets(binding: VB): List<SystemBarInsetTarget> =
        listOf(SystemBarInsetTarget(binding.root, includeTop = true))

    protected open fun onBindingCreated(binding: VB, savedInstanceState: Bundle?) = Unit

    protected open fun onBindingDestroyed(binding: VB) = Unit

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflateBinding(inflater, container).also { _binding = it }.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        systemBarInsetTargets(binding).forEach { target ->
            target.view.applySystemBarsPadding(
                includeTop = target.includeTop,
                includeBottom = target.includeBottom,
            )
        }
        onBindingCreated(binding, savedInstanceState)
    }

    final override fun onDestroyView() {
        onBindingDestroyed(binding)
        _binding = null
        super.onDestroyView()
    }
}
