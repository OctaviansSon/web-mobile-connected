package com.example.taskboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskboard.*

/** Accent color per DevOps topic (same colors as the web app). */
fun Category.color(): Color = when (this) {
    Category.LINUX -> Color(0xFFFBBF24)
    Category.NETWORKING -> Color(0xFFA78BFA)
    Category.DOCKER -> Color(0xFF38BDF8)
    Category.KUBERNETES -> Color(0xFF3B82F6)
    Category.CICD -> Color(0xFF34D399)
    Category.AWS -> Color(0xFFFB923C)
    Category.TERRAFORM -> Color(0xFFC084FC)
    Category.MONITORING -> Color(0xFFF472B6)
    Category.DEVSECOPS -> Color(0xFFF87171)
    Category.OTHER -> Color(0xFF94A3B8)
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = modifier)
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
        Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(priority.label, style = MaterialTheme.typography.labelSmall)
    }
}

/** Clickable pill with the pipeline stage; a click moves the task to the next stage. */
@Composable
fun StatusPill(status: TaskStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        TaskStatus.BACKLOG -> cs.surfaceVariant to cs.onSurfaceVariant
        TaskStatus.IN_PROGRESS -> cs.tertiaryContainer to cs.onTertiaryContainer
        TaskStatus.DONE -> cs.primaryContainer to cs.onPrimaryContainer
    }
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = container, contentColor = content, modifier = modifier) {
        Text(
            "● ${status.label}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TaskCard(
    task: TaskDto,
    assigneeName: String?,
    onCycleStatus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = task.category.toCategory()
    val status = task.status.toStatus()
    val done = status == TaskStatus.DONE
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(category.color()))
            Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(category.label, style = MaterialTheme.typography.labelMedium, color = category.color())
                    PriorityBadge(task.priority.toPriority())
                    if (task.dueDate != null) {
                        Text(
                            "⏱ ${task.dueDate}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (task.isOverdue()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(status, onCycleStatus)
                    Spacer(Modifier.weight(1f))
                    if (assigneeName != null) {
                        Text("@$assigneeName", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Search tasks"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

/** selected == null means "all stages". */
@Composable
fun StatusFilterRow(selected: TaskStatus?, onSelect: (TaskStatus?) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        TaskStatus.values().forEach { s ->
            FilterChip(selected = selected == s, onClick = { onSelect(s) }, label = { Text(s.label) })
        }
    }
}

/** selected == null means "any topic". */
@Composable
fun CategoryFilterRow(selected: Category?, onSelect: (Category?) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Any topic") })
        }
        items(Category.values().toList()) { c ->
            FilterChip(selected = selected == c, onClick = { onSelect(c) }, label = { Text(c.label) })
        }
    }
}

@Composable
fun ProgressRow(label: String, done: Int, total: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("$done / $total", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done.toFloat() / total },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Command snippet with a copy button; copying itself is done by the caller. */
@Composable
fun CheatCard(item: CheatItem, onCopy: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(item.topic.label, style = MaterialTheme.typography.labelMedium, color = item.topic.color())
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    item.command,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCopy) { Text("Copy") }
            }
        }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onRetry) { Text("Retry") }
    }
}
