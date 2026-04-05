package com.example.fintrack.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val type: String,        // "income" or "expense"
    val category: String,    // "Food", "Transport", etc.
    val date: Long,          // stored as timestamp
    val note: String = ""
)