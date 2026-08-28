package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.Instrument
import com.example.data.model.MarketExchange
import com.example.data.model.SignalType
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.components.SignalBadge
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

@Composable
fun MarketScannerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val instruments by viewModel.instruments.collectAsState()
    val scannerFilterExchange by viewModel.scannerFilterExchange.collectAsState()
    val scannerFilterSignalType by viewModel.scannerFilterSignalType.collectAsState()
    val scannerSearchQuery by viewModel.scannerSearchQuery.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val focusManager = LocalFocusManager.current

    val filteredList = remember(instruments, scannerFilterExchange, scannerFilterSignalType, scannerSearchQuery) {
        val query = scannerSearchQuery.trim().lowercase()
        instruments.filter { inst ->
            val matchExchange = scannerFilterExchange == null || inst.exchange == scannerFilterExchange
            val matchSignal = scannerFilterSignalType == null || inst.primarySignal?.signalType == scannerFilterSignalType
            val matchQuery = query.isEmpty() ||
                    inst.symbol.lowercase().contains(query) ||
                    inst.name.lowercase().contains(query) ||
                    inst.sector.lowercase().contains(query) ||
                    inst.exchange.code.lowercase().contains(query) ||
                    (inst.primarySignal?.signalType?.title?.lowercase()?.contains(query) == true) ||
                    (inst.primarySignal?.rationale?.lowercase()?.contains(query) == true)

            matchExchange && matchSignal && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("market_scanner_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.PAPER_TRADING)
        }

        // Header Title & Re-Scan Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MULTI-PATTERN SCANNER",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.triggerFullMarketScan()
                        },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            disabledContainerColor = ElectricIndigo.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("scanner_rescan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scanning...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Scan", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${filteredList.size} Setup(s)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Scanner Search Bar & Search Action Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("scanner_search_container")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scannerSearchQuery,
                            onValueChange = { viewModel.setScannerSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "Search ticker, pattern, sector (e.g. NIFTY, Breakout)...",
                                    color = TextTertiary,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (scannerSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.clearScannerSearchQuery() },
                                        modifier = Modifier.size(24.dp).testTag("scanner_clear_search_btn")
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
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
                                .testTag("scanner_search_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { focusManager.clearFocus() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("scanner_search_btn")
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

                    // Quick Search Tag Shortcuts
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf("NIFTY", "BANKNIFTY", "RELIANCE", "NVDA", "BTC", "Breakout", "Trapped", "Accumulation")
                        items(presets) { preset ->
                            val isCurrent = scannerSearchQuery.equals(preset, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) NeonCyan.copy(alpha = 0.2f) else TerminalSurfaceElevated)
                                    .border(1.dp, if (isCurrent) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (isCurrent) viewModel.clearScannerSearchQuery() else viewModel.setScannerSearchQuery(preset)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isCurrent) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Exchange Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().testTag("exchange_filter_row")
            ) {
                item {
                    FilterChip(
                        selected = scannerFilterExchange == null,
                        onClick = { viewModel.setScannerFilterExchange(null) },
                        label = { Text("All Exchanges", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
                items(MarketExchange.values()) { ex ->
                    FilterChip(
                        selected = scannerFilterExchange == ex,
                        onClick = { viewModel.setScannerFilterExchange(ex) },
                        label = { Text("${ex.region.flag} ${ex.code}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
            }
        }

        // Signal Strategy Pattern Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().testTag("signal_filter_row")
            ) {
                item {
                    FilterChip(
                        selected = scannerFilterSignalType == null,
                        onClick = { viewModel.setScannerFilterSignalType(null) },
                        label = { Text("All Patterns", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrightGold.copy(alpha = 0.2f),
                            selectedLabelColor = BrightGold
                        )
                    )
                }
                items(SignalType.values()) { sigType ->
                    FilterChip(
                        selected = scannerFilterSignalType == sigType,
                        onClick = { viewModel.setScannerFilterSignalType(sigType) },
                        label = { Text(sigType.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrightGold.copy(alpha = 0.2f),
                            selectedLabelColor = BrightGold
                        )
                    )
                }
            }
        }

        // List of Scanned Instruments with Pattern Setups
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(TerminalSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching setups found for your query/filters.", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                viewModel.clearScannerSearchQuery()
                                viewModel.setScannerFilterExchange(null)
                                viewModel.setScannerFilterSignalType(null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset All Filters", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredList) { inst ->
                ScannerInstrumentCard(
                    instrument = inst,
                    onSelect = {
                        viewModel.selectInstrument(inst)
                        viewModel.setTab(0) // Jump to Workstation
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ScannerInstrumentCard(
    instrument: Instrument,
    onSelect: () -> Unit
) {
    val sig = instrument.primarySignal
    val isUp = instrument.changePercent >= 0

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("scanner_card_${instrument.symbol}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = instrument.symbol,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerminalSurfaceElevated)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${instrument.exchange.region.flag} ${instrument.exchange.code}",
                            color = NeonCyan,
                            fontSize = 9.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${instrument.exchange.region.currency} ${String.format("%.2f", instrument.currentPrice)}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${if (isUp) "+" else ""}${String.format("%.2f", instrument.changePercent)}%",
                        color = if (isUp) BullishGreen else BearishRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Signal Info Block
            if (sig != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SignalBadge(signal = sig)
                    Text(
                        text = "R:R 1:${String.format("%.1f", sig.riskRewardRatio)} • ${sig.confidenceScore}% Conf",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = sig.rationale,
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SL: ${String.format("%.2f", sig.stopLossLevel)}", color = BearishRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Trigger: ${String.format("%.2f", sig.priceTrigger)}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("TP: ${String.format("%.2f", sig.takeProfitLevel)}", color = BullishGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            } else {
                Text(
                    text = "Monitoring order book & liquidity for high-probability setups...",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
