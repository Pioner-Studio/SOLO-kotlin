package ru.crmplatforma.solo.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.data.repository.TaskRepository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Секция для группировки задач.
 */
enum class TaskSection {
    OVERDUE,    // Просрочено
    TODAY,      // Сегодня
    THIS_WEEK,  // На этой неделе
    LATER,      // Позже
    COMPLETED   // Выполнено
}

/**
 * UI State для списка задач.
 */
data class TasksState(
    val overdueTasks: List<TaskEntity> = emptyList(),
    val todayTasks: List<TaskEntity> = emptyList(),
    val weekTasks: List<TaskEntity> = emptyList(),
    val laterTasks: List<TaskEntity> = emptyList(),
    val completedTasks: List<TaskEntity> = emptyList(),
    val expandedSections: Set<TaskSection> = setOf(
        TaskSection.OVERDUE,
        TaskSection.TODAY,
        TaskSection.THIS_WEEK
    )
) {
    val hasAnyTasks: Boolean
        get() = overdueTasks.isNotEmpty() ||
                todayTasks.isNotEmpty() ||
                weekTasks.isNotEmpty() ||
                laterTasks.isNotEmpty() ||
                completedTasks.isNotEmpty()

    val totalIncomplete: Int
        get() = overdueTasks.size + todayTasks.size + weekTasks.size + laterTasks.size
}

/**
 * TasksViewModel — управление списком задач.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _expandedSections = MutableStateFlow(
        setOf(TaskSection.OVERDUE, TaskSection.TODAY, TaskSection.THIS_WEEK)
    )

    // Все задачи с группировкой
    private val allTasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<TasksState> = combine(
        allTasks,
        _expandedSections
    ) { tasks, expanded ->
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val today = LocalDate.now()
        val weekEnd = today.plusDays(7)

        // Группируем задачи
        val (completed, incomplete) = tasks.partition { it.isCompleted }

        val overdue = incomplete.filter { task ->
            task.dueAt != null && task.dueAt.toLocalDate() < today
        }.sortedBy { it.dueAt }

        val todayList = incomplete.filter { task ->
            task.dueAt?.toLocalDate() == today
        }.sortedBy { it.dueAt }

        val thisWeek = incomplete.filter { task ->
            task.dueAt != null &&
            task.dueAt.toLocalDate() > today &&
            task.dueAt.toLocalDate() <= weekEnd
        }.sortedBy { it.dueAt }

        val later = incomplete.filter { task ->
            task.dueAt == null || task.dueAt.toLocalDate() > weekEnd
        }.sortedBy { it.dueAt ?: OffsetDateTime.MAX }

        TasksState(
            overdueTasks = overdue,
            todayTasks = todayList,
            weekTasks = thisWeek,
            laterTasks = later,
            completedTasks = completed.sortedByDescending { it.completedAt }.take(20),
            expandedSections = expanded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksState()
    )

    // === Actions ===

    fun toggleSection(section: TaskSection) {
        val current = _expandedSections.value
        _expandedSections.value = if (section in current) {
            current - section
        } else {
            current + section
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
        }
    }

    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.uncompleteTask(taskId)
        }
    }
}
