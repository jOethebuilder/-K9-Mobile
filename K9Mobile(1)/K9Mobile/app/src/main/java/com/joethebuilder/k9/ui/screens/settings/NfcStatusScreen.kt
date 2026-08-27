package com.joethebuilder.k9.ui.screens.settings

import android.app.Activity
import android.nfc.NfcAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Port of drawNfcStatus(). Reader connection state maps to "does this phone
 * have NFC hardware, and is it turned on" (there's no separate PN532
 * firmware version to show on a phone — the radio IS the "reader").
 * SCAN TEST briefly enables reader mode itself (independent of the app-wide
 * per-screen mode in MainActivity) and shows whatever tag's UID it finds.
 */
@Composable
fun NfcStatusScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adapter = remember { NfcAdapter.getDefaultAdapter(context) }

    var scanning by remember { mutableStateOf(false) }
    var foundUid by remember { mutableStateOf<String?>(null) }
    var scanAttempted by remember { mutableStateOf(false) }

    DisposableEffect(scanning) {
        if (scanning && activity != null && adapter != null) {
            adapter.enableReaderMode(
                activity,
                { tag ->
                    foundUid = tag.id.joinToString(":") { b -> "%02X".format(b) }
                    scanAttempted = true
                    scanning = false
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
        }
        onDispose {
            if (activity != null) adapter?.disableReaderMode(activity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — NFC Status") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("READER", style = MaterialTheme.typography.labelLarge)
            when {
                adapter == null -> Text("NOT SUPPORTED", color = MaterialTheme.colorScheme.error)
                !adapter.isEnabled -> Text("PRESENT BUT DISABLED — turn on NFC in system settings", color = MaterialTheme.colorScheme.error)
                else -> Text("CONNECTED", color = MaterialTheme.colorScheme.primary)
            }

            Text("SCAN TEST RESULT", style = MaterialTheme.typography.labelLarge)
            when {
                scanning -> Text("Hold tag near reader...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                foundUid != null -> Text("TAG FOUND  $foundUid", color = MaterialTheme.colorScheme.primary)
                scanAttempted -> Text("NO TAG DETECTED", color = MaterialTheme.colorScheme.error)
                else -> Text("Not tested yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    foundUid = null
                    scanAttempted = false
                    scanning = true
                },
                enabled = adapter != null && adapter.isEnabled && !scanning,
                modifier = Modifier.fillMaxWidth()
            ) { Text("SCAN TEST") }
        }
    }
}
