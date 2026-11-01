package com.example.model

import org.json.JSONArray
import org.json.JSONObject

data class PaperSpec(
    val name: String,
    val widthIn: Float?, // null for exact
    val heightIn: Float?,
    val isRoll: Boolean = false,
    val rollWidthIn: Float = 0f
)

data class PrintSize(
    val name: String,
    val w: Float,
    val h: Float,
    val view: String = "Front", // "Front", "Back"
    val place: String = "Center", // "Center", "Left chest", "Right chest", "Left sleeve", "Right sleeve"
    val top: Float = 3.0f,
    val side: Float = 3.0f,
    val paper: String = "A3 (11.69 × 16.54 in)"
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("name", name)
        o.put("w", w.toDouble())
        o.put("h", h.toDouble())
        o.put("view", view)
        o.put("place", place)
        o.put("top", top.toDouble())
        o.put("side", side.toDouble())
        o.put("paper", paper)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): PrintSize {
            return PrintSize(
                name = o.optString("name", "Custom"),
                w = o.optDouble("w", 8.0).toFloat(),
                h = o.optDouble("h", 8.0).toFloat(),
                view = o.optString("view", "Front"),
                place = o.optString("place", "Center"),
                top = o.optDouble("top", 3.0).toFloat(),
                side = o.optDouble("side", 3.0).toFloat(),
                paper = o.optString("paper", "A3 (11.69 × 16.54 in)")
            )
        }
    }
}

object PrintConstants {
    val SHIRT_BODY_IN = linkedMapOf(
        "Kids M" to 14f,
        "Youth M" to 16f,
        "S" to 18f,
        "M" to 20f,
        "L" to 22f,
        "XL" to 24f,
        "2XL" to 26f,
        "3XL" to 28f
    )

    const val PA4 = "A4 (8.27 × 11.69 in)"
    const val PA3 = "A3 (11.69 × 16.54 in)"
    const val PA3P = "A3+ / Super A3 (13 × 19 in)"

    val PAPERS = linkedMapOf(
        "Exact design size" to PaperSpec("Exact design size", null, null),
        PA4 to PaperSpec(PA4, 8.27f, 11.69f),
        PA3 to PaperSpec(PA3, 11.69f, 16.54f),
        PA3P to PaperSpec(PA3P, 13.0f, 19.0f),
        "Legal (8.5 × 14 in)" to PaperSpec("Legal (8.5 × 14 in)", 8.5f, 14.0f),
        "Letter (8.5 × 11 in)" to PaperSpec("Letter (8.5 × 11 in)", 8.5f, 11.0f),
        "Roll 24 in" to PaperSpec("Roll 24 in", null, null, isRoll = true, rollWidthIn = 24.0f),
        "Roll 36 in" to PaperSpec("Roll 36 in", null, null, isRoll = true, rollWidthIn = 36.0f),
        "Roll 44 in" to PaperSpec("Roll 44 in", null, null, isRoll = true, rollWidthIn = 44.0f)
    )

    val PLACEMENTS = listOf("Center", "Left chest", "Right chest", "Left sleeve", "Right sleeve")

    fun defaultSizes(): List<PrintSize> = listOf(
        PrintSize("Left Chest 4 × 3", 4f, 3f, "Front", "Left chest", 3.0f, 3.0f, PA4),
        PrintSize("Left Chest 3 × 3", 3f, 3f, "Front", "Left chest", 3.0f, 3.0f, PA4),
        PrintSize("Left Chest 4 × 4", 4f, 4f, "Front", "Left chest", 3.5f, 3.5f, PA4),
        PrintSize("Center Chest 8 × 8", 8f, 8f, "Front", "Center", 3.5f, 3.0f, PA4),
        PrintSize("Center Chest 8 × 10", 8f, 10f, "Front", "Center", 3.5f, 3.0f, PA3),
        PrintSize("Front Chest Wide 10 × 2", 10f, 2f, "Front", "Center", 3.0f, 3.0f, PA3),
        PrintSize("Full Front 10 × 12 (M, L)", 10f, 12f, "Front", "Center", 2.5f, 3.0f, PA3),
        PrintSize("Full Front 12 × 12", 12f, 12f, "Front", "Center", 2.0f, 3.0f, PA3P),
        PrintSize("Full Front 11 × 14 (XL, 2XL)", 11f, 14f, "Front", "Center", 2.5f, 3.0f, PA3),
        PrintSize("Back 10 × 12", 10f, 12f, "Back", "Center", 3.5f, 3.0f, PA3),
        PrintSize("Back 12 × 14", 12f, 14f, "Back", "Center", 4.0f, 3.0f, PA3P),
        PrintSize("Back 12 × 16", 12f, 16f, "Back", "Center", 3.5f, 3.0f, PA3P),
        PrintSize("Sleeve 2 × 2", 2f, 2f, "Front", "Left sleeve", 1.75f, 3.0f, PA4),
        PrintSize("Sleeve 3 × 3", 3f, 3f, "Front", "Left sleeve", 1.75f, 3.0f, PA4),
        PrintSize("Sleeve 3 × 4", 3f, 4f, "Front", "Left sleeve", 1.75f, 3.0f, PA4),
        PrintSize("Neck Label 2 × 2", 2f, 2f, "Back", "Center", 1.0f, 3.0f, PA4),
        PrintSize("Neck Label 2.5 × 2.5", 2.5f, 2.5f, "Back", "Center", 1.0f, 3.0f, PA4)
    )

    fun fmtNum(x: Float): String {
        val s = "%.2f".format(x).trimEnd('0').trimEnd('.')
        return if (s.isEmpty()) "0" else s
    }
}
