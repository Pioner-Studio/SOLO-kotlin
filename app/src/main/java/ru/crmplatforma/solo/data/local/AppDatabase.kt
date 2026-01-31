package ru.crmplatforma.solo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.crmplatforma.solo.data.local.dao.*
import ru.crmplatforma.solo.data.local.entity.*

/**
 * SOLO Database — единственный источник правды.
 *
 * Offline-first философия:
 * - Все данные сначала сохраняются локально
 * - synced = false пока не отправлено на сервер
 * - При конфликте: server wins (для MVP)
 *
 * Версионирование:
 * - Начинаем с version = 1
 * - При изменении схемы — инкремент + миграция
 */
@Database(
    entities = [
        ClientEntity::class,
        ServiceEntity::class,
        AppointmentEntity::class,
        TransactionEntity::class,
        TaskEntity::class,
        SubscriptionEntity::class,
        InventoryItemEntity::class,
        InventoryOperationEntity::class,
        ICEContactEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun taskDao(): TaskDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun iceDao(): ICEDao

    companion object {
        const val DATABASE_NAME = "solo_database"
    }
}
