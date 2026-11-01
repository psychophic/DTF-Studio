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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Stop
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
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardAlt
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        val isTablet = maxWidth >= 768.dp

        if (isTablet) {
            // Tablet Side-by-Side Layout
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    BatchFileListArea(
                        batchUris = batchUris,
                        onAddClick = {
                            multiPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveUri = onRemoveUri,
                        onClearList = onClearList,
                        logs = logs,
                        logListState = logListState,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .background(StudioPanel)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    BatchSettingsPanel(
                        presetsList = presetsList,
                        settingsChoice = settingsChoice,
                        suffix = suffix,
                        policy = policy,
                        isRunning = isRunning,
                        progress = progress,
                        statusText = statusText,
                        batchUrisCount = batchUris.size,
                        onSelectSettings = onSelectSettings,
                        onSetSuffix = onSetSuffix,
                        onSetPolicy = onSetPolicy,
                        onRunBatch = onRunBatch,
                        onStopBatch = onStopBatch,
                        onClose = onClose
                    )
                }
            }
        } else {
            // Mobile Stacked Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "ব্যাচ প্রসেসিং (Batch Processing)",
                    color = StudioAccent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Top Area: File list and log
                BatchFileListArea(
                    batchUris = batchUris,
                    onAddClick = {
                        multiPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveUri = onRemoveUri,
                    onClearList = onClearList,
                    logs = logs,
                    logListState = logListState,
                    modifier = Modifier.weight(1f)
                )

                // Bottom Area: Settings and Action in compact card
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = StudioPanel,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        BatchSettingsPanel(
                            presetsList = presetsList,
                            settingsChoice = settingsChoice,
                            suffix = suffix,
                            policy = policy,
                            isRunning = isRunning,
                            progress = progress,
                            statusText = statusText,
                            batchUrisCount = batchUris.size,
                            onSelectSettings = onSelectSettings,
                            onSetSuffix = onSetSuffix,
                            onSetPolicy = onSetPolicy,
                            onRunBatch = onRunBatch,
                            onStopBatch = onStopBatch,
                            onClose = onClose
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchFileListArea(
    batchUris: List<Uri>,
    onAddClick: () -> Unit,
    onRemoveUri: (Int) -> Unit,
    onClearList: () -> Unit,
    logs: List<String>,
    logListState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioButton(
                text = "ছবি যোগ করুন (Add Images)",
                isPrimary = true,
                icon = Icons.Default.AddPhotoAlternate,
                onClick = onAddClick
            )
            Text(
                text = "${batchUris.size} ছবি নির্বাচিত",
                color = StudioMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // File List Box
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
                    text = "কোনো ছবি যুক্ত করা হয়নি। 'ছবি যোগ করুন' বাটনে চাপুন।",
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
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${uri.lastPathSegment ?: "Image $index"}",
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            StudioButton(
                text = "তালিকা মুছুন (Clear)",
                onClick = onClearList
            )
        }

        StudioSectionHeader("এক্সিকিউশন লগ (Execution Log)")
        Box(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StudioCardAlt)
                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                state = logListState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(logs) { _, log ->
                    val color = when {
                        log.startsWith("OK") -> Color(0xFF16A34A)
                        log.startsWith("FAIL") -> Color(0xFFDC2626)
                        else -> StudioMuted
                    }
                    Text(
                        text = log,
                        color = color,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchSettingsPanel(
    presetsList: List<String>,
    settingsChoice: String,
    suffix: String,
    policy: String,
    isRunning: Boolean,
    progress: Float,
    statusText: String,
    batchUrisCount: Int,
    onSelectSettings: (String) -> Unit,
    onSetSuffix: (String) -> Unit,
    onSetPolicy: (String) -> Unit,
    onRunBatch: () -> Unit,
    onStopBatch: () -> Unit,
    onClose: () -> Unit
) {
    StudioSectionHeader("প্রিসেট নির্বাচন")
    val availableSettings = listOf("Current settings", "Default") + presetsList
    StudioDropdown(
        label = "প্রিসেট",
        options = availableSettings,
        selected = settingsChoice,
        onSelect = onSelectSettings
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "ফাইলের নামের সাফিক্স", color = StudioText, fontSize = 12.sp)
        BasicTextField(
            value = suffix,
            onValueChange = onSetSuffix,
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

    StudioDropdown(
        label = "ফাইল ইতিমধ্যে থাকলে",
        options = listOf("Auto-number", "Overwrite", "Skip"),
        selected = policy,
        onSelect = onSetPolicy
    )

    if (isRunning) {
        LinearProgressIndicator(
            progress = { progress },
            color = StudioAccent,
            trackColor = StudioCardAlt,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }

    Text(
        text = statusText,
        color = StudioMuted,
        fontSize = 11.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudioButton(
            text = if (isRunning) "প্রসেস হচ্ছে…" else "রান ব্যাচ (Run Batch)",
            isPrimary = true,
            enabled = !isRunning && batchUrisCount > 0,
            onClick = onRunBatch,
            modifier = Modifier.weight(1f)
        )
        if (isRunning) {
            StudioButton(
                text = "থামান",
                isDanger = true,
                icon = Icons.Default.Stop,
                onClick = onStopBatch
            )
        }
        StudioButton(
            text = "বন্ধ করুন",
            onClick = onClose
        )
    }
}
