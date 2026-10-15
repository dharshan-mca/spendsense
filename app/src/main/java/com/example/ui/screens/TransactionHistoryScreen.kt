package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseWithCategory
import com.example.ui.components.ExpenseItemCard
import com.example.ui.viewmodel.MainViewModel
import com.example.util.formatIndianCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val currency = userSettings?.currencySymbol ?: "₹"
    val totalFilteredAmount = filteredExpenses.sumOf { if (it.expense.type == "EXPENSE") it.expense.amount else -it.expense.amount }

    // Group expenses by Date
    val groupedExpenses = remember(filteredExpenses) {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        filteredExpenses.groupBy { sdf.format(Date(it.expense.dateMillis)) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Search Input Bar
        item {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { viewModel.updateFilterQuery(it) },
                placeholder = { Text("Search by note or category e.g. Swiggy, UPI...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = if (filterState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.updateFilterQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_history"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Category & Payment Method Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All / Clear Filter Chip
                val hasActiveFilters = filterState.selectedCategoryId != null || filterState.selectedPaymentMethod != null || filterState.selectedType != null || filterState.searchQuery.isNotEmpty()
                if (hasActiveFilters) {
                    AssistChip(
                        onClick = { viewModel.clearFilters() },
                        label = { Text("Clear Filters", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    )
                }

                // Type Filter: Expenses / Income
                FilterChip(
                    selected = filterState.selectedType == "EXPENSE",
                    onClick = {
                        viewModel.updateFilterType(if (filterState.selectedType == "EXPENSE") null else "EXPENSE")
                    },
                    label = { Text("Expenses") }
                )

                FilterChip(
                    selected = filterState.selectedType == "INCOME",
                    onClick = {
                        viewModel.updateFilterType(if (filterState.selectedType == "INCOME") null else "INCOME")
                    },
                    label = { Text("Income") }
                )

                // Payment Method Filters
                listOf("UPI", "Cash", "Card", "Net Banking").forEach { method ->
                    val isSelected = filterState.selectedPaymentMethod.equals(method, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.updateFilterPaymentMethod(if (isSelected) null else method)
                        },
                        label = { Text(method) }
                    )
                }

                // Category Filters
                categories.forEach { category ->
                    val isSelected = filterState.selectedCategoryId == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.updateFilterCategory(if (isSelected) null else category.id)
                        },
                        label = { Text(category.name) }
                    )
                }
            }
        }

        // Filter Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredExpenses.size} Transactions Found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Total: ${totalFilteredAmount.formatIndianCurrency(currency)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Grouped Transactions List
        if (groupedExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions match your filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            groupedExpenses.forEach { (dateHeader, items) ->
                item {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(items, key = { it.expense.id }) { item ->
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
}
