package ru.crmplatforma.solo.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.crmplatforma.solo.data.local.entity.TransactionEntity
import ru.crmplatforma.solo.data.local.entity.TransactionType

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    // === Запросы ===

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    // Операции за день
    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY createdAt DESC")
    fun getByDateFlow(date: String): Flow<List<TransactionEntity>>

    // Операции в диапазоне
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, createdAt DESC")
    fun getByDateRangeFlow(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    // По типу
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun getByTypeFlow(type: TransactionType): Flow<List<TransactionEntity>>

    // По записи
    @Query("SELECT * FROM transactions WHERE appointmentId = :appointmentId")
    suspend fun getByAppointment(appointmentId: String): TransactionEntity?

    // === Суммы ===

    // Доход за период
    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM transactions WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    fun getIncomeFlow(startDate: String, endDate: String): Flow<Long>

    // Расход за период
    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    fun getExpenseFlow(startDate: String, endDate: String): Flow<Long>

    // Доход за период (suspend)
    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM transactions WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    suspend fun getIncome(startDate: String, endDate: String): Long

    // Расход за период (suspend)
    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    suspend fun getExpense(startDate: String, endDate: String): Long

    // === Синхронизация ===

    @Query("SELECT * FROM transactions WHERE synced = 0")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: String, serverId: String)

    // === Удаление ===

    @Query("DELETE FROM transactions WHERE appointmentId = :appointmentId")
    suspend fun deleteByAppointment(appointmentId: String)
}
