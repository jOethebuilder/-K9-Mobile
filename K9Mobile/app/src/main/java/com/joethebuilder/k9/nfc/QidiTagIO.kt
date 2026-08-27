package com.joethebuilder.k9.nfc

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData

/**
 * Port of the firmware's QIDI Mifare Classic path:
 *   - qidiWriteTag() / the inline block-4 read in loop()
 *   - key A = FF FF FF FF FF FF, sector 1 (block 4), same as .ino
 *   - data[0]=matID, data[1]=colID, data[2]=mfgID  (unchanged layout)
 */
object QidiTagIO {

    private val DEFAULT_KEY_A = byteArrayOf(
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
    )
    private const val BLOCK_4 = 4
    private const val SECTOR_1 = 1

    /** Returns null if tag isn't Mifare Classic or auth/read fails (mirrors firmware's TAG_BLANK path). */
    fun read(tag: Tag): TagData? {
        val mfc = MifareClassic.get(tag) ?: return null
        return try {
            mfc.connect()
            if (!mfc.authenticateSectorWithKeyA(SECTOR_1, DEFAULT_KEY_A)) return null
            val data = mfc.readBlock(BLOCK_4)
            val matCode = data[0].toInt() and 0xFF
            val colCode = data[1].toInt() and 0xFF
            val mfgCode = data[2].toInt() and 0xFF

            val color = if (colCode in 1..24) QidiData.colors[colCode] else null
            TagData(
                manufacturer = QidiData.manufacturerName(mfgCode),
                material = QidiData.materialName(matCode),
                color = color?.label ?: "Unknown",
                r = color?.r ?: 128,
                g = color?.g ?: 128,
                b = color?.b ?: 128,
                uid = tag.id,
                hasData = true
            )
        } catch (e: Exception) {
            null
        } finally {
            runCatching { mfc.close() }
        }
    }

    /**
     * Writes matCode/colIdx/mfgCode into block 4, byte layout unchanged from firmware:
     * data[0]=matCode, data[1]=colIdx (1..24), data[2]=mfgCode (0=Generic,1=QIDI).
     */
    fun write(tag: Tag, matCode: Int, colIdx: Int, mfgCode: Int): Boolean {
        val mfc = MifareClassic.get(tag) ?: return false
        return try {
            mfc.connect()
            if (!mfc.authenticateSectorWithKeyA(SECTOR_1, DEFAULT_KEY_A)) return false
            val data = ByteArray(16)
            data[0] = matCode.toByte()
            data[1] = colIdx.toByte()
            data[2] = mfgCode.toByte()
            mfc.writeBlock(BLOCK_4, data)
            true
        } catch (e: Exception) {
            false
        } finally {
            runCatching { mfc.close() }
        }
    }
}
