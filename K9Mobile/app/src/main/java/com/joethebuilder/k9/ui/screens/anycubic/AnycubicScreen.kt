package com.joethebuilder.k9.ui.screens.anycubic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.nfc.WriteArmState
import com.joethebuilder.k9.protocol.AceData
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.ui.components.TagResultCard
import com.joethebuilder.k9.viewmodel.AnycubicViewModel

@Composable
fun AnycubicScreen(viewModel: AnycubicViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matIdx.collectAsState()
    val sizeIdx by viewModel.sizeIdx.collectAsState()
    val colorIdx by viewModel.colorIdx.collectAsState()
    val isCustom by viewModel.isCustom.collectAsState()
    val customR by viewModel.customR.collectAsState()
    val customG by viewModel.customG.collectAsState()
    val customB by viewModel.customB.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val writeResult by viewModel.writeResult.collectAsState()
    val armed by remember { WriteArmState.armed }

    var showCustom by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Anycubic") },
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

            Text("Material", style = MaterialTheme.typography.titleMedium)
            SelectableList(
                items = OpenSpoolData.materials,
                selectedIndex = matIdx,
                onSelect = viewModel::setMaterial,
                label = { it.name },
                modifier = Modifier.heightIn(max = 150.dp)
            )

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

            Text("Color", style = MaterialTheme.typography.titleMedium)
            if (isCustom) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorSwatch(customR, customG, customB)
                    Text("FF%02X%02X%02X".format(customR, customG, customB))
                    TextButton(onClick = { showCustom = true }) { Text("Edit") }
                }
            } else {
                SelectableList(
                    items = AceData.presetColors,
                    selectedIndex = colorIdx,
                    onSelect = viewModel::setPresetColor,
                    label = { "FF%02X%02X%02X".format(it.first, it.second, it.third) },
                    swatch = { c -> ColorSwatch(c.first, c.second, c.third) },
                    modifier = Modifier.heightIn(max = 200.dp)
                )
                TextButton(onClick = {
                    viewModel.startCustomFromPreset()
                    showCustom = true
                }) { Text("Customize…") }
            }

            Button(onClick = { viewModel.armWrite() }, enabled = !armed, modifier = Modifier.fillMaxWidth()) {
                Text(if (armed) "Hold tag near phone…" else "WRITE")
            }

            writeResult?.let {
                Text(
                    if (it) "Write OK" else "Write failed",
                    color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showCustom) {
        var r by remember { mutableStateOf(customR) }
        var g by remember { mutableStateOf(customG) }
        var b by remember { mutableStateOf(customB) }
        AlertDialog(
            onDismissRequest = { showCustom = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCustomColor(r, g, b)
                    showCustom = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCustom = false }) { Text("Cancel") } },
            title = { Text("Customize Color") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ColorSwatch(r, g, b, modifier = Modifier.fillMaxWidth().height(48.dp))
                    ChannelSlider("Red", r) { r = it }
                    ChannelSlider("Green", g) { g = it }
                    ChannelSlider("Blue", b) { b = it }
                }
            }
        )
    }
}

@Composable
private fun ChannelSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("$label: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..255f
        )
    }
}
