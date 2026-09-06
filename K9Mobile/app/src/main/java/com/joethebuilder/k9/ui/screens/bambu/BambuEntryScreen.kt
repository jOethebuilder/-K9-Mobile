package com.joethebuilder.k9.ui.screens.bambu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.BambuViewModel

/**
 * Mirrors OpenSpoolEntryScreen. One structural difference: SEND fires
 * directly (bambuMqtt call) instead of opening a slot picker — Bambu's
 * external spool is a single target (ams_id 255 / tray_id 254), there's
 * no multi-slot concept to pick from the way the U1 has 4 extruders.
 */
@Composable
fun BambuEntryScreen(
    viewModel: BambuViewModel,
    onBack: () -> Unit,
    onOpenManufacturerPicker: () -> Unit,
    onOpenMaterialPicker: () -> Unit,
    onOpenColorPicker: () -> Unit
) {
    val mfgIdx by viewModel.mfgIdx.collectAsState()
    val matIdx by viewModel.matIdx.collectAsState()
    val colIdx by viewModel.colIdx.collectAsState()
    val showingRead by viewModel.showingRead.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val armed by remember { NfcFlowState.armed }
    val armedForRead by remember { NfcFlowState.armedForRead }

    DisposableEffect(Unit) { onDispose { viewModel.cancelArm() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Bambu Manual Entry") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showingRead) {
                if (lastRead != null) {
                    val d = lastRead!!
                    TagResultCard(
                        manufacturer = d.manufacturer, material = d.material,
                        r = d.r, g = d.g, b = d.b, colorLabel = d.color,
                        extMin = d.extMin, extMax = d.extMax,
                        bedMin = d.bedMin, bedMax = d.bedMax,
                        uidHex = d.uidHex()
                    )
                } else {
                    Card(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text("Blank / unreadable tag")
                        }
                    }
                }
            } else {
                OutlinedButton(onClick = onOpenManufacturerPicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Manufacturer: ${OpenSpoolData.manufacturers[mfgIdx]}")
                }

                val material = OpenSpoolData.materials[matIdx]
                val matLabel = if (OpenSpoolData.materialHasSubtypes(matIdx) && viewModel.currentSubtype() != "Basic") {
                    "${material.name} - ${viewModel.currentSubtype()}"
                } else material.name
                OutlinedButton(onClick = onOpenMaterialPicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Material: $matLabel")
                }

                val color = QidiData.colors[colIdx]
                OutlinedButton(onClick = onOpenColorPicker, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSwatch(color.r, color.g, color.b)
                        Text("Color: ${color.label}")
                    }
                }

                Text(
                    "Nozzle: ${material.nozzleMin}-${material.nozzleMax} C   Bed: ${material.bedMin}-${material.bedMax} C",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.weight(1f))

            writeResult?.let {
                Text(
                    if (it) "Write OK" else "Write failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            sendResult?.let {
                Text(
                    if (it) "Sent to Bambu OK" else "Send to Bambu failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            val btnPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, contentPadding = btnPadding, modifier = Modifier.weight(1f)) {
                    Text("BACK", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = { viewModel.armWrite() },
                    enabled = !armed,
                    contentPadding = btnPadding,
                    modifier = Modifier.weight(1f)
                ) { Text(if (armed && !armedForRead) "…" else "SAVE", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                Button(
                    onClick = { viewModel.sendToBambu() },
                    contentPadding = btnPadding,
                    modifier = Modifier.weight(1f)
                ) { Text("SEND", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                OutlinedButton(
                    onClick = { viewModel.armEntryRead() },
                    enabled = !armed,
                    contentPadding = btnPadding,
                    modifier = Modifier.weight(1f)
                ) { Text(if (armed && armedForRead) "…" else "READ", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}
