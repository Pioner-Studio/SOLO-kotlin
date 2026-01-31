package ru.crmplatforma.solo.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.TaskPriority
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TaskEditorScreen — создание и редактирование задач.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    navController: NavController,
    taskId: String? = null,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (taskId == null) "Новая задача" else "Редактировать")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !isLoading && viewModel.isValid()
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Заголовок
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.setTitle(it) },
                    label = { Text("Задача *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.title.isBlank(),
                    placeholder = { Text("Что нужно сделать?") }
                )

                // Описание
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    placeholder = { Text("Подробности...") }
                )

                HorizontalDivider()

                // Приоритет
                Text(
                    text = "Приоритет",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.priority == TaskPriority.LOW,
                        onClick = { viewModel.setPriority(TaskPriority.LOW) },
                        label = { Text("Низкий") }
                    )
                    FilterChip(
                        selected = uiState.priority == TaskPriority.NORMAL,
                        onClick = { viewModel.setPriority(TaskPriority.NORMAL) },
                        label = { Text("Обычный") }
                    )
                    FilterChip(
                        selected = uiState.priority == TaskPriority.HIGH,
                        onClick = { viewModel.setPriority(TaskPriority.HIGH) },
                        label = { Text("Высокий") },
                        leadingIcon = if (uiState.priority == TaskPriority.HIGH) {
                            { Icon(Icons.Default.PriorityHigh, contentDescription = null, Modifier.size(16.dp)) }
                        } else null
                    )
                }

                HorizontalDivider()

                // Срок выполнения
                Text(
                    text = "Срок",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.dueDate != null) {
                    // Показываем выбранную дату
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    uiState.dueDate!!.format(
                                        DateTimeFormatter.ofPattern("d MMM yyyy")
                                    )
                                )
                            }
                        }

                        OutlinedCard(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    uiState.dueTime?.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ) ?: "Весь день"
                                )
                            }
                        }
                    }

                    TextButton(onClick = { viewModel.clearDueDate() }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Убрать срок")
                    }
                } else {
                    // Быстрый выбор
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { viewModel.setDueDate(LocalDate.now()) },
                            label = { Text("Сегодня") },
                            leadingIcon = { Icon(Icons.Default.Today, contentDescription = null, Modifier.size(16.dp)) }
                        )
                        AssistChip(
                            onClick = { viewModel.setDueDate(LocalDate.now().plusDays(1)) },
                            label = { Text("Завтра") }
                        )
                        AssistChip(
                            onClick = { viewModel.setDueDate(LocalDate.now().plusWeeks(1)) },
                            label = { Text("Через неделю") }
                        )
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать дату")
                    }
                }

                HorizontalDivider()

                // Напоминание
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Напоминание")
                        Text(
                            text = if (uiState.dueDate != null) "За 1 час до срока" else "Установите срок",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.hasReminder,
                        onCheckedChange = { viewModel.setHasReminder(it) },
                        enabled = uiState.dueDate != null
                    )
                }

                // Удаление
                if (taskId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.delete() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить задачу")
                    }
                }
            }
        }
    }

    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.dueDate ?: LocalDate.now())
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.setDueDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.dueTime?.hour ?: 18,
            initialMinute = uiState.dueTime?.minute ?: 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDueTime(
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
