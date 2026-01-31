package ru.crmplatforma.solo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension для создания DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "solo_preferences")

/**
 * UserPreferences — настройки пользователя через DataStore.
 *
 * Хранит:
 * - Флаги состояния (первый запуск, онбординг)
 * - Настройки бизнеса (специализация, рабочие часы)
 * - Настройки напоминаний
 * - Состояние синхронизации
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // === Keys ===
    private object Keys {
        // Флаги
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Бизнес
        val SPECIALIZATION_ID = stringPreferencesKey("specialization_id")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val WORK_START_HOUR = intPreferencesKey("work_start_hour")
        val WORK_END_HOUR = intPreferencesKey("work_end_hour")
        val WORK_DAYS_JSON = stringPreferencesKey("work_days_json")  // [1,2,3,4,5] = Пн-Пт

        // Напоминания (дефолты)
        val DEFAULT_REMIND_24H = booleanPreferencesKey("default_remind_24h")
        val DEFAULT_REMIND_1H = booleanPreferencesKey("default_remind_1h")
        val DEFAULT_REMIND_15M = booleanPreferencesKey("default_remind_15m")

        // Уведомления
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        // Синхронизация
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val SERVER_USER_ID = stringPreferencesKey("server_user_id")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")

        // UI
        val LAST_TAB_INDEX = intPreferencesKey("last_tab_index")
        val CALENDAR_VIEW_MODE = stringPreferencesKey("calendar_view_mode")  // DAY, TWO_WEEKS, MONTH
    }

    // === Flows ===

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_FIRST_LAUNCH] ?: true
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val specializationId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.SPECIALIZATION_ID]
    }

    val businessName: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.BUSINESS_NAME]
    }

    val workStartHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.WORK_START_HOUR] ?: 9
    }

    val workEndHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.WORK_END_HOUR] ?: 18
    }

    val defaultRemind24h: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_REMIND_24H] ?: true
    }

    val defaultRemind1h: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_REMIND_1H] ?: true
    }

    val defaultRemind15m: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_REMIND_15M] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val lastSyncTime: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_TIME]
    }

    val authToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.AUTH_TOKEN]
    }

    val lastTabIndex: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_TAB_INDEX] ?: 0
    }

    val calendarViewMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CALENDAR_VIEW_MODE] ?: "DAY"
    }

    // === Setters ===

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSpecialization(id: String, name: String? = null) {
        dataStore.edit { prefs ->
            prefs[Keys.SPECIALIZATION_ID] = id
            name?.let { prefs[Keys.BUSINESS_NAME] = it }
        }
    }

    suspend fun setWorkHours(startHour: Int, endHour: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.WORK_START_HOUR] = startHour
            prefs[Keys.WORK_END_HOUR] = endHour
        }
    }

    suspend fun setDefaultReminders(remind24h: Boolean, remind1h: Boolean, remind15m: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_REMIND_24H] = remind24h
            prefs[Keys.DEFAULT_REMIND_1H] = remind1h
            prefs[Keys.DEFAULT_REMIND_15M] = remind15m
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAuthToken(token: String?) {
        dataStore.edit { prefs ->
            if (token != null) {
                prefs[Keys.AUTH_TOKEN] = token
            } else {
                prefs.remove(Keys.AUTH_TOKEN)
            }
        }
    }

    suspend fun setServerUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId != null) {
                prefs[Keys.SERVER_USER_ID] = userId
            } else {
                prefs.remove(Keys.SERVER_USER_ID)
            }
        }
    }

    suspend fun setLastSyncTime(timeMillis: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = timeMillis
        }
    }

    suspend fun setLastTabIndex(index: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_TAB_INDEX] = index
        }
    }

    suspend fun setCalendarViewMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_VIEW_MODE] = mode
        }
    }

    // === Утилиты ===

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.AUTH_TOKEN)
            prefs.remove(Keys.SERVER_USER_ID)
            prefs.remove(Keys.LAST_SYNC_TIME)
        }
    }
}
