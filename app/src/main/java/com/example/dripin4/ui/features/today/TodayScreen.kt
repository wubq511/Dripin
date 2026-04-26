package com.example.dripin4.ui.features.today

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dripin4.ui.app.TodayItemUi
import com.example.dripin4.ui.app.TodayScreenState
import com.example.dripin4.ui.app.TodaySectionUi
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassBackdropAtmosphereLayer
import com.example.dripin4.ui.designsystem.components.liquidGlassSurface
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

private val ItemShape = RoundedCornerShape(18.dp)
private val ItemInnerShape = RoundedCornerShape(14.dp)
private val HeroShape = RoundedCornerShape(18.dp)
private val HeroPillColor = Color(0xFF1F7A4A)
private val HeroTitleGradient = listOf(
    Color(0xFF1F7A4A),
    Color(0xFF4FC98A),
    Color(0xFF9FE07A),
)

fun Modifier.customShadow(
    color: Color = DripColors.PureBlack,
    alpha: Float = 0.06f,
    offsetX: androidx.compose.ui.unit.Dp = 0.dp,
    offsetY: androidx.compose.ui.unit.Dp = 6.dp,
    blur: androidx.compose.ui.unit.Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = ItemShape
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = color.copy(alpha = 0f).toArgb()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            blur.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), frameworkPaint)
    }
}

@Composable
fun TodayScreen(
    state: TodayScreenState,
    onOpenDetail: (String) -> Unit,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val visualSections = remember(state.sections) { state.sections.toTodayVisualSections() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_today"),
    ) {
        TodayAtmosphereLayer(
            backdrop = backdrop,
            modifier = Modifier.fillMaxSize(),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 120.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 142.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("header") {
                Column {
                    Text(
                        text = DripStrings.TodayTitle,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.35).sp,
                            color = GlassPalette.TextTodaySelection,
                        ),
                    )
                }
            }

            item("hero") {
                Column {
                    TodayHeroTimePill(timeText = state.heroTimeText)
                    Spacer(modifier = Modifier.height(8.dp))
                    TodayHeroCard(backdrop = backdrop)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            visualSections.forEach { section ->
                item("section_${section.id}") {
                    TodayDateDivider(
                        label = section.label,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                    )
                }
                items(
                    items = section.items,
                    key = { item -> item.id },
                ) { visualItem ->
                    TodayItemCard(
                        item = visualItem,
                        onClick = { onOpenDetail(visualItem.id) },
                        backdrop = backdrop,
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayDateDivider(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_date_divider_$label"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(GlassPalette.TextTodaySelection.copy(alpha = 0.14f)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = GlassPalette.TextTodaySelection.copy(alpha = 0.52f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
            ),
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(GlassPalette.TextTodaySelection.copy(alpha = 0.14f)),
        )
    }
}

@Composable
private fun TodayHeroTimePill(
    timeText: String,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(pillShape)
            .background(DripColors.PureWhite.copy(alpha = 0.62f), pillShape)
            .border(1.dp, DripColors.PureWhite.copy(alpha = 0.82f), pillShape)
            .border(0.5.dp, DripColors.HairlineSoft, pillShape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = HeroPillColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = timeText,
            style = MaterialTheme.typography.titleSmall.copy(
                color = HeroPillColor,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun TodayHeroCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )
    val titleBrush = Brush.linearGradient(
        colors = HeroTitleGradient,
        start = Offset.Zero,
        end = Offset.Infinite,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .customShadow(
                color = DripColors.PureBlack,
                alpha = 0.10f,
                offsetY = 20.dp,
                blur = 44.dp,
                shape = HeroShape,
            )
            .customShadow(
                color = DripColors.PureBlack,
                alpha = 0.07f,
                offsetY = 6.dp,
                blur = 18.dp,
                shape = HeroShape,
            )
            .liquidGlassSurface(
                backdrop = backdrop,
                shape = HeroShape,
                fillAlpha = 0.62f,
                blurRadius = 28.dp,
                withTopHighlight = true,
                withHairline = true,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                text = "Slow Down\nMore Clear",
                style = TextStyle(
                    brush = titleBrush,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun TodayItemCard(
    item: TodayVisualItem,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_item_card_${item.id}")
            .customShadow(
                color = DripColors.PureBlack,
                alpha = 0.06f,
                offsetY = 6.dp,
                blur = 16.dp,
                shape = ItemShape
            )
            .then(
                if (isBlurSupported) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { ItemShape },
                        effects = {
                            vibrancy()
                            blur(radius = 32f)
                            lens(
                                refractionHeight = 5.dp.toPx(),
                                refractionAmount = 6.dp.toPx(),
                            )
                        },
                        highlight = {
                            Highlight.Plain.copy(
                                alpha = 0.88f,
                                width = 0.8.dp,
                            )
                        },
                        onDrawSurface = {
                            drawRect(color = DripColors.PureWhite.copy(alpha = 0.62f))
                        },
                    )
                } else {
                    Modifier.background(DripColors.PureWhite.copy(alpha = 0.62f), ItemShape)
                }
            )
            .border(
                width = 0.5.dp,
                color = DripColors.PureWhite.copy(alpha = 0.35f),
                shape = ItemShape,
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(DripColors.PureWhite.copy(alpha = 0.8f), Color.Transparent)
                ),
                shape = ItemShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ItemInnerShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassPalette.TodayItemInnerBgStart,
                            GlassPalette.TodayItemInnerBgMid,
                            GlassPalette.TodayItemInnerBgEnd,
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = DripColors.PureWhite.copy(alpha = 0.58f),
                    shape = ItemInnerShape,
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GlassPalette.TextTodayItemTitle,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.05.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .background(DripColors.PureWhite.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .border(0.5.dp, DripColors.PureWhite, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.badgeText,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlassPalette.TextTodayItemBadge
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = item.meta,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GlassPalette.TextTodayItemTitle.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.1.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayAtmosphereLayer(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    GlassBackdropAtmosphereLayer(
        backdrop = backdrop,
        modifier = modifier,
    )
}

@Immutable
private data class TodayVisualItem(
    val id: String,
    val title: String,
    val badgeText: String,
    val meta: String
)

@Immutable
private data class TodayVisualSection(
    val id: String,
    val label: String,
    val items: List<TodayVisualItem>,
)

private fun List<TodaySectionUi>.toTodayVisualSections(): List<TodayVisualSection> = map { section ->
    TodayVisualSection(
        id = section.id,
        label = section.label,
        items = section.items.toTodayVisualItems(),
    )
}

private fun List<TodayItemUi>.toTodayVisualItems(): List<TodayVisualItem> = map { item ->
    val badgeText = when {
        item.meta.contains("低压力回看") -> "Low input"
        item.meta.contains("设计") -> "Design"
        item.meta.contains("灵感") -> "Inspiration"
        item.meta.contains("阅读") -> "Reading"
        else -> "Content"
    }
    TodayVisualItem(
        id = item.id,
        title = item.title,
        badgeText = badgeText,
        meta = item.meta
    )
}
