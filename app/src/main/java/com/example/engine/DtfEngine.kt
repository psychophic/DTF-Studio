package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.model.DtfParams
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object DtfEngine {

    private fun smoothstep(x: Float): Float {
        val cx = x.coerceIn(0f, 1f)
        return cx * cx * (3.0f - 2.0f * cx)
    }

    private fun boxBlur(data: FloatArray, w: Int, h: Int, radius: Int): FloatArray {
        if (radius <= 0) return data.clone()
        val r = radius.coerceAtMost(max(w, h))
        val temp = FloatArray(w * h)
        val out = FloatArray(w * h)

        // Horizontal pass
        for (y in 0 until h) {
            val yOffset = y * w
            var sum = 0f
            val count = (2 * r + 1).toFloat()
            for (i in -r..r) {
                sum += data[yOffset + i.coerceIn(0, w - 1)]
            }
            for (x in 0 until w) {
                temp[yOffset + x] = sum / count
                val removeIdx = (x - r).coerceIn(0, w - 1)
                val addIdx = (x + r + 1).coerceIn(0, w - 1)
                sum += data[yOffset + addIdx] - data[yOffset + removeIdx]
            }
        }

        // Vertical pass
        for (x in 0 until w) {
            var sum = 0f
            val count = (2 * r + 1).toFloat()
            for (i in -r..r) {
                sum += temp[i.coerceIn(0, h - 1) * w + x]
            }
            for (y in 0 until h) {
                out[y * w + x] = sum / count
                val removeIdx = (y - r).coerceIn(0, h - 1)
                val addIdx = (y + r + 1).coerceIn(0, h - 1)
                sum += temp[addIdx * w + x] - temp[removeIdx * w + x]
            }
        }
        return out
    }

    /**
     * Halftone rendering matching Python halftone()
     */
    fun halftone(
        dens: FloatArray,
        rgbR: FloatArray,
        rgbG: FloatArray,
        rgbB: FloatArray,
        w: Int,
        h: Int,
        cell: Float,
        angleDeg: Float,
        shape: String,
        gain: Float,
        minDot: Float,
        flatColor: Boolean
    ): Pair<FloatArray, Triple<FloatArray, FloatArray, FloatArray>?> {
        val blurRadius = max(1, (cell * 0.5f).roundToInt())
        val dsm = boxBlur(dens, w, h, blurRadius)

        var rsmR: FloatArray? = null
        var rsmG: FloatArray? = null
        var rsmB: FloatArray? = null

        if (flatColor) {
            val wR = FloatArray(w * h) { i -> rgbR[i] * dens[i] }
            val wG = FloatArray(w * h) { i -> rgbG[i] * dens[i] }
            val wB = FloatArray(w * h) { i -> rgbB[i] * dens[i] }
            val bR = boxBlur(wR, w, h, blurRadius)
            val bG = boxBlur(wG, w, h, blurRadius)
            val bB = boxBlur(wB, w, h, blurRadius)

            rsmR = FloatArray(w * h) { i -> (bR[i] / max(dsm[i], 0.001f)).coerceIn(0f, 1f) }
            rsmG = FloatArray(w * h) { i -> (bG[i] / max(dsm[i], 0.001f)).coerceIn(0f, 1f) }
            rsmB = FloatArray(w * h) { i -> (bB[i] / max(dsm[i], 0.001f)).coerceIn(0f, 1f) }
        }

        val th = Math.toRadians(angleDeg.toDouble())
        val c = cos(th).toFloat()
        val s = sin(th).toFloat()

        val outA = FloatArray(w * h)
        val outR = if (flatColor) FloatArray(w * h) else null
        val outG = if (flatColor) FloatArray(w * h) else null
        val outB = if (flatColor) FloatArray(w * h) else null

        val gainInv = 1.0 / max(gain.toDouble(), 0.05)
        val pi = PI.toFloat()

        for (y in 0 until h) {
            val yOffset = y * w
            val yF = y.toFloat()
            for (x in 0 until w) {
                val xF = x.toFloat()
                val u = (xF * c + yF * s) / cell
                val v = (-xF * s + yF * c) / cell
                val iu = floor(u).toInt()
                val iv = floor(v).toInt()
                val du = u - iu - 0.5f
                val dv = v - iv - 0.5f

                val cx = ((iu + 0.5f) * c - (iv + 0.5f) * s) * cell
                val cy = ((iu + 0.5f) * s + (iv + 0.5f) * c) * cell
                val ix = cx.roundToInt().coerceIn(0, w - 1)
                val iy = cy.roundToInt().coerceIn(0, h - 1)
                val centerIdx = iy * w + ix

                var t = dsm[centerIdx].coerceIn(0f, 1f)
                t = t.toDouble().pow(gainInv).toFloat()

                val r: Float
                val maxR: Float
                when (shape) {
                    "Diamond" -> {
                        r = abs(du) + abs(dv)
                        maxR = if (t <= 0.5f) sqrt(t / 2.0f) else 0.5f + (t - 0.5f)
                    }
                    "Square" -> {
                        r = max(abs(du), abs(dv))
                        maxR = 0.5f * sqrt(t)
                    }
                    "Line" -> {
                        r = abs(dv)
                        maxR = 0.5f * t
                    }
                    else -> { // "Dot", "Circle"
                        r = hypot(du, dv)
                        maxR = if (t <= pi / 4f) {
                            sqrt(t / pi)
                        } else {
                            0.5f + (t - pi / 4f) / (1f - pi / 4f) * 0.2071f
                        }
                    }
                }

                var cov = ((maxR - r) * cell + 0.5f).coerceIn(0f, 1f)
                if (minDot > 0f && t < minDot) {
                    cov = 0f
                }

                val idx = yOffset + x
                outA[idx] = cov
                if (flatColor && outR != null && outG != null && outB != null && rsmR != null && rsmG != null && rsmB != null) {
                    outR[idx] = rsmR[centerIdx]
                    outG[idx] = rsmG[centerIdx]
                    outB[idx] = rsmB[centerIdx]
                }
            }
        }

        val outColors = if (flatColor && outR != null && outG != null && outB != null) {
            Triple(outR, outG, outB)
        } else null

        return Pair(outA, outColors)
    }

    /**
     * Render full effect pipeline matching Python render()
     */
    fun render(
        src: Bitmap,
        outW: Int,
        outH: Int,
        k: Float,
        p: DtfParams,
        fitLong: Int? = null
    ): Bitmap {
        val scaledSrc = if (src.width != outW || src.height != outH) {
            Bitmap.createScaledBitmap(src, outW, outH, true)
        } else {
            src
        }

        val w = scaledSrc.width
        val h = scaledSrc.height
        val total = w * h
        val pixels = IntArray(total)
        scaledSrc.getPixels(pixels, 0, w, 0, 0, w, h)

        val srcAlpha = FloatArray(total)
        val arrR = FloatArray(total)
        val arrG = FloatArray(total)
        val arrB = FloatArray(total)

        var hasTransparency = false
        for (i in 0 until total) {
            val c = pixels[i]
            val a = Color.alpha(c)
            if (a < 255) hasTransparency = true
            srcAlpha[i] = a / 255f
            arrR[i] = Color.red(c) / 255f
            arrG[i] = Color.green(c) / 255f
            arrB[i] = Color.blue(c) / 255f
        }

        // --- Tone / Pre-adjustments ---
        if (p.invert) {
            for (i in 0 until total) {
                arrR[i] = 1f - arrR[i]
                arrG[i] = 1f - arrG[i]
                arrB[i] = 1f - arrB[i]
            }
        }

        if (p.gray) {
            for (i in 0 until total) {
                val lum = (0.299f * arrR[i] + 0.587f * arrG[i] + 0.114f * arrB[i]).coerceIn(0f, 1f)
                arrR[i] = lum
                arrG[i] = lum
                arrB[i] = lum
            }
        }

        if (abs(p.bright - 100f) > 0.5f) {
            val factor = p.bright / 100f
            for (i in 0 until total) {
                arrR[i] = (arrR[i] * factor).coerceIn(0f, 1f)
                arrG[i] = (arrG[i] * factor).coerceIn(0f, 1f)
                arrB[i] = (arrB[i] * factor).coerceIn(0f, 1f)
            }
        }

        if (abs(p.contrast - 100f) > 0.5f) {
            val factor = p.contrast / 100f
            for (i in 0 until total) {
                arrR[i] = ((arrR[i] - 0.5f) * factor + 0.5f).coerceIn(0f, 1f)
                arrG[i] = ((arrG[i] - 0.5f) * factor + 0.5f).coerceIn(0f, 1f)
                arrB[i] = ((arrB[i] - 0.5f) * factor + 0.5f).coerceIn(0f, 1f)
            }
        }

        if (abs(p.sat - 100f) > 0.5f) {
            val factor = p.sat / 100f
            for (i in 0 until total) {
                val lum = 0.299f * arrR[i] + 0.587f * arrG[i] + 0.114f * arrB[i]
                arrR[i] = (lum + (arrR[i] - lum) * factor).coerceIn(0f, 1f)
                arrG[i] = (lum + (arrG[i] - lum) * factor).coerceIn(0f, 1f)
                arrB[i] = (lum + (arrB[i] - lum) * factor).coerceIn(0f, 1f)
            }
        }

        if (abs(p.gamma - 1.0f) > 0.001f) {
            val invGamma = 1.0 / p.gamma
            for (i in 0 until total) {
                arrR[i] = arrR[i].toDouble().pow(invGamma).toFloat().coerceIn(0f, 1f)
                arrG[i] = arrG[i].toDouble().pow(invGamma).toFloat().coerceIn(0f, 1f)
                arrB[i] = arrB[i].toDouble().pow(invGamma).toFloat().coerceIn(0f, 1f)
            }
        }

        // Sharpen unsharp mask
        if (p.sharp > 0f) {
            val factor = (p.sharp / 100f) * 0.4f
            val blurredR = boxBlur(arrR, w, h, 1)
            val blurredG = boxBlur(arrG, w, h, 1)
            val blurredB = boxBlur(arrB, w, h, 1)
            for (i in 0 until total) {
                arrR[i] = (arrR[i] + (arrR[i] - blurredR[i]) * factor).coerceIn(0f, 1f)
                arrG[i] = (arrG[i] + (arrG[i] - blurredG[i]) * factor).coerceIn(0f, 1f)
                arrB[i] = (arrB[i] + (arrB[i] - blurredB[i]) * factor).coerceIn(0f, 1f)
            }
        }

        // --- Key color removal ---
        val keyR: Float
        val keyG: Float
        val keyB: Float
        when (p.target) {
            "Black shirt" -> {
                keyR = 0f; keyG = 0f; keyB = 0f
            }
            "White shirt" -> {
                keyR = 1f; keyG = 1f; keyB = 1f
            }
            else -> {
                keyR = Color.red(p.keyColor) / 255f
                keyG = Color.green(p.keyColor) / 255f
                keyB = Color.blue(p.keyColor) / 255f
            }
        }

        val tol = p.tol / 100f
        val tolMax = max(1f - tol, 0.001f)
        val densityInv = 1.0 / max(p.density / 100.0, 0.05)
        val softVal = max(p.soft / 100f, 0.005f)

        var alpha = FloatArray(total)
        val colR = FloatArray(total)
        val colG = FloatArray(total)
        val colB = FloatArray(total)

        for (i in 0 until total) {
            val d = max(abs(arrR[i] - keyR), max(abs(arrG[i] - keyG), abs(arrB[i] - keyB)))
            val ramp = ((d - tol) / tolMax).coerceIn(0f, 1f)
            val dens = ramp.toDouble().pow(densityInv).toFloat()

            alpha[i] = if (p.method == "Soft blend") {
                dens
            } else {
                smoothstep((d - tol) / softVal)
            }

            if (p.unmix) {
                val denom = max(d, 0.001f)
                colR[i] = (keyR + (arrR[i] - keyR) / denom).coerceIn(0f, 1f)
                colG[i] = (keyG + (arrG[i] - keyG) / denom).coerceIn(0f, 1f)
                colB[i] = (keyB + (arrB[i] - keyB) / denom).coerceIn(0f, 1f)
            } else {
                colR[i] = arrR[i]
                colG[i] = arrG[i]
                colB[i] = arrB[i]
            }

            if (p.ink == "White") {
                colR[i] = 1f; colG[i] = 1f; colB[i] = 1f
            } else if (p.ink == "Black") {
                colR[i] = 0f; colG[i] = 0f; colB[i] = 0f
            }
        }

        // --- Halftone ---
        if (p.mode == "Halftone") {
            val hdens = FloatArray(total)
            val keyLum = (0.299f * keyR + 0.587f * keyG + 0.114f * keyB)
            for (i in 0 until total) {
                val lum = 0.299f * arrR[i] + 0.587f * arrG[i] + 0.114f * arrB[i]
                val base = when (p.target) {
                    "Black shirt" -> lum
                    "White shirt" -> 1f - lum
                    else -> abs(lum - keyLum) / max(keyLum, max(1f - keyLum, 0.001f))
                }
                val hr = ((base - tol) / tolMax).coerceIn(0f, 1f)
                hdens[i] = hr.toDouble().pow(densityInv).toFloat()
            }

            val cell = max(2.0f, p.dot * k)
            val (htA, htColors) = halftone(
                hdens,
                colR,
                colG,
                colB,
                w,
                h,
                cell,
                p.angle,
                p.shape,
                p.gain / 100f,
                p.mindot / 100f,
                p.flat
            )
            alpha = htA
            if (htColors != null) {
                System.arraycopy(htColors.first, 0, colR, 0, total)
                System.arraycopy(htColors.second, 0, colG, 0, total)
                System.arraycopy(htColors.third, 0, colB, 0, total)
            }
        }

        // --- Alpha clean-up ---
        val cut = p.cut / 100f
        val snap = p.snap / 100f
        for (i in 0 until total) {
            var a = alpha[i]
            if (cut > 0f && a < cut) a = 0f
            if (snap < 1f && a >= snap) a = 1f
            if (p.binary) a = if (a >= 0.5f) 1f else 0f
            if (hasTransparency) {
                a *= srcAlpha[i]
            }
            alpha[i] = a
        }

        // Choke / Spread 3x3 min/max filter
        val chokeN = (abs(p.choke) * k).roundToInt().coerceIn(0, 20)
        if (p.choke != 0 && chokeN > 0) {
            val isSpread = p.choke > 0
            for (step in 0 until chokeN) {
                val newA = FloatArray(total)
                for (y in 0 until h) {
                    val yOff = y * w
                    for (x in 0 until w) {
                        var target = alpha[yOff + x]
                        for (dy in -1..1) {
                            val ny = (y + dy).coerceIn(0, h - 1)
                            for (dx in -1..1) {
                                val nx = (x + dx).coerceIn(0, w - 1)
                                val v = alpha[ny * w + nx]
                                target = if (isSpread) max(target, v) else min(target, v)
                            }
                        }
                        newA[yOff + x] = target
                    }
                }
                alpha = newA
            }
        }

        // Build output Bitmap
        val outPixels = IntArray(total)
        for (i in 0 until total) {
            val aInt = (alpha[i] * 255f + 0.5f).toInt().coerceIn(0, 255)
            val rInt = (colR[i] * 255f + 0.5f).toInt().coerceIn(0, 255)
            val gInt = (colG[i] * 255f + 0.5f).toInt().coerceIn(0, 255)
            val bInt = (colB[i] * 255f + 0.5f).toInt().coerceIn(0, 255)
            outPixels[i] = Color.argb(aInt, rInt, gInt, bInt)
        }

        var result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(outPixels, 0, w, 0, 0, w, h)

        // Mirror
        if (p.mirror) {
            val matrix = android.graphics.Matrix().apply { preScale(-1f, 1f) }
            val mirrored = Bitmap.createBitmap(result, 0, 0, w, h, matrix, false)
            if (mirrored != result) {
                result.recycle()
                result = mirrored
            }
        }

        // Trim
        if (p.trim) {
            val bbox = findAlphaBoundingBox(result)
            if (bbox != null) {
                val cropped = Bitmap.createBitmap(result, bbox.left, bbox.top, bbox.width(), bbox.height())
                if (cropped != result) {
                    result.recycle()
                    result = cropped
                }
            }
        }

        // Fit long side if requested
        if (fitLong != null && fitLong > 0) {
            val curW = result.width
            val curH = result.height
            val curMax = max(curW, curH)
            if (curMax != fitLong) {
                val nW: Int
                val nH: Int
                if (curW >= curH) {
                    nW = fitLong
                    nH = max(1, (curH.toFloat() * fitLong / curW).roundToInt())
                } else {
                    nH = fitLong
                    nW = max(1, (curW.toFloat() * fitLong / curH).roundToInt())
                }
                val resized = Bitmap.createScaledBitmap(result, nW, nH, true)
                if (resized != result) {
                    result.recycle()
                    result = resized
                }
            }
        }

        return result
    }

    private fun findAlphaBoundingBox(bmp: Bitmap): Rect? {
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1

        for (y in 0 until h) {
            val yOff = y * w
            for (x in 0 until w) {
                if (Color.alpha(pixels[yOff + x]) > 0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        return if (maxX >= minX && maxY >= minY) {
            Rect(minX, minY, maxX + 1, maxY + 1)
        } else null
    }

    /**
     * Composite image with background: Checker, Black, White, Gray
     */
    fun composite(src: Bitmap, bgType: String): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        when (bgType) {
            "Black" -> canvas.drawColor(Color.BLACK)
            "White" -> canvas.drawColor(Color.WHITE)
            "Gray" -> canvas.drawColor(Color.rgb(128, 128, 128))
            else -> { // "Checker"
                val s = max(12, max(w, h) / 70)
                val paintDark = Paint().apply { color = Color.rgb(38, 38, 44) }
                val paintLight = Paint().apply { color = Color.rgb(65, 65, 75) }
                for (y in 0 until h step s) {
                    for (x in 0 until w step s) {
                        val isEven = ((x / s) + (y / s)) % 2 == 0
                        canvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            min(w, x + s).toFloat(),
                            min(h, y + s).toFloat(),
                            if (isEven) paintDark else paintLight
                        )
                    }
                }
            }
        }
        canvas.drawBitmap(src, 0f, 0f, null)
        return out
    }

    /**
     * Generate White Underbase PNG
     */
    fun makeUnderbase(src: Bitmap, choke: Int): Bitmap {
        val w = src.width
        val h = src.height
        val total = w * h
        val pixels = IntArray(total)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        var alpha = IntArray(total) { i ->
            if (Color.alpha(pixels[i]) >= 10) 255 else 0
        }

        // MinFilter 3x3 repeated choke times
        for (step in 0 until choke.coerceIn(0, 10)) {
            val newA = IntArray(total)
            for (y in 0 until h) {
                val yOff = y * w
                for (x in 0 until w) {
                    var minVal = alpha[yOff + x]
                    for (dy in -1..1) {
                        val ny = (y + dy).coerceIn(0, h - 1)
                        for (dx in -1..1) {
                            val nx = (x + dx).coerceIn(0, w - 1)
                            minVal = min(minVal, alpha[ny * w + nx])
                        }
                    }
                    newA[yOff + x] = minVal
                }
            }
            alpha = newA
        }

        val outPixels = IntArray(total) { i ->
            Color.argb(alpha[i], 255, 255, 255)
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, w, 0, 0, w, h)
        return out
    }

    fun fitBox(w: Int, h: Int, boxW: Int, boxH: Int): Pair<Int, Int> {
        val s = min(boxW.toFloat() / w, boxH.toFloat() / h)
        return Pair(max(1, (w * s).roundToInt()), max(1, (h * s).roundToInt()))
    }
}
