package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * ICE (In Case of Emergency) — экстренный контакт.
 *
 * Хранит информацию о владельце и до 3 контактных лиц.
 * Отображается крупным текстом для быстрого доступа.
 *
 * Напоминание обновить данные: раз в 6 месяцев.
 */
@Entity(
    tableName = "ice_contacts",
    indices = [
        Index("synced")
    ]
)
data class ICEContactEntity(
    @PrimaryKey
    val id: String,                          // UUID (обычно один на пользователя)

    // Владелец (о ком информация)
    val ownerName: String,                   // ФИО
    val ownerBloodType: String? = null,      // Группа крови
    val ownerAllergies: String? = null,      // Аллергии
    val ownerMedications: String? = null,    // Принимаемые препараты
    val ownerMedicalNotes: String? = null,   // Медицинские особенности

    // Контакт 1 (обязательный)
    val contact1Name: String,                // Имя
    val contact1Phone: String,               // Телефон
    val contact1Relation: String? = null,    // Кто это (мама, муж, друг)

    // Контакт 2 (опциональный)
    val contact2Name: String? = null,
    val contact2Phone: String? = null,
    val contact2Relation: String? = null,

    // Контакт 3 (опциональный)
    val contact3Name: String? = null,
    val contact3Phone: String? = null,
    val contact3Relation: String? = null,

    // Метаданные
    val lastUpdatedReminder: OffsetDateTime? = null, // Когда последний раз напоминали обновить

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
