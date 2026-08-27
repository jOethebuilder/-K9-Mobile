package com.joethebuilder.k9.protocol

import kotlin.math.pow

/**
 * Ported from qidiMaterialName() / QIDI_MATERIAL_CODES in the K-9 .ino.
 * Codes 1-50, spec-complete per Joe's verified table (PCTG/PETG Silk
 * genuinely absent from QIDI spec, not an oversight).
 */
object QidiData {

    val materialCodes: List<Int> = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 13, 14, 18, 19, 24, 25, 26, 27,
        30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 44, 45, 47, 49, 50
    )

    fun materialName(code: Int): String = when (code) {
        1 -> "PLA"; 2 -> "PLA Matte"; 3 -> "PLA Metal"; 4 -> "PLA Silk"
        5 -> "PLA-CF"; 6 -> "PLA-Wood"; 7 -> "PLA Basic"; 8 -> "PLA Matte Basic"
        11 -> "ABS"; 12 -> "ABS-GF"; 13 -> "ABS-Metal"; 14 -> "ABS-Odorless"
        18 -> "ASA"; 19 -> "ASA-AERO"
        24 -> "UltraPA"; 25 -> "PA-CF"; 26 -> "UltraPA-CF25"; 27 -> "PA12-CF"
        30 -> "PAHT-CF"; 31 -> "PAHT-GF"; 32 -> "Support PAHT"; 33 -> "Support PET/PA"
        34 -> "PC/ABS-FR"
        37 -> "PET-CF"; 38 -> "PET-GF"; 39 -> "PETG Basic"; 40 -> "PETG Tough"
        41 -> "PETG Rapido"; 42 -> "PETG-CF"; 43 -> "PETG-GF"; 44 -> "PPS-CF"
        45 -> "PETG Trans."
        47 -> "PVA"
        49 -> "TPU-Aero"; 50 -> "TPU"
        else -> "Unknown"
    }

    fun manufacturerName(code: Int): String = when (code) {
        0 -> "Generic"
        1 -> "QIDI"
        else -> "Unknown"
    }

    data class QidiColor(val r: Int, val g: Int, val b: Int, val label: String)

    // Index 0 reserved for "Unknown" to match firmware's 1..24 addressable range.
    val colors: List<QidiColor> = listOf(
        QidiColor(0, 0, 0, "Unknown"),
        QidiColor(250, 250, 250, "White"),
        QidiColor(6, 6, 6, "Black"),
        QidiColor(217, 227, 237, "Light Blue"),
        QidiColor(92, 243, 15, "Lime"),
        QidiColor(99, 228, 146, "Mint"),
        QidiColor(40, 80, 255, "Blue"),
        QidiColor(254, 152, 254, "Pink"),
        QidiColor(223, 214, 40, "Yellow"),
        QidiColor(34, 131, 50, "Green"),
        QidiColor(153, 222, 255, "Sky Blue"),
        QidiColor(23, 20, 176, "Dark Blue"),
        QidiColor(206, 192, 254, "Lavender"),
        QidiColor(202, 222, 75, "Yellow-Grn"),
        QidiColor(19, 83, 171, "Navy"),
        QidiColor(94, 169, 253, "Cornflower"),
        QidiColor(168, 120, 255, "Purple"),
        QidiColor(254, 113, 122, "Salmon"),
        QidiColor(255, 54, 45, "Red"),
        QidiColor(226, 223, 205, "Beige"),
        QidiColor(137, 143, 155, "Gray"),
        QidiColor(110, 56, 18, "Brown"),
        QidiColor(202, 197, 159, "Khaki"),
        QidiColor(242, 134, 54, "Orange"),
        QidiColor(184, 127, 43, "Gold"),
    )

    /** Direct port of nearestColorName() — nearest-neighbor in RGB space, indices 1..24. */
    fun nearestColorName(r: Int, g: Int, b: Int): String {
        var bestIdx = 1
        var bestDist = Long.MAX_VALUE
        for (i in 1 until colors.size) {
            val dr = (r - colors[i].r).toLong()
            val dg = (g - colors[i].g).toLong()
            val db = (b - colors[i].b).toLong()
            val dist = dr * dr + dg * dg + db * db
            if (dist < bestDist) { bestDist = dist; bestIdx = i }
        }
        return colors[bestIdx].label
    }
}
