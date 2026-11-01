package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DtfEngine
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioText
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomableCanvas(
    bitmap: Bitmap?,
    bgMode: String,
    isPickingColor: Boolean,
    onPixelTapped: (Float, Float) -> Unit,
    onCancelPickColor: () -> Unit,
    onOpenImage: () -> Unit,
    fitTrigger: Int,
    modifier: Modifier = Modifier
) {
    if (bitmap == null) {
        // High polish Upload Image Empty State
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(StudioBg)
                .padding(24.dp)
                .testTag("canvas_empty"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = StudioCard,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(StudioAccentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload",
                            tint = StudioAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "ছবি আপলোড করুন",
                        color = StudioText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "DTF প্রিন্টের ব্যাকগ্রাউন্ড রিমুভ ও হাফটোন ইফেক্ট দিতে যেকোনো ছবি নির্বাচন করুন",
                        color = StudioMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    StudioButton(
                        text = "ছবি আপলোড (Upload Image)",
                        onClick = onOpenImage,
                        isPrimary = true,
                        icon = Icons.Default.AddPhotoAlternate,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )
                }
            }
        }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(StudioBg)) {
        val containerW = maxWidth.value
        val containerH = maxHeight.value

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        // Fit logic completely local to canvas - never loops or triggers outer recompositions
        fun fitImage() {
            if (bitmap.width <= 0 || bitmap.height <= 0 || containerW <= 0 || containerH <= 0) return
            val sX = (containerW * 0.95f) / bitmap.width
            val sY = (containerH * 0.95f) / bitmap.height
            val newScale = min(sX, sY).coerceIn(0.05f, 20f)
            scale = newScale
            offsetX = (containerW - bitmap.width * newScale) / 2f
            offsetY = (containerH - bitmap.height * newScale) / 2f
        }

        // Only fit when bitmap changes or user explicitly presses fit
        LaunchedEffect(bitmap, fitTrigger) {
            fitImage()
        }

        // Cache composited bitmap
        val composited = remember(bitmap, bgMode) {
            DtfEngine.composite(bitmap, bgMode)
        }
        val imageBitmap = remember(composited) {
            composited.asImageBitmap()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("interactive_canvas")
                    .pointerInput(bitmap, isPickingColor) {
                        detectTapGestures(
                            onDoubleTap = {
                                fitImage()
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

                // Draw center crosshair if in eyedropper mode
                if (isPickingColor) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val chLen = 24.dp.toPx()
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

            // Eyedropper Banner Overlay
            if (isPickingColor) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter),
                    shape = RoundedCornerShape(8.dp),
                    color = StudioAccent,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "কালার পিক করতে ছবিতে ট্যাপ করুন",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onCancelPickColor() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "বাতিল",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Floating Zoom Badge & Fit Button (isolated in bottom-right corner)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = StudioCard.copy(alpha = 0.92f),
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = StudioText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(StudioAccentLight)
                            .clickable { fitImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitScreen,
                            contentDescription = "Fit to screen",
                            tint = StudioAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
