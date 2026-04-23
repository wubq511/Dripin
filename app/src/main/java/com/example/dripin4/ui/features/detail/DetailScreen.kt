package com.example.dripin4.ui.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import com.dripin.app.core.designsystem.component.ExpandableImagePreview
import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.DetailScreenState
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
import com.example.dripin4.ui.designsystem.components.GlassHeroAccent
import com.example.dripin4.ui.designsystem.components.GlassHeroHeader
import com.example.dripin4.ui.designsystem.components.GlassInfoPill
import com.example.dripin4.ui.designsystem.components.GlassPageHeader
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassScaffold
import com.example.dripin4.ui.designsystem.components.GlassSectionHeading

@Composable
fun DetailScreen(
    state: DetailScreenState,
    onPrimaryAction: () -> Unit,
    onOpenEditor: () -> Unit,
    onDismissEditor: () -> Unit,
    onEditorTitleChanged: (String) -> Unit,
    onEditorNoteChanged: (String) -> Unit,
    onEditorRawUrlChanged: (String) -> Unit,
    onEditorTextChanged: (String) -> Unit,
    onEditorRequestImages: () -> Unit,
    onEditorRemoveImage: (String) -> Unit,
    onEditorTagDraftChanged: (String) -> Unit,
    onEditorAddTag: () -> Unit,
    onEditorRemoveTag: (String) -> Unit,
    onEditorToggleRead: () -> Unit,
    onEditorSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.item
    val subtitleParts = listOf(item.source, item.time).filter(String::isNotBlank)
    val hasHeaderContent = item.title.isNotBlank() || subtitleParts.isNotEmpty() || item.tag.isNotBlank()
    val previewText = state.textContent?.takeIf(String::isNotBlank) ?: state.rawUrl?.takeIf(String::isNotBlank)
    val hasPreviewContent = when (state.contentType) {
        ContentType.IMAGE -> state.imageUris.isNotEmpty()
        else -> !previewText.isNullOrBlank()
    }
    val hasMetaContent = state.noteBody.isNotBlank()
    val hasDetailContent = hasHeaderContent || hasPreviewContent || hasMetaContent || state.primaryActionEnabled

    GlassScaffold(
        testTag = "screen_detail",
        modifier = modifier,
        header = {
            if (hasHeaderContent) {
                GlassHeroHeader(
                    eyebrow = DripStrings.DetailTitle,
                    title = item.title,
                    subtitle = subtitleParts.joinToString(" · "),
                    accent = GlassHeroAccent.Ink,
                    metaAtStart = true,
                    meta = DripStrings.inboxKindLabel(item.kind),
                    supportingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall)) {
                            if (item.tag.isNotBlank()) {
                                GlassInfoPill(
                                    text = item.tag,
                                    tint = GlassPalette.AccentMint,
                                )
                            }
                            GlassInfoPill(
                                text = if (state.editor.isRead) "已读" else "未读",
                                tint = GlassPalette.TextTodaySelection,
                            )
                        }
                    },
                )
            } else {
                GlassPageHeader(title = DripStrings.DetailTitle)
            }
        },
    ) {
        if (hasPreviewContent) item("detail_preview") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
            ) {
                GlassSectionHeading(title = DripStrings.DetailSectionPreview)
                Spacer(modifier = Modifier.height(DripSpacing.Small))
                GlassPanel {
                    when (state.contentType) {
                        ContentType.IMAGE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Small)) {
                                Text(
                                    text = "共 ${state.imageUris.size} 张图片",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DripColors.Graphite,
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                                ) {
                                    state.imageUris.forEachIndexed { index, imageUri ->
                                        DetailPreviewImageCard(
                                            imageUri = imageUri,
                                            index = index,
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = checkNotNull(previewText),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DripColors.Graphite,
                            )
                        }
                    }
                }
            }
        }

        if (hasMetaContent) item("detail_meta") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
            ) {
                GlassSectionHeading(title = DripStrings.DetailSectionMeta)
                Spacer(modifier = Modifier.height(DripSpacing.Small))
                GlassPanel {
                    Text(
                        text = state.noteBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DripColors.Graphite,
                    )
                }
            }
        }

        if (hasDetailContent) item("detail_actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DripSpacing.Small),
            ) {
                GlassButton(
                    text = DripStrings.DetailSecondaryAction,
                    onClick = onOpenEditor,
                    style = GlassButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = DripStrings.DetailPrimaryAction,
                    onClick = onPrimaryAction,
                    enabled = state.primaryActionEnabled,
                    modifier = Modifier.weight(1f),
                    testTag = "detail_primary_action",
                )
            }
        }
    }

    if (state.editor.visible) {
        DetailEditorDialog(
            state = state,
            onDismiss = onDismissEditor,
            onTitleChanged = onEditorTitleChanged,
            onNoteChanged = onEditorNoteChanged,
            onRawUrlChanged = onEditorRawUrlChanged,
            onTextChanged = onEditorTextChanged,
            onRequestImages = onEditorRequestImages,
            onRemoveImage = onEditorRemoveImage,
            onTagDraftChanged = onEditorTagDraftChanged,
            onAddTag = onEditorAddTag,
            onRemoveTag = onEditorRemoveTag,
            onToggleRead = onEditorToggleRead,
            onSave = onEditorSave,
        )
    }
}

@Composable
private fun DetailEditorDialog(
    state: DetailScreenState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onRawUrlChanged: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onRequestImages: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onTagDraftChanged: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onToggleRead: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            tone = GlassCardTone.Hero,
            contentPadding = PaddingValues(DripSpacing.HeroPadding),
        ) {
            GlassSectionHeading(
                title = DripStrings.DetailSecondaryAction,
                body = "修改标题、备注和原始内容，并保持当前设计系统一致。",
            )
            Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
            GlassField(
                label = DripStrings.CaptureTitleLabel,
                value = state.editor.titleDraft,
                onValueChange = onTitleChanged,
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(DripSpacing.Small))
            GlassField(
                label = DripStrings.CaptureNoteLabel,
                value = state.editor.noteDraft,
                onValueChange = onNoteChanged,
                placeholder = DripStrings.CaptureNotePlaceholder,
            )

            when (state.contentType) {
                ContentType.LINK -> {
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    GlassField(
                        label = "链接地址",
                        value = state.editor.rawUrlDraft,
                        onValueChange = onRawUrlChanged,
                        placeholder = "输入链接",
                    )
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    GlassField(
                        label = "附加文本",
                        value = state.editor.textContentDraft,
                        onValueChange = onTextChanged,
                        placeholder = "补充链接上下文",
                    )
                }

                ContentType.TEXT -> {
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    GlassField(
                        label = "文字内容",
                        value = state.editor.textContentDraft,
                        onValueChange = onTextChanged,
                        placeholder = "输入正文",
                    )
                }

                ContentType.IMAGE -> {
                    Spacer(modifier = Modifier.height(DripSpacing.Small))
                    GlassButton(
                        text = if (state.editor.imageUris.isEmpty()) "添加图片" else "继续添加",
                        onClick = onRequestImages,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.editor.imageUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(DripSpacing.Small))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                        ) {
                            state.editor.imageUris.forEachIndexed { index, imageUri ->
                                DetailEditableImageCard(
                                    imageUri = imageUri,
                                    index = index,
                                    onRemove = { onRemoveImage(imageUri) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
            GlassSectionHeading(title = "标签")
            Spacer(modifier = Modifier.height(DripSpacing.Small))
            if (state.editor.tags.isNotEmpty()) {
                GlassChipRow {
                    state.editor.tags.forEach { tag ->
                        GlassChip(
                            text = tag,
                            selected = true,
                            onClick = { onRemoveTag(tag) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(DripSpacing.Small))
            }
            GlassField(
                label = "添加标签",
                value = state.editor.tagDraft,
                onValueChange = onTagDraftChanged,
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(DripSpacing.Small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DripSpacing.Small),
            ) {
                GlassButton(
                    text = if (state.editor.isRead) "标记未读" else "标记已读",
                    onClick = onToggleRead,
                    style = GlassButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = "加入标签",
                    onClick = onAddTag,
                    style = GlassButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(DripSpacing.SectionGap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DripSpacing.Small),
            ) {
                GlassButton(
                    text = DripStrings.CaptureCancel,
                    onClick = onDismiss,
                    style = GlassButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = "保存修改",
                    onClick = onSave,
                    enabled = state.editor.canSave,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DetailPreviewImageCard(
    imageUri: String,
    index: Int,
) {
    ExpandableImagePreview(
        imageUri = imageUri,
        contentDescription = "详情图片预览 ${index + 1}",
        modifier = Modifier
            .width(168.dp)
            .testTag("detail_image_preview_${index + 1}"),
        height = 168.dp,
    )
}

@Composable
private fun DetailEditableImageCard(
    imageUri: String,
    index: Int,
    onRemove: () -> Unit,
) {
    GlassPanel(
        modifier = Modifier.width(176.dp),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.Small)) {
            ExpandableImagePreview(
                imageUri = imageUri,
                contentDescription = "编辑图片预览 ${index + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_editor_image_preview_${index + 1}"),
                height = 140.dp,
            )
            GlassButton(
                text = "删除",
                onClick = onRemove,
                style = GlassButtonStyle.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_editor_remove_image_${index + 1}"),
            )
        }
    }
}
