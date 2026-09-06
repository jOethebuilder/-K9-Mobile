package com.joethebuilder.k9.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.nfc.OpenSpoolTagIO
import com.joethebuilder.k9.nfc.TagPresencePoller
import com.joethebuilder.k9.network.BambuMqttClient
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.protocol.TagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Bambu OpenSpool — same tag format and same fields as OpenSpoolViewModel
 * (this class mirrors it 1:1), the only real difference being the
 * printer connection at SEND time: MQTT+TLS to a Bambu printer's local
 * broker instead of HTTP to Moonraker. Bambu's external spool slot is a
 * single target (ams_id 255 / tray_id 254), so there's no slot picker —
 * SEND goes straight from the entry screen.
 */
class BambuViewModel(
    private val prefs: PrefsRepository,
    private val bambuMqtt: BambuMqttClient = BambuMqttClient()
) : ViewModel() {

    private val _matIdx = MutableStateFlow(0)
    val matIdx: StateFlow<Int> = _matIdx

    private val _mfgIdx = MutableStateFlow(0)
    val mfgIdx: StateFlow<Int> = _mfgIdx

    private val _colIdx = MutableStateFlow(1)
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

    val bambuHost: StateFlow<String> = MutableStateFlow("").also { flow ->
        viewModelScope.launch { prefs.bambuHost.collect { flow.value = it } }
    }
    val bambuSerial: StateFlow<String> = MutableStateFlow("").also { flow ->
        viewModelScope.launch { prefs.bambuSerial.collect { flow.value = it } }
    }
    val bambuAccessCode: StateFlow<String> = MutableStateFlow("").also { flow ->
        viewModelScope.launch { prefs.bambuAccessCode.collect { flow.value = it } }
    }

    private val _bambuTestResult = MutableStateFlow<Boolean?>(null)
    val bambuTestResult: StateFlow<Boolean?> = _bambuTestResult

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

    /** SEND — no slot picker; Bambu's external spool is a single target (ams_id 255 / tray_id 254). */
    fun sendToBambu() {
        viewModelScope.launch(Dispatchers.IO) {
            val color = QidiData.colors[_colIdx.value]
            val colorHex = "%02X%02X%02X".format(color.r, color.g, color.b)
            val material = OpenSpoolData.materials[_matIdx.value]
            val ok = bambuMqtt.sendFilamentConfig(
                host = bambuHost.value,
                serial = bambuSerial.value,
                accessCode = bambuAccessCode.value,
                materialType = material.name,
                colorHex = colorHex,
                nozzleTempMin = material.nozzleMin,
                nozzleTempMax = material.nozzleMax
            )
            _sendResult.value = ok
        }
    }

    fun clearSendResult() { _sendResult.value = null }

    fun saveBambuHost(host: String) { viewModelScope.launch { prefs.saveBambuHost(host) } }
    fun saveBambuSerial(serial: String) { viewModelScope.launch { prefs.saveBambuSerial(serial) } }
    fun saveBambuAccessCode(code: String) { viewModelScope.launch { prefs.saveBambuAccessCode(code) } }

    fun testBambuConnection() {
        _bambuTestResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            _bambuTestResult.value = bambuMqtt.testConnection(bambuHost.value, bambuSerial.value, bambuAccessCode.value)
        }
    }
}
