package com.dripin.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.DripinHero
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.designsystem.component.StatChip
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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            DripinHero(
                eyebrow = "Dripin",
                title = "把稍后再看，\n收成自己的清单",
                subtitle = "更干净地收，更舒服地回看。",
                badge = "${uiState.totalCount} 条",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatChip(
                        label = "未读",
                        value = uiState.unreadCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        label = "待推送",
                        value = uiState.queuedCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        label = "当前列表",
                        value = uiState.items.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            SectionCard(title = "筛选面板") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FilterGroup(title = "内容类型") {
                        FilterChipRow(
                            options = listOf<ContentType?>(null, ContentType.LINK, ContentType.TEXT, ContentType.IMAGE),
                            selected = uiState.filterState.contentType,
                            labelOf = { option ->
                                when (option) {
                                    null -> "全部"
                                    ContentType.LINK -> "链接"
                                    ContentType.TEXT -> "文字"
                                    ContentType.IMAGE -> "图片"
                                }
                            },
                            onSelected = viewModel::onContentTypeChanged,
                        )
                    }
                    FilterGroup(title = "阅读状态") {
                        FilterChipRow(
                            options = ReadFilter.entries,
                            selected = uiState.filterState.readFilter,
                            labelOf = {
                                when (it) {
                                    ReadFilter.ALL -> "全部"
                                    ReadFilter.READ -> "已读"
                                    ReadFilter.UNREAD -> "未读"
                                }
                            },
                            onSelected = viewModel::onReadFilterChanged,
                        )
                    }
                    FilterGroup(title = "推送状态") {
                        FilterChipRow(
                            options = PushFilter.entries,
                            selected = uiState.filterState.pushFilter,
                            labelOf = {
                                when (it) {
                                    PushFilter.ALL -> "全部"
                                    PushFilter.PUSHED -> "已推送"
                                    PushFilter.UNPUSHED -> "未推送"
                                }
                            },
                            onSelected = viewModel::onPushFilterChanged,
                        )
                    }
                }
            }
        }

        if (uiState.items.isEmpty()) {
            item {
                DripinPanel {
                    Text(text = "还没有内容")
                    Text(
                        text = "先从分享面板收一条进来，首页会从这里慢慢长起来。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            itemsIndexed(uiState.items, key = { _, item -> item.id }) { index, item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { (it * 0.25f).toInt() + index * 18 }),
                ) {
                    SavedItemCard(
                        item = item,
                        onClick = { onOpenItem(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )
        content()
    }
}
