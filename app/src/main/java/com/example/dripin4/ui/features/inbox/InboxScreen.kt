package com.example.dripin4.ui.features.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
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
import com.example.dripin4.ui.designsystem.components.GlassInfoPill
import com.example.dripin4.ui.designsystem.components.GlassPageHeader
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassScaffold
import com.example.dripin4.ui.features.shared.EmptyStateCard
import com.example.dripin4.ui.features.shared.NoResultStateCard

@Composable
fun InboxScreen(
    state: InboxScreenState,
    onContentFilterSelected: (com.example.dripin4.ui.app.InboxFilter) -> Unit,
    onReadFilterSelected: (ReadFilter) -> Unit,
    onPushFilterSelected: (PushFilter) -> Unit,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(DripSpacing.Small),
            ) {
                FilterGroup(
                    label = "内容类型",
                    content = {
                        state.contentFilters.forEach { filter ->
                            GlassChip(
                                text = DripStrings.inboxFilterLabel(filter),
                                selected = state.selectedContentFilter == filter,
                                onClick = { onContentFilterSelected(filter) },
                                testTag = "chip_filter_${filter.name.lowercase()}",
                            )
                        }
                    },
                )
                FilterGroup(
                    label = "阅读状态",
                    content = {
                        state.readFilters.forEach { filter ->
                            GlassChip(
                                text = DripStrings.inboxReadFilterLabel(filter),
                                selected = state.selectedReadFilter == filter,
                                onClick = { onReadFilterSelected(filter) },
                                testTag = "chip_read_${filter.name.lowercase()}",
                            )
                        }
                    },
                )
                FilterGroup(
                    label = "推送状态",
                    content = {
                        state.pushFilters.forEach { filter ->
                            GlassChip(
                                text = DripStrings.inboxPushFilterLabel(filter),
                                selected = state.selectedPushFilter == filter,
                                onClick = { onPushFilterSelected(filter) },
                                testTag = "chip_push_${filter.name.lowercase()}",
                            )
                        }
                    },
                )
            }
        }

        when {
            state.items.isEmpty() && !state.hasActiveFilters -> {
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
private fun FilterGroup(
    label: String,
    content: @Composable RowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GlassPalette.TextTodaySubtitle,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
            content = content,
        )
    }
}

@Composable
private fun InboxItemCard(
    item: InboxItemUi,
    onOpenDetail: (String) -> Unit,
) {
    val cardMeta = listOf(item.source, item.time).filter(String::isNotBlank).joinToString(" · ")
    val decorativeTag = item.decorativeTagOrNull()
    val summaryText = item.summaryTextOrNull()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(item.id) }
            .testTag("inbox_card_${item.id}"),
        tone = GlassCardTone.Neutral,
        contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassInfoPill(
                text = item.contentContextLabel(),
                tint = GlassPalette.TextTodaySelection,
                modifier = Modifier.testTag("inbox_context_kind_${item.id}"),
            )
            decorativeTag?.let { tag ->
                GlassInfoPill(
                    text = tag,
                    tint = GlassPalette.AccentMint,
                    modifier = Modifier.testTag("inbox_context_tag_${item.id}"),
                )
            }
        }

        Spacer(modifier = Modifier.height(DripSpacing.Small))

        Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.18).sp,
                ),
                color = DripColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (cardMeta.isNotBlank()) {
                Text(
                    text = cardMeta,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassPalette.TextTodayItemMeta,
                )
            }
        }

        Spacer(modifier = Modifier.height(DripSpacing.Small))

        FlowRow(
            modifier = Modifier.testTag("inbox_status_group_${item.id}"),
            horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
            verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
        ) {
            GlassInfoPill(
                text = DripStrings.inboxReadFilterLabel(if (item.isRead) ReadFilter.READ else ReadFilter.UNREAD),
                tint = if (item.isRead) GlassPalette.TextTodaySubtitle else GlassPalette.TextTodaySelection,
                modifier = Modifier.testTag("inbox_status_read_${item.id}"),
            )
            GlassInfoPill(
                text = item.pushStatusLabel(),
                tint = if (item.pushCount > 0) GlassPalette.AccentMint else GlassPalette.TextTodaySubtitle,
                modifier = Modifier.testTag("inbox_status_push_${item.id}"),
            )
        }

        summaryText?.let { note ->
            Spacer(modifier = Modifier.height(DripSpacing.Small))
            GlassPanel(
                modifier = Modifier.testTag("inbox_note_${item.id}"),
                contentPadding = PaddingValues(
                    horizontal = DripSpacing.Medium,
                    vertical = DripSpacing.Small,
                ),
            ) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DripColors.Graphite,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
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

internal fun InboxItemUi.contentContextLabel(): String = when (kind) {
    InboxKind.Thread -> DripStrings.InboxFilterText
    else -> inboxKindLabel(kind)
}

internal fun InboxItemUi.decorativeTagOrNull(): String? {
    val normalizedTag = tag.trim()
    if (normalizedTag.isBlank()) return null

    return normalizedTag.takeUnless {
        it == inboxKindLabel(kind) || it == contentType.toContentLabel()
    }
}

internal fun InboxItemUi.summaryTextOrNull(): String? = note.trim().takeIf(String::isNotBlank)

internal fun InboxItemUi.pushStatusLabel(): String = when {
    pushCount > 0 -> "已推送 ${pushCount} 次"
    else -> DripStrings.InboxPushFilterUnpushed
}

private fun com.dripin.app.core.model.ContentType.toContentLabel(): String = when (this) {
    com.dripin.app.core.model.ContentType.LINK -> DripStrings.InboxFilterLink
    com.dripin.app.core.model.ContentType.TEXT -> DripStrings.InboxFilterText
    com.dripin.app.core.model.ContentType.IMAGE -> DripStrings.InboxFilterImage
}
