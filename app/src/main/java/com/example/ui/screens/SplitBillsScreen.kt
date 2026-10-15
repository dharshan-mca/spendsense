package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.VividCoral
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.formatIndianCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val splits by viewModel.allSplitsWithParticipants.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val currency = userSettings?.currencySymbol ?: "₹"
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    // Calculate how much others owe you
    val totalOwedToYou = remember(splits) {
        splits.filter { it.split.paidBy.equals("You", ignoreCase = true) && !it.split.isSettled }
            .flatMap { it.participants }
            .filter { !it.name.equals("You", ignoreCase = true) && !it.isPaid }
            .sumOf { it.shareAmount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Split Bills & IOUs",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald500,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_split")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Split Bill")
            }
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
            // Summary banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_splits_summary"),
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
                            Text(
                                text = "Group Expense Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (totalOwedToYou > 0) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (totalOwedToYou > 0) "Pending Collection" else "All Settled",
                                    color = if (totalOwedToYou > 0) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "You are Owed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = totalOwedToYou.formatIndianCurrency(currency),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald500
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Active Splits",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${splits.count { !it.split.isSettled }} Pending",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (splits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Bill Splits Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Split restaurant bills, trip cabs, or apartment rent with flatmates and friends effortlessly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Split a Bill")
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Group Splits (${splits.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(splits, key = { it.split.id }) { item ->
                    val split = item.split
                    val participants = item.participants
                    val allPaid = participants.all { it.isPaid }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("card_split_${split.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (split.isSettled || allPaid) Emerald500.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = split.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Paid by ${split.paidBy} • ${participants.size} people",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = split.totalAmount.formatIndianCurrency(currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Participants (${(split.totalAmount / (participants.size.coerceAtLeast(1))).formatIndianCurrency(currency)} each):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // List participants with checkboxes
                            participants.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = p.isPaid,
                                            onCheckedChange = { checked ->
                                                viewModel.toggleParticipantPaid(p.id, checked)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = Emerald500)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (p.isPaid) FontWeight.Normal else FontWeight.SemiBold,
                                            color = if (p.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = if (p.isPaid) "Settled" else p.shareAmount.formatIndianCurrency(currency),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (p.isPaid) Emerald500 else VividCoral
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.toggleSplitSettled(split.id, !split.isSettled)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (split.isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (split.isSettled) Emerald500 else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (split.isSettled) "Settled" else "Mark Bill Fully Settled")
                                }

                                IconButton(
                                    onClick = { viewModel.deleteSplit(split.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Split Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var totalAmountStr by remember { mutableStateOf("") }
        var friendsInput by remember { mutableStateOf("") }
        var paidByInput by remember { mutableStateOf("You") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Split a New Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Description (e.g. Dinner, Goa Cab)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_split_title")
                    )

                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { totalAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Total Bill Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_split_amount")
                    )

                    OutlinedTextField(
                        value = paidByInput,
                        onValueChange = { paidByInput = it },
                        label = { Text("Who Paid?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_split_payer")
                    )

                    OutlinedTextField(
                        value = friendsInput,
                        onValueChange = { friendsInput = it },
                        label = { Text("Friends (comma-separated names)") },
                        placeholder = { Text("e.g. Rahul, Sneha, Amit") },
                        modifier = Modifier.fillMaxWidth().testTag("input_split_friends")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = totalAmountStr.toDoubleOrNull()
                        val friendList = friendsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (title.isNotBlank() && amount != null && amount > 0) {
                            viewModel.createBillSplit(
                                title = title.trim(),
                                totalAmount = amount,
                                paidBy = paidByInput.trim().ifBlank { "You" },
                                friendNames = friendList
                            )
                            showAddDialog = false
                            Toast.makeText(context, "Bill split created!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter valid title and amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black)
                ) {
                    Text("Split Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
