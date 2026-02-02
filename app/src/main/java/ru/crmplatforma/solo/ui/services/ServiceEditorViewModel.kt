package ru.crmplatforma.solo.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.repository.ServiceRepository
import javax.inject.Inject

/**
 * UI State для редактора услуги.
 */
data class ServiceEditorState(
    val id: String? = null,
    val name: String = "",
    val priceRubles: String = "",
    val durationMinutes: String = "60",
    val description: String = "",
    val isArchived: Boolean = false
)

/**
 * ServiceEditorViewModel — создание и редактирование услуги.
 */
@HiltViewModel
class ServiceEditorViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceEditorState())
    val uiState: StateFlow<ServiceEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadService(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = serviceRepository.getServiceById(id)
                if (service != null) {
                    _uiState.value = ServiceEditorState(
                        id = service.id,
                        name = service.name,
                        priceRubles = (service.priceKopecks / 100).toString(),
                        durationMinutes = service.durationMinutes.toString(),
                        description = service.description ?: "",
                        isArchived = service.isArchived
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Setters ===

    fun setName(name: String) {
        android.util.Log.d("ServiceEditor", "setName: '$name', current state: ${_uiState.value}")
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun setPrice(rubles: String) {
        val filtered = rubles.filter { it.isDigit() }
        android.util.Log.d("ServiceEditor", "setPrice: '$rubles' -> '$filtered'")
        _uiState.value = _uiState.value.copy(priceRubles = filtered)
    }

    fun setDuration(minutes: String) {
        val filtered = minutes.filter { it.isDigit() }
        android.util.Log.d("ServiceEditor", "setDuration: '$minutes' -> '$filtered'")
        _uiState.value = _uiState.value.copy(durationMinutes = filtered)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    // === Validation ===

    fun isValid(): Boolean {
        val state = _uiState.value
        val valid = state.name.isNotBlank() &&
                state.priceRubles.isNotBlank() &&
                state.durationMinutes.isNotBlank()
        android.util.Log.d("ServiceEditor", "isValid: $valid, name='${state.name}', price='${state.priceRubles}', duration='${state.durationMinutes}'")
        return valid
    }

    // === Save ===

    fun save() {
        android.util.Log.d("ServiceEditor", "save() вызван, isValid=${isValid()}, state=${_uiState.value}")
        if (!isValid()) {
            android.util.Log.w("ServiceEditor", "save() отменён - isValid()=false")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value
                val priceKopecks = (state.priceRubles.toLongOrNull() ?: 0L) * 100
                val duration = state.durationMinutes.toIntOrNull() ?: 60
                android.util.Log.d("ServiceEditor", "Сохраняю услугу: id=${state.id}, name='${state.name}', price=$priceKopecks")

                if (state.id == null) {
                    // Создание
                    val created = serviceRepository.createService(
                        name = state.name,
                        priceKopecks = priceKopecks,
                        durationMinutes = duration,
                        description = state.description.takeIf { it.isNotBlank() }
                    )
                    android.util.Log.d("ServiceEditor", "Услуга создана: ${created.id}, name='${created.name}'")
                } else {
                    // Обновление
                    val existing = serviceRepository.getServiceById(state.id)
                    android.util.Log.d("ServiceEditor", "Существующая услуга: $existing")
                    existing?.copy(
                        name = state.name,
                        priceKopecks = priceKopecks,
                        durationMinutes = duration,
                        description = state.description.takeIf { it.isNotBlank() }
                    )?.also {
                        serviceRepository.updateService(it)
                        android.util.Log.d("ServiceEditor", "Услуга обновлена: ${it.id}")
                    }
                }

                android.util.Log.d("ServiceEditor", "Сохранение успешно, устанавливаю saveSuccess=true")
                _saveSuccess.value = true
            } catch (e: Exception) {
                android.util.Log.e("ServiceEditor", "Ошибка сохранения услуги", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Archive ===

    fun archive() {
        val id = _uiState.value.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                serviceRepository.archiveService(id)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unarchive() {
        val id = _uiState.value.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                serviceRepository.unarchiveService(id)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
