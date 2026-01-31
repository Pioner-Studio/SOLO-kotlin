package ru.crmplatforma.solo.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.UserPreferences
import javax.inject.Inject

/**
 * ViewModel для SoloApp.
 *
 * Отвечает за:
 * - Проверку завершения онбординга
 * - Сохранение последней вкладки
 */
@HiltViewModel
class SoloAppViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val onboardingCompleted: Flow<Boolean> = userPreferences.onboardingCompleted
}
