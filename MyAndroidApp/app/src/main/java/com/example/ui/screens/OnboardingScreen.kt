package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SpendSenseBadge
import com.example.ui.components.SpendSenseLogo
import com.example.ui.theme.Emerald500
import com.example.ui.viewmodel.MainViewModel

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    var selectedCurrency by remember { mutableStateOf("₹") }
    var budgetInput by remember { mutableStateOf("35000") }

    val steps = listOf(
        OnboardingStep(
            title = "Namaste! Welcome",
            subtitle = "Track your daily Swiggy, UPI, Kirana & bills effortlessly in ₹ with zero hassle.",
            icon = Icons.Default.Speed
        ),
        OnboardingStep(
            title = "Choose Currency",
            subtitle = "Select your preferred primary currency for daily logs and reports.",
            icon = Icons.Default.AccountBalanceWallet
        ),
        OnboardingStep(
            title = "Set Monthly Target",
            subtitle = "Define your monthly spending limit to keep your savings on track.",
            icon = Icons.Default.BarChart
        )
    )

    val currentStep = steps[stepIndex]

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpendSenseBadge(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SpendSense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald500
                    )
                }

                TextButton(
                    onClick = {
                        val current = viewModel.userSettings.value
                        viewModel.updateUserSettings(
                            (current ?: com.example.data.model.UserSettingsEntity()).copy(
                                isOnboardingCompleted = true
                            )
                        )
                        viewModel.setOnboardingCompleted(true)
                    },
                    modifier = Modifier.testTag("btn_skip_onboarding")
                ) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentStep.icon,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = currentStep.title,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentStep.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Dynamic Step Content
                when (stepIndex) {
                    1 -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("₹", "$", "€", "£").forEach { sym ->
                                val isSel = selectedCurrency == sym
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedCurrency = sym },
                                    label = { Text(sym, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = budgetInput,
                                onValueChange = { budgetInput = it },
                                label = { Text("Monthly Budget Limit") },
                                prefix = { Text("$selectedCurrency ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .testTag("input_onboarding_budget")
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("25000" to "₹25k", "35000" to "₹35k", "50000" to "₹50k", "75000" to "₹75k").forEach { (valStr, labelStr) ->
                                    AssistChip(
                                        onClick = { budgetInput = valStr },
                                        label = { Text(labelStr) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (budgetInput == valStr) Emerald500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation & Progress Dots
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == stepIndex) 28.dp else 10.dp, 10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == stepIndex) Emerald500
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (stepIndex < steps.lastIndex) {
                            stepIndex++
                        } else {
                            val budgetVal = budgetInput.toDoubleOrNull() ?: 30000.0
                            val current = viewModel.userSettings.value
                            viewModel.updateUserSettings(
                                (current ?: com.example.data.model.UserSettingsEntity()).copy(
                                    monthlyBudget = budgetVal,
                                    monthlyIncome = 0.0,
                                    currencySymbol = selectedCurrency,
                                    isOnboardingCompleted = true
                                )
                            )
                            viewModel.setOnboardingCompleted(true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_onboarding_next"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (stepIndex == steps.lastIndex) "GET STARTED" else "CONTINUE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (stepIndex == steps.lastIndex) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
