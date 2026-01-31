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
 * UI State для списка услуг.
 */
data class ServicesState(
    val services: List<ServiceEntity> = emptyList(),
    val searchQuery: String = "",
    val showArchived: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * ServicesViewModel — управление списком услуг.
 */
@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showArchived = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredServices: Flow<List<ServiceEntity>> = combine(
        _searchQuery,
        _showArchived
    ) { query, showArchived ->
        Pair(query, showArchived)
    }.flatMapLatest { (query, showArchived) ->
        when {
            query.isNotBlank() -> serviceRepository.searchServices(query)
            showArchived -> serviceRepository.getArchivedServices()
            else -> serviceRepository.getActiveServices()
        }
    }

    val uiState: StateFlow<ServicesState> = combine(
        filteredServices,
        _searchQuery,
        _showArchived
    ) { services, query, showArchived ->
        ServicesState(
            services = services,
            searchQuery = query,
            showArchived = showArchived,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServicesState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
