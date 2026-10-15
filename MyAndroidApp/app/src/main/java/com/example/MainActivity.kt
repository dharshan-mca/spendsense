package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppLockScreen
import com.example.ui.components.LowLatencyAiAssistantSheet
import com.example.ui.navigation.BottomNavBar
import com.example.ui.screens.*
import com.example.ui.theme.AmbientLiquidBackground
import com.example.ui.theme.Emerald500
import com.example.ui.theme.ExpanseTrackerTheme
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            val activeScreen by viewModel.activeScreen.collectAsState()
            val showAddSheet by viewModel.showAddExpenseSheet.collectAsState()
            val editingExpense by viewModel.editingExpense.collectAsState()
            val categories by viewModel.categories.collectAsState()

            val themeMode = userSettings?.themeMode ?: "DARK"
            val isOnboardingCompleted = userSettings?.isOnboardingCompleted ?: false

            var showSplashIntro by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1500)
                showSplashIntro = false
            }

            ExpanseTrackerTheme(themeMode = themeMode) {
                AnimatedContent(
                    targetState = showSplashIntro,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(350)) togetherWith
                            fadeOut(animationSpec = tween(350))
                    },
                    label = "SpendSenseSplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SpendSenseIntroSplash(onFinished = { showSplashIntro = false })
                    } else if (!isOnboardingCompleted || activeScreen is ActiveScreen.Onboarding) {
                        OnboardingScreen(viewModel = viewModel)
                    } else {
                        MainAppScaffold(
                            viewModel = viewModel,
                            activeScreen = activeScreen,
                            showAddSheet = showAddSheet,
                            editingExpense = editingExpense,
                            categories = categories,
                            currencySymbol = userSettings?.currencySymbol ?: "₹"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    activeScreen: ActiveScreen,
    showAddSheet: Boolean,
    editingExpense: com.example.data.model.ExpenseEntity?,
    categories: List<com.example.data.model.CategoryEntity>,
    currencySymbol: String
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val lastUpiToast by viewModel.lastDetectedUpiToast.collectAsState()

    LaunchedEffect(lastUpiToast) {
        lastUpiToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUpiToast()
        }
    }

    AmbientLiquidBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavBar(
                    activeScreen = activeScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    onQuickAddClick = { viewModel.openAddExpenseSheet() }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { target ->
                when (target) {
                    ActiveScreen.Home -> HomeScreen(viewModel = viewModel)
                    ActiveScreen.Analytics -> AnalyticsScreen(viewModel = viewModel)
                    ActiveScreen.History -> TransactionHistoryScreen(viewModel = viewModel)
                    ActiveScreen.Categories -> CategoryManagementScreen(viewModel = viewModel)
                    ActiveScreen.CategoryBudgets -> CategoryBudgetsScreen(viewModel = viewModel)
                    ActiveScreen.Subscriptions -> SubscriptionsScreen(viewModel = viewModel)
                    ActiveScreen.SplitBills -> SplitBillsScreen(viewModel = viewModel)
                    ActiveScreen.Reports -> ReportsScreen(viewModel = viewModel)
                    ActiveScreen.Settings -> SettingsScreen(viewModel = viewModel)
                    ActiveScreen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                }
            }
        }

        if (showAddSheet) {
            val presetPaymentMethod by viewModel.presetPaymentMethod.collectAsState()
            AddExpenseSheet(
                categories = categories,
                currencySymbol = currencySymbol,
                editingExpense = editingExpense,
                initialPaymentMethod = presetPaymentMethod,
                onDismiss = { viewModel.closeAddExpenseSheet() },
                onSave = { id, amount, categoryId, note, dateMillis, paymentMethod, type ->
                    viewModel.saveExpense(
                        id = id,
                        amount = amount,
                        categoryId = categoryId,
                        note = note,
                        dateMillis = dateMillis,
                        paymentMethod = paymentMethod,
                        type = type
                    )
                }
            )
        }

        val showAiSheet by viewModel.showAiAssistantSheet.collectAsState()
        val aiMessages by viewModel.aiMessages.collectAsState()
        val isAiLoading by viewModel.isAiLoading.collectAsState()

        if (showAiSheet) {
            LowLatencyAiAssistantSheet(
                messages = aiMessages,
                isLoading = isAiLoading,
                onSendMessage = { query -> viewModel.askAiQuestion(query) },
                onDismiss = { viewModel.closeAiAssistantSheet() }
            )
        }

        val isAppLocked by viewModel.isAppLocked.collectAsState()
        if (isAppLocked) {
            AppLockScreen(
                viewModel = viewModel,
                expectedPin = userSettings?.appLockPin
            )
        }
    }
}
}

@Composable
fun SpendSenseIntroSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val splashBg = remember { Color(0xFFF8F7F4) }
    val darkTeal = remember { Color(0xFF0F5F53) }
    val gold = remember { Color(0xFFFFB800) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(splashBg)
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .scale(pulseScale)
                    .background(darkTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = gold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SpendSense",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = darkTeal,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart UPI Finance & Expense Tracker",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = darkTeal.copy(alpha = 0.75f)
            )
        }
    }
}

