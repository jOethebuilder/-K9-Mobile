package com.joethebuilder.k9.ui.screens.qidi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.QidiViewModel

/**
 * Port of drawQidiEntry(). Toggles between edit-fields view and read-result
 * view based on showingRead, exactly like qidiEntryShowingRead in firmware.
 */
@Composable
fun QidiEntryScreen(
    viewModel: QidiViewModel,
    onBack: () -> Unit,
    onOpenMaterialPicker: () -> Unit,
    onOpenColorPicker: () -> Unit
) {
    val mfgCode by viewModel.mfgCode.collectAsState()
    val matIdx by viewModel.matCodeIdx.collectAsState()
    val colIdx by viewModel.colIdx.collectAsState()
    val showingRead by viewModel.showingRead.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()
    val armed by remember { NfcFlowState.armed }
    val armedForRead by remember { NfcFlowState.armedForRead }

    DisposableEffect(Unit) { onDispose { viewModel.cancelArm() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — QIDI Entry") },
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
                Text("Manufacturer", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mfgCode == 0,
                        onClick = { if (mfgCode != 0) viewModel.toggleManufacturer() },
                        label = { Text("Generic") }
                    )
                    FilterChip(
                        selected = mfgCode == 1,
                        onClick = { if (mfgCode != 1) viewModel.toggleManufacturer() },
                        label = { Text("QIDI") }
                    )
                }

                OutlinedButton(onClick = onOpenMaterialPicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Material: ${QidiData.materialName(QidiData.materialCodes[matIdx])}")
                }

                val color = QidiData.colors[colIdx]
                OutlinedButton(onClick = onOpenColorPicker, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSwatch(color.r, color.g, color.b)
                        Text("Color: ${color.label}")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            writeResult?.let {
                Text(
                    if (it) "Write OK" else "Write failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("BACK") }
                Button(
                    onClick = { viewModel.armWrite() },
                    enabled = !armed,
                    modifier = Modifier.weight(1f)
                ) { Text(if (armed && !armedForRead) "Hold tag…" else "SAVE") }
                OutlinedButton(
                    onClick = { viewModel.armEntryRead() },
                    enabled = !armed,
                    modifier = Modifier.weight(1f)
                ) { Text(if (armed && armedForRead) "Hold tag…" else "READ") }
            }
        }
    }
}
