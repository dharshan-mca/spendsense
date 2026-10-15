package com.example.util

data class ParsedUpiTransaction(
    val amount: Double,
    val merchant: String,
    val suggestedCategoryId: Long,
    val categoryName: String,
    val sourceApp: String,
    val rawText: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

object UpiNotificationParser {

    private val upiPackageMap = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM",
        "com.dreamplug.androidapp" to "CRED",
        "com.whatsapp" to "WhatsApp Pay",
        "com.axis.mobile" to "Axis Mobile",
        "com.msf.kbank.mobile" to "Kotak Bank",
        "com.icicibank.mobile" to "iMobile",
        "com.sbi.lotusintouch" to "YONO SBI",
        "com.snapwork.hdfc" to "HDFC MobileBanking"
    )

    // Regex patterns for amounts in Indian UPI format (e.g. ₹150, Rs 250.50, INR 499)
    private val amountRegex = Regex("""(?i)(?:₹|rs\.?|inr)\s*([\d,]+(?:\.\d{1,2})?)""")
    private val paidAmountRegex = Regex("""(?i)(?:paid|sent|debited|spent|transferred)\s*(?:₹|rs\.?|inr)?\s*([\d,]+(?:\.\d{1,2})?)""")

    // Regex patterns for merchant / payee
    private val merchantToRegex = Regex("""(?i)(?:to|at)\s+([A-Za-z0-9\s&'.-]{2,35}?)(?:\s+(?:using|via|on|from|ref|upi|is\s+successful|\.|$))""")
    private val paymentForRegex = Regex("""(?i)(?:payment\s+for|order\s+at)\s+([A-Za-z0-9\s&'.-]{2,35}?)(?:\s+(?:is|using|via|\.|$))""")

    fun isUpiApp(packageName: String): Boolean {
        return upiPackageMap.containsKey(packageName)
    }

    fun parse(packageName: String, title: String?, text: String?): ParsedUpiTransaction? {
        val combined = "${title.orEmpty()} ${text.orEmpty()}".trim()
        if (combined.isBlank()) return null

        val isKnownUpiApp = isUpiApp(packageName)
        val hasPaymentKeywords = combined.contains("paid", ignoreCase = true) ||
                combined.contains("debited", ignoreCase = true) ||
                combined.contains("sent", ignoreCase = true) ||
                combined.contains("payment of", ignoreCase = true) ||
                combined.contains("transferred", ignoreCase = true) ||
                combined.contains("upi", ignoreCase = true)

        if (!isKnownUpiApp && !hasPaymentKeywords) {
            return null
        }

        // 1. Extract Amount
        var amount: Double? = null
        val amountMatch = amountRegex.find(combined)
        if (amountMatch != null) {
            val rawNum = amountMatch.groupValues[1].replace(",", "")
            amount = rawNum.toDoubleOrNull()
        }

        if (amount == null) {
            val paidMatch = paidAmountRegex.find(combined)
            if (paidMatch != null) {
                val rawNum = paidMatch.groupValues[1].replace(",", "")
                amount = rawNum.toDoubleOrNull()
            }
        }

        if (amount == null || amount <= 0.0) {
            return null
        }

        // 2. Extract Merchant / Payee
        var merchant = ""
        val merchantMatch = merchantToRegex.find(combined)
        if (merchantMatch != null) {
            merchant = merchantMatch.groupValues[1].trim()
        }

        if (merchant.isBlank()) {
            val forMatch = paymentForRegex.find(combined)
            if (forMatch != null) {
                merchant = forMatch.groupValues[1].trim()
            }
        }

        if (merchant.isBlank()) {
            merchant = if (title.isNullOrBlank() || title.contains("Pay", ignoreCase = true) || title.contains("Payment", ignoreCase = true)) {
                "UPI Merchant"
            } else {
                title.trim()
            }
        }

        // Clean merchant name from trailing prepositions
        merchant = merchant.replace(Regex("""(?i)\s+(using|via|on|from|upi).*$"""), "").trim()
        if (merchant.length > 30) {
            merchant = merchant.take(30).trim()
        }

        // 3. Category Categorization
        val (categoryId, categoryName) = detectCategory(merchant, combined)

        val appName = upiPackageMap[packageName] ?: if (isKnownUpiApp) "UPI App" else "Bank SMS/Alert"

        return ParsedUpiTransaction(
            amount = amount,
            merchant = merchant,
            suggestedCategoryId = categoryId,
            categoryName = categoryName,
            sourceApp = appName,
            rawText = combined
        )
    }

    private fun detectCategory(merchant: String, fullText: String): Pair<Long, String> {
        val target = "$merchant $fullText".lowercase()

        return when {
            listOf("swiggy", "zomato", "mcdonald", "kfc", "domino", "pizza", "burger", "starbucks", "cafe", "restaurant", "chai", "coffee", "dhaba", "bakery", "food", "eatclub", "subway").any { target.contains(it) } ->
                1L to "Food & Dining"

            listOf("uber", "ola", "rapido", "metro", "fuel", "petrol", "diesel", "shell", "hp pay", "indian oil", "bpcl", "fastag", "parking", "auto", "rail", "irctc", "yulu").any { target.contains(it) } ->
                2L to "Transport & Auto"

            listOf("blinkit", "zepto", "instamart", "bigbasket", "kirana", "supermarket", "dmart", "provision", "vegetable", "fruit", "milk", "dairy", "grocery", "mart", "nature basket").any { target.contains(it) } ->
                3L to "Kirana & Groceries"

            listOf("jio", "airtel", "vi ", "vodafone", "electricity", "bescom", "bill", "recharge", "gas", "broadband", "water", "tata play", "dish tv", "dth", "indane", "hp gas").any { target.contains(it) } ->
                4L to "Bills & Recharge"

            listOf("amazon", "flipkart", "myntra", "zara", "h&m", "ajio", "shopping", "mall", "nykaa", "croma", "reliance digital", "clothing", "shoes", "fashion", "lifestyle", "meesho").any { target.contains(it) } ->
                5L to "Shopping & Malls"

            listOf("apollo", "pharmeasy", "1mg", "hospital", "clinic", "doctor", "medical", "medicine", "pharmacy", "netmeds", "dentist", "lab", "diagnostics").any { target.contains(it) } ->
                6L to "Health & Pharmacy"

            listOf("rent", "society", "maintenance", "maid", "flat", "nobroker", "mygate").any { target.contains(it) } ->
                7L to "House Rent & Maid"

            listOf("netflix", "prime", "hotstar", "spotify", "bookmyshow", "pvr", "inox", "cinema", "movie", "steam", "youtube", "gaming", "playstation").any { target.contains(it) } ->
                8L to "Entertainment & OTT"

            else -> 10L to "Others & Cash"
        }
    }
}
