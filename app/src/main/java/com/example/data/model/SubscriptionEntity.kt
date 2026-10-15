package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val categoryId: Long,
    val dueDayOfMonth: Int, // e.g. 1 to 31
    val billingCycle: String = "MONTHLY", // MONTHLY, YEARLY
    val autoPaid: Boolean = false,
    val lastPaidMonth: String? = null, // e.g. "2026-09"
    val note: String = ""
)
