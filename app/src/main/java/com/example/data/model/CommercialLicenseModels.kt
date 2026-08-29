package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LicenseStatus(val label: String, val isUnlocked: Boolean) {
    ACTIVE_LIFETIME("Lifetime Active (Paid ₹5,000)", true),
    TRIAL_ACTIVE("Trial Sandbox Mode", true),
    PENDING_VERIFICATION("Pending UPI Confirmation", false),
    EXPIRED("License Expired / Inactive", false)
}

enum class LicenseTier(
    val title: String,
    val priceInr: Double,
    val description: String,
    val maxBrokerGateways: Int,
    val benefits: List<String>
) {
    LIFETIME_TRADER(
        title = "zx26 Institutional Lifetime License",
        priceInr = 5000.0,
        description = "Full commercial access to 18+ broker failover hubs, AI Copilot, low-latency DMA routing, and multi-asset option workstations.",
        maxBrokerGateways = 18,
        benefits = listOf(
            "Lifetime unlimited algorithmic order dispatch across 18+ brokers",
            "Zero monthly subscription fees (One-time ₹5,000 payment)",
            "Instant 1-Tap Emergency Downtime Failover routing",
            "Institutional Level-5 Superadmin Control Panel access",
            "Gemini 2.5 Pro Neural Trading Copilot & Strategy Builder",
            "Complete F&O Option Chain, Greeks Matrix & Payoff Lab",
            "Sub-millisecond Direct Market Access (DMA) low latency",
            "Commercial resale & whitelabel distribution clearance"
        )
    )
}

data class CommercialLicense(
    val licenseKey: String = "ZX26-PRO8-941A-5000",
    val clientName: String = "Subhas Saha (Master Owner)",
    val tier: LicenseTier = LicenseTier.LIFETIME_TRADER,
    val status: LicenseStatus = LicenseStatus.ACTIVE_LIFETIME,
    val issuedDate: String = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
    val activationDate: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date()),
    val paymentReference: String = "UPI-ZX26-5000-REF9921",
    val paymentUpiId: String = "sahasubhas597@okaxis",
    val amountPaidInr: Double = 5000.0,
    val activeDevicesCount: Int = 3,
    val maxDevicesAllowed: Int = 10,
    val checksumHash: String = "SHA256-ZX26-AUTH-941A-VERIFIED"
)

data class CustomerLicenseRecord(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val licenseKey: String,
    val amountPaidInr: Double = 5000.0,
    val paymentMode: String = "UPI / QR",
    val transactionRef: String,
    val issueDate: String,
    val smsStatus: String = "Delivered",
    val emailStatus: String = "Delivered",
    val isActive: Boolean = true
)

object LicenseGeneratorUtil {
    fun generateLicenseKey(customerName: String = "CLIENT"): String {
        val segment1 = "ZX26"
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val segment2 = (1..4).map { chars.random() }.joinToString("")
        val segment3 = (1..4).map { chars.random() }.joinToString("")
        val segment4 = "5000"
        return "$segment1-$segment2-$segment3-$segment4"
    }

    fun buildUpiPaymentUri(
        payeeUpiId: String = "sahasubhas597@okaxis",
        payeeName: String = "zx26 Algo Trading",
        amount: Double = 5000.0,
        transactionNote: String = "zx26 Lifetime Trader License"
    ): String {
        val encodedName = payeeName.replace(" ", "%20")
        val encodedNote = transactionNote.replace(" ", "%20")
        return "upi://pay?pa=$payeeUpiId&pn=$encodedName&am=${amount.toInt()}&cu=INR&tn=$encodedNote"
    }

    fun isValidKey(key: String): Boolean {
        val clean = key.trim().uppercase()
        return clean.startsWith("ZX26-") && clean.length >= 16
    }

    fun buildLicenseMessage(
        customerName: String,
        licenseKey: String,
        amount: Double = 5000.0,
        appName: String = "zx26 Institutional Algo Terminal"
    ): String {
        return """
            🎉 Congratulations $customerName!
            Your lifetime commercial license for $appName has been activated.
            
            🔑 16-Digit License Key: $licenseKey
            💰 Amount Paid: ₹${amount.toInt()} INR (Zero Recurring Fees)
            ⚡ Supported Brokers: 18+ Direct Gateways & Failover Hub
            🚀 Status: Verified Lifetime Active
            
            How to Activate:
            1. Open the zx26 app
            2. Go to 'License ₹5k' tab -> 'Key Activation'
            3. Enter your 16-digit key: $licenseKey
            4. Enjoy unlimited automated algorithmic trading!
        """.trimIndent()
    }
}
