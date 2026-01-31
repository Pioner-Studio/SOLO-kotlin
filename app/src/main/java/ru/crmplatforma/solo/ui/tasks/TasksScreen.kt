package ru.crmplatforma.solo.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.data.local.entity.TaskPriority
import ru.crmplatforma.solo.ui.Screen
import java.time.format.DateTimeFormatter

/**
 * TasksScreen — список задач с группировкой по секциям.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачи") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.TaskNew.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { padding ->
        if (!uiState.hasAnyTasks) {
            EmptyTasksState(
                onAddTask = { navController.navigate(Screen.TaskNew.route) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Просрочено
                if (uiState.overdueTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(
                            title = "Просрочено",
                            count = uiState.overdueTasks.size,
                            color = MaterialTheme.colorScheme.error,
                            expanded = TaskSection.OVERDUE in uiState.expandedSections,
                            onToggle = { viewModel.toggleSection(TaskSection.OVERDUE) }
                        )
                    }
                    if (TaskSection.OVERDUE in uiState.expandedSections) {
                        items(uiState.overdueTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = { viewModel.completeTask(task.id) },
                                onClick = { navController.navigate(Screen.TaskEdit.createRoute(task.id)) },
                                isOverdue = true
                            )
                        }
                    }
                }

                // Сегодня
                if (uiState.todayTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(
                            title = "Сегодня",
                            count = uiState.todayTasks.size,
                            color = MaterialTheme.colorScheme.primary,
                            expanded = TaskSection.TODAY in uiState.expandedSections,
                            onToggle = { viewModel.toggleSection(TaskSection.TODAY) }
                        )
                    }
                    if (TaskSection.TODAY in uiState.expandedSections) {
                        items(uiState.todayTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = { viewModel.completeTask(task.id) },
                                onClick = { navController.navigate(Screen.TaskEdit.createRoute(task.id)) }
                            )
                        }
                    }
                }

                // На этой неделе
                if (uiState.weekTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(
                            title = "На этой неделе",
                            count = uiState.weekTasks.size,
                            color = MaterialTheme.colorScheme.tertiary,
                            expanded = TaskSection.THIS_WEEK in uiState.expandedSections,
                            onToggle = { viewModel.toggleSection(TaskSection.THIS_WEEK) }
                        )
                    }
                    if (TaskSection.THIS_WEEK in uiState.expandedSections) {
                        items(uiState.weekTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = { viewModel.completeTask(task.id) },
                                onClick = { navController.navigate(Screen.TaskEdit.createRoute(task.id)) }
                            )
                        }
                    }
                }

                // Позже
                if (uiState.laterTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(
                            title = "Позже",
                            count = uiState.laterTasks.size,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            expanded = TaskSection.LATER in uiState.expandedSections,
                            onToggle = { viewModel.toggleSection(TaskSection.LATER) }
                        )
                    }
                    if (TaskSection.LATER in uiState.expandedSections) {
                        items(uiState.laterTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = { viewModel.completeTask(task.id) },
                                onClick = { navController.navigate(Screen.TaskEdit.createRoute(task.id)) }
                            )
                        }
                    }
                }

                // Выполнено
                if (uiState.completedTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(
                            title = "Выполнено",
                            count = uiState.completedTasks.size,
                            color = Color(0xFF22C55E),
                            expanded = TaskSection.COMPLETED in uiState.expandedSections,
                            onToggle = { viewModel.toggleSection(TaskSection.COMPLETED) }
                        )
                    }
                    if (TaskSection.COMPLETED in uiState.expandedSections) {
                        items(uiState.completedTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = { viewModel.uncompleteTask(task.id) },
                                onClick = { navController.navigate(Screen.TaskEdit.createRoute(task.id)) },
                                isCompleted = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSectionHeader(
    title: String,
    count: Int,
    color: Color,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Badge(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ) {
            Text("$count")
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    isOverdue: Boolean = false,
    isCompleted: Boolean = false
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                task.priority == TaskPriority.HIGH -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.dueAt != null || task.priority == TaskPriority.HIGH) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.dueAt?.let { dueAt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isOverdue)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dueAt.format(dateFormatter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOverdue)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (task.priority == TaskPriority.HIGH) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "Высокий приоритет",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTasksState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Нет задач",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавьте задачи, чтобы ничего не забыть",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить задачу")
            }
        }
    }
}
