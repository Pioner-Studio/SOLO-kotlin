package ru.crmplatforma.solo.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.UserPreferences
import ru.crmplatforma.solo.data.local.entity.AppointmentStatus
import ru.crmplatforma.solo.data.local.entity.AppointmentType
import ru.crmplatforma.solo.data.repository.AppointmentRepository
import ru.crmplatforma.solo.work.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * UI State для редактора записей.
 */
data class AppointmentEditorState(
    val id: String? = null,
    val type: AppointmentType = AppointmentType.VISIT,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(10, 0),
    val endTime: LocalTime = LocalTime.of(11, 0),
    val clientId: String? = null,
    val clientName: String = "",
    val totalPriceRubles: String = "",
    val title: String = "",
    val notes: String = "",
    val remind24h: Boolean = true,
    val remind1h: Boolean = true,
    val remind15m: Boolean = false
)

/**
 * ViewModel для редактора записей.
 */
@HiltViewModel
class AppointmentEditorViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val reminderScheduler: ReminderScheduler,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentEditorState())
    val uiState: StateFlow<AppointmentEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        // Загружаем дефолтные настройки напоминаний
        viewModelScope.launch {
            userPreferences.defaultRemind24h.collect { remind24h ->
                _uiState.value = _uiState.value.copy(remind24h = remind24h)
            }
        }
    }

    fun initNewAppointment(date: LocalDate) {
        _uiState.value = AppointmentEditorState(date = date)
    }

    fun loadAppointment(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val appointment = appointmentRepository.getAppointmentByIdOnce(id)
                if (appointment != null) {
                    _uiState.value = AppointmentEditorState(
                        id = appointment.id,
                        type = appointment.type,
                        status = appointment.status,
                        date = appointment.startAt.toLocalDate(),
                        startTime = appointment.startAt.toLocalTime(),
                        endTime = appointment.endAt.toLocalTime(),
                        clientId = appointment.clientId,
                        clientName = appointment.clientName ?: "",
                        totalPriceRubles = (appointment.totalPriceKopecks / 100).toString(),
                        title = appointment.title ?: "",
                        notes = appointment.notes ?: "",
                        remind24h = appointment.remind24h,
                        remind1h = appointment.remind1h,
                        remind15m = appointment.remind15m
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Setters ===

    fun setType(type: AppointmentType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun setStartTime(time: LocalTime) {
        val state = _uiState.value
        // Автоматически сдвигаем endTime если startTime >= endTime
        val newEndTime = if (time >= state.endTime) {
            time.plusHours(1)
        } else {
            state.endTime
        }
        _uiState.value = state.copy(startTime = time, endTime = newEndTime)
    }

    fun setEndTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = time)
    }

    fun setClientId(id: String?) {
        _uiState.value = _uiState.value.copy(clientId = id)
    }

    fun setClientName(name: String) {
        _uiState.value = _uiState.value.copy(clientName = name)
    }

    fun setTotalPrice(rubles: String) {
        _uiState.value = _uiState.value.copy(totalPriceRubles = rubles)
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun setRemind24h(value: Boolean) {
        _uiState.value = _uiState.value.copy(remind24h = value)
    }

    fun setRemind1h(value: Boolean) {
        _uiState.value = _uiState.value.copy(remind1h = value)
    }

    fun setRemind15m(value: Boolean) {
        _uiState.value = _uiState.value.copy(remind15m = value)
    }

    // === Validation ===

    fun isValid(): Boolean {
        val state = _uiState.value
        return when (state.type) {
            AppointmentType.VISIT -> state.clientName.isNotBlank()
            AppointmentType.NOTE -> state.title.isNotBlank()
            AppointmentType.BLOCK -> true // Блок может быть без названия
        }
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value
                val startAt = OffsetDateTime.of(
                    state.date,
                    state.startTime,
                    ZoneOffset.UTC
                )
                val endAt = OffsetDateTime.of(
                    state.date,
                    state.endTime,
                    ZoneOffset.UTC
                )
                val priceKopecks = state.totalPriceRubles.toLongOrNull()?.times(100) ?: 0L

                val appointment = if (state.id == null) {
                    // Создание новой записи
                    when (state.type) {
                        AppointmentType.VISIT -> appointmentRepository.createVisit(
                            startAt = startAt,
                            endAt = endAt,
                            clientId = state.clientId ?: "",
                            clientName = state.clientName,
                            serviceIds = emptyList(), // TODO: выбор услуг
                            totalPriceKopecks = priceKopecks,
                            notes = state.notes.takeIf { it.isNotBlank() },
                            remind24h = state.remind24h,
                            remind1h = state.remind1h,
                            remind15m = state.remind15m
                        )
                        AppointmentType.NOTE -> appointmentRepository.createNote(
                            startAt = startAt,
                            endAt = endAt,
                            title = state.title,
                            notes = state.notes.takeIf { it.isNotBlank() }
                        )
                        AppointmentType.BLOCK -> appointmentRepository.createBlock(
                            startAt = startAt,
                            endAt = endAt,
                            title = state.title.takeIf { it.isNotBlank() } ?: "Занято",
                            notes = state.notes.takeIf { it.isNotBlank() }
                        )
                    }
                } else {
                    // Обновление существующей
                    val existing = appointmentRepository.getAppointmentByIdOnce(state.id)
                    existing?.copy(
                        startAt = startAt,
                        endAt = endAt,
                        clientId = state.clientId,
                        clientName = state.clientName.takeIf { it.isNotBlank() },
                        totalPriceKopecks = priceKopecks,
                        title = state.title.takeIf { it.isNotBlank() },
                        notes = state.notes.takeIf { it.isNotBlank() },
                        remind24h = state.remind24h,
                        remind1h = state.remind1h,
                        remind15m = state.remind15m
                    )?.also {
                        appointmentRepository.updateAppointment(it)
                    }
                }

                // Планируем напоминания
                appointment?.let {
                    reminderScheduler.cancelAppointmentReminders(it.id)
                    reminderScheduler.scheduleAppointmentReminders(it)
                }

                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Status Actions ===

    fun markCompleted() {
        val id = _uiState.value.id ?: return
        if (_uiState.value.status != AppointmentStatus.SCHEDULED) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = appointmentRepository.getAppointmentByIdOnce(id)
                existing?.copy(status = AppointmentStatus.COMPLETED)?.also {
                    appointmentRepository.updateAppointment(it)
                    reminderScheduler.cancelAppointmentReminders(id) // Отменяем напоминания
                }
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCancelled() {
        val id = _uiState.value.id ?: return
        if (_uiState.value.status != AppointmentStatus.SCHEDULED) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = appointmentRepository.getAppointmentByIdOnce(id)
                existing?.copy(status = AppointmentStatus.CANCELLED)?.also {
                    appointmentRepository.updateAppointment(it)
                    reminderScheduler.cancelAppointmentReminders(id) // Отменяем напоминания
                }
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markNoShow() {
        val id = _uiState.value.id ?: return
        if (_uiState.value.status != AppointmentStatus.SCHEDULED) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = appointmentRepository.getAppointmentByIdOnce(id)
                existing?.copy(status = AppointmentStatus.NO_SHOW)?.also {
                    appointmentRepository.updateAppointment(it)
                    reminderScheduler.cancelAppointmentReminders(id)
                }
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Delete ===

    fun delete() {
        val id = _uiState.value.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                reminderScheduler.cancelAppointmentReminders(id)
                appointmentRepository.deleteAppointment(id)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
