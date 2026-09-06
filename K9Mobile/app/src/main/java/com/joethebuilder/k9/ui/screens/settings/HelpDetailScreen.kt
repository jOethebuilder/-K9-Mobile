package com.joethebuilder.k9.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpDetailScreen(onBack: () -> Unit) {
    val topic = HelpSelection.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.title ?: "Help") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(topic?.body ?: "No topic selected.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
