package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val note: String? = null,
    val dateMillis: Long,
    val paymentMethod: String = "Card", // Cash, Card, UPI, Bank
    val type: String = "EXPENSE" // EXPENSE or INCOME
)
