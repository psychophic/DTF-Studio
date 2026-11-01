package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioDropdown
import com.example.ui.components.StudioSectionHeader
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioText

@Composable
fun BatchModalScreen(
    batchUris: List<Uri>,
    settingsChoice: String,
    presetsList: List<String>,
    suffix: String,
    policy: String,
    isRunning: Boolean,
    progress: Float,
    statusText: String,
    logs: List<String>,
    onAddUris: (List<Uri>) -> Unit,
    onRemoveUri: (Int) -> Unit,
    onClearList: () -> Unit,
    onSelectSettings: (String) -> Unit,
    onSetSuffix: (String) -> Unit,
    onSetPolicy: (String) -> Unit,
    onRunBatch: () -> Unit,
    onStopBatch: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val multiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddUris(uris)
        }
    }

    val logListState = rememberLazyListState()
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            logListState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: File list & Execution Log
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Batch background remove",
                    color = StudioText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioButton(
                        text = "Add images…",
                        isPrimary = true,
                        onClick = {
                            multiPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Text(
                        text = "${batchUris.size} images selected",
                        color = StudioMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // File list
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCard)
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (batchUris.isEmpty()) {
                        Text(
                            text = "No images added. Tap 'Add images…' to select multiple files.",
                            color = StudioMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(batchUris) { index, uri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uri.lastPathSegment ?: "Image $index",
                                        color = StudioText,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "✕",
                                        color = StudioMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .clickable { onRemoveUri(index) }
                                            .padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioButton(
                        text = "Clear list",
                        onClick = onClearList
                    )
                }

                StudioSectionHeader("Execution Log", modifier = Modifier.padding(top = 10.dp))
                // Log Box
                Box(
                    modifier = Modifier
                        .height(130.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF101014))
                        .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = logListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(logs) { _, log ->
                            val color = when {
                                log.startsWith("OK") -> Color(0xFF7BD88F)
                                log.startsWith("FAIL") -> Color(0xFFE07A7A)
                                else -> StudioMuted
                            }
                            Text(
                                text = log,
                                color = color,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Right Column: Batch settings & action
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .background(StudioPanel)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                StudioSectionHeader("Settings to use")
                val availableSettings = listOf("Current settings", "Default") + presetsList
                StudioDropdown(
                    label = "Preset",
                    options = availableSettings,
                    selected = settingsChoice,
                    onSelect = onSelectSettings
                )

                StudioSectionHeader("Output files")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "File name suffix", color = StudioText, fontSize = 12.sp)
                    BasicTextField(
                        value = suffix,
                        onValueChange = onSetSuffix,
                        textStyle = TextStyle(color = StudioText, fontSize = 12.sp),
                        cursorBrush = SolidColor(StudioText),
                        modifier = Modifier
                            .width(100.dp)
                            .height(28.dp)
                            .background(StudioCard, RoundedCornerShape(4.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                StudioDropdown(
                    label = "If file exists",
                    options = listOf("Auto-number", "Overwrite", "Skip"),
                    selected = policy,
                    onSelect = onSetPolicy
                )

                // Run / Stop buttons
                StudioButton(
                    text = if (isRunning) "Processing…" else "Run batch",
                    isPrimary = true,
                    enabled = !isRunning && batchUris.isNotEmpty(),
                    onClick = onRunBatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                )

                StudioButton(
                    text = "Stop",
                    isDanger = true,
                    enabled = isRunning,
                    onClick = onStopBatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                if (isRunning) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = StudioAccent,
                        trackColor = StudioCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }

                Text(
                    text = statusText,
                    color = StudioMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                StudioButton(
                    text = "Close",
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}
