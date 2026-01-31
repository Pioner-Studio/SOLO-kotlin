package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * Приоритет задачи.
 */
enum class TaskPriority {
    LOW,      // Низкий
    NORMAL,   // Обычный
    HIGH      // Высокий (выделяется цветом)
}

/**
 * Задача — дело, которое нужно выполнить.
 *
 * Группировка:
 * - Просрочено (dueAt < now, !completed)
 * - Сегодня (dueAt = today)
 * - Неделя (dueAt в течение 7 дней)
 * - Позже (остальное)
 * - Выполнено
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index("dueAt"),
        Index("isCompleted"),
        Index("priority"),
        Index("synced")
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val title: String,                       // Заголовок задачи
    val description: String? = null,         // Подробности
    val dueAt: OffsetDateTime? = null,       // Срок выполнения
    val remindAt: OffsetDateTime? = null,    // Когда напомнить
    val priority: TaskPriority = TaskPriority.NORMAL,

    val isCompleted: Boolean = false,        // Выполнена?
    val completedAt: OffsetDateTime? = null, // Когда выполнена

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
