package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.TaskEntity

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    // === Запросы ===

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getByIdFlow(id: String): Flow<TaskEntity?>

    // Все задачи (для списка)
    // CASE-выражение эмулирует NULLS LAST (NULL в конце списка)
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, priority DESC")
    fun getAllFlow(): Flow<List<TaskEntity>>

    // Невыполненные задачи
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, priority DESC")
    fun getIncompleteFlow(): Flow<List<TaskEntity>>

    // Просроченные
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueAt < :now ORDER BY dueAt ASC")
    fun getOverdueFlow(now: String): Flow<List<TaskEntity>>

    // Задачи на сегодня
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND DATE(dueAt) = :today ORDER BY dueAt ASC")
    fun getTodayFlow(today: String): Flow<List<TaskEntity>>

    // Задачи на неделю
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND DATE(dueAt) > :today AND DATE(dueAt) <= :weekEnd ORDER BY dueAt ASC")
    fun getThisWeekFlow(today: String, weekEnd: String): Flow<List<TaskEntity>>

    // Выполненные
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT :limit")
    fun getCompletedFlow(limit: Int = 50): Flow<List<TaskEntity>>

    // === Действия ===

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt, synced = 0 WHERE id = :id")
    suspend fun complete(id: String, completedAt: String)

    @Query("UPDATE tasks SET isCompleted = 0, completedAt = NULL, synced = 0 WHERE id = :id")
    suspend fun uncomplete(id: String)

    // === Напоминания ===

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND remindAt IS NOT NULL AND remindAt > :now")
    suspend fun getForReminders(now: String): List<TaskEntity>

    // === Синхронизация ===

    @Query("SELECT * FROM tasks WHERE synced = 0")
    suspend fun getUnsynced(): List<TaskEntity>

    @Query("UPDATE tasks SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)

    // === Статистика (для дашборда) ===

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND DATE(dueAt) = :today")
    fun getTodayCountFlow(today: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND dueAt < :now")
    fun getOverdueCountFlow(now: String): Flow<Int>
}
