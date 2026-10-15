package com.example.data.local

import androidx.room.*
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET monthlyBudget = :budget WHERE id = 1")
    suspend fun updateMonthlyBudget(budget: Double)

    @Query("UPDATE user_settings SET currencySymbol = :symbol WHERE id = 1")
    suspend fun updateCurrencySymbol(symbol: String)

    @Query("UPDATE user_settings SET isOnboardingCompleted = :completed WHERE id = 1")
    suspend fun setOnboardingCompleted(completed: Boolean)
}
