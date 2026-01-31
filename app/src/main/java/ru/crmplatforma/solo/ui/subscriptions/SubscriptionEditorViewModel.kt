package ru.crmplatforma.solo.ui.subscriptions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity
import ru.crmplatforma.solo.data.local.entity.SubscriptionPeriod
import ru.crmplatforma.solo.data.repository.SubscriptionRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * UI State для редактора подписки.
 */
data class SubscriptionEditorState(
    val name: String = "",
    val amountText: String = "",
    val period: SubscriptionPeriod = SubscriptionPeriod.MONTHLY,
    val billingDay: Int = 1,
    val description: String = "",
    val remind3Days: Boolean = true,
    val remind1Day: Boolean = true,
    val remindOnDay: Boolean = true,
    val isActive: Boolean = true
)

/**
 * SubscriptionEditorViewModel — создание и редактирование подписки.
 */
@HiltViewModel
class SubscriptionEditorViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subscriptionId: String? = savedStateHandle.get<String>("id")
    private var existingSubscription: SubscriptionEntity? = null

    private val _uiState = MutableStateFlow(SubscriptionEditorState())
    val uiState: StateFlow<SubscriptionEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    val isEditMode: Boolean = subscriptionId != null

    init {
        if (subscriptionId != null) {
            loadSubscription(subscriptionId)
        }
    }

    private fun loadSubscription(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val subscription = subscriptionRepository.getSubscriptionById(id).firstOrNull()
                if (subscription != null) {
                    existingSubscription = subscription
                    _uiState.value = SubscriptionEditorState(
                        name = subscription.name,
                        amountText = (subscription.amountKopecks / 100).toString(),
                        period = subscription.period,
                        billingDay = subscription.billingDay,
                        description = subscription.description ?: "",
                        remind3Days = subscription.remind3Days,
                        remind1Day = subscription.remind1Day,
                        remindOnDay = subscription.remindOnDay,
                        isActive = subscription.isActive
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Setters ===

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun setAmount(amount: String) {
        // Разрешаем только цифры
        val filtered = amount.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(amountText = filtered)
    }

    fun setPeriod(period: SubscriptionPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
    }

    fun setBillingDay(day: Int) {
        _uiState.value = _uiState.value.copy(billingDay = day.coerceIn(1, 31))
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun setRemind3Days(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remind3Days = enabled)
    }

    fun setRemind1Day(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remind1Day = enabled)
    }

    fun setRemindOnDay(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remindOnDay = enabled)
    }

    // === Validation ===

    fun isValid(): Boolean {
        val state = _uiState.value
        return state.name.isNotBlank() &&
               state.amountText.isNotBlank() &&
               state.amountText.toLongOrNull() != null &&
               state.amountText.toLong() > 0
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value
                val amountKopecks = state.amountText.toLong() * 100

                if (isEditMode && existingSubscription != null) {
                    // Обновляем
                    val updated = existingSubscription!!.copy(
                        name = state.name,
                        amountKopecks = amountKopecks,
                        period = state.period,
                        billingDay = state.billingDay,
                        description = state.description.takeIf { it.isNotBlank() },
                        remind3Days = state.remind3Days,
                        remind1Day = state.remind1Day,
                        remindOnDay = state.remindOnDay,
                        isActive = state.isActive,
                        updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )
                    subscriptionRepository.updateSubscription(updated)
                } else {
                    // Создаём
                    subscriptionRepository.createSubscription(
                        name = state.name,
                        amountKopecks = amountKopecks,
                        period = state.period,
                        billingDay = state.billingDay,
                        description = state.description.takeIf { it.isNotBlank() },
                        remind3Days = state.remind3Days,
                        remind1Day = state.remind1Day,
                        remindOnDay = state.remindOnDay
                    )
                }

                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Delete ===

    fun delete() {
        if (existingSubscription == null) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                subscriptionRepository.deleteSubscription(existingSubscription!!)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
