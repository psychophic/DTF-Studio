package com.example.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.DtfEngine
import com.example.model.DtfParams
import com.example.model.PrintConstants
import com.example.model.PrintSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Parameters
    private val _params = MutableStateFlow(DtfParams())
    val params = _params.asStateFlow()

    // Loaded source
    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap = _sourceBitmap.asStateFlow()

    var proxyBitmap: Bitmap? = null
        private set

    // Rendered result
    private val _resultBitmap = MutableStateFlow<Bitmap?>(null)
    val resultBitmap = _resultBitmap.asStateFlow()

    private val _sourceName = MutableStateFlow("No image loaded")
    val sourceName = _sourceName.asStateFlow()

    private val _sourceDimensions = MutableStateFlow(Pair(0, 0))
    val sourceDimensions = _sourceDimensions.asStateFlow()

    // View toggles
    private val _viewMode = MutableStateFlow("Result") // "Result", "Original"
    val viewMode = _viewMode.asStateFlow()

    private val _bgMode = MutableStateFlow("Checker") // "Checker", "Black", "White", "Gray"
    val bgMode = _bgMode.asStateFlow()

    private val _zoomPercent = MutableStateFlow(100f)
    val zoomPercent = _zoomPercent.asStateFlow()

    // Status & Progress
    private val _statusMessage = MutableStateFlow("Open an image to begin")
    val statusMessage = _statusMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy = _isBusy.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    private val _isPickingColor = MutableStateFlow(false)
    val isPickingColor = _isPickingColor.asStateFlow()

    // Navigation & Tabs
    private val _currentTab = MutableStateFlow("Remove")
    val currentTab = _currentTab.asStateFlow()

    private val _activeDialog = MutableStateFlow<String?>(null) // "Print", "Batch", "SavePreset", etc.
    val activeDialog = _activeDialog.asStateFlow()

    // Presets
    private val _activePreset = MutableStateFlow("Default")
    val activePreset = _activePreset.asStateFlow()

    private val _presetDirty = MutableStateFlow(false)
    val presetDirty = _presetDirty.asStateFlow()

    private val _presets = MutableStateFlow<Map<String, DtfParams>>(emptyMap())
    val presets = _presets.asStateFlow()

    // Print & Mockup
    private val _printUnit = MutableStateFlow("in") // "in", "cm"
    val printUnit = _printUnit.asStateFlow()

    private val _printSizes = MutableStateFlow<List<PrintSize>>(PrintConstants.defaultSizes())
    val printSizes = _printSizes.asStateFlow()

    private val _selectedSizeIndex = MutableStateFlow(0)
    val selectedSizeIndex = _selectedSizeIndex.asStateFlow()

    private val _shirtKind = MutableStateFlow("White") // "White", "Black", "Outline"
    val shirtKind = _shirtKind.asStateFlow()

    private val _shirtSize = MutableStateFlow("M")
    val shirtSize = _shirtSize.asStateFlow()

    private val _collarLine = MutableStateFlow(12.5f)
    val collarLine = _collarLine.asStateFlow()

    private val _printPaper = MutableStateFlow(PrintConstants.PA3)
    val printPaper = _printPaper.asStateFlow()

    private val _printDpi = MutableStateFlow("300")
    val printDpi = _printDpi.asStateFlow()

    private val _printCopies = MutableStateFlow(1)
    val printCopies = _printCopies.asStateFlow()

    private val _printMirror = MutableStateFlow(true)
    val printMirror = _printMirror.asStateFlow()

    private val _printStatus = MutableStateFlow("")
    val printStatus = _printStatus.asStateFlow()

    // Batch Processing
    private val _batchUris = MutableStateFlow<List<Uri>>(emptyList())
    val batchUris = _batchUris.asStateFlow()

    private val _batchSettingsChoice = MutableStateFlow("Current settings")
    val batchSettingsChoice = _batchSettingsChoice.asStateFlow()

    private val _batchSuffix = MutableStateFlow("_dtf")
    val batchSuffix = _batchSuffix.asStateFlow()

    private val _batchPolicy = MutableStateFlow("Auto-number")
    val batchPolicy = _batchPolicy.asStateFlow()

    private val _batchRunning = MutableStateFlow(false)
    val batchRunning = _batchRunning.asStateFlow()

    private val _batchProgress = MutableStateFlow(0f)
    val batchProgress = _batchProgress.asStateFlow()

    private val _batchStatus = MutableStateFlow("Add images to begin.")
    val batchStatus = _batchStatus.asStateFlow()

    private val _batchLogs = MutableStateFlow<List<String>>(emptyList())
    val batchLogs = _batchLogs.asStateFlow()

    private var previewJob: Job? = null
    private var batchJob: Job? = null

    private val presetFile = File(context.filesDir, "presets.json")
    private val sizesFile = File(context.filesDir, "print_sizes.json")

    init {
        loadSavedPresets()
        loadSavedSizes()
        loadSampleArt()
    }

    private fun loadSampleArt() {
        // Automatically provide a rich default graphic so user can immediately experiment
        viewModelScope.launch(Dispatchers.IO) {
            val sample = createSampleTshirtGraphic()
            withContext(Dispatchers.Main) {
                loadImageBitmap(sample, "Skull_Roses_Artwork.png")
            }
        }
    }

    private fun createSampleTshirtGraphic(): Bitmap {
        val w = 800
        val h = 800
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        // Background black
        canvas.drawColor(Color.BLACK)

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        // Draw vibrant artwork (vintage skull / rose style for DTF testing)
        // Outer glow
        paint.color = Color.rgb(220, 40, 70)
        canvas.drawCircle(400f, 400f, 260f, paint)

        paint.color = Color.rgb(255, 120, 50)
        canvas.drawCircle(400f, 400f, 210f, paint)

        paint.color = Color.rgb(255, 215, 0)
        canvas.drawCircle(400f, 400f, 160f, paint)

        // Skull silhouette center
        paint.color = Color.WHITE
        canvas.drawCircle(400f, 360f, 100f, paint)
        canvas.drawRect(340f, 400f, 460f, 470f, paint)

        // Eye sockets (black)
        paint.color = Color.BLACK
        canvas.drawCircle(365f, 360f, 24f, paint)
        canvas.drawCircle(435f, 360f, 24f, paint)
        canvas.drawOval(388f, 395f, 412f, 425f, paint) // Nose

        // Teeth lines
        paint.strokeWidth = 6f
        paint.style = android.graphics.Paint.Style.STROKE
        for (i in 360..440 step 20) {
            canvas.drawLine(i.toFloat(), 430f, i.toFloat(), 465f, paint)
        }

        // Ribbon / banner
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = Color.rgb(65, 105, 225)
        canvas.drawRoundRect(200f, 520f, 600f, 600f, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 42f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("DTF HALFTONE", 400f, 575f, paint)

        return bmp
    }

    fun openImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bmp != null) {
                    val name = queryFileName(uri) ?: "Loaded_Image.png"
                    withContext(Dispatchers.Main) {
                        loadImageBitmap(bmp, name)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "Failed to load image: ${e.localizedMessage}"
                }
            }
        }
    }

    fun loadImageBitmap(bitmap: Bitmap, name: String) {
        _sourceBitmap.value = bitmap
        _sourceName.value = "$name · ${bitmap.width} × ${bitmap.height} px"
        _sourceDimensions.value = Pair(bitmap.width, bitmap.height)

        // Create proxy bitmap for real-time live manipulation (max 1920)
        val maxDim = max(bitmap.width, bitmap.height)
        val proxyLimit = 1600
        proxyBitmap = if (maxDim > proxyLimit) {
            val scale = proxyLimit.toFloat() / maxDim
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        _viewMode.value = "Result"
        schedulePreview()
    }

    private fun queryFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { File(it).name }
        }
        return result
    }

    fun updateParams(transform: (DtfParams) -> DtfParams) {
        _params.value = transform(_params.value)
        _presetDirty.value = true
        schedulePreview()
    }

    fun setMode(newMode: String) {
        updateParams { it.copy(mode = newMode) }
        if (newMode == "Halftone") {
            _currentTab.value = "Halftone"
        }
    }

    fun setTarget(target: String) {
        updateParams { it.copy(target = target) }
    }

    fun setKeyColor(color: Int) {
        updateParams { it.copy(keyColor = color, target = "Custom") }
    }

    fun setMethod(method: String) {
        updateParams { it.copy(method = method) }
    }

    fun setShape(shape: String) {
        updateParams { it.copy(shape = shape, mode = "Halftone") }
    }

    fun setInk(ink: String) {
        updateParams { it.copy(ink = ink, mode = "Halftone") }
    }

    fun setViewMode(mode: String) {
        _viewMode.value = mode
    }

    fun setBgMode(mode: String) {
        _bgMode.value = mode
    }

    fun setZoom(percent: Float) {
        _zoomPercent.value = percent
    }

    fun setCurrentTab(tab: String) {
        _currentTab.value = tab
    }

    fun showDialog(dialogName: String?) {
        _activeDialog.value = dialogName
    }

    fun startPickColor() {
        if (proxyBitmap != null) {
            _isPickingColor.value = true
            _viewMode.value = "Original"
            _statusMessage.value = "Tap on the image to pick the background color to remove…"
        }
    }

    fun onImageTapped(xNormalized: Float, yNormalized: Float) {
        if (!_isPickingColor.value) return
        val bmp = proxyBitmap ?: return
        val px = (xNormalized * bmp.width).toInt().coerceIn(0, bmp.width - 1)
        val py = (yNormalized * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        val pixel = bmp.getPixel(px, py)

        _isPickingColor.value = false
        _viewMode.value = "Result"
        setKeyColor(pixel)
        _statusMessage.value = "Custom color picked: #${"%06X".format(pixel and 0xFFFFFF)}"
    }

    fun cancelPickColor() {
        _isPickingColor.value = false
        _viewMode.value = "Result"
    }

    fun reset() {
        _params.value = DtfParams()
        _activePreset.value = "Default"
        _presetDirty.value = false
        _isPickingColor.value = false
        _viewMode.value = "Result"
        _bgMode.value = "Checker"
        _currentTab.value = "Remove"
        schedulePreview()
    }

    fun clear() {
        previewJob?.cancel()
        _sourceBitmap.value = null
        proxyBitmap = null
        _resultBitmap.value = null
        _sourceName.value = "No image loaded"
        _sourceDimensions.value = Pair(0, 0)
        reset()
        _statusMessage.value = "Open an image to begin"
    }

    private fun schedulePreview() {
        previewJob?.cancel()
        val bmp = proxyBitmap ?: return
        _isBusy.value = true
        _statusMessage.value = "Processing…"

        previewJob = viewModelScope.launch(Dispatchers.Default) {
            delay(90) // Debounce rapid slider inputs
            try {
                val p = _params.value
                val (_, targetL) = p.calcOutputSize(bmp.width, bmp.height)
                val previewLimit = p.prevsz.toIntOrNull() ?: 1600
                val effectiveL = targetL.coerceAtMost(previewLimit)
                val maxDim = max(bmp.width, bmp.height)
                val scale = effectiveL.toFloat() / maxDim
                val outW = max(1, (bmp.width * scale).toInt())
                val outH = max(1, (bmp.height * scale).toInt())

                val rendered = DtfEngine.render(
                    src = bmp,
                    outW = outW,
                    outH = outH,
                    k = scale,
                    p = p
                )

                withContext(Dispatchers.Main) {
                    _resultBitmap.value = rendered
                    _isBusy.value = false
                    _statusMessage.value = "Preview ${rendered.width} × ${rendered.height} px · Double-tap to fit, pinch to zoom"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isBusy.value = false
                    _statusMessage.value = "Error: ${e.localizedMessage}"
                }
            }
        }
    }

    // Export transparent PNG
    fun exportTransparentPng(onSuccess: (String) -> Unit) {
        val src = _sourceBitmap.value ?: return
        _isExporting.value = true
        _statusMessage.value = "Rendering full resolution…"

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val p = _params.value
                val (outDims, targetL) = p.calcOutputSize(src.width, src.height)
                val rendered = DtfEngine.render(
                    src = src,
                    outW = outDims.first,
                    outH = outDims.second,
                    k = 1.0f,
                    p = p,
                    fitLong = targetL
                )

                // Save to MediaStore Gallery / Pictures
                val filename = "DTF_${System.currentTimeMillis()}.png"
                val savedUri = saveBitmapToGallery(rendered, filename, "image/png")

                var underbaseMsg = ""
                if (p.ub) {
                    val ub = DtfEngine.makeUnderbase(rendered, p.ubchoke)
                    val ubFilename = "DTF_${System.currentTimeMillis()}_underbase.png"
                    saveBitmapToGallery(ub, ubFilename, "image/png")
                    underbaseMsg = " + Underbase"
                }

                withContext(Dispatchers.Main) {
                    _isExporting.value = false
                    val msg = "Saved $filename (${rendered.width} × ${rendered.height} px)$underbaseMsg"
                    _statusMessage.value = msg
                    onSuccess(msg)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isExporting.value = false
                    _statusMessage.value = "Export failed: ${e.localizedMessage}"
                }
            }
        }
    }

    private fun saveBitmapToGallery(bmp: Bitmap, filename: String, mimeType: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DTF_Studio")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
        return uri
    }

    // Presets
    private fun loadSavedPresets() {
        try {
            if (presetFile.exists()) {
                val json = JSONObject(presetFile.readText())
                val pObj = json.optJSONObject("presets") ?: JSONObject()
                val map = mutableMapOf<String, DtfParams>()
                val keys = pObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = DtfParams.fromJson(pObj.getJSONObject(k).toString())
                }
                _presets.value = map
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writePresetsFile() {
        try {
            val root = JSONObject()
            val pObj = JSONObject()
            _presets.value.forEach { (name, param) ->
                pObj.put(name, JSONObject(param.toJson()))
            }
            root.put("presets", pObj)
            presetFile.writeText(root.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyPreset(name: String) {
        val preset = if (name == "Default") DtfParams() else _presets.value[name] ?: return
        _params.value = preset
        _activePreset.value = name
        _presetDirty.value = false
        _statusMessage.value = "Preset loaded: $name"
        schedulePreview()
    }

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.equals("Default", ignoreCase = true)) return
        val current = _presets.value.toMutableMap()
        current[trimmed] = _params.value
        _presets.value = current
        writePresetsFile()
        _activePreset.value = trimmed
        _presetDirty.value = false
        _statusMessage.value = "Preset saved: $trimmed"
    }

    fun deletePreset(name: String) {
        val current = _presets.value.toMutableMap()
        current.remove(name)
        _presets.value = current
        writePresetsFile()
        if (_activePreset.value == name) {
            _activePreset.value = "Default"
            _presetDirty.value = false
        }
        _statusMessage.value = "Preset deleted: $name"
    }

    fun deleteAllPresets() {
        _presets.value = emptyMap()
        writePresetsFile()
        reset()
        _statusMessage.value = "All presets deleted"
    }

    // Print & Mockup controls
    private fun loadSavedSizes() {
        try {
            if (sizesFile.exists()) {
                val json = JSONObject(sizesFile.readText())
                _printUnit.value = if (json.optString("unit") == "cm") "cm" else "in"
                val arr = json.optJSONArray("sizes") ?: JSONArray()
                val list = mutableListOf<PrintSize>()
                for (i in 0 until arr.length()) {
                    list.add(PrintSize.fromJson(arr.getJSONObject(i)))
                }
                if (list.isNotEmpty()) {
                    _printSizes.value = list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeSizesFile() {
        try {
            val root = JSONObject()
            root.put("unit", _printUnit.value)
            val arr = JSONArray()
            _printSizes.value.forEach { arr.put(it.toJson()) }
            root.put("sizes", arr)
            sizesFile.writeText(root.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPrintUnit(unit: String) {
        _printUnit.value = unit
        writeSizesFile()
    }

    fun selectPrintSize(index: Int) {
        if (index in _printSizes.value.indices) {
            _selectedSizeIndex.value = index
        }
    }

    fun savePrintSize(size: PrintSize) {
        val list = _printSizes.value.toMutableList()
        val existing = list.indexOfFirst { it.name.equals(size.name, ignoreCase = true) }
        if (existing >= 0) {
            list[existing] = size
            _selectedSizeIndex.value = existing
        } else {
            list.add(size)
            _selectedSizeIndex.value = list.size - 1
        }
        _printSizes.value = list
        writeSizesFile()
        _printStatus.value = "Size saved: ${size.name}"
    }

    fun deletePrintSize(index: Int) {
        val list = _printSizes.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _printSizes.value = list
            _selectedSizeIndex.value = (_selectedSizeIndex.value - 1).coerceAtLeast(0)
            writeSizesFile()
            _printStatus.value = "Size deleted"
        }
    }

    fun resetPrintSizes() {
        _printSizes.value = PrintConstants.defaultSizes()
        _selectedSizeIndex.value = 0
        writeSizesFile()
        _printStatus.value = "Size list restored"
    }

    fun setShirtKind(kind: String) { _shirtKind.value = kind }
    fun setShirtSize(size: String) { _shirtSize.value = size }
    fun setCollarLine(line: Float) { _collarLine.value = line }
    fun setPrintPaper(paper: String) { _printPaper.value = paper }
    fun setPrintDpi(dpi: String) { _printDpi.value = dpi }
    fun setPrintCopies(copies: Int) { _printCopies.value = copies }
    fun setPrintMirror(mirror: Boolean) { _printMirror.value = mirror }

    // Save Print PDF or PNG
    fun savePrintSheet(onSuccess: (String) -> Unit) {
        val src = _sourceBitmap.value ?: return
        val currentSize = _printSizes.value.getOrNull(_selectedSizeIndex.value) ?: return
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val dpi = _printDpi.value.toIntOrNull() ?: 300
                val paperSpec = PrintConstants.PAPERS[_printPaper.value]
                val mirror = _printMirror.value

                // Render for print box (inches * dpi)
                val boxW = (currentSize.w * dpi).toInt()
                val boxH = (currentSize.h * dpi).toInt()
                val design = DtfEngine.render(
                    src = src,
                    outW = boxW,
                    outH = boxH,
                    k = 1.0f,
                    p = _params.value.copy(mirror = mirror)
                )

                // Build paper sheet
                val margin = (0.25f * dpi).toInt()
                val pw: Int
                val ph: Int
                if (paperSpec?.widthIn != null && paperSpec.heightIn != null) {
                    var wP = (paperSpec.widthIn * dpi).toInt()
                    var hP = (paperSpec.heightIn * dpi).toInt()
                    if (design.width > wP - 2 * margin && design.width <= hP - 2 * margin) {
                        val temp = wP; wP = hP; hP = temp // landscape
                    }
                    pw = wP
                    ph = hP
                } else {
                    pw = design.width + 2 * margin
                    ph = design.height + 2 * margin
                }

                val sheet = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(sheet)
                canvas.drawColor(Color.WHITE)
                val posX = (pw - design.width) / 2f
                val posY = margin.toFloat()
                canvas.drawBitmap(design, posX, posY, null)

                val filename = "Print_${currentSize.name.replace(" ", "_")}_${System.currentTimeMillis()}.png"
                saveBitmapToGallery(sheet, filename, "image/png")

                withContext(Dispatchers.Main) {
                    val msg = "Saved $filename ($pw × $ph px @ $dpi DPI)"
                    _printStatus.value = msg
                    onSuccess(msg)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _printStatus.value = "Print generation failed: ${e.localizedMessage}"
                }
            }
        }
    }

    // Batch processing
    fun addBatchUris(uris: List<Uri>) {
        val current = _batchUris.value.toMutableList()
        uris.forEach { if (!current.contains(it)) current.add(it) }
        _batchUris.value = current
        _batchStatus.value = "${current.size} images ready."
    }

    fun removeBatchUri(index: Int) {
        val current = _batchUris.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _batchUris.value = current
            _batchStatus.value = "${current.size} images left."
        }
    }

    fun clearBatchList() {
        _batchUris.value = emptyList()
        _batchStatus.value = "0 images"
    }

    fun setBatchSettings(choice: String) { _batchSettingsChoice.value = choice }
    fun setBatchSuffix(suffix: String) { _batchSuffix.value = suffix }
    fun setBatchPolicy(policy: String) { _batchPolicy.value = policy }

    fun runBatch() {
        val uris = _batchUris.value
        if (uris.isEmpty()) {
            _batchStatus.value = "Add images first."
            return
        }
        _batchRunning.value = true
        _batchProgress.value = 0f
        _batchLogs.value = emptyList()

        val p = when (_batchSettingsChoice.value) {
            "Current settings" -> _params.value
            "Default" -> DtfParams()
            else -> _presets.value[_batchSettingsChoice.value] ?: _params.value
        }

        batchJob = viewModelScope.launch(Dispatchers.Default) {
            var ok = 0
            var fail = 0
            val total = uris.size

            for (i in uris.indices) {
                if (!batchJob!!.isActive) break
                val uri = uris[i]
                val name = queryFileName(uri) ?: "Image_${i + 1}"
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bmp = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bmp != null) {
                        val (outDims, targetL) = p.calcOutputSize(bmp.width, bmp.height)
                        val rendered = DtfEngine.render(
                            src = bmp,
                            outW = outDims.first,
                            outH = outDims.second,
                            k = 1.0f,
                            p = p,
                            fitLong = targetL
                        )

                        val stem = name.substringBeforeLast(".")
                        val outName = "${stem}${_batchSuffix.value}.png"
                        saveBitmapToGallery(rendered, outName, "image/png")

                        if (p.ub) {
                            val ub = DtfEngine.makeUnderbase(rendered, p.ubchoke)
                            saveBitmapToGallery(ub, "${stem}${_batchSuffix.value}_underbase.png", "image/png")
                        }

                        ok++
                        addBatchLog("OK     $name → $outName (${rendered.width} × ${rendered.height})")
                    } else {
                        fail++
                        addBatchLog("FAIL   $name (Failed to decode image)")
                    }
                } catch (e: Exception) {
                    fail++
                    addBatchLog("FAIL   $name (${e.localizedMessage})")
                }

                _batchProgress.value = (i + 1).toFloat() / total
                _batchStatus.value = "${i + 1}/$total · $name"
            }

            withContext(Dispatchers.Main) {
                _batchRunning.value = false
                _batchStatus.value = "Finished: $ok saved, $fail failed."
            }
        }
    }

    private suspend fun addBatchLog(log: String) {
        withContext(Dispatchers.Main) {
            _batchLogs.value = _batchLogs.value + log
        }
    }

    fun stopBatch() {
        batchJob?.cancel()
        _batchRunning.value = false
        _batchStatus.value = "Batch processing stopped."
    }
}
