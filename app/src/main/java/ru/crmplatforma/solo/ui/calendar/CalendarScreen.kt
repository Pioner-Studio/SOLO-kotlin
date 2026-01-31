package ru.crmplatforma.solo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.local.entity.AppointmentStatus
import ru.crmplatforma.solo.data.local.entity.AppointmentType
import ru.crmplatforma.solo.ui.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * CalendarScreen — главный экран календаря.
 *
 * Поддерживает 3 режима:
 * - День (timeline с часами)
 * - 2 недели (сетка 14 дней)
 * - Месяц (компактный вид)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val appointmentsByDate by viewModel.appointmentsByDate.collectAsState()

    Scaffold(
        topBar = {
            CalendarTopBar(
                selectedDate = selectedDate,
                viewMode = viewMode,
                onViewModeChange = { viewModel.setViewMode(it) },
                onTodayClick = { viewModel.goToToday() },
                onPreviousClick = {
                    when (viewMode) {
                        CalendarViewMode.DAY -> viewModel.goToPreviousDay()
                        CalendarViewMode.TWO_WEEKS -> viewModel.goToPreviousWeek()
                        CalendarViewMode.MONTH -> viewModel.goToPreviousMonth()
                    }
                },
                onNextClick = {
                    when (viewMode) {
                        CalendarViewMode.DAY -> viewModel.goToNextDay()
                        CalendarViewMode.TWO_WEEKS -> viewModel.goToNextWeek()
                        CalendarViewMode.MONTH -> viewModel.goToNextMonth()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AppointmentNew.createRoute(selectedDate))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить запись")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (viewMode) {
                CalendarViewMode.DAY -> {
                    DayView(
                        date = selectedDate,
                        appointments = appointments,
                        onAppointmentClick = { appointment ->
                            navController.navigate(Screen.AppointmentEdit.createRoute(appointment.id))
                        }
                    )
                }
                CalendarViewMode.TWO_WEEKS -> {
                    TwoWeeksView(
                        selectedDate = selectedDate,
                        appointmentsByDate = appointmentsByDate,
                        onDateSelect = { viewModel.selectDate(it) },
                        onAppointmentClick = { appointment ->
                            navController.navigate(Screen.AppointmentEdit.createRoute(appointment.id))
                        }
                    )
                }
                CalendarViewMode.MONTH -> {
                    MonthView(
                        selectedDate = selectedDate,
                        appointmentsByDate = appointmentsByDate,
                        onDateSelect = { viewModel.selectDate(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onPreviousClick) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Назад")
                }
            },
            actions = {
                IconButton(onClick = onNextClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Вперёд")
                }
                if (selectedDate != LocalDate.now()) {
                    TextButton(onClick = onTodayClick) {
                        Text("Сегодня")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Переключатель режимов
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val modes = listOf(
                CalendarViewMode.DAY to "День",
                CalendarViewMode.TWO_WEEKS to "2 недели",
                CalendarViewMode.MONTH to "Месяц"
            )
            modes.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                    onClick = { onViewModeChange(mode) },
                    selected = viewMode == mode
                ) {
                    Text(label)
                }
            }
        }
    }
}

/**
 * Day View — timeline с часами.
 */
@Composable
private fun DayView(
    date: LocalDate,
    appointments: List<AppointmentEntity>,
    onAppointmentClick: (AppointmentEntity) -> Unit
) {
    if (appointments.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет записей",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Нажмите + чтобы добавить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appointments.sortedBy { it.startAt }) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onClick = { onAppointmentClick(appointment) }
                )
            }
        }
    }
}

/**
 * Карточка записи.
 */
@Composable
private fun AppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = appointment.startAt.toLocalTime().format(timeFormatter)
    val endTime = appointment.endAt.toLocalTime().format(timeFormatter)

    val backgroundColor = when (appointment.type) {
        AppointmentType.VISIT -> MaterialTheme.colorScheme.primaryContainer
        AppointmentType.NOTE -> MaterialTheme.colorScheme.tertiaryContainer
        AppointmentType.BLOCK -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusColor = when (appointment.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> Color(0xFF22C55E)
        AppointmentStatus.CANCELLED -> Color(0xFFEF4444)
        AppointmentStatus.NO_SHOW -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Время
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Разделитель со статусом
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Контент
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (appointment.type) {
                        AppointmentType.VISIT -> appointment.clientName ?: "Клиент"
                        AppointmentType.NOTE -> appointment.title ?: "Заметка"
                        AppointmentType.BLOCK -> appointment.title ?: "Занято"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (appointment.type == AppointmentType.VISIT && appointment.totalPriceKopecks > 0) {
                    Text(
                        text = "${appointment.totalPriceKopecks / 100} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                appointment.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Иконка типа
            Icon(
                imageVector = when (appointment.type) {
                    AppointmentType.VISIT -> Icons.Default.Person
                    AppointmentType.NOTE -> Icons.Default.Note
                    AppointmentType.BLOCK -> Icons.Default.Block
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Two Weeks View — сетка 14 дней.
 */
@Composable
private fun TwoWeeksView(
    selectedDate: LocalDate,
    appointmentsByDate: Map<LocalDate, List<AppointmentEntity>>,
    onDateSelect: (LocalDate) -> Unit,
    onAppointmentClick: (AppointmentEntity) -> Unit
) {
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовки дней недели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2 недели
        repeat(2) { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                repeat(7) { dayOfWeek ->
                    val date = startOfWeek.plusDays((week * 7 + dayOfWeek).toLong())
                    val hasAppointments = appointmentsByDate.containsKey(date)
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()

                    DayCell(
                        date = date,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasAppointments = hasAppointments,
                        onClick = { onDateSelect(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Записи выбранного дня
        val dayAppointments = appointmentsByDate[selectedDate] ?: emptyList()
        if (dayAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет записей на этот день",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayAppointments.sortedBy { it.startAt }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onAppointmentClick(appointment) }
                    )
                }
            }
        }
    }
}

/**
 * Month View — компактный месячный вид.
 */
@Composable
private fun MonthView(
    selectedDate: LocalDate,
    appointmentsByDate: Map<LocalDate, List<AppointmentEntity>>,
    onDateSelect: (LocalDate) -> Unit
) {
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1 // Пн = 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовки дней недели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Дни месяца
        val totalCells = startOffset + lastDayOfMonth.dayOfMonth
        val weeks = (totalCells + 6) / 7

        repeat(weeks) { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - startOffset + 1

                    if (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
                        val date = selectedDate.withDayOfMonth(dayOfMonth)
                        val hasAppointments = appointmentsByDate.containsKey(date)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        DayCell(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasAppointments = hasAppointments,
                            onClick = { onDateSelect(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Ячейка дня в календаре.
 */
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasAppointments: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
        if (hasAppointments) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}
