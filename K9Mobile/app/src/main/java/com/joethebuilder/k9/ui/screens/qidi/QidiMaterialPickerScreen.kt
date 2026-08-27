package com.joethebuilder.k9.ui.screens.qidi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.viewmodel.QidiViewModel

/** Port of drawQidiMaterialPicker() — paged grid on firmware, scrollable list here. */
@Composable
fun QidiMaterialPickerScreen(viewModel: QidiViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matCodeIdx.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Material") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SelectableList(
            items = QidiData.materialCodes,
            selectedIndex = matIdx,
            onSelect = { idx -> viewModel.setMaterial(idx); onBack() },
            label = { code -> QidiData.materialName(code) },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
