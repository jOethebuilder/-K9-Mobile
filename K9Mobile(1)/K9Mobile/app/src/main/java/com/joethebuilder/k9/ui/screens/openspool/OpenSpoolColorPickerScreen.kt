package com.joethebuilder.k9.ui.screens.openspool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel

/** Port of drawOpenSpoolColorPicker(). */
@Composable
fun OpenSpoolColorPickerScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val colIdx by viewModel.colIdx.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Color") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SelectableList(
            items = QidiData.colors.drop(1),
            selectedIndex = colIdx - 1,
            onSelect = { idx -> viewModel.setColor(idx + 1); onBack() },
            label = { it.label },
            swatch = { c -> ColorSwatch(c.r, c.g, c.b) },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
