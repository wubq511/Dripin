package com.dripin.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val cardTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

@Composable
fun SavedItemCard(
    item: SavedItemEntity,
    onClick: () -> Unit,
) {
    DripinPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accentColor = when (item.contentType) {
            ContentType.LINK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ContentType.TEXT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ContentType.IMAGE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!item.imageUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp)
                        .clip(RoundedCornerShape(28.dp)),
                ) {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = item.title ?: "image preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xA614171A),
                                    ),
                                ),
                            ),
                    )
                    FlowRow(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaChip(text = item.contentType.toLabel(), emphasized = true)
                        MetaChip(text = if (item.isRead) "已读" else "未读")
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = item.title ?: "(无标题)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = listOf(
                                item.sourcePlatform ?: item.sourceDomain ?: "未知来源",
                                item.createdAt.atZone(ZoneId.systemDefault()).format(cardTimeFormatter),
                            ).joinToString(" / "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.86f),
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.contentType.watermark(),
                        modifier = Modifier.align(Alignment.TopEnd),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        fontWeight = FontWeight.Bold,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MetaChip(text = item.contentType.toLabel(), emphasized = true)
                            MetaChip(text = if (item.isRead) "已读" else "未读")
                            if (item.pushCount > 0) {
                                MetaChip(text = "已推送 ${item.pushCount} 次")
                            }
                        }
                        Text(
                            text = listOf(
                                item.sourcePlatform ?: item.sourceDomain ?: "未知来源",
                                item.createdAt.atZone(ZoneId.systemDefault()).format(cardTimeFormatter),
                            ).joinToString(" / "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = item.title ?: "(无标题)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            item.textContent?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            item.note?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.sourceAppLabel?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
                item.sourceDomain?.takeIf(String::isNotBlank)?.let { MetaChip(text = it) }
            }
        }
    }
}

private fun ContentType.toLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}

private fun ContentType.watermark(): String = when (this) {
    ContentType.LINK -> "LINK"
    ContentType.TEXT -> "TEXT"
    ContentType.IMAGE -> "IMAGE"
}
