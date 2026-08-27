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
 * osTagPresent booleans in loop(), each only active `if (currentScreen == SCR_X)`)
 * with Android's push-based reader mode.
 *
 * IMPORTANT: unlike the first pass, this does NOT auto-route across protocols.
 * Android's reader-mode flags (FLAG_READER_NFC_A/B) select radio-level tech,
 * not Mifare-vs-NTAG — both are NFC-A at that layer, so the split has to happen
 * in software, matching firmware's actual behavior: QIDI's sub-menu screen only
 * reacts to Mifare Classic; OpenSpool/Anycubic sub-menu screens only react to
 * NTAG21x. Tags of the "wrong" type for the current screen are ignored, same
 * as firmware simply never calling qidiWriteTag()-style logic while on a
 * different screen.
 */
object NfcSessionManager {

    enum class Mode { NONE, QIDI, NTAG }

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

    /**
     * @param mode restricts which detections are delivered — NONE means the
     *   callback is still registered (needed for the arm-read/arm-write flow
     *   in NfcFlowState, which works regardless of screen) but sub-menu
     *   auto-detect is suppressed.
     */
    fun enable(activity: Activity, mode: Mode, onTag: (DetectionResult) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        adapter.enableReaderMode(
            activity,
            { tag ->
                val result = detect(tag)
                val allowed = when (mode) {
                    Mode.NONE -> true // let arm-read/arm-write consume it; sub-menu auto-display is gated by caller
                    Mode.QIDI -> result.protocol == DetectedProtocol.QIDI || result.protocol == DetectedProtocol.UNKNOWN_MIFARE_CLASSIC
                    Mode.NTAG -> result.protocol == DetectedProtocol.ANYCUBIC_ACE ||
                        result.protocol == DetectedProtocol.OPENSPOOL_U1 ||
                        result.protocol == DetectedProtocol.UNKNOWN_NTAG
                }
                if (allowed) onTag(result)
            },
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
            val ace = AceTagIO.read(tag)
            if (ace != null) return DetectionResult(DetectedProtocol.ANYCUBIC_ACE, tag, aceData = ace)

            val openSpool = OpenSpoolTagIO.read(tag)
            if (openSpool != null) return DetectionResult(DetectedProtocol.OPENSPOOL_U1, tag, openSpoolData = openSpool)

            return DetectionResult(DetectedProtocol.UNKNOWN_NTAG, tag)
        }

        return DetectionResult(DetectedProtocol.BLANK, tag)
    }
}
