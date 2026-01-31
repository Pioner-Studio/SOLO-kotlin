package ru.crmplatforma.solo.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.AppointmentDao
import ru.crmplatforma.solo.data.local.entity.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppointmentRepository — работа с записями в календаре.
 *
 * Поддерживает:
 * - Визиты клиентов (VISIT)
 * - Заметки (NOTE)
 * - Блокировки (BLOCK)
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val gson: Gson
) {
    // === Queries ===

    fun getAppointmentsByDate(date: LocalDate): Flow<List<AppointmentEntity>> {
        return appointmentDao.getByDateFlow(date.toString())
    }

    fun getAppointmentsByDateRange(start: LocalDate, end: LocalDate): Flow<List<AppointmentEntity>> {
        return appointmentDao.getByDateRangeFlow(start.toString(), end.toString())
    }

    suspend fun getAppointmentsByDateRangeOnce(start: LocalDate, end: LocalDate): List<AppointmentEntity> {
        return appointmentDao.getByDateRange(start.toString(), end.toString())
    }

    fun getAppointmentById(id: String): Flow<AppointmentEntity?> = appointmentDao.getByIdFlow(id)

    suspend fun getAppointmentByIdOnce(id: String): AppointmentEntity? = appointmentDao.getById(id)

    fun getAppointmentsByClient(clientId: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getByClientFlow(clientId)
    }

    fun getUpcomingAppointments(limit: Int = 10): Flow<List<AppointmentEntity>> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return appointmentDao.getUpcomingFlow(now, limit)
    }

    fun getNextAppointment(): Flow<AppointmentEntity?> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return appointmentDao.getNextAppointmentFlow(now)
    }

    fun getTodayAppointmentCount(): Flow<Int> {
        val today = LocalDate.now().toString()
        return appointmentDao.getCountByDateFlow(today)
    }

    // === Commands: VISIT ===

    suspend fun createVisit(
        startAt: OffsetDateTime,
        endAt: OffsetDateTime,
        clientId: String,
        clientName: String,
        serviceIds: List<String>,
        servicesSnapshot: String? = null,
        totalPriceKopecks: Long,
        notes: String? = null,
        remind24h: Boolean = true,
        remind1h: Boolean = true,
        remind15m: Boolean = false
    ): AppointmentEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val appointment = AppointmentEntity(
            id = UUID.randomUUID().toString(),
            type = AppointmentType.VISIT,
            status = AppointmentStatus.SCHEDULED,
            startAt = startAt,
            endAt = endAt,
            clientId = clientId,
            clientName = clientName,
            serviceIdsJson = gson.toJson(serviceIds),
            servicesSnapshot = servicesSnapshot,
            totalPriceKopecks = totalPriceKopecks,
            notes = notes,
            remind24h = remind24h,
            remind1h = remind1h,
            remind15m = remind15m,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        appointmentDao.insert(appointment)
        return appointment
    }

    // === Commands: NOTE ===

    suspend fun createNote(
        startAt: OffsetDateTime,
        endAt: OffsetDateTime,
        title: String,
        notes: String? = null,
        color: String? = null
    ): AppointmentEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val appointment = AppointmentEntity(
            id = UUID.randomUUID().toString(),
            type = AppointmentType.NOTE,
            status = AppointmentStatus.SCHEDULED,
            startAt = startAt,
            endAt = endAt,
            title = title,
            notes = notes,
            color = color,
            remind24h = false,
            remind1h = false,
            remind15m = false,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        appointmentDao.insert(appointment)
        return appointment
    }

    // === Commands: BLOCK ===

    suspend fun createBlock(
        startAt: OffsetDateTime,
        endAt: OffsetDateTime,
        title: String = "Занято",
        notes: String? = null
    ): AppointmentEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val appointment = AppointmentEntity(
            id = UUID.randomUUID().toString(),
            type = AppointmentType.BLOCK,
            status = AppointmentStatus.SCHEDULED,
            startAt = startAt,
            endAt = endAt,
            title = title,
            notes = notes,
            color = "#9CA3AF", // Серый для блокировок
            remind24h = false,
            remind1h = false,
            remind15m = false,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        appointmentDao.insert(appointment)
        return appointment
    }

    // === Commands: Update ===

    suspend fun updateAppointment(appointment: AppointmentEntity) {
        val updated = appointment.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        appointmentDao.update(updated)
    }

    suspend fun updateStatus(id: String, status: AppointmentStatus) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        appointmentDao.updateStatus(id, status, now)
    }

    suspend fun completeAppointment(id: String) {
        updateStatus(id, AppointmentStatus.COMPLETED)
    }

    suspend fun cancelAppointment(id: String) {
        updateStatus(id, AppointmentStatus.CANCELLED)
    }

    suspend fun markNoShow(id: String) {
        updateStatus(id, AppointmentStatus.NO_SHOW)
    }

    suspend fun deleteAppointment(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        appointmentDao.softDelete(id, now)
    }

    // === Reminders ===

    suspend fun getAppointmentsForReminders(): List<AppointmentEntity> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        return appointmentDao.getForReminders(now)
    }

    // === Sync ===

    suspend fun getUnsyncedAppointments(): List<AppointmentEntity> = appointmentDao.getUnsynced()

    suspend fun markAppointmentSynced(id: String, serverId: String) {
        appointmentDao.markSynced(id, serverId)
    }

    // === Stats ===

    suspend fun getRevenueByDateRange(start: LocalDate, end: LocalDate): Long {
        return appointmentDao.getRevenueByDateRange(start.toString(), end.toString())
    }
}
