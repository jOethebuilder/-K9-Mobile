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

/** Port of drawOpenSpoolSubtypePicker() — only reached for PLA/PETG. */
@Composable
fun OpenSpoolSubtypePickerScreen(viewModel: OpenSpoolViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matIdx.collectAsState()
    val subIdx by viewModel.subIdx.collectAsState()
    val subtypes = OpenSpoolData.subtypeList(matIdx)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Subtype") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SelectableList(
            items = subtypes,
            selectedIndex = subIdx,
            onSelect = { idx -> viewModel.setSubtype(idx); onBack() },
            label = { it },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
