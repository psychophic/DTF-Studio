package com.example.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.DtfParams
import com.example.ui.components.StudioSectionHeader
import com.example.ui.components.StudioSlider
import com.example.ui.components.StudioSwitch
import kotlin.math.roundToInt

@Composable
fun AdjustTab(
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
        StudioSectionHeader("Tone")
        StudioSlider(
            label = "Brightness",
            value = params.bright,
            onValueChange = { onUpdateParams { p -> p.copy(bright = it) } },
            valueRange = 50f..150f,
            valueText = "${params.bright.roundToInt()}%"
        )
        StudioSlider(
            label = "Contrast",
            value = params.contrast,
            onValueChange = { onUpdateParams { p -> p.copy(contrast = it) } },
            valueRange = 50f..200f,
            valueText = "${params.contrast.roundToInt()}%"
        )
        StudioSlider(
            label = "Saturation",
            value = params.sat,
            onValueChange = { onUpdateParams { p -> p.copy(sat = it) } },
            valueRange = 0f..200f,
            valueText = "${params.sat.roundToInt()}%"
        )
        StudioSlider(
            label = "Gamma",
            value = params.gamma,
            onValueChange = { onUpdateParams { p -> p.copy(gamma = it) } },
            valueRange = 0.5f..2.0f,
            valueText = "%.2f".format(params.gamma)
        )
        StudioSlider(
            label = "Sharpen",
            value = params.sharp,
            onValueChange = { onUpdateParams { p -> p.copy(sharp = it) } },
            valueRange = 0f..300f,
            valueText = "${params.sharp.roundToInt()}%"
        )

        StudioSectionHeader("Colour")
        StudioSwitch(
            text = "Convert to black & white",
            checked = params.gray,
            onCheckedChange = { onUpdateParams { p -> p.copy(gray = it) } }
        )
        StudioSwitch(
            text = "Invert image",
            checked = params.invert,
            onCheckedChange = { onUpdateParams { p -> p.copy(invert = it) } }
        )
    }
}
