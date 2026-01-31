package ru.crmplatforma.solo.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.crmplatforma.solo.data.local.entity.TransactionEntity
import ru.crmplatforma.solo.data.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Периоды для отображения финансов.
 */
enum class FinancePeriod {
    DAY, WEEK, MONTH
}

/**
 * UI State для экрана финансов.
 */
data class FinanceState(
    val period: FinancePeriod = FinancePeriod.MONTH,
    val incomeKopecks: Long = 0,
    val expenseKopecks: Long = 0,
    val transactions: List<TransactionEntity> = emptyList()
) {
    val profitKopecks: Long get() = incomeKopecks - expenseKopecks

    fun formatMoney(kopecks: Long): String {
        val rubles = kopecks / 100
        return "%,d ₽".format(rubles).replace(',', ' ')
    }

    val incomeFormatted: String get() = formatMoney(incomeKopecks)
    val expenseFormatted: String get() = formatMoney(expenseKopecks)
    val profitFormatted: String get() = formatMoney(profitKopecks)
}

/**
 * FinanceViewModel — управление экраном финансов.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _period = MutableStateFlow(FinancePeriod.MONTH)
    val period: StateFlow<FinancePeriod> = _period.asStateFlow()

    // Вычисляем диапазон дат на основе выбранного периода
    private val dateRange: StateFlow<Pair<LocalDate, LocalDate>> = _period.map { period ->
        val today = LocalDate.now()
        when (period) {
            FinancePeriod.DAY -> today to today
            FinancePeriod.WEEK -> today.minusDays(6) to today
            FinancePeriod.MONTH -> today.withDayOfMonth(1) to today
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocalDate.now().withDayOfMonth(1) to LocalDate.now()
    )

    // Доходы
    private val income: Flow<Long> = dateRange.flatMapLatest { (start, end) ->
        transactionRepository.getIncomeFlow(start, end)
    }

    // Расходы
    private val expense: Flow<Long> = dateRange.flatMapLatest { (start, end) ->
        transactionRepository.getExpenseFlow(start, end)
    }

    // Транзакции
    private val transactions: Flow<List<TransactionEntity>> = dateRange.flatMapLatest { (start, end) ->
        transactionRepository.getTransactionsByDateRange(start, end)
    }

    // Объединённый State
    val uiState: StateFlow<FinanceState> = combine(
        _period,
        income,
        expense,
        transactions
    ) { period, inc, exp, txs ->
        FinanceState(
            period = period,
            incomeKopecks = inc,
            expenseKopecks = exp,
            transactions = txs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceState()
    )

    // === Actions ===

    fun setPeriod(period: FinancePeriod) {
        _period.value = period
    }
}
