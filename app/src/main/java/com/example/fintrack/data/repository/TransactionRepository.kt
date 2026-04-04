package com.example.fintrack.data.repository


import com.example.fintrack.data.local.dao.TransactionDao
import com.example.fintrack.data.local.entity.TransactionEntity
import com.example.fintrack.domain.model.Transaction
import com.example.fintrack.domain.model.TransactionCategory
import com.example.fintrack.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val dao: TransactionDao) {

    // ─── Observe all transactions as domain models ───────────────────────
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
        .map { entities -> entities.map { it.toDomain() } }

    val totalIncome: Flow<Double> = dao.getTotalIncome()
        .map { it ?: 0.0 }

    val totalExpense: Flow<Double> = dao.getTotalExpense()
        .map { it ?: 0.0 }

    // ─── CRUD Operations ─────────────────────────────────────────────────
    suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    // ─── Filtered queries ─────────────────────────────────────────────────
    fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return dao.getTransactionsByType(type.name.lowercase())
            .map { entities -> entities.map { it.toDomain() } }
    }

    // ─── Mappers ──────────────────────────────────────────────────────────
    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            type = try {
                TransactionType.valueOf(type.uppercase())
            } catch (e: Exception) {
                TransactionType.EXPENSE
            },
            category = try {
                TransactionCategory.valueOf(category.uppercase())
            } catch (e: Exception) {
                TransactionCategory.OTHER
            },
            date = date,
            note = note
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            type = type.name.lowercase(),
            category = category.name.lowercase(),
            date = date,
            note = note
        )
    }
}