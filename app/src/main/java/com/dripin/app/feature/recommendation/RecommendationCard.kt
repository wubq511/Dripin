package com.dripin.app.feature.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dripin.app.core.designsystem.component.ActionCluster
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.model.ContentType
import coil3.compose.AsyncImage

@Composable
fun RecommendationCard(
    card: TodayCardModel,
    onMarkRead: () -> Unit,
    onOpenLink: (String) -> Unit,
) {
    DripinPanel(
        accentColor = card.contentType.accentColor(),
    ) {
        card.imageUri?.let { imageUri ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .clip(RoundedCornerShape(28.dp)),
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
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xBA15171A),
                                ),
                            ),
                        ),
                )
                Text(
                    text = card.rank.toString().padStart(2, '0'),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    fontWeight = FontWeight.Bold,
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaChip(text = card.contentType.toLabel(), emphasized = true)
                        MetaChip(text = card.sourceLabel)
                    }
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (card.imageUri == null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = card.rank.toString().padStart(2, '0'),
                    modifier = Modifier.align(Alignment.TopEnd),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    fontWeight = FontWeight.Bold,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaChip(text = "#${card.rank}", emphasized = true)
                        MetaChip(text = card.contentType.toLabel())
                        MetaChip(text = card.sourceLabel)
                    }
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        card.textPreview?.takeIf(String::isNotBlank)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        card.note?.takeIf(String::isNotBlank)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ActionCluster {
            OutlinedButton(
                onClick = onMarkRead,
                modifier = Modifier.weight(1f),
            ) {
                Text("标记已读")
            }
            card.rawUrl?.let { url ->
                Button(
                    onClick = { onOpenLink(url) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("打开原链接")
                }
            }
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
