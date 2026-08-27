package com.joethebuilder.k9.ui.screens.openspool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.WriteArmState
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel

@Composable
fun OpenSpoolScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matIdx.collectAsState()
    val mfgIdx by viewModel.mfgIdx.collectAsState()
    val colIdx by viewModel.colIdx.collectAsState()
    val subIdx by viewModel.subIdx.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val u1Host by viewModel.u1Host.collectAsState()
    val armed by remember { WriteArmState.armed }

    var showSlotPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — OpenSpool U1") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lastRead?.let { data ->
                Text("Last tag read", style = MaterialTheme.typography.titleMedium)
                TagResultCard(
                    manufacturer = data.manufacturer, material = data.material,
                    r = data.r, g = data.g, b = data.b, colorLabel = data.color,
                    extMin = data.extMin, extMax = data.extMax,
                    bedMin = data.bedMin, bedMax = data.bedMax,
                    uidHex = data.uidHex()
                )
            }

            Text("Manufacturer", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = OpenSpoolData.manufacturers,
                selectedIndex = mfgIdx,
                onSelect = viewModel::setManufacturer,
                label = { it },
                modifier = Modifier.heightIn(max = 150.dp)
            )

            Text("Material", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = OpenSpoolData.materials,
                selectedIndex = matIdx,
                onSelect = viewModel::setMaterial,
                label = { it.name },
                modifier = Modifier.heightIn(max = 150.dp)
            )

            if (OpenSpoolData.materialHasSubtypes(matIdx)) {
                Text("Subtype", style = MaterialTheme.typography.titleMedium)
                val subtypes = OpenSpoolData.subtypeList(matIdx)
                SelectableList(
                    items = subtypes,
                    selectedIndex = subIdx,
                    onSelect = viewModel::setSubtype,
                    label = { it },
                    modifier = Modifier.heightIn(max = 120.dp)
                )
            }

            Text("Color", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = QidiData.colors.drop(1),
                selectedIndex = colIdx - 1,
                onSelect = { idx -> viewModel.setColor(idx + 1) },
                label = { it.label },
                swatch = { c -> ColorSwatch(c.r, c.g, c.b) },
                modifier = Modifier.heightIn(max = 150.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.armWrite() }, enabled = !armed, modifier = Modifier.weight(1f)) {
                    Text(if (armed) "Hold tag…" else "WRITE TAG")
                }
                Button(
                    onClick = { showSlotPicker = true },
                    enabled = u1Host.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SEND TO U1")
                }
            }
            if (u1Host.isBlank()) {
                Text(
                    "Set a U1 host in settings to enable SEND.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            writeResult?.let {
                Text(
                    if (it) "Write OK" else "Write failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            sendResult?.let {
                Text(
                    if (it) "Sent to printer" else "Send failed — check U1 connection / slot state",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showSlotPicker) {
        AlertDialog(
            onDismissRequest = { showSlotPicker = false },
            confirmButton = {},
            title = { Text("Select U1 Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { slot ->
                        Button(
                            onClick = {
                                viewModel.sendToSlot(slot)
                                showSlotPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Slot $slot") }
                    }
                }
            }
        )
    }
}
