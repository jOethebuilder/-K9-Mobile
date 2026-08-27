package com.joethebuilder.k9.nfc

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import com.joethebuilder.k9.protocol.AceData
import com.joethebuilder.k9.protocol.TagData
import java.nio.charset.Charset

/**
 * Port of aceReadTag() / aceWriteTag() (N033 protocol) from the K-9 .ino.
 * NTAG215 is command-compatible with MifareUltralight for page read/write,
 * so this reuses that tech class instead of the PN532 ntag2xx_* calls —
 * same page numbers and byte layout as firmware, confirmed against
 * multiACE / official Kobra3 source per Joe's notes.
 *
 * Page layout (unchanged from firmware):
 *   4        -> header, byte0 must be 0x7B for a valid ACE tag
 *   10-14    -> brand, 20 bytes ("AC" padded)
 *   15-19    -> material, 20 bytes
 *   20       -> [alpha, blue, green, red]
 *   24       -> [extMinLE(2), extMaxLE(2)]
 *   29       -> [bedMinLE(2), bedMaxLE(2)]
 *   30       -> [diameter100 LE(2), lengthM LE(2)]
 *   31       -> fixed constant E8 03 00 00
 */
object AceTagIO {

    private fun intToByteLE(v: Int): ByteArray =
        byteArrayOf((v and 0xFF).toByte(), ((v shr 8) and 0xFF).toByte())

    private fun byteToIntLE(b: ByteArray, offset: Int = 0): Int =
        (b[offset].toInt() and 0xFF) or ((b[offset + 1].toInt() and 0xFF) shl 8)

    private fun readPage(mfu: MifareUltralight, page: Int): ByteArray {
        // readPages() returns 16 bytes (4 pages) starting at `page`; we only need the first 4.
        val chunk = mfu.readPages(page)
        return chunk.copyOfRange(0, 4)
    }

    private fun writeStringAcrossPages(mfu: MifareUltralight, startPage: Int, value: String, maxLen: Int) {
        val bytes = ByteArray(maxLen)
        val src = value.toByteArray(Charset.forName("US-ASCII"))
        System.arraycopy(src, 0, bytes, 0, minOf(src.size, maxLen))
        val pages = maxLen / 4
        for (p in 0 until pages) {
            mfu.writePage(startPage + p, bytes.copyOfRange(p * 4, p * 4 + 4))
        }
    }

    private fun readStringAcrossPages(mfu: MifareUltralight, startPage: Int, maxLen: Int): String {
        val bytes = ByteArray(maxLen)
        val pages = maxLen / 4
        for (p in 0 until pages) {
            val page = readPage(mfu, startPage + p)
            System.arraycopy(page, 0, bytes, p * 4, 4)
        }
        return String(bytes, Charset.forName("US-ASCII")).trimEnd('\u0000')
    }

    fun read(tag: Tag): TagData? {
        val mfu = MifareUltralight.get(tag) ?: return null
        return try {
            mfu.connect()
            val header = readPage(mfu, 4)
            if ((header[0].toInt() and 0xFF) != 0x7B) return null

            val brand = readStringAcrossPages(mfu, 10, 20)
            val material = readStringAcrossPages(mfu, 15, 20)

            val colorPage = readPage(mfu, 20)
            // page = [Alpha, Blue, Green, Red] — same order as firmware
            val b = colorPage[1].toInt() and 0xFF
            val g = colorPage[2].toInt() and 0xFF
            val r = colorPage[3].toInt() and 0xFF

            val extPage = readPage(mfu, 24)
            val extMin = byteToIntLE(extPage, 0)
            val extMax = byteToIntLE(extPage, 2)

            val bedPage = readPage(mfu, 29)
            val bedMin = byteToIntLE(bedPage, 0)
            val bedMax = byteToIntLE(bedPage, 2)

            TagData(
                manufacturer = brand,
                material = material,
                color = "FF%02X%02X%02X".format(r, g, b),
                r = r, g = g, b = b,
                extMin = extMin, extMax = extMax,
                bedMin = bedMin, bedMax = bedMax,
                uid = tag.id,
                hasData = true
            )
        } catch (e: Exception) {
            null
        } finally {
            runCatching { mfu.close() }
        }
    }

    /**
     * Writes a full ACE tag. `alpha` defaults to 0xFF per firmware's locked-alpha rule.
     * `diameter100` (e.g. 175 == 1.75mm) and `lengthM` come from the selected weight preset.
     */
    fun write(
        tag: Tag,
        material: String,
        r: Int, g: Int, b: Int,
        alpha: Int = 0xFF,
        extMin: Int, extMax: Int,
        bedMin: Int, bedMax: Int,
        diameter100: Int = 175,
        lengthM: Int
    ): Boolean {
        val mfu = MifareUltralight.get(tag) ?: return false
        return try {
            mfu.connect()

            mfu.writePage(4, byteArrayOf(0x7B, 0x00, 0x65, 0x00))

            val sku = AceData.getSku(material)
            writeStringAcrossPages(mfu, 5, sku, 20)      // firmware writes SKU at pages 5-9
            writeStringAcrossPages(mfu, 10, "AC", 20)    // brand hardcoded to "AC"
            writeStringAcrossPages(mfu, 15, material, 20)

            mfu.writePage(20, byteArrayOf(alpha.toByte(), b.toByte(), g.toByte(), r.toByte()))

            mfu.writePage(24, intToByteLE(extMin) + intToByteLE(extMax))
            mfu.writePage(29, intToByteLE(bedMin) + intToByteLE(bedMax))
            mfu.writePage(30, intToByteLE(diameter100) + intToByteLE(lengthM))
            mfu.writePage(31, byteArrayOf(0xE8.toByte(), 0x03, 0x00, 0x00))

            true
        } catch (e: Exception) {
            false
        } finally {
            runCatching { mfu.close() }
        }
    }
}
