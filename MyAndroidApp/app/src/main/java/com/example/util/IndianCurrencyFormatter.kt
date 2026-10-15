package com.example.util

import java.text.NumberFormat
import java.util.Locale

object IndianCurrencyFormatter {

    /**
     * Formats a double value according to the Indian Numbering System.
     * E.g.
     * 1000 -> ₹1,000
     * 50000 -> ₹50,000
     * 150000 -> ₹1,50,000
     * 10000000 -> ₹1,00,00,000
     * 
     * If compact is true:
     * >= 1,00,00,000 (1 Crore) -> ₹1.5 Cr
     * >= 1,00,000 (1 Lakh) -> ₹2.5 L
     */
    fun format(amount: Double, symbol: String = "₹", compact: Boolean = false): String {
        if (compact) {
            val absAmount = Math.abs(amount)
            val sign = if (amount < 0) "-" else ""
            when {
                absAmount >= 10000000 -> { // 1 Crore
                    val cr = absAmount / 10000000.0
                    val formatted = if (cr % 1.0 == 0.0) String.format(Locale.US, "%.0f", cr) else String.format(Locale.US, "%.1f", cr)
                    return "$sign$symbol$formatted Cr"
                }
                absAmount >= 100000 -> { // 1 Lakh
                    val lakh = absAmount / 100000.0
                    val formatted = if (lakh % 1.0 == 0.0) String.format(Locale.US, "%.0f", lakh) else String.format(Locale.US, "%.1f", lakh)
                    return "$sign$symbol$formatted L"
                }
            }
        }

        return try {
            val indianLocale = Locale("en", "IN")
            val formatter = NumberFormat.getNumberInstance(indianLocale)
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            val formattedNum = formatter.format(amount)
            "$symbol$formattedNum"
        } catch (e: Exception) {
            val fallbackNum = String.format(Locale.US, "%,.0f", amount)
            "$symbol$fallbackNum"
        }
    }
}

fun Double.formatIndianCurrency(symbol: String = "₹", compact: Boolean = false): String {
    return IndianCurrencyFormatter.format(this, symbol, compact)
}
