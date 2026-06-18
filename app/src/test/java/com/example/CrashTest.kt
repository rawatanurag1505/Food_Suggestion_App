package com.example

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Rule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@RunWith(RobolectricTestRunner::class)
class CrashTest {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun launchAndRender() {
        // Wait for composition and idle
        composeTestRule.waitForIdle()
        // Try to find the login screen title
        composeTestRule.onNodeWithText("MealMate Suggestions").assertExists()
    }
}
