package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioSegmentedControl
import com.example.ui.components.ZoomableCanvas
import com.example.ui.tabs.AdjustTab
import com.example.ui.tabs.HalftoneTab
import com.example.ui.tabs.OutputTab
import com.example.ui.tabs.PresetsTab
import com.example.ui.tabs.RemoveTab
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioMuted
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioText
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

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
    val zoomPercent by viewModel.zoomPercent.collectAsState()
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBg)
        ) {
            val isWide = maxWidth >= 760.dp

            if (isWide) {
                // Wide Desktop / Tablet Side-by-Side layout
                Row(modifier = Modifier.fillMaxSize()) {
                    StudioSidebar(
                        viewModel = viewModel,
                        params = params,
                        currentTab = currentTab,
                        activePreset = activePreset,
                        presetDirty = presetDirty,
                        presets = presets,
                        isExporting = isExporting,
                        sourceDimensions = sourceDimensions,
                        onExport = {
                            viewModel.exportTransparentPng { msg ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                    )

                    StudioMainArea(
                        viewModel = viewModel,
                        sourceBitmap = sourceBitmap,
                        resultBitmap = resultBitmap,
                        sourceName = sourceName,
                        viewMode = viewMode,
                        bgMode = bgMode,
                        zoomPercent = zoomPercent,
                        statusMessage = statusMessage,
                        isBusy = isBusy,
                        isPickingColor = isPickingColor,
                        fitTrigger = fitTrigger,
                        onFit = { fitTrigger++ },
                        onOpenImage = {
                            singlePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Mobile Portrait Responsive layout
                Column(modifier = Modifier.fillMaxSize()) {
                    // Canvas Preview Area on Top (40% height)
                    StudioMainArea(
                        viewModel = viewModel,
                        sourceBitmap = sourceBitmap,
                        resultBitmap = resultBitmap,
                        sourceName = sourceName,
                        viewMode = viewMode,
                        bgMode = bgMode,
                        zoomPercent = zoomPercent,
                        statusMessage = statusMessage,
                        isBusy = isBusy,
                        isPickingColor = isPickingColor,
                        fitTrigger = fitTrigger,
                        onFit = { fitTrigger++ },
                        onOpenImage = {
                            singlePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    // Controls Sidebar / Bottom Sheet
                    StudioSidebar(
                        viewModel = viewModel,
                        params = params,
                        currentTab = currentTab,
                        activePreset = activePreset,
                        presetDirty = presetDirty,
                        presets = presets,
                        isExporting = isExporting,
                        sourceDimensions = sourceDimensions,
                        onExport = {
                            viewModel.exportTransparentPng { msg ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSidebar(
    viewModel: StudioViewModel,
    params: com.example.model.DtfParams,
    currentTab: String,
    activePreset: String,
    presetDirty: Boolean,
    presets: Map<String, com.example.model.DtfParams>,
    isExporting: Boolean,
    sourceDimensions: Pair<Int, Int>,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioPanel)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // App Header matching Python
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DTF Halftone",
                        color = StudioText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " Studio",
                        color = StudioMuted,
                        fontSize = 17.sp
                    )
                }
                Text(
                    text = "Developed by Shanewas",
                    color = StudioMuted,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StudioButton(
                    text = "Reset",
                    onClick = { viewModel.reset() }
                )
                StudioButton(
                    text = "Clear",
                    onClick = { viewModel.clear() }
                )
            }
        }

        // Mode switch: ["Remove Background", "Halftone"]
        StudioSegmentedControl(
            items = listOf("Remove Background", "Halftone"),
            selected = params.mode,
            onSelect = { viewModel.setMode(it) },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Tabs: ["Remove", "Adjust", "Halftone", "Output", "Presets"]
        StudioSegmentedControl(
            items = listOf("Remove", "Adjust", "Halftone", "Output", "Presets"),
            selected = currentTab,
            onSelect = { viewModel.setCurrentTab(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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

        // Bottom Action Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isExporting) {
                LinearProgressIndicator(
                    color = StudioAccent,
                    trackColor = StudioCard,
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }
            StudioButton(
                text = if (isExporting) "Exporting…" else "Export Transparent PNG",
                isPrimary = true,
                enabled = !isExporting,
                onClick = onExport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StudioButton(
                    text = "Print…",
                    onClick = { viewModel.showDialog("Print") },
                    modifier = Modifier.weight(1f)
                )
                StudioButton(
                    text = "Batch…",
                    onClick = { viewModel.showDialog("Batch") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StudioMainArea(
    viewModel: StudioViewModel,
    sourceBitmap: android.graphics.Bitmap?,
    resultBitmap: android.graphics.Bitmap?,
    sourceName: String,
    viewMode: String,
    bgMode: String,
    zoomPercent: Float,
    statusMessage: String,
    isBusy: Boolean,
    isPickingColor: Boolean,
    fitTrigger: Int,
    onFit: () -> Unit,
    onOpenImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioBg)
            .padding(8.dp)
    ) {
        // Top Toolbar matching Python
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                StudioButton(
                    text = "Open Image",
                    onClick = onOpenImage
                )
                Text(
                    text = sourceName,
                    color = StudioMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // View: [Result, Original]
                StudioSegmentedControl(
                    items = listOf("Result", "Original"),
                    selected = viewMode,
                    onSelect = { viewModel.setViewMode(it) },
                    modifier = Modifier.width(140.dp)
                )

                // Background: [Checker, Black, White, Gray]
                StudioSegmentedControl(
                    items = listOf("Checker", "Black", "White", "Gray"),
                    selected = bgMode,
                    onSelect = { viewModel.setBgMode(it) },
                    modifier = Modifier.width(190.dp)
                )

                StudioButton(
                    text = "Fit",
                    onClick = onFit,
                    modifier = Modifier.width(44.dp)
                )

                Text(
                    text = "${(zoomPercent * 100).toInt()}%",
                    color = StudioMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.width(42.dp)
                )
            }
        }

        // Canvas Area
        val displayBitmap = if (viewMode == "Original") {
            viewModel.proxyBitmap ?: sourceBitmap
        } else {
            resultBitmap ?: viewModel.proxyBitmap ?: sourceBitmap
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
        ) {
            ZoomableCanvas(
                bitmap = displayBitmap,
                bgMode = bgMode,
                isPickingColor = isPickingColor,
                onPixelTapped = { x, y -> viewModel.onImageTapped(x, y) },
                onZoomChanged = { z -> viewModel.setZoom(z) },
                fitTrigger = fitTrigger
            )
        }

        // Bottom Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
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
                Text(
                    text = "● Rendering…",
                    color = StudioAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
