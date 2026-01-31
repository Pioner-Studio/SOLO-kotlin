package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * Единица измерения материала.
 */
enum class InventoryUnit {
    PIECE,    // Штука
    ML,       // Миллилитр
    GRAM,     // Грамм
    PACK      // Упаковка
}

/**
 * Материал на складе.
 *
 * Отслеживает количество, минимальный запас.
 * При quantity <= minQuantity появляется в "Low Stock" виджете.
 */
@Entity(
    tableName = "inventory_items",
    indices = [
        Index("name"),
        Index("synced")
    ]
)
data class InventoryItemEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val name: String,                        // Название материала
    val unit: InventoryUnit = InventoryUnit.PIECE,
    val quantity: Double = 0.0,              // Текущее количество
    val minQuantity: Double = 0.0,           // Минимальный запас (для предупреждения)
    val description: String? = null,         // Описание
    val category: String? = null,            // Категория

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
