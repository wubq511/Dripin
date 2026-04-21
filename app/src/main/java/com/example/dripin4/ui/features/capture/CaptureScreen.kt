package com.example.dripin4.ui.features.capture

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.CaptureScreenState
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.components.GlassButton
import com.example.dripin4.ui.designsystem.components.GlassButtonStyle
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassChip
import com.example.dripin4.ui.designsystem.components.GlassField
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
        item("capture_form") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
            ) {
                GlassSectionHeading(
                    title = DripStrings.CaptureSource,
                )

                Spacer(modifier = Modifier.height(DripSpacing.Small))

                GlassPanel {
                    androidx.compose.material3.Text(
                        text = state.sourceDetail,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = DripColors.Graphite,
                    )
                }

                if (state.isManualEntry) {
                    Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                    ) {
                        ContentType.entries.forEach { contentType ->
                            GlassChip(
                                text = contentType.captureLabel(),
                                selected = state.contentType == contentType,
                                onClick = { onContentTypeChanged(contentType) },
                                testTag = "capture_type_${contentType.name.lowercase()}",
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DripSpacing.SectionGap))

                GlassField(
                    label = DripStrings.CaptureTitleLabel,
                    value = state.title,
                    onValueChange = onTitleChanged,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(DripSpacing.Small))

                GlassField(
                    label = DripStrings.CaptureNoteLabel,
                    value = state.note,
                    onValueChange = onNoteChanged,
                    placeholder = DripStrings.CaptureNotePlaceholder,
                )

                Spacer(modifier = Modifier.height(DripSpacing.SectionGap))

                when (state.contentType) {
                    ContentType.LINK -> {
                        if (state.isManualEntry || state.sharedUrl.isNotBlank() || state.sharedText.isNotBlank()) {
                            GlassField(
                                label = "链接",
                                value = state.sharedUrl,
                                onValueChange = onSharedUrlChanged,
                                placeholder = "粘贴或输入链接",
                            )
                        }
                        if (state.isManualEntry || state.sharedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(DripSpacing.Small))
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
                        GlassPanel {
                            androidx.compose.material3.Text(
                                text = if (state.imageUris.isEmpty()) "还没有选择图片" else "已选择 ${state.imageUris.size} 张图片",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = DripColors.Graphite,
                            )
                        }
                        Spacer(modifier = Modifier.height(DripSpacing.Small))
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
                            Spacer(modifier = Modifier.height(DripSpacing.Small))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                            ) {
                                state.imageUris.forEachIndexed { index, uri ->
                                    GlassChip(
                                        text = "图片 ${index + 1}",
                                        selected = true,
                                        onClick = { onRemoveImage(uri) },
                                        testTag = "capture_image_${index + 1}",
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.duplicateMessage != null) {
                    Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
                    GlassPanel {
                        androidx.compose.material3.Text(
                            text = state.duplicateMessage,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = DripColors.Graphite,
                        )
                    }
                }

                if (state.autoTags.isNotEmpty() || state.availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
                    GlassSectionHeading(title = "标签")
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                    ) {
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
                        Spacer(modifier = Modifier.height(DripSpacing.Small))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                        ) {
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
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    GlassField(
                        label = "添加标签",
                        value = state.draftTag,
                        onValueChange = onDraftTagChanged,
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
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

                Spacer(modifier = Modifier.height(DripSpacing.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DripSpacing.Small, alignment = Alignment.End),
                ) {
                    GlassButton(
                        text = DripStrings.CaptureCancel,
                        onClick = onCancel,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier
                            .heightIn(max = 48.dp)
                            .defaultMinSize(minWidth = 116.dp),
                        testTag = "capture_cancel",
                    )
                    GlassButton(
                        text = if (state.isSaving) "保存中..." else state.saveLabel,
                        onClick = onSave,
                        enabled = state.canSave && !state.isSaving,
                        icon = Icons.Outlined.Check,
                        modifier = Modifier
                            .heightIn(max = 48.dp)
                            .defaultMinSize(minWidth = 116.dp),
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
