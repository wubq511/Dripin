package com.dripin.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenItem: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "筛选") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChipRow(
                        options = listOf<ContentType?>(null, ContentType.LINK, ContentType.TEXT, ContentType.IMAGE),
                        selected = uiState.filterState.contentType,
                        labelOf = { option ->
                            when (option) {
                                null -> "全部类型"
                                ContentType.LINK -> "链接"
                                ContentType.TEXT -> "文字"
                                ContentType.IMAGE -> "图片"
                            }
                        },
                        onSelected = viewModel::onContentTypeChanged,
                    )
                    FilterChipRow(
                        options = ReadFilter.entries,
                        selected = uiState.filterState.readFilter,
                        labelOf = {
                            when (it) {
                                ReadFilter.ALL -> "全部阅读状态"
                                ReadFilter.READ -> "已读"
                                ReadFilter.UNREAD -> "未读"
                            }
                        },
                        onSelected = viewModel::onReadFilterChanged,
                    )
                    FilterChipRow(
                        options = PushFilter.entries,
                        selected = uiState.filterState.pushFilter,
                        labelOf = {
                            when (it) {
                                PushFilter.ALL -> "全部推送状态"
                                PushFilter.PUSHED -> "已推送"
                                PushFilter.UNPUSHED -> "未推送"
                            }
                        },
                        onSelected = viewModel::onPushFilterChanged,
                    )
                }
            }
        }

        if (uiState.items.isEmpty()) {
            item {
                Text(
                    text = "还没有保存内容，先从分享面板塞一条进来。",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                )
            }
        } else {
            items(uiState.items, key = { it.id }) { item ->
                SavedItemCard(
                    item = item,
                    onClick = { onOpenItem(item.id) },
                )
            }
        }
    }
}
