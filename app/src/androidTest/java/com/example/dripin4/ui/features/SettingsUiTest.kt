package com.example.dripin4.ui.features

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.dripin4.ui.app.SettingsScreenState
import com.example.dripin4.ui.app.SystemNotificationUi
import com.example.dripin4.ui.designsystem.DripTheme
import com.example.dripin4.ui.features.settings.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreen_showsSystemNotificationStatusAndTriggersAction() {
        var actionCount = 0

        rule.setContent {
            DripTheme {
                SettingsScreen(
                    state = SettingsScreenState(
                        dailyCount = 3,
                        groups = emptyList(),
                        reminderSubtitle = "每天 20:30",
                        repeatUnreadEnabled = true,
                        sortModeLabel = "最早优先",
                        systemNotification = SystemNotificationUi(
                            enabled = false,
                            statusLabel = "未开启",
                            detail = "系统还没授予通知权限，Dripin 现在发不出提醒。",
                            actionLabel = "开启系统通知",
                        ),
                    ),
                    onDecreaseDailyCount = {},
                    onIncreaseDailyCount = {},
                    onToggleSetting = { _, _ -> },
                    onOpenReminderTime = {},
                    onSystemNotificationAction = { actionCount += 1 },
                )
            }
        }

        rule.onNodeWithTag("settings_notification_panel").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("系统通知").assertIsDisplayed()
        rule.onNodeWithText("未开启").assertIsDisplayed()
        rule.onAllNodesWithText("系统还没授予通知权限，Dripin 现在发不出提醒。").assertCountEquals(0)
        rule.onNodeWithTag("settings_notification_action").performClick()

        rule.runOnIdle {
            assertEquals(1, actionCount)
        }
    }
}
