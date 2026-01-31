package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.ICEDao
import ru.crmplatforma.solo.data.local.entity.ICEContactEntity
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ICERepository — экстренные контакты (In Case of Emergency).
 *
 * Обычно один ICE на пользователя.
 */
@Singleton
class ICERepository @Inject constructor(
    private val iceDao: ICEDao
) {
    // === Queries ===

    fun getICE(): Flow<ICEContactEntity?> = iceDao.getFlow()

    suspend fun getICEOnce(): ICEContactEntity? = iceDao.get()

    fun hasICE(): Flow<Boolean> = iceDao.hasICEFlow()

    // === Commands ===

    suspend fun saveICE(
        ownerName: String,
        ownerBloodType: String? = null,
        ownerAllergies: String? = null,
        ownerMedications: String? = null,
        ownerMedicalNotes: String? = null,
        contact1Name: String,
        contact1Phone: String,
        contact1Relation: String? = null,
        contact2Name: String? = null,
        contact2Phone: String? = null,
        contact2Relation: String? = null,
        contact3Name: String? = null,
        contact3Phone: String? = null,
        contact3Relation: String? = null
    ): ICEContactEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        // Проверяем, есть ли уже ICE
        val existing = iceDao.get()
        val id = existing?.id ?: UUID.randomUUID().toString()

        val ice = ICEContactEntity(
            id = id,
            ownerName = ownerName,
            ownerBloodType = ownerBloodType,
            ownerAllergies = ownerAllergies,
            ownerMedications = ownerMedications,
            ownerMedicalNotes = ownerMedicalNotes,
            contact1Name = contact1Name,
            contact1Phone = contact1Phone,
            contact1Relation = contact1Relation,
            contact2Name = contact2Name,
            contact2Phone = contact2Phone,
            contact2Relation = contact2Relation,
            contact3Name = contact3Name,
            contact3Phone = contact3Phone,
            contact3Relation = contact3Relation,
            synced = false,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        iceDao.insert(ice)
        return ice
    }

    suspend fun deleteICE() {
        val ice = iceDao.get() ?: return
        iceDao.delete(ice)
    }

    suspend fun markReminded(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        iceDao.markReminded(id, now)
    }

    // === Sync ===

    suspend fun getUnsyncedICE(): List<ICEContactEntity> = iceDao.getUnsynced()

    suspend fun markICESynced(id: String, serverId: String) {
        iceDao.markSynced(id, serverId)
    }
}
