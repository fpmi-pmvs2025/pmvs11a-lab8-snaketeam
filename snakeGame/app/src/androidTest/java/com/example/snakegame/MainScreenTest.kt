package com.example.snakegame

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testMainScreenButtonsExist() {
        // Check play button exists and is displayed
        onView(withId(R.id.btnPlay))
            .check(matches(isDisplayed()))
            .check(matches(withText("Play")))

        // Check settings button exists and is displayed
        onView(withId(R.id.btnSettings))
            .check(matches(isDisplayed()))
            .check(matches(withText("Weather")))
    }
}