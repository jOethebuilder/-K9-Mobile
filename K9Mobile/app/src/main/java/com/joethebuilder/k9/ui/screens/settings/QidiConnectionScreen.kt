package com.joethebuilder.k9.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.network.PrefsRepository
import kotlinx.coroutines.launch

/** QIDI equivalent of U1ConnectionScreen. Saves qidiHost via PrefsRepository. */
@Composable
fun QidiConnectionScreen(prefs: PrefsRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val savedHost by prefs.qidiHost.collectAsState(initial = "")
    var hostInput by remember(savedHost) { mutableStateOf(savedHost) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — QIDI Connection") },
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
                label = { Text("QIDI host or IP") },
                placeholder = { Text("(not set)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { scope.launch { prefs.saveQidiHost(hostInput) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("SAVE") }
        }
    }
}
