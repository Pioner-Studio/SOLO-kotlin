package ru.crmplatforma.solo.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.data.local.entity.TaskPriority
import ru.crmplatforma.solo.data.repository.TaskRepository
import ru.crmplatforma.solo.work.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * UI State для редактора задачи.
 */
data class TaskEditorState(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val hasReminder: Boolean = false,
    val isCompleted: Boolean = false
)

/**
 * TaskEditorViewModel — создание и редактирование задач.
 */
@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditorState())
    val uiState: StateFlow<TaskEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private var originalTask: TaskEntity? = null

    fun loadTask(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val task = taskRepository.getTaskById(id).first()
                if (task != null) {
                    originalTask = task
                    _uiState.value = TaskEditorState(
                        id = task.id,
                        title = task.title,
                        description = task.description ?: "",
                        dueDate = task.dueAt?.toLocalDate(),
                        dueTime = task.dueAt?.toLocalTime(),
                        priority = task.priority,
                        hasReminder = task.remindAt != null,
                        isCompleted = task.isCompleted
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Setters ===

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun setDueDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    fun setDueTime(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(dueTime = time)
    }

    fun setPriority(priority: TaskPriority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun setHasReminder(hasReminder: Boolean) {
        _uiState.value = _uiState.value.copy(hasReminder = hasReminder)
    }

    fun clearDueDate() {
        _uiState.value = _uiState.value.copy(dueDate = null, dueTime = null)
    }

    // === Validation ===

    fun isValid(): Boolean {
        return _uiState.value.title.isNotBlank()
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value

                val dueAt = if (state.dueDate != null) {
                    OffsetDateTime.of(
                        state.dueDate,
                        state.dueTime ?: LocalTime.of(23, 59),
                        ZoneOffset.UTC
                    )
                } else null

                val remindAt = if (state.hasReminder && dueAt != null) {
                    // Напоминание за 1 час до срока
                    dueAt.minusHours(1)
                } else null

                if (state.id == null) {
                    // Создание
                    val task = taskRepository.createTask(
                        title = state.title,
                        description = state.description.takeIf { it.isNotBlank() },
                        dueAt = dueAt,
                        remindAt = remindAt,
                        priority = state.priority
                    )
                    // Планируем напоминание
                    if (remindAt != null) {
                        reminderScheduler.scheduleTaskReminder(task)
                    }
                } else {
                    // Обновление
                    originalTask?.copy(
                        title = state.title,
                        description = state.description.takeIf { it.isNotBlank() },
                        dueAt = dueAt,
                        remindAt = remindAt,
                        priority = state.priority
                    )?.also { task ->
                        taskRepository.updateTask(task)
                        // Перепланируем напоминание
                        reminderScheduler.cancelTaskReminder(task.id)
                        if (remindAt != null) {
                            reminderScheduler.scheduleTaskReminder(task)
                        }
                    }
                }

                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Delete ===

    fun delete() {
        val task = originalTask ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                reminderScheduler.cancelTaskReminder(task.id)
                taskRepository.deleteTask(task)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
