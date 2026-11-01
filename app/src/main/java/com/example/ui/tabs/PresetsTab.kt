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
import androidx.compose.material3.Surface
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
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardAlt
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
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        // Active preset banner
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = StudioAccentLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccent.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সক্রিয় প্রিসেট: ",
                    color = StudioMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = activePreset + if (presetDirty) " (পরিবর্তিত)" else "",
                    color = StudioAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        StudioSectionHeader("সংরক্ষিত প্রিসেট (Saved Presets)")
        val allPresets = listOf("Default") + presets.keys.filter { it != "Default" }
        allPresets.forEach { name ->
            val isActive = name == activePreset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        StudioSectionHeader("বর্তমান সেটিংস সেভ করুন")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = newPresetName,
                onValueChange = { newPresetName = it },
                textStyle = TextStyle(color = StudioText, fontSize = 13.sp),
                cursorBrush = SolidColor(StudioAccent),
                decorationBox = { innerTextField ->
                    if (newPresetName.isEmpty()) {
                        Text("প্রিসেটের নাম দিন…", color = StudioMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(StudioCard, RoundedCornerShape(8.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp)
            )
            StudioButton(
                text = "সেভ",
                isPrimary = true,
                onClick = {
                    if (newPresetName.isNotBlank()) {
                        onSavePreset(newPresetName)
                        newPresetName = ""
                    }
                }
            )
        }

        StudioSectionHeader("রিসেট অপশন")
        StudioButton(
            text = "ডিফল্ট সেটিংসে রিসেট করুন",
            onClick = onResetToDefaults,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        StudioButton(
            text = "সব সংরক্ষিত প্রিসেট মুছুন",
            onClick = onDeleteAllPresets,
            isDanger = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
    }
}
