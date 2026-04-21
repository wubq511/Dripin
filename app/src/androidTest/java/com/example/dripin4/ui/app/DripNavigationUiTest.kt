package com.example.dripin4.ui.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DripNavigationUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomNav_routesBetweenScreens() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("tab_inbox").performClick()
        rule.onNodeWithTag("screen_inbox").assertIsDisplayed()

        rule.onNodeWithTag("tab_settings").performClick()
        rule.onNodeWithTag("screen_settings").assertIsDisplayed()

        rule.onNodeWithTag("tab_capture").performClick()
        rule.onNodeWithTag("screen_capture").assertIsDisplayed()

        rule.onNodeWithTag("tab_detail").performClick()
        rule.onNodeWithTag("screen_detail").assertIsDisplayed()
    }

    @Test
    fun bottomNav_exposesExpectedTabsAndDescriptions() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("tab_inbox").assertIsDisplayed()
        rule.onNodeWithTag("tab_today").assertIsDisplayed()
        rule.onNodeWithTag("tab_capture").assertIsDisplayed()
        rule.onNodeWithTag("tab_detail").assertIsDisplayed()
        rule.onNodeWithTag("tab_settings").assertIsDisplayed()

        rule.onNodeWithContentDescription("Inbox").assertIsDisplayed()
        rule.onNodeWithContentDescription("Today").assertIsDisplayed()
        rule.onNodeWithContentDescription("Capture").assertIsDisplayed()
        rule.onNodeWithContentDescription("Detail").assertIsDisplayed()
        rule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }
}
