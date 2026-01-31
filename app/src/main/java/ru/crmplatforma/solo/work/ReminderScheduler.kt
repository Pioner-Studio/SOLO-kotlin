package ru.crmplatforma.solo.work

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.crmplatforma.solo.data.local.entity.AppointmentEntity
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity
import ru.crmplatforma.solo.data.local.entity.TaskEntity
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReminderScheduler — планировщик напоминаний.
 *
 * Отвечает за:
 * - Создание WorkRequests для напоминаний
 * - Отмену напоминаний
 * - Пересчёт при изменении данных
 *
 * Философия: WorkManager — гарантия доставки.
 * Даже если приложение закрыто, напоминание придёт.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    // === Записи (Appointments) ===

    /**
     * Планирует напоминания для записи.
     * Создаёт до 3 напоминаний: 24ч, 1ч, 15м до начала.
     */
    fun scheduleAppointmentReminders(appointment: AppointmentEntity) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val startAt = appointment.startAt
        val clientName = appointment.clientName ?: "Запись"

        // 24 часа до
        if (appointment.remind24h) {
            val remindAt = startAt.minusHours(24)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "${appointment.id}_24h",
                    title = "Завтра: $clientName",
                    body = formatAppointmentBody(appointment),
                    channelId = ReminderWorker.CHANNEL_APPOINTMENTS,
                    deepLink = "solo://appointment/${appointment.id}",
                    scheduledAt = remindAt
                )
            }
        }

        // 1 час до
        if (appointment.remind1h) {
            val remindAt = startAt.minusHours(1)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "${appointment.id}_1h",
                    title = "Через час: $clientName",
                    body = formatAppointmentBody(appointment),
                    channelId = ReminderWorker.CHANNEL_APPOINTMENTS,
                    deepLink = "solo://appointment/${appointment.id}",
                    scheduledAt = remindAt
                )
            }
        }

        // 15 минут до
        if (appointment.remind15m) {
            val remindAt = startAt.minusMinutes(15)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "${appointment.id}_15m",
                    title = "Через 15 минут: $clientName",
                    body = formatAppointmentBody(appointment),
                    channelId = ReminderWorker.CHANNEL_APPOINTMENTS,
                    deepLink = "solo://appointment/${appointment.id}",
                    scheduledAt = remindAt
                )
            }
        }
    }

    /**
     * Отменяет все напоминания для записи.
     */
    fun cancelAppointmentReminders(appointmentId: String) {
        workManager.cancelAllWorkByTag("reminder_${appointmentId}_24h")
        workManager.cancelAllWorkByTag("reminder_${appointmentId}_1h")
        workManager.cancelAllWorkByTag("reminder_${appointmentId}_15m")
    }

    private fun formatAppointmentBody(appointment: AppointmentEntity): String {
        val time = appointment.startAt.toLocalTime()
        return "В ${time.hour}:${time.minute.toString().padStart(2, '0')}"
    }

    // === Задачи (Tasks) ===

    fun scheduleTaskReminder(task: TaskEntity) {
        val remindAt = task.remindAt ?: return
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        if (remindAt.isAfter(now)) {
            scheduleReminder(
                id = "task_${task.id}",
                title = "Задача: ${task.title}",
                body = task.description ?: "Напоминание о задаче",
                channelId = ReminderWorker.CHANNEL_TASKS,
                deepLink = "solo://task/${task.id}",
                scheduledAt = remindAt
            )
        }
    }

    fun cancelTaskReminder(taskId: String) {
        workManager.cancelAllWorkByTag("reminder_task_$taskId")
    }

    // === Подписки (Subscriptions) ===

    fun scheduleSubscriptionReminders(subscription: SubscriptionEntity) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val paymentDate = subscription.nextPaymentDate.atStartOfDay().atOffset(ZoneOffset.UTC)

        // За 3 дня
        if (subscription.remind3Days) {
            val remindAt = paymentDate.minusDays(3).withHour(10)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "sub_${subscription.id}_3d",
                    title = "Оплата через 3 дня",
                    body = "${subscription.name}: ${subscription.amountKopecks / 100} ₽",
                    channelId = ReminderWorker.CHANNEL_SUBSCRIPTIONS,
                    deepLink = "solo://subscription/${subscription.id}",
                    scheduledAt = remindAt
                )
            }
        }

        // За 1 день
        if (subscription.remind1Day) {
            val remindAt = paymentDate.minusDays(1).withHour(10)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "sub_${subscription.id}_1d",
                    title = "Оплата завтра",
                    body = "${subscription.name}: ${subscription.amountKopecks / 100} ₽",
                    channelId = ReminderWorker.CHANNEL_SUBSCRIPTIONS,
                    deepLink = "solo://subscription/${subscription.id}",
                    scheduledAt = remindAt
                )
            }
        }

        // В день оплаты
        if (subscription.remindOnDay) {
            val remindAt = paymentDate.withHour(9)
            if (remindAt.isAfter(now)) {
                scheduleReminder(
                    id = "sub_${subscription.id}_0d",
                    title = "Сегодня оплата",
                    body = "${subscription.name}: ${subscription.amountKopecks / 100} ₽",
                    channelId = ReminderWorker.CHANNEL_SUBSCRIPTIONS,
                    deepLink = "solo://subscription/${subscription.id}",
                    scheduledAt = remindAt
                )
            }
        }
    }

    fun cancelSubscriptionReminders(subscriptionId: String) {
        workManager.cancelAllWorkByTag("reminder_sub_${subscriptionId}_3d")
        workManager.cancelAllWorkByTag("reminder_sub_${subscriptionId}_1d")
        workManager.cancelAllWorkByTag("reminder_sub_${subscriptionId}_0d")
    }

    // === Дни рождения ===

    fun scheduleBirthdayReminder(
        clientId: String,
        clientName: String,
        birthdayDate: OffsetDateTime
    ) {
        val remindAt = birthdayDate.withHour(9) // Утром в 9:00

        scheduleReminder(
            id = "birthday_$clientId",
            title = "День рождения!",
            body = "Сегодня день рождения: $clientName",
            channelId = ReminderWorker.CHANNEL_BIRTHDAYS,
            deepLink = "solo://client/$clientId",
            scheduledAt = remindAt
        )
    }

    // === Склад (Low Stock) ===

    fun scheduleLowStockReminder(itemId: String, itemName: String) {
        // Напоминание сразу (или через минуту для группировки)
        scheduleReminder(
            id = "lowstock_$itemId",
            title = "Заканчивается: $itemName",
            body = "Пора пополнить запасы",
            channelId = ReminderWorker.CHANNEL_INVENTORY,
            deepLink = "solo://inventory/$itemId",
            scheduledAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1)
        )
    }

    // === Базовый метод планирования ===

    private fun scheduleReminder(
        id: String,
        title: String,
        body: String,
        channelId: String,
        deepLink: String?,
        scheduledAt: OffsetDateTime
    ) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val delay = Duration.between(now, scheduledAt).toMillis()

        if (delay <= 0) return // Уже прошло

        val data = workDataOf(
            ReminderWorker.KEY_TITLE to title,
            ReminderWorker.KEY_BODY to body,
            ReminderWorker.KEY_CHANNEL_ID to channelId,
            ReminderWorker.KEY_DEEP_LINK to deepLink,
            ReminderWorker.KEY_NOTIFICATION_ID to id.hashCode()
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$id")
            .build()

        // Заменяем существующий work с тем же тегом
        workManager.enqueueUniqueWork(
            "reminder_$id",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // === Утилиты ===

    /**
     * Отменяет все напоминания.
     * Использовать при logout или очистке данных.
     */
    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }
}
