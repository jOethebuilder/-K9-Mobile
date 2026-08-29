package com.joethebuilder.k9.ui.screens.spoolman

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SpoolmanSubMenuScreen(
    onBack: () -> Unit,
    onOpenFilamentManager: () -> Unit
) {
    val context = LocalContext.current

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
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.37:7912"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SPOOLMAN")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("BACK")
        }
    }
}
