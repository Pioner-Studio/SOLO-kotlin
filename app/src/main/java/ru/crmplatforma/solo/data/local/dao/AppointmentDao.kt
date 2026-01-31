package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.local.entity.AppointmentStatus
import ru.crmplatforma.solo.data.local.entity.AppointmentType

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Update
    suspend fun update(appointment: AppointmentEntity)

    // === Запросы по дате ===

    // Записи на конкретный день
    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND DATE(startAt) = :date
        ORDER BY startAt ASC
    """)
    fun getByDateFlow(date: String): Flow<List<AppointmentEntity>>

    // Записи в диапазоне дат (для 2-недельного и месячного вида)
    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND DATE(startAt) >= :startDate
          AND DATE(startAt) <= :endDate
        ORDER BY startAt ASC
    """)
    fun getByDateRangeFlow(startDate: String, endDate: String): Flow<List<AppointmentEntity>>

    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND DATE(startAt) >= :startDate
          AND DATE(startAt) <= :endDate
        ORDER BY startAt ASC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<AppointmentEntity>

    // === Запросы по ID ===

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: String): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getByIdFlow(id: String): Flow<AppointmentEntity?>

    // === Запросы по клиенту ===

    @Query("""
        SELECT * FROM appointments
        WHERE clientId = :clientId AND deletedAt IS NULL
        ORDER BY startAt DESC
    """)
    fun getByClientFlow(clientId: String): Flow<List<AppointmentEntity>>

    // === Запросы по статусу ===

    // Предстоящие записи (для дашборда)
    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND status = 'SCHEDULED'
          AND startAt >= :now
        ORDER BY startAt ASC
        LIMIT :limit
    """)
    fun getUpcomingFlow(now: String, limit: Int = 10): Flow<List<AppointmentEntity>>

    // Следующая запись
    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND status = 'SCHEDULED'
          AND startAt >= :now
        ORDER BY startAt ASC
        LIMIT 1
    """)
    fun getNextAppointmentFlow(now: String): Flow<AppointmentEntity?>

    // === Обновление статуса ===

    @Query("UPDATE appointments SET status = :status, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: AppointmentStatus, updatedAt: String)

    // === Soft Delete ===

    @Query("UPDATE appointments SET deletedAt = :deletedAt, synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String)

    // === Синхронизация ===

    @Query("SELECT * FROM appointments WHERE synced = 0")
    suspend fun getUnsynced(): List<AppointmentEntity>

    @Query("UPDATE appointments SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)

    // === Напоминания ===

    // Записи для напоминаний (remind24h/1h/15m и startAt в будущем)
    @Query("""
        SELECT * FROM appointments
        WHERE deletedAt IS NULL
          AND status = 'SCHEDULED'
          AND startAt > :now
          AND (remind24h = 1 OR remind1h = 1 OR remind15m = 1)
    """)
    suspend fun getForReminders(now: String): List<AppointmentEntity>

    // === Статистика ===

    // Количество записей на день
    @Query("""
        SELECT COUNT(*) FROM appointments
        WHERE deletedAt IS NULL
          AND DATE(startAt) = :date
          AND type = 'VISIT'
    """)
    fun getCountByDateFlow(date: String): Flow<Int>

    // Доход за период (завершённые записи)
    @Query("""
        SELECT COALESCE(SUM(totalPriceKopecks), 0) FROM appointments
        WHERE deletedAt IS NULL
          AND status = 'COMPLETED'
          AND DATE(startAt) >= :startDate
          AND DATE(startAt) <= :endDate
    """)
    suspend fun getRevenueByDateRange(startDate: String, endDate: String): Long
}
