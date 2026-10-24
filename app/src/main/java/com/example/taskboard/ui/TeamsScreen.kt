@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskboard.TeamDto
import com.example.taskboard.vm.TeamsUiState

@Composable
fun TeamsScreen(
    state: TeamsUiState,
    onRefresh: () -> Unit,
    onOpenTeam: (Int) -> Unit,
    onCreateTeam: (name: String, description: String) -> Unit,
    onConsumeMessage: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    var creating by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { onRefresh() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onConsumeMessage()
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My teams") }) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Default.Add, contentDescription = "New team")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingBox()
                state.error != null -> ErrorBox(state.error, onRefresh)
                state.teams.isEmpty() -> EmptyState("No teams yet. Tap + to create the first one.", Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.teams, key = { it.id }) { team -> TeamCard(team) { onOpenTeam(team.id) } }
                }
            }
        }
    }

    if (creating) {
        NewTeamDialog(
            onDismiss = { creating = false },
            onCreate = { name, description ->
                creating = false
                onCreateTeam(name, description)
            }
        )
    }
}

@Composable
private fun TeamCard(team: TeamDto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(team.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    team.myRole.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (team.myRole == "owner") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!team.description.isNullOrBlank()) {
                Text(team.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${team.membersCount} member${if (team.membersCount == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun NewTeamDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 2)
            }
        },
        confirmButton = { Button(onClick = { onCreate(name, description) }, enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
