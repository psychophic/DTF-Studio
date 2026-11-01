package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentHover
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDanger
import com.example.ui.theme.StudioDangerHover
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioText

@Composable
fun StudioSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = StudioMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp)
    )
}

@Composable
fun StudioSegmentedControl(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StudioCard)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) StudioAccent else Color.Transparent)
                    .clickable { onSelect(item) }
                    .testTag("segment_$item"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color.White else StudioMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StudioSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = StudioText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = StudioMuted,
                fontSize = 12.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = StudioAccent,
                activeTrackColor = StudioAccent,
                inactiveTrackColor = StudioBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .testTag("slider_$label")
        )
    }
}

@Composable
fun StudioSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = StudioText,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudioAccent,
                uncheckedThumbColor = StudioMuted,
                uncheckedTrackColor = StudioCard
            ),
            modifier = Modifier.testTag("switch_$text")
        )
    }
}

@Composable
fun StudioDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = StudioText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioCard)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("dropdown_$label"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selected,
                    color = StudioText,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = StudioMuted,
                    modifier = Modifier.height(18.dp).width(18.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(StudioCard)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = opt,
                                color = if (opt == selected) StudioAccent else StudioText,
                                fontSize = 12.sp,
                                fontWeight = if (opt == selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudioButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isDanger: Boolean = false,
    enabled: Boolean = true
) {
    val bg = when {
        isPrimary -> StudioAccent
        isDanger -> StudioCard
        else -> StudioCard
    }
    val contentColor = when {
        isPrimary -> Color.White
        isDanger -> StudioDanger
        else -> StudioText
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = contentColor,
            disabledContainerColor = StudioCard.copy(alpha = 0.5f),
            disabledContentColor = StudioMuted
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .height(34.dp)
            .testTag("btn_$text")
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
        )
    }
}
