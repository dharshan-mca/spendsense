package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.ui.components.*
import com.example.ui.components.SpendSenseBadge
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.Emerald500
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassPill
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.VividCoral
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.liquidGlassBorderBrush
import com.example.ui.theme.liquidGlassSurfaceBrush
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.formatIndianCurrency
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentCalendar by viewModel.currentCalendar.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val categorySummaries by viewModel.categorySummaries.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val pendingUpiTransactions by viewModel.pendingUpiTransactions.collectAsState()

    val isDark = isSystemInDarkTheme()
    val currency = userSettings?.currencySymbol ?: "₹"
    val budget = userSettings?.monthlyBudget ?: 30000.0
    val income = userSettings?.monthlyIncome ?: 0.0

    val expensesOnly = monthlyExpenses.filter { it.expense.type == "EXPENSE" }
    val incomeOnly = monthlyExpenses.filter { it.expense.type == "INCOME" }

    val totalSpent = expensesOnly.sumOf { it.expense.amount }
    val totalIncome = income + incomeOnly.sumOf { it.expense.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // SpendSense Apple-Style Liquid Glass Top Brand Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiquidGlassPill(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpendSenseBadge(size = 34.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Spend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sense",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald500
                                )
                            }
                            Text(
                                text = "Liquid Glass Edition",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Emerald500
                            )
                        }
                    }
                }

                // Apple-style live optical indicator badge
                LiquidGlassPill(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Sync",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Month Header Selector (Frosted Liquid Glass Capsule)
        item {
            MonthHeaderSelector(
                currentCalendar = currentCalendar,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onResetToCurrentMonth = {
                    val now = java.util.Calendar.getInstance()
                    viewModel.setMonth(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH))
                }
            )
        }

        // Pending Auto-Detected UPI Payments
        if (pendingUpiTransactions.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingUpiTransactions.forEach { item ->
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_detected_upi_${item.id}"),
                            shape = RoundedCornerShape(22.dp),
                            accentBorder = Emerald500
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Auto-Detected UPI Payment",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald500
                                        )
                                        Text(
                                            text = "Captured from ${item.sourceApp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.dismissDetectedUpi(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.merchant,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Category: ${item.categoryName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = item.amount.formatIndianCurrency(currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VividCoral
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.confirmDetectedUpi(item) },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Confirm & Log", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.openAddExpenseSheet(defaultPaymentMethod = "UPI")
                                        viewModel.dismissDetectedUpi(item.id)
                                    },
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apple Liquid Glass Hero Balance & Budget Progress Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                elevation = 8.dp,
                accentBorder = LiquidGlassTokens.RefractTeal
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BudgetProgressRing(
                        spentAmount = totalSpent,
                        budgetAmount = budget,
                        currencySymbol = currency
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Optical Translucent Income vs Expense Glass Pods
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Income Capsule
                        LiquidGlassPill(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            elevation = 2.dp,
                            accentBorder = ElectricLime.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ElectricLime.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Income",
                                        tint = ElectricLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "INCOME",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = totalIncome.formatIndianCurrency(currency, compact = true),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Total Expense Capsule
                        LiquidGlassPill(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            elevation = 2.dp,
                            accentBorder = VividCoral.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(VividCoral.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Expense",
                                        tint = VividCoral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "EXPENSES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = totalSpent.formatIndianCurrency(currency, compact = true),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row (Liquid Emerald & Frosted Glass Pills)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Liquid Emerald Action Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = Emerald500.copy(alpha = 0.4f),
                            spotColor = Emerald500.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF34D399),
                                    Emerald500,
                                    Color(0xFF0F5F53)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { viewModel.openAddExpenseSheet() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Log Expense", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // Secondary Optical Frosted Glass Action Button
                LiquidGlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable { viewModel.openAddExpenseSheet(defaultPaymentMethod = "UPI") },
                    shape = RoundedCornerShape(18.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UPI Spend", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Low Latency AI Assistant Banner Card (Iridescent Glass Refraction)
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openAiAssistantSheet() }
                    .testTag("card_low_latency_ai"),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp,
                accentBorder = ElectricLime
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ElectricLime.copy(alpha = 0.22f))
                            .border(1.dp, ElectricLime.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Low-Latency AI",
                            tint = ElectricLime,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Low-Latency AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald500
                            ) {
                                Text(
                                    text = "FAST",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = "Instant budget insights & spend analysis powered by Gemini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask AI",
                        tint = ElectricLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick Smart Tools Row (Floating Liquid Glass Capsules)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(ActiveScreen.Subscriptions) }
                        .testTag("quick_tool_subs"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventRepeat, contentDescription = null, tint = Emerald500, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Bills & Subs", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                LiquidGlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(ActiveScreen.SplitBills) }
                        .testTag("quick_tool_splits"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Split Bills", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                LiquidGlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(ActiveScreen.CategoryBudgets) }
                        .testTag("quick_tool_budgets"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Budgets", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                LiquidGlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(ActiveScreen.Reports) }
                        .testTag("quick_tool_reports"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = VividCoral, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PDF Report", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Breakdown Chart (Liquid Glass Card)
        item {
            CategoryDonutChart(
                summaries = categorySummaries,
                currencySymbol = currency
            )
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.History) },
                    modifier = Modifier.testTag("btn_see_all_transactions")
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Recent Transactions Items
        if (monthlyExpenses.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_empty_transactions"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.15f))
                                .border(1.dp, Emerald500.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions logged yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your spending will appear here as soon as you log an expense or receive UPI payment notifications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openAddExpenseSheet() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                            modifier = Modifier.testTag("btn_log_first_expense")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log First Expense", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(monthlyExpenses.take(6), key = { it.expense.id }) { item ->
                ExpenseItemCard(
                    item = item,
                    currencySymbol = currency,
                    onEdit = { viewModel.openAddExpenseSheet(item.expense) },
                    onDelete = { viewModel.deleteExpense(item.expense.id) }
                )
            }
        }
    }
}
