package com.joethebuilder.k9.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joethebuilder.k9.protocol.HelpContent

@Composable
fun HelpMenuScreen(onBack: () -> Unit, onOpenTopic: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-9 — Help") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(HelpContent.topics.size) { idx ->
                val topic = HelpContent.topics[idx]
                ListItem(
                    headlineContent = { Text(topic.title) },
                    modifier = Modifier.clickable {
                        HelpSelection.current = topic
                        onOpenTopic()
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
