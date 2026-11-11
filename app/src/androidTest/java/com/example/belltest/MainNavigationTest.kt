package com.example.belltest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.belltest.feed.FeedFragment
import com.example.belltest.feed.R as FeedR
import com.example.belltest.featured.R as FeaturedR
import com.example.belltest.message.R as MessageR
import com.example.belltest.user.R as UserR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun coldStartCreatesOnlyOneFeedFragment() {
        activityRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.executePendingTransactions()
            val feedCount = activity.supportFragmentManager.fragments.count { it is FeedFragment }
            assertEquals(1, feedCount)
        }
    }

    @Test
    fun bottomNavigationContainsTheFourProductModules() {
        listOf(R.id.nav_home, R.id.nav_featured, R.id.nav_message, R.id.nav_user).forEach { itemId ->
            onView(withId(itemId)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun feedTopNavigationStartsBelowTheStatusBar() {
        onView(allOf(withId(FeedR.id.top_bar), isDisplayed())).check { view, noViewException ->
            if (noViewException != null) throw noViewException
            val statusBarHeight = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())
                ?.top
                ?: 0
            assertTrue(
                "feed top navigation paddingTop=${view.paddingTop} must be >= status bar height=$statusBarHeight",
                view.paddingTop >= statusBarHeight,
            )
        }
    }

    @Test
    fun eachTabShowsItsModuleCallChain() {
        onView(withId(FeedR.id.feed_module_chain)).check(matches(isDisplayed()))

        onView(withId(R.id.nav_featured)).perform(click())
        onView(withId(FeaturedR.id.featured_module_chain)).check(matches(isDisplayed()))

        onView(withId(R.id.nav_message)).perform(click())
        onView(withId(MessageR.id.message_module_chain)).check(matches(isDisplayed()))

        onView(withId(R.id.nav_user)).perform(click())
        onView(withId(UserR.id.user_module_chain)).check(matches(isDisplayed()))
    }
}
