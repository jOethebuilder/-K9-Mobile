package com.joethebuilder.k9.ui.screens.anycubic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.ui.components.SelectableList
import com.joethebuilder.k9.viewmodel.AnycubicViewModel

/** Port of drawAnycubicMaterialPicker(). */
@Composable
fun AnycubicMaterialPickerScreen(viewModel: AnycubicViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matIdx.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Material") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SelectableList(
            items = OpenSpoolData.materials,
            selectedIndex = matIdx,
            onSelect = { idx -> viewModel.setMaterial(idx); onBack() },
            label = { it.name },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
