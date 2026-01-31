package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    // === Запросы ===

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: String): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getByIdFlow(id: String): Flow<SubscriptionEntity?>

    // Активные подписки
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextPaymentDate ASC")
    fun getActiveFlow(): Flow<List<SubscriptionEntity>>

    // Все подписки
    @Query("SELECT * FROM subscriptions ORDER BY isActive DESC, nextPaymentDate ASC")
    fun getAllFlow(): Flow<List<SubscriptionEntity>>

    // Подписки с ближайшей оплатой (для виджета)
    @Query("""
        SELECT * FROM subscriptions
        WHERE isActive = 1
          AND nextPaymentDate <= :limitDate
        ORDER BY nextPaymentDate ASC
    """)
    fun getUpcomingFlow(limitDate: String): Flow<List<SubscriptionEntity>>

    // === Действия ===

    // Отметить как оплаченную (сдвинуть дату)
    @Query("UPDATE subscriptions SET nextPaymentDate = :newDate, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPaid(id: String, newDate: String, updatedAt: String)

    // Деактивировать
    @Query("UPDATE subscriptions SET isActive = 0, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivate(id: String, updatedAt: String)

    // Активировать
    @Query("UPDATE subscriptions SET isActive = 1, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun activate(id: String, updatedAt: String)

    // === Напоминания ===

    @Query("""
        SELECT * FROM subscriptions
        WHERE isActive = 1
          AND (
            (remind3Days = 1 AND nextPaymentDate = :in3Days) OR
            (remind1Day = 1 AND nextPaymentDate = :in1Day) OR
            (remindOnDay = 1 AND nextPaymentDate = :today)
          )
    """)
    suspend fun getForReminders(today: String, in1Day: String, in3Days: String): List<SubscriptionEntity>

    // === Синхронизация ===

    @Query("SELECT * FROM subscriptions WHERE synced = 0")
    suspend fun getUnsynced(): List<SubscriptionEntity>

    @Query("UPDATE subscriptions SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)

    // === Статистика ===

    // Сумма ежемесячных подписок
    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM subscriptions WHERE isActive = 1 AND period = 'MONTHLY'")
    fun getMonthlyTotalFlow(): Flow<Long>

    // Количество активных
    @Query("SELECT COUNT(*) FROM subscriptions WHERE isActive = 1")
    fun getActiveCountFlow(): Flow<Int>

    // Количество с ближайшей оплатой (в течение 7 дней)
    @Query("SELECT COUNT(*) FROM subscriptions WHERE isActive = 1 AND nextPaymentDate <= :limitDate")
    fun getUpcomingCountFlow(limitDate: String): Flow<Int>
}
