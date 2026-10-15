package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BillSplitEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.SplitParticipantEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        UserSettingsEntity::class,
        SubscriptionEntity::class,
        BillSplitEntity::class,
        SplitParticipantEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun billSplitDao(): BillSplitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expanse_tracker_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val userSettingsDao = database.userSettingsDao()

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

            if (userSettingsDao.getUserSettingsDirect() == null) {
                userSettingsDao.saveUserSettings(
                    UserSettingsEntity(
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
}
