package com.joethebuilder.k9.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.nfc.QidiTagIO
import com.joethebuilder.k9.nfc.TagPresencePoller
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Port of QIDI-related firmware state: qidiEntryMatCodeIdx / qidiEntryMfgCode /
 * qidiEntryColIdx entry fields, plus qidiTagPresent (sub-menu auto-scan) and
 * qidiEntryShowingRead (entry screen manual-read display).
 */
class QidiViewModel : ViewModel() {

    private val _matCodeIdx = MutableStateFlow(0)
    val matCodeIdx: StateFlow<Int> = _matCodeIdx

    private val _mfgCode = MutableStateFlow(0) // 0=Generic, 1=QIDI
    val mfgCode: StateFlow<Int> = _mfgCode

    private val _colIdx = MutableStateFlow(1) // 1..24 into QidiData.colors
    val colIdx: StateFlow<Int> = _colIdx

    private val _lastRead = MutableStateFlow<TagData?>(null)
    val lastRead: StateFlow<TagData?> = _lastRead

    private val _subMenuState = MutableStateFlow(SubMenuTagState.NONE)
    val subMenuState: StateFlow<SubMenuTagState> = _subMenuState

    private val _showingRead = MutableStateFlow(false)
    val showingRead: StateFlow<Boolean> = _showingRead

    private val _writeResult = MutableStateFlow<Boolean?>(null)
    val writeResult: StateFlow<Boolean?> = _writeResult

    fun setMaterial(idx: Int) { _matCodeIdx.value = idx }
    fun toggleManufacturer() { _mfgCode.value = if (_mfgCode.value == 0) 1 else 0 }
    fun setColor(idx: Int) { _colIdx.value = idx }

    /** Port of loop()'s SCR_QIDI auto-scan block (qidiTagPresent tracking). */
    fun onSubMenuTagDetected(tag: Tag, data: TagData?) {
        if (data != null) {
            _lastRead.value = data
            _subMenuState.value = SubMenuTagState.PRESENT
        } else {
            _lastRead.value = null
            _subMenuState.value = SubMenuTagState.BLANK
        }
        TagPresencePoller.start(tag, viewModelScope) {
            _lastRead.value = null
            _subMenuState.value = SubMenuTagState.NONE
        }
    }

    /** Port of the entry screen's READ button: waitForTag() -> read -> hold until lifted. */
    fun armEntryRead() {
        NfcFlowState.armRead { result ->
            _lastRead.value = result.qidiData
            _showingRead.value = true
            TagPresencePoller.start(result.tag, viewModelScope) {
                _showingRead.value = false
            }
        }
    }

    fun cancelArm() { NfcFlowState.cancel() }

    /** Port of the entry screen's SAVE button: qidiWriteTag(). */
    fun armWrite() {
        val matCode = QidiData.materialCodes[_matCodeIdx.value]
        val colIdx = _colIdx.value
        val mfgCode = _mfgCode.value
        NfcFlowState.armWrite({ tag -> QidiTagIO.write(tag, matCode, colIdx, mfgCode) }) { ok ->
            _writeResult.value = ok
        }
    }

    fun clearWriteResult() { _writeResult.value = null }
}
