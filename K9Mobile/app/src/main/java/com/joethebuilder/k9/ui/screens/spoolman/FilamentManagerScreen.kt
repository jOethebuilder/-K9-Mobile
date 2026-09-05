package com.joethebuilder.k9.ui.screens.spoolman

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.network.QidiBoxSpoolmanAdapter
import com.joethebuilder.k9.network.SlotAssignment
import com.joethebuilder.k9.network.SlotSpoolAdapter
import com.joethebuilder.k9.network.SpoolInfo
import com.joethebuilder.k9.network.SpoolmanClient
import com.joethebuilder.k9.network.U1AfcAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PrinterTab { U1, QIDI }

@Composable
fun FilamentManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsRepository(context) }
    val scope = rememberCoroutineScope()

    val u1Host by prefs.u1Host.collectAsState(initial = "")
    val qidiHost by prefs.qidiHost.collectAsState(initial = "")

    var selectedTab by remember { mutableStateOf(PrinterTab.U1) }
    var slots by remember { mutableStateOf<List<SlotAssignment>>(emptyList()) }
    var spools by remember { mutableStateOf<List<SpoolInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var pickerSlot by remember { mutableStateOf<Int?>(null) }

    fun currentAdapter(): SlotSpoolAdapter? = when (selectedTab) {
        PrinterTab.U1 -> {
            val host = u1Host.ifBlank { "192.168.1.19" }
            U1AfcAdapter(moonrakerBaseUrl = "http://$host:7125")
        }
        PrinterTab.QIDI -> {
            if (qidiHost.isBlank()) null
            else QidiBoxSpoolmanAdapter(moonrakerBaseUrl = "http://$qidiHost")
        }
    }

    fun refresh() {
        val adapter = currentAdapter()
        if (adapter == null) {
            errorMsg = "QIDI host not set — add it in Settings"
            slots = emptyList()
            return
        }
        loading = true
        errorMsg = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val s = try { adapter.readSlots() } catch (e: Exception) { emptyList() }
                val sp = try { SpoolmanClient().listSpools() } catch (e: Exception) { emptyList() }
                Pair(s, sp)
            }
            slots = result.first
            spools = result.second
            loading = false
            if (result.first.isEmpty()) errorMsg = "Couldn't read slots — check printer is on and connected"
        }
    }

    LaunchedEffect(selectedTab, u1Host, qidiHost) {
        refresh()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filament manager", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == PrinterTab.U1,
                onClick = { selectedTab = PrinterTab.U1 },
                text = { Text("U1") }
            )
            Tab(
                selected = selectedTab == PrinterTab.QIDI,
                onClick = { selectedTab = PrinterTab.QIDI },
                text = { Text("QIDI") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMsg != null -> Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error)
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(slots) { slot ->
                        val spool = slot.spoolId?.let { id -> spools.firstOrNull { it.id == id } }
                        SlotCard(
                            slotIndex = slot.slot,
                            spool = spool,
                            onClick = { pickerSlot = slot.slot }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("BACK")
        }
    }

    val pickerSlotValue = pickerSlot
    if (pickerSlotValue != null) {
        SpoolPickerSheet(
            spools = spools,
            onDismiss = { pickerSlot = null },
            onPick = { spoolId ->
                val adapter = currentAdapter()
                pickerSlot = null
                if (adapter != null) {
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            try { adapter.assignSpool(pickerSlotValue, spoolId) } catch (e: Exception) { false }
                        }
                        if (ok) refresh() else errorMsg = "Assign failed — check printer connection"
                    }
                }
            }
        )
    }
}

@Composable
private fun SlotCard(slotIndex: Int, spool: SpoolInfo?, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Slot ${slotIndex + 1}", style = MaterialTheme.typography.labelLarge)
            if (spool != null) {
                Text(spool.name, style = MaterialTheme.typography.bodyLarge)
                Text("${spool.material} · ${spool.remainingWeight.toInt()} g left", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Empty", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                Text("Tap to assign", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun parseSwatchColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor("#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpoolPickerSheet(
    spools: List<SpoolInfo>,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Pick a spool", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (spools.isEmpty()) {
                Text("No spools found in Spoolman")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(spools) { spool ->
                        ListItem(
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(parseSwatchColor(spool.color))
                                )
                            },
                            headlineContent = { Text(spool.name) },
                            supportingContent = { Text("${spool.material} · ${spool.remainingWeight.toInt()} g remaining") },
                            modifier = Modifier.clickable { onPick(spool.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
