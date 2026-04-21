package com.example.dripin4.ui.features.today

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.getNoiseBitmap
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraTransition")

    // Fix drift periods to be within 28-36 seconds
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuroraPhase"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuroraPhase2"
    )

    val noiseBitmap = remember { getNoiseBitmap() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val baseDriftAmp = 56.dp.toPx()
        val maxRadius = width * 0.65f

        val driftX1 = cos(phase) * baseDriftAmp * 1.1f
        val driftY1 = sin(phase) * baseDriftAmp * 0.9f

        val driftX2 = sin(phase2) * baseDriftAmp * 0.95f
        val driftY2 = cos(phase2) * baseDriftAmp * 1.05f

        val driftX3 = cos(phase + Math.PI / 2) * baseDriftAmp * 0.8f
        val driftY3 = sin(phase + Math.PI / 2) * baseDriftAmp * 1.1f

        val driftX4 = sin(phase2 + Math.PI) * baseDriftAmp * 1.0f
        val driftY4 = cos(phase2 + Math.PI) * baseDriftAmp * 0.85f

        // Four-corner balanced aurora field.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlassPalette.AuroraBlue.copy(alpha = 0.34f),
                    GlassPalette.AuroraBlue.copy(alpha = 0.16f),
                    Color.Transparent
                ),
                center = Offset(width * 0.22f + driftX1.toFloat(), height * 0.20f + driftY1.toFloat()),
                radius = minOf(width * 0.58f, maxRadius)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlassPalette.AuroraPurple.copy(alpha = 0.33f),
                    GlassPalette.AuroraPurple.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(width * 0.78f + driftX2.toFloat(), height * 0.22f + driftY2.toFloat()),
                radius = minOf(width * 0.56f, maxRadius)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlassPalette.AuroraPeach.copy(alpha = 0.36f),
                    GlassPalette.AuroraPeach.copy(alpha = 0.17f),
                    Color.Transparent
                ),
                center = Offset(width * 0.25f + driftX3.toFloat(), height * 0.76f + driftY3.toFloat()),
                radius = minOf(width * 0.60f, maxRadius)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlassPalette.AuroraMint.copy(alpha = 0.32f),
                    GlassPalette.AuroraMint.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(width * 0.76f + driftX4.toFloat(), height * 0.78f + driftY4.toFloat()),
                radius = minOf(width * 0.57f, maxRadius)
            )
        )

        val noiseW = noiseBitmap.width
        val noiseH = noiseBitmap.height
        val tileXCount = (size.width / noiseW).toInt() + 1
        val tileYCount = (size.height / noiseH).toInt() + 1
        
        for (x in 0 until tileXCount) {
            for (y in 0 until tileYCount) {
                drawImage(
                    image = noiseBitmap,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(noiseW, noiseH),
                    dstOffset = IntOffset(x * noiseW, y * noiseH),
                    dstSize = IntSize(noiseW, noiseH),
                    alpha = 0.03f,
                    blendMode = BlendMode.Overlay
                )
            }
        }
    }
}
