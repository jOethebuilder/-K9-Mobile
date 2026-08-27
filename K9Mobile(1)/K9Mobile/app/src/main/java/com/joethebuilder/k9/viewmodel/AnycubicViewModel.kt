package com.joethebuilder.k9.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joethebuilder.k9.nfc.AceTagIO
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.nfc.TagPresencePoller
import com.joethebuilder.k9.protocol.AceData
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.protocol.TagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Port of Anycubic ACE firmware state: aceEntryMatIdx / aceEntrySizeIdx /
 * aceEntryColIdx / aceEntryIsCustom + aceCustomR/G/B steppers, aceTagPresent
 * (sub-menu auto-scan), aceEntryShowingRead (entry manual-read display).
 * Alpha stays locked to 0xFF, same rule as firmware.
 */
class AnycubicViewModel : ViewModel() {

    private val _matIdx = MutableStateFlow(0)      // index into OpenSpoolData.materials, reused like firmware
    val matIdx: StateFlow<Int> = _matIdx

    private val _sizeIdx = MutableStateFlow(0)     // index into OpenSpoolData.aceWeightLabels
    val sizeIdx: StateFlow<Int> = _sizeIdx

    private val _colorIdx = MutableStateFlow(0)    // index into AceData.presetColors
    val colorIdx: StateFlow<Int> = _colorIdx

    private val _isCustom = MutableStateFlow(false)
    val isCustom: StateFlow<Boolean> = _isCustom

    private val _customR = MutableStateFlow(0)
    private val _customG = MutableStateFlow(0)
    private val _customB = MutableStateFlow(255)
    val customR: StateFlow<Int> = _customR
    val customG: StateFlow<Int> = _customG
    val customB: StateFlow<Int> = _customB

    private val _lastRead = MutableStateFlow<TagData?>(null)
    val lastRead: StateFlow<TagData?> = _lastRead

    private val _subMenuState = MutableStateFlow(SubMenuTagState.NONE)
    val subMenuState: StateFlow<SubMenuTagState> = _subMenuState

    private val _showingRead = MutableStateFlow(false)
    val showingRead: StateFlow<Boolean> = _showingRead

    private val _writeResult = MutableStateFlow<Boolean?>(null)
    val writeResult: StateFlow<Boolean?> = _writeResult

    fun setMaterial(idx: Int) { _matIdx.value = idx }
    fun setSize(idx: Int) { _sizeIdx.value = idx }
    fun setPresetColor(idx: Int) { _colorIdx.value = idx; _isCustom.value = false }
    fun startCustomFromPreset() {
        val preset = AceData.presetColors[_colorIdx.value]
        _customR.value = preset.first
        _customG.value = preset.second
        _customB.value = preset.third
    }
    fun setCustomColor(r: Int, g: Int, b: Int) {
        _customR.value = r.coerceIn(0, 255)
        _customG.value = g.coerceIn(0, 255)
        _customB.value = b.coerceIn(0, 255)
        _isCustom.value = true
    }

    /** Port of loop()'s SCR_ANYCUBIC auto-scan block (aceTagPresent tracking). */
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

    /** Port of the entry screen's READ button. */
    fun armEntryRead() {
        NfcFlowState.armRead { result ->
            _lastRead.value = result.aceData
            _showingRead.value = true
            TagPresencePoller.start(result.tag, viewModelScope) {
                _showingRead.value = false
            }
        }
    }

    fun cancelArm() { NfcFlowState.cancel() }

    /** Port of the entry screen's SAVE button: aceWriteTag(). */
    fun armWrite() {
        val (r, g, b) = if (_isCustom.value) {
            Triple(_customR.value, _customG.value, _customB.value)
        } else {
            AceData.presetColors[_colorIdx.value]
        }
        val material = OpenSpoolData.materials[_matIdx.value]
        val lengthM = OpenSpoolData.aceWeightLengths[_sizeIdx.value]
        NfcFlowState.armWrite({ tag ->
            AceTagIO.write(
                tag = tag,
                material = material.name,
                r = r, g = g, b = b,
                alpha = 0xFF,
                extMin = material.nozzleMin, extMax = material.nozzleMax,
                bedMin = material.bedMin, bedMax = material.bedMax,
                diameter100 = 175,
                lengthM = lengthM
            )
        }) { ok ->
            _writeResult.value = ok
        }
    }

    fun clearWriteResult() { _writeResult.value = null }
}
