package com.joethebuilder.k9.ui.screens.settings

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.network.PrefsRepository
import kotlinx.coroutines.launch

/**
 * Port of drawSettings(). Firmware order: WIFI, U1 CONNECTION, BACKLIGHT,
 * NFC STATUS, TOUCH CALIBRATION, FIRMWARE INFO, FACTORY RESET, BACK.
 *
 * BACKLIGHT and TOUCH CALIBRATION are dropped — phones don't need screen
 * dimming or resistive-touch calibration the way the CYD board does.
 * WIFI opens Android's own system WiFi settings instead of rebuilding the
 * firmware's on-screen SSID/password keyboard, since the phone already
 * manages its own WiFi at the OS level (see project notes).
 */
@Composable
fun SettingsMenuScreen(
    prefs: PrefsRepository,
    onBack: () -> Unit,
    onOpenU1Connection: () -> Unit,
    onOpenNfcStatus: () -> Unit,
    onOpenFirmwareInfo: () -> Unit,
    onFactoryResetDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { context.startActivity(Intent(AndroidSettings.ACTION_WIFI_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("WIFI") }

            OutlinedButton(onClick = onOpenU1Connection, modifier = Modifier.fillMaxWidth()) {
                Text("U1 CONNECTION")
            }
            OutlinedButton(onClick = onOpenNfcStatus, modifier = Modifier.fillMaxWidth()) {
                Text("NFC STATUS")
            }
            OutlinedButton(onClick = onOpenFirmwareInfo, modifier = Modifier.fillMaxWidth()) {
                Text("APP INFO")
            }
            OutlinedButton(
                onClick = { showResetConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("FACTORY RESET") }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Factory Reset") },
            text = { Text("Erase saved settings (U1 host)? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        prefs.clearAll()
                        showResetConfirm = false
                        onFactoryResetDone()
                    }
                }) { Text("ERASE", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("CANCEL") }
            }
        )
    }
}
