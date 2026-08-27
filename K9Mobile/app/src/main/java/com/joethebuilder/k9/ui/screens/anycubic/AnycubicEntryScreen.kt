package com.joethebuilder.k9.ui.screens.anycubic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.NfcFlowState
import com.joethebuilder.k9.protocol.AceData
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.AnycubicViewModel

/** Port of drawAnycubicEntry(). */
@Composable
fun AnycubicEntryScreen(
    viewModel: AnycubicViewModel,
    onBack: () -> Unit,
    onOpenMaterialPicker: () -> Unit,
    onOpenColorPicker: () -> Unit
) {
    val matIdx by viewModel.matIdx.collectAsState()
    val sizeIdx by viewModel.sizeIdx.collectAsState()
    val colorIdx by viewModel.colorIdx.collectAsState()
    val isCustom by viewModel.isCustom.collectAsState()
    val customR by viewModel.customR.collectAsState()
    val customG by viewModel.customG.collectAsState()
    val customB by viewModel.customB.collectAsState()
    val showingRead by viewModel.showingRead.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()
    val armed by remember { NfcFlowState.armed }
    val armedForRead by remember { NfcFlowState.armedForRead }

    DisposableEffect(Unit) { onDispose { viewModel.cancelArm() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Anycubic Entry") },
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
                OutlinedButton(onClick = onOpenMaterialPicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Material: ${OpenSpoolData.materials[matIdx].name}")
                }

                Text("Spool Size", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OpenSpoolData.aceWeightLabels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = sizeIdx == idx,
                            onClick = { viewModel.setSize(idx) },
                            label = { Text(label) }
                        )
                    }
                }

                val (r, g, b) = if (isCustom) Triple(customR, customG, customB) else AceData.presetColors[colorIdx]
                OutlinedButton(onClick = onOpenColorPicker, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSwatch(r, g, b)
                        Text("Color: FF%02X%02X%02X".format(r, g, b))
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
