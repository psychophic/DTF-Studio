package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
            .padding(bottom = 16.dp)
    ) {
        StudioSectionHeader("Print on")
        StudioSegmentedControl(
            items = listOf("Black shirt", "White shirt", "Custom"),
            selected = params.target,
            onSelect = { onUpdateParams { p -> p.copy(target = it) } }
        )
        Text(
            text = "Black shirt → black removed  ·  White shirt → white removed",
            color = StudioMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioButton(
                text = "Pick colour from image",
                onClick = onStartPickColor
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(params.keyColor))
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            )
        }

        StudioSectionHeader("Method")
        StudioSegmentedControl(
            items = listOf("Soft blend", "Solid cut"),
            selected = params.method,
            onSelect = { onUpdateParams { p -> p.copy(method = it) } }
        )

        StudioSlider(
            label = "Tolerance",
            value = params.tol,
            onValueChange = { onUpdateParams { p -> p.copy(tol = it) } },
            valueRange = 0f..60f,
            valueText = "${params.tol.roundToInt()}%"
        )
        StudioSlider(
            label = "Edge softness (solid cut)",
            value = params.soft,
            onValueChange = { onUpdateParams { p -> p.copy(soft = it) } },
            valueRange = 0f..50f,
            valueText = "${params.soft.roundToInt()}%"
        )
        StudioSlider(
            label = "Ink density",
            value = params.density,
            onValueChange = { onUpdateParams { p -> p.copy(density = it) } },
            valueRange = 40f..220f,
            valueText = "${params.density.roundToInt()}%"
        )
        StudioSwitch(
            text = "Recover colours (un-blend edges)",
            checked = params.unmix,
            onCheckedChange = { onUpdateParams { p -> p.copy(unmix = it) } }
        )

        StudioSectionHeader("DTF clean-up")
        StudioSlider(
            label = "Drop faint pixels below",
            value = params.cut,
            onValueChange = { onUpdateParams { p -> p.copy(cut = it) } },
            valueRange = 0f..50f,
            valueText = "${params.cut.roundToInt()}%"
        )
        StudioSlider(
            label = "Make opaque above",
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
            text = "Binary alpha (no semi-transparency)",
            checked = params.binary,
            onCheckedChange = { onUpdateParams { p -> p.copy(binary = it) } }
        )
    }
}
