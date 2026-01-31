package ru.crmplatforma.solo.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import ru.crmplatforma.solo.MainActivity
import ru.crmplatforma.solo.R

/**
 * Firebase Cloud Messaging сервис для SOLO.
 *
 * Обрабатывает входящие push-уведомления и обновление FCM токена.
 * FCM — это BACKUP механизм, основные напоминания работают через WorkManager.
 */
@AndroidEntryPoint
class SoloFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Данные из push
        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "SOLO"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val channelId = data["channel_id"] ?: "appointments"
        val deepLink = data["deep_link"]

        showNotification(title, body, channelId, deepLink)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Отправить новый токен на сервер при реализации синхронизации
        // SyncManager.updateFcmToken(token)
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        deepLink: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            deepLink?.let { putExtra("deep_link", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
