package com.example.belltest

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.example.belltest.databinding.ActivityMainBinding
import com.example.belltest.featured.FeaturedFragment
import com.example.belltest.feed.FeedFragment
import com.example.belltest.message.MessageFragment
import com.example.belltest.user.UserFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedTabId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureSystemBars()

        selectedTabId = savedInstanceState?.getInt(STATE_SELECTED_TAB) ?: R.id.nav_home
        binding.bottomNavigation.selectedItemId = selectedTabId
        binding.bottomNavigation.setOnItemSelectedListener { item -> showTab(item.itemId) }
        showTab(selectedTabId)
        supportFragmentManager.setFragmentResultListener(
            FeaturedFragment.REQUEST_OPEN_FEED,
            this,
        ) { _, _ ->
            binding.bottomNavigation.selectedItemId = R.id.nav_home
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

    private fun configureSystemBars() {
        val initialBottom = binding.bottomNavigation.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, initialBottom + safe.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        ViewCompat.requestApplyInsets(binding.root)
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
