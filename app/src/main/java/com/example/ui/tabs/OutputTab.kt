package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtfParams
import com.example.ui.components.StudioDropdown
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.StudioSlider
import com.example.ui.components.StudioSwitch
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardAlt
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioText
import kotlin.math.roundToInt

@Composable
fun OutputTab(
    params: DtfParams,
    srcDimensions: Pair<Int, Int>,
    onUpdateParams: ((DtfParams) -> DtfParams) -> Unit,
    modifier: Modifier = Modifier
) {
    val (outDims, _) = params.calcOutputSize(
        if (srcDimensions.first > 0) srcDimensions.first else 1000,
        if (srcDimensions.second > 0) srcDimensions.second else 1000
    )
    val dpiVal = params.dpi.toIntOrNull() ?: 300
    val wIn = outDims.first.toFloat() / dpiVal
    val hIn = outDims.second.toFloat() / dpiVal
    val wCm = wIn * 2.54f
    val hCm = hIn * 2.54f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        StudioSectionHeader("Output Resolution (রেজোলিউশন)")
        StudioDropdown(
            label = "Size (আকার)",
            options = DtfParams.SIZES.keys.toList(),
            selected = params.size,
            onSelect = { onUpdateParams { p -> p.copy(size = it) } }
        )

        val isCustom = params.size == "Custom"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(StudioCard, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom px (কাস্টম পিক্সেল)",
                color = if (isCustom) StudioText else StudioMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            BasicTextField(
                value = params.custom,
                onValueChange = { newVal ->
                    if (isCustom && newVal.all { it.isDigit() }) {
                        onUpdateParams { p -> p.copy(custom = newVal) }
                    }
                },
                enabled = isCustom,
                textStyle = TextStyle(
                    color = if (isCustom) StudioText else StudioMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(StudioAccent),
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .background(if (isCustom) StudioCardAlt else StudioBorder, RoundedCornerShape(6.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        StudioDropdown(
            label = "DPI (প্রিন্ট ঘনত্ব)",
            options = listOf("150", "300", "600"),
            selected = params.dpi,
            onSelect = { onUpdateParams { p -> p.copy(dpi = it) } }
        )

        // Physical dimensions info box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = StudioAccentLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccent.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ফিজিক্যাল প্রিন্ট সাইজ:",
                    color = StudioAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${outDims.first} × ${outDims.second} px  ·  " +
                            "%.1f × %.1f in  ·  ".format(wIn, hIn) +
                            "%.1f × %.1f cm @ $dpiVal DPI".format(wCm, hCm),
                    color = StudioText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        StudioSectionHeader("Film & Orientation")
        StudioSwitch(
            text = "হরিজন্টাল মিরর (Mirror for film)",
            checked = params.mirror,
            onCheckedChange = { onUpdateParams { p -> p.copy(mirror = it) } }
        )
        StudioSwitch(
            text = "বর্ডার ট্রিম করুন (Trim transparent borders)",
            checked = params.trim,
            onCheckedChange = { onUpdateParams { p -> p.copy(trim = it) } }
        )

        StudioSectionHeader("White Underbase (সাদা আন্ডারবেস)")
        StudioSwitch(
            text = "সাদা আন্ডারবেস PNG ও তৈরি করুন",
            checked = params.ub,
            onCheckedChange = { onUpdateParams { p -> p.copy(ub = it) } }
        )
        StudioSlider(
            label = "Underbase choke (আন্ডারবেস সংকোচন)",
            value = params.ubchoke.toFloat(),
            onValueChange = { onUpdateParams { p -> p.copy(ubchoke = it.roundToInt()) } },
            valueRange = 0f..10f,
            steps = 10,
            valueText = "${params.ubchoke} px"
        )

        StudioSectionHeader("Preview Quality")
        StudioDropdown(
            label = "প্রিভিউ ডিটেইল (Preview detail)",
            options = listOf("1280", "1920", "2560"),
            selected = params.prevsz,
            onSelect = { onUpdateParams { p -> p.copy(prevsz = it) } }
        )
    }
}
