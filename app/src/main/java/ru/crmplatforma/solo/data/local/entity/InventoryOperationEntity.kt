package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * Тип операции со складом.
 */
enum class InventoryOperationType {
    IN,      // Приход (поступление)
    OUT,     // Расход (использование)
    ADJUST   // Корректировка (инвентаризация)
}

/**
 * Операция со складом — движение материала.
 *
 * История всех приходов, расходов и корректировок.
 */
@Entity(
    tableName = "inventory_operations",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("createdAt"),
        Index("synced")
    ]
)
data class InventoryOperationEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val itemId: String,                      // FK → inventory_items
    val type: InventoryOperationType,        // IN / OUT / ADJUST
    val quantity: Double,                    // Количество (+ или -)
    val note: String? = null,                // Комментарий

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime
)
