package com.joethebuilder.k9.nfc

import android.nfc.Tag
import android.nfc.tech.NfcA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Port of the firmware's continuous "is the tag still there" polling — the
 * mechanism behind qidiTagPresent/aceTagPresent/osTagPresent on sub-menu
 * screens, and the `while (nfc.readPassiveTargetID(...))` hold-loop after a
 * manual READ on entry screens. Same interval (~150ms) and same 30s safety
 * cap as firmware's entry-screen hold loop.
 *
 * Android's reader mode has no "tag removed" event, so presence is checked
 * by repeatedly attempting a trivial NfcA connect/close on the retained Tag
 * object; once that throws (tag out of field), we call onLost().
 */
object TagPresencePoller {

    private const val POLL_INTERVAL_MS = 150L
    private const val SAFETY_CAP_MS = 30_000L

    fun start(tag: Tag, scope: CoroutineScope, onLost: () -> Unit): Job {
        return scope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < SAFETY_CAP_MS) {
                delay(POLL_INTERVAL_MS)
                if (!isPresent(tag)) {
                    onLost()
                    return@launch
                }
            }
            // Safety cap hit, same as firmware's 30s hold-loop cap.
            onLost()
        }
    }

    private fun isPresent(tag: Tag): Boolean {
        val nfcA = NfcA.get(tag) ?: return false
        return try {
            nfcA.connect()
            val ok = nfcA.isConnected
            nfcA.close()
            ok
        } catch (e: Exception) {
            false
        }
    }
}
