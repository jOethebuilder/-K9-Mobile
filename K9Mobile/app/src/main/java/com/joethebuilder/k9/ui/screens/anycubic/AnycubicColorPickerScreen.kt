package com.joethebuilder.k9.ui.screens.anycubic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.AceData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.viewmodel.AnycubicViewModel

/** Port of drawAnycubicColorPicker() — includes the CUSTOM button routing to AnycubicCustomColorScreen. */
@Composable
fun AnycubicColorPickerScreen(
    viewModel: AnycubicViewModel,
    onBack: () -> Unit,
    onOpenCustom: () -> Unit
) {
    val colorIdx by viewModel.colorIdx.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Color") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            SelectableList(
                items = AceData.presetColors,
                selectedIndex = colorIdx,
                onSelect = { idx -> viewModel.setPresetColor(idx); onBack() },
                label = { "FF%02X%02X%02X".format(it.first, it.second, it.third) },
                swatch = { c -> ColorSwatch(c.first, c.second, c.third) },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.startCustomFromPreset()
                    onOpenCustom()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("CUSTOM") }
        }
    }
}
