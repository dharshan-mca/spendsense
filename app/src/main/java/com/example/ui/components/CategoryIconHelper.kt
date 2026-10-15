package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "directions_car", "car", "transport" -> Icons.Default.DirectionsCar
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "receipt", "bills" -> Icons.Default.Receipt
            "movie", "entertainment" -> Icons.Default.Movie
            "medical_services", "health" -> Icons.Default.MedicalServices
            "home", "rent" -> Icons.Default.Home
            "subscriptions", "tv" -> Icons.Default.Subscriptions
            "payments", "salary", "income" -> Icons.Default.Payments
            "coffee", "cafe" -> Icons.Default.LocalCafe
            "fitness_center", "gym" -> Icons.Default.FitnessCenter
            "flight", "travel" -> Icons.Default.Flight
            "pets" -> Icons.Default.Pets
            "school", "education" -> Icons.Default.School
            "work", "business" -> Icons.Default.Work
            "sports_esports", "gaming" -> Icons.Default.SportsEsports
            "phone", "mobile" -> Icons.Default.Smartphone
            "build", "maintenance" -> Icons.Default.Build
            else -> Icons.Default.MoreHoriz
        }
    }

    fun parseColorHex(hex: String, fallback: Color = Color(0xFF10B981)): Color {
        return try {
            val cleaned = hex.removePrefix("#")
            val colorInt = cleaned.toLong(16)
            if (cleaned.length == 6) {
                Color(0xFF000000 or colorInt)
            } else if (cleaned.length == 8) {
                Color(colorInt)
            } else {
                fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    val availableCategoryIcons = listOf(
        "restaurant", "directions_car", "shopping_bag", "receipt",
        "movie", "medical_services", "home", "subscriptions",
        "payments", "coffee", "fitness_center", "flight",
        "pets", "school", "work", "gaming", "phone", "build"
    )

    val availableColorHexes = listOf(
        "#10B981", "#3B82F6", "#EC4899", "#F59E0B",
        "#8B5CF6", "#EF4444", "#6366F1", "#06B6D4",
        "#84CC16", "#14B8A6", "#F97316", "#A855F7",
        "#64748B", "#E11D48"
    )
}
