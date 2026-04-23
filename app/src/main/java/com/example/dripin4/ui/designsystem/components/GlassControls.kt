package com.example.dripin4.ui.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripMotion
import com.example.dripin4.ui.designsystem.DripRadius
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette

enum class GlassButtonStyle {
    Primary, Secondary
}

fun Modifier.dockSelectedTint(
    selected: Boolean,
    cornerRadius: Dp = 18.dp,
    bloomSpread: Dp = 10.dp,
    bloomAlpha: Float = 0.18f,
): Modifier {
    val tintShape = RoundedCornerShape(cornerRadius)
    return this
        .glassBloom(
            color = GlassPalette.HeroBlobMint,
            spread = bloomSpread,
            alpha = if (selected) bloomAlpha else 0f,
            shape = tintShape,
        )
        .clip(tintShape)
        .drawBehind {
            if (!selected) return@drawBehind

            val radius = minOf(cornerRadius.toPx(), size.height / 2f)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        GlassPalette.DockSelectedTintStart,
                        GlassPalette.DockSelectedTintEnd,
                    ),
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )
            drawRoundRect(
                color = DripColors.PureWhite.copy(alpha = 0.18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )

            val topHighlightStroke = 1.dp.toPx()
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        DripColors.PureWhite.copy(alpha = 0.8f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = 18.dp.toPx(),
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    topHighlightStroke / 2,
                    topHighlightStroke / 2,
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.width - topHighlightStroke,
                    size.height - topHighlightStroke,
                ),
                style = Stroke(width = topHighlightStroke),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    radius - topHighlightStroke / 2,
                ),
            )

            val hairlineStroke = 0.5.dp.toPx()
            drawRoundRect(
                color = DripColors.PureWhite.copy(alpha = 0.35f),
                topLeft = androidx.compose.ui.geometry.Offset(
                    hairlineStroke / 2,
                    hairlineStroke / 2,
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.width - hairlineStroke,
                    size.height - hairlineStroke,
                ),
                style = Stroke(width = hairlineStroke),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    radius - hairlineStroke / 2,
                ),
            )
        }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isPrimaryStyle = style == GlassButtonStyle.Primary
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) DripMotion.PressedScale else 1f,
        animationSpec = tween(durationMillis = DripMotion.StandardMs, easing = FastOutSlowInEasing),
        label = "button_press_scale"
    )

    val backgroundBrush = when (style) {
        GlassButtonStyle.Primary -> null

        GlassButtonStyle.Secondary -> Brush.verticalGradient(
            listOf(
                DripColors.PureWhite.copy(alpha = 0.64f),
                DripColors.PureWhite.copy(alpha = 0.52f)
            )
        )
    }

    val shape = RoundedCornerShape(20.dp)
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = shape,
            fillAlpha = if (isPrimaryStyle) 0.52f else 0.50f,
            blurRadius = 18.dp,
            withTopHighlight = !isPrimaryStyle,
            withHairline = !isPrimaryStyle,
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.72f), shape)
    }

    val buttonModifier = modifier
        .height(54.dp)
        .graphicsLayer {
            scaleX = pressScale
            scaleY = pressScale
        }
        .clip(shape)
        .then(surfaceModifier)
        .then(
            if (backgroundBrush != null) {
                Modifier.background(backgroundBrush, shape)
            } else {
                Modifier
            }
        )
        .dockSelectedTint(selected = isPrimaryStyle, cornerRadius = 20.dp)
        .border(
            1.dp,
            if (isPrimaryStyle) Color.Transparent else DripColors.PureWhite.copy(alpha = 0.75f),
            shape
        )
        .border(
            0.5.dp,
            if (isPrimaryStyle) Color.Transparent else DripColors.HairlineDark,
            shape
        )
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
        .padding(horizontal = 16.dp)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DripColors.Ink,
                    modifier = Modifier.size(18.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) {
                    if (isPrimaryStyle) DripColors.Ink else DripColors.Ink
                } else {
                    DripColors.SoftGray
                }
            )
        }
    }
}

@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) DripMotion.PressedScale else 1f,
        animationSpec = tween(durationMillis = DripMotion.StandardMs, easing = FastOutSlowInEasing),
        label = "glass_switch_press_scale",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 31.dp else 3.dp,
        animationSpec = tween(durationMillis = DripMotion.StandardMs, easing = FastOutSlowInEasing),
        label = "glass_switch_thumb_offset",
    )

    val trackShape = RoundedCornerShape(999.dp)
    val trackModifier = if (checked) {
        Modifier.background(DripColors.Ink, trackShape)
    } else if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = trackShape,
            fillAlpha = 0.56f,
            blurRadius = 18.dp,
            withTopHighlight = true,
            withHairline = true,
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.68f), trackShape)
    }

    Box(
        modifier = modifier
            .size(width = 58.dp, height = 34.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(trackShape)
            .then(trackModifier)
            .then(
                if (checked) {
                    Modifier
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                DripColors.PureWhite.copy(alpha = 0.32f),
                                DripColors.PureWhite.copy(alpha = 0.14f),
                            ),
                        ),
                        trackShape,
                    )
                }
            )
            .border(
                1.dp,
                if (checked) DripColors.Ink else DripColors.PureWhite.copy(alpha = 0.88f),
                trackShape,
            )
            .border(
                0.5.dp,
                if (checked) DripColors.Ink.copy(alpha = 0.8f) else DripColors.Ink.copy(alpha = 0.08f),
                trackShape,
            )
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange,
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clip(CircleShape)
                .background(DripColors.PureWhite.copy(alpha = 0.96f), CircleShape)
                .border(0.5.dp, DripColors.PureWhite.copy(alpha = 0.92f), CircleShape),
        )
    }
}

@Composable
fun GlassChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = DripSpacing.XSmall),
        horizontalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
        content = content,
    )
}

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) DripMotion.PressedScale else 1f,
        animationSpec = tween(durationMillis = DripMotion.QuickMs, easing = FastOutSlowInEasing),
        label = "chip_press_scale"
    )
    val shape = RoundedCornerShape(DripRadius.PillDp)
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = shape,
            fillAlpha = if (selected) 0.52f else 0.44f,
            blurRadius = 14.dp,
            withTopHighlight = !selected,
            withHairline = !selected,
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.62f), shape)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(surfaceModifier)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                DripColors.PureWhite.copy(alpha = 0.62f),
                                DripColors.PureWhite.copy(alpha = 0.5f)
                            )
                        ),
                        shape
                    )
                }
            )
            .dockSelectedTint(
                selected = selected,
                bloomSpread = 0.dp,
                bloomAlpha = 0f,
            )
            .border(
                1.dp,
                if (selected) Color.Transparent else DripColors.PureWhite.copy(alpha = 0.8f),
                shape
            )
            .border(
                0.5.dp,
                if (selected) Color.Transparent else DripColors.HairlineSoft,
                shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) GlassPalette.AccentMint else DripColors.Graphite
        )
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) DripMotion.PressedScale else 1f,
        animationSpec = tween(durationMillis = DripMotion.QuickMs, easing = FastOutSlowInEasing),
        label = "icon_press_scale"
    )
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = CircleShape,
            fillAlpha = 0.48f,
            blurRadius = 16.dp,
            withTopHighlight = true,
            withHairline = true,
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
            .clip(CircleShape)
            .then(surfaceModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
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
fun GlassField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = false
) {
    val backdrop = LocalGlassBackdrop.current
    val shape = RoundedCornerShape(DripRadius.SecondaryCardDp)
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = shape,
            fillAlpha = 0.46f,
            blurRadius = 18.dp,
            withTopHighlight = true,
            withHairline = true,
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.58f), shape)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (singleLine) 72.dp else 110.dp)
            .clip(shape)
            .then(surfaceModifier)
            .background(DripColors.PureWhite.copy(alpha = 0.18f), shape)
            .border(1.dp, DripColors.PureWhite.copy(alpha = 0.85f), shape)
            .border(0.5.dp, DripColors.HairlineSoft, shape)
            .padding(horizontal = DripSpacing.Small, vertical = DripSpacing.Small)
    ) {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = DripColors.SoftGray
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = DripColors.Ink,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                ),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DripColors.MistGray
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
