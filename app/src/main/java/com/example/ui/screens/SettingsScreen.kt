package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.VividCoral
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.UpiDetectionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()

    var budgetText by remember(userSettings) { mutableStateOf(userSettings?.monthlyBudget?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "30000") }
    var incomeText by remember(userSettings) { mutableStateOf(userSettings?.monthlyIncome?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "0") }
    var currencySymbol by remember(userSettings) { mutableStateOf(userSettings?.currencySymbol ?: "₹") }
    var themeMode by remember(userSettings) { mutableStateOf(userSettings?.themeMode ?: "DARK") }
    var reminderEnabled by remember(userSettings) { mutableStateOf(userSettings?.dailyReminderEnabled ?: false) }
    var autoDetectUpi by remember(userSettings) { mutableStateOf(userSettings?.autoDetectUpi ?: true) }
    var autoSaveUpi by remember(userSettings) { mutableStateOf(userSettings?.autoSaveUpi ?: false) }
    var appLockEnabled by remember(userSettings) { mutableStateOf(userSettings?.isAppLockEnabled ?: false) }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    val currencies = listOf("₹", "$", "€", "£", "¥", "A$", "C$")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Budget & Income Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Budget & Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Monthly Budget Target") },
                        prefix = { Text("$currencySymbol ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_budget")
                    )

                    OutlinedTextField(
                        value = incomeText,
                        onValueChange = { incomeText = it },
                        label = { Text("Monthly Income Estimate") },
                        prefix = { Text("$currencySymbol ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_income")
                    )

                    Button(
                        onClick = {
                            val b = budgetText.toDoubleOrNull() ?: 2500.0
                            val i = incomeText.toDoubleOrNull() ?: 4000.0
                            userSettings?.let { current ->
                                viewModel.updateUserSettings(
                                    current.copy(
                                        monthlyBudget = b,
                                        monthlyIncome = i,
                                        currencySymbol = currencySymbol,
                                        themeMode = themeMode,
                                        dailyReminderEnabled = reminderEnabled
                                    )
                                )
                                Toast.makeText(context, "Budget settings saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_settings")
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Currency Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { symbol ->
                            val isSelected = currencySymbol == symbol
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    currencySymbol = symbol
                                    userSettings?.let {
                                        viewModel.updateUserSettings(it.copy(currencySymbol = symbol))
                                    }
                                },
                                label = { Text(symbol, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }
        }

        // Appearance / Theme Mode Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = themeMode == "DARK",
                            onClick = {
                                themeMode = "DARK"
                                userSettings?.let { viewModel.updateUserSettings(it.copy(themeMode = "DARK")) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("Editorial Dark", fontWeight = FontWeight.Bold)
                        }
                        SegmentedButton(
                            selected = themeMode == "LIGHT",
                            onClick = {
                                themeMode = "LIGHT"
                                userSettings?.let { viewModel.updateUserSettings(it.copy(themeMode = "LIGHT")) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Warm Light", fontWeight = FontWeight.Bold)
                        }
                        SegmentedButton(
                            selected = themeMode == "SYSTEM",
                            onClick = {
                                themeMode = "SYSTEM"
                                userSettings?.let { viewModel.updateUserSettings(it.copy(themeMode = "SYSTEM")) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("System", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Automatic UPI Expense Detection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_upi_detection_settings"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto UPI Detection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detect spends from GPay, PhonePe, Paytm & SMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission Status Row
                    val hasAccess = remember { UpiDetectionManager.isNotificationAccessGranted(context) }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (hasAccess) Emerald500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (hasAccess) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = if (hasAccess) Emerald500 else VividCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasAccess) "Listener active & monitoring" else "Notification access needed",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!hasAccess) {
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Open Settings > Notification Access to grant permission", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Grant Access", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Save Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Save Spends",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (autoSaveUpi) "Directly saves spends to database" else "Prompts on Home screen to review first",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoSaveUpi,
                            onCheckedChange = { checked ->
                                autoSaveUpi = checked
                                userSettings?.let { viewModel.updateUserSettings(it.copy(autoSaveUpi = checked)) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Simulator Button
                    OutlinedButton(
                        onClick = {
                            viewModel.simulateUpiNotification()
                            Toast.makeText(context, "Simulated UPI notification triggered! Check Home screen.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Sample UPI Notification")
                    }
                }
            }
        }

        // Smart Financial Features & Security
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_smart_tools"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    Text(
                        text = "Smart Financial Tools",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
                    )

                    // 1. Category Budget Limits
                    ListItem(
                        headlineContent = { Text("Category Budget Limits", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Set individual category limits with 80% alerts") },
                        leadingContent = { Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald500) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.ActiveScreen.CategoryBudgets) }
                            .padding(horizontal = 4.dp)
                            .testTag("item_category_budgets")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // 2. Recurring Subscriptions & Bills
                    ListItem(
                        headlineContent = { Text("Subscriptions & Bill Reminders", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Track monthly services with due countdowns") },
                        leadingContent = { Icon(Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.ActiveScreen.Subscriptions) }
                            .padding(horizontal = 4.dp)
                            .testTag("item_subscriptions")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // 3. Split Bills / IOUs
                    ListItem(
                        headlineContent = { Text("Split Bills & Group Expenses", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Calculate & track who owes what among friends") },
                        leadingContent = { Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.ActiveScreen.SplitBills) }
                            .padding(horizontal = 4.dp)
                            .testTag("item_split_bills")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // 4. Monthly Statement Reports
                    ListItem(
                        headlineContent = { Text("Monthly Statement Reports", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("View, share & print styled PDF statements") },
                        leadingContent = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = VividCoral) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.ActiveScreen.Reports) }
                            .padding(horizontal = 4.dp)
                            .testTag("item_monthly_reports")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // 5. App PIN Lock
                    ListItem(
                        headlineContent = { Text("App PIN Lock", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Lock app when closed to protect financial data") },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500) },
                        trailingContent = {
                            Switch(
                                checked = appLockEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (userSettings?.appLockPin.isNullOrBlank()) {
                                            showPinSetupDialog = true
                                        } else {
                                            appLockEnabled = true
                                            viewModel.setAppLock(true, userSettings?.appLockPin, true)
                                            Toast.makeText(context, "App lock enabled!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        appLockEnabled = false
                                        viewModel.setAppLock(false)
                                        Toast.makeText(context, "App lock disabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }

        // Actions & Management List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Category Management
                    ListItem(
                        headlineContent = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Add custom expense categories and colors") },
                        leadingContent = { Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.example.ui.viewmodel.ActiveScreen.Categories) }
                            .padding(4.dp)
                            .testTag("item_manage_categories")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Daily Reminder Toggle
                    ListItem(
                        headlineContent = { Text("Daily Expense Reminder", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Receive a daily evening nudge to log transactions") },
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Emerald500) },
                        trailingContent = {
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { checked ->
                                    reminderEnabled = checked
                                    userSettings?.let { viewModel.updateUserSettings(it.copy(dailyReminderEnabled = checked)) }
                                    val msg = if (checked) "Daily reminders enabled for 8:00 PM!" else "Reminders disabled"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Export CSV
                    ListItem(
                        headlineContent = { Text("Export Data (CSV)", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Share or save transaction history spreadsheet") },
                        leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = {
                            Button(
                                onClick = {
                                    val csv = viewModel.generateCsvData()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "SpendSense Export.csv")
                                        putExtra(Intent.EXTRA_TEXT, csv)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export Transactions"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Export")
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Clear All Data
                    ListItem(
                        headlineContent = { Text("Clear All Data & Reset App", fontWeight = FontWeight.Bold, color = VividCoral) },
                        supportingContent = { Text("Permanently delete all expenses, subscriptions, bill splits, category budgets, and reset app to fresh install") },
                        leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = VividCoral) },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VividCoral),
                                modifier = Modifier.testTag("btn_clear_all_data")
                            ) {
                                Text("Clear Data")
                            }
                        }
                    )
                }
            }
        }

        // App Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                com.example.ui.components.SpendSenseLogo(badgeSize = 64.dp, textSize = 22)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SpendSense v1.0.0 • Smart Personal Finance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data & Reset App?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete ALL data records (expenses, subscriptions, split bills, category budget limits, security PIN) and return the app to a clean, brand new installation state. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                        Toast.makeText(context, "All data deleted. App reset to new state.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividCoral, contentColor = Color.White),
                    modifier = Modifier.testTag("btn_confirm_clear_all")
                ) {
                    Text("Yes, Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPinSetupDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinSetupDialog = false
                appLockEnabled = false
            },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                Column {
                    Text(
                        text = "Create a 4-digit PIN to secure SpendSense whenever the app is opened.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) pinInput = it.filter { ch -> ch.isDigit() }
                        },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("input_setup_pin")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.setAppLock(true, pinInput, true)
                            appLockEnabled = true
                            showPinSetupDialog = false
                            pinInput = ""
                            Toast.makeText(context, "App PIN lock enabled successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black)
                ) {
                    Text("Set & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinSetupDialog = false
                    appLockEnabled = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
