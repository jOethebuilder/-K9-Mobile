package com.joethebuilder.k9.ui.screens.spoolman

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Placeholder — real U1/QIDI tabbed slot UI comes next. */
@Composable
fun FilamentManagerScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Filament manager", style = MaterialTheme.typography.headlineMedium)
        Text("Coming soon")
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("BACK")
        }
    }
}
