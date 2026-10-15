package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.GeminiRepository
import com.example.util.DetectedUpiTransaction
import com.example.util.UpiDetectionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val latencyMs: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

sealed class ActiveScreen {
    object Onboarding : ActiveScreen()
    object Home : ActiveScreen()
    object Analytics : ActiveScreen()
    object History : ActiveScreen()
    object Categories : ActiveScreen()
    object CategoryBudgets : ActiveScreen()
    object Subscriptions : ActiveScreen()
    object SplitBills : ActiveScreen()
    object Reports : ActiveScreen()
    object Settings : ActiveScreen()
}

data class FilterState(
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val selectedPaymentMethod: String? = null,
    val selectedType: String? = null,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ExpenseRepository(
        database.expenseDao(),
        database.categoryDao(),
        database.userSettingsDao(),
        database.subscriptionDao(),
        database.billSplitDao()
    )
    private val geminiRepository = GeminiRepository()

    // AI Assistant State
    private val _showAiAssistantSheet = MutableStateFlow(false)
    val showAiAssistantSheet: StateFlow<Boolean> = _showAiAssistantSheet.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                sender = "AI",
                text = "Hello! I am your low-latency AI Expense Assistant powered by Gemini 3.1 Flash-Lite. Ask me about budget analysis, savings tips, or expense breakdown!"
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Current selected Month/Year
    private val _currentCalendar = MutableStateFlow(Calendar.getInstance())
    val currentCalendar: StateFlow<Calendar> = _currentCalendar.asStateFlow()

    val currentMonthYear: StateFlow<String> = _currentCalendar.map { cal ->
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()))

    // Navigation State
    private val _activeScreen = MutableStateFlow<ActiveScreen>(ActiveScreen.Home)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // User Settings Flow
    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Categories Flow
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All-time expenses
    val allExpenses: StateFlow<List<ExpenseWithCategory>> = repository.allExpensesWithCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Month Expenses Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyExpenses: StateFlow<List<ExpenseWithCategory>> = _currentCalendar
        .flatMapLatest { cal ->
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            repository.getExpensesForMonth(year, month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter state for Transaction History
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Filtered Expenses
    val filteredExpenses: StateFlow<List<ExpenseWithCategory>> = combine(
        allExpenses,
        _filterState
    ) { expenses, filter ->
        expenses.filter { item ->
            val matchesQuery = filter.searchQuery.isEmpty() ||
                    (item.expense.note?.contains(filter.searchQuery, ignoreCase = true) == true) ||
                    (item.category?.name?.contains(filter.searchQuery, ignoreCase = true) == true)

            val matchesCategory = filter.selectedCategoryId == null || item.expense.categoryId == filter.selectedCategoryId
            val matchesPayment = filter.selectedPaymentMethod == null || item.expense.paymentMethod.equals(filter.selectedPaymentMethod, ignoreCase = true)

            val matchesStart = filter.startDateMillis == null || item.expense.dateMillis >= filter.startDateMillis
            val matchesEnd = filter.endDateMillis == null || item.expense.dateMillis <= filter.endDateMillis

            val matchesType = filter.selectedType == null || item.expense.type.equals(filter.selectedType, ignoreCase = true)

            matchesQuery && matchesCategory && matchesPayment && matchesType && matchesStart && matchesEnd
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Summaries for Selected Month
    val categorySummaries: StateFlow<List<CategoryExpenseSummary>> = monthlyExpenses.map { list ->
        val expenseItems = list.filter { it.expense.type == "EXPENSE" }
        val totalSpent = expenseItems.sumOf { it.expense.amount }
        if (totalSpent == 0.0) return@map emptyList()

        val grouped = expenseItems.groupBy { it.category }
        grouped.mapNotNull { (category, items) ->
            if (category == null) null
            else {
                val catTotal = items.sumOf { it.expense.amount }
                CategoryExpenseSummary(
                    category = category,
                    totalAmount = catTotal,
                    percentage = ((catTotal / totalSpent) * 100).toFloat(),
                    transactionCount = items.size
                )
            }
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6 Month Spend Trend for Analytics
    val sixMonthTrends: StateFlow<List<MonthlySpendTrend>> = allExpenses.map { all ->
        val result = mutableListOf<MonthlySpendTrend>()
        val cal = Calendar.getInstance()
        val sdfLabel = SimpleDateFormat("MMM", Locale.getDefault())
        val sdfKey = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (i in 5 downTo 0) {
            val targetCal = (cal.clone() as Calendar).apply {
                add(Calendar.MONTH, -i)
            }
            val targetYear = targetCal.get(Calendar.YEAR)
            val targetMonth = targetCal.get(Calendar.MONTH)

            val monthExpenses = all.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.expense.dateMillis }
                c.get(Calendar.YEAR) == targetYear && c.get(Calendar.MONTH) == targetMonth
            }

            val spend = monthExpenses.filter { it.expense.type == "EXPENSE" }.sumOf { it.expense.amount }
            val income = monthExpenses.filter { it.expense.type == "INCOME" }.sumOf { it.expense.amount }

            result.add(
                MonthlySpendTrend(
                    monthYearLabel = sdfLabel.format(targetCal.time),
                    yearMonth = sdfKey.format(targetCal.time),
                    totalSpend = spend,
                    totalIncome = income
                )
            )
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add / Edit Sheet Dialog State
    private val _editingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val editingExpense: StateFlow<ExpenseEntity?> = _editingExpense.asStateFlow()

    private val _showAddExpenseSheet = MutableStateFlow(false)
    val showAddExpenseSheet: StateFlow<Boolean> = _showAddExpenseSheet.asStateFlow()

    // Subscriptions Flow
    val allSubscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bill Splits Flow
    val allSplitsWithParticipants: StateFlow<List<BillSplitWithParticipants>> = repository.allSplitsWithParticipants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Lock State
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        // Seed initial data asynchronously on start
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }

        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                if (settings?.isAppLockEnabled == true && !settings.appLockPin.isNullOrBlank()) {
                    _isAppLocked.value = true
                }
            }
        }
    }

    // --- Actions ---

    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun previousMonth() {
        _currentCalendar.update { cal ->
            (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        }
    }

    fun nextMonth() {
        _currentCalendar.update { cal ->
            (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        }
    }

    fun setMonth(year: Int, monthZeroBased: Int) {
        _currentCalendar.update { cal ->
            (cal.clone() as Calendar).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthZeroBased)
            }
        }
    }

    private val _presetPaymentMethod = MutableStateFlow<String>("UPI")
    val presetPaymentMethod: StateFlow<String> = _presetPaymentMethod.asStateFlow()

    fun openAddExpenseSheet(expenseToEdit: ExpenseEntity? = null, defaultPaymentMethod: String? = null) {
        _editingExpense.value = expenseToEdit
        _presetPaymentMethod.value = defaultPaymentMethod ?: expenseToEdit?.paymentMethod ?: "UPI"
        _showAddExpenseSheet.value = true
    }

    fun closeAddExpenseSheet() {
        _showAddExpenseSheet.value = false
        _editingExpense.value = null
    }

    fun saveExpense(
        id: Long = 0,
        amount: Double,
        categoryId: Long,
        note: String?,
        dateMillis: Long,
        paymentMethod: String,
        type: String = "EXPENSE"
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = id,
                amount = amount,
                categoryId = categoryId,
                note = note,
                dateMillis = dateMillis,
                paymentMethod = paymentMethod,
                type = type
            )
            if (id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
            closeAddExpenseSheet()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun createCustomCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    isCustom = true
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            repository.setOnboardingCompleted(completed)
            if (completed) {
                _activeScreen.value = ActiveScreen.Home
            }
        }
    }

    fun updateFilterQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updateFilterCategory(categoryId: Long?) {
        _filterState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateFilterPaymentMethod(method: String?) {
        _filterState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun updateFilterType(type: String?) {
        _filterState.update { it.copy(selectedType = type) }
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            UpiDetectionManager.clearAllPending()
            _isAppLocked.value = false
            _filterState.value = FilterState()
            _editingExpense.value = null
            _showAddExpenseSheet.value = false
            _showAiAssistantSheet.value = false
            _aiMessages.value = listOf(
                AiMessage(
                    sender = "AI",
                    text = "Hello! I am your low-latency AI Expense Assistant powered by Gemini 3.1 Flash-Lite. Ask me about budget analysis, savings tips, or expense breakdown!"
                )
            )
            _activeScreen.value = ActiveScreen.Home
        }
    }

    fun generateCsvData(): String {
        val expenses = allExpenses.value
        val sb = StringBuilder()
        sb.append("ID,Date,Category,Amount,Payment Method,Note,Type\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (item in expenses) {
            val dateStr = sdf.format(Date(item.expense.dateMillis))
            val categoryName = item.category?.name ?: "Uncategorized"
            val note = (item.expense.note ?: "").replace(",", ";")
            sb.append("${item.expense.id},$dateStr,$categoryName,${item.expense.amount},${item.expense.paymentMethod},\"$note\",${item.expense.type}\n")
        }
        return sb.toString()
    }

    fun openAiAssistantSheet() {
        _showAiAssistantSheet.value = true
    }

    fun closeAiAssistantSheet() {
        _showAiAssistantSheet.value = false
    }

    fun askAiQuestion(question: String) {
        if (question.isBlank() || _isAiLoading.value) return

        val userMsg = AiMessage(sender = "USER", text = question)
        _aiMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val userSet = userSettings.value
            val currSymbol = userSet?.currencySymbol ?: "₹"
            val budgetVal = userSet?.monthlyBudget ?: 30000.0
            val mExpenses = monthlyExpenses.value
            val totalSpent = mExpenses.filter { it.expense.type == "EXPENSE" }.sumOf { it.expense.amount }
            val totalIncome = mExpenses.filter { it.expense.type == "INCOME" }.sumOf { it.expense.amount } + (userSet?.monthlyIncome ?: 0.0)

            val catSummaryText = categorySummaries.value.take(5).joinToString(", ") {
                "${it.category.name}: $currSymbol${it.totalAmount.toInt()}"
            }

            val systemContext = """
                You are a smart financial advisor providing low-latency, concise, actionable financial advice in an expense tracking app.
                User context:
                - Currency: $currSymbol
                - Monthly Income: $currSymbol${totalIncome.toInt()}
                - Monthly Budget: $currSymbol${budgetVal.toInt()}
                - Current Spent this month: $currSymbol${totalSpent.toInt()}
                - Top spending categories: $catSummaryText
                Be extremely quick, friendly, and concise (2-4 bullet points or 2 sentences max). Powered by Gemini 3.1 Flash-Lite.
            """.trimIndent()

            val result = geminiRepository.generateLowLatencyResponse(
                prompt = question,
                systemInstruction = systemContext
            )

            val endTime = System.currentTimeMillis()
            val latency = endTime - startTime

            val aiResponseText = result.getOrElse { err ->
                "Low-latency response attempt completed (${latency}ms). Note: ${err.message ?: "Unable to connect to Gemini API"}"
            }

            _aiMessages.update {
                it + AiMessage(
                    sender = "AI",
                    text = aiResponseText,
                    latencyMs = latency
                )
            }
            _isAiLoading.value = false
        }
    }

    // --- Automatic UPI Detection ---
    val pendingUpiTransactions = UpiDetectionManager.pendingTransactions
    val lastDetectedUpiToast = UpiDetectionManager.lastDetectedToast

    fun confirmDetectedUpi(transaction: DetectedUpiTransaction) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    amount = transaction.amount,
                    categoryId = transaction.suggestedCategoryId,
                    note = "${transaction.merchant} (${transaction.sourceApp})",
                    dateMillis = transaction.timestampMillis,
                    paymentMethod = "UPI",
                    type = "EXPENSE"
                )
            )
            UpiDetectionManager.dismissDetected(transaction.id)
        }
    }

    fun dismissDetectedUpi(id: String) {
        UpiDetectionManager.dismissDetected(id)
    }

    fun simulateUpiNotification(typeIndex: Int = (0..3).random()) {
        UpiDetectionManager.simulateSampleNotification(getApplication(), typeIndex)
    }

    fun clearUpiToast() {
        UpiDetectionManager.clearToast()
    }

    // --- Category Budget Limits ---
    fun updateCategoryBudgetLimit(categoryId: Long, limit: Double?) {
        viewModelScope.launch {
            repository.updateCategoryBudgetLimit(categoryId, limit)
        }
    }

    // --- Subscriptions & Recurring Bills ---
    fun addSubscription(
        name: String,
        amount: Double,
        categoryId: Long,
        dueDayOfMonth: Int,
        billingCycle: String = "MONTHLY",
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.insertSubscription(
                SubscriptionEntity(
                    name = name,
                    amount = amount,
                    categoryId = categoryId,
                    dueDayOfMonth = dueDayOfMonth,
                    billingCycle = billingCycle,
                    note = note
                )
            )
        }
    }

    fun markSubscriptionPaid(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            repository.updateSubscription(subscription.copy(lastPaidMonth = currentMonthStr))
            // Also log as an actual expense
            repository.insertExpense(
                ExpenseEntity(
                    amount = subscription.amount,
                    categoryId = subscription.categoryId,
                    note = "Subscription: ${subscription.name}",
                    dateMillis = System.currentTimeMillis(),
                    paymentMethod = "UPI",
                    type = "EXPENSE"
                )
            )
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    // --- Split Bills & Group Expenses ---
    fun createBillSplit(
        title: String,
        totalAmount: Double,
        paidBy: String,
        friendNames: List<String>,
        note: String = ""
    ) {
        viewModelScope.launch {
            val split = BillSplitEntity(
                title = title,
                totalAmount = totalAmount,
                paidBy = paidBy,
                note = note
            )
            val allNames = (listOf(paidBy) + friendNames).filter { it.isNotBlank() }.distinct()
            val perPerson = if (allNames.isNotEmpty()) totalAmount / allNames.size else totalAmount
            val participants = allNames.map { name ->
                SplitParticipantEntity(
                    splitId = 0,
                    name = name,
                    shareAmount = perPerson,
                    isPaid = (name.equals(paidBy, ignoreCase = true) || name.equals("You", ignoreCase = true))
                )
            }
            repository.insertSplit(split, participants)
        }
    }

    fun toggleParticipantPaid(participantId: Long, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updateParticipantPaidStatus(participantId, isPaid)
        }
    }

    fun toggleSplitSettled(splitId: Long, isSettled: Boolean) {
        viewModelScope.launch {
            repository.updateSplitSettledStatus(splitId, isSettled)
        }
    }

    fun deleteSplit(splitId: Long) {
        viewModelScope.launch {
            repository.deleteSplitById(splitId)
        }
    }

    // --- App Lock & Biometrics ---
    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        val settings = userSettings.value
        if (settings?.isAppLockEnabled == true && !settings.appLockPin.isNullOrBlank()) {
            _isAppLocked.value = true
        }
    }

    fun setAppLock(enabled: Boolean, pin: String? = null, biometric: Boolean = true) {
        viewModelScope.launch {
            val current = userSettings.value ?: return@launch
            val updated = current.copy(
                isAppLockEnabled = enabled,
                appLockPin = if (enabled) (pin ?: current.appLockPin) else null,
                biometricEnabled = biometric
            )
            repository.updateSettings(updated)
            if (!enabled) {
                _isAppLocked.value = false
            }
        }
    }
}
