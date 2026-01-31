package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.TaskDao
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import ru.crmplatforma.solo.data.local.entity.TaskPriority
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskRepository — работа с задачами.
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    // === Queries ===

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllFlow()

    fun getIncompleteTasks(): Flow<List<TaskEntity>> = taskDao.getIncompleteFlow()

    fun getOverdueTasks(): Flow<List<TaskEntity>> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return taskDao.getOverdueFlow(now)
    }

    fun getTodayTasks(): Flow<List<TaskEntity>> {
        val today = LocalDate.now().toString()
        return taskDao.getTodayFlow(today)
    }

    fun getThisWeekTasks(): Flow<List<TaskEntity>> {
        val today = LocalDate.now()
        val weekEnd = today.plusDays(7)
        return taskDao.getThisWeekFlow(today.toString(), weekEnd.toString())
    }

    fun getCompletedTasks(limit: Int = 50): Flow<List<TaskEntity>> = taskDao.getCompletedFlow(limit)

    fun getTaskById(id: String): Flow<TaskEntity?> = taskDao.getByIdFlow(id)

    fun getTodayTaskCount(): Flow<Int> {
        val today = LocalDate.now().toString()
        return taskDao.getTodayCountFlow(today)
    }

    fun getOverdueTaskCount(): Flow<Int> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return taskDao.getOverdueCountFlow(now)
    }

    // === Commands ===

    suspend fun createTask(
        title: String,
        description: String? = null,
        dueAt: OffsetDateTime? = null,
        remindAt: OffsetDateTime? = null,
        priority: TaskPriority = TaskPriority.NORMAL
    ): TaskEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            dueAt = dueAt,
            remindAt = remindAt,
            priority = priority,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        taskDao.insert(task)
        return task
    }

    suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        taskDao.update(updated)
    }

    suspend fun completeTask(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        taskDao.complete(id, now)
    }

    suspend fun uncompleteTask(id: String) {
        taskDao.uncomplete(id)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
    }

    // === Reminders ===

    suspend fun getTasksForReminders(): List<TaskEntity> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return taskDao.getForReminders(now)
    }

    // === Sync ===

    suspend fun getUnsyncedTasks(): List<TaskEntity> = taskDao.getUnsynced()

    suspend fun markTaskSynced(id: String, serverId: String) {
        taskDao.markSynced(id, serverId)
    }
}
