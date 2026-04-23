package com.example.dripin4.ui.app

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassInfoPill
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassSectionHeading
import com.example.dripin4.ui.designsystem.components.LocalGlassBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

enum class TopBarPanel {
    Search,
    NotificationHistory,
}

@Composable
fun TopBarSearchOverlay(
    visible: Boolean,
    query: String,
    results: List<InboxItemUi>,
    onQueryChange: (String) -> Unit,
    onOpenResult: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = fadeOut(animationSpec = tween(durationMillis = 160)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("overlay_search"),
        ) {
            OverlayBackdropBlur(onDismiss = onDismiss)
            SearchInputRow(
                query = query,
                onQueryChange = onQueryChange,
                onDismiss = onDismiss,
                modifier = Modifier
                    .overlaySurfaceMotion(visible = visible)
                    .fillMaxWidth()
                    .padding(start = 18.dp, top = topInset + 72.dp, end = 18.dp),
            )
            SearchResultList(
                visible = query.isNotBlank(),
                query = query,
                results = results,
                onOpenResult = onOpenResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, top = topInset + 132.dp, end = 18.dp),
            )
        }
    }
}

@Composable
fun NotificationHistoryOverlay(
    visible: Boolean,
    history: List<NotificationHistoryUi>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopBarOverlayContainer(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier.testTag("overlay_notification_history"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassSectionHeading(title = "通知历史")
            CloseCircleButton(onClick = onDismiss)
        }
        Spacer(modifier = Modifier.height(DripSpacing.Small))

        if (history.isEmpty()) {
            OverlayEmptyState(title = "还没有推送记录")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(DripSpacing.Small),
                contentPadding = PaddingValues(bottom = DripSpacing.XSmall),
            ) {
                items(history, key = NotificationHistoryUi::id) { entry ->
                    NotificationHistoryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun TopBarOverlayContainer(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = fadeOut(animationSpec = tween(durationMillis = 160)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            OverlayBackdropBlur(onDismiss = onDismiss)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, top = topInset + 72.dp, end = 18.dp)
                    .overlaySurfaceMotion(visible = visible)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun SearchInputRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    GlassPanel(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
            )
            .testTag("search_box"),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = GlassPalette.TextTodaySubtitle,
                modifier = Modifier.size(18.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DripColors.Ink),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .testTag("search_input"),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isBlank()) {
                        Text(
                            text = "搜索标题、备注、来源或标签",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GlassPalette.TextTodaySubtitle,
                        )
                    }
                    innerTextField()
                },
            )
            CloseCircleButton(onClick = onDismiss)
        }
    }
}

@Composable
private fun SearchResultList(
    visible: Boolean,
    query: String,
    results: List<InboxItemUi>,
    onOpenResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 180)),
        exit = fadeOut(animationSpec = tween(durationMillis = 120)),
        modifier = modifier.overlaySurfaceMotion(visible = visible),
    ) {
        GlassPanel(
            modifier = Modifier.testTag("search_results"),
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        ) {
            if (results.isEmpty()) {
                Text(
                    text = "没有找到和“$query”相关的内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassPalette.TextTodaySubtitle,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 288.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(results, key = InboxItemUi::id) { item ->
                        SearchResultRow(
                            item = item,
                            onOpenResult = onOpenResult,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: InboxItemUi,
    onOpenResult: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val meta = listOf(item.source, item.tag).filter(String::isNotBlank).joinToString(" · ")

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onOpenResult(item.id) },
            )
            .testTag("search_result_${item.id}"),
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DripColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassPalette.TextTodaySubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NotificationHistoryRow(
    entry: NotificationHistoryUi,
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_history_${entry.id}"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassInfoPill(
                    text = entry.statusLabel,
                    tint = if (entry.successful) GlassPalette.AccentMint else GlassPalette.TextTodaySelection,
                )
                Text(
                    text = entry.attemptedAtLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassPalette.TextTodaySubtitle,
                )
            }
            Text(
                text = entry.countLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DripColors.Ink,
            )
        }
    }
}

@Composable
private fun OverlayEmptyState(
    title: String,
    body: String? = null,
) {
    GlassPanel(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DripColors.Ink,
            )
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassPalette.TextTodaySubtitle,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.OverlayBackdropBlur(
    onDismiss: () -> Unit,
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val blurBackdropModifier = if (backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = {
                    blur(radius = 22.dp.toPx())
                    vibrancy()
                },
                highlight = {
                    Highlight.Plain.copy(alpha = 0f, width = 0.dp)
                },
                onDrawSurface = {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.06f),
                            ),
                        ),
                    )
                },
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb2Mid.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.78f, size.height * 0.14f),
                        radius = size.minDimension * 0.84f,
                    ),
                    blendMode = BlendMode.SrcOver,
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb3Mid.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.74f),
                        radius = size.minDimension * 0.88f,
                    ),
                    blendMode = BlendMode.SrcOver,
                )
            }
    } else {
        Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.06f),
                ),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(blurBackdropModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDismiss,
            ),
    )
}

@Composable
private fun Modifier.overlaySurfaceMotion(
    visible: Boolean,
): Modifier {
    val density = LocalDensity.current
    val translationY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-18).dp,
        animationSpec = spring(
            dampingRatio = 0.88f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "overlay_surface_offset",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.965f,
        animationSpec = spring(
            dampingRatio = 0.90f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "overlay_surface_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (visible) 220 else 150,
            easing = FastOutSlowInEasing,
        ),
        label = "overlay_surface_alpha",
    )

    return graphicsLayer {
        this.translationY = with(density) { translationY.toPx() }
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

@Composable
private fun CloseCircleButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        Color.White.copy(alpha = 0.48f),
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            tint = DripColors.Ink,
            modifier = Modifier.size(16.dp),
        )
    }
}
