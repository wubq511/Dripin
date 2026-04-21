package com.example.dripin4.ui.features.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.dripin4.ui.app.InboxScreenState
import com.example.dripin4.ui.app.InboxItemUi
import com.example.dripin4.ui.app.InboxKind
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassChip
import com.example.dripin4.ui.designsystem.components.GlassPageHeader
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassScaffold
import com.example.dripin4.ui.features.shared.EmptyStateCard
import com.example.dripin4.ui.features.shared.NoResultStateCard

@Composable
fun InboxScreen(
    state: InboxScreenState,
    onFilterSelected: (com.example.dripin4.ui.app.InboxFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassScaffold(
        testTag = "screen_inbox",
        modifier = modifier,
        header = {
            GlassPageHeader(
                title = DripStrings.InboxTitle,
            )
        },
    ) {
        item("filters") {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
            ) {
                state.filters.forEach { filter ->
                    GlassChip(
                        text = DripStrings.inboxFilterLabel(filter),
                        selected = state.selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        testTag = "chip_filter_${filter.name.lowercase()}",
                    )
                }
            }
        }

        when {
            state.items.isEmpty() && state.selectedFilter == com.example.dripin4.ui.app.InboxFilter.All -> {
                item("empty_state") {
                    EmptyStateCard(
                        title = DripStrings.InboxEmptyTitle,
                        body = DripStrings.InboxEmptyBody,
                    )
                }
            }

            state.items.isEmpty() -> {
                item("no_result_state") {
                    NoResultStateCard(
                        title = DripStrings.InboxNoResultTitle,
                        body = DripStrings.InboxNoResultBody,
                    )
                }
            }

            else -> {
                items(
                    items = state.items,
                    key = { it.id },
                ) { item ->
                    InboxItemCard(
                        item = item,
                        onOpenDetail = onOpenDetail,
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxItemCard(
    item: InboxItemUi,
    onOpenDetail: (String) -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(item.id) },
        tone = GlassCardTone.Neutral,
        contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = inboxKindLabel(item.kind),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.08.sp,
                ),
                color = GlassPalette.TextTodayItemTitle,
            )
            Text(
                text = item.tag,
                style = MaterialTheme.typography.labelMedium,
                color = GlassPalette.TextTodayItemTitle,
            )
        }

        Spacer(modifier = Modifier.height(DripSpacing.Small))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            color = DripColors.Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(DripSpacing.XSmall))

        Text(
            text = "${item.source} · ${item.time}",
            style = MaterialTheme.typography.labelMedium,
            color = GlassPalette.TextTodaySubtitle,
        )

        Spacer(modifier = Modifier.height(DripSpacing.Small))

        GlassPanel {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DripColors.Graphite,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun inboxKindLabel(kind: InboxKind): String = when (kind) {
    InboxKind.Article -> DripStrings.InboxKindArticle
    InboxKind.Video -> DripStrings.InboxKindVideo
    InboxKind.Image -> DripStrings.InboxKindImage
    InboxKind.Thread -> DripStrings.InboxKindThread
}
