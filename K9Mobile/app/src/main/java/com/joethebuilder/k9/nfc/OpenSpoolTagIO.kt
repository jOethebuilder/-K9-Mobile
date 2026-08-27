package com.joethebuilder.k9.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Port of openSpoolReadTag() / openSpoolWriteTag() from the K-9 .ino.
 *
 * The firmware hand-builds the NDEF TLV + MIME record byte-for-byte because
 * the PN532 library only gives raw page read/write. Android's Ndef tech
 * class already implements NDEF TLV framing and CC setup (equivalent to
 * firmware's ensureOpenSpoolCC()), so this uses NdefRecord.createMime()
 * instead of re-deriving the byte layout — same wire format, less code.
 */
object OpenSpoolTagIO {

    private const val MIME_TYPE = "application/json"

    fun read(tag: Tag): TagData? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val message = ndef.ndefMessage ?: ndef.cachedNdefMessage ?: return null
            val record = message.records.firstOrNull {
                it.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    String(it.type, Charset.forName("US-ASCII")) == MIME_TYPE
            } ?: return null

            val json = JSONObject(String(record.payload, Charsets.UTF_8))
            if (json.optString("protocol") != "openspool") return null

            val hex = json.optString("color_hex", "808080").removePrefix("#")
            val colorInt = hex.toLong(16).toInt()
            val r = (colorInt shr 16) and 0xFF
            val g = (colorInt shr 8) and 0xFF
            val b = colorInt and 0xFF

            TagData(
                manufacturer = json.optString("brand", "Generic"),
                material = json.optString("type", "PLA"),
                color = QidiData.nearestColorName(r, g, b),
                r = r, g = g, b = b,
                extMin = json.optInt("min_temp", 190),
                extMax = json.optInt("max_temp", 220),
                bedMin = json.optInt("bed_min_temp", 0),
                bedMax = json.optInt("bed_max_temp", 60),
                uid = tag.id,
                hasData = true
            )
        } catch (e: Exception) {
            null
        } finally {
            runCatching { ndef.close() }
        }
    }

    /**
     * Writes an OpenSpool JSON payload as a single NDEF MIME record.
     * Falls back to NdefFormatable if the tag hasn't been NDEF-formatted yet
     * (equivalent to firmware's ensureOpenSpoolCC() creating the CC page).
     */
    fun write(tag: Tag, tagData: TagData, subtype: String): Boolean {
        val json = JSONObject().apply {
            put("protocol", "openspool")
            put("brand", tagData.manufacturer.ifBlank { "Generic" })
            put("type", tagData.material.ifBlank { "PLA" })
            put("subtype", subtype)
            put("color_hex", "#%02X%02X%02X".format(tagData.r, tagData.g, tagData.b))
            put("min_temp", tagData.extMin)
            put("max_temp", tagData.extMax)
            put("bed_min_temp", tagData.bedMin)
            put("bed_max_temp", tagData.bedMax)
        }
        val payload = json.toString().toByteArray(Charsets.UTF_8)
        val record = NdefRecord.createMime(MIME_TYPE, payload)
        val message = NdefMessage(arrayOf(record))

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return try {
                ndef.connect()
                if (!ndef.isWritable) return false
                if (message.toByteArray().size > ndef.maxSize) return false
                ndef.writeNdefMessage(message)
                true
            } catch (e: Exception) {
                false
            } finally {
                runCatching { ndef.close() }
            }
        }

        // Not yet NDEF-formatted — format it, same role as ensureOpenSpoolCC().
        val formatable = NdefFormatable.get(tag) ?: return false
        return try {
            formatable.connect()
            formatable.format(message)
            true
        } catch (e: Exception) {
            false
        } finally {
            runCatching { formatable.close() }
        }
    }
}
