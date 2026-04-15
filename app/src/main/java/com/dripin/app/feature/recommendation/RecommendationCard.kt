package com.dripin.app.feature.recommendation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import coil3.compose.AsyncImage
import com.dripin.app.core.designsystem.component.ActionCluster
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.model.ContentType

@Composable
fun RecommendationCard(
    card: TodayCardModel,
    onMarkRead: () -> Unit,
    onOpenLink: (String) -> Unit,
) {
    var locallyMarkedRead by rememberSaveable(card.id) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val isRead = card.isRead || locallyMarkedRead
    val panelAccent by animateColorAsState(
        targetValue = if (isRead) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        } else {
            card.contentType.accentColor()
        },
        label = "recommendation-panel-accent",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isRead) 0.94f else 1f,
        label = "recommendation-card-alpha",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isRead) 0.992f else 1f,
        label = "recommendation-card-scale",
    )

    DripinPanel(
        modifier = Modifier.graphicsLayer {
            alpha = cardAlpha
            scaleX = cardScale
            scaleY = cardScale
        },
        accentColor = panelAccent,
    ) {
        RecommendationHeader(
            card = card,
            isRead = isRead,
        )

        RecommendationPreview(
            card = card,
            isRead = isRead,
        )

        card.note?.takeIf(String::isNotBlank)?.let { note ->
            RecommendationNote(
                note = note,
                isRead = isRead,
            )
        }

        AnimatedVisibility(
            visible = isRead,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            RecommendationReadFeedback(cardId = card.id)
        }

        ActionCluster {
            OutlinedButton(
                onClick = {
                    locallyMarkedRead = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMarkRead()
                },
                enabled = !isRead,
                modifier = Modifier
                    .weight(1f)
                    .testTag("today-card-mark-read-${card.id}"),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isRead) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    },
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            ) {
                if (isRead) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isRead) "已标记已读" else "标记已读")
            }

            card.rawUrl?.let { url ->
                Button(
                    onClick = { onOpenLink(url) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text("打开原链接")
                }
            }
        }
    }
}

@Composable
private fun RecommendationHeader(
    card: TodayCardModel,
    isRead: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaChip(text = "#${card.rank}", emphasized = true)
                MetaChip(text = card.contentType.toLabel())
                MetaChip(text = card.sourceLabel)
                if (isRead) {
                    MetaChip(text = "已读")
                }
            }
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = card.rank.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isRead) 0.18f else 0.14f),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RecommendationPreview(
    card: TodayCardModel,
    isRead: Boolean,
) {
    card.imageUri?.let { imageUri ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            ),
            tonalElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(228.dp),
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xA615171A),
                                ),
                            ),
                        ),
                )
                MetaChip(
                    text = "图片预览",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                )
                card.textPreview?.takeIf(String::isNotBlank)?.let { preview ->
                    Text(
                        text = preview,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = if (isRead) 0.8f else 0.92f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    } ?: run {
        val previewText = card.textPreview?.takeIf(String::isNotBlank) ?: card.rawUrl
        previewText?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                ),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = if (card.contentType == ContentType.LINK) "内容摘要" else "内容摘录",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationNote(
    note: String,
    isRead: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isRead) 0.06f else 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "备注",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecommendationReadFeedback(cardId: Long) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today-card-read-feedback-$cardId"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        ),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "已标记已读",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ContentType.accentColor() = when (this) {
    ContentType.LINK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
    ContentType.TEXT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f)
    ContentType.IMAGE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.09f)
}

private fun ContentType.toLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}
