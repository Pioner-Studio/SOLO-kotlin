package ru.crmplatforma.solo.data.repository

import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.dao.TransactionDao
import ru.crmplatforma.solo.data.local.entity.TransactionEntity
import ru.crmplatforma.solo.data.local.entity.TransactionType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TransactionRepository — финансовые операции.
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    // === Queries ===

    fun getTransactionsByDateRange(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>> {
        return transactionDao.getByDateRangeFlow(start.toString(), end.toString())
    }

    fun getIncomeFlow(start: LocalDate, end: LocalDate): Flow<Long> {
        return transactionDao.getIncomeFlow(start.toString(), end.toString())
    }

    fun getExpenseFlow(start: LocalDate, end: LocalDate): Flow<Long> {
        return transactionDao.getExpenseFlow(start.toString(), end.toString())
    }

    suspend fun getIncome(start: LocalDate, end: LocalDate): Long {
        return transactionDao.getIncome(start.toString(), end.toString())
    }

    suspend fun getExpense(start: LocalDate, end: LocalDate): Long {
        return transactionDao.getExpense(start.toString(), end.toString())
    }

    fun getIncomeForMonth(year: Int, month: Int): Flow<Long> {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return transactionDao.getIncomeFlow(start.toString(), end.toString())
    }

    fun getTodayIncome(): Flow<Long> {
        val today = LocalDate.now()
        return transactionDao.getIncomeFlow(today.toString(), today.toString())
    }

    // === Commands ===

    suspend fun createIncome(
        amountKopecks: Long,
        date: LocalDate,
        description: String? = null,
        appointmentId: String? = null,
        clientId: String? = null,
        clientName: String? = null
    ): TransactionEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amountKopecks = amountKopecks,
            date = date,
            description = description,
            appointmentId = appointmentId,
            clientId = clientId,
            clientName = clientName,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        transactionDao.insert(transaction)
        return transaction
    }

    suspend fun createExpense(
        amountKopecks: Long,
        date: LocalDate,
        description: String? = null,
        category: String? = null
    ): TransactionEntity {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amountKopecks = amountKopecks,
            date = date,
            description = description,
            category = category,
            synced = false,
            createdAt = now,
            updatedAt = now
        )
        transactionDao.insert(transaction)
        return transaction
    }

    suspend fun deleteByAppointment(appointmentId: String) {
        transactionDao.deleteByAppointment(appointmentId)
    }

    // === Sync ===

    suspend fun getUnsyncedTransactions(): List<TransactionEntity> = transactionDao.getUnsynced()

    suspend fun markTransactionSynced(id: String, serverId: String) {
        transactionDao.markSynced(id, serverId)
    }
}
