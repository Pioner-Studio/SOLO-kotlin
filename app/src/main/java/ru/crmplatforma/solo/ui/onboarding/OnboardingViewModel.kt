package ru.crmplatforma.solo.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.UserPreferences
import javax.inject.Inject

/**
 * ViewModel для Onboarding.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedSpecialization = MutableStateFlow<Specialization?>(null)
    val selectedSpecialization: StateFlow<Specialization?> = _selectedSpecialization

    fun selectSpecialization(specialization: Specialization) {
        _selectedSpecialization.value = specialization
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Сохраняем выбранную специализацию
            _selectedSpecialization.value?.let { spec ->
                userPreferences.setSpecialization(spec.id, spec.title)
            }

            // Отмечаем онбординг завершённым
            userPreferences.setOnboardingCompleted(true)
            userPreferences.setFirstLaunchCompleted()
        }
    }
}
