package com.dripin.app.feature.recommendation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dripin.app.core.designsystem.theme.DripinTheme
import com.dripin.app.core.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecommendationCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsImmediateConfirmationFeedbackAfterMarkRead() {
        var markReadCalls = 0

        composeRule.setContent {
            DripinTheme {
                RecommendationCard(
                    card = fakeTodayCard(),
                    onMarkRead = { markReadCalls += 1 },
                    onOpenLink = {},
                )
            }
        }

        composeRule.onAllNodesWithText("已标记已读").assertCountEquals(0)

        composeRule.onNodeWithTag("today-card-mark-read-1", useUnmergedTree = true).performClick()

        composeRule.onNodeWithTag("today-card-mark-read-1", useUnmergedTree = true).assertIsNotEnabled()
        composeRule.onNodeWithTag("today-card-read-feedback-1").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(1, markReadCalls)
        }
    }
}

private fun fakeTodayCard(): TodayCardModel = TodayCardModel(
    id = 1L,
    rank = 2,
    title = "DeepSeek的模型，让AI第一次学会了反思。",
    contentType = ContentType.LINK,
    sourceLabel = "微信",
    textPreview = "这是一个摘要片段，用来模拟链接类内容在今日页中的预览文案。",
    note = "补一条备注，确认卡片在多段内容下依旧能保持秩序。",
    rawUrl = "https://mp.weixin.qq.com/s/example",
    imageUri = null,
)
