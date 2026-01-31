package ru.crmplatforma.solo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * Тип записи в календаре.
 */
enum class AppointmentType {
    VISIT,  // Визит клиента (основной тип)
    NOTE,   // Заметка без клиента
    BLOCK   // Блокировка времени (выходной, занято)
}

/**
 * Статус записи.
 */
enum class AppointmentStatus {
    SCHEDULED,   // Запланирована
    COMPLETED,   // Завершена (услуга оказана)
    CANCELLED,   // Отменена
    NO_SHOW      // Клиент не пришёл
}

/**
 * Тип повторения записи.
 */
enum class RepeatType {
    NONE,        // Без повтора
    WEEKLY,      // Каждую неделю
    BIWEEKLY,    // Каждые 2 недели
    MONTHLY,     // Каждый месяц
    CUSTOM       // Каждые N дней
}

/**
 * Запись в календаре — центральная сущность расписания.
 *
 * Поддерживает:
 * - Визиты клиентов с услугами
 * - Заметки без клиента
 * - Блокировки времени
 * - Ночные записи (пересечение полуночи)
 * - Повторяющиеся записи
 */
@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clientId"),
        Index("startAt"),
        Index("endAt"),
        Index("status"),
        Index("type"),
        Index("synced")
    ]
)
data class AppointmentEntity(
    @PrimaryKey
    val id: String,                          // UUID

    val type: AppointmentType,               // VISIT / NOTE / BLOCK
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,

    // Время
    val startAt: OffsetDateTime,             // Начало
    val endAt: OffsetDateTime,               // Конец

    // Клиент (только для VISIT)
    val clientId: String? = null,            // FK → clients
    val clientName: String? = null,          // Денормализация для быстрого отображения

    // Услуги (JSON массив ID услуг)
    val serviceIdsJson: String? = null,      // ["uuid1", "uuid2"]
    val servicesSnapshot: String? = null,    // Снапшот услуг на момент записи (JSON)

    // Финансы
    val totalPriceKopecks: Long = 0,         // Итого (сумма услуг)
    val paidKopecks: Long = 0,               // Оплачено

    // Заметка (для NOTE и BLOCK)
    val title: String? = null,               // Заголовок
    val notes: String? = null,               // Подробности
    val color: String? = null,               // Цвет (#RRGGBB)

    // Повторение
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatEveryDays: Int? = null,        // Для CUSTOM: каждые N дней
    val repeatUntil: OffsetDateTime? = null, // До какой даты повторять
    val repeatParentId: String? = null,      // ID родительской записи (для повторов)

    // Напоминания
    val remind24h: Boolean = true,           // Напомнить за 24ч
    val remind1h: Boolean = true,            // Напомнить за 1ч
    val remind15m: Boolean = false,          // Напомнить за 15м

    // Синхронизация
    val synced: Boolean = false,
    val serverId: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val deletedAt: OffsetDateTime? = null
)
