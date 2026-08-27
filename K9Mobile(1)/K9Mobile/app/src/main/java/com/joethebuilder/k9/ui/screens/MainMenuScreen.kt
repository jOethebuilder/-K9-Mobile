package com.joethebuilder.k9.ui.screens

import android.nfc.NfcAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Direct port of drawMain(): 4 vertical buttons in the same order
 * (QIDI, OPENSPOOL U1, ANYCUBIC, SETTINGS), same "no reader" warning
 * firmware shows when nfcReady is false — here, when the phone has no
 * NFC radio at all (NfcAdapter.getDefaultAdapter() == null).
 *
 * Manual protocol buttons ARE the primary way in now (not a fallback
 * alongside auto-detect) since per-screen NFC mode means auto-detect-from-
 * anywhere no longer applies the way it did in the first pass — tapping a
 * tag only does something once you're on the matching section's screen,
 * same as firmware.
 */
@Composable
fun MainMenuScreen(
    onSelectQidi: () -> Unit,
    onSelectOpenSpool: () -> Unit,
    onSelectAnycubic: () -> Unit,
    onSelectSettings: () -> Unit
) {
    val context = LocalContext.current
    val hasNfc = NfcAdapter.getDefaultAdapter(context) != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("K-9 mark 1", style = MaterialTheme.typography.headlineMedium)

        if (!hasNfc) {
            Text(
                "! NO NFC HARDWARE ON THIS DEVICE !",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onSelectQidi, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("QIDI")
        }
        Button(onClick = onSelectOpenSpool, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("OPENSPOOL U1")
        }
        Button(onClick = onSelectAnycubic, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("ANYCUBIC")
        }
        OutlinedButton(onClick = onSelectSettings, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("SETTINGS")
        }
    }
}
