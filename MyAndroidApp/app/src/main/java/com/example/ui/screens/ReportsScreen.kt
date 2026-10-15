package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.VividCoral
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.formatIndianCurrency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val currentMonthYear by viewModel.currentMonthYear.collectAsState()
    val currency = userSettings?.currencySymbol ?: "₹"
    val context = LocalContext.current

    val totalExpense = remember(monthlyExpenses) {
        monthlyExpenses.filter { it.expense.type == "EXPENSE" }.sumOf { it.expense.amount }
    }
    val totalIncome = remember(monthlyExpenses) {
        monthlyExpenses.filter { it.expense.type == "INCOME" }.sumOf { it.expense.amount }
    }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toInt().coerceAtLeast(0) else 0

    // Top 5 highest expenses
    val topExpenses = remember(monthlyExpenses) {
        monthlyExpenses
            .filter { it.expense.type == "EXPENSE" }
            .sortedByDescending { it.expense.amount }
            .take(5)
    }

    // Category breakdown
    val categoryBreakdown = remember(monthlyExpenses) {
        monthlyExpenses
            .filter { it.expense.type == "EXPENSE" }
            .groupBy { it.category?.name ?: "Others" }
            .mapValues { (_, list) -> list.sumOf { it.expense.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Monthly Statement Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ActiveScreen.Settings) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Selector Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_report_header"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousMonth() }) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                            }

                            Text(
                                text = currentMonthYear,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Expenses", style = MaterialTheme.typography.labelSmall, color = VividCoral)
                                    Text(
                                        totalExpense.formatIndianCurrency(currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Income", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                    Text(
                                        totalIncome.formatIndianCurrency(currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Savings Rate", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                    Text(
                                        "$savingsRate%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Print / Save PDF & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    printMonthlyStatementPdf(context, currentMonthYear, currency, totalExpense, totalIncome, categoryBreakdown, topExpenses)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Print / PDF", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    shareStatementText(context, currentMonthYear, currency, totalExpense, totalIncome, categoryBreakdown)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Report")
                            }
                        }
                    }
                }
            }

            // Category Breakdown Section
            item {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (categoryBreakdown.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded for $currentMonthYear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(categoryBreakdown) { (catName, amount) ->
                    val percentage = if (totalExpense > 0) ((amount / totalExpense) * 100).toInt() else 0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = catName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "$percentage% of monthly outflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = amount.formatIndianCurrency(currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Top Spends
            if (topExpenses.isNotEmpty()) {
                item {
                    Text(
                        text = "Top 5 Spending Merchants / Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(topExpenses) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.expense.note?.ifBlank { item.category?.name ?: "Expense" } ?: (item.category?.name ?: "Expense"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${item.category?.name ?: "Uncategorized"} • ${item.expense.paymentMethod}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = item.expense.amount.formatIndianCurrency(currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VividCoral
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

private fun printMonthlyStatementPdf(
    context: Context,
    monthYear: String,
    currency: String,
    totalExpense: Double,
    totalIncome: Double,
    categories: List<Pair<String, Double>>,
    topExpenses: List<com.example.data.model.ExpenseWithCategory>
) {
    try {
        val webView = WebView(context)
        val htmlContent = buildHtmlStatement(monthYear, currency, totalExpense, totalIncome, categories, topExpenses)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("SpendSense_Statement_$monthYear")
                printManager?.print(
                    "SpendSense Statement - $monthYear",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        Toast.makeText(context, "Preparing Print / PDF Document...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to start print: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareStatementText(
    context: Context,
    monthYear: String,
    currency: String,
    totalExpense: Double,
    totalIncome: Double,
    categories: List<Pair<String, Double>>
) {
    val net = totalIncome - totalExpense
    val sb = StringBuilder()
    sb.appendLine("📊 SpendSense Statement: $monthYear")
    sb.appendLine("----------------------------------")
    sb.appendLine("💸 Total Expenses: $currency${"%.2f".format(totalExpense)}")
    sb.appendLine("💰 Total Income: $currency${"%.2f".format(totalIncome)}")
    sb.appendLine("📈 Net Balance: $currency${"%.2f".format(net)}")
    sb.appendLine("\nTop Spending Categories:")
    categories.take(5).forEach { (cat, amt) ->
        val pct = if (totalExpense > 0) ((amt / totalExpense) * 100).toInt() else 0
        sb.appendLine(" • $cat: $currency${"%.2f".format(amt)} ($pct%)")
    }
    sb.appendLine("\nGenerated with SpendSense - Fast & Private UPI Expense Tracker")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "SpendSense Statement - $monthYear")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Financial Statement"))
}

private fun buildHtmlStatement(
    monthYear: String,
    currency: String,
    totalExpense: Double,
    totalIncome: Double,
    categories: List<Pair<String, Double>>,
    topExpenses: List<com.example.data.model.ExpenseWithCategory>
): String {
    val catRows = categories.joinToString("") { (cat, amt) ->
        val pct = if (totalExpense > 0) ((amt / totalExpense) * 100).toInt() else 0
        "<tr><td style='padding:8px;border-bottom:1px solid #e2e8f0;'>$cat</td><td style='padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;'>$currency${"%.2f".format(amt)}</td><td style='padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;'>$pct%</td></tr>"
    }

    val topRows = topExpenses.joinToString("") { item ->
        val title = item.expense.note?.ifBlank { item.category?.name ?: "Expense" } ?: "Expense"
        "<tr><td style='padding:8px;border-bottom:1px solid #e2e8f0;'>$title</td><td style='padding:8px;border-bottom:1px solid #e2e8f0;'>${item.category?.name ?: "General"}</td><td style='padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;color:#ef4444;'>$currency${"%.2f".format(item.expense.amount)}</td></tr>"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 24px; color: #1e293b; }
                h1 { margin: 0 0 6px 0; font-size: 24px; color: #0f172a; }
                .subtitle { color: #64748b; font-size: 14px; margin-bottom: 24px; }
                .grid { display: flex; gap: 12px; margin-bottom: 24px; }
                .card { flex: 1; padding: 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; }
                .label { font-size: 12px; color: #64748b; font-weight: 600; text-transform: uppercase; }
                .value { font-size: 20px; font-weight: bold; margin-top: 4px; }
                table { width: 100%; border-collapse: collapse; margin-top: 12px; margin-bottom: 24px; font-size: 14px; }
                th { background: #f1f5f9; padding: 8px; text-align: left; font-size: 12px; color: #475569; }
                .footer { font-size: 12px; color: #94a3b8; text-align: center; margin-top: 36px; }
            </style>
        </head>
        <body>
            <h1>SpendSense Financial Statement</h1>
            <div class="subtitle">Statement Period: $monthYear</div>

            <div class="grid">
                <div class="card">
                    <div class="label">Total Expenses</div>
                    <div class="value" style="color: #ef4444;">$currency${"%.2f".format(totalExpense)}</div>
                </div>
                <div class="card">
                    <div class="label">Total Income</div>
                    <div class="value" style="color: #10b981;">$currency${"%.2f".format(totalIncome)}</div>
                </div>
                <div class="card">
                    <div class="label">Net Savings</div>
                    <div class="value" style="color: #0284c7;">$currency${"%.2f".format(totalIncome - totalExpense)}</div>
                </div>
            </div>

            <h2 style="font-size: 16px; margin-top: 24px;">Category Spending Breakdown</h2>
            <table>
                <thead>
                    <tr><th>Category</th><th style="text-align:right;">Amount</th><th style="text-align:right;">Share</th></tr>
                </thead>
                <tbody>
                    $catRows
                </tbody>
            </table>

            <h2 style="font-size: 16px; margin-top: 24px;">Top Recorded Transactions</h2>
            <table>
                <thead>
                    <tr><th>Description</th><th>Category</th><th style="text-align:right;">Amount</th></tr>
                </thead>
                <tbody>
                    $topRows
                </tbody>
            </table>

            <div class="footer">
                Generated securely on-device with SpendSense. All financial records are stored offline.
            </div>
        </body>
        </html>
    """.trimIndent()
}
