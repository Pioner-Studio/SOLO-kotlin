package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Период подписки.
 */
enum class SubscriptionPeriod {
    MONTHLY,  // Ежемесячно
    YEARLY    // Ежегодно
}

/**
 * Подписка — регулярный платёж.
 *
 * Примеры: аренда, интернет, подписка на сервисы.
 * Напоминания: за 3 дня, за 1 день, в день оплаты.
 */
@Entity(
    tableName = "subscriptions",
    indices = [
        Index("nextPaymentDate"),
        Index("isActive"),
        Index("synced")
    ]
)
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val name: String,                        // Название (Аренда, Интернет)
    val amountKopecks: Long,                 // Сумма в копейках
    val period: SubscriptionPeriod = SubscriptionPeriod.MONTHLY,
    val billingDay: Int,                     // День оплаты (1-31)
    val description: String? = null,         // Примечание

    val nextPaymentDate: LocalDate,          // Следующая дата оплаты
    val isActive: Boolean = true,            // Активна?

    // Напоминания
    val remind3Days: Boolean = true,         // За 3 дня
    val remind1Day: Boolean = true,          // За 1 день
    val remindOnDay: Boolean = true,         // В день оплаты

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
