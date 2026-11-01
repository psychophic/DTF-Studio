package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtfParams
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.StudioSegmentedControl
import com.example.ui.components.StudioSlider
import com.example.ui.components.StudioSwitch
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioText
import kotlin.math.roundToInt

@Composable
fun RemoveTab(
    params: DtfParams,
    onUpdateParams: ((DtfParams) -> DtfParams) -> Unit,
    onStartPickColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        StudioSectionHeader("Print on (শার্টের রঙ)")
        StudioSegmentedControl(
            items = listOf("Black shirt", "White shirt", "Custom"),
            selected = params.target,
            onSelect = { onUpdateParams { p -> p.copy(target = it) } }
        )
        Text(
            text = "Black shirt → কালো রঙ রিমুভ  ·  White shirt → সাদা রঙ রিমুভ",
            color = StudioMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(StudioCard, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioButton(
                text = "ছবি থেকে রঙ বাছুন (Pick Color)",
                onClick = onStartPickColor,
                icon = Icons.Default.Colorize
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(params.keyColor))
                    .border(2.dp, StudioBorder, RoundedCornerShape(8.dp))
            )
        }

        StudioSectionHeader("Removal Method")
        StudioSegmentedControl(
            items = listOf("Soft blend", "Solid cut"),
            selected = params.method,
            onSelect = { onUpdateParams { p -> p.copy(method = it) } }
        )

        StudioSlider(
            label = "Tolerance (সহনশীলতা)",
            value = params.tol,
            onValueChange = { onUpdateParams { p -> p.copy(tol = it) } },
            valueRange = 0f..60f,
            valueText = "${params.tol.roundToInt()}%"
        )
        StudioSlider(
            label = "Edge softness (প্রান্তের কোমলতা)",
            value = params.soft,
            onValueChange = { onUpdateParams { p -> p.copy(soft = it) } },
            valueRange = 0f..50f,
            valueText = "${params.soft.roundToInt()}%"
        )
        StudioSlider(
            label = "Ink density (কালির ঘনত্ব)",
            value = params.density,
            onValueChange = { onUpdateParams { p -> p.copy(density = it) } },
            valueRange = 40f..220f,
            valueText = "${params.density.roundToInt()}%"
        )
        StudioSwitch(
            text = "রঙ পুনরুদ্ধার (Recover colours)",
            checked = params.unmix,
            onCheckedChange = { onUpdateParams { p -> p.copy(unmix = it) } }
        )

        StudioSectionHeader("DTF clean-up")
        StudioSlider(
            label = "হালকা পিক্সেল বাদ দিন (Drop faint)",
            value = params.cut,
            onValueChange = { onUpdateParams { p -> p.copy(cut = it) } },
            valueRange = 0f..50f,
            valueText = "${params.cut.roundToInt()}%"
        )
        StudioSlider(
            label = "পূর্ণ অস্বচ্ছ করুন (Make opaque above)",
            value = params.snap,
            onValueChange = { onUpdateParams { p -> p.copy(snap = it) } },
            valueRange = 50f..100f,
            valueText = "${params.snap.roundToInt()}%"
        )
        StudioSlider(
            label = "Spread (+) / Choke (−)",
            value = params.choke.toFloat(),
            onValueChange = { onUpdateParams { p -> p.copy(choke = it.roundToInt()) } },
            valueRange = -10f..10f,
            steps = 20,
            valueText = "${if (params.choke > 0) "+" else ""}${params.choke} px"
        )
        StudioSwitch(
            text = "Binary alpha (অর্ধ-স্বচ্ছতা ছাড়া)",
            checked = params.binary,
            onCheckedChange = { onUpdateParams { p -> p.copy(binary = it) } }
        )
    }
}
