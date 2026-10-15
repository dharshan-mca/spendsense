package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.liquidGlassBorderBrush
import com.example.ui.theme.liquidGlassSurfaceBrush
import com.example.ui.viewmodel.ActiveScreen

data class NavItemData(
    val screen: ActiveScreen,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNavBar(
    activeScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val navItems = listOf(
        NavItemData(ActiveScreen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItemData(ActiveScreen.Analytics, "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_analytics"),
        NavItemData(ActiveScreen.History, "History", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, "nav_history"),
        NavItemData(ActiveScreen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    val surfaceBrush = liquidGlassSurfaceBrush(isDark)
    val borderBrush = liquidGlassBorderBrush(isDark)

    // Apple Floating Island Liquid Glass Dock
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(surfaceBrush)
                .border(
                    width = 1.2.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(32.dp)
                )
                .drawBehind {
                    // Refractive top sheen line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isDark) 0.35f else 0.6f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(24f, 1f),
                        end = Offset(size.width - 24f, 1f),
                        strokeWidth = 1.5f
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    // Center Floating Liquid Emerald Quick Add Button
                    if (index == 2) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    ambientColor = Emerald500.copy(alpha = 0.5f),
                                    spotColor = Emerald500.copy(alpha = 0.6f)
                                )
                                .clip(CircleShape)
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
                                            Color.White.copy(alpha = 0.7f),
                                            Color.White.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { onQuickAddClick() }
                                .testTag("fab_quick_add"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Quick Add Expense",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    val isSelected = activeScreen == item.screen
                    val activeBgColor = if (isDark) {
                        LiquidGlassTokens.RefractTeal.copy(alpha = 0.45f)
                    } else {
                        Emerald500.copy(alpha = 0.18f)
                    }

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) activeBgColor else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "nav_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "nav_content"
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                Emerald500.copy(alpha = 0.7f),
                                                Emerald500.copy(alpha = 0.15f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { onNavigate(item.screen) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag(item.testTag),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
