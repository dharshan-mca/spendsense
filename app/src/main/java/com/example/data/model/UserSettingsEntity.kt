package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyBudget: Double = 30000.0,
    val monthlyIncome: Double = 0.0,
    val currencySymbol: String = "₹",
    val isOnboardingCompleted: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM
    val autoDetectUpi: Boolean = true,
    val autoSaveUpi: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val appLockPin: String? = null,
    val biometricEnabled: Boolean = false
)
