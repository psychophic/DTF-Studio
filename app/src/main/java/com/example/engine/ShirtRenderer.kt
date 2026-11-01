package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

object ShirtRenderer {

    data class ShirtStyle(
        val fill: Int,
        val line: Int,
        val rib: Int
    )

    private val STYLES = mapOf(
        "white" to ShirtStyle(
            Color.argb(255, 244, 244, 247),
            Color.argb(255, 176, 176, 186),
            Color.argb(255, 205, 205, 214)
        ),
        "black" to ShirtStyle(
            Color.argb(255, 24, 24, 27),
            Color.argb(255, 78, 78, 88),
            Color.argb(255, 52, 52, 60)
        ),
        "outline" to ShirtStyle(
            Color.TRANSPARENT,
            Color.argb(255, 214, 214, 226),
            Color.argb(255, 160, 160, 172)
        )
    )

    fun makeShirt(kind: String, back: Boolean, targetW: Int, targetH: Int): Bitmap {
        val shirtStyle = STYLES[kind.lowercase()] ?: STYLES["white"]!!
        val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val u = targetW / 100.0f
        val depth = if (back) 4.5f else 13.0f

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = shirtStyle.fill
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = shirtStyle.line
            strokeWidth = if (kind.lowercase() == "outline") 9f * (targetW / 1040f) else 6f * (targetW / 1040f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val ribPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = shirtStyle.rib
            strokeWidth = 14f * (targetW / 1040f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val seamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = shirtStyle.rib
            strokeWidth = 5f * (targetW / 1040f)
            strokeJoin = Paint.Join.ROUND
        }

        // T-shirt polygon path
        val poly = Path().apply {
            moveTo(36f * u, 1.5f * u)
            lineTo(15f * u, 8f * u)
            lineTo(1f * u, 27f * u)
            lineTo(14f * u, 34f * u)
            lineTo(14f * u, 104f * u)
            lineTo(86f * u, 104f * u)
            lineTo(86f * u, 34f * u)
            lineTo(99f * u, 27f * u)
            lineTo(85f * u, 8f * u)
            lineTo(64f * u, 1.5f * u)
            // Neckline quadratic bezier back to (36, 1.5)
            quadTo(50f * u, (2f * depth - 1.5f) * u, 36f * u, 1.5f * u)
            close()
        }

        if (shirtStyle.fill != Color.TRANSPARENT) {
            canvas.drawPath(poly, fillPaint)
        }
        canvas.drawPath(poly, linePaint)

        // Draw neck rib
        val neckPath = Path().apply {
            moveTo(64f * u, 1.5f * u)
            quadTo(50f * u, (2f * depth - 1.5f) * u, 36f * u, 1.5f * u)
        }
        canvas.drawPath(neckPath, ribPaint)

        // Draw sleeve seams
        canvas.drawLine(15f * u, 8f * u, 14f * u, 34f * u, seamPaint)
        canvas.drawLine(85f * u, 8f * u, 86f * u, 34f * u, seamPaint)

        return bmp
    }
}
