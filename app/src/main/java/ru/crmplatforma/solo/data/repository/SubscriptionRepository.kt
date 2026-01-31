package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.SubscriptionDao
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity
import ru.crmplatforma.solo.data.local.entity.SubscriptionPeriod
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SubscriptionRepository — работа с подписками (регулярными платежами).
 */
@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    // === Queries ===

    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getActiveFlow()

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getAllFlow()

    fun getUpcomingSubscriptions(daysAhead: Int = 7): Flow<List<SubscriptionEntity>> {
        val limitDate = LocalDate.now().plusDays(daysAhead.toLong()).toString()
        return subscriptionDao.getUpcomingFlow(limitDate)
    }

    fun getSubscriptionById(id: String): Flow<SubscriptionEntity?> = subscriptionDao.getByIdFlow(id)

    fun getMonthlyTotal(): Flow<Long> = subscriptionDao.getMonthlyTotalFlow()

    fun getActiveCount(): Flow<Int> = subscriptionDao.getActiveCountFlow()

    fun getUpcomingCount(daysAhead: Int = 7): Flow<Int> {
        val limitDate = LocalDate.now().plusDays(daysAhead.toLong()).toString()
        return subscriptionDao.getUpcomingCountFlow(limitDate)
    }

    // === Commands ===

    suspend fun createSubscription(
        name: String,
        amountKopecks: Long,
        billingDay: Int,
        period: SubscriptionPeriod = SubscriptionPeriod.MONTHLY,
        description: String? = null,
        remind3Days: Boolean = true,
        remind1Day: Boolean = true,
        remindOnDay: Boolean = true
    ): SubscriptionEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        // Вычисляем следующую дату оплаты
        val today = LocalDate.now()
        val nextPaymentDate = if (today.dayOfMonth <= billingDay) {
            today.withDayOfMonth(billingDay.coerceAtMost(today.lengthOfMonth()))
        } else {
            val nextMonth = today.plusMonths(1)
            nextMonth.withDayOfMonth(billingDay.coerceAtMost(nextMonth.lengthOfMonth()))
        }

        val subscription = SubscriptionEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            amountKopecks = amountKopecks,
            period = period,
            billingDay = billingDay,
            description = description,
            nextPaymentDate = nextPaymentDate,
            remind3Days = remind3Days,
            remind1Day = remind1Day,
            remindOnDay = remindOnDay,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        subscriptionDao.insert(subscription)
        return subscription
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        val updated = subscription.copy(
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            synced = false
        )
        subscriptionDao.update(updated)
    }

    suspend fun markPaid(id: String) {
        val subscription = subscriptionDao.getById(id) ?: return
        val newDate = when (subscription.period) {
            SubscriptionPeriod.MONTHLY -> subscription.nextPaymentDate.plusMonths(1)
            SubscriptionPeriod.YEARLY -> subscription.nextPaymentDate.plusYears(1)
        }
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        subscriptionDao.markPaid(id, newDate.toString(), now)
    }

    suspend fun deactivate(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        subscriptionDao.deactivate(id, now)
    }

    suspend fun activate(id: String) {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        subscriptionDao.activate(id, now)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.delete(subscription)
    }

    // === Reminders ===

    suspend fun getSubscriptionsForReminders(): List<SubscriptionEntity> {
        val today = LocalDate.now()
        return subscriptionDao.getForReminders(
            today.toString(),
            today.plusDays(1).toString(),
            today.plusDays(3).toString()
        )
    }

    // === Sync ===

    suspend fun getUnsyncedSubscriptions(): List<SubscriptionEntity> = subscriptionDao.getUnsynced()

    suspend fun markSubscriptionSynced(id: String, serverId: String) {
        subscriptionDao.markSynced(id, serverId)
    }
}
