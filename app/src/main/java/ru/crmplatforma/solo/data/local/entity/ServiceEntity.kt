package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * Услуга — то, что мы предлагаем клиентам.
 *
 * Цена в копейках (Long), чтобы избежать проблем с плавающей точкой.
 * Длительность в минутах.
 */
@Entity(
    tableName = "services",
    indices = [
        Index("name"),
        Index("isArchived"),
        Index("synced")
    ]
)
data class ServiceEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val name: String,                        // Название услуги
    val priceKopecks: Long,                  // Цена в копейках (100 = 1 рубль)
    val durationMinutes: Int,                // Длительность в минутах
    val description: String? = null,         // Описание
    val category: String? = null,            // Категория (опционально)
    val color: String? = null,               // Цвет для отображения в календаре (#RRGGBB)

    val isArchived: Boolean = false,         // Архивная (не удаляем, скрываем)

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
