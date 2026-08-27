package com.joethebuilder.k9.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel

/** Port of drawU1Connection(). Reuses OpenSpoolViewModel since it already owns u1Host state. */
@Composable
fun U1ConnectionScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val savedHost by viewModel.u1Host.collectAsState()
    val testResult by viewModel.u1TestResult.collectAsState()
    var hostInput by remember(savedHost) { mutableStateOf(savedHost) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — U1 Connection") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = hostInput,
                onValueChange = { hostInput = it },
                label = { Text("U1 host or IP") },
                placeholder = { Text("(not set)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.saveU1Host(hostInput) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("SAVE") }

            Spacer(Modifier.height(8.dp))

            when (testResult) {
                true -> Text("CONNECTED", color = MaterialTheme.colorScheme.primary)
                false -> Text("CONNECTION FAILED", color = MaterialTheme.colorScheme.error)
                null -> Text("Not tested yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { viewModel.testU1Connection() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("TEST CONNECTION") }
        }
    }
}
