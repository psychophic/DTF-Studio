package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ShirtRenderer
import com.example.model.PrintConstants
import com.example.model.PrintSize
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioDropdown
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.StudioSegmentedControl
import com.example.ui.components.StudioSlider
import com.example.ui.components.StudioSwitch
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentHover
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioText
import com.example.ui.theme.StudioWarning
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun PrintModalScreen(
    designBitmap: Bitmap?,
    unit: String,
    sizes: List<PrintSize>,
    selectedIndex: Int,
    shirtKind: String,
    shirtSize: String,
    collarLine: Float,
    paper: String,
    dpi: String,
    copies: Int,
    mirror: Boolean,
    statusText: String,
    onSelectUnit: (String) -> Unit,
    onSelectSize: (Int) -> Unit,
    onSaveSize: (PrintSize) -> Unit,
    onDeleteSize: (Int) -> Unit,
    onResetSizes: () -> Unit,
    onSetShirtKind: (String) -> Unit,
    onSetShirtSize: (String) -> Unit,
    onSetCollarLine: (Float) -> Unit,
    onSetPaper: (String) -> Unit,
    onSetDpi: (String) -> Unit,
    onSetCopies: (Int) -> Unit,
    onSetMirror: (Boolean) -> Unit,
    onSavePrintFile: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSpec = sizes.getOrNull(selectedIndex) ?: PrintConstants.defaultSizes()[0]

    var formName by remember(currentSpec) { mutableStateOf(currentSpec.name) }
    var formW by remember(currentSpec, unit) {
        val v = if (unit == "cm") currentSpec.w * 2.54f else currentSpec.w
        mutableStateOf(PrintConstants.fmtNum(v))
    }
    var formH by remember(currentSpec, unit) {
        val v = if (unit == "cm") currentSpec.h * 2.54f else currentSpec.h
        mutableStateOf(PrintConstants.fmtNum(v))
    }
    var formTop by remember(currentSpec, unit) {
        val v = if (unit == "cm") currentSpec.top * 2.54f else currentSpec.top
        mutableStateOf(PrintConstants.fmtNum(v))
    }
    var formSide by remember(currentSpec, unit) {
        val v = if (unit == "cm") currentSpec.side * 2.54f else currentSpec.side
        mutableStateOf(PrintConstants.fmtNum(v))
    }
    var formView by remember(currentSpec) { mutableStateOf(currentSpec.view) }
    var formPlace by remember(currentSpec) { mutableStateOf(currentSpec.place) }

    fun parseInch(s: String): Float {
        val num = s.replace(",", ".").toFloatOrNull() ?: 1.0f
        return if (unit == "cm") num / 2.54f else num
    }

    // Dynamic warning calculations
    val wInches = parseInch(formW)
    val hInches = parseInch(formH)
    val topInches = parseInch(formTop)
    val sideInches = parseInch(formSide)
    val bodyInches = PrintConstants.SHIRT_BODY_IN[shirtSize] ?: 20f

    val warnings = mutableListOf<String>()
    if (formPlace == "Center" && wInches > bodyInches * 0.98f) {
        warnings.add("wider than the shirt body")
    }
    val paperSpec = PrintConstants.PAPERS[paper]
    if (paperSpec?.widthIn != null && paperSpec.heightIn != null) {
        val fits = (wInches <= paperSpec.widthIn && hInches <= paperSpec.heightIn) ||
                (wInches <= paperSpec.heightIn && hInches <= paperSpec.widthIn)
        if (!fits) {
            warnings.add("does not fit on ${paper.substringBefore(" (")}")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: Controls (Width 360.dp)
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .background(StudioPanel)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = "Print",
                    color = StudioText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                StudioSectionHeader("Print size")
                StudioSegmentedControl(
                    items = listOf("in", "cm"),
                    selected = unit,
                    onSelect = onSelectUnit
                )

                // List of sizes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    sizes.take(7).forEachIndexed { index, s ->
                        val isSelected = index == selectedIndex
                        val displayW = if (unit == "cm") s.w * 2.54f else s.w
                        val displayH = if (unit == "cm") s.h * 2.54f else s.h
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) StudioAccent else StudioCard)
                                .clickable { onSelectSize(index) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = s.name,
                                color = if (isSelected) Color.White else StudioText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${PrintConstants.fmtNum(displayW)} × ${PrintConstants.fmtNum(displayH)} $unit",
                                color = if (isSelected) Color.White else StudioMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Size editor form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCard, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrintFormField("Name", formName) { formName = it }
                    PrintFormField("Width", formW) { formW = it }
                    PrintFormField("Height", formH) { formH = it }
                    PrintFormField("Collar/hem offset", formTop) { formTop = it }
                    PrintFormField("Chest side offset", formSide) { formSide = it }

                    StudioSegmentedControl(
                        items = listOf("Front", "Back"),
                        selected = formView,
                        onSelect = { formView = it }
                    )
                    StudioDropdown(
                        label = "Placement",
                        options = PrintConstants.PLACEMENTS,
                        selected = formPlace,
                        onSelect = { formPlace = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StudioButton(
                            text = "Save size",
                            isPrimary = true,
                            onClick = {
                                val s = PrintSize(
                                    name = formName.trim().ifEmpty { "Custom" },
                                    w = parseInch(formW),
                                    h = parseInch(formH),
                                    view = formView,
                                    place = formPlace,
                                    top = parseInch(formTop),
                                    side = parseInch(formSide),
                                    paper = paper
                                )
                                onSaveSize(s)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        StudioButton(
                            text = "Delete",
                            isDanger = true,
                            onClick = { onDeleteSize(selectedIndex) },
                            modifier = Modifier.weight(1f)
                        )
                        StudioButton(
                            text = "Reset",
                            onClick = onResetSizes,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                StudioSectionHeader("T-shirt preview")
                StudioSegmentedControl(
                    items = listOf("White", "Black", "Outline"),
                    selected = shirtKind,
                    onSelect = onSetShirtKind
                )
                StudioDropdown(
                    label = "Shirt size",
                    options = PrintConstants.SHIRT_BODY_IN.keys.toList(),
                    selected = shirtSize,
                    onSelect = onSetShirtSize
                )
                StudioSlider(
                    label = "Collar line",
                    value = collarLine,
                    onValueChange = onSetCollarLine,
                    valueRange = 5f..25f,
                    valueText = "%.1f%%".format(collarLine)
                )

                StudioSectionHeader("Paper & printer")
                StudioDropdown(
                    label = "Paper",
                    options = PrintConstants.PAPERS.keys.toList(),
                    selected = paper,
                    onSelect = onSetPaper
                )
                StudioDropdown(
                    label = "Print DPI",
                    options = listOf("150", "300", "600"),
                    selected = dpi,
                    onSelect = onSetDpi
                )
                StudioSwitch(
                    text = "Mirror for sublimation transfer",
                    checked = mirror,
                    onCheckedChange = onSetMirror
                )
            }

            // Right Column: Mockup Canvas + Action Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(14.dp)
            ) {
                // T-Shirt Mockup Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A30))
                ) {
                    val isBack = formView == "Back"
                    val shirtBitmap = remember(shirtKind, isBack) {
                        ShirtRenderer.makeShirt(shirtKind, isBack, 1040, 1081)
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height

                        // Calculate scale to fit shirt nicely on stage
                        val W_in = bodyInches / 0.72f
                        val H_in = W_in * 1081f / 1040f
                        val s = min((canvasW * 0.85f) / W_in, (canvasH * 0.85f) / H_in)

                        val sw = (W_in * s).roundToInt()
                        val sh = (H_in * s).roundToInt()
                        val sx = (canvasW - sw) / 2f
                        val sy = (canvasH - sh) / 2f

                        // Draw shirt
                        drawImage(
                            image = shirtBitmap.asImageBitmap(),
                            dstOffset = IntOffset(sx.roundToInt(), sy.roundToInt()),
                            dstSize = IntSize(sw, sh)
                        )

                        // Center guideline
                        val midX = sx + sw / 2f
                        drawLine(
                            color = Color(0xFF6B6B78),
                            start = Offset(midX, sy),
                            end = Offset(midX, sy + sh),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Draw collar ruler and design box
                        val collarDepthFactor = if (isBack) 0.34f else 1.0f
                        val collarY = sy + (collarLine / 100f) * collarDepthFactor * sh
                        val designTopY = collarY + topInches * s

                        val boxW = wInches * s
                        val boxH = hInches * s
                        val xOff = when (formPlace) {
                            "Left chest" -> (sideInches + wInches / 2f) * s
                            "Right chest" -> -(sideInches + wInches / 2f) * s
                            else -> 0f
                        }
                        val cx = midX + xOff
                        val bLeft = cx - boxW / 2f
                        val bTop = designTopY

                        // Draw design if present
                        if (designBitmap != null) {
                            val dAspect = designBitmap.width.toFloat() / designBitmap.height
                            val (dW, dH) = if (boxW / boxH > dAspect) {
                                Pair((boxH * dAspect).roundToInt(), boxH.roundToInt())
                            } else {
                                Pair(boxW.roundToInt(), (boxW / dAspect).roundToInt())
                            }
                            val dx = cx - dW / 2f
                            val dy = bTop
                            drawImage(
                                image = designBitmap.asImageBitmap(),
                                dstOffset = IntOffset(dx.roundToInt(), dy.roundToInt()),
                                dstSize = IntSize(dW, dH)
                            )
                        }

                        // Draw bounding box
                        drawRect(
                            color = StudioAccentHover,
                            topLeft = Offset(bLeft, bTop),
                            size = Size(boxW, boxH),
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                        )

                        // Draw collar line ruler to design
                        drawLine(
                            color = StudioAccentHover,
                            start = Offset(midX, collarY),
                            end = Offset(midX, designTopY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = StudioAccentHover,
                            start = Offset(midX - 10f, collarY),
                            end = Offset(midX + 10f, collarY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = StudioAccentHover,
                            start = Offset(midX - 10f, designTopY),
                            end = Offset(midX + 10f, designTopY),
                            strokeWidth = 2f
                        )
                    }
                }

                // Info & Warnings
                val infoText = "${currentSpec.name} · ${formW} × ${formH} $unit · inside shirt $shirtSize ($bodyInches\")"
                Text(
                    text = infoText + if (warnings.isNotEmpty()) "\nWarning: design " + warnings.joinToString(", ") else "\nFits the shirt and paper",
                    color = if (warnings.isNotEmpty()) StudioWarning else StudioMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        color = StudioAccent,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioButton(
                        text = "Save print file…",
                        isPrimary = true,
                        onClick = onSavePrintFile,
                        modifier = Modifier.weight(1f)
                    )
                    StudioButton(
                        text = "Close",
                        onClick = onClose,
                        modifier = Modifier.width(90.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrintFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = StudioText, fontSize = 11.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = StudioText, fontSize = 11.sp),
            cursorBrush = SolidColor(StudioText),
            modifier = Modifier
                .width(100.dp)
                .height(26.dp)
                .background(StudioBg, RoundedCornerShape(4.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}
