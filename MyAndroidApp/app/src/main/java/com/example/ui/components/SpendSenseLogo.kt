package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val GoldAccent = Color(0xFFE2B857)
val DarkTealStart = Color(0xFF063228)
val DarkTealEnd = Color(0xFF126853)

@Composable
fun SpendSenseBadge(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DarkTealStart, DarkTealEnd)
                )
            )
            .testTag("spendsense_logo_badge"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = "₹",
                color = GoldAccent,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "SpendSense Growth",
                tint = GoldAccent,
                modifier = Modifier.size((size.value * 0.38f).dp)
            )
        }
    }
}

@Composable
fun SpendSenseLogo(
    badgeSize: Dp = 72.dp,
    textSize: Int = 26,
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("spendsense_logo_container")
    ) {
        SpendSenseBadge(size = badgeSize)
        if (showText) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Spend",
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0E5443)
                )
                Text(
                    text = "Sense",
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0E5443)
                )
            }
        }
    }
}
