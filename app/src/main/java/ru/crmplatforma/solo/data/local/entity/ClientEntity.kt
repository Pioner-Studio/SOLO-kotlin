package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Клиент — основная сущность CRM.
 *
 * Философия: клиент — это человек, которому мы помогаем.
 * У него есть история, предпочтения, день рождения.
 */
@Entity(
    tableName = "clients",
    indices = [
        Index("phone"),
        Index("name"),
        Index("birthday"),
        Index("synced")
    ]
)
data class ClientEntity(
    @PrimaryKey
    val id: String,                          // UUID, генерируется локально

    val name: String,                        // ФИО
    val phone: String? = null,               // Телефон (основной идентификатор)
    val email: String? = null,               // Email
    val birthday: LocalDate? = null,         // День рождения (для напоминаний)
    val notes: String? = null,               // Заметки о клиенте
    val isVip: Boolean = false,              // VIP-статус
    val avatarUrl: String? = null,           // URL аватара (если есть)

    // Динамические поля для разных специализаций (JSON)
    val customFieldsJson: String? = null,    // {"skinType": "сухая", "allergies": ["латекс"]}

    // Статистика (денормализация для быстрого отображения)
    val totalVisits: Int = 0,                // Всего визитов
    val lastVisitAt: OffsetDateTime? = null, // Последний визит
    val totalSpent: Long = 0,                // Всего потрачено (в копейках)

    // Синхронизация
    val synced: Boolean = false,             // Отправлено на сервер?
    val serverId: String? = null,            // ID на сервере (после синхронизации)
    val createdAt: OffsetDateTime,           // Создан
    val updatedAt: OffsetDateTime,           // Обновлён
    val deletedAt: OffsetDateTime? = null    // Soft delete
)
