package com.joethebuilder.k9.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Small color swatch, replaces the firmware's tft.fillRect() color preview boxes. */
@Composable
fun ColorSwatch(r: Int, g: Int, b: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(Color(r, g, b))
            .border(1.dp, MaterialTheme.colorScheme.outline)
    )
}

/**
 * Scrollable selection list — replaces the firmware's paged 3x3/4x3 grid
 * pickers (drawXMaterialPicker / drawXColorPicker). A phone screen scrolls,
 * so there's no need to paginate the way a fixed 320x240 touchscreen does.
 */
@Composable
fun <T> SelectableList(
    items: List<T>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: (T) -> String,
    swatch: (@Composable (T) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(items.size) { idx ->
            val item = items[idx]
            val selected = idx == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(idx) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatch?.invoke(item)
                Text(label(item))
            }
        }
    }
}

/** Read-result card — mirrors the firmware's tagData.hasData display block. */
@Composable
fun TagResultCard(
    manufacturer: String,
    material: String,
    r: Int, g: Int, b: Int,
    colorLabel: String,
    extMin: Int, extMax: Int,
    bedMin: Int, bedMax: Int,
    uidHex: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("MANUFACTURER: $manufacturer", style = MaterialTheme.typography.labelLarge)
            Text("MATERIAL: $material", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch(r, g, b)
                Text("COLOR: $colorLabel")
            }
            Text("NOZZLE: $extMin-$extMax C")
            Text("BED: $bedMin-$bedMax C")
            Text("UID: $uidHex", style = MaterialTheme.typography.bodySmall)
        }
    }
}
