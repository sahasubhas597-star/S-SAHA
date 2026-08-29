package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.TradeExecution
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.components.QuantMetricCard
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun BacktestLabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val backtestResult by viewModel.backtestResult.collectAsState()
    val isBacktesting by viewModel.isBacktesting.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val activeStrategy by viewModel.activeStrategy.collectAsState()
    val allStrategies by viewModel.strategies.collectAsState()
    val instruments by viewModel.instruments.collectAsState()

    var selectedCapital by remember { mutableStateOf(100000.0) }
    var selectedSlippage by remember { mutableStateOf(0.05) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("backtest_lab_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.BACKTEST)
        }

        // Execution & Configuration Control Center
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("backtest_control_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "10-YEAR QUANTITATIVE BACKTEST",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedInstrument.symbol} • ${activeStrategy.name}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        // Primary Execute Button
                        Button(
                            onClick = {
                                viewModel.runBacktest10Years(
                                    initialCapital = selectedCapital,
                                    slippagePercent = selectedSlippage
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isBacktesting,
                            modifier = Modifier.testTag("run_backtest_btn")
                        ) {
                            if (isBacktesting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Running...", color = Color.White, fontSize = 11.sp)
                            } else {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Instrument Selector Chips
                    Text(
                        text = "SELECT ASSET / INSTRUMENT",
                        color = BrightGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val popular = listOf("NIFTY 50", "BANKNIFTY", "RELIANCE", "HDFCBANK", "NVDA", "AAPL", "BTC/USD", "TSLA", "SENSEX")
                        items(popular) { sym ->
                            val isSelected = selectedInstrument.symbol == sym
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else TerminalSurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val inst = instruments.find { it.symbol == sym } ?: selectedInstrument
                                        viewModel.selectInstrument(inst)
                                        viewModel.executeBacktest(
                                            instrument = inst,
                                            initialCapital = selectedCapital,
                                            slippagePercent = selectedSlippage
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("backtest_chip_$sym")
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

                    // Capital Selection Chips
                    Text(
                        text = "STARTING CAPITAL ALLOCATION",
                        color = BrightGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val capitalOptions = listOf(10000.0, 50000.0, 100000.0, 250000.0, 1000000.0)
                        items(capitalOptions) { cap ->
                            val isSelected = selectedCapital == cap
                            val label = when (cap) {
                                10000.0 -> "$10k"
                                50000.0 -> "$50k"
                                100000.0 -> "$100k"
                                250000.0 -> "$250k"
                                else -> "$1M"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) BrightGold.copy(alpha = 0.2f) else TerminalSurfaceElevated)
                                    .border(1.dp, if (isSelected) BrightGold else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedCapital = cap
                                        viewModel.runBacktest10Years(
                                            initialCapital = cap,
                                            slippagePercent = selectedSlippage
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("backtest_capital_$label")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BrightGold else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Strategy Parameters Summary Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceElevated, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "SL: ${activeStrategy.stopLossPercent}%", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "TP: ${activeStrategy.takeProfitPercent}%", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Trail: ${activeStrategy.trailingStopPercent}%", color = BrightGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Risk/Trd: ${activeStrategy.maxRiskPerTradePercent}%", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Large Prominent Action Bar for Explicit Execution
                    Button(
                        onClick = {
                            viewModel.runBacktest10Years(
                                initialCapital = selectedCapital,
                                slippagePercent = selectedSlippage
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBacktesting) ElectricIndigo.copy(alpha = 0.5f) else ElectricIndigo),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isBacktesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("execute_backtest_btn")
                    ) {
                        if (isBacktesting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulating 10 Years Historical Trades...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXECUTE 10-YEAR HISTORICAL BACKTEST", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        val res = backtestResult
        if (res != null) {
            val m = res.metrics

            // Equity Curve Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("backtest_equity_curve_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "10-YEAR EQUITY CURVE & MONTE CARLO BANDS",
                                color = BrightGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Initial: $${String.format("%,.0f", selectedCapital)} → Final: $${String.format("%,.0f", m.finalCapital)}",
                                color = BullishGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        EquityCurveCanvas(
                            equityCurve = res.equityCurve,
                            monteCarloSims = res.monteCarloSimulations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            // Core Quantitative Performance Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INSTITUTIONAL PERFORMANCE AUDIT",
                        color = BrightGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuantMetricCard(
                            title = "Net Profit",
                            value = "${if (m.netProfitPercent >= 0) "+" else ""}${String.format("%.1f", m.netProfitPercent)}%",
                            subValue = "$${String.format("%,.0f", m.netProfitAmount)}",
                            isPositive = m.netProfitPercent >= 0,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "10-Yr CAGR",
                            value = "${String.format("%.1f", m.cagr)}%",
                            subValue = "Annual Compounded",
                            isPositive = m.cagr > 10.0,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "Win Rate",
                            value = "${String.format("%.1f", m.winRate)}%",
                            subValue = "${m.winningTrades}W / ${m.losingTrades}L",
                            isPositive = m.winRate >= 50.0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuantMetricCard(
                            title = "Sharpe Ratio",
                            value = "${String.format("%.2f", m.sharpeRatio)}",
                            subValue = "Annualized Risk-Adj",
                            isPositive = m.sharpeRatio >= 1.5,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "Sortino Ratio",
                            value = "${String.format("%.2f", m.sortinoRatio)}",
                            subValue = "Downside Risk-Adj",
                            isPositive = m.sortinoRatio >= 1.8,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "Profit Factor",
                            value = "${String.format("%.2f", m.profitFactor)}",
                            subValue = "Gross Win/Loss",
                            isPositive = m.profitFactor >= 1.5,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuantMetricCard(
                            title = "Max Drawdown",
                            value = "-${String.format("%.1f", m.maxDrawdownPercent)}%",
                            subValue = "$${String.format("%,.0f", m.maxDrawdownAmount)}",
                            isPositive = false,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "Expectancy",
                            value = "$${String.format("%.1f", m.expectancy)}",
                            subValue = "Avg / Trade",
                            isPositive = m.expectancy > 0,
                            modifier = Modifier.weight(1f)
                        )
                        QuantMetricCard(
                            title = "Calmar Ratio",
                            value = "${String.format("%.2f", m.calmarRatio)}",
                            subValue = "CAGR / MaxDD",
                            isPositive = m.calmarRatio >= 1.5,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Robustness & Statistical Validation Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STATISTICAL OVERFITTING & TAIL-RISK AUDIT",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AuditRow("Walk-Forward Efficiency", "${String.format("%.1f", m.walkForwardEfficiency)}%", NeonCyan)
                            AuditRow("Out-of-Sample Win Rate", "${String.format("%.1f", m.outOfSampleWinRate)}%", BullishGreen)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AuditRow("Monte Carlo 95% VaR", "-${String.format("%.1f", m.monteCarloVaR95)}%", BrightGold)
                            AuditRow("Degradation Score", "${String.format("%.1f", m.strategyDegradationScore)}%", if (m.strategyDegradationScore < 15) BullishGreen else BearishRed)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AuditRow("Friction / Slippage Drag", "$${String.format("%,.0f", m.slippageImpactAmount)}", TextSecondary)
                            AuditRow("Brokerage & Exchange Fees", "$${String.format("%,.0f", m.totalFeesAndTaxes)}", TextSecondary)
                        }
                    }
                }
            }

            // AI Stress Test Button
            item {
                Button(
                    onClick = {
                        viewModel.requestAiStressTest()
                        viewModel.setTab(10) // Go to AI Copilot tab
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("ai_stress_test_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit Strategy with Gemini AI Risk Officer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Trade Execution Log
            if (res.trades.isNotEmpty()) {
                val displayTrades = res.trades.takeLast(10).reversed()
                item {
                    Text(
                        text = "HISTORICAL TRADE LOG (LAST ${displayTrades.size} OF ${res.trades.size})",
                        color = BrightGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                items(displayTrades) { t ->
                    TradeLogItem(trade = t)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AuditRow(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TradeLogItem(trade: TradeExecution) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dateStr = dateFormat.format(Date(trade.exitTime))

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${trade.side.name} ${trade.symbol}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dateStr, color = TextTertiary, fontSize = 10.sp)
                }
                Text(text = "Entry: $${String.format("%.2f", trade.entryPrice)} → Exit: $${String.format("%.2f", trade.exitPrice)} (${trade.exitReason})", color = TextSecondary, fontSize = 10.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (trade.pnlAmount >= 0) "+" else ""}$${String.format("%.2f", trade.pnlAmount)}",
                    color = if (trade.pnlAmount >= 0) BullishGreen else BearishRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${if (trade.pnlPercent >= 0) "+" else ""}${String.format("%.2f", trade.pnlPercent)}%",
                    color = if (trade.pnlPercent >= 0) BullishGreen else BearishRed,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun EquityCurveCanvas(
    equityCurve: List<Pair<Long, Double>>,
    monteCarloSims: List<List<Double>>,
    modifier: Modifier = Modifier
) {
    if (equityCurve.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        var minVal = equityCurve.minOf { it.second }
        var maxVal = equityCurve.maxOf { it.second }

        for (sim in monteCarloSims) {
            if (sim.isNotEmpty()) {
                minVal = min(minVal, sim.minOrNull() ?: minVal)
                maxVal = max(maxVal, sim.maxOrNull() ?: maxVal)
            }
        }

        val range = max(1.0, maxVal - minVal) * 1.1

        fun toY(value: Double): Float {
            val norm = (value - minVal) / range
            return (height * (1.0 - norm)).toFloat()
        }

        // Draw Monte Carlo simulation shadow lines
        for (sim in monteCarloSims) {
            if (sim.size > 1) {
                val simPath = Path()
                for (i in sim.indices) {
                    val x = (i.toFloat() / (sim.size - 1)) * width
                    val y = toY(sim[i])
                    if (i == 0) simPath.moveTo(x, y) else simPath.lineTo(x, y)
                }
                drawPath(simPath, color = ElectricIndigo.copy(alpha = 0.15f), style = Stroke(width = 1f))
            }
        }

        // Draw Main Strategy Equity Curve
        val mainPath = Path()
        for (i in equityCurve.indices) {
            val x = (i.toFloat() / (equityCurve.size - 1)) * width
            val y = toY(equityCurve[i].second)
            if (i == 0) mainPath.moveTo(x, y) else mainPath.lineTo(x, y)
        }
        drawPath(mainPath, color = NeonCyan, style = Stroke(width = 2.5f))
    }
}

