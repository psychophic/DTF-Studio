package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioScrollableTabs
import com.example.ui.components.StudioSegmentedControl
import com.example.ui.components.ZoomableCanvas
import com.example.ui.tabs.AdjustTab
import com.example.ui.tabs.HalftoneTab
import com.example.ui.tabs.OutputTab
import com.example.ui.tabs.PresetsTab
import com.example.ui.tabs.RemoveTab
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentLight
import com.example.ui.theme.StudioAccentMedium
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardAlt
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioText
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val params by viewModel.params.collectAsState()
    val sourceBitmap by viewModel.sourceBitmap.collectAsState()
    val resultBitmap by viewModel.resultBitmap.collectAsState()
    val sourceName by viewModel.sourceName.collectAsState()
    val sourceDimensions by viewModel.sourceDimensions.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val bgMode by viewModel.bgMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isPickingColor by viewModel.isPickingColor.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()

    val activePreset by viewModel.activePreset.collectAsState()
    val presetDirty by viewModel.presetDirty.collectAsState()
    val presets by viewModel.presets.collectAsState()

    var fitTrigger by remember { mutableIntStateOf(0) }

    val singlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openImage(uri)
        }
    }

    // Modal dialogs
    if (activeDialog == "Print") {
        BackHandler { viewModel.showDialog(null) }
        val unit by viewModel.printUnit.collectAsState()
        val sizes by viewModel.printSizes.collectAsState()
        val selectedIndex by viewModel.selectedSizeIndex.collectAsState()
        val shirtKind by viewModel.shirtKind.collectAsState()
        val shirtSize by viewModel.shirtSize.collectAsState()
        val collarLine by viewModel.collarLine.collectAsState()
        val printPaper by viewModel.printPaper.collectAsState()
        val printDpi by viewModel.printDpi.collectAsState()
        val printCopies by viewModel.printCopies.collectAsState()
        val printMirror by viewModel.printMirror.collectAsState()
        val printStatus by viewModel.printStatus.collectAsState()

        PrintModalScreen(
            designBitmap = resultBitmap ?: sourceBitmap,
            unit = unit,
            sizes = sizes,
            selectedIndex = selectedIndex,
            shirtKind = shirtKind,
            shirtSize = shirtSize,
            collarLine = collarLine,
            paper = printPaper,
            dpi = printDpi,
            copies = printCopies,
            mirror = printMirror,
            statusText = printStatus,
            onSelectUnit = { viewModel.setPrintUnit(it) },
            onSelectSize = { viewModel.selectPrintSize(it) },
            onSaveSize = { viewModel.savePrintSize(it) },
            onDeleteSize = { viewModel.deletePrintSize(it) },
            onResetSizes = { viewModel.resetPrintSizes() },
            onSetShirtKind = { viewModel.setShirtKind(it) },
            onSetShirtSize = { viewModel.setShirtSize(it) },
            onSetCollarLine = { viewModel.setCollarLine(it) },
            onSetPaper = { viewModel.setPrintPaper(it) },
            onSetDpi = { viewModel.setPrintDpi(it) },
            onSetCopies = { viewModel.setPrintCopies(it) },
            onSetMirror = { viewModel.setPrintMirror(it) },
            onSavePrintFile = {
                viewModel.savePrintSheet { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onClose = { viewModel.showDialog(null) }
        )
        return
    }

    if (activeDialog == "Batch") {
        BackHandler { viewModel.showDialog(null) }
        val batchUris by viewModel.batchUris.collectAsState()
        val settingsChoice by viewModel.batchSettingsChoice.collectAsState()
        val suffix by viewModel.batchSuffix.collectAsState()
        val policy by viewModel.batchPolicy.collectAsState()
        val isRunning by viewModel.batchRunning.collectAsState()
        val progress by viewModel.batchProgress.collectAsState()
        val batchStatus by viewModel.batchStatus.collectAsState()
        val logs by viewModel.batchLogs.collectAsState()

        BatchModalScreen(
            batchUris = batchUris,
            settingsChoice = settingsChoice,
            presetsList = presets.keys.toList(),
            suffix = suffix,
            policy = policy,
            isRunning = isRunning,
            progress = progress,
            statusText = batchStatus,
            logs = logs,
            onAddUris = { viewModel.addBatchUris(it) },
            onRemoveUri = { viewModel.removeBatchUri(it) },
            onClearList = { viewModel.clearBatchList() },
            onSelectSettings = { viewModel.setBatchSettings(it) },
            onSetSuffix = { viewModel.setBatchSuffix(it) },
            onSetPolicy = { viewModel.setBatchPolicy(it) },
            onRunBatch = { viewModel.runBatch() },
            onStopBatch = { viewModel.stopBatch() },
            onClose = { viewModel.showDialog(null) }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DTF Halftone",
                                color = StudioAccent,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " Studio",
                                color = StudioText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Developed by Shanewas",
                            color = StudioMuted,
                            fontSize = 10.sp
                        )
                    }
                },
                actions = {
                    // Upload button in Top Bar
                    StudioButton(
                        text = "ছবি আপলোড",
                        isPrimary = true,
                        icon = Icons.Default.AddPhotoAlternate,
                        onClick = {
                            singlePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset settings",
                            tint = StudioMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clear() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear image",
                            tint = StudioMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioPanel,
                    titleContentColor = StudioText
                ),
                modifier = Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(0.dp))
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StudioPanel,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isExporting) {
                        LinearProgressIndicator(
                            color = StudioAccent,
                            trackColor = StudioCardAlt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    StudioButton(
                        text = if (isExporting) "এক্সপোর্ট হচ্ছে…" else "Export Transparent PNG (এক্সপোর্ট)",
                        isPrimary = true,
                        enabled = !isExporting && sourceBitmap != null,
                        onClick = {
                            viewModel.exportTransparentPng { msg ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioButton(
                            text = "Print (প্রিন্ট…)",
                            icon = Icons.Default.Print,
                            onClick = { viewModel.showDialog("Print") },
                            modifier = Modifier.weight(1f)
                        )
                        StudioButton(
                            text = "Batch (ব্যাচ…)",
                            icon = Icons.Default.ViewCarousel,
                            onClick = { viewModel.showDialog("Batch") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBg)
        ) {
            val isTabletOrLandscape = maxWidth >= 768.dp

            if (isTabletOrLandscape) {
                // Tablet / Desktop side-by-side layout
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Control Panel (380.dp)
                    Column(
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                            .background(StudioPanel)
                            .padding(14.dp)
                    ) {
                        StudioControlsHeader(params, viewModel)
                        StudioScrollableTabs(
                            items = listOf("Remove", "Adjust", "Halftone", "Output", "Presets"),
                            selected = currentTab,
                            onSelect = { viewModel.setCurrentTab(it) }
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            StudioTabContent(
                                currentTab = currentTab,
                                params = params,
                                sourceDimensions = sourceDimensions,
                                activePreset = activePreset,
                                presetDirty = presetDirty,
                                presets = presets,
                                viewModel = viewModel
                            )
                        }
                    }

                    // Right Canvas Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(14.dp)
                    ) {
                        CanvasToolbar(
                            sourceName = sourceName,
                            viewMode = viewMode,
                            bgMode = bgMode,
                            viewModel = viewModel
                        )

                        val displayBitmap = if (viewMode == "Original") {
                            viewModel.proxyBitmap ?: sourceBitmap
                        } else {
                            resultBitmap ?: viewModel.proxyBitmap ?: sourceBitmap
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        ) {
                            ZoomableCanvas(
                                bitmap = displayBitmap,
                                bgMode = bgMode,
                                isPickingColor = isPickingColor,
                                onPixelTapped = { x, y -> viewModel.onImageTapped(x, y) },
                                onCancelPickColor = { viewModel.cancelPickColor() },
                                onOpenImage = {
                                    singlePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                fitTrigger = fitTrigger
                            )
                        }

                        StudioStatusBar(statusMessage = statusMessage, isBusy = isBusy)
                    }
                }
            } else {
                // Mobile Portrait Responsive Layout (Clean Stack)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    // Preview Canvas Toolbar
                    CanvasToolbar(
                        sourceName = sourceName,
                        viewMode = viewMode,
                        bgMode = bgMode,
                        viewModel = viewModel
                    )

                    // Canvas Area (Fixed height 250.dp to leave ample space for controls without layout jumping)
                    val displayBitmap = if (viewMode == "Original") {
                        viewModel.proxyBitmap ?: sourceBitmap
                    } else {
                        resultBitmap ?: viewModel.proxyBitmap ?: sourceBitmap
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    ) {
                        ZoomableCanvas(
                            bitmap = displayBitmap,
                            bgMode = bgMode,
                            isPickingColor = isPickingColor,
                            onPixelTapped = { x, y -> viewModel.onImageTapped(x, y) },
                            onCancelPickColor = { viewModel.cancelPickColor() },
                            onOpenImage = {
                                singlePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            fitTrigger = fitTrigger
                        )
                    }

                    StudioStatusBar(statusMessage = statusMessage, isBusy = isBusy)

                    // Mode Toggle: Remove Background vs Halftone
                    StudioControlsHeader(params, viewModel)

                    // Category Tabs
                    StudioScrollableTabs(
                        items = listOf("Remove", "Adjust", "Halftone", "Output", "Presets"),
                        selected = currentTab,
                        onSelect = { viewModel.setCurrentTab(it) }
                    )

                    // Tab Content (takes remaining vertical space)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        StudioTabContent(
                            currentTab = currentTab,
                            params = params,
                            sourceDimensions = sourceDimensions,
                            activePreset = activePreset,
                            presetDirty = presetDirty,
                            presets = presets,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioControlsHeader(
    params: com.example.model.DtfParams,
    viewModel: StudioViewModel
) {
    StudioSegmentedControl(
        items = listOf("Remove Background", "Halftone"),
        selected = params.mode,
        onSelect = { viewModel.setMode(it) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun CanvasToolbar(
    sourceName: String,
    viewMode: String,
    bgMode: String,
    viewModel: StudioViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sourceName,
            color = StudioMuted,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View Mode: Result / Original
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioCardAlt)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                listOf("Result", "Original").forEach { item ->
                    val isSel = item == viewMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) StudioAccent else Color.Transparent)
                            .clickable { viewModel.setViewMode(item) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item,
                            color = if (isSel) Color.White else StudioMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // BG Mode: Checker / Black / White / Gray
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioCardAlt)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                listOf("Checker", "Black", "White", "Gray").forEach { item ->
                    val isSel = item == bgMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) StudioAccent else Color.Transparent)
                            .clickable { viewModel.setBgMode(item) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.take(3), // Che, Bla, Whi, Gra for compact display
                            color = if (isSel) Color.White else StudioMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioStatusBar(statusMessage: String, isBusy: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statusMessage,
            color = StudioMuted,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (isBusy) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = StudioAccentLight
            ) {
                Text(
                    text = "রেন্ডার হচ্ছে…",
                    color = StudioAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StudioTabContent(
    currentTab: String,
    params: com.example.model.DtfParams,
    sourceDimensions: Pair<Int, Int>,
    activePreset: String,
    presetDirty: Boolean,
    presets: Map<String, com.example.model.DtfParams>,
    viewModel: StudioViewModel
) {
    when (currentTab) {
        "Remove" -> RemoveTab(
            params = params,
            onUpdateParams = { viewModel.updateParams(it) },
            onStartPickColor = { viewModel.startPickColor() }
        )
        "Adjust" -> AdjustTab(
            params = params,
            onUpdateParams = { viewModel.updateParams(it) }
        )
        "Halftone" -> HalftoneTab(
            params = params,
            onUpdateParams = { viewModel.updateParams(it) }
        )
        "Output" -> OutputTab(
            params = params,
            srcDimensions = sourceDimensions,
            onUpdateParams = { viewModel.updateParams(it) }
        )
        "Presets" -> PresetsTab(
            activePreset = activePreset,
            presetDirty = presetDirty,
            presets = presets,
            onApplyPreset = { viewModel.applyPreset(it) },
            onSavePreset = { viewModel.savePreset(it) },
            onDeletePreset = { viewModel.deletePreset(it) },
            onDeleteAllPresets = { viewModel.deleteAllPresets() },
            onResetToDefaults = { viewModel.reset() }
        )
    }
}
