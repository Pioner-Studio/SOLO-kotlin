package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.ServiceDao
import ru.crmplatforma.solo.data.local.entity.ServiceEntity
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ServiceRepository — работа с услугами.
 */
@Singleton
class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao
) {
    // === Queries ===

    fun getActiveServices(): Flow<List<ServiceEntity>> = serviceDao.getActiveFlow()

    fun getAllServices(): Flow<List<ServiceEntity>> = serviceDao.getAllFlow()

    fun searchServices(query: String): Flow<List<ServiceEntity>> = serviceDao.searchFlow(query)

    suspend fun getServiceById(id: String): ServiceEntity? = serviceDao.getById(id)

    suspend fun getServicesByIds(ids: List<String>): List<ServiceEntity> = serviceDao.getByIds(ids)

    // === Commands ===

    suspend fun createService(
        name: String,
        priceKopecks: Long,
        durationMinutes: Int,
        description: String? = null,
        category: String? = null,
        color: String? = null
    ): ServiceEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val service = ServiceEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            priceKopecks = priceKopecks,
            durationMinutes = durationMinutes,
            description = description,
            category = category,
            color = color,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        serviceDao.insert(service)
        return service
    }

    suspend fun updateService(service: ServiceEntity) {
        val updated = service.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        serviceDao.update(updated)
    }

    suspend fun archiveService(id: String) {
        serviceDao.archive(id)
    }

    suspend fun unarchiveService(id: String) {
        serviceDao.unarchive(id)
    }

    // === Sync ===

    suspend fun getUnsyncedServices(): List<ServiceEntity> = serviceDao.getUnsynced()

    suspend fun markServiceSynced(id: String, serverId: String) {
        serviceDao.markSynced(id, serverId)
    }
}
