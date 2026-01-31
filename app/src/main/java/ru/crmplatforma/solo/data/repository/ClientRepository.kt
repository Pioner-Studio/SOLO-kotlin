package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.ClientDao
import ru.crmplatforma.solo.data.local.entity.ClientEntity
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClientRepository — работа с клиентами.
 *
 * Отвечает за:
 * - CRUD операции с клиентами
 * - Поиск и фильтрация
 * - Генерация UUID
 * - Подготовка данных для синхронизации
 */
@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    // === Queries ===

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllFlow()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchFlow(query)

    fun getVipClients(): Flow<List<ClientEntity>> = clientDao.getVipFlow()

    fun getClientById(id: String): Flow<ClientEntity?> = clientDao.getByIdFlow(id)

    suspend fun getClientByIdOnce(id: String): ClientEntity? = clientDao.getById(id)

    suspend fun getClientByPhone(phone: String): ClientEntity? = clientDao.getByPhone(phone)

    fun getUpcomingBirthdays(limit: Int = 10): Flow<List<ClientEntity>> {
        val today = LocalDate.now().toString()
        return clientDao.getUpcomingBirthdaysFlow(today, limit)
    }

    fun getClientCount(): Flow<Int> = clientDao.getCountFlow()

    fun getClientsWithBirthdayThisWeek(): Flow<List<ClientEntity>> {
        val today = LocalDate.now()
        val weekEnd = today.plusDays(7)
        // Конвертируем в MM-dd формат для сравнения (без года)
        return clientDao.getBirthdaysInRangeFlow(
            today.toString().substring(5), // MM-dd
            weekEnd.toString().substring(5)
        )
    }

    // === Commands ===

    suspend fun createClient(
        name: String,
        phone: String? = null,
        email: String? = null,
        birthday: LocalDate? = null,
        notes: String? = null,
        isVip: Boolean = false,
        customFieldsJson: String? = null
    ): ClientEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val client = ClientEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            phone = phone,
            email = email,
            birthday = birthday,
            notes = notes,
            isVip = isVip,
            customFieldsJson = customFieldsJson,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        clientDao.insert(client)
        return client
    }

    suspend fun updateClient(client: ClientEntity) {
        val updated = client.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        clientDao.update(updated)
    }

    suspend fun deleteClient(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        clientDao.softDelete(id, now)
    }

    suspend fun toggleVip(id: String) {
        val client = clientDao.getById(id) ?: return
        updateClient(client.copy(isVip = !client.isVip))
    }

    // === Sync ===

    suspend fun getUnsyncedClients(): List<ClientEntity> = clientDao.getUnsynced()

    suspend fun markClientSynced(id: String, serverId: String) {
        clientDao.markSynced(id, serverId)
    }

    // === Stats ===

    suspend fun updateClientStats(clientId: String, visitAt: OffsetDateTime, spentKopecks: Long) {
        clientDao.updateStats(clientId, visitAt.toString(), spentKopecks)
    }
}
