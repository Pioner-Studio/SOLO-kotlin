package ru.crmplatforma.solo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.local.entity.AppointmentStatus
import ru.crmplatforma.solo.data.local.entity.AppointmentType
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.data.repository.AppointmentRepository
import ru.crmplatforma.solo.data.repository.ClientRepository
import ru.crmplatforma.solo.data.repository.TaskRepository
import ru.crmplatforma.solo.data.repository.TransactionRepository
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.inject.Inject

/**
 * UI State для Dashboard.
 */
data class DashboardState(
    val todayAppointments: List<AppointmentEntity> = emptyList(),
    val nextAppointment: AppointmentEntity? = null,
    val overdueTasks: List<TaskEntity> = emptyList(),
    val todayTasks: List<TaskEntity> = emptyList(),
    val todayIncome: Long = 0L,  // в копейках
    val monthIncome: Long = 0L,
    val upcomingBirthdays: Int = 0,
    val isLoading: Boolean = true
)

/**
 * DashboardViewModel — управление данными экрана "Сегодня".
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val taskRepository: TaskRepository,
    private val transactionRepository: TransactionRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val today = LocalDate.now()

    // Записи на сегодня
    private val todayAppointments: Flow<List<AppointmentEntity>> =
        appointmentRepository.getAppointmentsByDate(today)
            .map { list ->
                list.filter { it.status == AppointmentStatus.SCHEDULED }
                    .sortedBy { it.startAt }
            }

    // Следующая запись (ближайшая с startAt > now)
    private val nextAppointment: Flow<AppointmentEntity?> = todayAppointments
        .map { list ->
            val now = OffsetDateTime.now()
            list.firstOrNull { it.startAt.isAfter(now) }
        }

    // Просроченные задачи
    private val overdueTasks: Flow<List<TaskEntity>> = taskRepository.getOverdueTasks()

    // Задачи на сегодня
    private val todayTasks: Flow<List<TaskEntity>> = taskRepository.getTodayTasks()

    // Доход за сегодня (из завершённых визитов)
    private val todayIncome: Flow<Long> = appointmentRepository.getAppointmentsByDate(today)
        .map { list ->
            list.filter {
                it.type == AppointmentType.VISIT && it.status == AppointmentStatus.COMPLETED
            }.sumOf { it.totalPriceKopecks }
        }

    // Доход за месяц
    private val monthIncome: Flow<Long> = transactionRepository.getIncomeForMonth(
        today.year,
        today.monthValue
    )

    // Дни рождения на этой неделе
    private val upcomingBirthdays: Flow<Int> = clientRepository.getClientsWithBirthdayThisWeek()
        .map { it.size }

    // Комбинированный UI State
    val uiState: StateFlow<DashboardState> = combine(
        todayAppointments,
        nextAppointment,
        overdueTasks,
        todayTasks,
        todayIncome,
        monthIncome,
        upcomingBirthdays
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        DashboardState(
            todayAppointments = values[0] as List<AppointmentEntity>,
            nextAppointment = values[1] as AppointmentEntity?,
            overdueTasks = values[2] as List<TaskEntity>,
            todayTasks = values[3] as List<TaskEntity>,
            todayIncome = values[4] as Long,
            monthIncome = values[5] as Long,
            upcomingBirthdays = values[6] as Int,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )
}
