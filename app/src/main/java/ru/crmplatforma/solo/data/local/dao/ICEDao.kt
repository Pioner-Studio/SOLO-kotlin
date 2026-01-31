package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.ICEContactEntity

@Dao
interface ICEDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ice: ICEContactEntity)

    @Update
    suspend fun update(ice: ICEContactEntity)

    @Delete
    suspend fun delete(ice: ICEContactEntity)

    // Обычно один ICE на пользователя
    @Query("SELECT * FROM ice_contacts LIMIT 1")
    suspend fun get(): ICEContactEntity?

    @Query("SELECT * FROM ice_contacts LIMIT 1")
    fun getFlow(): Flow<ICEContactEntity?>

    @Query("SELECT * FROM ice_contacts WHERE id = :id")
    suspend fun getById(id: String): ICEContactEntity?

    // Проверка: заполнен ли ICE
    @Query("SELECT COUNT(*) > 0 FROM ice_contacts")
    fun hasICEFlow(): Flow<Boolean>

    // Напоминание обновить (раз в 6 месяцев)
    @Query("UPDATE ice_contacts SET lastUpdatedReminder = :now WHERE id = :id")
    suspend fun markReminded(id: String, now: String)

    // Синхронизация
    @Query("SELECT * FROM ice_contacts WHERE synced = 0")
    suspend fun getUnsynced(): List<ICEContactEntity>

    @Query("UPDATE ice_contacts SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)
}
