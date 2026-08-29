package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.engine.pinescript.PineScriptGenerator
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

@Composable
fun StrategyBuilderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeStrategy by viewModel.activeStrategy.collectAsState()
    val allStrategies by viewModel.strategies.collectAsState()

    var stopLossSlider by remember(activeStrategy) { mutableStateOf(activeStrategy.stopLossPercent.toFloat()) }
    var takeProfitSlider by remember(activeStrategy) { mutableStateOf(activeStrategy.takeProfitPercent.toFloat()) }
    var trailingStopSlider by remember(activeStrategy) { mutableStateOf(activeStrategy.trailingStopPercent.toFloat()) }
    var riskSlider by remember(activeStrategy) { mutableStateOf(activeStrategy.maxRiskPerTradePercent.toFloat()) }
    var autoTradingToggle by remember(activeStrategy) { mutableStateOf(activeStrategy.isAutoTradingEnabled) }
    var generatedPineScript by remember { mutableStateOf("") }
    var showPineScriptModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("strategy_builder_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.BACKTEST)
        }

        // Active Strategy Header Card
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
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeStrategy.name,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${activeStrategy.targetAssetClass} • ${activeStrategy.defaultTimeframe}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Auto-trade Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Auto-Trade", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = autoTradingToggle,
                                onCheckedChange = {
                                    autoTradingToggle = it
                                    val updated = activeStrategy.copy(isAutoTradingEnabled = it)
                                    viewModel.saveStrategy(updated)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = BullishGreen,
                                    uncheckedThumbColor = TextTertiary,
                                    uncheckedTrackColor = TerminalCardBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeStrategy.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Strategy Parameter Tuning Sliders
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RISK CONTROLS & EXECUTION RULES",
                        color = BrightGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Stop Loss Slider
                    ParameterSlider(
                        label = "Fixed Stop Loss",
                        valueStr = "${String.format("%.1f", stopLossSlider)}%",
                        value = stopLossSlider,
                        range = 0.5f..8.0f,
                        color = BearishRed,
                        onValueChange = { stopLossSlider = it }
                    )

                    // Take Profit Slider
                    ParameterSlider(
                        label = "Take Profit Target",
                        valueStr = "${String.format("%.1f", takeProfitSlider)}%",
                        value = takeProfitSlider,
                        range = 1.0f..15.0f,
                        color = BullishGreen,
                        onValueChange = { takeProfitSlider = it }
                    )

                    // Trailing Stop Slider
                    ParameterSlider(
                        label = "Trailing Stop Trigger",
                        valueStr = "${String.format("%.1f", trailingStopSlider)}%",
                        value = trailingStopSlider,
                        range = 0.5f..5.0f,
                        color = NeonCyan,
                        onValueChange = { trailingStopSlider = it }
                    )

                    // Risk Per Trade Slider
                    ParameterSlider(
                        label = "Max Portfolio Risk / Trade",
                        valueStr = "${String.format("%.1f", riskSlider)}%",
                        value = riskSlider,
                        range = 0.2f..4.0f,
                        color = BrightGold,
                        onValueChange = { riskSlider = it }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons: Save & Export Pine Script & Execute Backtest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val updated = activeStrategy.copy(
                                    stopLossPercent = stopLossSlider.toDouble(),
                                    takeProfitPercent = takeProfitSlider.toDouble(),
                                    trailingStopPercent = trailingStopSlider.toDouble(),
                                    maxRiskPerTradePercent = riskSlider.toDouble(),
                                    isAutoTradingEnabled = autoTradingToggle
                                )
                                viewModel.saveStrategy(updated)
                                Toast.makeText(context, "Strategy Saved to Database", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("save_strategy_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Rules", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                generatedPineScript = PineScriptGenerator.generatePineScriptV5(activeStrategy)
                                showPineScriptModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("export_pinescript_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PineScript v5", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Direct Execute 10-Year Backtest Button
                    Button(
                        onClick = {
                            val updated = activeStrategy.copy(
                                stopLossPercent = stopLossSlider.toDouble(),
                                takeProfitPercent = takeProfitSlider.toDouble(),
                                trailingStopPercent = trailingStopSlider.toDouble(),
                                maxRiskPerTradePercent = riskSlider.toDouble(),
                                isAutoTradingEnabled = autoTradingToggle
                            )
                            viewModel.saveStrategy(updated)
                            viewModel.executeBacktest(strategy = updated)
                            viewModel.setTab(6) // Switch to Backtest tab
                            Toast.makeText(context, "Executing 10-Year Backtest...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("strategy_execute_backtest_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXECUTE 10-YEAR BACKTEST", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TradingView Pine Script v5 Code Viewer Box
        if (showPineScriptModal && generatedPineScript.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRADINGVIEW PINE SCRIPT v5 ENGINE",
                                color = BrightGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("PineScript", generatedPineScript))
                                    Toast.makeText(context, "Pine Script Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF070B12))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = generatedPineScript,
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Strategy Preset Selector
        item {
            Text(
                text = "QUANT STRATEGY PRESETS",
                color = BrightGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        items(allStrategies) { strat ->
            val isSelected = strat.id == activeStrategy.id
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelected) TerminalSurfaceElevated else TerminalSurface),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else TerminalCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strat.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = strat.targetAssetClass, color = TextSecondary, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.selectStrategy(strat)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = if (isSelected) "Active" else "Load", color = if (isSelected) NeonCyan else TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    valueStr: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = valueStr, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = TerminalCardBorder
            )
        )
    }
}
