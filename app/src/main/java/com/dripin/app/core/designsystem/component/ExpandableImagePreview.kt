package com.dripin.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage

@Composable
fun ExpandableImagePreview(
    imageUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
) {
    var isExpanded by rememberSaveable(imageUri) { mutableStateOf(false) }
    var zoomScale by rememberSaveable(imageUri) { mutableFloatStateOf(1f) }
    var panX by rememberSaveable(imageUri) { mutableFloatStateOf(0f) }
    var panY by rememberSaveable(imageUri) { mutableFloatStateOf(0f) }
    val dismissExpanded = {
        isExpanded = false
        zoomScale = 1f
        panX = 0f
        panY = 0f
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { isExpanded = true },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 6.dp,
    ) {
                AsyncImage(
            model = imageUri,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentScale = ContentScale.Crop,
        )
    }

    if (isExpanded) {
        Dialog(
            onDismissRequest = dismissExpanded,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                zoomScale = (zoomScale * zoomChange).coerceIn(1f, 5f)
                panX += panChange.x
                panY += panChange.y
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE614171A)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { dismissExpanded() },
                )
                AsyncImage(
                    model = imageUri,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .graphicsLayer {
                            translationX = panX
                            translationY = panY
                            scaleX = zoomScale
                            scaleY = zoomScale
                        }
                        .transformable(state = transformableState),
                    contentScale = ContentScale.Fit,
                )
                IconButton(
                    onClick = { dismissExpanded() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "关闭图片预览",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
