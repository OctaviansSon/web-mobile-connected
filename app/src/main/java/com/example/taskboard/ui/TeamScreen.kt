@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskboard.*
import com.example.taskboard.vm.NEW_TASK
import com.example.taskboard.vm.TeamUiState

@Composable
fun TeamScreen(
    state: TeamUiState,
    myUserId: Int,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStatusFilter: (TaskStatus?) -> Unit,
    onCategoryFilter: (Category?) -> Unit,
    onCycleStatus: (TaskDto) -> Unit,
    onOpenTask: (Int) -> Unit,
    onLoadStats: () -> Unit,
    onAddMember: (String) -> Unit,
    onRemoveMember: (Int) -> Unit,
    onSaveTeam: (String, String) -> Unit,
    onDeleteTeam: () -> Unit,
    onConsumeMessage: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { onReload() }                       // refresh when the screen is opened / returned to
    LaunchedEffect(tab) { if (tab == 2) onLoadStats() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onConsumeMessage()
        }
    }
    LaunchedEffect(state.closed) { if (state.closed) onBack() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.team?.name ?: "Team") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (tab == 0 && state.team != null) {
                FloatingActionButton(onClick = { onOpenTask(NEW_TASK) }) {
                    Icon(Icons.Default.Add, contentDescription = "New task")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                listOf("Board", "Members", "Stats").forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                }
            }
            val team = state.team
            when {
                state.loading -> LoadingBox()
                team == null -> ErrorBox(state.error ?: "Could not load the team", onReload)
                tab == 0 -> BoardTab(state, team, onQueryChange, onStatusFilter, onCategoryFilter, onCycleStatus, onOpenTask)
                tab == 1 -> MembersTab(team, myUserId, onAddMember, onRemoveMember, onSaveTeam, onDeleteTeam)
                else -> StatsTab(state.stats, state.statsError, onLoadStats)
            }
        }
    }
}

@Composable
private fun BoardTab(
    state: TeamUiState,
    team: TeamDetailDto,
    onQueryChange: (String) -> Unit,
    onStatusFilter: (TaskStatus?) -> Unit,
    onCategoryFilter: (Category?) -> Unit,
    onCycleStatus: (TaskDto) -> Unit,
    onOpenTask: (Int) -> Unit
) {
    val names = team.members.associate { it.userId to it.username }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(4.dp))
        SearchField(state.query, onQueryChange)
        StatusFilterRow(state.statusFilter, onStatusFilter)
        CategoryFilterRow(state.category, onCategoryFilter)
        if (state.tasks.isEmpty()) {
            EmptyState("No tasks here. Tap + to add one.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(state.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        assigneeName = task.assigneeId?.let { names[it] },
                        onCycleStatus = { onCycleStatus(task) },
                        onClick = { onOpenTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersTab(
    team: TeamDetailDto,
    myUserId: Int,
    onAddMember: (String) -> Unit,
    onRemoveMember: (Int) -> Unit,
    onSaveTeam: (String, String) -> Unit,
    onDeleteTeam: () -> Unit
) {
    val isOwner = team.myRole == "owner"
    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable(team.id) { mutableStateOf(team.name) }
    var description by rememberSaveable(team.id) { mutableStateOf(team.description ?: "") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle("Members (${team.members.size})") }
        items(team.members, key = { it.userId }) { member ->
            val isMe = member.userId == myUserId
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(member.username + if (isMe) " (you)" else "", style = MaterialTheme.typography.bodyLarge)
                        Text(member.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(member.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (member.role != "owner" && (isOwner || isMe)) {
                        TextButton(onClick = { onRemoveMember(member.userId) }) { Text(if (isMe) "Leave" else "Remove") }
                    }
                }
            }
        }

        if (isOwner) {
            item { SectionTitle("Invite by e-mail") }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("teammate@example.com") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { onAddMember(email); email = "" }, enabled = email.contains("@")) { Text("Add") }
                }
            }
            item { SectionTitle("Team settings") }
            item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth()) }
            item { Button(onClick = { onSaveTeam(name, description) }, enabled = name.isNotBlank()) { Text("Save changes") } }
            item {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete team") }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete team?") },
            text = { Text("All tasks of \"${team.name}\" will be deleted for every member.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteTeam() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatsTab(stats: StatsDto?, error: String?, onRetry: () -> Unit) {
    when {
        error != null && stats == null -> ErrorBox(error, onRetry)
        stats == null -> LoadingBox()
        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$ devops-study --status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("${stats.progressPercent.toInt()}%", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${stats.doneTasks} of ${stats.totalTasks} tasks shipped" +
                            if (stats.overdueTasks > 0) " · ${stats.overdueTasks} overdue" else "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    LinearProgressIndicator(progress = { (stats.progressPercent / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
                }
            }

            SectionTitle("Pipeline")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskStatus.values().forEach { s ->
                    StatCard(s.label, "${stats.byStatus[s.apiValue] ?: 0}", Modifier.weight(1f))
                }
            }

            SectionTitle("Certification goals")
            stats.certifications.forEach { ProgressRow(it.name, it.done, it.total) }

            SectionTitle("By member")
            stats.byMember.forEach { ProgressRow(it.username, it.done, it.assigned) }

            val topics = Category.values().filter { (stats.byCategory[it.apiValue] ?: 0) > 0 }
            if (topics.isNotEmpty()) {
                SectionTitle("By topic")
                topics.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.label, color = c.color(), modifier = Modifier.weight(1f))
                        Text("${stats.byCategory[c.apiValue] ?: 0}")
                    }
                }
            }
        }
    }
}
