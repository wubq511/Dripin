package com.dripin.app.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dripin.app.core.model.ContentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveItemScreen(
    viewModel: SaveItemViewModel,
    onDone: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.completedItemId) {
        if (uiState.completedItemId != null) {
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "保存内容") },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (uiState.isSaving) "保存中..." else uiState.saveActionLabel)
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.duplicateExistingItemId != null) {
                Card {
                    Text(
                        text = "检测到相同链接，保存后会更新已有内容。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标题") },
                singleLine = true,
            )

            when (uiState.contentType) {
                ContentType.LINK -> SummaryCard(
                    title = "链接",
                    content = uiState.sharedUrl.orEmpty(),
                )

                ContentType.TEXT -> SummaryCard(
                    title = "文字内容",
                    content = uiState.sharedText.orEmpty(),
                )

                ContentType.IMAGE -> {
                    SummaryCard(
                        title = "图片来源",
                        content = uiState.sharedImageUri.orEmpty(),
                    )
                    if (!uiState.sharedImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.sharedImageUri,
                            contentDescription = uiState.title.ifBlank { "Shared image preview" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    }
                }
            }

            SummaryCard(
                title = "来源",
                content = listOfNotNull(
                    uiState.sourceAppLabel,
                    uiState.sourcePlatform,
                    uiState.sourceDomain,
                ).joinToString(" · ").ifBlank { "未识别" },
            )

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
                minLines = 3,
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.tags.isNotEmpty()) {
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
                }

                OutlinedTextField(
                    value = uiState.draftTag,
                    onValueChange = viewModel::onDraftTagChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("添加标签") },
                    trailingIcon = {
                        Text(
                            text = "添加",
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
                Button(
                    onClick = viewModel::addDraftTag,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text("加入标签")
                }
            }

            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    content: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = content.ifBlank { "暂无" },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
