package com.joethebuilder.k9.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Port of drawFirmwareInfo(). ESP32 fields map to their nearest phone
 * equivalent: chip model -> device model, free heap -> free JVM memory,
 * flash size -> internal storage isn't meaningfully comparable so it's
 * dropped; app version stands in for firmware version.
 */
@Composable
fun FirmwareInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) { "unknown" }
    }
    val runtime = Runtime.getRuntime()
    val freeMemMb = (runtime.freeMemory() / (1024 * 1024))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — App Info") },
                navigationIcon = { androidx.compose.material3.TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Card(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("VERSION", style = MaterialTheme.typography.labelMedium)
                Text("K-9 Mobile  v$versionName", style = MaterialTheme.typography.titleMedium)

                Text("DEVICE", style = MaterialTheme.typography.labelMedium)
                Text("${Build.MANUFACTURER} ${Build.MODEL}")

                Text("ANDROID", style = MaterialTheme.typography.labelMedium)
                Text("API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")

                Text("FREE MEMORY", style = MaterialTheme.typography.labelMedium)
                Text("$freeMemMb MB")
            }
        }
    }
}
