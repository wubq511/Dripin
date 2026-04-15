package com.dripin.app.feature.recommendation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.DripinHero
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.StatChip
import java.time.format.DateTimeFormatter

private val todayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M 月 d 日")

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenLink: (String) -> Unit,
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
                eyebrow = "Today's Picks",
                title = "今天，回看一点\n你之前留住的东西",
                subtitle = "不是催促，只是把旧收藏轻轻推回你眼前。",
                badge = uiState.date.format(todayFormatter),
            ) {
                StatChip(label = "推荐数量", value = uiState.cards.size.toString())
            }
        }

        if (uiState.cards.isEmpty()) {
            item {
                DripinPanel {
                    MetaChip(text = "今日暂空", emphasized = true)
                    Text(
                        text = "今天还没有生成推荐。等到下一个提醒时间，或者先去首页多收几条内容。",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
            itemsIndexed(uiState.cards, key = { _, card -> card.id }) { index, card ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { (it * 0.35f).toInt() + index * 20 }),
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
