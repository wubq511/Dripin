package com.dripin.app.feature.recommendation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.SectionCard
import java.time.format.DateTimeFormatter

private val todayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M 月 d 日")

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenLink: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "今日回看") {
                Text(
                    text = "${uiState.date.format(todayFormatter)} · ${uiState.cards.size} 条候选",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "今天的内容更偏向轻提醒：同一套视觉语言，但卡片会更聚焦阅读动作。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.cards.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "今天还没有生成推荐。等到下一个提醒时间，或者先去首页多存几条内容。",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
            itemsIndexed(uiState.cards, key = { _, card -> card.id }) { index, card ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 * 2 }),
                ) {
                    RecommendationCard(
                        card = card,
                        onMarkRead = { viewModel.markRead(card.id) },
                        onOpenLink = onOpenLink,
                    )
                }
            }
        }
    }
}
