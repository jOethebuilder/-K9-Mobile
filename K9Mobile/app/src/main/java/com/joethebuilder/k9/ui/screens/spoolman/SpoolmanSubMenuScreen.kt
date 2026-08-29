package com.joethebuilder.k9.ui.screens.spoolman

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpoolmanSubMenuScreen(
    onBack: () -> Unit,
    onOpenFilamentManager: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Spoolman", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOpenFilamentManager, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("FILAMENT MANAGER")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("BACK")
        }
    }
}
