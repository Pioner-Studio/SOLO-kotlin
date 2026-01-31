package ru.crmplatforma.solo.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController

/**
 * DeepLinkRouter — обработка deep links.
 *
 * Формат: solo://[entity]/[id]
 *
 * Примеры:
 * - solo://appointment/uuid — открыть запись
 * - solo://client/uuid — открыть клиента
 * - solo://task/uuid — открыть задачу
 * - solo://subscription/uuid — открыть подписку
 * - solo://inventory/uuid — открыть материал
 * - solo://ice — открыть ICE
 * - solo://calendar?date=2026-01-31 — открыть календарь на дату
 */
object DeepLinkRouter {

    /**
     * Обрабатывает Intent и возвращает route для навигации.
     */
    fun parseIntent(intent: Intent?): DeepLinkDestination? {
        intent ?: return null

        // Из Intent напрямую (схема solo://)
        val data = intent.data
        if (data != null && data.scheme == "solo") {
            return parseUri(data)
        }

        // Из extras (из уведомлений)
        val deepLink = intent.getStringExtra("deep_link")
        if (deepLink != null) {
            return parseUri(Uri.parse(deepLink))
        }

        return null
    }

    /**
     * Парсит URI и возвращает destination.
     */
    fun parseUri(uri: Uri): DeepLinkDestination? {
        val host = uri.host ?: return null
        val pathSegments = uri.pathSegments

        return when (host) {
            "appointment" -> {
                val id = pathSegments.getOrNull(0) ?: return null
                DeepLinkDestination.Appointment(id)
            }
            "client" -> {
                val id = pathSegments.getOrNull(0) ?: return null
                DeepLinkDestination.Client(id)
            }
            "task" -> {
                val id = pathSegments.getOrNull(0) ?: return null
                DeepLinkDestination.Task(id)
            }
            "subscription" -> {
                val id = pathSegments.getOrNull(0) ?: return null
                DeepLinkDestination.Subscription(id)
            }
            "inventory" -> {
                val id = pathSegments.getOrNull(0) ?: return null
                DeepLinkDestination.Inventory(id)
            }
            "ice" -> DeepLinkDestination.ICE
            "calendar" -> {
                val date = uri.getQueryParameter("date")
                DeepLinkDestination.Calendar(date)
            }
            "dashboard" -> DeepLinkDestination.Dashboard
            "clients" -> DeepLinkDestination.ClientsList
            "finance" -> DeepLinkDestination.Finance
            "more" -> DeepLinkDestination.More
            else -> null
        }
    }

    /**
     * Навигация по deep link.
     */
    fun navigate(navController: NavController, destination: DeepLinkDestination) {
        when (destination) {
            is DeepLinkDestination.Appointment -> {
                navController.navigate("appointment/${destination.id}")
            }
            is DeepLinkDestination.Client -> {
                navController.navigate("client/${destination.id}")
            }
            is DeepLinkDestination.Task -> {
                navController.navigate("task/${destination.id}")
            }
            is DeepLinkDestination.Subscription -> {
                navController.navigate("subscription/${destination.id}")
            }
            is DeepLinkDestination.Inventory -> {
                navController.navigate("inventory/${destination.id}")
            }
            is DeepLinkDestination.ICE -> {
                navController.navigate("ice")
            }
            is DeepLinkDestination.Calendar -> {
                // Переключаем на вкладку календаря
                // Дата будет обработана в CalendarScreen
                navController.navigate("calendar") {
                    popUpTo("dashboard") { inclusive = false }
                }
            }
            is DeepLinkDestination.Dashboard -> {
                navController.navigate("dashboard") {
                    popUpTo("dashboard") { inclusive = true }
                }
            }
            is DeepLinkDestination.ClientsList -> {
                navController.navigate("clients") {
                    popUpTo("dashboard") { inclusive = false }
                }
            }
            is DeepLinkDestination.Finance -> {
                navController.navigate("finance") {
                    popUpTo("dashboard") { inclusive = false }
                }
            }
            is DeepLinkDestination.More -> {
                navController.navigate("more") {
                    popUpTo("dashboard") { inclusive = false }
                }
            }
        }
    }
}

/**
 * Destination для deep link навигации.
 */
sealed class DeepLinkDestination {
    data class Appointment(val id: String) : DeepLinkDestination()
    data class Client(val id: String) : DeepLinkDestination()
    data class Task(val id: String) : DeepLinkDestination()
    data class Subscription(val id: String) : DeepLinkDestination()
    data class Inventory(val id: String) : DeepLinkDestination()
    data object ICE : DeepLinkDestination()
    data class Calendar(val date: String?) : DeepLinkDestination()
    data object Dashboard : DeepLinkDestination()
    data object ClientsList : DeepLinkDestination()
    data object Finance : DeepLinkDestination()
    data object More : DeepLinkDestination()
}
