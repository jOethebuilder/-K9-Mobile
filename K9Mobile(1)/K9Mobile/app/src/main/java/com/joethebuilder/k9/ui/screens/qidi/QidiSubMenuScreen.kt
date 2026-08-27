package com.joethebuilder.k9.ui.screens.qidi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.ui.components.SubMenuTagDisplay
import com.joethebuilder.k9.viewmodel.QidiViewModel

/**
 * Port of drawSubMenu("K-9 — QIDI") + the SCR_QIDI switch-case handlers.
 * Auto-scan (continuous, Mifare-only while this screen is active) is driven
 * externally by MainActivity calling viewModel.onSubMenuTagDetected().
 */
@Composable
fun QidiSubMenuScreen(
    viewModel: QidiViewModel,
    onBack: () -> Unit,
    onOpenEntry: () -> Unit
) {
    val subMenuState by viewModel.subMenuState.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("K-9 — QIDI") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubMenuTagDisplay(state = subMenuState, data = lastRead)

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("BACK") }
                // Labeled WRITE to match the physical firmware button, though it
                // actually opens the manual entry form (same as firmware's handler).
                Button(onClick = onOpenEntry, modifier = Modifier.weight(1f)) { Text("WRITE") }
            }
        }
    }
}
