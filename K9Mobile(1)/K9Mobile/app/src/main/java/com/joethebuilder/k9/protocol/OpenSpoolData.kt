package com.joethebuilder.k9.protocol

/**
 * Ported from OS_MATERIALS / OS_MANUFACTURERS / PLA_SUBTYPES / PETG_SUBTYPES
 * in the K-9 .ino. Subtypes only apply to PLA and PETG, same rule as firmware
 * (osMaterialHasSubtypes) — everything else is silently "Basic".
 */
object OpenSpoolData {

    data class Material(
        val name: String,
        val nozzleMin: Int,
        val nozzleMax: Int,
        val bedMin: Int,
        val bedMax: Int
    )

    val materials: List<Material> = listOf(
        Material("PLA", 190, 220, 40, 60),
        Material("PETG", 220, 250, 70, 90),
        Material("ABS", 230, 260, 90, 110),
        Material("ASA", 240, 270, 90, 110),
        Material("TPU", 210, 230, 30, 60),
        Material("PA", 240, 270, 70, 100),
        Material("PA12", 240, 270, 70, 100),
        Material("PC", 270, 310, 100, 120),
        Material("PEEK", 360, 400, 100, 140),
        Material("PVA", 190, 220, 45, 60),
        Material("HIPS", 230, 250, 90, 110),
        Material("PCTG", 220, 250, 70, 85),
        Material("PLA-CF", 190, 220, 45, 60),
        Material("PETG-CF", 230, 260, 70, 90),
        Material("PA-CF", 250, 280, 70, 100),
    )

    val manufacturers: List<String> = listOf(
        "Generic", "Snapmaker", "SUNLU", "eSun", "Jayo", "QIDI", "Bambu Lab",
        "Polymaker", "TECBEARS", "GIANTARM", "HATCHBOX", "Overture", "Prusament",
        "TINMORRY", "Kingroon", "Elegoo", "Creality", "Deeplee", "ANYCUBIC",
        "FLASHFORGE", "CC3D", "ZIRO"
    )

    val plaSubtypes = listOf("Basic", "Matte", "Silk", "Wood", "Metallic", "CF")
    val petgSubtypes = listOf("Basic", "Translucent", "HF")

    fun materialHasSubtypes(matIndex: Int): Boolean = matIndex == 0 || matIndex == 1

    fun subtypeList(matIndex: Int): List<String> = when (matIndex) {
        0 -> plaSubtypes
        1 -> petgSubtypes
        else -> emptyList()
    }

    val aceWeightLabels = listOf("1 KG", "750 G", "500 G", "250 G")
    val aceWeightLengths = listOf(330, 247, 165, 82)
}
