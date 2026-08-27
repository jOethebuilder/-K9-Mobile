package com.joethebuilder.k9.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Port of drawMain(). Manual navigation is kept even though tag tap now
 * auto-routes — useful for "browse the entry form without a tag on hand"
 * and for the Mifare/NTAG techs that don't get FLAG_READER_SKIP_NDEF_CHECK
 * treatment quite right on some phones.
 */
@Composable
fun MainMenuScreen(
    onSelectQidi: () -> Unit,
    onSelectOpenSpool: () -> Unit,
    onSelectAnycubic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("K-9 mark 1", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Tap a spool tag anytime to auto-detect, or choose a protocol below.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSelectQidi, modifier = Modifier.fillMaxWidth()) { Text("QIDI") }
        Button(onClick = onSelectOpenSpool, modifier = Modifier.fillMaxWidth()) { Text("OPENSPOOL U1") }
        Button(onClick = onSelectAnycubic, modifier = Modifier.fillMaxWidth()) { Text("ANYCUBIC") }
    }
}
