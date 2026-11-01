package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardAlt
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioText
import com.example.ui.theme.StudioWarning
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

    val wInches = parseInch(formW)
    val hInches = parseInch(formH)
    val topInches = parseInch(formTop)
    val sideInches = parseInch(formSide)
    val bodyInches = PrintConstants.SHIRT_BODY_IN[shirtSize] ?: 20f

    val warnings = mutableListOf<String>()
    if (formPlace == "Center" && wInches > bodyInches * 0.98f) {
        warnings.add("শার্ট বডির চেয়ে চওড়া")
    }
    val paperSpec = PrintConstants.PAPERS[paper]
    if (paperSpec?.widthIn != null && paperSpec.heightIn != null) {
        val fits = (wInches <= paperSpec.widthIn && hInches <= paperSpec.heightIn) ||
                (wInches <= paperSpec.heightIn && hInches <= paperSpec.widthIn)
        if (!fits) {
            warnings.add("${paper.substringBefore(" (")} সাইজে আটবে না")
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        val isTablet = maxWidth >= 768.dp

        if (isTablet) {
            // Wide Screen Side-by-Side
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .background(StudioPanel)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    PrintSettingsPanel(
                        unit = unit,
                        sizes = sizes,
                        selectedIndex = selectedIndex,
                        formName = formName,
                        onNameChange = { formName = it },
                        formW = formW,
                        onWChange = { formW = it },
                        formH = formH,
                        onHChange = { formH = it },
                        formTop = formTop,
                        onTopChange = { formTop = it },
                        formSide = formSide,
                        onSideChange = { formSide = it },
                        formView = formView,
                        onViewChange = { formView = it },
                        formPlace = formPlace,
                        onPlaceChange = { formPlace = it },
                        shirtKind = shirtKind,
                        onShirtKindChange = onSetShirtKind,
                        shirtSize = shirtSize,
                        onShirtSizeChange = onSetShirtSize,
                        collarLine = collarLine,
                        onCollarLineChange = onSetCollarLine,
                        paper = paper,
                        onPaperChange = onSetPaper,
                        dpi = dpi,
                        onDpiChange = onSetDpi,
                        mirror = mirror,
                        onMirrorChange = onSetMirror,
                        onSelectUnit = onSelectUnit,
                        onSelectSize = onSelectSize,
                        onSaveSize = {
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
                        onDeleteSize = { onDeleteSize(selectedIndex) },
                        onResetSizes = onResetSizes
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    ) {
                        ShirtMockupCanvas(
                            shirtKind = shirtKind,
                            formView = formView,
                            bodyInches = bodyInches,
                            collarLine = collarLine,
                            topInches = topInches,
                            wInches = wInches,
                            hInches = hInches,
                            sideInches = sideInches,
                            formPlace = formPlace,
                            designBitmap = designBitmap
                        )
                    }

                    PrintActionBar(
                        currentSpec = currentSpec,
                        formW = formW,
                        formH = formH,
                        unit = unit,
                        shirtSize = shirtSize,
                        bodyInches = bodyInches,
                        warnings = warnings,
                        statusText = statusText,
                        onSavePrintFile = onSavePrintFile,
                        onClose = onClose
                    )
                }
            }
        } else {
            // Mobile Portrait Stacked Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top Mockup Canvas (260.dp height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                ) {
                    ShirtMockupCanvas(
                        shirtKind = shirtKind,
                        formView = formView,
                        bodyInches = bodyInches,
                        collarLine = collarLine,
                        topInches = topInches,
                        wInches = wInches,
                        hInches = hInches,
                        sideInches = sideInches,
                        formPlace = formPlace,
                        designBitmap = designBitmap
                    )
                }

                // Controls in scrollable container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    PrintSettingsPanel(
                        unit = unit,
                        sizes = sizes,
                        selectedIndex = selectedIndex,
                        formName = formName,
                        onNameChange = { formName = it },
                        formW = formW,
                        onWChange = { formW = it },
                        formH = formH,
                        onHChange = { formH = it },
                        formTop = formTop,
                        onTopChange = { formTop = it },
                        formSide = formSide,
                        onSideChange = { formSide = it },
                        formView = formView,
                        onViewChange = { formView = it },
                        formPlace = formPlace,
                        onPlaceChange = { formPlace = it },
                        shirtKind = shirtKind,
                        onShirtKindChange = onSetShirtKind,
                        shirtSize = shirtSize,
                        onShirtSizeChange = onSetShirtSize,
                        collarLine = collarLine,
                        onCollarLineChange = onSetCollarLine,
                        paper = paper,
                        onPaperChange = onSetPaper,
                        dpi = dpi,
                        onDpiChange = onSetDpi,
                        mirror = mirror,
                        onMirrorChange = onSetMirror,
                        onSelectUnit = onSelectUnit,
                        onSelectSize = onSelectSize,
                        onSaveSize = {
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
                        onDeleteSize = { onDeleteSize(selectedIndex) },
                        onResetSizes = onResetSizes
                    )
                }

                PrintActionBar(
                    currentSpec = currentSpec,
                    formW = formW,
                    formH = formH,
                    unit = unit,
                    shirtSize = shirtSize,
                    bodyInches = bodyInches,
                    warnings = warnings,
                    statusText = statusText,
                    onSavePrintFile = onSavePrintFile,
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
private fun PrintSettingsPanel(
    unit: String,
    sizes: List<PrintSize>,
    selectedIndex: Int,
    formName: String,
    onNameChange: (String) -> Unit,
    formW: String,
    onWChange: (String) -> Unit,
    formH: String,
    onHChange: (String) -> Unit,
    formTop: String,
    onTopChange: (String) -> Unit,
    formSide: String,
    onSideChange: (String) -> Unit,
    formView: String,
    onViewChange: (String) -> Unit,
    formPlace: String,
    onPlaceChange: (String) -> Unit,
    shirtKind: String,
    onShirtKindChange: (String) -> Unit,
    shirtSize: String,
    onShirtSizeChange: (String) -> Unit,
    collarLine: Float,
    onCollarLineChange: (Float) -> Unit,
    paper: String,
    onPaperChange: (String) -> Unit,
    dpi: String,
    onDpiChange: (String) -> Unit,
    mirror: Boolean,
    onMirrorChange: (Boolean) -> Unit,
    onSelectUnit: (String) -> Unit,
    onSelectSize: (Int) -> Unit,
    onSaveSize: () -> Unit,
    onDeleteSize: () -> Unit,
    onResetSizes: () -> Unit
) {
    Text(
        text = "টি-শার্ট প্রিন্ট ও সাইজিং (Print & Mockup)",
        color = StudioAccent,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )

    StudioSectionHeader("পরিমাপ ইউনিট (Unit)")
    StudioSegmentedControl(
        items = listOf("in", "cm"),
        selected = unit,
        onSelect = onSelectUnit
    )

    StudioSectionHeader("প্রিন্ট সাইজ তালিকা (Sizes)")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        sizes.take(5).forEachIndexed { index, s ->
            val isSelected = index == selectedIndex
            val displayW = if (unit == "cm") s.w * 2.54f else s.w
            val displayH = if (unit == "cm") s.h * 2.54f else s.h
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) StudioAccent else StudioCard)
                    .border(1.dp, if (isSelected) StudioAccent else StudioBorder, RoundedCornerShape(6.dp))
                    .clickable { onSelectSize(index) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = s.name,
                    color = if (isSelected) Color.White else StudioText,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${PrintConstants.fmtNum(displayW)} × ${PrintConstants.fmtNum(displayH)} $unit",
                    color = if (isSelected) Color.White else StudioMuted,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Size Form
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(StudioCard, RoundedCornerShape(10.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PrintFormField("সাইজের নাম", formName, onNameChange)
        PrintFormField("প্রস্থ (Width)", formW, onWChange)
        PrintFormField("উচ্চতা (Height)", formH, onHChange)
        PrintFormField("কলার থেকে দূরত্ব", formTop, onTopChange)
        PrintFormField("বুক থেকে সাইড দূরত্ব", formSide, onSideChange)

        StudioSegmentedControl(
            items = listOf("Front", "Back"),
            selected = formView,
            onSelect = onViewChange
        )
        StudioDropdown(
            label = "প্লেসমেন্ট",
            options = PrintConstants.PLACEMENTS,
            selected = formPlace,
            onSelect = onPlaceChange
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StudioButton(
                text = "সেভ সাইজ",
                isPrimary = true,
                onClick = onSaveSize,
                modifier = Modifier.weight(1f)
            )
            StudioButton(
                text = "মুছুন",
                isDanger = true,
                onClick = onDeleteSize,
                modifier = Modifier.weight(1f)
            )
            StudioButton(
                text = "রিসেট",
                onClick = onResetSizes,
                modifier = Modifier.weight(1f)
            )
        }
    }

    StudioSectionHeader("টি-শার্ট প্রিভিউ অপশনস")
    StudioSegmentedControl(
        items = listOf("White", "Black", "Outline"),
        selected = shirtKind,
        onSelect = onShirtKindChange
    )
    StudioDropdown(
        label = "শার্ট সাইজ",
        options = PrintConstants.SHIRT_BODY_IN.keys.toList(),
        selected = shirtSize,
        onSelect = onShirtSizeChange
    )
    StudioSlider(
        label = "কলার পজিশন (Collar line)",
        value = collarLine,
        onValueChange = onCollarLineChange,
        valueRange = 5f..25f,
        valueText = "%.1f%%".format(collarLine)
    )

    StudioSectionHeader("পেপার ও প্রিন্টার")
    StudioDropdown(
        label = "কাগজের মাপ (Paper)",
        options = PrintConstants.PAPERS.keys.toList(),
        selected = paper,
        onSelect = onPaperChange
    )
    StudioDropdown(
        label = "প্রিন্ট DPI",
        options = listOf("150", "300", "600"),
        selected = dpi,
        onSelect = onDpiChange
    )
    StudioSwitch(
        text = "মিরর করুন (Mirror for transfer)",
        checked = mirror,
        onCheckedChange = onMirrorChange
    )
}

@Composable
private fun ShirtMockupCanvas(
    shirtKind: String,
    formView: String,
    bodyInches: Float,
    collarLine: Float,
    topInches: Float,
    wInches: Float,
    hInches: Float,
    sideInches: Float,
    formPlace: String,
    designBitmap: Bitmap?
) {
    val isBack = formView == "Back"
    val shirtBitmap = remember(shirtKind, isBack) {
        ShirtRenderer.makeShirt(shirtKind, isBack, 1040, 1081)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECE9))
    ) {
        val canvasW = size.width
        val canvasH = size.height

        val W_in = bodyInches / 0.72f
        val H_in = W_in * 1081f / 1040f
        val s = min((canvasW * 0.88f) / W_in, (canvasH * 0.88f) / H_in)

        val sw = (W_in * s).roundToInt()
        val sh = (H_in * s).roundToInt()
        val sx = (canvasW - sw) / 2f
        val sy = (canvasH - sh) / 2f

        drawImage(
            image = shirtBitmap.asImageBitmap(),
            dstOffset = IntOffset(sx.roundToInt(), sy.roundToInt()),
            dstSize = IntSize(sw, sh)
        )

        val midX = sx + sw / 2f
        drawLine(
            color = Color(0xFF8A9A90),
            start = Offset(midX, sy),
            end = Offset(midX, sy + sh),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

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

        drawRect(
            color = StudioAccent,
            topLeft = Offset(bLeft, bTop),
            size = Size(boxW, boxH),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )

        drawLine(
            color = StudioAccent,
            start = Offset(midX, collarY),
            end = Offset(midX, designTopY),
            strokeWidth = 2f
        )
        drawLine(
            color = StudioAccent,
            start = Offset(midX - 8f, collarY),
            end = Offset(midX + 8f, collarY),
            strokeWidth = 2f
        )
        drawLine(
            color = StudioAccent,
            start = Offset(midX - 8f, designTopY),
            end = Offset(midX + 8f, designTopY),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun PrintActionBar(
    currentSpec: PrintSize,
    formW: String,
    formH: String,
    unit: String,
    shirtSize: String,
    bodyInches: Float,
    warnings: List<String>,
    statusText: String,
    onSavePrintFile: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val infoText = "${currentSpec.name} · ${formW} × ${formH} $unit · শার্ট: $shirtSize ($bodyInches\")"
        Text(
            text = infoText + if (warnings.isNotEmpty()) "\nসতর্কতা: " + warnings.joinToString(", ") else "\nশার্ট ও পেপারে পারফেক্ট মাপ",
            color = if (warnings.isNotEmpty()) StudioWarning else StudioMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                color = StudioAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioButton(
                text = "প্রিন্ট ফাইল সেভ করুন (Save Print File)",
                isPrimary = true,
                onClick = onSavePrintFile,
                modifier = Modifier.weight(1f)
            )
            StudioButton(
                text = "বন্ধ করুন",
                onClick = onClose,
                modifier = Modifier.width(90.dp)
            )
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
        Text(text = label, color = StudioText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = StudioText, fontSize = 12.sp, fontWeight = FontWeight.Bold),
            cursorBrush = SolidColor(StudioAccent),
            modifier = Modifier
                .width(100.dp)
                .height(30.dp)
                .background(StudioCardAlt, RoundedCornerShape(6.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
