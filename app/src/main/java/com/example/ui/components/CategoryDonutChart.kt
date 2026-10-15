package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryExpenseSummary
import com.example.ui.theme.LiquidGlassCard
import com.example.util.formatIndianCurrency
import java.util.Locale
import kotlin.math.atan2

@Composable
fun CategoryDonutChart(
    summaries: List<CategoryExpenseSummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    onCategorySelected: ((CategoryExpenseSummary?) -> Unit)? = null
) {
    if (summaries.isEmpty()) {
        LiquidGlassCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No category data available for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalSpent = summaries.sumOf { it.totalAmount }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(summaries) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Canvas
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(summaries) {
                            detectTapGestures { offset ->
                                val center = this.size.width / 2f
                                val dx = offset.x - center
                                val dy = offset.y - center
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                // Angle relative to startAngle = -90f
                                var relativeAngle = (angle - 270f) % 360f
                                if (relativeAngle < 0) relativeAngle += 360f

                                var cumulative = 0f
                                var tappedIdx: Int? = null
                                summaries.forEachIndexed { index, summary ->
                                    val sweep = (summary.totalAmount / totalSpent * 360f).toFloat()
                                    if (relativeAngle >= cumulative && relativeAngle <= cumulative + sweep) {
                                        tappedIdx = index
                                    }
                                    cumulative += sweep
                                }
                                selectedIndex = if (selectedIndex == tappedIdx) null else tappedIdx
                                onCategorySelected?.invoke(selectedIndex?.let { summaries[it] })
                            }
                        }
                ) {
                    val strokeWidth = 24.dp.toPx()
                    var startAngle = -90f

                    summaries.forEachIndexed { index, summary ->
                        val sweepAngle = (summary.totalAmount / totalSpent * 360f).toFloat() * animatedProgress.value
                        val color = CategoryIconHelper.parseColorHex(summary.category.colorHex)
                        val isSelected = selectedIndex == index
                        val width = if (isSelected) strokeWidth + 8f else strokeWidth

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f, // slight slice gap
                            useCenter = false,
                            style = Stroke(width = width, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center Info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val activeSummary = selectedIndex?.let { summaries.getOrNull(it) }
                    if (activeSummary != null) {
                        Text(
                            text = activeSummary.category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeSummary.totalAmount.formatIndianCurrency(currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", activeSummary.percentage)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = CategoryIconHelper.parseColorHex(activeSummary.category.colorHex)
                        )
                    } else {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = totalSpent.formatIndianCurrency(currencySymbol, compact = true),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${summaries.size} Categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend / Category list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                summaries.take(5).forEachIndexed { index, summary ->
                    val color = CategoryIconHelper.parseColorHex(summary.category.colorHex)
                    val isSelected = selectedIndex == index

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else Color.Transparent
                            )
                            .clickable {
                                selectedIndex = if (selectedIndex == index) null else index
                                onCategorySelected?.invoke(selectedIndex?.let { summaries[it] })
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = summary.category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = summary.totalAmount.formatIndianCurrency(currencySymbol),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${String.format(Locale.US, "%.1f", summary.percentage)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(42.dp)
                        )
                }
            }
        }
    }
}
