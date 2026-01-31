package ru.crmplatforma.solo.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.local.entity.AppointmentType
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.ui.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Сегодня",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = today.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions
                item {
                    QuickActionsRow(
                        onNewAppointment = {
                            navController.navigate(Screen.AppointmentNew.createRoute(today))
                        },
                        onNewClient = {
                            // TODO: Навигация к созданию клиента
                        },
                        onNewExpense = {
                            // TODO: Навигация к созданию расхода
                        }
                    )
                }

                // Summary Card
                item {
                    SummaryCard(
                        appointmentCount = uiState.todayAppointments.size,
                        nextAppointment = uiState.nextAppointment,
                        todayIncome = uiState.todayIncome,
                        monthIncome = uiState.monthIncome
                    )
                }

                // Следующая запись
                uiState.nextAppointment?.let { next ->
                    item {
                        NextAppointmentCard(
                            appointment = next,
                            onClick = {
                                navController.navigate(Screen.AppointmentEdit.createRoute(next.id))
                            }
                        )
                    }
                }

                // Записи на сегодня
                if (uiState.todayAppointments.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Записи на сегодня",
                            count = uiState.todayAppointments.size
                        )
                    }
                    items(uiState.todayAppointments) { appointment ->
                        AppointmentListItem(
                            appointment = appointment,
                            onClick = {
                                navController.navigate(Screen.AppointmentEdit.createRoute(appointment.id))
                            }
                        )
                    }
                }

                // Просроченные задачи
                if (uiState.overdueTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Просроченные задачи",
                            count = uiState.overdueTasks.size,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(uiState.overdueTasks) { task ->
                        TaskListItem(
                            task = task,
                            isOverdue = true
                        )
                    }
                }

                // Задачи на сегодня
                if (uiState.todayTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Задачи на сегодня",
                            count = uiState.todayTasks.size
                        )
                    }
                    items(uiState.todayTasks) { task ->
                        TaskListItem(task = task)
                    }
                }

                // Дни рождения
                if (uiState.upcomingBirthdays > 0) {
                    item {
                        BirthdayAlert(count = uiState.upcomingBirthdays)
                    }
                }

                // Empty state
                if (uiState.todayAppointments.isEmpty() &&
                    uiState.todayTasks.isEmpty() &&
                    uiState.overdueTasks.isEmpty()
                ) {
                    item {
                        EmptyDayCard(
                            onCreateAppointment = {
                                navController.navigate(Screen.AppointmentNew.createRoute(today))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNewAppointment: () -> Unit,
    onNewClient: () -> Unit,
    onNewExpense: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.Event,
            label = "Запись",
            onClick = onNewAppointment
        )
        QuickActionButton(
            icon = Icons.Default.PersonAdd,
            label = "Клиент",
            onClick = onNewClient
        )
        QuickActionButton(
            icon = Icons.Default.RemoveCircleOutline,
            label = "Расход",
            onClick = onNewExpense
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun SummaryCard(
    appointmentCount: Int,
    nextAppointment: AppointmentEntity?,
    todayIncome: Long,
    monthIncome: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                value = appointmentCount.toString(),
                label = "Записей"
            )
            SummaryItem(
                value = "${todayIncome / 100} ₽",
                label = "Сегодня"
            )
            SummaryItem(
                value = "${monthIncome / 100} ₽",
                label = "За месяц"
            )
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun NextAppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Следующая запись",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = appointment.startAt.toLocalTime().format(timeFormatter),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (appointment.type) {
                        AppointmentType.VISIT -> appointment.clientName ?: "Клиент"
                        AppointmentType.NOTE -> appointment.title ?: "Заметка"
                        AppointmentType.BLOCK -> appointment.title ?: "Занято"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Badge(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        ) {
            Text(count.toString())
        }
    }
}

@Composable
private fun AppointmentListItem(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appointment.startAt.toLocalTime().format(timeFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(56.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (appointment.type) {
                        AppointmentType.VISIT -> appointment.clientName ?: "Клиент"
                        AppointmentType.NOTE -> appointment.title ?: "Заметка"
                        AppointmentType.BLOCK -> appointment.title ?: "Занято"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                if (appointment.type == AppointmentType.VISIT && appointment.totalPriceKopecks > 0) {
                    Text(
                        text = "${appointment.totalPriceKopecks / 100} ₽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

@Composable
private fun TaskListItem(
    task: TaskEntity,
    isOverdue: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completedAt != null,
                onCheckedChange = { /* TODO */ }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                task.dueAt?.let { dueAt ->
                    Text(
                        text = dueAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BirthdayAlert(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Cake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Дни рождения на этой неделе",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$count ${if (count == 1) "клиент" else "клиентов"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun EmptyDayCard(
    onCreateAppointment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Свободный день!",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нет записей на сегодня",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Создайте запись или отдохните",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCreateAppointment) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать запись")
            }
        }
    }
}
