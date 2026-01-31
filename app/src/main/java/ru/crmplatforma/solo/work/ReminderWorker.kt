package ru.crmplatforma.solo.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.crmplatforma.solo.MainActivity
import ru.crmplatforma.solo.R

/**
 * ReminderWorker — базовый worker для всех напоминаний.
 *
 * Показывает уведомление с переданными параметрами.
 * Используется для:
 * - Напоминаний о записях (24ч, 1ч, 15м)
 * - Напоминаний о задачах
 * - Напоминаний о подписках
 * - Напоминаний о днях рождения
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: ""
        val channelId = inputData.getString(KEY_CHANNEL_ID) ?: CHANNEL_APPOINTMENTS
        val deepLink = inputData.getString(KEY_DEEP_LINK)
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, System.currentTimeMillis().toInt())

        showNotification(title, body, channelId, deepLink, notificationId)

        return Result.success()
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        deepLink: String?,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            deepLink?.let { putExtra("deep_link", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_CHANNEL_ID = "channel_id"
        const val KEY_DEEP_LINK = "deep_link"
        const val KEY_NOTIFICATION_ID = "notification_id"

        // Каналы уведомлений
        const val CHANNEL_APPOINTMENTS = "appointments"
        const val CHANNEL_TASKS = "tasks"
        const val CHANNEL_SUBSCRIPTIONS = "subscriptions"
        const val CHANNEL_BIRTHDAYS = "birthdays"
        const val CHANNEL_INVENTORY = "inventory"
        const val CHANNEL_SERVICE = "service"
    }
}
