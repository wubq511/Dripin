package com.dripin.app.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onOpenLink: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item ?: return

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
            item.rawUrl?.let {
                MetaChip(text = it)
                Button(
                    onClick = { onOpenLink(it) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text("打开原链接")
                }
            }
            item.textContent?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item.imageUri?.let { uri ->
                ExpandableImagePreview(
                    imageUri = uri,
                    contentDescription = item.title ?: "image preview",
                    modifier = Modifier.fillMaxWidth(),
                    height = 260.dp,
                )
            }
        }

        SectionCard(title = "编辑") {
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
