package ru.crmplatforma.solo.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.repository.ClientRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI State для редактора клиента.
 */
data class ClientEditorState(
    val id: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val birthday: LocalDate? = null,
    val notes: String = "",
    val isVip: Boolean = false
)

/**
 * ClientEditorViewModel — создание и редактирование клиента.
 */
@HiltViewModel
class ClientEditorViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientEditorState())
    val uiState: StateFlow<ClientEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadClient(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val client = clientRepository.getClientByIdOnce(id)
                if (client != null) {
                    _uiState.value = ClientEditorState(
                        id = client.id,
                        name = client.name,
                        phone = client.phone ?: "",
                        email = client.email ?: "",
                        birthday = client.birthday,
                        notes = client.notes ?: "",
                        isVip = client.isVip
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

    fun setPhone(phone: String) {
        // Фильтруем только цифры и +
        val filtered = phone.filter { it.isDigit() || it == '+' }
        _uiState.value = _uiState.value.copy(phone = filtered)
    }

    fun setEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun setBirthday(birthday: LocalDate?) {
        _uiState.value = _uiState.value.copy(birthday = birthday)
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun setVip(isVip: Boolean) {
        _uiState.value = _uiState.value.copy(isVip = isVip)
    }

    // === Validation ===

    fun isValid(): Boolean {
        return _uiState.value.name.isNotBlank()
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value

                if (state.id == null) {
                    // Создание нового клиента
                    clientRepository.createClient(
                        name = state.name,
                        phone = state.phone.takeIf { it.isNotBlank() },
                        email = state.email.takeIf { it.isNotBlank() },
                        birthday = state.birthday,
                        notes = state.notes.takeIf { it.isNotBlank() },
                        isVip = state.isVip
                    )
                } else {
                    // Обновление существующего
                    val existing = clientRepository.getClientByIdOnce(state.id)
                    existing?.copy(
                        name = state.name,
                        phone = state.phone.takeIf { it.isNotBlank() },
                        email = state.email.takeIf { it.isNotBlank() },
                        birthday = state.birthday,
                        notes = state.notes.takeIf { it.isNotBlank() },
                        isVip = state.isVip
                    )?.also {
                        clientRepository.updateClient(it)
                    }
                }

                _saveSuccess.value = true
            } catch (e: Exception) {
                android.util.Log.e("ClientEditor", "Ошибка сохранения клиента", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Delete ===

    fun delete() {
        val id = _uiState.value.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                clientRepository.deleteClient(id)
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
