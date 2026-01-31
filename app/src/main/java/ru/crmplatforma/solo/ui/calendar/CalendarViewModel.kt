package ru.crmplatforma.solo.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.UserPreferences
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.repository.AppointmentRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Режим отображения календаря.
 */
enum class CalendarViewMode {
    DAY,        // День (timeline)
    TWO_WEEKS,  // 2 недели (14-day grid)
    MONTH       // Месяц (compact)
}

/**
 * CalendarViewModel — управление состоянием календаря.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // === State ===

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.DAY)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Записи для текущего выбранного периода
    val appointments: StateFlow<List<AppointmentEntity>> = combine(
        _selectedDate,
        _viewMode
    ) { date, mode ->
        Pair(date, mode)
    }.flatMapLatest { (date, mode) ->
        when (mode) {
            CalendarViewMode.DAY -> {
                appointmentRepository.getAppointmentsByDate(date)
            }
            CalendarViewMode.TWO_WEEKS -> {
                val start = date.minusDays(date.dayOfWeek.value.toLong() - 1) // Понедельник
                val end = start.plusDays(13) // 2 недели
                appointmentRepository.getAppointmentsByDateRange(start, end)
            }
            CalendarViewMode.MONTH -> {
                val start = date.withDayOfMonth(1)
                val end = date.withDayOfMonth(date.lengthOfMonth())
                appointmentRepository.getAppointmentsByDateRange(start, end)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Записи сгруппированные по дате (для 2-недельного и месячного вида)
    val appointmentsByDate: StateFlow<Map<LocalDate, List<AppointmentEntity>>> = appointments
        .map { list ->
            list.groupBy { it.startAt.toLocalDate() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        // Загружаем сохранённый режим отображения
        viewModelScope.launch {
            userPreferences.calendarViewMode.collect { mode ->
                _viewMode.value = when (mode) {
                    "TWO_WEEKS" -> CalendarViewMode.TWO_WEEKS
                    "MONTH" -> CalendarViewMode.MONTH
                    else -> CalendarViewMode.DAY
                }
            }
        }
    }

    // === Actions ===

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _currentMonth.value = YearMonth.from(date)
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            userPreferences.setCalendarViewMode(mode.name)
        }
    }

    fun goToToday() {
        selectDate(LocalDate.now())
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToPreviousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun goToNextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun goToPreviousMonth() {
        val newMonth = _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        _selectedDate.value = _selectedDate.value.withMonth(newMonth.monthValue).withYear(newMonth.year)
    }

    fun goToNextMonth() {
        val newMonth = _currentMonth.value.plusMonths(1)
        _currentMonth.value = newMonth
        _selectedDate.value = _selectedDate.value.withMonth(newMonth.monthValue).withYear(newMonth.year)
    }

    // === Helpers ===

    fun getAppointmentsForDate(date: LocalDate): List<AppointmentEntity> {
        return appointmentsByDate.value[date] ?: emptyList()
    }

    fun hasAppointmentsOnDate(date: LocalDate): Boolean {
        return appointmentsByDate.value.containsKey(date)
    }
}
