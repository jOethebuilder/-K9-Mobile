package com.joethebuilder.k9.ui.screens.openspool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel

/** Port of drawOpenSpoolManufacturerPicker(). */
@Composable
fun OpenSpoolManufacturerPickerScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val mfgIdx by viewModel.mfgIdx.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Manufacturer") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SelectableList(
            items = OpenSpoolData.manufacturers,
            selectedIndex = mfgIdx,
            onSelect = { idx -> viewModel.setManufacturer(idx); onBack() },
            label = { it },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
