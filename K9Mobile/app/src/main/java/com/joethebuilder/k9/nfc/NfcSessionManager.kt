package com.joethebuilder.k9.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import com.joethebuilder.k9.protocol.DetectedProtocol
import com.joethebuilder.k9.protocol.TagData

/**
 * Replaces the firmware's per-screen polling loop (qidiTagPresent / aceTagPresent /
 * osTagPresent booleans in loop()) with Android's push-based reader mode: the OS
 * calls back exactly once per tag tap, no polling needed.
 *
 * Auto-detect flow (the design choice flagged before writing this):
 *   1. Mifare Classic present  -> try QIDI block-4 read
 *   2. NTAG21x present         -> try ACE header byte first, then OpenSpool NDEF
 *   3. Nothing recognized      -> UNKNOWN_* / BLANK, let the UI decide what to show
 */
object NfcSessionManager {

    data class DetectionResult(
        val protocol: DetectedProtocol,
        val tag: Tag,
        val qidiData: TagData? = null,
        val aceData: TagData? = null,
        val openSpoolData: TagData? = null
    )

    private val READER_FLAGS =
        NfcAdapter.FLAG_READER_NFC_A or
        NfcAdapter.FLAG_READER_NFC_B or
        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

    fun enable(activity: Activity, onTag: (DetectionResult) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        adapter.enableReaderMode(
            activity,
            { tag -> onTag(detect(tag)) },
            READER_FLAGS,
            null
        )
    }

    fun disable(activity: Activity) {
        NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
    }

    private fun detect(tag: Tag): DetectionResult {
        val techList = tag.techList.toSet()

        if (techList.contains(MifareClassic::class.java.name)) {
            val data = QidiTagIO.read(tag)
            return if (data != null) {
                DetectionResult(DetectedProtocol.QIDI, tag, qidiData = data)
            } else {
                DetectionResult(DetectedProtocol.UNKNOWN_MIFARE_CLASSIC, tag)
            }
        }

        if (techList.contains(MifareUltralight::class.java.name)) {
            // ACE first: cheap header-byte check, no JSON parsing needed to rule it out.
            val ace = AceTagIO.read(tag)
            if (ace != null) return DetectionResult(DetectedProtocol.ANYCUBIC_ACE, tag, aceData = ace)

            val openSpool = OpenSpoolTagIO.read(tag)
            if (openSpool != null) return DetectionResult(DetectedProtocol.OPENSPOOL_U1, tag, openSpoolData = openSpool)

            return DetectionResult(DetectedProtocol.UNKNOWN_NTAG, tag)
        }

        return DetectionResult(DetectedProtocol.BLANK, tag)
    }
}
