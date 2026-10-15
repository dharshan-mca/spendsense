package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.Emerald500
import com.example.ui.theme.VividCoral
import com.example.util.formatIndianCurrency
import java.util.Locale

@Composable
fun BudgetProgressRing(
    spentAmount: Double,
    budgetAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 18.dp
) {
    val percentage = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).coerceAtLeast(0.0) else 0.0
    val progressFraction = (percentage / 100.0).coerceIn(0.0, 1.0).toFloat()

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(spentAmount, budgetAmount) {
        animatedProgress.animateTo(
            targetValue = progressFraction,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val isOverBudget = spentAmount > budgetAmount && budgetAmount > 0
    val ringColorStart = if (isOverBudget) VividCoral else Emerald500
    val ringColorEnd = if (isOverBudget) Color(0xFFFF8888) else ElectricLime
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val radius = (this.size.minDimension - strokePx) / 2

            // Background Track Arc
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc with Gradient
            if (animatedProgress.value > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ringColorStart, ringColorEnd, ringColorStart)
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Typography Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TOTAL SPENT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = spentAmount.formatIndianCurrency(currencySymbol),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isOverBudget) VividCoral else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            val remaining = (budgetAmount - spentAmount)
            Text(
                text = if (isOverBudget) {
                    "Over budget by ${(-remaining).formatIndianCurrency(currencySymbol)}"
                } else {
                    "${remaining.formatIndianCurrency(currencySymbol)} left of ${budgetAmount.formatIndianCurrency(currencySymbol)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOverBudget) VividCoral else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${String.format(Locale.US, "%.1f", percentage)}% used",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
