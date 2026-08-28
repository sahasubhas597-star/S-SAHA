package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.Instrument
import com.example.data.model.MarketRegion
import com.example.data.model.OrderSide
import com.example.data.model.OrderType
import com.example.data.model.Timeframe
import com.example.ui.components.CandlestickChart
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.components.SignalBadge
import com.example.ui.components.TickerTape
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MarketWorkstationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val currentCandles by viewModel.currentCandles.collectAsState()
    val detectedSignals by viewModel.detectedSignals.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val feedSearchQuery by viewModel.feedSearchQuery.collectAsState()

    val focusManager = LocalFocusManager.current
    var showOrderDialog by remember { mutableStateOf(false) }
    var orderSide by remember { mutableStateOf(OrderSide.BUY) }
    var searchFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val searchResults = remember(instruments, feedSearchQuery) {
        val query = feedSearchQuery.trim().lowercase()
        if (query.isEmpty()) {
            emptyList()
        } else {
            instruments.filter {
                it.symbol.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.sector.lowercase().contains(query) ||
                it.exchange.code.lowercase().contains(query)
            }.take(6)
        }
    }

    val filteredInstruments = remember(instruments, selectedRegion) {
        val reg = selectedRegion
        if (reg == null) instruments else instruments.filter { it.exchange.region == reg }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("market_workstation_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ZX26 Official Institutional Brand Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setTab(0) }
                    .testTag("workstation_zx26_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_zx26_logo_1787908955898),
                            contentDescription = "ZX26 Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "zx26 TERMINAL",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BrightGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Admin: zx26",
                                        color = BrightGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = "Institutional Multi-Broker Engine • Tap for Web Portal",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Open ZX26 Web Portal",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Financial Safety Institutional Badge
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.PAPER_TRADING)
        }

        // Live Feed Search & Quick Discovery Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("feed_search_container")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = feedSearchQuery,
                            onValueChange = {
                                viewModel.setFeedSearchQuery(it)
                                searchFeedbackMessage = null
                            },
                            placeholder = {
                                Text(
                                    text = "Search ticker/feed (e.g. NIFTY, BANKNIFTY, NVDA, BTC)...",
                                    color = TextTertiary,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Feed",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (feedSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.clearFeedSearchQuery()
                                            searchFeedbackMessage = null
                                        },
                                        modifier = Modifier.size(24.dp).testTag("feed_clear_search_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    val success = viewModel.searchAndSelectInstrument(feedSearchQuery)
                                    if (!success && feedSearchQuery.isNotBlank()) {
                                        searchFeedbackMessage = "Symbol '${feedSearchQuery}' not found."
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = TerminalSurfaceElevated,
                                unfocusedContainerColor = TerminalSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("feed_search_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                val success = viewModel.searchAndSelectInstrument(feedSearchQuery)
                                if (!success && feedSearchQuery.isNotBlank()) {
                                    searchFeedbackMessage = "Symbol '${feedSearchQuery}' not found."
                                } else {
                                    searchFeedbackMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("feed_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Run Search",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Search",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Autocomplete Search Results Dropdown List
                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SEARCH RESULTS (${searchResults.size})",
                            color = BrightGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().testTag("feed_search_results_list")
                        ) {
                            searchResults.forEach { resultInst ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TerminalSurfaceElevated)
                                        .clickable {
                                            viewModel.selectInstrument(resultInst)
                                            viewModel.setFeedSearchQuery(resultInst.symbol)
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("feed_search_result_${resultInst.symbol}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = resultInst.exchange.region.flag,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = resultInst.symbol,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${resultInst.name} • ${resultInst.exchange.code}",
                                                color = TextTertiary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    val isUp = resultInst.changePercent >= 0
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${resultInst.exchange.region.currency} ${String.format("%.2f", resultInst.currentPrice)}",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (isUp) "+" else ""}${String.format("%.2f", resultInst.changePercent)}%",
                                            color = if (isUp) BullishGreen else BearishRed,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (searchFeedbackMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = searchFeedbackMessage!!,
                            color = BearishRed,
                            fontSize = 11.sp
                        )
                    }

                    // Popular Indian & Global Feed Quick Picks
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("feed_popular_chips_row")
                    ) {
                        val popularSymbols = listOf("NIFTY 50", "BANKNIFTY", "RELIANCE", "HDFCBANK", "NVDA", "AAPL", "BTC/USD", "TSLA", "XAU/USD")
                        items(popularSymbols) { sym ->
                            val isSelected = selectedInstrument.symbol.equals(sym, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else TerminalSurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.searchAndSelectInstrument(sym)
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("feed_chip_$sym")
                            ) {
                                Text(
                                    text = sym,
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Market Selector Bar
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().testTag("market_region_row")
            ) {
                item {
                    FilterChip(
                        selected = selectedRegion == null,
                        onClick = { viewModel.setRegion(null) },
                        label = { Text("Global (All)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
                items(MarketRegion.values()) { region ->
                    FilterChip(
                        selected = selectedRegion == region,
                        onClick = { viewModel.setRegion(region) },
                        label = { Text("${region.flag} ${region.displayName}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
            }
        }

        // Live Ticker Tape
        item {
            TickerTape(
                instruments = filteredInstruments,
                selectedSymbol = selectedInstrument.symbol,
                onSelect = { viewModel.selectInstrument(it) }
            )
        }

        // Active Instrument Header
        item {
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
                                Text(
                                    text = selectedInstrument.symbol,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TerminalSurfaceElevated)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${selectedInstrument.exchange.code} • ${selectedInstrument.assetClass.name}",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = selectedInstrument.name,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${selectedInstrument.exchange.region.currency} ${String.format("%.2f", selectedInstrument.currentPrice)}",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            val isUp = selectedInstrument.changePercent >= 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isUp) BullishGreen else BearishRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isUp) "+" else ""}${String.format("%.2f", selectedInstrument.changeAmount)} (${String.format("%.2f", selectedInstrument.changePercent)}%)",
                                    color = if (isUp) BullishGreen else BearishRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Statistics Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Day Range", value = "${String.format("%.1f", selectedInstrument.dayLow)} - ${String.format("%.1f", selectedInstrument.dayHigh)}")
                        StatItem(label = "Volume", value = "${String.format("%.1f", selectedInstrument.volume / 1000000.0)}M")
                        StatItem(label = "Beta", value = "${selectedInstrument.beta}")
                        StatItem(label = "Mkt Cap", value = selectedInstrument.marketCap)
                    }

                    if (selectedInstrument.exchange.region == MarketRegion.INDIA) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.12f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.selectOptionUnderlying(selectedInstrument.symbol)
                                    viewModel.setTab(1) // Jump to Option Chain
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🇮🇳 Live F&O Option Chain Available",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Open Chain & Greeks →",
                                color = BrightGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Timeframe Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Timeframe.values().forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonCyan else TerminalSurface)
                            .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setTimeframe(tf) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("timeframe_btn_${tf.code}")
                    ) {
                        Text(
                            text = tf.label,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Candlestick Chart with Target & Stop Loss Lines
        item {
            val primSignal = selectedInstrument.primarySignal
            CandlestickChart(
                candles = currentCandles,
                showEma = true,
                showBollinger = true,
                showVwap = true,
                stopLossPrice = primSignal?.stopLossLevel,
                takeProfitPrice = primSignal?.takeProfitLevel
            )
        }

        // Detected Algorithmic Signals Bar
        if (detectedSignals.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE ALGO SIGNALS (${detectedSignals.size})",
                    color = BrightGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            items(detectedSignals) { sig ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SignalBadge(signal = sig)
                            Text(
                                text = "Conf: ${sig.confidenceScore}%",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sig.rationale,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SL: ${String.format("%.2f", sig.stopLossLevel)}",
                                color = BearishRed,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Trigger: ${String.format("%.2f", sig.priceTrigger)}",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "TP: ${String.format("%.2f", sig.takeProfitLevel)}",
                                color = BullishGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Quick Execution Action Bar (Buy / Sell Buttons)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        orderSide = OrderSide.BUY
                        showOrderDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("quick_buy_btn")
                ) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BUY / LONG", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        orderSide = OrderSide.SELL
                        showOrderDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BearishRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("quick_sell_btn")
                ) {
                    Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SELL / SHORT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Top-Level Modal Dialog
    if (showOrderDialog) {
        OrderPlacementModal(
            instrument = selectedInstrument,
            side = orderSide,
            onDismiss = { showOrderDialog = false },
            onConfirm = { qty, sl, tp, type ->
                viewModel.executePaperTrade(
                    symbol = selectedInstrument.symbol,
                    side = orderSide,
                    orderType = type,
                    quantity = qty,
                    stopLoss = sl,
                    takeProfit = tp,
                    strategyName = "Terminal Manual Ticket"
                )
                showOrderDialog = false
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 9.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun OrderPlacementModal(
    instrument: Instrument,
    side: OrderSide,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, stopLoss: Double, takeProfit: Double, type: OrderType) -> Unit
) {
    var quantityText by remember { mutableStateOf("10") }
    var stopLossText by remember {
        val sl = if (side == OrderSide.BUY) instrument.currentPrice * 0.98 else instrument.currentPrice * 1.02
        mutableStateOf(String.format("%.2f", sl))
    }
    var takeProfitText by remember {
        val tp = if (side == OrderSide.BUY) instrument.currentPrice * 1.05 else instrument.currentPrice * 0.95
        mutableStateOf(String.format("%.2f", tp))
    }
    var selectedOrderType by remember { mutableStateOf(OrderType.MARKET) }

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
                    Text(
                        text = side.name,
                        color = if (side == OrderSide.BUY) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${instrument.symbol} Order Ticket",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ref Price: ${instrument.exchange.region.currency} ${String.format("%.2f", instrument.currentPrice)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Quantity Input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity / Contracts", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TerminalCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("order_quantity_input")
                )

                val parsedQty = quantityText.toDoubleOrNull() ?: 1.0
                val totalEstVal = parsedQty * instrument.currentPrice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Order Value:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${instrument.exchange.region.currency} ${String.format("%,.2f", totalEstVal)}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Stop Loss Input
                OutlinedTextField(
                    value = stopLossText,
                    onValueChange = { stopLossText = it },
                    label = { Text("Stop Loss Level", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BearishRed,
                        unfocusedBorderColor = TerminalCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Take Profit Input
                OutlinedTextField(
                    value = takeProfitText,
                    onValueChange = { takeProfitText = it },
                    label = { Text("Take Profit Target", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BullishGreen,
                        unfocusedBorderColor = TerminalCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    val sl = stopLossText.toDoubleOrNull() ?: (instrument.currentPrice * 0.98)
                    val tp = takeProfitText.toDoubleOrNull() ?: (instrument.currentPrice * 1.05)
                    onConfirm(qty, sl, tp, selectedOrderType)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (side == OrderSide.BUY) BullishGreen else BearishRed
                )
            ) {
                Text(
                    text = "Submit Paper Order",
                    color = if (side == OrderSide.BUY) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
