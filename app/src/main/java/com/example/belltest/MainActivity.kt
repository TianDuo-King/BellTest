package com.example.belltest

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.example.belltest.base.BaseViewBindingActivity
import com.example.belltest.base.SystemBarInsetTarget
import com.example.belltest.databinding.ActivityMainBinding
import com.example.belltest.featured.FeaturedFragment
import com.example.belltest.feed.FeedFragment
import com.example.belltest.message.MessageFragment
import com.example.belltest.user.UserFragment
import androidx.core.os.bundleOf

class MainActivity : BaseViewBindingActivity<ActivityMainBinding>() {
    private var selectedTabId = R.id.nav_home

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)

    override fun systemBarInsetTargets(binding: ActivityMainBinding): List<SystemBarInsetTarget> =
        listOf(SystemBarInsetTarget(binding.bottomNavigation, includeTop = false, includeBottom = true))

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        selectedTabId = savedInstanceState?.getInt(STATE_SELECTED_TAB) ?: R.id.nav_home
        binding.bottomNavigation.selectedItemId = selectedTabId
        binding.bottomNavigation.setOnItemSelectedListener { item -> showTab(item.itemId) }
        showTab(selectedTabId)
        supportFragmentManager.setFragmentResultListener(
            FeaturedFragment.REQUEST_OPEN_FEED,
            this,
        ) { _, result ->
            val mediaId = result.getString(
                FeaturedFragment.KEY_MEDIA_ID
            ) ?: return@setFragmentResultListener

            // 切换到首页
            binding.bottomNavigation.selectedItemId = R.id.nav_home

            // 将目标视频 ID 传递给 FeedFragment
            supportFragmentManager.setFragmentResult(
                FeedFragment.REQUEST_OPEN_MEDIA,
                bundleOf(FeedFragment.KEY_MEDIA_ID to mediaId)
            )
        }

        onBackPressedDispatcher.addCallback(this) {
            if (selectedTabId != R.id.nav_home) {
                binding.bottomNavigation.selectedItemId = R.id.nav_home
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun showTab(itemId: Int): Boolean {
        val tag = tagFor(itemId) ?: return false
        val manager = supportFragmentManager
        val current = manager.primaryNavigationFragment
        val target = manager.findFragmentByTag(tag) ?: createFragment(itemId)

        if (current === target && target.isAdded) {
            selectedTabId = itemId
            return true
        }

        manager.commit {
            setReorderingAllowed(true)
            if (current != null) {
                hide(current)
                setMaxLifecycle(current, Lifecycle.State.CREATED)
            }
            if (target.isAdded) show(target) else add(R.id.fragment_container, target, tag)
            setMaxLifecycle(target, Lifecycle.State.RESUMED)
            setPrimaryNavigationFragment(target)
        }
        selectedTabId = itemId
        return true
    }

    private fun createFragment(itemId: Int): Fragment = when (itemId) {
        R.id.nav_home -> FeedFragment()
        R.id.nav_featured -> FeaturedFragment()
        R.id.nav_message -> MessageFragment()
        R.id.nav_user -> UserFragment()
        else -> error("Unknown navigation item: $itemId")
    }

    private fun tagFor(itemId: Int): String? = when (itemId) {
        R.id.nav_home -> "tab:feed"
        R.id.nav_featured -> "tab:featured"
        R.id.nav_message -> "tab:message"
        R.id.nav_user -> "tab:user"
        else -> null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_SELECTED_TAB, selectedTabId)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val STATE_SELECTED_TAB = "selectedTabId"
    }
}
