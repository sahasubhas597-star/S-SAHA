package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.OIBuildupItem
import com.example.data.model.OIBuildupType
import com.example.data.model.OptionChainData
import com.example.data.model.OptionChainStrikeRow
import com.example.data.model.OptionContract
import com.example.data.model.OptionStrategyPayoff
import com.example.data.model.OptionStrategyType
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

val INDIAN_DERIVATIVE_UNDERLYINGS = listOf(
    "NIFTY 50" to "Index",
    "BANKNIFTY" to "Index",
    "FINNIFTY" to "Index",
    "MIDCPNIFTY" to "Index",
    "SENSEX" to "Index",
    "BANKEX" to "Index",
    "NIFTYIT" to "Index",
    "RELIANCE" to "Stock",
    "HDFCBANK" to "Stock",
    "ICICIBANK" to "Stock",
    "INFY" to "Stock",
    "TCS" to "Stock",
    "TATAMOTORS" to "Stock",
    "SBIN" to "Stock",
    "BHARTIARTL" to "Stock",
    "BAJFINANCE" to "Stock",
    "LT" to "Stock",
    "MARUTI" to "Stock",
    "ITC" to "Stock",
    "TATASTEEL" to "Stock",
    "KOTAKBANK" to "Stock",
    "AXISBANK" to "Stock",
    "ZOMATO" to "Stock",
    "COALINDIA" to "Stock"
)

val EXPIRY_CYCLES = listOf(
    "28-Aug-2026 (Weekly)",
    "04-Sep-2026 (Weekly)",
    "11-Sep-2026 (Weekly)",
    "25-Sep-2026 (Monthly)",
    "29-Oct-2026 (Monthly)"
)

enum class OptionChainViewMode(val title: String) {
    STANDARD("Standard (OI & LTP)"),
    GREEKS("Greeks (Δ, Γ, θ, ν)"),
    OI_BARS("OI Distribution Profile")
}

@Composable
fun OptionChainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedUnderlying by viewModel.selectedOptionUnderlying.collectAsState()
    val selectedExpiry by viewModel.selectedOptionExpiry.collectAsState()
    val optionChainData by viewModel.optionChainData.collectAsState()
    val selectedStrategyType by viewModel.selectedOptionStrategyType.collectAsState()
    val strategyPayoff by viewModel.optionStrategyPayoff.collectAsState()
    val oiBuildups by viewModel.oiBuildupScans.collectAsState()
    val oiFilter by viewModel.oiBuildupFilter.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Option Chain, 1: Strategy Payoffs, 2: OI Buildup
    var viewMode by remember { mutableStateOf(OptionChainViewMode.STANDARD) }
    var selectedLegOrderContract by remember { mutableStateOf<OptionContract?>(null) }
    var selectedOrderSide by remember { mutableStateOf(OrderSide.BUY) }
    var underlyingFilterCategory by remember { mutableStateOf("ALL") } // ALL, Index, Stock
    var searchQuery by remember { mutableStateOf("") }
    var isLiveFeedActive by remember { mutableStateOf(true) }
    var liveTickFlash by remember { mutableStateOf(false) }

    // Live Market 2-Second Tick Simulation for authentic NSE experience
    LaunchedEffect(isLiveFeedActive, selectedUnderlying) {
        while (isLiveFeedActive) {
            delay(2500)
            liveTickFlash = !liveTickFlash
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("option_chain_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.PAPER_TRADING)
        }

        // Live NSE / BSE Market Header & India VIX
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveFeedActive) BullishGreen else TextTertiary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NSE / BSE LIVE OPTION CHAIN",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BrightGold.copy(alpha = 0.15f))
                                    .border(1.dp, BrightGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "INDIA VIX: 13.45 (-1.8%)",
                                    color = BrightGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isLiveFeedActive) BullishGreen.copy(alpha = 0.15f) else TerminalSurfaceElevated)
                                    .clickable { isLiveFeedActive = !isLiveFeedActive }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isLiveFeedActive) "● LIVE FEED" else "PAUSED",
                                    color = if (isLiveFeedActive) BullishGreen else TextTertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Mini Ticker for key Indian Indices
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            IndexChip(name = "NIFTY", price = "24,852.40", change = "+182.10 (+0.74%)", isUp = true) {
                                viewModel.selectOptionUnderlying("NIFTY 50")
                            }
                        }
                        item {
                            IndexChip(name = "BANKNIFTY", price = "51,340.80", change = "+425.60 (+0.84%)", isUp = true) {
                                viewModel.selectOptionUnderlying("BANKNIFTY")
                            }
                        }
                        item {
                            IndexChip(name = "FINNIFTY", price = "23,145.20", change = "+168.40 (+0.73%)", isUp = true) {
                                viewModel.selectOptionUnderlying("FINNIFTY")
                            }
                        }
                        item {
                            IndexChip(name = "MIDCPNIFTY", price = "12,890.50", change = "+142.30 (+1.12%)", isUp = true) {
                                viewModel.selectOptionUnderlying("MIDCPNIFTY")
                            }
                        }
                        item {
                            IndexChip(name = "SENSEX", price = "81,480.00", change = "+595.00 (+0.74%)", isUp = true) {
                                viewModel.selectOptionUnderlying("SENSEX")
                            }
                        }
                    }
                }
            }
        }

        // Sub-Navigation Tabs: Option Chain, Strategy Payoff, OI Buildup
        item {
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = TerminalSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                        color = NeonCyan,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Option Chain", fontSize = 12.sp, fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Strategy Payoffs", fontSize = 12.sp, fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("OI Buildups", fontSize = 12.sp, fontWeight = if (activeSubTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Underlying Filter & Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL" to "All F&O", "Index" to "🇮🇳 Indices", "Stock" to "🏢 Stocks").forEach { (key, label) ->
                            FilterChip(
                                selected = underlyingFilterCategory == key,
                                onClick = { underlyingFilterCategory = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricIndigo.copy(alpha = 0.3f),
                                    selectedLabelColor = ElectricIndigo
                                )
                            )
                        }
                    }

                    // Search input toggle
                    IconButton(
                        onClick = { searchQuery = if (searchQuery.isEmpty()) " " else "" },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                }

                if (searchQuery.isNotEmpty()) {
                    OutlinedTextField(
                        value = searchQuery.trim(),
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search NSE/BSE F&O Underlyings...", color = TextTertiary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                val filteredUnderlyings = INDIAN_DERIVATIVE_UNDERLYINGS.filter {
                    (underlyingFilterCategory == "ALL" || it.second == underlyingFilterCategory) &&
                    (searchQuery.isBlank() || it.first.contains(searchQuery.trim(), ignoreCase = true))
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredUnderlyings) { (sym, type) ->
                        val isSelected = sym == selectedUnderlying
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan else TerminalSurface)
                                .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.selectOptionUnderlying(sym) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sym,
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.DarkGray else TextTertiary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expiry Selector Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "EXPIRY:", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(EXPIRY_CYCLES) { exp ->
                        val isSelected = exp == selectedExpiry
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) BrightGold.copy(alpha = 0.25f) else TerminalSurface)
                                .border(1.dp, if (isSelected) BrightGold else TerminalCardBorder, RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectOptionExpiry(exp) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = exp.substringBefore(" "),
                                color = if (isSelected) BrightGold else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Selected Underlying Snapshot Card
        if (optionChainData != null) {
            val data = optionChainData!!
            val atmStrikeVal = data.strikes.firstOrNull { it.isATM }?.strikePrice ?: data.underlyingPrice
            val pcrInterpretation = when {
                data.pcr >= 1.3 -> "Strongly Bullish"
                data.pcr >= 1.0 -> "Mildly Bullish"
                data.pcr >= 0.8 -> "Neutral"
                else -> "Bearish"
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = data.underlyingSymbol, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "Lot: ${data.lotSize}", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(text = data.underlyingName, color = TextSecondary, fontSize = 10.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%.2f", data.underlyingPrice)}",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "ATM Strike: ${atmStrikeVal.toInt()}",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quantitative Derivative Metrics Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerminalSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnalyticsItem(
                                label = "PCR (OI)",
                                value = "${data.pcr} ($pcrInterpretation)",
                                color = if (data.pcr >= 1.0) BullishGreen else BearishRed
                            )
                            AnalyticsItem(
                                label = "Max Pain",
                                value = "₹${data.maxPainStrike.toInt()}",
                                color = BrightGold
                            )
                            AnalyticsItem(
                                label = "Total Call OI",
                                value = "${String.format("%.1f", data.totalCallOI / 100000.0)}L",
                                color = BearishRed
                            )
                            AnalyticsItem(
                                label = "Total Put OI",
                                value = "${String.format("%.1f", data.totalPutOI / 100000.0)}L",
                                color = BullishGreen
                            )
                        }

                        // OI Ratio Visual Meter
                        Spacer(modifier = Modifier.height(6.dp))
                        val totalOiCombined = max(1L, data.totalCallOI + data.totalPutOI)
                        val callShare = (data.totalCallOI.toFloat() / totalOiCombined.toFloat())
                        val putShare = (data.totalPutOI.toFloat() / totalOiCombined.toFloat())

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Box(modifier = Modifier.weight(callShare).fillMaxHeight().background(BearishRed))
                            Box(modifier = Modifier.weight(putShare).fillMaxHeight().background(BullishGreen))
                        }
                    }
                }
            }
        }

        // Sub-Tab Content
        when (activeSubTab) {
            0 -> {
                // View Mode Switcher: Standard, Greeks, OI Bars
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "VIEW MODE:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OptionChainViewMode.values().forEach { mode ->
                                val isSelected = mode == viewMode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else TerminalSurface)
                                        .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                        .clickable { viewMode = mode }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = mode.title.substringBefore(" "),
                                        color = if (isSelected) NeonCyan else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                if (optionChainData != null) {
                    val data = optionChainData!!
                    val maxOiInChain = max(1L, data.strikes.maxOfOrNull { max(it.call.openInterest, it.put.openInterest) } ?: 1L)

                    item {
                        OptionChainHeader(viewMode = viewMode)
                    }

                    items(data.strikes) { strikeRow ->
                        OptionStrikeRowView(
                            strikeRow = strikeRow,
                            spotPrice = data.underlyingPrice,
                            isAtm = strikeRow.isATM,
                            viewMode = viewMode,
                            maxOiInChain = maxOiInChain,
                            onTradeCall = { contract, side ->
                                selectedLegOrderContract = contract
                                selectedOrderSide = side
                            },
                            onTradePut = { contract, side ->
                                selectedLegOrderContract = contract
                                selectedOrderSide = side
                            }
                        )
                    }
                }
            }

            1 -> {
                item {
                    StrategyPayoffSection(
                        selectedStrategy = selectedStrategyType,
                        payoff = strategyPayoff,
                        onSelectStrategy = { viewModel.selectOptionStrategyType(it) },
                        onExecuteBasket = {
                            strategyPayoff?.let { p ->
                                viewModel.executeMultiLegStrategy(p)
                                Toast.makeText(context, "Multi-Leg Option Strategy Placed in Paper Account!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            2 -> {
                item {
                    OIBuildupSection(
                        buildupList = oiBuildups,
                        selectedFilter = oiFilter,
                        onFilterChange = { filterType -> viewModel.setOIBuildupFilter(filterType) },
                        onSelectUnderlying = { sym ->
                            viewModel.selectOptionUnderlying(sym)
                            activeSubTab = 0
                        }
                    )
                }
            }
        }

        if (selectedLegOrderContract != null) {
            val contract = selectedLegOrderContract!!
            item {
                OptionOrderModal(
                    contract = contract,
                    side = selectedOrderSide,
                    onDismiss = { selectedLegOrderContract = null },
                    onConfirm = { lots ->
                        viewModel.executeOptionLegTrade(contract, selectedOrderSide, lots)
                        Toast.makeText(context, "Executed $lots Lot(s) of ${contract.underlyingSymbol} ${contract.strikePrice.toInt()} ${contract.optionType}", Toast.LENGTH_SHORT).show()
                        selectedLegOrderContract = null
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IndexChip(name: String, price: String, change: String, isUp: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalSurfaceElevated)
            .border(0.5.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = price, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Text(text = change, color = if (isUp) BullishGreen else BearishRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun AnalyticsItem(label: String, value: String, color: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 9.sp)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun OptionChainHeader(viewMode: OptionChainViewMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalSurfaceElevated)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (viewMode) {
            OptionChainViewMode.STANDARD -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "CALL OI", color = BearishRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "IV%", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "Δ Delta", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "CALL LTP", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier.width(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "STRIKE", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "PUT LTP", color = BearishRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Δ Delta", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "IV%", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "PUT OI", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            OptionChainViewMode.GREEKS -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "Δ Delta", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Γ Gamma", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "θ Theta", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "ν Vega", color = TextTertiary, fontSize = 9.sp)
                }

                Box(
                    modifier = Modifier.width(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "STRIKE", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "ν Vega", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "θ Theta", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "Γ Gamma", color = TextTertiary, fontSize = 9.sp)
                    Text(text = "Δ Delta", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            OptionChainViewMode.OI_BARS -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "CALL OI PROFILE", color = BearishRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    Text(text = "LTP", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                }

                Box(
                    modifier = Modifier.width(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "STRIKE", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "LTP", color = BearishRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    Text(text = "PUT OI PROFILE", color = BullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionStrikeRowView(
    strikeRow: OptionChainStrikeRow,
    spotPrice: Double,
    isAtm: Boolean,
    viewMode: OptionChainViewMode,
    maxOiInChain: Long,
    onTradeCall: (OptionContract, OrderSide) -> Unit,
    onTradePut: (OptionContract, OrderSide) -> Unit
) {
    val call = strikeRow.call
    val put = strikeRow.put
    val isCallItm = strikeRow.strikePrice < spotPrice
    val isPutItm = strikeRow.strikePrice > spotPrice

    // Authentic NSE Yellow tint for In-The-Money options
    val callBg = if (isAtm) NeonCyan.copy(alpha = 0.18f) else if (isCallItm) BrightGold.copy(alpha = 0.08f) else TerminalSurface
    val putBg = if (isAtm) NeonCyan.copy(alpha = 0.18f) else if (isPutItm) BrightGold.copy(alpha = 0.08f) else TerminalSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(if (isAtm) 1.5.dp else 0.5.dp, if (isAtm) NeonCyan else TerminalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ================== CALLS SIDE ==================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(callBg)
                    .clickable { onTradeCall(call, OrderSide.BUY) }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                when (viewMode) {
                    OptionChainViewMode.STANDARD -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(call.openInterest / 1000)}k",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "${String.format("%.1f", call.greeks.iv)}%",
                                color = TextTertiary,
                                fontSize = 9.sp,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = String.format("%.2f", call.greeks.delta),
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%.2f", call.ltp)}",
                                    color = if (call.changePercent >= 0) BullishGreen else BearishRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${if (call.changePercent >= 0) "+" else ""}${String.format("%.1f", call.changePercent)}%",
                                    color = if (call.changePercent >= 0) BullishGreen else BearishRed,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    OptionChainViewMode.GREEKS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = String.format("%.2f", call.greeks.delta), color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.3f", call.greeks.gamma), color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.1f", call.greeks.theta), color = BearishRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.1f", call.greeks.vega), color = BrightGold, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OptionChainViewMode.OI_BARS -> {
                        val callOiFraction = (call.openInterest.toFloat() / maxOiInChain.toFloat()).coerceIn(0.05f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Call OI horizontal bar
                            Row(
                                modifier = Modifier.weight(1f).height(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(callOiFraction)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(BearishRed.copy(alpha = 0.65f))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${call.openInterest / 1000}k", color = TextTertiary, fontSize = 8.sp)
                            }

                            Text(
                                text = "₹${String.format("%.1f", call.ltp)}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // ================== STRIKE PRICE COLUMN ==================
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .background(if (isAtm) NeonCyan else TerminalSurfaceElevated)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${strikeRow.strikePrice.toInt()}",
                        color = if (isAtm) Color.Black else BrightGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isAtm) {
                        Text(text = "ATM", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // ================== PUTS SIDE ==================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(putBg)
                    .clickable { onTradePut(put, OrderSide.BUY) }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                when (viewMode) {
                    OptionChainViewMode.STANDARD -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "₹${String.format("%.2f", put.ltp)}",
                                    color = if (put.changePercent >= 0) BullishGreen else BearishRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${if (put.changePercent >= 0) "+" else ""}${String.format("%.1f", put.changePercent)}%",
                                    color = if (put.changePercent >= 0) BullishGreen else BearishRed,
                                    fontSize = 8.sp
                                )
                            }
                            Text(
                                text = String.format("%.2f", put.greeks.delta),
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${String.format("%.1f", put.greeks.iv)}%",
                                color = TextTertiary,
                                fontSize = 9.sp,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${(put.openInterest / 1000)}k",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    OptionChainViewMode.GREEKS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = String.format("%.1f", put.greeks.vega), color = BrightGold, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.1f", put.greeks.theta), color = BearishRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.3f", put.greeks.gamma), color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(text = String.format("%.2f", put.greeks.delta), color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OptionChainViewMode.OI_BARS -> {
                        val putOiFraction = (put.openInterest.toFloat() / maxOiInChain.toFloat()).coerceIn(0.05f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${String.format("%.1f", put.ltp)}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            // Put OI horizontal bar
                            Row(
                                modifier = Modifier.weight(1f).height(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(text = "${put.openInterest / 1000}k", color = TextTertiary, fontSize = 8.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(putOiFraction)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(BullishGreen.copy(alpha = 0.65f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyPayoffSection(
    selectedStrategy: OptionStrategyType,
    payoff: OptionStrategyPayoff?,
    onSelectStrategy: (OptionStrategyType) -> Unit,
    onExecuteBasket: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT STRATEGY BLUEPRINT",
                color = BrightGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            if (selectedStrategy == OptionStrategyType.BATMAN_STRATEGY) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrightGold.copy(alpha = 0.2f))
                        .border(1.dp, BrightGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🦇 DUAL EAR PAYOFF",
                        color = BrightGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(OptionStrategyType.values()) { strat ->
                val isSelected = strat == selectedStrategy
                val isBatman = strat == OptionStrategyType.BATMAN_STRATEGY
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectStrategy(strat) },
                    label = { 
                        Text(
                            text = if (isBatman) "🦇 ${strat.title}" else strat.title, 
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isBatman) BrightGold.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.25f),
                        selectedLabelColor = if (isBatman) BrightGold else NeonCyan
                    )
                )
            }
        }

        if (payoff != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (payoff.strategyType == OptionStrategyType.BATMAN_STRATEGY) {
                                    Text(text = "🦇 ", fontSize = 16.sp)
                                }
                                Text(text = payoff.strategyType.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "${payoff.underlyingSymbol} • ${payoff.legs.size} Legs • ${payoff.strategyType.outlook}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (payoff.netDebitOrCredit >= 0) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (payoff.netDebitOrCredit >= 0) "Net Credit: ₹${String.format("%.2f", payoff.netDebitOrCredit)}" else "Net Debit: ₹${String.format("%.2f", abs(payoff.netDebitOrCredit))}",
                                color = if (payoff.netDebitOrCredit >= 0) BullishGreen else BearishRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Payoff Graph
                    PayoffGraphCanvas(
                        payoffPoints = payoff.payoffPoints,
                        breakevens = payoff.breakevens,
                        isBatmanStrategy = payoff.strategyType == OptionStrategyType.BATMAN_STRATEGY
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strategy Explanation / Insight Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (payoff.strategyType == OptionStrategyType.BATMAN_STRATEGY) BrightGold.copy(alpha = 0.08f) else ElectricIndigo.copy(alpha = 0.08f))
                            .border(0.5.dp, if (payoff.strategyType == OptionStrategyType.BATMAN_STRATEGY) BrightGold.copy(alpha = 0.3f) else ElectricIndigo.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = payoff.strategyType.description,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Max Profit (Peak)", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = if (payoff.maxProfit > 500000) "Unlimited" else "₹${String.format("%.0f", payoff.maxProfit)}",
                                color = BullishGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text(text = "Max Loss (Tail)", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = if (payoff.maxLoss < -500000) "Unlimited" else "₹${String.format("%.0f", abs(payoff.maxLoss))}",
                                color = BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text(text = "R:R Ratio", color = TextTertiary, fontSize = 9.sp)
                            Text(text = payoff.riskReward, color = BrightGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Net Delta / Theta", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = "${String.format("%.2f", payoff.netDelta)} / ${String.format("%.1f", payoff.netTheta)}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (payoff.breakevens.isNotEmpty()) "Breakevens: ${payoff.breakevens.joinToString(", ") { "₹${it.toInt()}" }}" else "No Expiry Breakeven Points",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Est. Margin: ₹${String.format("%.0f", payoff.marginRequired)}",
                            color = BrightGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "STRATEGY LEGS (${payoff.legs.size})", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    payoff.legs.forEach { leg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerminalSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (leg.action == OrderSide.BUY) BullishGreen else BearishRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${leg.action.name} ${leg.lots}x",
                                        color = if (leg.action == OrderSide.BUY) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${leg.optionContract.strikePrice.toInt()} ${leg.optionContract.optionType}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${leg.lots * leg.optionContract.lotSize} Qty @ ₹${String.format("%.2f", leg.optionContract.ltp)}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onExecuteBasket,
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedStrategy == OptionStrategyType.BATMAN_STRATEGY) BrightGold else NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("execute_strategy_basket_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedStrategy == OptionStrategyType.BATMAN_STRATEGY) "Execute 6-Leg Batman Strategy Basket" else "Execute Multi-Leg Strategy Basket",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoffGraphCanvas(
    payoffPoints: List<Pair<Double, Double>>,
    breakevens: List<Double>,
    isBatmanStrategy: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF04060A))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (payoffPoints.size < 2) return@Canvas

            val minX = payoffPoints.first().first
            val maxX = payoffPoints.last().first
            val minY = payoffPoints.minOf { it.second }
            val maxY = payoffPoints.maxOf { it.second }
            val yRange = if (maxY - minY == 0.0) 1.0 else maxY - minY

            val zeroY = size.height - (((0.0 - minY) / yRange) * size.height).toFloat()

            // Draw Zero PnL Reference Line
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            // Positive Profit Area Path
            val profitPath = Path()
            profitPath.moveTo(0f, zeroY)

            // Negative Loss Area Path
            val lossPath = Path()
            lossPath.moveTo(0f, zeroY)

            val curvePath = Path()

            payoffPoints.forEachIndexed { index, point ->
                val x = ((point.first - minX) / (maxX - minX) * size.width).toFloat()
                val y = size.height - (((point.second - minY) / yRange) * size.height).toFloat()

                if (index == 0) curvePath.moveTo(x, y) else curvePath.lineTo(x, y)

                if (point.second >= 0) {
                    profitPath.lineTo(x, y)
                } else {
                    profitPath.lineTo(x, zeroY)
                }

                if (point.second <= 0) {
                    lossPath.lineTo(x, y)
                } else {
                    lossPath.lineTo(x, zeroY)
                }
            }

            profitPath.lineTo(size.width, zeroY)
            profitPath.close()

            lossPath.lineTo(size.width, zeroY)
            lossPath.close()

            // Draw Profit & Loss Shaded Polygons
            drawPath(path = profitPath, color = BullishGreen.copy(alpha = 0.22f))
            drawPath(path = lossPath, color = BearishRed.copy(alpha = 0.22f))

            // Draw Main Payoff Curve Line
            drawPath(
                path = curvePath,
                color = if (isBatmanStrategy) BrightGold else NeonCyan,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Breakeven Points
            breakevens.forEach { be ->
                val beX = ((be - minX) / (maxX - minX) * size.width).toFloat()
                drawCircle(
                    color = BrightGold,
                    radius = 4.5.dp.toPx(),
                    center = Offset(beX, zeroY)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2.dp.toPx(),
                    center = Offset(beX, zeroY)
                )
            }
        }

        // Overlay labels for min strike, max strike, and zero line
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBatmanStrategy) "🦇 EAR 1 (PUT PEAK)" else "Max Profit Zone",
                    color = BullishGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isBatmanStrategy) "🦇 EAR 2 (CALL PEAK)" else "PROFIT (₹)",
                    color = if (isBatmanStrategy) BrightGold else BullishGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${payoffPoints.firstOrNull()?.first?.toInt() ?: 0}",
                    color = TextTertiary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "EXPIRY SPOT PRICE (₹)",
                    color = TextTertiary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${payoffPoints.lastOrNull()?.first?.toInt() ?: 0}",
                    color = TextTertiary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun OIBuildupSection(
    buildupList: List<OIBuildupItem>,
    selectedFilter: OIBuildupType?,
    onFilterChange: (OIBuildupType?) -> Unit,
    onSelectUnderlying: (String) -> Unit
) {
    val filtered = if (selectedFilter == null) buildupList else buildupList.filter { it.buildupType == selectedFilter }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "OI BUILDUP & DERIVATIVE HEATMAP",
            color = BrightGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All Buildups", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan
                    )
                )
            }
            items(OIBuildupType.values()) { type ->
                val isSelected = type == selectedFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(type) },
                    label = { Text(type.label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (type) {
                            OIBuildupType.LONG_BUILDUP -> BullishGreen.copy(alpha = 0.25f)
                            OIBuildupType.SHORT_BUILDUP -> BearishRed.copy(alpha = 0.25f)
                            OIBuildupType.SHORT_COVERING -> BrightGold.copy(alpha = 0.25f)
                            OIBuildupType.LONG_UNWINDING -> ElectricIndigo.copy(alpha = 0.25f)
                        },
                        selectedLabelColor = when (type) {
                            OIBuildupType.LONG_BUILDUP -> BullishGreen
                            OIBuildupType.SHORT_BUILDUP -> BearishRed
                            OIBuildupType.SHORT_COVERING -> BrightGold
                            OIBuildupType.LONG_UNWINDING -> ElectricIndigo
                        }
                    )
                )
            }
        }

        filtered.forEach { item ->
            val buildupColor = when (item.buildupType) {
                OIBuildupType.LONG_BUILDUP -> BullishGreen
                OIBuildupType.SHORT_BUILDUP -> BearishRed
                OIBuildupType.SHORT_COVERING -> BrightGold
                OIBuildupType.LONG_UNWINDING -> ElectricIndigo
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectUnderlying(item.symbol) }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.symbol, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(buildupColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = item.buildupType.label, color = buildupColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%.2f", item.ltp)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                                color = if (item.changePercent >= 0) BullishGreen else BearishRed,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "OI: ${(item.openInterest / 100000)}L", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "OI Chg: ${if (item.oiChangePercent >= 0) "+" else ""}${item.oiChangePercent}%",
                            color = if (item.oiChangePercent >= 0) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = "PCR: ${item.pcr}", color = if (item.pcr >= 1.0) BullishGreen else BearishRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "Tap for Chain →", color = NeonCyan, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OptionOrderModal(
    contract: OptionContract,
    side: OrderSide,
    onDismiss: () -> Unit,
    onConfirm: (lots: Int) -> Unit
) {
    var lots by remember { mutableStateOf(1) }
    var orderProductType by remember { mutableStateOf("MIS (Intraday)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (side == OrderSide.BUY) BullishGreen else BearishRed)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = side.name, color = if (side == OrderSide.BUY) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${contract.underlyingSymbol} ${contract.strikePrice.toInt()} ${contract.optionType}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Expiry: ${contract.expiryDate} • Premium: ₹${String.format("%.2f", contract.ltp)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Product Type (MIS / NRML)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MIS (Intraday)", "NRML (Overnight)").forEach { prod ->
                        val isSelected = prod == orderProductType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else TerminalSurfaceElevated)
                                .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                .clickable { orderProductType = prod }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = prod,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Lots (${contract.lotSize} / lot):", color = TextPrimary, fontSize = 13.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (lots > 1) lots-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$lots (${lots * contract.lotSize} qty)",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { lots++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Δ ${String.format("%.2f", contract.greeks.delta)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("θ ₹${String.format("%.1f", contract.greeks.theta)}/d", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("IV ${String.format("%.1f", contract.greeks.iv)}%", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                val totalAmount = lots * contract.lotSize * contract.ltp
                val approxMargin = if (side == OrderSide.BUY) totalAmount else (contract.strikePrice * contract.lotSize * lots * 0.14)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (side == OrderSide.BUY) "Premium Payable:" else "Premium Inflow:", color = TextTertiary, fontSize = 11.sp)
                        Text(text = "₹${String.format("%.2f", totalAmount)}", color = BrightGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Required Margin:", color = TextTertiary, fontSize = 11.sp)
                        Text(text = "₹${String.format("%.2f", approxMargin)}", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(lots) },
                colors = ButtonDefaults.buttonColors(containerColor = if (side == OrderSide.BUY) BullishGreen else BearishRed)
            ) {
                Text(text = "Place Option Order (${side.name})", color = if (side == OrderSide.BUY) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
