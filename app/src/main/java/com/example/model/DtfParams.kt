package com.example.model

import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class DtfParams(
    // Mode
    val mode: String = "Remove Background", // "Remove Background", "Halftone"

    // Removal
    val target: String = "Black shirt", // "Black shirt", "White shirt", "Custom"
    val keyColor: Int = 0xFF00FF00.toInt(), // 0x00FF00 green
    val method: String = "Soft blend", // "Soft blend", "Solid cut"
    val tol: Float = 6f, // 0..60%
    val soft: Float = 12f, // 0..50%
    val density: Float = 100f, // 40..220%
    val unmix: Boolean = true,
    val cut: Float = 3f, // 0..50%
    val snap: Float = 100f, // 50..100%
    val choke: Int = 0, // -10..10 px
    val binary: Boolean = false,

    // Adjustments
    val bright: Float = 100f, // 50..150%
    val contrast: Float = 100f, // 50..200%
    val sat: Float = 100f, // 0..200%
    val gamma: Float = 1.0f, // 0.5..2.0
    val sharp: Float = 0f, // 0..300%
    val gray: Boolean = false,
    val invert: Boolean = false,

    // Halftone
    val shape: String = "Dot", // "Dot", "Diamond", "Square", "Line"
    val dot: Float = 10f, // 3..40 px
    val angle: Float = 45f, // 0..90 deg
    val gain: Float = 100f, // 50..200%
    val mindot: Float = 3f, // 0..20%
    val ink: String = "Original", // "Original", "White", "Black"
    val flat: Boolean = true,

    // Output
    val size: String = "4K · 3840 px", // "Original", "2K · 2048 px", "4K · 3840 px", "8K · 7680 px", "Custom"
    val custom: String = "4096",
    val dpi: String = "300", // "150", "300", "600"
    val mirror: Boolean = false,
    val trim: Boolean = false,
    val ub: Boolean = false,
    val ubchoke: Int = 2,
    val prevsz: String = "1920"
) {
    companion object {
        val SIZES = mapOf(
            "Original" to null,
            "2K · 2048 px" to 2048,
            "4K · 3840 px" to 3840,
            "8K · 7680 px" to 7680,
            "Custom" to -1
        )

        fun fromJson(jsonStr: String): DtfParams {
            return try {
                val obj = JSONObject(jsonStr)
                var p = DtfParams()
                if (obj.has("mode")) p = p.copy(mode = obj.getString("mode"))
                if (obj.has("target")) p = p.copy(target = obj.getString("target"))
                if (obj.has("keyColor")) p = p.copy(keyColor = obj.getInt("keyColor"))
                if (obj.has("method")) p = p.copy(method = obj.getString("method"))
                if (obj.has("tol")) p = p.copy(tol = obj.getDouble("tol").toFloat())
                if (obj.has("soft")) p = p.copy(soft = obj.getDouble("soft").toFloat())
                if (obj.has("density")) p = p.copy(density = obj.getDouble("density").toFloat())
                if (obj.has("unmix")) p = p.copy(unmix = obj.getBoolean("unmix"))
                if (obj.has("cut")) p = p.copy(cut = obj.getDouble("cut").toFloat())
                if (obj.has("snap")) p = p.copy(snap = obj.getDouble("snap").toFloat())
                if (obj.has("choke")) p = p.copy(choke = obj.getInt("choke"))
                if (obj.has("binary")) p = p.copy(binary = obj.getBoolean("binary"))

                if (obj.has("bright")) p = p.copy(bright = obj.getDouble("bright").toFloat())
                if (obj.has("contrast")) p = p.copy(contrast = obj.getDouble("contrast").toFloat())
                if (obj.has("sat")) p = p.copy(sat = obj.getDouble("sat").toFloat())
                if (obj.has("gamma")) p = p.copy(gamma = obj.getDouble("gamma").toFloat())
                if (obj.has("sharp")) p = p.copy(sharp = obj.getDouble("sharp").toFloat())
                if (obj.has("gray")) p = p.copy(gray = obj.getBoolean("gray"))
                if (obj.has("invert")) p = p.copy(invert = obj.getBoolean("invert"))

                if (obj.has("shape")) p = p.copy(shape = obj.getString("shape"))
                if (obj.has("dot")) p = p.copy(dot = obj.getDouble("dot").toFloat())
                if (obj.has("angle")) p = p.copy(angle = obj.getDouble("angle").toFloat())
                if (obj.has("gain")) p = p.copy(gain = obj.getDouble("gain").toFloat())
                if (obj.has("mindot")) p = p.copy(mindot = obj.getDouble("mindot").toFloat())
                if (obj.has("ink")) p = p.copy(ink = obj.getString("ink"))
                if (obj.has("flat")) p = p.copy(flat = obj.getBoolean("flat"))

                if (obj.has("size")) p = p.copy(size = obj.getString("size"))
                if (obj.has("custom")) p = p.copy(custom = obj.getString("custom"))
                if (obj.has("dpi")) p = p.copy(dpi = obj.getString("dpi"))
                if (obj.has("mirror")) p = p.copy(mirror = obj.getBoolean("mirror"))
                if (obj.has("trim")) p = p.copy(trim = obj.getBoolean("trim"))
                if (obj.has("ub")) p = p.copy(ub = obj.getBoolean("ub"))
                if (obj.has("ubchoke")) p = p.copy(ubchoke = obj.getInt("ubchoke"))
                p
            } catch (e: Exception) {
                DtfParams()
            }
        }
    }

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("mode", mode)
        obj.put("target", target)
        obj.put("keyColor", keyColor)
        obj.put("method", method)
        obj.put("tol", tol.toDouble())
        obj.put("soft", soft.toDouble())
        obj.put("density", density.toDouble())
        obj.put("unmix", unmix)
        obj.put("cut", cut.toDouble())
        obj.put("snap", snap.toDouble())
        obj.put("choke", choke)
        obj.put("binary", binary)

        obj.put("bright", bright.toDouble())
        obj.put("contrast", contrast.toDouble())
        obj.put("sat", sat.toDouble())
        obj.put("gamma", gamma.toDouble())
        obj.put("sharp", sharp.toDouble())
        obj.put("gray", gray)
        obj.put("invert", invert)

        obj.put("shape", shape)
        obj.put("dot", dot.toDouble())
        obj.put("angle", angle.toDouble())
        obj.put("gain", gain.toDouble())
        obj.put("mindot", mindot.toDouble())
        obj.put("ink", ink)
        obj.put("flat", flat)

        obj.put("size", size)
        obj.put("custom", custom)
        obj.put("dpi", dpi)
        obj.put("mirror", mirror)
        obj.put("trim", trim)
        obj.put("ub", ub)
        obj.put("ubchoke", ubchoke)
        return obj.toString()
    }

    fun calcOutputSize(srcW: Int, srcH: Int): Pair<Pair<Int, Int>, Int> {
        val maxDim = max(srcW, srcH)
        var targetL: Int? = SIZES[size]
        if (targetL == -1) {
            targetL = try {
                custom.toFloat().toInt()
            } catch (e: Exception) {
                maxDim
            }
        }
        val L = min(max(targetL ?: maxDim, 64), 16000)
        val ratio = L.toFloat() / maxDim
        val outW = max(1, (srcW * ratio).roundToInt())
        val outH = max(1, (srcH * ratio).roundToInt())
        return Pair(Pair(outW, outH), L)
    }
}
