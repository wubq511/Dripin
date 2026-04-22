package com.example.dripin4.ui.features

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.DripApp
import com.example.dripin4.ui.app.CaptureScreenState
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripTheme
import com.example.dripin4.ui.features.capture.CaptureScreen
import org.junit.Rule
import org.junit.Test

class CaptureUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureTagToggle_changesSelectedState() {
        rule.setContent { DripApp() }
        rule.onNodeWithTag("tab_capture").performClick()
        rule.onNodeWithTag("tag_研究").performClick()
        rule.onNodeWithTag("tag_研究_selected").assertIsDisplayed()
    }

    @Test
    fun captureScreen_showsSaveAndCancelControls() {
        rule.setContent { DripApp() }
        rule.onNodeWithTag("tab_capture").performClick()
        rule.onNodeWithTag("capture_cancel").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("capture_save").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun captureImageMode_showsMultiplePreviewCards() {
        rule.setContent {
            DripTheme {
                CaptureScreen(
                    state = CaptureScreenState(
                        contentType = ContentType.IMAGE,
                        isManualEntry = true,
                        sourceDetail = "来自相册",
                        title = "图片收集",
                        note = "",
                        selectedTags = emptySet(),
                        availableTags = emptyList(),
                        draftTag = "",
                        autoTags = emptyList(),
                        sharedUrl = "",
                        sharedText = "",
                        imageUris = listOf("content://image/1", "content://image/2"),
                        canSave = true,
                        isSaving = false,
                        saveLabel = DripStrings.CaptureSave,
                        duplicateMessage = null,
                    ),
                    onTitleChanged = {},
                    onNoteChanged = {},
                    onTagToggle = {},
                    onDraftTagChanged = {},
                    onAddDraftTag = {},
                    onContentTypeChanged = {},
                    onSharedUrlChanged = {},
                    onSharedTextChanged = {},
                    onPickImages = {},
                    onRemoveImage = {},
                    onSave = {},
                    onCancel = {},
                )
            }
        }

        rule.onNodeWithTag("capture_image_preview_1").assertIsDisplayed()
        rule.onNodeWithTag("capture_image_preview_2").assertIsDisplayed()
    }

    @Test
    fun captureScreen_usesLayeredCardsInsteadOfSingleFormCard() {
        rule.setContent {
            DripTheme {
                CaptureScreen(
                    state = CaptureScreenState(
                        contentType = ContentType.LINK,
                        isManualEntry = true,
                        sourceDetail = "来自设计灵感站的分享",
                        title = "收集一篇文章",
                        note = "准备稍后回看",
                        selectedTags = setOf("设计"),
                        availableTags = listOf("设计", "研究"),
                        draftTag = "",
                        autoTags = listOf("灵感"),
                        sharedUrl = "https://example.com/story",
                        sharedText = "补充一点上下文",
                        imageUris = emptyList(),
                        canSave = true,
                        isSaving = false,
                        saveLabel = DripStrings.CaptureSave,
                        duplicateMessage = null,
                    ),
                    onTitleChanged = {},
                    onNoteChanged = {},
                    onTagToggle = {},
                    onDraftTagChanged = {},
                    onAddDraftTag = {},
                    onContentTypeChanged = {},
                    onSharedUrlChanged = {},
                    onSharedTextChanged = {},
                    onPickImages = {},
                    onRemoveImage = {},
                    onSave = {},
                    onCancel = {},
                )
            }
        }

        rule.onAllNodesWithTag("capture_form").assertCountEquals(0)
        rule.onNodeWithTag("capture_primary_card").assertIsDisplayed()
        rule.onNodeWithTag("capture_content_card").performScrollTo()
        rule.onAllNodesWithTag("capture_content_card").assertCountEquals(1)
        rule.onNodeWithTag("capture_tags_card").performScrollTo()
        rule.onAllNodesWithTag("capture_tags_card").assertCountEquals(1)
    }
}
