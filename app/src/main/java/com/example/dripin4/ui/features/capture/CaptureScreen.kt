package com.example.dripin4.ui.features.capture

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dripin.app.core.designsystem.component.ExpandableImagePreview
import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.CaptureScreenState
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassButton
import com.example.dripin4.ui.designsystem.components.GlassButtonStyle
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassChip
import com.example.dripin4.ui.designsystem.components.GlassChipRow
import com.example.dripin4.ui.designsystem.components.GlassField
import com.example.dripin4.ui.designsystem.components.GlassInfoPill
import com.example.dripin4.ui.designsystem.components.GlassPageHeader
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassScaffold
import com.example.dripin4.ui.designsystem.components.GlassSectionHeading

@Composable
fun CaptureScreen(
    state: CaptureScreenState,
    onTitleChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onDraftTagChanged: (String) -> Unit,
    onAddDraftTag: () -> Unit,
    onContentTypeChanged: (ContentType) -> Unit,
    onSharedUrlChanged: (String) -> Unit,
    onSharedTextChanged: (String) -> Unit,
    onPickImages: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassScaffold(
        testTag = "screen_capture",
        modifier = modifier,
        header = {
            GlassPageHeader(
                title = DripStrings.CaptureTitle,
            )
        },
    ) {
        item("capture_primary_card") {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_primary_card"),
                tone = GlassCardTone.Hero,
                contentPadding = PaddingValues(DripSpacing.HeroPadding),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Large)) {
                    Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Small)) {
                        if (state.isManualEntry) {
                            GlassChipRow {
                                ContentType.entries.forEach { contentType ->
                                    GlassChip(
                                        text = contentType.captureLabel(),
                                        selected = state.contentType == contentType,
                                        onClick = { onContentTypeChanged(contentType) },
                                        testTag = "capture_type_${contentType.name.lowercase()}",
                                    )
                                }
                            }
                        } else {
                            GlassInfoPill(
                                text = state.contentType.captureLabel(),
                                tint = GlassPalette.AccentMint,
                            )
                        }

                        Text(
                            text = state.sourceDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassPalette.TextTodaySubtitle,
                        )
                    }

                    GlassField(
                        label = DripStrings.CaptureTitleLabel,
                        value = state.title,
                        onValueChange = onTitleChanged,
                        singleLine = true,
                    )

                    GlassField(
                        label = DripStrings.CaptureNoteLabel,
                        value = state.note,
                        onValueChange = onNoteChanged,
                        placeholder = DripStrings.CaptureNotePlaceholder,
                    )

                    state.duplicateMessage?.let { duplicateMessage ->
                        GlassPanel {
                            Text(
                                text = duplicateMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DripColors.Graphite,
                            )
                        }
                    }
                }
            }
        }

        if (state.shouldShowContentCard()) {
            item("capture_content_card") {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_content_card"),
                    tone = GlassCardTone.Neutral,
                    contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Small)) {
                        GlassSectionHeading(title = state.contentCardTitle())
                        when (state.contentType) {
                            ContentType.LINK -> {
                                GlassField(
                                    label = "链接",
                                    value = state.sharedUrl,
                                    onValueChange = onSharedUrlChanged,
                                    placeholder = "粘贴或输入链接",
                                )
                                if (state.isManualEntry || state.sharedText.isNotBlank()) {
                                    GlassField(
                                        label = "附加文本",
                                        value = state.sharedText,
                                        onValueChange = onSharedTextChanged,
                                        placeholder = "补充一点上下文",
                                    )
                                }
                            }

                            ContentType.TEXT -> {
                                GlassField(
                                    label = "文字内容",
                                    value = state.sharedText,
                                    onValueChange = onSharedTextChanged,
                                    placeholder = "写下想保存的文字",
                                )
                            }

                            ContentType.IMAGE -> {
                                Text(
                                    text = if (state.imageUris.isEmpty()) "还没有选择图片" else "已选择 ${state.imageUris.size} 张图片",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassPalette.TextTodaySubtitle,
                                )
                                GlassButton(
                                    text = if (state.imageUris.isEmpty()) "选择图片" else "继续添加",
                                    onClick = onPickImages,
                                    style = GlassButtonStyle.Secondary,
                                    icon = Icons.Outlined.AddPhotoAlternate,
                                    modifier = Modifier
                                        .heightIn(max = 48.dp)
                                        .defaultMinSize(minWidth = 140.dp),
                                    testTag = "capture_pick_images",
                                )
                                if (state.imageUris.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                                    ) {
                                        state.imageUris.forEachIndexed { index, uri ->
                                            CaptureImageCard(
                                                imageUri = uri,
                                                index = index,
                                                onRemove = { onRemoveImage(uri) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.autoTags.isNotEmpty() || state.availableTags.isNotEmpty()) {
            item("capture_tags_card") {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_tags_card"),
                    tone = GlassCardTone.Neutral,
                    contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Small)) {
                        GlassSectionHeading(title = "标签")
                        GlassChipRow {
                            state.availableTags.forEach { tag ->
                                val selected = state.selectedTags.contains(tag)
                                val baseTag = "tag_${tag.lowercase()}"
                                GlassChip(
                                    text = tag,
                                    selected = selected,
                                    onClick = { onTagToggle(tag) },
                                    testTag = if (selected) "${baseTag}_selected" else baseTag,
                                )
                            }
                        }
                        if (state.autoTags.isNotEmpty()) {
                            GlassChipRow {
                                state.autoTags.forEachIndexed { index, tag ->
                                    GlassChip(
                                        text = tag,
                                        selected = true,
                                        onClick = {},
                                        enabled = false,
                                        testTag = "capture_auto_tag_${index + 1}",
                                    )
                                }
                            }
                        }
                        GlassField(
                            label = "添加标签",
                            value = state.draftTag,
                            onValueChange = onDraftTagChanged,
                            singleLine = true,
                        )
                        GlassButton(
                            text = "加入标签",
                            onClick = onAddDraftTag,
                            style = GlassButtonStyle.Secondary,
                            modifier = Modifier
                                .heightIn(max = 48.dp)
                                .defaultMinSize(minWidth = 120.dp),
                            testTag = "capture_add_tag",
                        )
                    }
                }
            }
        }

        item("capture_actions_card") {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_actions_card"),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(DripSpacing.Small),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DripSpacing.Small),
                ) {
                    GlassButton(
                        text = DripStrings.CaptureCancel,
                        onClick = onCancel,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        testTag = "capture_cancel",
                    )
                    GlassButton(
                        text = if (state.isSaving) "保存中..." else state.saveLabel,
                        onClick = onSave,
                        enabled = state.canSave && !state.isSaving,
                        icon = Icons.Outlined.Check,
                        modifier = Modifier.weight(1f),
                        testTag = "capture_save",
                    )
                }
            }
        }
    }
}

private fun ContentType.captureLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}

private fun CaptureScreenState.shouldShowContentCard(): Boolean = when (contentType) {
    ContentType.LINK -> isManualEntry || sharedUrl.isNotBlank() || sharedText.isNotBlank()
    ContentType.TEXT -> true
    ContentType.IMAGE -> true
}

private fun CaptureScreenState.contentCardTitle(): String = when (contentType) {
    ContentType.LINK -> "链接内容"
    ContentType.TEXT -> "文字内容"
    ContentType.IMAGE -> "图片"
}

@Composable
private fun CaptureImageCard(
    imageUri: String,
    index: Int,
    onRemove: () -> Unit,
) {
    GlassPanel(
        modifier = Modifier.width(176.dp),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DripSpacing.Small),
        ) {
            ExpandableImagePreview(
                imageUri = imageUri,
                contentDescription = "已选图片预览 ${index + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_image_preview_${index + 1}"),
                height = 140.dp,
            )
            GlassButton(
                text = "移除",
                onClick = onRemove,
                style = GlassButtonStyle.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capture_remove_image_${index + 1}"),
            )
        }
    }
}
