package com.joethebuilder.k9.nfc

import android.nfc.Tag
import androidx.compose.runtime.mutableStateOf

/**
 * Firmware writes were synchronous: waitForTag() blocked until a tag showed up,
 * then wrote immediately (see aceWriteTag/openSpoolWriteTag/qidiWriteTag call sites).
 * A phone can't block the UI thread waiting for a tap, so WRITE becomes two steps:
 *   1. User picks fields, taps WRITE -> arm() stores what to do next
 *   2. Next NFC tap -> MainActivity checks armed state first, executes it,
 *      and skips the normal auto-detect/read/navigate flow for that tap
 *
 * Screens should show "hold tag near phone to write" after calling arm(),
 * mirroring the firmware's "Hold tag near reader..." footer message.
 */
object WriteArmState {
    private val pending = mutableStateOf<((Tag) -> Boolean)?>(null)

    // Observable by Compose screens (e.g. to show "hold tag near phone to write").
    val armed = mutableStateOf(false)

    fun arm(action: (Tag) -> Boolean) {
        pending.value = action
        armed.value = true
    }

    fun cancel() {
        pending.value = null
        armed.value = false
    }

    /** Returns true if a write was consumed (caller should skip normal detect routing). */
    fun consumeIfArmed(tag: Tag, onResult: (Boolean) -> Unit): Boolean {
        val action = pending.value ?: return false
        pending.value = null
        armed.value = false
        onResult(action(tag))
        return true
    }
}
