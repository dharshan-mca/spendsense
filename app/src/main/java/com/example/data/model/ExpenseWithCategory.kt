package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

data class CategoryExpenseSummary(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class MonthlySpendTrend(
    val monthYearLabel: String, // e.g. "Mar 2026"
    val yearMonth: String, // e.g. "2026-03"
    val totalSpend: Double,
    val totalIncome: Double
)
