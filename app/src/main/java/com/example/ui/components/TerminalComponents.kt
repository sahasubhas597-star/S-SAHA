package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.Instrument
import com.example.data.model.SignalAlert
import com.example.data.model.SignalType
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedDim
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenDim
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun FinancialSafetyBanner(
    modifier: Modifier = Modifier,
    currentMode: ExecutionMode = ExecutionMode.BACKTEST
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("financial_safety_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Risk Protocol",
                    tint = BrightGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "INSTITUTIONAL RISK COMPLIANCE",
                        color = BrightGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Strict Separation: Backtest ≠ Paper Trading ≠ Live Execution. No guarantees on returns.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            ExecutionModeBadge(mode = currentMode)
        }
    }
}

@Composable
fun ExecutionModeBadge(mode: ExecutionMode) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(mode.badgeColor).copy(alpha = 0.18f))
            .border(1.dp, Color(mode.badgeColor).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("execution_mode_badge")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(mode.badgeColor))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = mode.label.uppercase(),
                color = Color(mode.badgeColor),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TickerTape(
    instruments: List<Instrument>,
    selectedSymbol: String,
    onSelect: (Instrument) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
            .testTag("ticker_tape_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (inst in instruments) {
            val isSelected = inst.symbol == selectedSymbol
            val isUp = inst.changePercent >= 0

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) TerminalSurfaceElevated else TerminalSurface)
                    .border(
                        1.dp,
                        if (isSelected) NeonCyan else TerminalCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(inst) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("ticker_item_${inst.symbol}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inst.exchange.region.flag,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = inst.symbol,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = inst.exchange.code,
                                color = TextTertiary,
                                fontSize = 9.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.2f", inst.currentPrice),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isUp) "+" else ""}${String.format("%.2f", inst.changePercent)}%",
                                color = if (isUp) BullishGreen else BearishRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuantMetricCard(
    title: String,
    value: String,
    subValue: String? = null,
    isPositive: Boolean? = null,
    modifier: Modifier = Modifier,
    tooltip: String? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("metric_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Column {
            Text(
                text = title.uppercase(),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = when (isPositive) {
                    true -> BullishGreen
                    false -> BearishRed
                    null -> TextPrimary
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun SignalBadge(
    signal: SignalAlert,
    modifier: Modifier = Modifier
) {
    val type = signal.signalType
    val color = Color(type.badgeColorHex)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (type.isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = type.title,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "R:R 1:${signal.riskRewardRatio}",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
