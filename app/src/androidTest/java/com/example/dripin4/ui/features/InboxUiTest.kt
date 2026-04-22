package com.example.dripin4.ui.features

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import com.example.dripin4.ui.app.DripApp
import com.example.dripin4.ui.app.InboxFilter
import com.example.dripin4.ui.app.InboxItemUi
import com.example.dripin4.ui.app.InboxKind
import com.example.dripin4.ui.app.InboxScreenState
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripTheme
import com.example.dripin4.ui.features.inbox.InboxScreen
import org.junit.Rule
import org.junit.Test

class InboxUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun inboxShowsOnlyLinkTextImageContentChipsAndStateFilters() {
        composeRule.setContent { DripApp() }
        composeRule.onNodeWithTag("tab_inbox").performClick()
        composeRule.onNodeWithTag("chip_filter_all").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_filter_link").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_filter_text").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_filter_image").assertIsDisplayed()
        composeRule.onAllNodesWithTag("chip_filter_article").assertCountEquals(0)
        composeRule.onAllNodesWithTag("chip_filter_video").assertCountEquals(0)
        composeRule.onAllNodesWithTag("chip_filter_thread").assertCountEquals(0)
        composeRule.onAllNodesWithTag("chip_filter_audio").assertCountEquals(0)
        composeRule.onNodeWithTag("chip_read_all").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_read_read").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_read_unread").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_push_all").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_push_pushed").assertIsDisplayed()
        composeRule.onNodeWithTag("chip_push_unpushed").assertIsDisplayed()
    }

    @Test
    fun inboxCards_showReadAndPushStatusBadges() {
        composeRule.setContent { DripApp() }
        composeRule.onNodeWithTag("tab_inbox").performClick()
        composeRule.onNodeWithTag("inbox_card_inbox-1").performScrollTo()
        composeRule.onAllNodesWithTag("inbox_status_read_inbox-1", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("inbox_status_push_inbox-1", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onNodeWithTag("inbox_card_inbox-2").performScrollTo()
        composeRule.onAllNodesWithTag("inbox_status_read_inbox-2", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("inbox_status_push_inbox-2", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun inboxCard_upgradedLayout_hidesGenericTagAndShowsPushCount() {
        composeRule.setContent {
            DripTheme {
                InboxScreen(
                    state = InboxScreenState(
                        contentFilters = emptyList(),
                        selectedContentFilter = InboxFilter.All,
                        readFilters = emptyList(),
                        selectedReadFilter = ReadFilter.ALL,
                        pushFilters = emptyList(),
                        selectedPushFilter = PushFilter.ALL,
                        hasActiveFilters = false,
                        items = listOf(
                            InboxItemUi(
                                id = "layout-1",
                                title = "hi",
                                note = "test",
                                source = "未知来源",
                                time = "11 分钟前",
                                tag = "文字",
                                kind = InboxKind.Thread,
                                contentType = ContentType.TEXT,
                                isRead = false,
                                pushCount = 2,
                            ),
                        ),
                    ),
                    onContentFilterSelected = {},
                    onReadFilterSelected = {},
                    onPushFilterSelected = {},
                    onOpenDetail = {},
                )
            }
        }

        composeRule.onNodeWithTag("inbox_card_layout-1").performScrollTo()
        composeRule.onAllNodesWithTag("inbox_context_kind_layout-1", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithText("文字").assertCountEquals(1)
        composeRule.onAllNodesWithText("讨论").assertCountEquals(0)
        composeRule.onAllNodesWithText("已推送 2 次").assertCountEquals(1)
        composeRule.onAllNodesWithTag("inbox_note_layout-1", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun inboxCard_blankSummary_collapsesNotePanel() {
        composeRule.setContent {
            DripTheme {
                InboxScreen(
                    state = InboxScreenState(
                        contentFilters = emptyList(),
                        selectedContentFilter = InboxFilter.All,
                        readFilters = emptyList(),
                        selectedReadFilter = ReadFilter.ALL,
                        pushFilters = emptyList(),
                        selectedPushFilter = PushFilter.ALL,
                        hasActiveFilters = false,
                        items = listOf(
                            InboxItemUi(
                                id = "layout-2",
                                title = "一条只有标题的内容",
                                note = "",
                                source = "设计文章",
                                time = "8 分钟前",
                                tag = "设计",
                                kind = InboxKind.Article,
                                contentType = ContentType.LINK,
                                isRead = true,
                                pushCount = 0,
                            ),
                        ),
                    ),
                    onContentFilterSelected = {},
                    onReadFilterSelected = {},
                    onPushFilterSelected = {},
                    onOpenDetail = {},
                )
            }
        }

        composeRule.onNodeWithTag("inbox_card_layout-2").performScrollTo()
        composeRule.onAllNodesWithTag("inbox_note_layout-2", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun todayHeader_showsLocalizedCopy() {
        composeRule.setContent { DripApp() }
        composeRule.onNodeWithTag("tab_today").performClick()
        composeRule.onNodeWithText(DripStrings.TodayTitle).assertIsDisplayed()
    }
}
