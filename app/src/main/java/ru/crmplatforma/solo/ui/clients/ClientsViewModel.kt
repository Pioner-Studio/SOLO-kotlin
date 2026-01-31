package ru.crmplatforma.solo.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.crmplatforma.solo.data.local.entity.ClientEntity
import ru.crmplatforma.solo.data.repository.ClientRepository
import javax.inject.Inject

/**
 * Фильтры для списка клиентов.
 */
enum class ClientFilter {
    ALL,      // Все клиенты
    VIP,      // Только VIP
    BIRTHDAY  // ДР скоро
}

/**
 * UI State для списка клиентов.
 */
data class ClientsState(
    val clients: List<ClientEntity> = emptyList(),
    val searchQuery: String = "",
    val filter: ClientFilter = ClientFilter.ALL,
    val isLoading: Boolean = true,
    val totalCount: Int = 0
)

/**
 * ClientsViewModel — управление списком клиентов.
 */
@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(ClientFilter.ALL)

    // Комбинированный поиск + фильтр
    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredClients: Flow<List<ClientEntity>> = combine(
        _searchQuery,
        _filter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        when {
            query.isNotBlank() -> clientRepository.searchClients(query)
            filter == ClientFilter.VIP -> clientRepository.getVipClients()
            filter == ClientFilter.BIRTHDAY -> clientRepository.getUpcomingBirthdays(50)
            else -> clientRepository.getAllClients()
        }
    }

    // Общее количество клиентов
    private val totalCount: Flow<Int> = clientRepository.getClientCount()

    // Комбинированный UI State
    val uiState: StateFlow<ClientsState> = combine(
        filteredClients,
        _searchQuery,
        _filter,
        totalCount
    ) { clients, query, filter, count ->
        ClientsState(
            clients = clients,
            searchQuery = query,
            filter = filter,
            isLoading = false,
            totalCount = count
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientsState()
    )

    // === Actions ===

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ClientFilter) {
        _filter.value = filter
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
