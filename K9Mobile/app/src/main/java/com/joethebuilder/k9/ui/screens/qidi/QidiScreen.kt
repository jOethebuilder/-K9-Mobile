package com.joethebuilder.k9.ui.screens.qidi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.WriteArmState
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.QidiViewModel

@Composable
fun QidiScreen(viewModel: QidiViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matCodeIdx.collectAsState()
    val mfgCode by viewModel.mfgCode.collectAsState()
    val colIdx by viewModel.colIdx.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — QIDI") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
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

            Text("Material", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = QidiData.materialCodes,
                selectedIndex = matIdx,
                onSelect = viewModel::setMaterial,
                label = { code -> QidiData.materialName(code) },
                modifier = Modifier.weight(1f)
            )

            Text("Color", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = QidiData.colors.drop(1), // index 0 is "Unknown", not selectable
                selectedIndex = colIdx - 1,
                onSelect = { idx -> viewModel.setColor(idx + 1) },
                label = { it.label },
                swatch = { c -> ColorSwatch(c.r, c.g, c.b) },
                modifier = Modifier.weight(1f)
            )

            val armed by remember { WriteArmState.armed }
            Button(
                onClick = { viewModel.armWrite() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !armed
            ) {
                Text(if (armed) "Hold tag near phone…" else "WRITE")
            }

            writeResult?.let {
                Text(
                    if (it) "Write OK" else "Write failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Text(
                "Tap WRITE, then hold a Mifare Classic tag to the phone's NFC antenna.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
