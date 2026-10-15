package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlySpendTrend
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.Emerald500
import com.example.ui.theme.VividCoral
import com.example.util.formatIndianCurrency
import java.util.Locale

@Composable
fun MonthlyTrendBarChart(
    trends: List<MonthlySpendTrend>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    onMonthTrendSelected: ((MonthlySpendTrend) -> Unit)? = null
) {
    if (trends.isEmpty()) return

    val maxSpend = (trends.maxOfOrNull { it.totalSpend } ?: 100.0).coerceAtLeast(1.0)
    var selectedIndex by remember { mutableStateOf(trends.lastIndex) }

    val animatedHeightFraction = remember { Animatable(0f) }
    LaunchedEffect(trends) {
        animatedHeightFraction.snapTo(0f)
        animatedHeightFraction.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Trend (Last 6 Months)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any bar to view details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val activeTrend = trends.getOrNull(selectedIndex)
                if (activeTrend != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = activeTrend.totalSpend.formatIndianCurrency(currencySymbol, compact = true),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bars Area
            val barCount = trends.size
            val primaryColor = MaterialTheme.colorScheme.primary
            val selectedBarColor = ElectricLime
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = (canvasWidth / (barCount * 1.6f)).coerceIn(16f, 36f)
                    val step = canvasWidth / barCount

                    trends.forEachIndexed { index, trend ->
                        val x = step * index + (step - barWidth) / 2f
                        val heightRatio = (trend.totalSpend / maxSpend).toFloat()
                        val barHeight = (canvasHeight * heightRatio * animatedHeightFraction.value).coerceAtLeast(8f)
                        val y = canvasHeight - barHeight

                        val isSelected = index == selectedIndex
                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(ElectricLime, Emerald500)
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                            )
                        }

                        // Track Background bar
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, canvasHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )

                        // Filled Bar
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }
                }

                // Invisible Touch Overlay for selection
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trends.forEachIndexed { index, trend ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    selectedIndex = index
                                    onMonthTrendSelected?.invoke(trend)
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Labels Below
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEachIndexed { index, trend ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = trend.monthYearLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
