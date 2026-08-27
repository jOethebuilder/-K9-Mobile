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

/**
 * Port of drawOpenSpoolMaterialPicker(). Firmware routes to the subtype
 * picker next if the chosen material has subtypes (PLA/PETG), else straight
 * back to the entry screen — same branch here via onNeedsSubtype.
 */
@Composable
fun OpenSpoolMaterialPickerScreen(
    viewModel: OpenSpoolViewModel,
    onBack: () -> Unit,
    onNeedsSubtype: () -> Unit
) {
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
            onSelect = { idx ->
                viewModel.setMaterial(idx)
                if (OpenSpoolData.materialHasSubtypes(idx)) onNeedsSubtype() else onBack()
            },
            label = { it.name },
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        )
    }
}
