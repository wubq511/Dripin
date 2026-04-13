package com.dripin.app.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dripin.app.core.designsystem.component.SectionCard

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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "内容") {
            Text(item.title ?: "(无标题)", style = MaterialTheme.typography.headlineSmall)
            item.rawUrl?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = { onOpenLink(it) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text("打开原链接")
                }
            }
            item.textContent?.let { Text(it) }
            item.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = item.title ?: "image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
            Text("来源: ${item.sourcePlatform ?: item.sourceDomain ?: "未知"}")
            Text("推送次数: ${item.pushCount}")
        }

        SectionCard(title = "编辑") {
            OutlinedTextField(
                value = uiState.titleDraft,
                onValueChange = viewModel::onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标题") },
            )
            OutlinedTextField(
                value = uiState.noteDraft,
                onValueChange = viewModel::onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
                minLines = 3,
            )
        }

        SectionCard(title = "标签") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.tags.forEach { tag ->
                    AssistChip(
                        onClick = { viewModel.removeTag(tag) },
                        label = { Text("$tag ×") },
                    )
                }
            }
            OutlinedTextField(
                value = uiState.tagDraft,
                onValueChange = viewModel::onTagDraftChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("添加标签") },
            )
            Button(onClick = viewModel::addTag) {
                Text("加入标签")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = viewModel::saveEdits, modifier = Modifier.fillMaxWidth()) {
                    Text("保存修改")
                }
                if (item.isRead) {
                    Button(onClick = viewModel::markUnread, modifier = Modifier.fillMaxWidth()) {
                        Text("标记为未读")
                    }
                } else {
                    Button(onClick = viewModel::markRead, modifier = Modifier.fillMaxWidth()) {
                        Text("标记为已读")
                    }
                }
            }
        }
    }
}
