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
import com.joethebuilder.k9.protocol.OpenSpoolData
import com.joethebuilder.k9.viewmodel.BambuViewModel

@Composable
fun BambuSubtypePickerScreen(viewModel: BambuViewModel, onBack: () -> Unit) {
    val matIdx by viewModel.matIdx.collectAsState()
    val subIdx by viewModel.subIdx.collectAsState()
    val list = OpenSpoolData.subtypeList(matIdx)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Subtype") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(list.size) { idx ->
                val selected = idx == subIdx
                ListItem(
                    headlineContent = { Text(list[idx]) },
                    trailingContent = { if (selected) Text("✓") },
                    modifier = Modifier.clickable {
                        viewModel.setSubtype(idx)
                        onBack()
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
