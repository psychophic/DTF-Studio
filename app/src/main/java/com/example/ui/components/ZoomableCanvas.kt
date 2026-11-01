package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DtfEngine
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioMuted
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomableCanvas(
    bitmap: Bitmap?,
    bgMode: String,
    isPickingColor: Boolean,
    onPixelTapped: (Float, Float) -> Unit,
    onZoomChanged: (Float) -> Unit,
    fitTrigger: Int,
    modifier: Modifier = Modifier
) {
    if (bitmap == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(StudioBg)
                .testTag("canvas_empty"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open an image to begin",
                color = StudioMuted,
                fontSize = 14.sp
            )
        }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(StudioBg)) {
        val containerW = maxWidth.value
        val containerH = maxHeight.value

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        // Fit function
        fun fit() {
            if (bitmap.width <= 0 || bitmap.height <= 0) return
            val sX = (containerW * 0.95f) / bitmap.width
            val sY = (containerH * 0.95f) / bitmap.height
            val newScale = min(sX, sY).coerceIn(0.05f, 20f)
            scale = newScale
            offsetX = (containerW - bitmap.width * newScale) / 2f
            offsetY = (containerH - bitmap.height * newScale) / 2f
            onZoomChanged(newScale)
        }

        // Auto-fit on layout or trigger change
        LaunchedEffect(bitmap.width, bitmap.height, containerW, containerH, fitTrigger) {
            fit()
        }

        // Cache composited bitmap for performance
        val composited = remember(bitmap, bgMode) {
            DtfEngine.composite(bitmap, bgMode)
        }
        val imageBitmap = remember(composited) {
            composited.asImageBitmap()
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_canvas")
                .pointerInput(bitmap, isPickingColor) {
                    detectTapGestures(
                        onDoubleTap = {
                            fit()
                        },
                        onTap = { tapOffset ->
                            if (isPickingColor) {
                                val imgW = bitmap.width * scale
                                val imgH = bitmap.height * scale
                                val relX = tapOffset.x - offsetX
                                val relY = tapOffset.y - offsetY
                                if (relX in 0f..imgW && relY in 0f..imgH) {
                                    val normX = relX / imgW
                                    val normY = relY / imgH
                                    onPixelTapped(normX, normY)
                                }
                            }
                        }
                    )
                }
                .pointerInput(bitmap) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = (scale * zoom).coerceIn(0.02f, 20f)
                        val factor = newScale / oldScale
                        offsetX = centroid.x - (centroid.x - offsetX) * factor + pan.x
                        offsetY = centroid.y - (centroid.y - offsetY) * factor + pan.y
                        scale = newScale
                        onZoomChanged(newScale)
                    }
                }
        ) {
            val drawW = (bitmap.width * scale).roundToInt()
            val drawH = (bitmap.height * scale).roundToInt()
            val dstOffset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            val dstSize = IntSize(drawW, drawH)

            drawImage(
                image = imageBitmap,
                dstOffset = dstOffset,
                dstSize = dstSize
            )

            // Draw crosshair if picking
            if (isPickingColor) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val chLen = 20.dp.toPx()
                drawLine(
                    color = Color.Red,
                    start = Offset(center.x - chLen, center.y),
                    end = Offset(center.x + chLen, center.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.Red,
                    start = Offset(center.x, center.y - chLen),
                    end = Offset(center.x, center.y + chLen),
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(
                    color = Color.Red,
                    radius = 8.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
