package com.joethebuilder.k9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.nfc.NfcSessionManager
import com.joethebuilder.k9.protocol.DetectedProtocol
import com.joethebuilder.k9.ui.navigation.K9NavHost
import com.joethebuilder.k9.ui.navigation.Routes
import com.joethebuilder.k9.viewmodel.AnycubicViewModel
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel
import com.joethebuilder.k9.viewmodel.QidiViewModel

class OpenSpoolViewModelFactory(private val prefs: PrefsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        OpenSpoolViewModel(prefs) as T
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PrefsRepository(applicationContext)

        setContent {
            val qidiVm: QidiViewModel = viewModel()
            val openSpoolVm: OpenSpoolViewModel = viewModel(factory = OpenSpoolViewModelFactory(prefs))
            val anycubicVm: AnycubicViewModel = viewModel()
            val navController = rememberNavController()

            // Auto-detect result drives navigation, per the design choice discussed with Joe:
            // tap first, route to the matching protocol screen automatically.
            var lastDetected by mutableStateOf<DetectedProtocol?>(null)

            MaterialTheme {
                Surface {
                    K9NavHost(
                        navController = navController,
                        qidiViewModel = qidiVm,
                        openSpoolViewModel = openSpoolVm,
                        anycubicViewModel = anycubicVm
                    )
                }
            }

            // Reader mode is enabled/disabled from onResume/onPause below; the callback
            // reaches into this composition via the activity-scoped ViewModels directly,
            // so it works even while a screen other than the target one is on top.
            readerCallback = { result ->
                // If a write is armed (user tapped WRITE and is now holding a tag),
                // consume this tap as the write target instead of auto-routing it.
                val consumedAsWrite = com.joethebuilder.k9.nfc.WriteArmState.consumeIfArmed(result.tag) { ok ->
                    when (result.protocol) {
                        DetectedProtocol.QIDI, DetectedProtocol.UNKNOWN_MIFARE_CLASSIC -> qidiVm.writeResultOverride(ok)
                        DetectedProtocol.ANYCUBIC_ACE, DetectedProtocol.UNKNOWN_NTAG -> anycubicVm.writeResultOverride(ok)
                        DetectedProtocol.OPENSPOOL_U1 -> openSpoolVm.writeResultOverride(ok)
                        else -> {}
                    }
                }

                if (!consumedAsWrite) {
                    when (result.protocol) {
                        DetectedProtocol.QIDI -> {
                            result.qidiData?.let { qidiVm.onTagDetected(it) }
                            lastDetected = result.protocol
                            navController.navigate(Routes.QIDI)
                        }
                        DetectedProtocol.ANYCUBIC_ACE -> {
                            result.aceData?.let { anycubicVm.onTagDetected(it) }
                            lastDetected = result.protocol
                            navController.navigate(Routes.ANYCUBIC)
                        }
                        DetectedProtocol.OPENSPOOL_U1 -> {
                            result.openSpoolData?.let { openSpoolVm.onTagDetected(it) }
                            lastDetected = result.protocol
                            navController.navigate(Routes.OPENSPOOL)
                        }
                        else -> {
                            // UNKNOWN_MIFARE_CLASSIC / UNKNOWN_NTAG / BLANK — stay put.
                            // TODO: surface a toast/snackbar for "tag not recognized" once
                            // a shared snackbar host exists in K9NavHost.
                        }
                    }
                }
            }
        }
    }

    private var readerCallback: ((NfcSessionManager.DetectionResult) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        // enableReaderMode's callback fires on a worker thread, not main —
        // hop back before touching Compose state or the ViewModels.
        NfcSessionManager.enable(this) { result ->
            runOnUiThread { readerCallback?.invoke(result) }
        }
    }

    override fun onPause() {
        super.onPause()
        NfcSessionManager.disable(this)
    }
}
