package com.dripin.app.feature.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dripin.app.core.model.ContentType

@Composable
fun RecommendationCard(
    card: TodayCardModel,
    onMarkRead: () -> Unit,
    onOpenLink: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                Text(
                    text = "TODAY ${card.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "${card.contentType.toLabel()} · ${card.sourceLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            card.imageUri?.let { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            when (card.contentType) {
                ContentType.LINK, ContentType.TEXT -> {
                    card.textPreview?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                ContentType.IMAGE -> {
                    card.textPreview?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
            card.note?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onMarkRead) {
                    Text("标记已读")
                }
                card.rawUrl?.let { url ->
                    Button(onClick = { onOpenLink(url) }) {
                        Text("打开原链接")
                    }
                }
            }
        }
    }
}

private fun ContentType.toLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}
