package com.example.dripin4.ui.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripMotion
import com.example.dripin4.ui.designsystem.GlassPalette
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

data class DripBottomNavItem<T>(
    val destination: T,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun DripTopBar(
    brand: String,
    subtitle: String,
    onSearch: () -> Unit,
    onBell: () -> Unit,
    todayGlassMode: Boolean = false,
    todayBackdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val topBarShape = RectangleShape
    val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarBackgroundHeight = topInsetPadding + 60.dp
    val topBarSurfaceModifier = when {
        todayGlassMode && todayBackdrop != null -> {
            Modifier
                .drawBackdrop(
                    backdrop = todayBackdrop,
                    shape = { topBarShape },
                    effects = {
                        blur(radius = 14.dp.toPx())
                        vibrancy()
                    },
                    highlight = {
                        Highlight.Plain.copy(alpha = 0f, width = 0.dp)
                    },
                    onDrawSurface = {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DripColors.PureWhite.copy(alpha = 0.035f),
                                    DripColors.PureWhite.copy(alpha = 0.012f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
                )
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.White,
                                0.35f to Color.White.copy(alpha = 0.88f),
                                0.70f to Color.White.copy(alpha = 0.58f),
                                0.88f to Color.White.copy(alpha = 0.22f),
                                0.97f to Color.White.copy(alpha = 0.04f),
                                1.00f to Color.Transparent,
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        }
        todayGlassMode -> Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    DripColors.PureWhite.copy(alpha = 0.32f),
                    DripColors.PureWhite.copy(alpha = 0.10f),
                    Color.Transparent
                )
            )
        )
        else -> Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_bar")
    ) {
        if (todayGlassMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarBackgroundHeight)
                    .then(topBarSurfaceModifier)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = topInsetPadding + 3.dp,
                    bottom = 6.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = brand,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    letterSpacing = (-0.25).sp,
                ),
                color = if (todayGlassMode) DripColors.Ink else GlassPalette.TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DripTopBarIconButton(
                    icon = Icons.Outlined.Search,
                    contentDescription = "搜索",
                    onClick = onSearch,
                    todayGlassMode = todayGlassMode,
                    todayBackdrop = todayBackdrop
                )
                DripTopBarIconButton(
                    icon = Icons.Outlined.NotificationsNone,
                    contentDescription = "通知",
                    onClick = onBell,
                    todayGlassMode = todayGlassMode,
                    todayBackdrop = todayBackdrop
                )
            }
        }
    }
}

@Composable
private fun DripTopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    todayGlassMode: Boolean,
    todayBackdrop: Backdrop?,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) DripMotion.PressedScale else 1f,
        animationSpec = tween(durationMillis = DripMotion.QuickMs, easing = FastOutSlowInEasing),
        label = "icon_press_scale"
    )

    val surfaceModifier = if (todayGlassMode && todayBackdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = todayBackdrop,
            shape = CircleShape,
            fillAlpha = 0.58f,
            blurRadius = 6.dp,
            withHairline = false
        )
    } else {
        Modifier
            .background(DripColors.PureWhite.copy(alpha = 0.68f))
            .border(1.dp, DripColors.PureWhite.copy(alpha = 0.85f), CircleShape)
            .border(0.5.dp, DripColors.HairlineSoft, CircleShape)
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = GlassPalette.DockShadowAmbient,
                spotColor = GlassPalette.DockShadowAmbient
            )
            .clip(CircleShape)
            .then(surfaceModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = DripColors.Ink,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun <T> DripBottomNav(
    items: List<DripBottomNavItem<T>>,
    currentDestination: T,
    onDestinationSelected: (T) -> Unit,
    todayGlassMode: Boolean = false,
    todayBackdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val navShape = RoundedCornerShape(32.dp)

    val surfaceModifier = if (todayBackdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = todayBackdrop,
            shape = navShape,
            fillAlpha = 0.50f,
            blurRadius = 28.dp,
            withTopHighlight = true,
            withHairline = true
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.86f), navShape)
            .border(1.dp, DripColors.FrostWhiteStroke, navShape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(68.dp)
                .widthIn(max = 460.dp)
                .testTag("bottom_nav")
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paintAmbient = Paint().asFrameworkPaint().apply {
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                44.dp.toPx(),
                                0f,
                                20.dp.toPx(),
                                GlassPalette.AccentMint.copy(alpha = 0.14f).toArgb()
                            )
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height, 32.dp.toPx(), 32.dp.toPx(), paintAmbient
                        )
                        val paintNear = Paint().asFrameworkPaint().apply {
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                18.dp.toPx(),
                                0f,
                                6.dp.toPx(),
                                GlassPalette.ShadowNear.toArgb()
                            )
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height, 32.dp.toPx(), 32.dp.toPx(), paintNear
                        )
                    }
                }
                .then(surfaceModifier)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                DripBottomNavEntry(
                    icon = item.icon,
                    label = item.label,
                    selected = item.destination == currentDestination,
                    onClick = { onDestinationSelected(item.destination) },
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}

@Composable
private fun DripBottomNavEntry(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pillShape = RoundedCornerShape(18.dp)

    val bloomAlpha by animateFloatAsState(
        targetValue = if (selected) 0.18f else 0f,
        animationSpec = tween(DripMotion.StandardMs),
        label = "nav_bloom"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else if (isPressed) 0.85f else 0.55f,
        animationSpec = tween(120),
        label = "icon_alpha"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) GlassPalette.AccentMint else GlassPalette.TextSecondary,
        animationSpec = tween(DripMotion.StandardMs),
        label = "nav_color"
    )

    var pulseScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            androidx.compose.animation.core.animate(
                initialValue = 0.999f,
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 140
                    1f at 0
                    0.92f at 70
                    1f at 140
                }
            ) { value, _ -> pulseScale = value }
        } else {
            pulseScale = 1f
        }
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = label }
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .selectable(
                selected = selected,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f))
                .height(36.dp)
                .dockSelectedTint(selected = selected, cornerRadius = 18.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val finalIconColor = contentColor.copy(alpha = iconAlpha)
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .size(if (selected) 20.dp else 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = finalIconColor,
                    modifier = Modifier
                        .size(if (selected) 20.dp else 22.dp)
                        .drawBehind {
                            if (selected) {
                                drawIntoCanvas { canvas ->
                                    val paint = Paint().asFrameworkPaint().apply {
                                        color = android.graphics.Color.TRANSPARENT
                                        setShadowLayer(
                                            1.dp.toPx(),
                                            0f,
                                            0f,
                                            DripColors.PureBlack.copy(alpha = 0.06f).toArgb()
                                        )
                                    }
                                    canvas.nativeCanvas.drawCircle(
                                        center.x, center.y, (size.width / 2) * 0.8f, paint
                                    )
                                }
                            }
                        }
                )
            }

            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn(animationSpec = tween(120)) + slideInHorizontally(
                        animationSpec = tween(120),
                        initialOffsetX = { fullWidth -> fullWidth / 4 }
                    ) togetherWith fadeOut(animationSpec = tween(120)) + slideOutHorizontally(
                        animationSpec = tween(120),
                        targetOffsetX = { fullWidth -> -fullWidth / 4 }
                    )
                },
                label = "nav_label_anim"
            ) { isSelected ->
                if (isSelected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = finalIconColor,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
