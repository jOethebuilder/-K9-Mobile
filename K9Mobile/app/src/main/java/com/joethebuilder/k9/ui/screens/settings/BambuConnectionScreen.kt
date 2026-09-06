package com.joethebuilder.k9.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.viewmodel.BambuViewModel

/**
 * Mirrors U1ConnectionScreen's pattern, expanded to 3 fields since Bambu
 * needs host + serial number + LAN access code (the U1 only needs a host).
 * Access code is masked since it's effectively a password (Settings > WLAN
 * > Access Code on the printer).
 */
@Composable
fun BambuConnectionScreen(viewModel: BambuViewModel, onBack: () -> Unit) {
    val savedHost by viewModel.bambuHost.collectAsState()
    val savedSerial by viewModel.bambuSerial.collectAsState()
    val savedCode by viewModel.bambuAccessCode.collectAsState()
    val testResult by viewModel.bambuTestResult.collectAsState()

    var host by remember(savedHost) { mutableStateOf(savedHost) }
    var serial by remember(savedSerial) { mutableStateOf(savedSerial) }
    var code by remember(savedCode) { mutableStateOf(savedCode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Bambu Connection") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it; viewModel.saveBambuHost(it) },
                label = { Text("Printer IP address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = serial,
                onValueChange = { serial = it; viewModel.saveBambuSerial(it) },
                label = { Text("Serial number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it; viewModel.saveBambuAccessCode(it) },
                label = { Text("LAN access code") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                "Printer > Settings > WLAN > Access Code. Requires LAN Mode + " +
                    "Developer Mode enabled on the printer (not LAN-Only Mode).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { viewModel.testBambuConnection() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("TEST CONNECTION") }

            when (testResult) {
                true -> Text("CONNECTED", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                false -> Text("CONNECTION FAILED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                null -> Text("Not tested yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
