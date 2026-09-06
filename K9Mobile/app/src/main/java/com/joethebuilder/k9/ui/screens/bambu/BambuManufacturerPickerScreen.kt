package com.joethebuilder.k9.ui.screens.bambu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.viewmodel.BambuViewModel

@Composable
fun BambuManufacturerPickerScreen(viewModel: BambuViewModel, onBack: () -> Unit) {
    val mfgIdx by viewModel.mfgIdx.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Manufacturer") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(OpenSpoolData.manufacturers.size) { idx ->
                val selected = idx == mfgIdx
                ListItem(
                    headlineContent = { Text(OpenSpoolData.manufacturers[idx]) },
                    trailingContent = { if (selected) Text("✓") },
                    modifier = Modifier.clickable {
                        viewModel.setManufacturer(idx)
                        onBack()
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
