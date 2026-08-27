package com.joethebuilder.k9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.nfc.NfcFlowState
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

    // Read by the reader-mode callback (registered once in onResume) to decide
    // routing — this replaces per-mode re-registration, since reader-mode's
    // FLAG_READER_NFC_A/B covers both Mifare Classic and NTAG21x at the radio
    // level regardless of which protocol screen is showing; the actual
    // QIDI-vs-NTAG / sub-menu-vs-ignore split happens here in software.
    @Volatile private var currentRoute: String? = Routes.MAIN

    private lateinit var qidiVm: QidiViewModel
    private lateinit var openSpoolVm: OpenSpoolViewModel
    private lateinit var anycubicVm: AnycubicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PrefsRepository(applicationContext)

        setContent {
            qidiVm = viewModel()
            openSpoolVm = viewModel(factory = OpenSpoolViewModelFactory(prefs))
            anycubicVm = viewModel()
            val navController = rememberNavController()

            val backStackEntry by navController.currentBackStackEntryAsState()
            currentRoute = backStackEntry?.destination?.route ?: Routes.MAIN

            MaterialTheme {
                Surface {
                    K9NavHost(
                        navController = navController,
                        qidiViewModel = qidiVm,
                        openSpoolViewModel = openSpoolVm,
                        anycubicViewModel = anycubicVm,
                        prefs = prefs
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Mode.NONE = deliver every detection unfiltered; routing by protocol
        // AND current screen happens in handleDetection() below, since the
        // radio-level reader-mode flags can't distinguish Mifare from NTAG.
        NfcSessionManager.enable(this, NfcSessionManager.Mode.NONE) { result ->
            runOnUiThread { handleDetection(result) }
        }
    }

    override fun onPause() {
        super.onPause()
        NfcSessionManager.disable(this)
    }

    private fun handleDetection(result: NfcSessionManager.DetectionResult) {
        // Arm-read / arm-write (from an entry screen's READ or SAVE button)
        // always takes priority over sub-menu auto-scan, matching firmware's
        // single-tag-at-a-time model.
        if (NfcFlowState.consumeIfArmed(result)) return

        when (currentRoute) {
            Routes.QIDI_SUBMENU -> {
                val isMifareFamily = result.protocol == DetectedProtocol.QIDI ||
                    result.protocol == DetectedProtocol.UNKNOWN_MIFARE_CLASSIC
                if (isMifareFamily) {
                    val data = if (result.protocol == DetectedProtocol.QIDI) result.qidiData else null
                    qidiVm.onSubMenuTagDetected(result.tag, data)
                }
            }
            Routes.OPENSPOOL_SUBMENU -> {
                val isNtagFamily = result.protocol == DetectedProtocol.OPENSPOOL_U1 ||
                    result.protocol == DetectedProtocol.ANYCUBIC_ACE ||
                    result.protocol == DetectedProtocol.UNKNOWN_NTAG
                if (isNtagFamily) {
                    val data = if (result.protocol == DetectedProtocol.OPENSPOOL_U1) result.openSpoolData else null
                    openSpoolVm.onSubMenuTagDetected(result.tag, data)
                }
            }
            Routes.ANYCUBIC_SUBMENU -> {
                val isNtagFamily = result.protocol == DetectedProtocol.OPENSPOOL_U1 ||
                    result.protocol == DetectedProtocol.ANYCUBIC_ACE ||
                    result.protocol == DetectedProtocol.UNKNOWN_NTAG
                if (isNtagFamily) {
                    val data = if (result.protocol == DetectedProtocol.ANYCUBIC_ACE) result.aceData else null
                    anycubicVm.onSubMenuTagDetected(result.tag, data)
                }
            }
            else -> {
                // Not a sub-menu screen (entry/picker/settings) and nothing was
                // armed — ignore the tap, same as firmware simply not polling
                // on those screens.
            }
        }
    }
}
