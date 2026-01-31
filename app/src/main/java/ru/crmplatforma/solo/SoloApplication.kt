package ru.crmplatforma.solo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SoloApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Канал для записей
            val appointmentsChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Записи",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Напоминания о записях"
            }

            // Канал для задач
            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Задачи",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о задачах"
            }

            // Канал для подписок
            val subscriptionsChannel = NotificationChannel(
                CHANNEL_SUBSCRIPTIONS,
                "Подписки",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о платежах"
            }

            // Канал для дней рождения
            val birthdaysChannel = NotificationChannel(
                CHANNEL_BIRTHDAYS,
                "Дни рождения",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Дни рождения клиентов"
            }

            // Канал для склада
            val inventoryChannel = NotificationChannel(
                CHANNEL_INVENTORY,
                "Склад",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о низком остатке"
            }

            // Сервисный канал (ICE, синхронизация)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Сервисные",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "ICE, синхронизация"
            }

            notificationManager.createNotificationChannels(
                listOf(
                    appointmentsChannel,
                    tasksChannel,
                    subscriptionsChannel,
                    birthdaysChannel,
                    inventoryChannel,
                    serviceChannel
                )
            )
        }
    }

    companion object {
        const val CHANNEL_APPOINTMENTS = "appointments"
        const val CHANNEL_TASKS = "tasks"
        const val CHANNEL_SUBSCRIPTIONS = "subscriptions"
        const val CHANNEL_BIRTHDAYS = "birthdays"
        const val CHANNEL_INVENTORY = "inventory"
        const val CHANNEL_SERVICE = "service"
    }
}
