package com.example

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Rule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class CrashTest2 {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun testNavigation() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("to_signup_button").performScrollTo().performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create Account").assertExists()
    }
}
