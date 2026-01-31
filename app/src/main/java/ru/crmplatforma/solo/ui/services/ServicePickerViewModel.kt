package ru.crmplatforma.solo.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.crmplatforma.solo.data.local.entity.ServiceEntity
import ru.crmplatforma.solo.data.repository.ServiceRepository
import javax.inject.Inject

/**
 * ServicePickerViewModel — выбор услуг для записи.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ServicePickerViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedServiceIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedServiceIds: StateFlow<Set<String>> = _selectedServiceIds.asStateFlow()

    // Активные услуги с фильтрацией по поиску
    val services: StateFlow<List<ServiceEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            serviceRepository.getActiveServices()
        } else {
            serviceRepository.searchServices(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Общая цена выбранных услуг
    val totalPriceKopecks: StateFlow<Long> = combine(
        services,
        _selectedServiceIds
    ) { serviceList, selectedIds ->
        serviceList
            .filter { it.id in selectedIds }
            .sumOf { it.priceKopecks }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // === Actions ===

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleService(serviceId: String) {
        val current = _selectedServiceIds.value
        _selectedServiceIds.value = if (serviceId in current) {
            current - serviceId
        } else {
            current + serviceId
        }
    }

    fun clearSelection() {
        _selectedServiceIds.value = emptySet()
    }

    fun setInitialSelection(serviceIds: List<String>) {
        _selectedServiceIds.value = serviceIds.toSet()
    }
}
