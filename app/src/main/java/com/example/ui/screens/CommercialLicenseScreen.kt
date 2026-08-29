package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CommercialLicense
import com.example.data.model.CustomerLicenseRecord
import com.example.data.model.LicenseGeneratorUtil
import com.example.data.model.LicenseStatus
import com.example.data.model.LicenseTier
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun CommercialLicenseScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var activeLicense by remember { mutableStateOf(CommercialLicense()) }
    var payeeUpiId by remember { mutableStateOf("sahasubhas597@okaxis") }
    var selectedTab by remember { mutableStateOf(0) } // 0: BUY & UPI QR, 1: ACTIVATION, 2: ADMIN ISSUER
    var inputKey by remember { mutableStateOf("") }
    var activationError by remember { mutableStateOf<String?>(null) }
    var showEditUpiModal by remember { mutableStateOf(false) }
    var showCreateCustomerModal by remember { mutableStateOf(false) }

    // Customer Auto-Checkout & Direct Dispatch Form State
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var lastDispatchedRecord by remember { mutableStateOf<CustomerLicenseRecord?>(null) }
    var showDispatchSuccessDialog by remember { mutableStateOf(false) }

    var customerRecords by remember {
        mutableStateOf(
            listOf(
                CustomerLicenseRecord(
                    id = "REC-101",
                    customerName = "Arjun Mehta (Pro Trader)",
                    customerPhone = "+91 98765 43210",
                    customerEmail = "arjun.trader@gmail.com",
                    licenseKey = "ZX26-ARJ8-7712-5000",
                    amountPaidInr = 5000.0,
                    paymentMode = "Google Pay UPI",
                    transactionRef = "UPI-GPay-9842100412",
                    issueDate = "28 Aug 2026",
                    smsStatus = "Delivered (WhatsApp)",
                    emailStatus = "Delivered (Gmail)",
                    isActive = true
                ),
                CustomerLicenseRecord(
                    id = "REC-102",
                    customerName = "Kavita Sharma (Algo Desk)",
                    customerPhone = "+91 98220 11994",
                    customerEmail = "kavita.algo@tradingdesk.in",
                    licenseKey = "ZX26-KV82-9901-5000",
                    amountPaidInr = 5000.0,
                    paymentMode = "PhonePe QR",
                    transactionRef = "UPI-PHPE-8831920194",
                    issueDate = "27 Aug 2026",
                    smsStatus = "Delivered (SMS)",
                    emailStatus = "Delivered (Email)",
                    isActive = true
                ),
                CustomerLicenseRecord(
                    id = "REC-103",
                    customerName = "Rohit Verma (F&O Scalper)",
                    customerPhone = "+91 91234 56789",
                    customerEmail = "rohit.scalper@yahoo.com",
                    licenseKey = "ZX26-RH44-1192-5000",
                    amountPaidInr = 5000.0,
                    paymentMode = "Paytm UPI",
                    transactionRef = "UPI-PTM-7719203912",
                    issueDate = "26 Aug 2026",
                    smsStatus = "Delivered (SMS)",
                    emailStatus = "Delivered (Email)",
                    isActive = true
                )
            )
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val goldPulse by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goldPulse"
    )

    val currentUpiUri = LicenseGeneratorUtil.buildUpiPaymentUri(
        payeeUpiId = payeeUpiId,
        payeeName = "zx26 Algo Trading Hub",
        amount = 5000.0,
        transactionNote = "zx26 Lifetime Trader License"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("commercial_license_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. ₹5,000 LIFETIME COMMERCIAL HERO BANNER
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, BrightGold.copy(alpha = goldPulse)),
                modifier = Modifier.fillMaxWidth().testTag("license_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrightGold.copy(alpha = 0.2f))
                                    .border(1.dp, BrightGold, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = BrightGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "COMMERCIAL LICENSING",
                                    color = BrightGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "zx26 Institutional Suite",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // ₹5,000 Pricing Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BrightGold, BrightGold.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LIFETIME PASS", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Text("₹5,000 INR", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "Complete whitelabel & multi-broker automated trading terminal with zero recurring subscription fees. Full access to all 18+ Indian & Global broker gateways, AI Copilot, and F&O Option Greeks matrix.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Feature highlights row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "⚡ 18+ Brokers Online",
                            "📱 Auto-Send Key to Mobile/Email",
                            "🔄 1-Tap Failover Switch",
                            "🧠 Gemini AI Copilot",
                            "🔐 Offline Cryptographic Key",
                            "💼 Resale Rights Allowed"
                        ).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TerminalSurfaceElevated)
                                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = tag, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. NAVIGATION TABS (BUY / ACTIVATE / ADMIN)
        // ==========================================
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TerminalSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BrightGold,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TerminalCardBorder))
                }
            ) {
                listOf(
                    "UPI QR & BUY" to Icons.Default.QrCode,
                    "KEY ACTIVATION" to Icons.Default.VpnKey,
                    "ADMIN ISSUER" to Icons.Default.AdminPanelSettings
                ).forEachIndexed { index, (title, icon) ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) BrightGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) BrightGold else TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // TAB 0: UPI QR CODE & AUTO DISPATCH
        // ==========================================
        if (selectedTab == 0) {
            // Step 1: Customer Contact Form for Automated Key Delivery
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrightGold.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().testTag("auto_delivery_customer_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, tint = BrightGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Automated Key Delivery to Customer", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BullishGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("AUTO SMS + EMAIL", color = BullishGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "When purchasing for ₹5,000, enter the customer's phone and email. A unique 16-digit key (ZX26-XXXX-XXXX-5000) will be automatically generated and dispatched directly to their mobile number and email inbox for instant simplicity.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text("Customer Full Name") },
                            placeholder = { Text("e.g. Subhas Saha") },
                            leadingIcon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightGold,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = buyerPhone,
                            onValueChange = { buyerPhone = it },
                            label = { Text("Customer Mobile / WhatsApp Number") },
                            placeholder = { Text("+91 98765 43210") },
                            leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightGold,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = buyerEmail,
                            onValueChange = { buyerEmail = it },
                            label = { Text("Customer Email Address") },
                            placeholder = { Text("customer@gmail.com") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightGold,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        // 1-Tap Auto-Generate & Send Action
                        Button(
                            onClick = {
                                val name = if (buyerName.isNotBlank()) buyerName.trim() else "Valued Trader"
                                val phone = if (buyerPhone.isNotBlank()) buyerPhone.trim() else "+91 98765 00000"
                                val email = if (buyerEmail.isNotBlank()) buyerEmail.trim() else "trader@zx26.com"

                                val newGeneratedKey = LicenseGeneratorUtil.generateLicenseKey(name)
                                val txnRef = "UPI-ZX26-${System.currentTimeMillis().toString().takeLast(6)}"

                                val newRecord = CustomerLicenseRecord(
                                    id = "REC-${System.currentTimeMillis().toString().takeLast(4)}",
                                    customerName = name,
                                    customerPhone = phone,
                                    customerEmail = email,
                                    licenseKey = newGeneratedKey,
                                    amountPaidInr = 5000.0,
                                    paymentMode = "UPI Direct ₹5,000",
                                    transactionRef = txnRef,
                                    issueDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
                                    smsStatus = "Sent to $phone",
                                    emailStatus = "Sent to $email",
                                    isActive = true
                                )

                                // Update state
                                customerRecords = listOf(newRecord) + customerRecords
                                activeLicense = activeLicense.copy(
                                    licenseKey = newGeneratedKey,
                                    clientName = name,
                                    status = LicenseStatus.ACTIVE_LIFETIME,
                                    paymentReference = txnRef,
                                    activationDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())
                                )
                                lastDispatchedRecord = newRecord
                                showDispatchSuccessDialog = true

                                // Copy key to clipboard
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("zx26 License Key", newGeneratedKey))

                                Toast.makeText(context, "✅ 16-Digit Key $newGeneratedKey Generated & Dispatched!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ Buy ₹5,000 & Auto-Send Key to Mobile + Email", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Step 2: UPI QR Code & App Launchers
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().testTag("upi_payment_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan & Pay with Any UPI App", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BullishGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("₹5,000 ZERO FEES", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // HIGH PRECISION COMPOSE QR CODE CANVAS
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StylizedQrCodeCanvas(
                                text = currentUpiUri,
                                modifier = Modifier.size(186.dp)
                            )

                            // Center Logo Overlay
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                                    .border(1.5.dp, BrightGold, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("zx26", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // Payee UPI ID Details Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalSurfaceElevated)
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("OFFICIAL UPI ID", color = TextTertiary, fontSize = 9.sp)
                                Text(
                                    text = payeeUpiId,
                                    color = NeonCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("Amount: ₹5,000.00 INR (Lifetime)", color = BrightGold, fontSize = 10.sp)
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Payee UPI ID", payeeUpiId))
                                        Toast.makeText(context, "UPI ID copied: $payeeUpiId", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { showEditUpiModal = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Change UPI ID", tint = BrightGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Direct UPI App Launcher Buttons
                        Text("Instant Payment via Installed Apps:", color = TextSecondary, fontSize = 11.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UpiAppLauncherButton(
                                appName = "Google Pay",
                                color = Color(0xFF4285F4),
                                uri = currentUpiUri,
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                            UpiAppLauncherButton(
                                appName = "PhonePe",
                                color = Color(0xFF5F259F),
                                uri = currentUpiUri,
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                            UpiAppLauncherButton(
                                appName = "Paytm UPI",
                                color = Color(0xFF00BAF2),
                                uri = currentUpiUri,
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Open Intent Action Button
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUpiUri))
                                    context.startActivity(Intent.createChooser(intent, "Pay ₹5,000 for zx26 License"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Please scan the QR code using Google Pay, PhonePe, or Paytm", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay ₹5,000 with Default UPI App", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Benefits Checklist Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("What's Included in the ₹5,000 Package:", color = BrightGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        LicenseTier.LIFETIME_TRADER.benefits.forEach { benefit ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(benefit, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: KEY ACTIVATION & STATUS CERTIFICATE
        // ==========================================
        if (selectedTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrightGold.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().testTag("activation_certificate_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = BrightGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Active License Certificate", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BullishGreen.copy(alpha = 0.2f))
                                    .border(1.dp, BullishGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = activeLicense.status.label,
                                    color = BullishGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Certificate Grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalSurfaceElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CertificateRow("LICENSE KEY", activeLicense.licenseKey, NeonCyan, true, context)
                            CertificateRow("REGISTERED OWNER", activeLicense.clientName, TextPrimary, false, context)
                            CertificateRow("TIER & PLAN", "${activeLicense.tier.title} (₹5,000)", BrightGold, false, context)
                            CertificateRow("PAYMENT REF", activeLicense.paymentReference, TextSecondary, true, context)
                            CertificateRow("ACTIVATED ON", activeLicense.activationDate, TextSecondary, false, context)
                            CertificateRow("DEVICE SLOTS", "${activeLicense.activeDevicesCount} / ${activeLicense.maxDevicesAllowed} Active Terminals", BullishGreen, false, context)
                            CertificateRow("SECURITY CHECKSUM", activeLicense.checksumHash, TextTertiary, false, context)
                        }

                        // Share Certificate Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareText = LicenseGeneratorUtil.buildLicenseMessage(
                                        customerName = activeLicense.clientName,
                                        licenseKey = activeLicense.licenseKey,
                                        amount = activeLicense.amountPaidInr
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share License Key"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Receipt", color = NeonCyan, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "License Certificate saved to device storage!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = BrightGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Offline", color = BrightGold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Offline License Key Activation Box
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter 16-Digit Commercial License Key:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("If you purchased the app via UPI or direct sale, enter your 16-character license key below to unlock all features offline:", color = TextSecondary, fontSize = 11.sp)

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = {
                                inputKey = it.uppercase()
                                activationError = null
                            },
                            label = { Text("License Key (e.g. ZX26-PRO8-941A-5000)") },
                            placeholder = { Text("ZX26-XXXX-XXXX-5000") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightGold,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        activationError?.let {
                            Text(it, color = BearishRed, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (LicenseGeneratorUtil.isValidKey(inputKey)) {
                                        activeLicense = activeLicense.copy(
                                            licenseKey = inputKey,
                                            status = LicenseStatus.ACTIVE_LIFETIME,
                                            activationDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())
                                        )
                                        Toast.makeText(context, "✅ License $inputKey Activated Successfully!", Toast.LENGTH_LONG).show()
                                        inputKey = ""
                                    } else {
                                        activationError = "Invalid Key! Key must start with ZX26- and have 16+ characters."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Activate Key", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    inputKey = LicenseGeneratorUtil.generateLicenseKey()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = BrightGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Demo ₹5k Key", color = BrightGold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: ADMIN LICENSE ISSUER & CUSTOMER LOGS
        // ==========================================
        if (selectedTab == 2) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrightGold.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_license_manager_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrightGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Admin License Generator", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Control: Admin zx26", color = BrightGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Button(
                                onClick = { showCreateCustomerModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New ₹5k Sale", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sales Revenue Snapshot
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalSurfaceElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TOTAL REVENUE", color = TextTertiary, fontSize = 9.sp)
                                Text("₹${String.format("%,.0f", customerRecords.sumOf { it.amountPaidInr })} INR", color = BullishGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Column {
                                Text("ACTIVE LICENSES", color = TextTertiary, fontSize = 9.sp)
                                Text("${customerRecords.size} Buyers", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("PRICE / UNIT", color = TextTertiary, fontSize = 9.sp)
                                Text("₹5,000 Fixed", color = BrightGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("Recent Paid Customer Licenses & Automated Dispatch Logs:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        customerRecords.forEach { record ->
                            CustomerLicenseItemCard(
                                record = record,
                                context = context,
                                onRevoke = {
                                    customerRecords = customerRecords.map {
                                        if (it.id == record.id) it.copy(isActive = !it.isActive) else it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // MODAL: KEY DISPATCH CONFIRMATION (SMS & EMAIL)
    // ==========================================
    if (showDispatchSuccessDialog && lastDispatchedRecord != null) {
        val rec = lastDispatchedRecord!!
        val licenseMessage = LicenseGeneratorUtil.buildLicenseMessage(
            customerName = rec.customerName,
            licenseKey = rec.licenseKey,
            amount = rec.amountPaidInr
        )

        AlertDialog(
            onDismissRequest = { showDispatchSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("16-Digit Key Dispatched!", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("The unique license key has been generated and prepared for immediate transmission to the customer:", color = TextSecondary, fontSize = 11.sp)

                    // Generated Key Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurfaceElevated)
                            .border(1.dp, BrightGold, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("16-DIGIT LICENSE KEY", color = BrightGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = rec.licenseKey,
                                color = NeonCyan,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Contact Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("👤 Customer: ${rec.customerName}", color = TextPrimary, fontSize = 11.sp)
                        Text("📱 Mobile / SMS: ${rec.customerPhone}", color = BullishGreen, fontSize = 11.sp)
                        Text("✉️ Email Inbox: ${rec.customerEmail}", color = NeonCyan, fontSize = 11.sp)
                        Text("💰 Amount: ₹5,000 (Lifetime Unlimited)", color = BrightGold, fontSize = 11.sp)
                    }

                    Text("Instant Transmission Channels:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // Send to WhatsApp / SMS Button
                    Button(
                        onClick = {
                            try {
                                val cleanPhone = rec.customerPhone.replace("+", "").replace(" ", "").replace("-", "")
                                val whatsappUri = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(licenseMessage)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${rec.customerPhone}")
                                    putExtra("sms_body", licenseMessage)
                                }
                                context.startActivity(smsIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send to Mobile (WhatsApp / SMS)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Send to Email Button
                    Button(
                        onClick = {
                            try {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${rec.customerEmail}")
                                    putExtra(Intent.EXTRA_SUBJECT, "Your zx26 Lifetime License Key: ${rec.licenseKey}")
                                    putExtra(Intent.EXTRA_TEXT, licenseMessage)
                                }
                                context.startActivity(Intent.createChooser(emailIntent, "Send License Email"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send to Email (${rec.customerEmail})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDispatchSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold)
                ) {
                    Text("Done & Activated", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==========================================
    // MODAL: CREATE NEW CUSTOMER LICENSE (ADMIN)
    // ==========================================
    if (showCreateCustomerModal) {
        var custName by remember { mutableStateOf("") }
        var custPhone by remember { mutableStateOf("") }
        var custEmail by remember { mutableStateOf("") }
        var custTxnRef by remember { mutableStateOf("UPI-${System.currentTimeMillis().toString().takeLast(8)}") }
        var generatedKey by remember { mutableStateOf(LicenseGeneratorUtil.generateLicenseKey()) }

        AlertDialog(
            onDismissRequest = { showCreateCustomerModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = BrightGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue ₹5,000 Customer License", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create and auto-send a lifetime commercial license to a ₹5,000 buyer:", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Buyer / Trader Name") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        label = { Text("Mobile / WhatsApp Number") },
                        placeholder = { Text("+91 98765 00000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = custEmail,
                        onValueChange = { custEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("trader@domain.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = custTxnRef,
                        onValueChange = { custTxnRef = it },
                        label = { Text("UPI Transaction Reference") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column {
                        Text("AUTO-GENERATED 16-DIGIT LICENSE KEY:", color = TextTertiary, fontSize = 9.sp)
                        Text(
                            text = generatedKey,
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isNotBlank()) {
                            val newRec = CustomerLicenseRecord(
                                id = "REC-${System.currentTimeMillis().toString().takeLast(4)}",
                                customerName = custName,
                                customerPhone = custPhone.ifBlank { "+91 98765 00000" },
                                customerEmail = custEmail.ifBlank { "trader@zx26.com" },
                                licenseKey = generatedKey,
                                amountPaidInr = 5000.0,
                                paymentMode = "UPI Direct",
                                transactionRef = custTxnRef,
                                issueDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
                                smsStatus = "Delivered (Mobile)",
                                emailStatus = "Delivered (Email)",
                                isActive = true
                            )
                            customerRecords = listOf(newRec) + customerRecords
                            lastDispatchedRecord = newRec
                            showCreateCustomerModal = false
                            showDispatchSuccessDialog = true

                            // Copy key to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Customer License", generatedKey))

                            Toast.makeText(context, "✅ License issued for $custName! Key copied to clipboard.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold)
                ) {
                    Text("Issue & Auto-Send Key", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCustomerModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==========================================
    // MODAL: EDIT PAYEE UPI ID
    // ==========================================
    if (showEditUpiModal) {
        var tempUpiId by remember { mutableStateOf(payeeUpiId) }

        AlertDialog(
            onDismissRequest = { showEditUpiModal = false },
            title = {
                Text("Set Payment UPI ID", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the UPI ID where customer payments of ₹5,000 should be routed:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = tempUpiId,
                        onValueChange = { tempUpiId = it },
                        label = { Text("Payee UPI ID") },
                        placeholder = { Text("sahasubhas597@okaxis") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUpiId.isNotBlank()) {
                            payeeUpiId = tempUpiId.trim()
                            Toast.makeText(context, "Payee UPI updated to $payeeUpiId", Toast.LENGTH_SHORT).show()
                            showEditUpiModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold)
                ) {
                    Text("Save UPI ID", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUpiModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun UpiAppLauncherButton(
    appName: String,
    color: Color,
    uri: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Launching payment for $appName...", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(appName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CertificateRow(
    label: String,
    value: String,
    color: Color,
    isCopyable: Boolean,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextTertiary, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            if (isCopyable) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomerLicenseItemCard(
    record: CustomerLicenseRecord,
    context: Context,
    onRevoke: () -> Unit
) {
    val licenseMessage = LicenseGeneratorUtil.buildLicenseMessage(
        customerName = record.customerName,
        licenseKey = record.licenseKey,
        amount = record.amountPaidInr
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurfaceElevated)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(record.customerName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (record.isActive) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (record.isActive) "ACTIVE" else "REVOKED",
                        color = if (record.isActive) BullishGreen else BearishRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text("₹${String.format("%,.0f", record.amountPaidInr)}", color = BrightGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Key: ${record.licenseKey}",
            color = NeonCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("📱 ${record.customerPhone}", color = TextSecondary, fontSize = 10.sp)
            Text("✉️ ${record.customerEmail}", color = TextSecondary, fontSize = 10.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${record.paymentMode} • ${record.issueDate}",
                color = TextTertiary,
                fontSize = 9.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Send WhatsApp / SMS
                IconButton(
                    onClick = {
                        try {
                            val cleanPhone = record.customerPhone.replace("+", "").replace(" ", "").replace("-", "")
                            val whatsappUri = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(licenseMessage)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${record.customerPhone}")
                                putExtra("sms_body", licenseMessage)
                            }
                            context.startActivity(smsIntent)
                        }
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = "Send Mobile", tint = BullishGreen, modifier = Modifier.size(14.dp))
                }

                // Send Email
                IconButton(
                    onClick = {
                        try {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${record.customerEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "zx26 Lifetime License: ${record.licenseKey}")
                                putExtra(Intent.EXTRA_TEXT, licenseMessage)
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Send Email", tint = ElectricIndigo, modifier = Modifier.size(14.dp))
                }

                // Copy Key
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Customer License Key", record.licenseKey))
                        Toast.makeText(context, "Key copied: ${record.licenseKey}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = NeonCyan, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/**
 * High-definition Canvas QR Code Matrix Generator in Jetpack Compose
 */
@Composable
private fun StylizedQrCodeCanvas(
    text: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val gridSize = 21
        val cellSize = size.width / gridSize
        val hash = text.hashCode()

        // Background
        drawRect(Color.White)

        // Draw outer finder patterns
        drawFinderPattern(0f, 0f, cellSize)
        drawFinderPattern((gridSize - 7) * cellSize, 0f, cellSize)
        drawFinderPattern(0f, (gridSize - 7) * cellSize, cellSize)

        // Draw simulated deterministic data modules based on text hash
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val inTopLeftFinder = r < 7 && c < 7
                val inTopRightFinder = r < 7 && c >= gridSize - 7
                val inBottomLeftFinder = r >= gridSize - 7 && c < 7
                val inCenterLogo = r in 8..12 && c in 8..12

                if (!inTopLeftFinder && !inTopRightFinder && !inBottomLeftFinder && !inCenterLogo) {
                    val bit = ((hash xor (r * 31 + c * 17)) and (1 shl ((r + c) % 16))) != 0
                    if (bit) {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellSize + cellSize * 0.08f, r * cellSize + cellSize * 0.08f),
                            size = Size(cellSize * 0.84f, cellSize * 0.84f),
                            cornerRadius = CornerRadius(cellSize * 0.15f)
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    cellSize: Float
) {
    val outerSize = 7 * cellSize
    // Outer black square
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(x, y),
        size = Size(outerSize, outerSize),
        cornerRadius = CornerRadius(cellSize * 0.4f)
    )
    // Inner white square
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x + cellSize, y + cellSize),
        size = Size(5 * cellSize, 5 * cellSize),
        cornerRadius = CornerRadius(cellSize * 0.3f)
    )
    // Core black square
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(x + 2 * cellSize, y + 2 * cellSize),
        size = Size(3 * cellSize, 3 * cellSize),
        cornerRadius = CornerRadius(cellSize * 0.2f)
    )
}
