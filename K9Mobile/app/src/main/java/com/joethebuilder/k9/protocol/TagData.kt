package com.joethebuilder.k9.protocol

/**
 * Direct port of the K-9 firmware's TagData struct.
 * Used as the shared "currently on reader / currently being edited" model
 * across all three protocols, same role it plays in the .ino.
 */
data class TagData(
    val manufacturer: String = "",
    val material: String = "",
    val color: String = "",
    val r: Int = 0,
    val g: Int = 0,
    val b: Int = 0,
    val extMin: Int = 0,
    val extMax: Int = 0,
    val bedMin: Int = 0,
    val bedMax: Int = 0,
    val uid: ByteArray = ByteArray(0),
    val hasData: Boolean = false
) {
    fun uidHex(): String = uid.joinToString(":") { b -> "%02X".format(b) }
}

/** Mirrors AceTagData from the firmware — ACE has its own field set (sku, brand, diameter). */
data class AceTagData(
    val brand: String = "AC",
    val material: String = "",
    val sku: String = "",
    val r: Int = 0,
    val g: Int = 0,
    val b: Int = 0,
    val extMin: Int = 0,
    val extMax: Int = 0,
    val bedMin: Int = 0,
    val bedMax: Int = 0,
    val diameter100: Int = 175,
    val lengthM: Int = 330
)

/** What kind of tag was detected on tap, for the auto-route flow. */
enum class DetectedProtocol {
    QIDI,
    OPENSPOOL_U1,
    ANYCUBIC_ACE,
    UNKNOWN_MIFARE_CLASSIC,   // Mifare Classic present but block 4 didn't parse as QIDI
    UNKNOWN_NTAG,             // NTAG21x present but neither NDEF-JSON nor ACE header matched
    BLANK
}
