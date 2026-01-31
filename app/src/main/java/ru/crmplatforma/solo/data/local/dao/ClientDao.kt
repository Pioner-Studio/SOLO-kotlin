package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.ClientEntity
import java.time.LocalDate

@Dao
interface ClientDao {

    // === Вставка / Обновление ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<ClientEntity>)

    @Update
    suspend fun update(client: ClientEntity)

    // === Запросы ===

    @Query("SELECT * FROM clients WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllFlow(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE deletedAt IS NULL ORDER BY name ASC")
    suspend fun getAll(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE phone = :phone AND deletedAt IS NULL LIMIT 1")
    suspend fun getByPhone(phone: String): ClientEntity?

    // Поиск по имени или телефону
    @Query("""
        SELECT * FROM clients
        WHERE deletedAt IS NULL
          AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchFlow(query: String): Flow<List<ClientEntity>>

    // VIP клиенты
    @Query("SELECT * FROM clients WHERE isVip = 1 AND deletedAt IS NULL ORDER BY name ASC")
    fun getVipFlow(): Flow<List<ClientEntity>>

    // Дни рождения в диапазоне (для виджета "Ближайшие ДР")
    @Query("""
        SELECT * FROM clients
        WHERE deletedAt IS NULL
          AND birthday IS NOT NULL
        ORDER BY
          CASE
            WHEN SUBSTR(birthday, 6) >= SUBSTR(:today, 6)
            THEN SUBSTR(birthday, 6)
            ELSE '13' || SUBSTR(birthday, 6)
          END
        LIMIT :limit
    """)
    fun getUpcomingBirthdaysFlow(today: String, limit: Int = 10): Flow<List<ClientEntity>>

    // === Синхронизация ===

    @Query("SELECT * FROM clients WHERE synced = 0")
    suspend fun getUnsynced(): List<ClientEntity>

    @Query("UPDATE clients SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)

    // === Soft Delete ===

    @Query("UPDATE clients SET deletedAt = :deletedAt, synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String)

    // === Статистика ===

    @Query("SELECT COUNT(*) FROM clients WHERE deletedAt IS NULL")
    fun getCountFlow(): Flow<Int>

    @Query("""
        UPDATE clients
        SET totalVisits = totalVisits + 1,
            lastVisitAt = :visitAt,
            totalSpent = totalSpent + :spentKopecks,
            synced = 0
        WHERE id = :clientId
    """)
    suspend fun updateStats(clientId: String, visitAt: String, spentKopecks: Long)
}
