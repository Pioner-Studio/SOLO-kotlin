package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<ServiceEntity>)

    @Update
    suspend fun update(service: ServiceEntity)

    // Активные услуги (не архивные)
    @Query("SELECT * FROM services WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE isArchived = 0 ORDER BY name ASC")
    suspend fun getActive(): List<ServiceEntity>

    // Все услуги (включая архивные)
    @Query("SELECT * FROM services ORDER BY isArchived ASC, name ASC")
    fun getAllFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: String): ServiceEntity?

    @Query("SELECT * FROM services WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ServiceEntity>

    // Поиск
    @Query("SELECT * FROM services WHERE isArchived = 0 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFlow(query: String): Flow<List<ServiceEntity>>

    // Архивация (вместо удаления)
    @Query("UPDATE services SET isArchived = 1, synced = 0 WHERE id = :id")
    suspend fun archive(id: String)

    // Разархивация
    @Query("UPDATE services SET isArchived = 0, synced = 0 WHERE id = :id")
    suspend fun unarchive(id: String)

    // Синхронизация
    @Query("SELECT * FROM services WHERE synced = 0")
    suspend fun getUnsynced(): List<ServiceEntity>

    @Query("UPDATE services SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)
}
