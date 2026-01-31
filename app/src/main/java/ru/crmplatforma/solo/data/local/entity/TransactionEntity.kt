package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Тип финансовой операции.
 */
enum class TransactionType {
    INCOME,   // Доход (оплата услуги)
    EXPENSE   // Расход (материалы, аренда и т.д.)
}

/**
 * Финансовая операция — доход или расход.
 *
 * Доходы создаются автоматически при завершении записи.
 * Расходы вводятся вручную.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AppointmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["appointmentId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("appointmentId"),
        Index("clientId"),
        Index("date"),
        Index("type"),
        Index("synced")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val type: TransactionType,               // INCOME / EXPENSE
    val amountKopecks: Long,                 // Сумма в копейках
    val date: LocalDate,                     // Дата операции
    val description: String? = null,         // Описание
    val category: String? = null,            // Категория расхода

    // Связи (для доходов)
    val appointmentId: String? = null,       // FK → appointments (авто-создание)
    val clientId: String? = null,            // FK → clients
    val clientName: String? = null,          // Денормализация

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
