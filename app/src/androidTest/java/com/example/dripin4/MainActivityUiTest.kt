package com.example.dripin4

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.dripin.app.MainActivity
import org.junit.Rule
import org.junit.Test

class MainActivityUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsChromeAndAllowsDetailNavigation() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("top_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_today").assertIsSelected()
        composeTestRule.onNodeWithTag("tab_detail").performClick()
        composeTestRule.onNodeWithTag("screen_detail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_detail").assertIsSelected()
    }

    @Test
    fun selectedTab_statePersistsAfterNavigation() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tab_today").assertIsSelected()
        composeTestRule.onNodeWithTag("tab_inbox").assertIsNotSelected()

        composeTestRule.onNodeWithTag("tab_detail").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tab_detail").assertIsSelected()
        composeTestRule.onNodeWithTag("tab_today").assertIsNotSelected()
        composeTestRule.onNodeWithTag("screen_detail").assertIsDisplayed()
    }
}
