package ru.crmplatforma.solo.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity
import ru.crmplatforma.solo.data.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * UI State для списка подписок.
 */
data class SubscriptionsState(
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val monthlyTotalKopecks: Long = 0,
    val upcomingCount: Int = 0,
    val showArchived: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * SubscriptionsViewModel — управление списком подписок.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _showArchived = MutableStateFlow(false)

    val uiState: StateFlow<SubscriptionsState> = combine(
        _showArchived.flatMapLatest { showArchived ->
            if (showArchived) {
                subscriptionRepository.getAllSubscriptions()
            } else {
                subscriptionRepository.getActiveSubscriptions()
            }
        },
        subscriptionRepository.getMonthlyTotal(),
        subscriptionRepository.getUpcomingCount(7),
        _showArchived
    ) { subscriptions, monthlyTotal, upcomingCount, showArchived ->
        SubscriptionsState(
            subscriptions = subscriptions.sortedBy { it.nextPaymentDate },
            monthlyTotalKopecks = monthlyTotal,
            upcomingCount = upcomingCount,
            showArchived = showArchived,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionsState()
    )

    // === Actions ===

    fun toggleShowArchived() {
        _showArchived.value = !_showArchived.value
    }

    suspend fun markPaid(subscriptionId: String) {
        subscriptionRepository.markPaid(subscriptionId)
    }

    suspend fun toggleActive(subscription: SubscriptionEntity) {
        if (subscription.isActive) {
            subscriptionRepository.deactivate(subscription.id)
        } else {
            subscriptionRepository.activate(subscription.id)
        }
    }
}
