package com.joethebuilder.k9.ui.screens.anycubic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.ui.components.ColorSwatch
import com.joethebuilder.k9.viewmodel.AnycubicViewModel

/** Port of drawAnycubicCustomColor() — R/G/B sliders, live swatch + hex, matching firmware's steppers. */
@Composable
fun AnycubicCustomColorScreen(viewModel: AnycubicViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val startR by viewModel.customR.collectAsState()
    val startG by viewModel.customG.collectAsState()
    val startB by viewModel.customB.collectAsState()

    var r by remember { mutableStateOf(startR) }
    var g by remember { mutableStateOf(startG) }
    var b by remember { mutableStateOf(startB) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Customize Color") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ColorSwatch(r, g, b, modifier = Modifier.fillMaxWidth().height(56.dp))
            Text("FF%02X%02X%02X".format(r, g, b), style = MaterialTheme.typography.titleMedium)

            ChannelSlider("RED", r) { r = it }
            ChannelSlider("GREEN", g) { g = it }
            ChannelSlider("BLUE", b) { b = it }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("BACK") }
                Button(
                    onClick = {
                        viewModel.setCustomColor(r, g, b)
                        onSaved()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("OK") }
            }
        }
    }
}

@Composable
private fun ChannelSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("$label: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..255f
        )
    }
}
