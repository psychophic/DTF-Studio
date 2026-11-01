package com.example.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtfParams
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.StudioSegmentedControl
import com.example.ui.components.StudioSlider
import com.example.ui.components.StudioSwitch
import com.example.ui.theme.StudioMuted
import kotlin.math.roundToInt

@Composable
fun HalftoneTab(
    params: DtfParams,
    onUpdateParams: ((DtfParams) -> DtfParams) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Pick a pattern to apply halftone. Zoom in on the preview to see the dots.",
            color = StudioMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        StudioSectionHeader("Pattern")
        StudioSegmentedControl(
            items = listOf("Dot", "Diamond", "Square", "Line"),
            selected = params.shape,
            onSelect = { onUpdateParams { p -> p.copy(shape = it, mode = "Halftone") } }
        )

        StudioSectionHeader("Dots")
        StudioSlider(
            label = "Dot spacing (output px)",
            value = params.dot,
            onValueChange = { onUpdateParams { p -> p.copy(dot = it, mode = "Halftone") } },
            valueRange = 3f..40f,
            valueText = "${params.dot.roundToInt()} px"
        )
        StudioSlider(
            label = "Screen angle",
            value = params.angle,
            onValueChange = { onUpdateParams { p -> p.copy(angle = it, mode = "Halftone") } },
            valueRange = 0f..90f,
            valueText = "${params.angle.roundToInt()}°"
        )
        StudioSlider(
            label = "Dot gain",
            value = params.gain,
            onValueChange = { onUpdateParams { p -> p.copy(gain = it, mode = "Halftone") } },
            valueRange = 50f..200f,
            valueText = "${params.gain.roundToInt()}%"
        )
        StudioSlider(
            label = "Drop tiny dots below",
            value = params.mindot,
            onValueChange = { onUpdateParams { p -> p.copy(mindot = it, mode = "Halftone") } },
            valueRange = 0f..20f,
            valueText = "${params.mindot.roundToInt()}%"
        )

        StudioSectionHeader("Ink")
        StudioSegmentedControl(
            items = listOf("Original", "White", "Black"),
            selected = params.ink,
            onSelect = { onUpdateParams { p -> p.copy(ink = it, mode = "Halftone") } }
        )
        StudioSwitch(
            text = "Solid colour per dot",
            checked = params.flat,
            onCheckedChange = { onUpdateParams { p -> p.copy(flat = it, mode = "Halftone") } }
        )
    }
}
