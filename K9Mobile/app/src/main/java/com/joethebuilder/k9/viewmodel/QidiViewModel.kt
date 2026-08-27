package com.joethebuilder.k9.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import com.joethebuilder.k9.nfc.QidiTagIO
import com.joethebuilder.k9.nfc.WriteArmState
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Port of qidiEntryMatCodeIdx / qidiEntryMfgCode / qidiEntryColIdx entry state. */
class QidiViewModel : ViewModel() {

    private val _matCodeIdx = MutableStateFlow(0)
    val matCodeIdx: StateFlow<Int> = _matCodeIdx

    private val _mfgCode = MutableStateFlow(0) // 0=Generic, 1=QIDI
    val mfgCode: StateFlow<Int> = _mfgCode

    private val _colIdx = MutableStateFlow(1) // 1..24 into QidiData.colors
    val colIdx: StateFlow<Int> = _colIdx

    private val _lastRead = MutableStateFlow<TagData?>(null)
    val lastRead: StateFlow<TagData?> = _lastRead

    private val _writeResult = MutableStateFlow<Boolean?>(null)
    val writeResult: StateFlow<Boolean?> = _writeResult

    fun setMaterial(idx: Int) { _matCodeIdx.value = idx }
    fun toggleManufacturer() { _mfgCode.value = if (_mfgCode.value == 0) 1 else 0 }
    fun setColor(idx: Int) { _colIdx.value = idx }

    fun onTagDetected(data: TagData) { _lastRead.value = data }

    /** Arms the next NFC tap as a write. Screen should prompt "hold tag near phone". */
    fun armWrite() {
        val matCode = QidiData.materialCodes[_matCodeIdx.value]
        val colIdx = _colIdx.value
        val mfgCode = _mfgCode.value
        WriteArmState.arm { tag -> QidiTagIO.write(tag, matCode, colIdx, mfgCode) }
    }

    fun writeResultOverride(ok: Boolean) { _writeResult.value = ok }
}
