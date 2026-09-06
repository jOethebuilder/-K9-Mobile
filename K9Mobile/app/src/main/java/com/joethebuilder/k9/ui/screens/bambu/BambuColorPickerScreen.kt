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
import com.joethebuilder.k9.protocol.QidiData
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.viewmodel.BambuViewModel

@Composable
fun BambuColorPickerScreen(viewModel: BambuViewModel, onBack: () -> Unit) {
    val colIdx by viewModel.colIdx.collectAsState()
    // Index 0 is "Unknown" — excluded from picker, same as every other color picker in the app.
    val selectable = QidiData.colors.drop(1)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Select Color") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(selectable.size) { i ->
                val idx = i + 1
                val c = selectable[i]
                val selected = idx == colIdx
                ListItem(
                    leadingContent = { ColorSwatch(c.r, c.g, c.b) },
                    headlineContent = { Text(c.label) },
                    trailingContent = { if (selected) Text("✓") },
                    modifier = Modifier.clickable {
                        viewModel.setColor(idx)
                        onBack()
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
