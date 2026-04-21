package com.dripin.app.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.dripin.app.MainActivity
import org.junit.Rule
import org.junit.Test

class DripinNavGraphTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsPrimaryRouteLabelsOnLaunch() {
        composeRule.onNodeWithTag("tab_inbox").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_today").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_settings").assertIsDisplayed()
    }
}
