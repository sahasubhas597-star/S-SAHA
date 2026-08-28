package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrokerAccountEntity
import com.example.data.model.BrokerType
import com.example.data.model.DispatchedSandboxOrder
import com.example.data.model.ExecutionMode
import com.example.data.model.OrderSide
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerIntegrationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brokers by viewModel.brokers.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val dispatchedOrders by viewModel.dispatchedOrders.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()

    // Sandbox Order Dispatch State
    var selectedBrokerName by remember { mutableStateOf("Zerodha Kite Connect") }
    var selectedSymbol by remember { mutableStateOf(selectedInstrument.symbol) }
    var selectedOrderType by remember { mutableStateOf("MARKET") } // MARKET, LIMIT, SL-LMT
    var selectedProductType by remember { mutableStateOf("MIS") } // MIS, CNC, NRML
    var quantityText by remember { mutableStateOf("25") }
    var priceText by remember { mutableStateOf(String.format("%.2f", selectedInstrument.currentPrice)) }
    var stopLossText by remember { mutableStateOf(String.format("%.2f", selectedInstrument.currentPrice * 0.98)) }
    var takeProfitText by remember { mutableStateOf(String.format("%.2f", selectedInstrument.currentPrice * 1.05)) }

    // Dialogs & Modals
    var showAddBrokerDialog by remember { mutableStateOf(false) }
    var showFailoverNoticeDialog by remember { mutableStateOf(false) }
    var executedReceiptOrder by remember { mutableStateOf<DispatchedSandboxOrder?>(null) }
    var failoverTargetBroker by remember { mutableStateOf("Dhan Lightning #DH1109") }

    val activeInst = remember(selectedSymbol, instruments) {
        instruments.find { it.symbol == selectedSymbol } ?: selectedInstrument
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("broker_integration_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.LIVE_BROKER)
        }

        // ==========================================
        // EMERGENCY OUTAGE & MULTI-BROKER ACCESS BANNER
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrightGold.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("emergency_failover_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmergencyShare, contentDescription = null, tint = BrightGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MULTI-BROKER DOWNTIME FAILOVER HUB",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BullishGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("100% REDUNDANT", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "When any primary broker app experiences server downtime or login blockage, easily launch web terminals or switch all live/sandbox execution routes with 1 tap.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showFailoverNoticeDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1-Tap Failover Switch", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddBrokerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect Broker API", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // ACTIVE BROKER ADAPTERS & QUICK LAUNCHPAD
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Cable, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONNECTED BROKERS & WEB ACCESS",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Direct API link & backup web portals for instantaneous access",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${brokers.count { it.isConnected }} Active",
                        color = BullishGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Broker Adapters List
        items(brokers) { broker ->
            BrokerCard(
                broker = broker,
                onToggle = {
                    viewModel.toggleBrokerConnection(broker.id, broker.isConnected)
                    Toast.makeText(context, "${broker.accountName} status toggled", Toast.LENGTH_SHORT).show()
                },
                onLaunchWeb = {
                    val matchingType = BrokerType.values().find { it.name == broker.brokerType }
                    val url = matchingType?.webLoginUrl ?: "https://google.com"
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Opening $url", Toast.LENGTH_SHORT).show()
                    }
                },
                onSelectAsActiveRoute = {
                    selectedBrokerName = broker.accountName
                    Toast.makeText(context, "Routing set to ${broker.accountName}", Toast.LENGTH_SHORT).show()
                },
                isSelected = selectedBrokerName == broker.accountName
            )
        }

        // ==========================================
        // BROKER ORDER DISPATCH SANDBOX (BUY & SELL)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("broker_sandbox_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = BrightGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ORDER DISPATCH SANDBOX",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrightGold.copy(alpha = 0.15f))
                                .border(1.dp, BrightGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE OMS ROUTE", color = BrightGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Instantly execute BUY & SELL orders with live low-latency simulation across your connected broker sandboxes.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    // 1. Broker Route Selector
                    Text("1. Select Broker Execution Gateway:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val availableBrokers = brokers.map { it.accountName }.ifEmpty {
                            listOf(
                                "Zerodha Kite Connect",
                                "Groww Trading #GW7182",
                                "Dhan Lightning #DH1109",
                                "Angel One SmartAPI #A9941",
                                "Upstox Pro API #UP5521",
                                "Interactive Brokers",
                                "Alpaca Markets API",
                                "Binance Futures API"
                            )
                        }
                        availableBrokers.forEach { bName ->
                            val isSel = bName == selectedBrokerName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonCyan else TerminalSurfaceElevated)
                                    .border(1.dp, if (isSel) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedBrokerName = bName }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = bName,
                                    color = if (isSel) Color.Black else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // 2. Instrument Quick Picker
                    Text("2. Target Asset / Symbol:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("NIFTY 50", "BANKNIFTY", "RELIANCE", "HDFCBANK", "NVDA", "BTC/USD", "SPY", "TCS", "XAU/USD").forEach { sym ->
                            val isSel = sym == selectedSymbol
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) ElectricIndigo else TerminalSurfaceElevated)
                                    .border(1.dp, if (isSel) ElectricIndigo else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedSymbol = sym
                                        val inst = instruments.find { it.symbol == sym }
                                        if (inst != null) {
                                            priceText = String.format("%.2f", inst.currentPrice)
                                            stopLossText = String.format("%.2f", inst.currentPrice * 0.98)
                                            takeProfitText = String.format("%.2f", inst.currentPrice * 1.05)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sym,
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Current Live Price Banner
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
                            Text(text = activeInst.symbol, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "(${activeInst.exchange.code})", color = TextTertiary, fontSize = 10.sp)
                        }
                        Text(
                            text = "${activeInst.exchange.region.currency} ${String.format("%,.2f", activeInst.currentPrice)}",
                            color = if (activeInst.changePercent >= 0) BullishGreen else BearishRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 3. Order Type & Product Code
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Order Type
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Order Type", color = TextTertiary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("MARKET", "LIMIT", "SL-LMT").forEach { ot ->
                                    val isSel = ot == selectedOrderType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) BrightGold else TerminalSurfaceElevated)
                                            .clickable { selectedOrderType = ot }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ot,
                                            color = if (isSel) Color.Black else TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Product Code
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Product Code", color = TextTertiary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("MIS", "CNC", "NRML").forEach { pt ->
                                    val isSel = pt == selectedProductType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) NeonCyan else TerminalSurfaceElevated)
                                            .clickable { selectedProductType = pt }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pt,
                                            color = if (isSel) Color.Black else TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Quantity & Price Inputs
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sandbox_quantity_input")
                        )

                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text(if (selectedOrderType == "MARKET") "Mkt Price" else "Limit Price", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = selectedOrderType != "MARKET",
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightGold,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick Quantity Increment Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Presets:", color = TextTertiary, fontSize = 10.sp)
                        listOf(10, 25, 50, 100, 250, 500).forEach { addQty ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalSurfaceElevated)
                                    .clickable { quantityText = addQty.toString() }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(text = "$addQty", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // 5. Stop Loss & Take Profit Target Inputs
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stopLossText,
                            onValueChange = { stopLossText = it },
                            label = { Text("Stop Loss (SL)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BearishRed,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = takeProfitText,
                            onValueChange = { takeProfitText = it },
                            label = { Text("Take Profit (TP)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BullishGreen,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Calculated Notional & Margin Matrix
                    val parsedQty = quantityText.toDoubleOrNull() ?: 1.0
                    val parsedPrice = if (selectedOrderType == "MARKET") activeInst.currentPrice else (priceText.toDoubleOrNull() ?: activeInst.currentPrice)
                    val totalOrderValue = parsedQty * parsedPrice
                    val marginMultiplier = if (selectedProductType == "MIS") 0.20 else 1.0 // 5x leverage for intraday
                    val requiredMargin = totalOrderValue * marginMultiplier

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Order Value", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                "${activeInst.exchange.region.currency} ${String.format("%,.2f", totalOrderValue)}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (selectedProductType == "MIS") "Margin Req. (5x)" else "Margin Req. (1x)", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                "${activeInst.exchange.region.currency} ${String.format("%,.2f", requiredMargin)}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // ==========================================
                    // DIRECT ACTION BUTTONS: 🟢 BUY & 🔴 SELL
                    // ==========================================
                    Text("Direct 1-Click Order Execution:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 🟢 INSTANT BUY / LONG BUTTON
                        Button(
                            onClick = {
                                val sl = stopLossText.toDoubleOrNull() ?: (parsedPrice * 0.98)
                                val tp = takeProfitText.toDoubleOrNull() ?: (parsedPrice * 1.05)
                                val order = viewModel.dispatchSandboxOrder(
                                    brokerName = selectedBrokerName,
                                    symbol = selectedSymbol,
                                    side = OrderSide.BUY,
                                    orderType = selectedOrderType,
                                    productType = selectedProductType,
                                    quantity = parsedQty,
                                    price = parsedPrice,
                                    stopLoss = sl,
                                    takeProfit = tp
                                )
                                executedReceiptOrder = order
                                Toast.makeText(
                                    context,
                                    "✓ BUY Order Dispatched to ${order.brokerName} (${order.latencyMs}ms)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("sandbox_buy_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BUY / LONG",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "@ ${String.format("%.2f", parsedPrice)}",
                                    color = Color.Black.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // 🔴 INSTANT SELL / SHORT BUTTON
                        Button(
                            onClick = {
                                val sl = stopLossText.toDoubleOrNull() ?: (parsedPrice * 1.02)
                                val tp = takeProfitText.toDoubleOrNull() ?: (parsedPrice * 0.95)
                                val order = viewModel.dispatchSandboxOrder(
                                    brokerName = selectedBrokerName,
                                    symbol = selectedSymbol,
                                    side = OrderSide.SELL,
                                    orderType = selectedOrderType,
                                    productType = selectedProductType,
                                    quantity = parsedQty,
                                    price = parsedPrice,
                                    stopLoss = sl,
                                    takeProfit = tp
                                )
                                executedReceiptOrder = order
                                Toast.makeText(
                                    context,
                                    "✓ SELL Order Dispatched to ${order.brokerName} (${order.latencyMs}ms)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BearishRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("sandbox_sell_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SELL / SHORT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "@ ${String.format("%.2f", parsedPrice)}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // DISPATCHED ORDERS AUDIT LOG & ORDER BOOK
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = BrightGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SANDBOX ORDER BOOK & AUDIT LOG (${dispatchedOrders.size})",
                        color = BrightGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Live Multi-Broker OMS",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        if (dispatchedOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(TerminalSurface, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders dispatched in this session. Click Buy/Sell above to execute.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(dispatchedOrders) { order ->
                DispatchedOrderCard(
                    order = order,
                    onCancel = {
                        viewModel.cancelDispatchedOrder(order.orderId)
                        Toast.makeText(context, "Order ${order.orderId} cancelled", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ==========================================
        // COMPLETE GLOBAL & INDIAN BROKER DIRECTORY
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FULL BROKER DIRECTORY & WEB PORTALS (${BrokerType.values().size})",
                        color = BrightGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Instant backup login access for all supported brokers",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        items(BrokerType.values().toList()) { bt ->
            SupportedBrokerRow(
                brokerType = bt,
                onOpenPortal = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bt.webLoginUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Opening ${bt.webLoginUrl}", Toast.LENGTH_SHORT).show()
                    }
                },
                onQuickConnect = {
                    viewModel.addCustomBrokerAccount(
                        brokerType = bt,
                        accountName = "${bt.displayName} (Direct)",
                        apiKey = "key_auto_${bt.name.lowercase()}"
                    )
                    Toast.makeText(context, "${bt.displayName} adapter provisioned!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // ORDER EXECUTION RECEIPT MODAL DIALOG
    // ==========================================
    executedReceiptOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { executedReceiptOrder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Dispatched Successfully", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Simulated matching engine has acknowledged and filled your order with zero slippage.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = TerminalSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ReceiptRow("Order ID", order.orderId)
                            ReceiptRow("Broker Gateway", order.brokerName)
                            ReceiptRow("Symbol & Side", "${order.side} ${order.quantity.toInt()} ${order.symbol}")
                            ReceiptRow("Executed Price", "$${String.format("%.2f", order.price)}")
                            ReceiptRow("Latency", "${order.latencyMs} ms")
                            ReceiptRow("Product & Type", "${order.productType} • ${order.orderType}")
                            ReceiptRow("Status", order.status)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { executedReceiptOrder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==========================================
    // 1-TAP FAILOVER NOTICE DIALOG
    // ==========================================
    if (showFailoverNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showFailoverNoticeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, tint = BrightGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Route Failover", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "If your active broker (e.g. Zerodha or Groww) has an outage, switch your active trading routing target immediately:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    listOf(
                        "Dhan Lightning #DH1109" to "⚡ 16ms • 99.95% Uptime (Fastest Indian F&O)",
                        "Angel One SmartAPI #A9941" to "⚡ 22ms • 99.90% Uptime (Backup Indian Route)",
                        "Upstox Pro API #UP5521" to "⚡ 20ms • 99.88% Uptime (Backup Multi-Asset)",
                        "IBKR Pro Institutional #U992811" to "⚡ 21ms • 99.98% Uptime (Global Failover)"
                    ).forEach { (targetName, detail) ->
                        val isSelected = targetName == failoverTargetBroker
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonCyan.copy(alpha = 0.15f) else TerminalSurfaceElevated),
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else TerminalCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { failoverTargetBroker = targetName }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(targetName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(detail, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBrokerName = failoverTargetBroker
                        showFailoverNoticeDialog = false
                        Toast.makeText(context, "✓ Switched primary route to $failoverTargetBroker", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Apply Failover Route", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFailoverNoticeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==========================================
    // ADD BROKER API KEY MODAL DIALOG
    // ==========================================
    if (showAddBrokerDialog) {
        var newBrokerType by remember { mutableStateOf(BrokerType.GROWW) }
        var newAccountName by remember { mutableStateOf("") }
        var newApiKey by remember { mutableStateOf("") }
        var newEnv by remember { mutableStateOf("PRODUCTION") }

        AlertDialog(
            onDismissRequest = { showAddBrokerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Broker API", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Broker & enter your encrypted credentials:", color = TextSecondary, fontSize = 11.sp)

                    // Broker Dropdown Picker
                    Text("Broker Platform:", color = TextTertiary, fontSize = 10.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BrokerType.values().forEach { bt ->
                            val isSel = bt == newBrokerType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonCyan else TerminalSurfaceElevated)
                                    .clickable {
                                        newBrokerType = bt
                                        if (newAccountName.isBlank() || newAccountName.endsWith("User")) {
                                            newAccountName = "${bt.iconLabel} Direct Trader"
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = bt.displayName,
                                    color = if (isSel) Color.Black else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        label = { Text("Account Label (e.g. My Groww F&O)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newApiKey,
                        onValueChange = { newApiKey = it },
                        label = { Text("API Key / App Token") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRODUCTION", "SANDBOX").forEach { env ->
                            val isSel = env == newEnv
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) ElectricIndigo else TerminalSurfaceElevated)
                                    .clickable { newEnv = env }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = env,
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCustomBrokerAccount(
                            brokerType = newBrokerType,
                            accountName = newAccountName.ifBlank { "${newBrokerType.displayName} #${(1000..9999).random()}" },
                            apiKey = newApiKey.ifBlank { "key_demo_${newBrokerType.name.lowercase()}" },
                            environment = newEnv
                        )
                        showAddBrokerDialog = false
                        Toast.makeText(context, "${newBrokerType.displayName} added successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Save & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBrokerDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextTertiary, fontSize = 10.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DispatchedOrderCard(
    order: DispatchedSandboxOrder,
    onCancel: () -> Unit
) {
    val isBuy = order.side == "BUY"
    val isFilled = order.status == "FILLED"
    val isCancelled = order.status == "CANCELLED"
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = remember(order.timestamp) { sdf.format(Date(order.timestamp)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TerminalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BullishGreen else BearishRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.side,
                            color = if (isBuy) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.symbol,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${order.quantity.toInt()} @ ${String.format("%.2f", order.price)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isFilled -> BullishGreen.copy(alpha = 0.15f)
                                    isCancelled -> TextTertiary.copy(alpha = 0.15f)
                                    else -> BearishRed.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.status,
                            color = when {
                                isFilled -> BullishGreen
                                isCancelled -> TextTertiary
                                else -> BearishRed
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isFilled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(TerminalSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.brokerName} (${order.productType} • ${order.orderType})",
                    color = TextTertiary,
                    fontSize = 9.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${order.latencyMs}ms • $timeStr",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun BrokerCard(
    broker: BrokerAccountEntity,
    onToggle: () -> Unit,
    onLaunchWeb: () -> Unit,
    onSelectAsActiveRoute: () -> Unit,
    isSelected: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) NeonCyan else (if (broker.isConnected) BullishGreen.copy(alpha = 0.5f) else TerminalCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (broker.isConnected) BullishGreen else BearishRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = broker.accountName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(NeonCyan)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("DEFAULT ROUTE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = "${broker.brokerType} • ${broker.environment}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = broker.isConnected,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = BullishGreen,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = TerminalCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerminalSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = broker.apiKeyMasked, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Text(
                    text = if (broker.isConnected) "Latency: ${broker.pingLatencyMs}ms" else "Offline",
                    color = if (broker.isConnected) BullishGreen else TextTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Open Web Portal & Set As Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLaunchWeb,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = BrightGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Web App", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSelectAsActiveRoute,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) NeonCyan else TerminalSurfaceElevated),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelected) "Active Route" else "Use For Orders",
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportedBrokerRow(
    brokerType: BrokerType,
    onOpenPortal: () -> Unit,
    onQuickConnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TerminalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = brokerType.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(BullishGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("${brokerType.uptimePercent}% Up", color = BullishGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = "${brokerType.supportedMarkets} • ~${brokerType.defaultLatencyMs}ms ping", color = TextSecondary, fontSize = 10.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrightGold.copy(alpha = 0.15f))
                        .clickable { onOpenPortal() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = BrightGold, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Web App", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .clickable { onQuickConnect() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Connect", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
