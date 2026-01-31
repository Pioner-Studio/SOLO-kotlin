package ru.crmplatforma.solo.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.TransactionType
import ru.crmplatforma.solo.data.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Категории расходов.
 */
val expenseCategories = listOf(
    "Материалы",
    "Аренда",
    "Реклама",
    "Налоги",
    "Транспорт",
    "Оборудование",
    "Связь",
    "Прочее"
)

/**
 * UI State для редактора транзакции.
 */
data class TransactionEditorState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountRubles: String = "",
    val date: LocalDate = LocalDate.now(),
    val description: String = "",
    val category: String? = null
)

/**
 * TransactionEditorViewModel — создание и редактирование транзакций.
 */
@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditorState())
    val uiState: StateFlow<TransactionEditorState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // === Setters ===

    fun setType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setAmount(rubles: String) {
        // Только цифры и одна точка
        val filtered = rubles.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(amountRubles = filtered)
    }

    fun setDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun setCategory(category: String?) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    // === Validation ===

    fun isValid(): Boolean {
        val state = _uiState.value
        val amount = state.amountRubles.toDoubleOrNull() ?: 0.0
        return amount > 0
    }

    // === Save ===

    fun save() {
        if (!isValid()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _uiState.value
                val amountKopecks = ((state.amountRubles.toDoubleOrNull() ?: 0.0) * 100).toLong()

                when (state.type) {
                    TransactionType.INCOME -> {
                        transactionRepository.createIncome(
                            amountKopecks = amountKopecks,
                            date = state.date,
                            description = state.description.takeIf { it.isNotBlank() }
                        )
                    }
                    TransactionType.EXPENSE -> {
                        transactionRepository.createExpense(
                            amountKopecks = amountKopecks,
                            date = state.date,
                            description = state.description.takeIf { it.isNotBlank() },
                            category = state.category
                        )
                    }
                }

                _saveSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
