package com.example.fintrack.viewmodel

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fintrack.data.repository.TransactionRepository
import com.example.fintrack.domain.model.Transaction
import com.example.fintrack.domain.model.TransactionCategory
import com.example.fintrack.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // ─── UI State ─────────────────────────────────────────────────────────
    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ─── Search & Filter State ────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedType: StateFlow<TransactionType?> = _selectedType.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TransactionCategory?>(null)
    val selectedCategory: StateFlow<TransactionCategory?> = _selectedCategory.asStateFlow()

    // ─── All Transactions ─────────────────────────────────────────────────
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── Init — set Success once data loads ───────────────────────────────
    init {
        viewModelScope.launch {
            allTransactions.collect {
                _uiState.value = UiState.Success
            }
        }
    }

    // ─── Filtered Transactions ────────────────────────────────────────────
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _searchQuery,
        _selectedType,
        _selectedCategory
    ) { transactions, query, type, category ->
        transactions.filter { transaction ->
            val matchesQuery = query.isEmpty() ||
                    transaction.note.contains(query, ignoreCase = true) ||
                    transaction.category.displayName.contains(
                        query, ignoreCase = true
                    ) ||
                    transaction.amount.toString().contains(query)
            val matchesType = type == null || transaction.type == type
            val matchesCategory = category == null || transaction.category == category
            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ─── Dashboard Summary ────────────────────────────────────────────────
    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val currentBalance: StateFlow<Double> = combine(
        totalIncome,
        totalExpense
    ) { income, expense ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // ─── Expense by Category ──────────────────────────────────────────────
    val expenseByCategory: StateFlow<Map<TransactionCategory, Double>> =
        allTransactions
            .map { transactions ->
                transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    // ─── Most Frequent Category ───────────────────────────────────────────
    val mostFrequentCategory: StateFlow<TransactionCategory?> = allTransactions
        .map { transactions ->
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .maxByOrNull { it.value.size }
                ?.key
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // ─── This Week Expenses ───────────────────────────────────────────────
    val thisWeekExpenses: StateFlow<Double> = allTransactions
        .map { transactions ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val weekStart = calendar.timeInMillis
            transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                            it.date >= weekStart
                }
                .sumOf { it.amount }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // ─── Last Week Expenses ───────────────────────────────────────────────
    val lastWeekExpenses: StateFlow<Double> = allTransactions
        .map { transactions ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val thisWeekStart = calendar.timeInMillis
            val lastWeekStart = thisWeekStart - 7 * 24 * 60 * 60 * 1000L
            transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                            it.date >= lastWeekStart &&
                            it.date < thisWeekStart
                }
                .sumOf { it.amount }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // ─── Last 7 Days Expenses ─────────────────────────────────────────────
    val last7DaysExpenses: StateFlow<List<Pair<String, Double>>> = allTransactions
        .map { transactions ->
            val result = mutableListOf<Pair<String, Double>>()
            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dayLabel = when (i) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    else -> DateFormat.format("EEE", cal.time).toString()
                }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
                val total = transactions
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                                it.date in dayStart..dayEnd
                    }
                    .sumOf { it.amount }
                result.add(Pair(dayLabel, total))
            }
            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── Monthly Expenses ─────────────────────────────────────────────────
    val monthlyExpenses: StateFlow<List<Pair<String, Double>>> = allTransactions
        .map { transactions ->
            val result = mutableListOf<Pair<String, Double>>()
            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val monthStart = cal.timeInMillis
                cal.set(
                    Calendar.DAY_OF_MONTH,
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val monthEnd = cal.timeInMillis
                val monthLabel = DateFormat.format("MMM", cal.time).toString()
                val total = transactions
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                                it.date in monthStart..monthEnd
                    }
                    .sumOf { it.amount }
                result.add(Pair(monthLabel, total))
            }
            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── No Spend Streak ──────────────────────────────────────────────────
    val noSpendStreak: StateFlow<Int> = allTransactions
        .map { transactions ->
            var streak = 0
            for (i in 0..365) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
                val hadExpense = transactions.any {
                    it.type == TransactionType.EXPENSE &&
                            it.date in dayStart..dayEnd
                }
                if (!hadExpense) streak++ else break
            }
            streak
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ─── Best Streak ──────────────────────────────────────────────────────
    val bestStreak: StateFlow<Int> = allTransactions
        .map { transactions ->
            var best = 0
            var current = 0
            for (i in 365 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
                val hadExpense = transactions.any {
                    it.type == TransactionType.EXPENSE &&
                            it.date in dayStart..dayEnd
                }
                if (!hadExpense) {
                    current++
                    if (current > best) best = current
                } else {
                    current = 0
                }
            }
            best
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ─── Actions ──────────────────────────────────────────────────────────
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save transaction")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to update transaction")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to delete transaction")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedType.value = type
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _selectedCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _selectedCategory.value = null
    }

    // ─── Factory ──────────────────────────────────────────────────────────
    class Factory(
        private val repository: TransactionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}