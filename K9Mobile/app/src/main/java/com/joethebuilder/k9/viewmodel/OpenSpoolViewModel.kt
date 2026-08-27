package com.joethebuilder.k9.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.nfc.OpenSpoolTagIO
import com.joethebuilder.k9.nfc.TagPresencePoller
import com.joethebuilder.k9.network.MoonrakerClient
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Port of OpenSpool U1 firmware state: osEntryMatIdx / osEntryMfgIdx / osEntryColIdx /
 * osEntrySubIdx entry fields, osTagPresent (sub-menu auto-scan), osEntryShowingRead
 * (entry manual-read display), plus U1 connection settings and SEND.
 */
class OpenSpoolViewModel(
    private val prefs: PrefsRepository,
    private val moonraker: MoonrakerClient = MoonrakerClient()
) : ViewModel() {

    private val _matIdx = MutableStateFlow(0)
    val matIdx: StateFlow<Int> = _matIdx

    private val _mfgIdx = MutableStateFlow(0)
    val mfgIdx: StateFlow<Int> = _mfgIdx

    private val _colIdx = MutableStateFlow(1) // 1..24 into QidiData.colors, same range firmware reuses
    val colIdx: StateFlow<Int> = _colIdx

    private val _subIdx = MutableStateFlow(0)
    val subIdx: StateFlow<Int> = _subIdx

    private val _lastRead = MutableStateFlow<TagData?>(null)
    val lastRead: StateFlow<TagData?> = _lastRead

    private val _subMenuState = MutableStateFlow(SubMenuTagState.NONE)
    val subMenuState: StateFlow<SubMenuTagState> = _subMenuState

    private val _showingRead = MutableStateFlow(false)
    val showingRead: StateFlow<Boolean> = _showingRead

    private val _writeResult = MutableStateFlow<Boolean?>(null)
    val writeResult: StateFlow<Boolean?> = _writeResult

    private val _sendResult = MutableStateFlow<Boolean?>(null)
    val sendResult: StateFlow<Boolean?> = _sendResult

    val u1Host: StateFlow<String> = MutableStateFlow("").also { flow ->
        viewModelScope.launch { prefs.u1Host.collect { flow.value = it } }
    }

    private val _u1TestResult = MutableStateFlow<Boolean?>(null)
    val u1TestResult: StateFlow<Boolean?> = _u1TestResult

    fun setMaterial(idx: Int) {
        _matIdx.value = idx
        if (!OpenSpoolData.materialHasSubtypes(idx)) _subIdx.value = 0
    }
    fun setManufacturer(idx: Int) { _mfgIdx.value = idx }
    fun setColor(idx: Int) { _colIdx.value = idx }
    fun setSubtype(idx: Int) { _subIdx.value = idx }

    fun currentSubtype(): String {
        val list = OpenSpoolData.subtypeList(_matIdx.value)
        return list.getOrNull(_subIdx.value) ?: "Basic"
    }

    /** Port of loop()'s SCR_OPENSPOOL auto-scan block (osTagPresent tracking). */
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
            _lastRead.value = result.openSpoolData
            _showingRead.value = true
            TagPresencePoller.start(result.tag, viewModelScope) {
                _showingRead.value = false
            }
        }
    }

    fun cancelArm() { NfcFlowState.cancel() }

    /** Port of the entry screen's SAVE button: openSpoolWriteTag(). */
    fun armWrite() {
        val color = QidiData.colors[_colIdx.value]
        val material = OpenSpoolData.materials[_matIdx.value]
        val manufacturer = OpenSpoolData.manufacturers[_mfgIdx.value]
        val subtype = currentSubtype()
        val tagData = TagData(
            manufacturer = manufacturer,
            material = material.name,
            color = color.label,
            r = color.r, g = color.g, b = color.b,
            extMin = material.nozzleMin, extMax = material.nozzleMax,
            bedMin = material.bedMin, bedMax = material.bedMax,
            hasData = true
        )
        NfcFlowState.armWrite({ tag -> OpenSpoolTagIO.write(tag, tagData, subtype) }) { ok ->
            _writeResult.value = ok
        }
    }

    fun clearWriteResult() { _writeResult.value = null }

    /** Port of SCR_OPENSPOOL_SLOT_PICKER tile-tap handler -> u1SendFilamentConfig(). */
    fun sendToSlot(slot: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val color = QidiData.colors[_colIdx.value]
            val colorHex = "%02X%02X%02X%02X".format(color.r, color.g, color.b, 0xFF)
            val ok = moonraker.sendFilamentConfig(
                host = u1Host.value,
                slot = slot,
                vendor = OpenSpoolData.manufacturers[_mfgIdx.value],
                type = OpenSpoolData.materials[_matIdx.value].name,
                subtype = currentSubtype(),
                colorHexRgba = colorHex
            )
            _sendResult.value = ok
        }
    }

    fun clearSendResult() { _sendResult.value = null }

    /** Port of SCR_U1_CONNECTION's keyboard OK -> u1Host = kbBuffer; saveU1Host(u1Host). */
    fun saveU1Host(host: String) {
        viewModelScope.launch { prefs.saveU1Host(host) }
    }

    /** Port of SCR_U1_CONNECTION's TEST CONNECTION button -> u1TestConnection(u1Host). */
    fun testU1Connection() {
        _u1TestResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            _u1TestResult.value = moonraker.testConnection(u1Host.value)
        }
    }
}
