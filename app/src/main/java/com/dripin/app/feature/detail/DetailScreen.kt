package com.dripin.app.feature.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.ActionCluster
import com.dripin.app.core.designsystem.component.DripinHero
import com.dripin.app.core.designsystem.component.ExpandableImagePreview
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.model.ContentType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val detailFieldShape = RoundedCornerShape(20.dp)
private val detailContentShape = RoundedCornerShape(24.dp)

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onOpenLink: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item ?: return
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        viewModel.addImageUris(uris.map(Uri::toString))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        DripinHero(
            eyebrow = item.contentType.detailEyebrow(),
            title = item.title ?: "(无标题)",
            subtitle = listOf(
                item.sourcePlatform ?: item.sourceDomain ?: "未知来源",
                item.createdAt.atZone(ZoneId.systemDefault()).format(detailFormatter),
            ).joinToString(" / "),
            badge = if (item.isRead) "已读" else "未读",
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaChip(text = item.contentType.detailLabel(), emphasized = true)
                MetaChip(text = "推送 ${item.pushCount} 次")
                item.sourceAppLabel?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
            }
        }

        SectionCard(title = "内容") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (item.contentType) {
                    ContentType.LINK -> {
                        item.rawUrl?.takeIf(String::isNotBlank)?.let { rawUrl ->
                            ContentBlock(
                                label = "原链接",
                                value = rawUrl,
                                actionLabel = "打开原链接",
                                onAction = { onOpenLink(rawUrl) },
                            )
                        }
                        item.textContent?.takeIf(String::isNotBlank)?.let { text ->
                            ContentBlock(
                                label = "附加文本",
                                value = text,
                            )
                        }
                    }

                    ContentType.TEXT -> {
                        item.textContent?.takeIf(String::isNotBlank)?.let { text ->
                            ContentBlock(
                                label = "正文",
                                value = text,
                            )
                        } ?: Text(
                            text = "暂无正文",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    ContentType.IMAGE -> {
                        if (item.imageUris.isEmpty()) {
                            Text(
                                text = "暂无图片",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "共 ${item.imageUris.size} 张",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                item.imageUris.forEachIndexed { index, uri ->
                                    DetailImageCard(
                                        imageUri = uri,
                                        contentDescription = item.title ?: "image preview ${index + 1}",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SectionCard(title = "编辑") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.titleDraft,
                    onValueChange = viewModel::onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    shape = detailFieldShape,
                    label = { Text("标题") },
                )
                OutlinedTextField(
                    value = uiState.noteDraft,
                    onValueChange = viewModel::onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    shape = detailFieldShape,
                    label = { Text("备注") },
                    minLines = 3,
                )

                when (item.contentType) {
                    ContentType.LINK -> {
                        OutlinedTextField(
                            value = uiState.rawUrlDraft,
                            onValueChange = viewModel::onRawUrlChanged,
                            modifier = Modifier.fillMaxWidth(),
                            shape = detailFieldShape,
                            label = { Text("链接地址") },
                            minLines = 2,
                        )
                        OutlinedTextField(
                            value = uiState.textContentDraft,
                            onValueChange = viewModel::onTextContentChanged,
                            modifier = Modifier.fillMaxWidth(),
                            shape = detailFieldShape,
                            label = { Text("附加文本") },
                            minLines = 4,
                        )
                    }

                    ContentType.TEXT -> {
                        OutlinedTextField(
                            value = uiState.textContentDraft,
                            onValueChange = viewModel::onTextContentChanged,
                            modifier = Modifier.fillMaxWidth(),
                            shape = detailFieldShape,
                            label = { Text("正文") },
                            minLines = 6,
                        )
                    }

                    ContentType.IMAGE -> {
                        Button(onClick = { imagePicker.launch("image/*") }) {
                            Text(if (uiState.imageUriDrafts.isEmpty()) "添加图片" else "继续添加")
                        }
                        if (uiState.imageUriDrafts.isEmpty()) {
                            Text(
                                text = "至少保留一张图片",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "当前 ${uiState.imageUriDrafts.size} 张",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                uiState.imageUriDrafts.forEachIndexed { index, imageUri ->
                                    DetailEditableImageCard(
                                        imageUri = imageUri,
                                        contentDescription = uiState.titleDraft.ifBlank { "editable image ${index + 1}" },
                                        onRemove = { viewModel.removeImageUri(imageUri) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SectionCard(title = "标签") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.sourceDomain?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                item.topicCategory?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                uiState.tags.forEach { tag ->
                    AssistChip(
                        onClick = { viewModel.removeTag(tag) },
                        label = { Text(tag) },
                    )
                }
            }
            OutlinedTextField(
                value = uiState.tagDraft,
                onValueChange = viewModel::onTagDraftChanged,
                modifier = Modifier.fillMaxWidth(),
                shape = detailFieldShape,
                label = { Text("添加标签") },
            )
            OutlinedButton(onClick = viewModel::addTag) {
                Text("加入标签")
            }
        }

        SectionCard(title = "动作") {
            ActionCluster {
                Button(
                    onClick = viewModel::saveEdits,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.canSaveEdits,
                ) {
                    Text("保存修改")
                }
                if (item.isRead) {
                    OutlinedButton(
                        onClick = viewModel::markUnread,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("标记未读")
                    }
                } else {
                    OutlinedButton(
                        onClick = viewModel::markRead,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("标记已读")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DetailImageCard(
    imageUri: String,
    contentDescription: String,
) {
    ExpandableImagePreview(
        imageUri = imageUri,
        contentDescription = contentDescription,
        modifier = Modifier.width(160.dp),
        height = 160.dp,
    )
}

@Composable
private fun DetailEditableImageCard(
    imageUri: String,
    contentDescription: String,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.width(160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpandableImagePreview(
            imageUri = imageUri,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxWidth(),
            height = 160.dp,
        )
        OutlinedButton(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("删除")
        }
    }
}

@Composable
private fun ContentBlock(
    label: String,
    value: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = detailContentShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun ContentType.detailEyebrow(): String = when (this) {
    ContentType.LINK -> "Link Archive"
    ContentType.TEXT -> "Text Archive"
    ContentType.IMAGE -> "Image Archive"
}

private fun ContentType.detailLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}
