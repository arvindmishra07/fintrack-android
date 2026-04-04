package com.example.fintrack.viewmodel


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

    // ─── Search & Filter State ────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedType: StateFlow<TransactionType?> = _selectedType.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TransactionCategory?>(null)
    val selectedCategory: StateFlow<TransactionCategory?> = _selectedCategory.asStateFlow()

    // ─── All Transactions (raw from repository) ───────────────────────────
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── Filtered Transactions (based on search + filters) ────────────────
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _searchQuery,
        _selectedType,
        _selectedCategory
    ) { transactions, query, type, category ->
        transactions.filter { transaction ->
            val matchesQuery = query.isEmpty() ||
                    transaction.note.contains(query, ignoreCase = true) ||
                    transaction.category.displayName.contains(query, ignoreCase = true) ||
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

    val currentBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // ─── Insights Data ────────────────────────────────────────────────────

    // Spending by category (for pie chart)
    val expenseByCategory: StateFlow<Map<TransactionCategory, Double>> = allTransactions
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

    // This week's expenses
    val thisWeekExpenses: StateFlow<Double> = allTransactions
        .map { transactions ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val weekStart = calendar.timeInMillis

            transactions
                .filter { it.type == TransactionType.EXPENSE && it.date >= weekStart }
                .sumOf { it.amount }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Last week's expenses
    val lastWeekExpenses: StateFlow<Double> = allTransactions
        .map { transactions ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
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

    // Daily expenses for last 7 days (for bar chart)
    val last7DaysExpenses: StateFlow<List<Pair<String, Double>>> = allTransactions
        .map { transactions ->
            val result = mutableListOf<Pair<String, Double>>()
            val calendar = Calendar.getInstance()

            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)

                val dayLabel = when (i) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    else -> android.text.format.DateFormat
                        .format("EEE", cal.time).toString(