package com.joethebuilder.k9.ui.screens.spoolman

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
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
    var connected by remember { mutableStateOf(false) }
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
            connected = false
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
            connected = result.first.isNotEmpty()
            if (result.first.isEmpty()) errorMsg = "Couldn't read slots — check printer is on and connected"
        }
    }

    fun clearSlot(slot: Int) {
        val adapter = currentAdapter() ?: return
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                try { adapter.assignSpool(slot, 0) } catch (e: Exception) { false }
            }
            if (ok) refresh() else errorMsg = "Reset failed — check printer connection"
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
            Text("Filament Manager", style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = if (connected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (connected) "Connected" else "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { refresh() }) { Text("Read spool tags") }
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
                            onAssign = { pickerSlot = slot.slot },
                            onRefresh = { refresh() },
                            onReset = { clearSlot(slot.slot) }
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
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp)
    }
}

@Composable
private fun SlotCard(
    slotIndex: Int,
    spool: SpoolInfo?,
    onAssign: () -> Unit,
    onRefresh: () -> Unit,
    onReset: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Slot ${slotIndex + 1}", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Badge("OFFICIAL", Color(0xFFE65100))
                    Badge("SPOOLMAN", Color(0xFF6A1B9A))
                    Badge(if (spool != null) "ASSIGNED" else "EMPTY", if (spool != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (spool != null) {
                LabelValueRow("Material", spool.name)
                LabelValueRow("Spool ID", "#${spool.id}")
                LabelValueRow("Remaining", "${spool.remainingWeight.toInt()} g", valueColor = Color(0xFF2E7D32))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Color", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(80.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(parseSwatchColor(spool.color))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("#${spool.color}", style = MaterialTheme.typography.bodySmall)
                }
                LabelValueRow("Card UID", "N/A")
            } else {
                Text("Empty", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                Text("Tap Spool below to assign", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onRefresh, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Refresh", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(onClick = { /* manual entry not built yet */ }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("User", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(onClick = onReset, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onAssign, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Spool", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
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
