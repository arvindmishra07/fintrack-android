package com.example.fintrack.domain.model


data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: Long,
    val note: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory(val displayName: String, val emoji: String) {
    // Expense categories
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊"),
    BILLS("Bills", "📄"),
    EDUCATION("Education", "📚"),
    OTHER("Other", "📦"),

    // Income categories
    SALARY("Salary", "💼"),
    FREELANCE("Freelance", "💻"),
    INVESTMENT("Investment", "📈"),
    GIFT("Gift", "🎁")
}