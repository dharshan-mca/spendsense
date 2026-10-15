package com.example.data.repository

import com.example.data.local.BillSplitDao
import com.example.data.local.CategoryDao
import com.example.data.local.ExpenseDao
import com.example.data.local.SubscriptionDao
import com.example.data.local.UserSettingsDao
import com.example.data.model.BillSplitEntity
import com.example.data.model.BillSplitWithParticipants
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.SplitParticipantEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val userSettingsDao: UserSettingsDao,
    private val subscriptionDao: SubscriptionDao,
    private val billSplitDao: BillSplitDao
) {
    val allExpensesWithCategory: Flow<List<ExpenseWithCategory>> = expenseDao.getAllExpensesWithCategory()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    val allSplitsWithParticipants: Flow<List<BillSplitWithParticipants>> = billSplitDao.getAllSplitsWithParticipants()

    fun getExpensesForMonth(year: Int, monthZeroBased: Int): Flow<List<ExpenseWithCategory>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endMillis = calendar.timeInMillis - 1

        return expenseDao.getExpensesForDateRange(startMillis, endMillis)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    suspend fun updateCategoryBudgetLimit(id: Long, limit: Double?) = categoryDao.updateCategoryBudgetLimit(id, limit)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Subscriptions
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long = subscriptionDao.insertSubscription(subscription)

    suspend fun updateSubscription(subscription: SubscriptionEntity) = subscriptionDao.updateSubscription(subscription)

    suspend fun deleteSubscription(subscription: SubscriptionEntity) = subscriptionDao.deleteSubscription(subscription)

    suspend fun deleteSubscriptionById(id: Long) = subscriptionDao.deleteSubscriptionById(id)

    // Bill Splits
    suspend fun insertSplit(split: BillSplitEntity, participants: List<SplitParticipantEntity>): Long {
        val splitId = billSplitDao.insertSplit(split)
        val participantsWithId = participants.map { it.copy(splitId = splitId) }
        billSplitDao.insertParticipants(participantsWithId)
        return splitId
    }

    suspend fun updateParticipantPaidStatus(participantId: Long, isPaid: Boolean) {
        billSplitDao.updateParticipantPaidStatus(participantId, isPaid)
    }

    suspend fun updateSplitSettledStatus(splitId: Long, isSettled: Boolean) {
        billSplitDao.updateSplitSettledStatus(splitId, isSettled)
    }

    suspend fun deleteSplitById(id: Long) = billSplitDao.deleteSplitById(id)

    suspend fun updateSettings(settings: UserSettingsEntity) = userSettingsDao.saveUserSettings(settings)

    suspend fun setOnboardingCompleted(completed: Boolean) = userSettingsDao.setOnboardingCompleted(completed)

    suspend fun clearAllData() {
        expenseDao.deleteAllExpenses()
        subscriptionDao.deleteAllSubscriptions()
        billSplitDao.deleteAllParticipants()
        billSplitDao.deleteAllSplits()
        categoryDao.resetAllCategoryBudgets()
        categoryDao.deleteCustomCategories()
        userSettingsDao.saveUserSettings(
            UserSettingsEntity(
                id = 1,
                monthlyBudget = 30000.0,
                monthlyIncome = 0.0,
                currencySymbol = "₹",
                isOnboardingCompleted = false,
                dailyReminderEnabled = false,
                themeMode = "DARK",
                autoDetectUpi = true,
                autoSaveUpi = false,
                isAppLockEnabled = false,
                appLockPin = null,
                biometricEnabled = false
            )
        )
    }

    suspend fun seedSampleDataIfEmpty() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", colorHex = "#10B981", isDefault = true),
                CategoryEntity(id = 2, name = "Transport & Auto", iconName = "directions_car", colorHex = "#3B82F6", isDefault = true),
                CategoryEntity(id = 3, name = "Kirana & Groceries", iconName = "shopping_bag", colorHex = "#EC4899", isDefault = true),
                CategoryEntity(id = 4, name = "Bills & Recharge", iconName = "receipt", colorHex = "#F59E0B", isDefault = true),
                CategoryEntity(id = 5, name = "Shopping & Malls", iconName = "storefront", colorHex = "#8B5CF6", isDefault = true),
                CategoryEntity(id = 6, name = "Health & Pharmacy", iconName = "medical_services", colorHex = "#EF4444", isDefault = true),
                CategoryEntity(id = 7, name = "House Rent & Maid", iconName = "home", colorHex = "#6366F1", isDefault = true),
                CategoryEntity(id = 8, name = "Entertainment & OTT", iconName = "movie", colorHex = "#06B6D4", isDefault = true),
                CategoryEntity(id = 9, name = "Salary & Income", iconName = "payments", colorHex = "#84CC16", isDefault = true),
                CategoryEntity(id = 10, name = "Others & Cash", iconName = "more_horiz", colorHex = "#64748B", isDefault = true)
            )
            categoryDao.insertCategories(defaultCategories)
        }

        val currentSettings = userSettingsDao.getUserSettingsDirect()
        if (currentSettings == null) {
            userSettingsDao.saveUserSettings(
                UserSettingsEntity(
                    id = 1,
                    monthlyBudget = 30000.0,
                    monthlyIncome = 0.0,
                    currencySymbol = "₹",
                    isOnboardingCompleted = false,
                    dailyReminderEnabled = false
                )
            )
        }
    }
}
