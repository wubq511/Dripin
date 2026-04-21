package com.example.dripin4.ui.features

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.dripin4.ui.app.DripApp
import com.example.dripin4.ui.content.DripStrings
import org.junit.Rule
import org.junit.Test

class InboxUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun inboxFilter_audioChipCanBeTapped() {
        composeRule.setContent { DripApp() }
        composeRule.onNodeWithTag("tab_inbox").performClick()
        composeRule.onNodeWithTag("chip_filter_audio").performClick()
    }

    @Test
    fun todayHeader_showsLocalizedCopy() {
        composeRule.setContent { DripApp() }
        composeRule.onNodeWithTag("tab_today").performClick()
        composeRule.onNodeWithText(DripStrings.TodayTitle).assertIsDisplayed()
    }
}
