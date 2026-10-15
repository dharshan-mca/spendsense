package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String, // e.g. "food", "transport", "shopping", "bills", "entertainment", "health", "rent", "salary", "other"
    val colorHex: String, // e.g. "#10B981"
    val isCustom: Boolean = false,
    val isDefault: Boolean = false,
    val budgetLimit: Double? = null
)
