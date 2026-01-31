package ru.crmplatforma.solo.ui.appointment

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
import ru.crmplatforma.solo.data.local.entity.AppointmentStatus
import ru.crmplatforma.solo.data.local.entity.AppointmentType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * AppointmentEditorScreen — создание и редактирование записей.
 *
 * Поддерживает 3 типа:
 * - VISIT (визит клиента)
 * - NOTE (заметка)
 * - BLOCK (блокировка времени)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentEditorScreen(
    navController: NavController,
    appointmentId: String? = null, // null = создание новой
    initialDate: LocalDate = LocalDate.now(),
    viewModel: AppointmentEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Эффект: загрузить запись если редактируем
    LaunchedEffect(appointmentId) {
        if (appointmentId != null) {
            viewModel.loadAppointment(appointmentId)
        } else {
            viewModel.initNewAppointment(initialDate)
        }
    }

    // Эффект: закрыть экран после успешного сохранения
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (appointmentId == null) "Новая запись" else "Редактировать")
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
                // Тип записи
                AppointmentTypeSelector(
                    selectedType = uiState.type,
                    onTypeChange = { viewModel.setType(it) },
                    enabled = appointmentId == null // Тип нельзя менять при редактировании
                )

                HorizontalDivider()

                // Дата и время
                DateTimeSection(
                    date = uiState.date,
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    onDateChange = { viewModel.setDate(it) },
                    onStartTimeChange = { viewModel.setStartTime(it) },
                    onEndTimeChange = { viewModel.setEndTime(it) }
                )

                HorizontalDivider()

                // Контент в зависимости от типа
                when (uiState.type) {
                    AppointmentType.VISIT -> {
                        VisitFields(
                            clientName = uiState.clientName,
                            onClientNameChange = { viewModel.setClientName(it) },
                            onSelectClient = { /* TODO: Открыть выбор клиента */ },
                            totalPrice = uiState.totalPriceRubles,
                            onTotalPriceChange = { viewModel.setTotalPrice(it) },
                            notes = uiState.notes,
                            onNotesChange = { viewModel.setNotes(it) }
                        )
                    }
                    AppointmentType.NOTE -> {
                        NoteFields(
                            title = uiState.title,
                            onTitleChange = { viewModel.setTitle(it) },
                            notes = uiState.notes,
                            onNotesChange = { viewModel.setNotes(it) }
                        )
                    }
                    AppointmentType.BLOCK -> {
                        BlockFields(
                            title = uiState.title,
                            onTitleChange = { viewModel.setTitle(it) },
                            notes = uiState.notes,
                            onNotesChange = { viewModel.setNotes(it) }
                        )
                    }
                }

                // Напоминания (только для VISIT)
                if (uiState.type == AppointmentType.VISIT) {
                    HorizontalDivider()
                    RemindersSection(
                        remind24h = uiState.remind24h,
                        remind1h = uiState.remind1h,
                        remind15m = uiState.remind15m,
                        onRemind24hChange = { viewModel.setRemind24h(it) },
                        onRemind1hChange = { viewModel.setRemind1h(it) },
                        onRemind15mChange = { viewModel.setRemind15m(it) }
                    )
                }

                // Кнопки управления статусом (только для VISIT в статусе SCHEDULED)
                if (appointmentId != null &&
                    uiState.type == AppointmentType.VISIT &&
                    uiState.status == AppointmentStatus.SCHEDULED
                ) {
                    HorizontalDivider()
                    StatusActionsSection(
                        onComplete = { viewModel.markCompleted() },
                        onCancel = { viewModel.markCancelled() },
                        onNoShow = { viewModel.markNoShow() }
                    )
                }

                // Индикатор статуса (если не SCHEDULED)
                if (appointmentId != null && uiState.status != AppointmentStatus.SCHEDULED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusBadge(status = uiState.status)
                }

                // Кнопка удаления (только при редактировании)
                if (appointmentId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.delete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить запись")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentTypeSelector(
    selectedType: AppointmentType,
    onTypeChange: (AppointmentType) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = "Тип записи",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val types = listOf(
                AppointmentType.VISIT to "Визит",
                AppointmentType.NOTE to "Заметка",
                AppointmentType.BLOCK to "Занято"
            )
            types.forEachIndexed { index, (type, label) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                    onClick = { onTypeChange(type) },
                    selected = selectedType == type,
                    enabled = enabled
                ) {
                    Text(label)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeSection(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Дата и время",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Дата
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(date.format(dateFormatter))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Время
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                onClick = { showStartTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(startTime.format(timeFormatter))
                }
            }

            Text(
                text = "—",
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            OutlinedCard(
                onClick = { showEndTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(endTime.format(timeFormatter))
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateChange(LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)))
                    }
                    showDatePicker = false
                }) {
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

    // Time Picker Dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onConfirm = {
                onStartTimeChange(it)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime,
            onConfirm = {
                onEndTimeChange(it)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@Composable
private fun VisitFields(
    clientName: String,
    onClientNameChange: (String) -> Unit,
    onSelectClient: () -> Unit,
    totalPrice: String,
    onTotalPriceChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Клиент
        OutlinedTextField(
            value = clientName,
            onValueChange = onClientNameChange,
            label = { Text("Клиент") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onSelectClient) {
                    Icon(Icons.Default.Search, contentDescription = "Выбрать клиента")
                }
            },
            singleLine = true
        )

        // Цена
        OutlinedTextField(
            value = totalPrice,
            onValueChange = { onTotalPriceChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Стоимость, ₽") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
            singleLine = true
        )

        // Заметки
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Заметки") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
private fun NoteFields(
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )
    }
}

@Composable
private fun BlockFields(
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Причина") },
            placeholder = { Text("Выходной, обед, личные дела...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Примечание") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
private fun RemindersSection(
    remind24h: Boolean,
    remind1h: Boolean,
    remind15m: Boolean,
    onRemind24hChange: (Boolean) -> Unit,
    onRemind1hChange: (Boolean) -> Unit,
    onRemind15mChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Напоминания",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = remind24h,
                onCheckedChange = onRemind24hChange
            )
            Text("За 24 часа")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = remind1h,
                onCheckedChange = onRemind1hChange
            )
            Text("За 1 час")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = remind15m,
                onCheckedChange = onRemind15mChange
            )
            Text("За 15 минут")
        }
    }
}

@Composable
private fun StatusActionsSection(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onNoShow: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Действия",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Завершить
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Завершить")
            }

            // Отменить
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Отменить")
            }
        }

        // Не пришёл
        OutlinedButton(
            onClick = onNoShow,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Не пришёл")
        }
    }
}

@Composable
private fun StatusBadge(status: AppointmentStatus) {
    val (text, containerColor, contentColor) = when (status) {
        AppointmentStatus.SCHEDULED -> Triple(
            "Запланировано",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        AppointmentStatus.COMPLETED -> Triple(
            "Завершено",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        AppointmentStatus.CANCELLED -> Triple(
            "Отменено",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        AppointmentStatus.NO_SHOW -> Triple(
            "Не пришёл",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (status) {
                    AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
                    AppointmentStatus.COMPLETED -> Icons.Default.CheckCircle
                    AppointmentStatus.CANCELLED -> Icons.Default.Cancel
                    AppointmentStatus.NO_SHOW -> Icons.Default.PersonOff
                },
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}
