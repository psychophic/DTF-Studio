package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtfParams
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioSectionHeader
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioText

@Composable
fun PresetsTab(
    activePreset: String,
    presetDirty: Boolean,
    presets: Map<String, DtfParams>,
    onApplyPreset: (String) -> Unit,
    onSavePreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onDeleteAllPresets: () -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newPresetName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Active: $activePreset" + if (presetDirty) "  ·  modified" else "",
            color = StudioText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        StudioSectionHeader("Saved presets")
        val allPresets = listOf("Default") + presets.keys.filter { it != "Default" }
        allPresets.forEach { name ->
            val isActive = name == activePreset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioButton(
                    text = name,
                    onClick = { onApplyPreset(name) },
                    isPrimary = isActive,
                    modifier = Modifier.weight(1f)
                )
                if (name != "Default") {
                    StudioButton(
                        text = "✕",
                        onClick = { onDeletePreset(name) },
                        isDanger = true,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        StudioSectionHeader("Save current settings as")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = newPresetName,
                onValueChange = { newPresetName = it },
                textStyle = TextStyle(color = StudioText, fontSize = 12.sp),
                cursorBrush = SolidColor(StudioText),
                decorationBox = { innerTextField ->
                    if (newPresetName.isEmpty()) {
                        Text("Preset name", color = StudioMuted, fontSize = 12.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .background(StudioCard, RoundedCornerShape(6.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            StudioButton(
                text = "Save",
                isPrimary = true,
                onClick = {
                    if (newPresetName.isNotBlank()) {
                        onSavePreset(newPresetName)
                        newPresetName = ""
                    }
                }
            )
        }
        Text(
            text = "Same name = overwrite. Presets keep every setting (mode, removal, halftone, output).",
            color = StudioMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        StudioSectionHeader("Reset")
        StudioButton(
            text = "Reset to default settings",
            onClick = onResetToDefaults,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
        )
        StudioButton(
            text = "Delete all saved presets",
            onClick = onDeleteAllPresets,
            isDanger = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}
