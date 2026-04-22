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
import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.DetailEditorState
import com.example.dripin4.ui.app.DetailScreenState
import com.example.dripin4.ui.app.InboxItemUi
import com.example.dripin4.ui.app.InboxKind
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripTheme
import com.example.dripin4.ui.features.detail.DetailScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun detailImageMode_showsPreviewGalleryAndEditorRemovalActions() {
        var removedUri: String? = null
        rule.setContent {
            DripTheme {
                DetailScreen(
                    state = DetailScreenState(
                        item = InboxItemUi(
                            id = "detail-1",
                            title = "图像细节",
                            note = "两张图片",
                            source = "相册",
                            time = "刚刚",
                            tag = "图片",
                            kind = InboxKind.Image,
                            contentType = ContentType.IMAGE,
                            isRead = false,
                            pushCount = 0,
                        ),
                        contentType = ContentType.IMAGE,
                        noteBody = "两张图片",
                        rawUrl = null,
                        textContent = null,
                        imageUris = listOf("content://image/1", "content://image/2"),
                        primaryActionEnabled = false,
                        editor = DetailEditorState(
                            visible = true,
                            titleDraft = "图像细节",
                            noteDraft = "两张图片",
                            rawUrlDraft = "",
                            textContentDraft = "",
                            imageUris = listOf("content://image/1", "content://image/2"),
                            tags = emptyList(),
                            tagDraft = "",
                            canSave = true,
                            isRead = false,
                        ),
                    ),
                    onPrimaryAction = {},
                    onOpenEditor = {},
                    onDismissEditor = {},
                    onEditorTitleChanged = {},
                    onEditorNoteChanged = {},
                    onEditorRawUrlChanged = {},
                    onEditorTextChanged = {},
                    onEditorRequestImages = {},
                    onEditorRemoveImage = { removedUri = it },
                    onEditorTagDraftChanged = {},
                    onEditorAddTag = {},
                    onEditorRemoveTag = {},
                    onEditorToggleRead = {},
                    onEditorSave = {},
                )
            }
        }

        rule.onNodeWithTag("detail_image_preview_1").assertIsDisplayed()
        rule.onNodeWithTag("detail_image_preview_2").assertIsDisplayed()
        rule.onNodeWithTag("detail_editor_image_preview_1").assertIsDisplayed()
        rule.onNodeWithTag("detail_editor_remove_image_2").assertIsDisplayed().performClick()
        rule.runOnIdle {
            assertEquals("content://image/2", removedUri)
        }
    }

    @Test
    fun detailBlankState_doesNotRenderPresetPreviewOrMetaSections() {
        rule.setContent {
            DripTheme {
                DetailScreen(
                    state = DetailScreenState(
                        item = InboxItemUi(
                            id = "detail-empty",
                            title = "",
                            note = "",
                            source = "",
                            time = "",
                            tag = "",
                            kind = InboxKind.Article,
                            contentType = ContentType.LINK,
                            isRead = false,
                            pushCount = 0,
                        ),
                        contentType = ContentType.LINK,
                        noteBody = "",
                        rawUrl = null,
                        textContent = null,
                        imageUris = emptyList(),
                        primaryActionEnabled = false,
                        editor = DetailEditorState(
                            visible = false,
                            titleDraft = "",
                            noteDraft = "",
                            rawUrlDraft = "",
                            textContentDraft = "",
                            imageUris = emptyList(),
                            tags = emptyList(),
                            tagDraft = "",
                            canSave = false,
                            isRead = false,
                        ),
                    ),
                    onPrimaryAction = {},
                    onOpenEditor = {},
                    onDismissEditor = {},
                    onEditorTitleChanged = {},
                    onEditorNoteChanged = {},
                    onEditorRawUrlChanged = {},
                    onEditorTextChanged = {},
                    onEditorRequestImages = {},
                    onEditorRemoveImage = {},
                    onEditorTagDraftChanged = {},
                    onEditorAddTag = {},
                    onEditorRemoveTag = {},
                    onEditorToggleRead = {},
                    onEditorSave = {},
                )
            }
        }

        rule.onNodeWithTag("screen_detail").assertIsDisplayed()
        rule.onNodeWithText(DripStrings.DetailTitle).assertIsDisplayed()
        rule.onAllNodesWithText(DripStrings.DetailSectionPreview).assertCountEquals(0)
        rule.onAllNodesWithText(DripStrings.DetailSectionMeta).assertCountEquals(0)
        rule.onAllNodesWithTag("detail_primary_action").assertCountEquals(0)
    }
}
