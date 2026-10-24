@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskboard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskboard.*
import com.example.taskboard.vm.TaskEditUiState

private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")

@Composable
fun TaskEditScreen(
    state: TaskEditUiState,
    isNew: Boolean,
    onBack: () -> Unit,
    onSave: (TaskBody) -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    LaunchedEffect(state.done) { if (state.done) onBack() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNew) "New task" else "Edit task") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingBox()
                state.members.isEmpty() -> ErrorBox(state.error ?: "Could not load the team", onRetry)
                !isNew && state.task == null -> ErrorBox(state.error ?: "Task not found", onRetry)
                else -> TaskForm(state, isNew, onSave, onDelete)
            }
        }
    }
}

/** Form fields are UI state (rememberSaveable, keyed by task id); saving goes up to the ViewModel. */
@Composable
private fun TaskForm(state: TaskEditUiState, isNew: Boolean, onSave: (TaskBody) -> Unit, onDelete: () -> Unit) {
    val initial = state.task
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title ?: "") }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description ?: "") }
    var category by rememberSaveable(initial?.id) { mutableStateOf(initial?.category?.toCategory() ?: Category.OTHER) }
    var priority by rememberSaveable(initial?.id) { mutableStateOf(initial?.priority?.toPriority() ?: Priority.MEDIUM) }
    var status by rememberSaveable(initial?.id) { mutableStateOf(initial?.status?.toStatus() ?: TaskStatus.BACKLOG) }
    var dueDate by rememberSaveable(initial?.id) { mutableStateOf(initial?.dueDate ?: "") }
    var assignee by rememberSaveable(initial?.id) { mutableStateOf(initial?.assigneeId) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val dueValid = dueDate.isBlank() || DATE_REGEX.matches(dueDate)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

        SectionTitle("Topic")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.values().forEach { c ->
                FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.label) })
            }
        }
        SectionTitle("Priority")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.values().forEach { p ->
                FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p.label) })
            }
        }
        SectionTitle("Stage")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskStatus.values().forEach { s ->
                FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s.label) })
            }
        }
        OutlinedTextField(
            value = dueDate, onValueChange = { dueDate = it },
            label = { Text("Due date (YYYY-MM-DD, optional)") }, singleLine = true,
            isError = !dueValid, modifier = Modifier.fillMaxWidth()
        )
        SectionTitle("Assignee")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = assignee == null, onClick = { assignee = null }, label = { Text("Unassigned") })
            state.members.forEach { m ->
                FilterChip(selected = assignee == m.userId, onClick = { assignee = m.userId }, label = { Text(m.username) })
            }
        }

        if (state.error != null) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                onSave(
                    TaskBody(
                        title = title.trim(),
                        description = description,
                        category = category.apiValue,
                        priority = priority.apiValue,
                        status = status.apiValue,
                        dueDate = dueDate.ifBlank { null },
                        assigneeId = assignee
                    )
                )
            },
            enabled = title.isNotBlank() && dueValid && !state.saving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isNew) "Create task" else "Save changes") }

        if (!isNew) {
            OutlinedButton(
                onClick = { confirmDelete = true },
                enabled = !state.saving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete task") }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
