package com.dripin.app.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.DripinBackground
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.ExpandableImagePreview
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.model.ContentType

private val fieldShape = RoundedCornerShape(20.dp)

@Composable
fun SaveItemScreen(
    viewModel: SaveItemViewModel,
    onDone: () -> Unit,
    onPickImage: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.completedItemId) {
        if (uiState.completedItemId != null) {
            onDone()
        }
    }

    DripinBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                SaveActionBar(
                    enabled = !uiState.isSaving && uiState.canSave,
                    label = if (uiState.isSaving) "保存中..." else uiState.saveActionLabel,
                    onClick = viewModel::save,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                SaveHeader(uiState = uiState)

                DripinPanel(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = heroTitle(uiState),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaChip(text = uiState.contentType.label(), emphasized = true)
                        uiState.sourceAppLabel?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                        uiState.sourcePlatform?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                        uiState.sourceDomain?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                    }
                }

                if (uiState.duplicateExistingItemId != null) {
                    DripinPanel {
                        Text(
                            text = "检测到相同链接，继续保存会合并到原条目。",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                if (uiState.isManualEntry) {
                    SectionCard(title = "类型") {
                        FilterChipRow(
                            options = ContentType.entries,
                            selected = uiState.contentType,
                            labelOf = ContentType::label,
                            onSelected = viewModel::setContentType,
                        )
                    }
                }

                SectionCard(title = "标题与备注") {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        label = { Text("标题") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::onNoteChanged,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        label = { Text("备注") },
                        minLines = 3,
                    )
                }

                SectionCard(title = contentSectionTitle(uiState.contentType, uiState.isManualEntry)) {
                    when (uiState.contentType) {
                        ContentType.LINK -> {
                            OutlinedTextField(
                                value = uiState.sharedUrl.orEmpty(),
                                onValueChange = viewModel::onSharedUrlChanged,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                label = { Text("链接") },
                                minLines = 2,
                            )
                            OutlinedTextField(
                                value = uiState.sharedText.orEmpty(),
                                onValueChange = viewModel::onSharedTextChanged,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                label = { Text("附加文本") },
                                minLines = 2,
                            )
                        }

                        ContentType.TEXT -> {
                            OutlinedTextField(
                                value = uiState.sharedText.orEmpty(),
                                onValueChange = viewModel::onSharedTextChanged,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                label = { Text("文字内容") },
                                minLines = 6,
                            )
                        }

                        ContentType.IMAGE -> {
                            if (uiState.isManualEntry) {
                                Button(
                                    onClick = { onPickImage?.invoke() },
                                    enabled = onPickImage != null,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = if (uiState.sharedImageUri.isNullOrBlank()) {
                                            "选择图片"
                                        } else {
                                            "重新选择"
                                        },
                                    )
                                }
                            }
                            if (!uiState.sharedImageUri.isNullOrBlank()) {
                                ExpandableImagePreview(
                                    imageUri = uiState.sharedImageUri.orEmpty(),
                                    contentDescription = uiState.title.ifBlank { "Shared image preview" },
                                    height = 240.dp,
                                )
                            } else if (uiState.isManualEntry) {
                                Text(
                                    text = "还没有选择图片",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (uiState.autoTags.isNotEmpty()) {
                    SectionCard(title = "识别结果") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.autoTags.forEach { tag ->
                                MetaChip(text = tag, emphasized = tag.equals(uiState.sourcePlatform, ignoreCase = true))
                            }
                        }
                    }
                }

                SectionCard(title = "标签") {
                    if (uiState.userTags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.userTags.forEach { tag ->
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.removeTag(tag) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    label = { Text(tag) },
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
                    }

                    OutlinedTextField(
                        value = uiState.draftTag,
                        onValueChange = viewModel::onDraftTagChanged,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        label = { Text("添加标签") },
                    )
                    Button(
                        onClick = viewModel::addDraftTag,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("加入标签")
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
private fun SaveHeader(
    uiState: SaveItemUiState,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (uiState.isManualEntry) "手动添加" else "保存内容",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        MetaChip(
            text = when {
                uiState.isManualEntry -> "手动录入"
                uiState.duplicateExistingItemId != null -> "更新模式"
                else -> "新收录"
            },
            emphasized = true,
        )
    }
}

@Composable
private fun SaveActionBar(
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(12.dp, shape = MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.36f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
            ),
        ) {
            Text(text = label)
        }
    }
}

private fun heroTitle(uiState: SaveItemUiState): String {
    return when {
        uiState.title.isNotBlank() -> uiState.title
        uiState.contentType == ContentType.LINK && uiState.isManualEntry -> "手动补一条链接"
        uiState.contentType == ContentType.LINK -> "把这条链接先收进来"
        uiState.contentType == ContentType.TEXT && uiState.isManualEntry -> "手动补一段文字"
        uiState.contentType == ContentType.TEXT -> "把这段文字先安放好"
        uiState.contentType == ContentType.IMAGE && uiState.isManualEntry -> "手动补一张图片"
        else -> "把这张图片先留住"
    }
}

private fun contentSectionTitle(
    contentType: ContentType,
    isManualEntry: Boolean,
): String = when (contentType) {
    ContentType.LINK -> if (isManualEntry) "链接内容" else "链接预览"
    ContentType.TEXT -> "文字内容"
    ContentType.IMAGE -> "图片"
}

private fun ContentType.label(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}
