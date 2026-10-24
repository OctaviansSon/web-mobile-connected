@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskboard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.taskboard.*
import kotlinx.coroutines.launch

/** Command cheatsheet: local data, no server needed. Search / filter are UI-only state. */
@Composable
fun CheatsScreen(bottomBar: @Composable () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf<Category?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    val shown = remember(query, topic) {
        val q = query.trim()
        cheatSheet.filter { item ->
            (topic == null || item.topic == topic) &&
                (item.title.contains(q, ignoreCase = true) ||
                    item.command.contains(q, ignoreCase = true) ||
                    item.description.contains(q, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Command cheatsheet") }) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.height(4.dp))
            SearchField(query, { query = it }, label = "Search commands")
            CategoryFilterRow(topic, { topic = it })
            if (shown.isEmpty()) {
                EmptyState("No commands found")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(shown, key = { it.command }) { item ->
                        CheatCard(item, onCopy = {
                            clipboard.setText(AnnotatedString(item.command))
                            scope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                snackbar.showSnackbar("Copied: ${item.command}")
                            }
                        })
                    }
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

/** Study timer. The countdown runs in FocusViewModel (a coroutine), so it survives rotation. */
@Composable
fun FocusScreen(
    state: FocusState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSelectMinutes: (Int) -> Unit,
    onSelectTopic: (Category) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Focus") }) },
        bottomBar = bottomBar
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("What are you studying?")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.values().forEach { c ->
                    FilterChip(selected = state.topic == c, onClick = { onSelectTopic(c) }, enabled = !state.running, label = { Text(c.label) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 15, 25, 45).forEach { m ->
                    FilterChip(selected = state.sessionMinutes == m, onClick = { onSelectMinutes(m) }, enabled = !state.running, label = { Text("$m min") })
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                CircularProgressIndicator(
                    progress = { state.elapsedFraction },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 10.dp,
                    color = state.topic.color()
                )
                Text(formatTime(state.remainingSeconds), style = MaterialTheme.typography.displayMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onToggle) { Text(if (state.running) "Pause" else "Start") }
                OutlinedButton(onClick = onReset) { Text("Reset") }
            }
            Text("Sessions completed: ${state.completedSessions}", style = MaterialTheme.typography.bodyLarge)
            if (state.minutesByTopic.isNotEmpty()) {
                SectionTitle("Time by topic")
                state.minutesByTopic.entries.sortedByDescending { it.value }.forEach { (topic, minutes) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(topic.label, color = topic.color(), modifier = Modifier.weight(1f))
                        Text("$minutes min")
                    }
                }
            }
        }
    }
}

@Composable
fun AccountScreen(username: String, email: String, serverUrl: String, onLogout: () -> Unit, bottomBar: @Composable () -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Account") }) },
        bottomBar = bottomBar
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(username.ifBlank { "Signed in" }, style = MaterialTheme.typography.titleMedium)
                    if (email.isNotBlank()) Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Server", style = MaterialTheme.typography.labelMedium)
                    Text(serverUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Log out") }
        }
    }
}
