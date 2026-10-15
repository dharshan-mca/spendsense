package com.example.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseEntity
import com.example.service.UpiNotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DetectedUpiTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val merchant: String,
    val suggestedCategoryId: Long,
    val categoryName: String,
    val sourceApp: String,
    val rawNotification: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

object UpiDetectionManager {

    private val _pendingTransactions = MutableStateFlow<List<DetectedUpiTransaction>>(emptyList())
    val pendingTransactions: StateFlow<List<DetectedUpiTransaction>> = _pendingTransactions.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _lastDetectedToast = MutableStateFlow<String?>(null)
    val lastDetectedToast: StateFlow<String?> = _lastDetectedToast.asStateFlow()

    fun updateServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun clearToast() {
        _lastDetectedToast.value = null
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val cn = ComponentName(context, UpiNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    fun onTransactionDetected(context: Context, parsed: ParsedUpiTransaction) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val settings = db.userSettingsDao().getUserSettingsDirect()
            val autoSave = settings?.autoSaveUpi ?: false

            if (autoSave) {
                // Directly insert into Room database
                val expense = ExpenseEntity(
                    amount = parsed.amount,
                    categoryId = parsed.suggestedCategoryId,
                    note = "${parsed.merchant} (${parsed.sourceApp})",
                    dateMillis = parsed.timestampMillis,
                    paymentMethod = "UPI",
                    type = "EXPENSE"
                )
                db.expenseDao().insertExpense(expense)
                _lastDetectedToast.value = "Auto-logged ₹${String.format(java.util.Locale.US, "%.0f", parsed.amount)} at ${parsed.merchant}"
            } else {
                // Add to pending transactions for user review
                val detected = DetectedUpiTransaction(
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    suggestedCategoryId = parsed.suggestedCategoryId,
                    categoryName = parsed.categoryName,
                    sourceApp = parsed.sourceApp,
                    rawNotification = parsed.rawText,
                    timestampMillis = parsed.timestampMillis
                )
                _pendingTransactions.update { listOf(detected) + it }
                _lastDetectedToast.value = "UPI payment detected: ₹${String.format(java.util.Locale.US, "%.0f", parsed.amount)} to ${parsed.merchant}"
            }
        }
    }

    fun dismissDetected(id: String) {
        _pendingTransactions.update { list -> list.filterNot { it.id == id } }
    }

    fun clearAllPending() {
        _pendingTransactions.value = emptyList()
    }

    fun simulateSampleNotification(context: Context, sampleType: Int = 0) {
        val sample = when (sampleType % 4) {
            0 -> ParsedUpiTransaction(
                amount = 220.0,
                merchant = "Swiggy",
                suggestedCategoryId = 1L,
                categoryName = "Food & Dining",
                sourceApp = "Google Pay",
                rawText = "Paid ₹220.00 to Swiggy using Bank Account"
            )
            1 -> ParsedUpiTransaction(
                amount = 550.0,
                merchant = "Shell Petrol Station",
                suggestedCategoryId = 2L,
                categoryName = "Transport & Auto",
                sourceApp = "PhonePe",
                rawText = "Payment of ₹550 to Shell Petrol Station is successful"
            )
            2 -> ParsedUpiTransaction(
                amount = 145.0,
                merchant = "Sharma Kirana Store",
                suggestedCategoryId = 3L,
                categoryName = "Kirana & Groceries",
                sourceApp = "Paytm",
                rawText = "Paid ₹145 to Sharma Kirana Store via UPI"
            )
            else -> ParsedUpiTransaction(
                amount = 299.0,
                merchant = "Jio Prepaid Recharge",
                suggestedCategoryId = 4L,
                categoryName = "Bills & Recharge",
                sourceApp = "BHIM",
                rawText = "Paid ₹299 to Jio Prepaid Recharge using UPI"
            )
        }

        onTransactionDetected(context, sample)
    }
}
