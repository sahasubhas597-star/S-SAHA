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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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

@Composable
fun Zx26WebAdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val instruments by viewModel.instruments.collectAsState()
    val brokers by viewModel.brokers.collectAsState()

    // Admin State & Controls
    var adminName by remember { mutableStateOf("zx26") }
    var masterAlgoEngineEnabled by remember { mutableStateOf(true) }
    var dmaLowLatencyEnabled by remember { mutableStateOf(true) }
    var aiRiskGuardEnabled by remember { mutableStateOf(true) }
    var autoFailoverEnabled by remember { mutableStateOf(true) }
    var adminApiKey by remember { mutableStateOf("zx26_live_sec_9941a87bf26") }

    var selectedWebTab by remember { mutableStateOf("TERMINAL") } // TERMINAL, GATEWAYS, TELEMETRY, AUDIT
    var showApiKeyModal by remember { mutableStateOf(false) }
    var showHaltConfirmationModal by remember { mutableStateOf(false) }
    var auditLogs by remember {
        mutableStateOf(
            listOf(
                "10:45:02 - [ADMIN zx26] Initialized ZX26 master algo neural routing pipeline",
                "10:43:18 - [ADMIN zx26] Direct Market Access DMA latency calibrated to 12ms",
                "10:40:55 - [ADMIN zx26] Multi-broker fallback cluster verified: 18 gateways online",
                "10:38:12 - [ADMIN zx26] AI risk circuit breaker threshold locked at 3.0% drawdown",
                "10:35:00 - [ADMIN zx26] Session authenticated with Level 5 Superadmin Clearance"
            )
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("zx26_web_admin_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. ZX26 OFFICIAL BRAND HERO BANNER & LOGO
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zx26_hero_banner_card")
            ) {
                Column {
                    // Hero Banner Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_zx26_banner_1787908971465),
                            contentDescription = "ZX26 Algorithmic Trading Terminal Hero Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Dark Gradient Overlay for high readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            TerminalBg.copy(alpha = 0.85f),
                                            TerminalBg
                                        )
                                    )
                                )
                        )

                        // Top Badges on Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BullishGreen.copy(alpha = pulseAlpha))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE SYSTEM ONLINE",
                                        color = BullishGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Admin Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrightGold.copy(alpha = 0.9f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ADMIN: zx26",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Logo & App Identity Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official ZX26 App Logo
                        Image(
                            painter = painterResource(id = R.drawable.img_zx26_logo_1787908955898),
                            contentDescription = "ZX26 App Logo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "zx26",
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricIndigo.copy(alpha = 0.3f))
                                        .border(1.dp, ElectricIndigo, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "v26.4 INSTITUTIONAL",
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Next-Gen Algorithmic Trading & Multi-Broker Orchestration Desk",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. MASTER ADMIN IDENTITY (ADMIN: zx26)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrightGold.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth().testTag("zx26_admin_profile_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = BrightGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUPERADMIN CONTROL STATION",
                                color = BrightGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrightGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "CLEARANCE: LEVEL 5",
                                color = BrightGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Admin Details Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ADMIN OPERATOR", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = adminName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column {
                            Text("SESSION ROLE", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = "Master Architect",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXECUTION AUTHORITY", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = "Omnipotent (Full DMA)",
                                color = BullishGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Master Engine Switches
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdminSwitchRow(
                            label = "Master Algorithmic Trading Engine",
                            description = "Executes automated signals across all connected exchanges",
                            checked = masterAlgoEngineEnabled,
                            onCheckedChange = {
                                masterAlgoEngineEnabled = it
                                auditLogs = listOf("${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())} - [ADMIN zx26] Master engine ${if (it) "ACTIVATED" else "DEACTIVATED"}") + auditLogs
                                Toast.makeText(context, "Master Engine ${if (it) "Enabled" else "Disabled"} by zx26", Toast.LENGTH_SHORT).show()
                            }
                        )

                        AdminSwitchRow(
                            label = "Ultra Low-Latency DMA Direct Routing (12ms)",
                            description = "Bypasses retail order queue for sub-millisecond execution",
                            checked = dmaLowLatencyEnabled,
                            onCheckedChange = { dmaLowLatencyEnabled = it }
                        )

                        AdminSwitchRow(
                            label = "AI Risk Guard & Circuit Breaker",
                            description = "Real-time auto stop-loss and daily drawdown limiter",
                            checked = aiRiskGuardEnabled,
                            onCheckedChange = { aiRiskGuardEnabled = it }
                        )

                        AdminSwitchRow(
                            label = "Multi-Broker Auto-Failover Hub",
                            description = "Seamless switchover if Zerodha/Groww experience outages",
                            checked = autoFailoverEnabled,
                            onCheckedChange = { autoFailoverEnabled = it }
                        )
                    }

                    // Admin Quick Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showApiKeyModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = BrightGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("API Secrets", color = BrightGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showHaltConfirmationModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BearishRed.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Emergency Halt", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. INTERACTIVE ZX26 WEB PORTAL (WEB PAGE)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zx26_web_portal_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Web Browser Address Bar Simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZX26 OFFICIAL WEB PORTAL",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BullishGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("HTTPS SECURE", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Browser Address Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "https://zx26.trade/terminal/admin",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("ZX26 Web URL", "https://zx26.trade/terminal/admin")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "URL Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }

                            IconButton(
                                onClick = {
                                    val url = "https://zx26.trade/terminal/admin"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Launching $url in browser", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = "Open in Browser", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Web Portal Navigation Sub-tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "TERMINAL" to "Live Web Trader",
                            "GATEWAYS" to "18+ Brokers",
                            "TELEMETRY" to "System Health",
                            "AUDIT" to "Admin Logs"
                        ).forEach { (tabKey, label) ->
                            val isSelected = selectedWebTab == tabKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeonCyan else TerminalSurfaceElevated)
                                    .clickable { selectedWebTab = tabKey }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Web Portal Tab Content
                    when (selectedWebTab) {
                        "TERMINAL" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Live Web Feed (100ms Low-Latency Stream):", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                instruments.take(5).forEach { inst ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TerminalSurfaceElevated)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(inst.symbol, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(inst.exchange.code, color = TextTertiary, fontSize = 10.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${inst.exchange.region.currency} ${String.format("%,.2f", inst.currentPrice)}",
                                                color = if (inst.changePercent >= 0) BullishGreen else BearishRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${if (inst.changePercent >= 0) "+" else ""}${String.format("%.2f", inst.changePercent)}%",
                                                color = if (inst.changePercent >= 0) BullishGreen else BearishRed,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "GATEWAYS" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Active Broker Web Interfaces Connected to zx26:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                brokers.forEach { broker ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TerminalSurfaceElevated)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(broker.accountName, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Ping: ${broker.pingLatencyMs}ms • Limit: $${String.format("%,.0f", broker.maxOrderValueLimit)}", color = TextSecondary, fontSize = 9.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (broker.isConnected) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (broker.isConnected) "ONLINE" else "STANDBY",
                                                color = if (broker.isConnected) BullishGreen else BearishRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "TELEMETRY" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TelemetryMetricRow("Master Cluster", "ZX26-CORE-CLUSTER-US-EAST & IN-MUMBAI")
                                TelemetryMetricRow("Server Uptime", "99.995% (312 days continuous)")
                                TelemetryMetricRow("Neural Co-Processor", "Gemini 2.5 Pro Trading Matrix Active")
                                TelemetryMetricRow("Execution Engine Latency", "12.4 ms (Optimized)")
                                TelemetryMetricRow("Memory Allocation", "24.8 GB / 64.0 GB Pool")
                                TelemetryMetricRow("Active Strategy Workers", "12 Threaded Daemons Running")
                            }
                        }

                        "AUDIT" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                auditLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Web Launch Action Button
                    Button(
                        onClick = {
                            val url = "https://zx26.trade/terminal/admin"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening $url", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Launch ZX26 Web Portal in Full Browser",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // API KEY & CREDENTIALS MODAL
    // ==========================================
    if (showApiKeyModal) {
        AlertDialog(
            onDismissRequest = { showApiKeyModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = BrightGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin API Credentials (zx26)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Use this master API key to authenticate algorithmic webhooks, Pine Script alerts, and external Python/C++ trading bots:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalBg)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = adminApiKey,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                adminApiKey = "zx26_sec_${System.currentTimeMillis().toString(36)}_${(1000..9999).random()}"
                                auditLogs = listOf("${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())} - [ADMIN zx26] Regenerated master API secret token") + auditLogs
                                Toast.makeText(context, "New API Secret generated!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = BrightGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regenerate Key", color = BrightGold, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ZX26 Admin Key", adminApiKey)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "API Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showApiKeyModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Copy & Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==========================================
    // EMERGENCY MASTER HALT CONFIRMATION
    // ==========================================
    if (showHaltConfirmationModal) {
        AlertDialog(
            onDismissRequest = { showHaltConfirmationModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BearishRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Emergency Master Halt", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure, Admin zx26? This will instantly square-off all active open trades, cancel pending limit orders, and disconnect all 18 broker gateways.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        masterAlgoEngineEnabled = false
                        auditLogs = listOf("${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())} - [ADMIN zx26] 🛑 EMERGENCY GLOBAL HALT TRIGGERED") + auditLogs
                        Toast.makeText(context, "🛑 All algorithmic trading halted by Admin zx26", Toast.LENGTH_LONG).show()
                        showHaltConfirmationModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BearishRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Confirm Master Halt", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHaltConfirmationModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun AdminSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(description, color = TextSecondary, fontSize = 9.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = BullishGreen,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = TerminalBg
            )
        )
    }
}

@Composable
private fun TelemetryMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Text(value, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
