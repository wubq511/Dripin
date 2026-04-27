package com.example.dripin4.ui.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class TopBarOverlayUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun topBarSearchButton_opensSearchOverlay() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("top_bar_search").performClick()
        rule.onNodeWithTag("search_box").assertIsDisplayed()
        rule.onNodeWithTag("search_input").assertIsDisplayed().assertIsFocused()
    }

    @Test
    fun topBarNotificationButton_opensNotificationHistoryOverlay() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("top_bar_notifications").performClick()
        rule.onNodeWithTag("overlay_notification_history").assertIsDisplayed()
    }

    @Test
    fun notificationHistoryRow_expandsToShowDeliveredTitles() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("top_bar_notifications").performClick()
        rule.onNodeWithTag("notification_history_sample-history-1").performClick()
        rule.onNodeWithTag("notification_history_titles_sample-history-1").assertIsDisplayed()
        rule.onNodeWithTag("notification_history_title_sample-history-1_0").assertIsDisplayed()
        rule.onNodeWithTag("notification_history_title_sample-history-1_1").assertIsDisplayed()
    }

    @Test
    fun searchQuery_showsCompactResultsAndCanOpenDetail() {
        rule.setContent { DripApp() }

        rule.onNodeWithTag("top_bar_search").performClick()
        rule.onNodeWithTag("search_input").performTextInput("视觉")
        rule.onNodeWithTag("search_results").assertIsDisplayed()
        rule.onNodeWithTag("search_result_inbox-3").assertIsDisplayed().performClick()
        rule.onNodeWithTag("screen_detail").assertIsDisplayed()
    }
}
