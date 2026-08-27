package com.joethebuilder.k9.ui.screens.openspool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel

/** Port of drawOpenSpoolSlotPicker() — 4 slot tiles -> u1SendFilamentConfig(). */
@Composable
fun OpenSpoolSlotPickerScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val sendResult by viewModel.sendResult.collectAsState()

    DisposableEffect(Unit) { onDispose { viewModel.clearSendResult() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select U1 Slot") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            (1..4).forEach { slot ->
                Button(
                    onClick = { viewModel.sendToSlot(slot) },
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) { Text("SLOT $slot") }
            }

            sendResult?.let {
                Text(
                    if (it) "Sent to printer" else "Send failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
