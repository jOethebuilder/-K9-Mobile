package com.joethebuilder.k9.nfc

import android.nfc.Tag
import androidx.compose.runtime.mutableStateOf

/**
 * Generalizes the earlier WriteArmState to also cover READ, since entry
 * screens need both:
 *   - SAVE button -> port of e.g. openSpoolWriteTag() blocking on waitForTag()
 *   - READ button -> port of the manual "Hold tag to read..." + waitForTag()
 *     block before showing osEntryShowingRead = true
 *
 * Both are synchronous/blocking in firmware; on a phone both become
 * "arm an action, wait for the next NFC tap, run it" — same two-step
 * pattern, just generalized to either direction.
 */
object NfcFlowState {

    private sealed class PendingAction {
        data class Write(val execute: (Tag) -> Boolean, val onResult: (Boolean) -> Unit) : PendingAction()
        data class Read(val onResult: (NfcSessionManager.DetectionResult) -> Unit) : PendingAction()
    }

    private var pending: PendingAction? = null

    // Observable by screens, e.g. to swap button label to "Hold tag near phone…"
    val armed = mutableStateOf(false)
    val armedForRead = mutableStateOf(false)

    fun armWrite(execute: (Tag) -> Boolean, onResult: (Boolean) -> Unit) {
        pending = PendingAction.Write(execute, onResult)
        armed.value = true
        armedForRead.value = false
    }

    fun armRead(onResult: (NfcSessionManager.DetectionResult) -> Unit) {
        pending = PendingAction.Read(onResult)
        armed.value = true
        armedForRead.value = true
    }

    fun cancel() {
        pending = null
        armed.value = false
        armedForRead.value = false
    }

    /** Returns true if this tap was consumed as an armed action (caller should skip normal routing). */
    fun consumeIfArmed(result: NfcSessionManager.DetectionResult): Boolean {
        val action = pending ?: return false
        pending = null
        armed.value = false
        armedForRead.value = false
        when (action) {
            is PendingAction.Write -> action.onResult(action.execute(result.tag))
            is PendingAction.Read -> action.onResult(result)
        }
        return true
    }
}
