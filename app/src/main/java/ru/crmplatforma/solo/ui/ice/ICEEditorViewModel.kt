package ru.crmplatforma.solo.ui.ice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.repository.ICERepository
import javax.inject.Inject

/**
 * UI State для редактора ICE.
 */
data class ICEEditorState(
    val ownerName: String = "",
    val ownerBloodType: String = "",
    val ownerAllergies: String = "",
    val ownerMedications: String = "",
    val ownerMedicalNotes: String = "",
    val contact1Name: String = "",
    val contact1Phone: String = "",
    val contact1Relation: String = "",
    val contact2Name: String = "",
    val contact2Phone: String = "",
    val contact2Relation: String = "",
    val contact3Name: String = "",
    val contact3Phone: String = "",
    val contact3Relation: String = ""
)

/**
 * Группы крови для выбора.
 */
val bloodTypes = listOf(
    "I (O) Rh+",
    "I (O) Rh-",
    "II (A) Rh+",
    "II (A) Rh-",
    "III (B) Rh+",
    "III (B) Rh-",
    "IV (AB) Rh+",
    "IV (AB) Rh-"
)

/**
 * ICEEditorViewModel — создание и редактирование ICE.
 */
@HiltViewModel
class ICEEditorViewModel @Inject constructor(
    private val iceRepository: ICERepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ICEEditorState())
    val uiState: StateFlow<ICEEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ice = iceRepository.getICEOnce()
                if (ice != null) {
                    _uiState.value = ICEEditorState(
                        ownerName = ice.ownerName,
                        ownerBloodType = ice.ownerBloodType ?: "",
                        ownerAllergies = ice.ownerAllergies ?: "",
                        ownerMedications = ice.ownerMedications ?: "",
                        ownerMedicalNotes = ice.ownerMedicalNotes ?: "",
                        contact1Name = ice.contact1Name,
                        contact1Phone = ice.contact1Phone,
                        contact1Relation = ice.contact1Relation ?: "",
                        contact2Name = ice.contact2Name ?: "",
                        contact2Phone = ice.contact2Phone ?: "",
                        contact2Relation = ice.contact2Relation ?: "",
                        contact3Name = ice.contact3Name ?: "",
                        contact3Phone = ice.contact3Phone ?: "",
                        contact3Relation = ice.contact3Relation ?: ""
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Setters — Владелец ===

    fun setOwnerName(name: String) {
        _uiState.value = _uiState.value.copy(ownerName = name)
    }

    fun setOwnerBloodType(bloodType: String) {
        _uiState.value = _uiState.value.copy(ownerBloodType = bloodType)
    }

    fun setOwnerAllergies(allergies: String) {
        _uiState.value = _uiState.value.copy(ownerAllergies = allergies)
    }

    fun setOwnerMedications(medications: String) {
        _uiState.value = _uiState.value.copy(ownerMedications = medications)
    }

    fun setOwnerMedicalNotes(notes: String) {
        _uiState.value = _uiState.value.copy(ownerMedicalNotes = notes)
    }

    // === Setters — Контакт 1 ===

    fun setContact1Name(name: String) {
        _uiState.value = _uiState.value.copy(contact1Name = name)
    }

    fun setContact1Phone(phone: String) {
        _uiState.value = _uiState.value.copy(contact1Phone = phone)
    }

    fun setContact1Relation(relation: String) {
        _uiState.value = _uiState.value.copy(contact1Relation = relation)
    }

    // === Setters — Контакт 2 ===

    fun setContact2Name(name: String) {
        _uiState.value = _uiState.value.copy(contact2Name = name)
    }

    fun setContact2Phone(phone: String) {
        _uiState.value = _uiState.value.copy(contact2Phone = phone)
    }

    fun setContact2Relation(relation: String) {
        _uiState.value = _uiState.value.copy(contact2Relation = relation)
    }

    // === Setters — Контакт 3 ===

    fun setContact3Name(name: String) {
        _uiState.value = _uiState.value.copy(contact3Name = name)
    }

    fun setContact3Phone(phone: String) {
        _uiState.value = _uiState.value.copy(contact3Phone = phone)
    }

    fun setContact3Relation(relation: String) {
        _uiState.value = _uiState.value.copy(contact3Relation = relation)
    }

    // === Validation ===

    fun isValid(): Boolean {
        val state = _uiState.value
        return state.ownerName.isNotBlank() &&
               state.contact1Name.isNotBlank() &&
               state.contact1Phone.isNotBlank()
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value

                iceRepository.saveICE(
                    ownerName = state.ownerName,
                    ownerBloodType = state.ownerBloodType.takeIf { it.isNotBlank() },
                    ownerAllergies = state.ownerAllergies.takeIf { it.isNotBlank() },
                    ownerMedications = state.ownerMedications.takeIf { it.isNotBlank() },
                    ownerMedicalNotes = state.ownerMedicalNotes.takeIf { it.isNotBlank() },
                    contact1Name = state.contact1Name,
                    contact1Phone = state.contact1Phone,
                    contact1Relation = state.contact1Relation.takeIf { it.isNotBlank() },
                    contact2Name = state.contact2Name.takeIf { it.isNotBlank() },
                    contact2Phone = state.contact2Phone.takeIf { it.isNotBlank() },
                    contact2Relation = state.contact2Relation.takeIf { it.isNotBlank() },
                    contact3Name = state.contact3Name.takeIf { it.isNotBlank() },
                    contact3Phone = state.contact3Phone.takeIf { it.isNotBlank() },
                    contact3Relation = state.contact3Relation.takeIf { it.isNotBlank() }
                )

                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Delete ===

    fun delete() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                iceRepository.deleteICE()
                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
