package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.OrderSide
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.RebalanceAction
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.components.QuantMetricCard
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
fun PortfolioRiskScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("portfolio_risk_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.PAPER_TRADING)
        }

        val p = portfolioSummary
        if (p != null) {
            // Portfolio Value Summary Card
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
                            Text(
                                text = "TOTAL PORTFOLIO NET WORTH",
                                color = BrightGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalSurfaceElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "6 Multi-Asset Holdings",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%,.2f", p.totalValue)}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total Unrealized Gain: ",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${if (p.totalUnrealizedPnl >= 0) "+" else ""}$${String.format("%,.2f", p.totalUnrealizedPnl)} (+${p.totalUnrealizedPnlPercent}%)",
                                color = if (p.totalUnrealizedPnl >= 0) BullishGreen else BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Quantitative Risk Diagnostics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuantMetricCard(
                        title = "Portfolio Beta",
                        value = "${p.portfolioBeta}",
                        subValue = "vs Global Equities",
                        isPositive = p.portfolioBeta < 1.1,
                        modifier = Modifier.weight(1f)
                    )
                    QuantMetricCard(
                        title = "1-Day 95% VaR",
                        value = "$${String.format("%,.0f", p.valueAtRisk95)}",
                        subValue = "Max Expected Loss",
                        isPositive = false,
                        modifier = Modifier.weight(1f)
                    )
                    QuantMetricCard(
                        title = "Sharpe Ratio",
                        value = "${p.sharpeRatio}",
                        subValue = "Risk-Adjusted",
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Automated Markowitz Rebalancing Engine Recommendations
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DYNAMIC ASSET REBALANCING",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.applyRebalance()
                                    Toast.makeText(context, "Portfolio Rebalance Executed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Rebalance Now", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        p.rebalanceRecommendations.forEach { rec ->
                            RebalanceActionRow(rec = rec)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Holdings List
            item {
                Text(
                    text = "PORTFOLIO ASSET ALLOCATION",
                    color = BrightGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            items(p.positions) { pos ->
                HoldingItemCard(pos = pos)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RebalanceActionRow(rec: RebalanceAction) {
    val isBuy = rec.suggestedAction == OrderSide.BUY
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalSurfaceElevated)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isBuy) BullishGreen else BearishRed)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = rec.suggestedAction.name, color = if (isBuy) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = rec.symbol, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = rec.rationale, color = TextSecondary, fontSize = 10.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${rec.currentWeightPercent}% → ${rec.targetWeightPercent}%",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${String.format("%,.0f", rec.estimatedAmount)}",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun HoldingItemCard(pos: PortfolioPositionEntity) {
    val currentVal = pos.quantity * pos.currentPrice
    val costVal = pos.quantity * pos.avgEntryPrice
    val pnl = currentVal - costVal
    val pnlPct = if (costVal > 0) (pnl / costVal) * 100.0 else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
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
                    Text(text = pos.symbol, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = pos.assetClass, color = TextTertiary, fontSize = 9.sp)
                }
                Text(text = "${pos.quantity} units @ $${String.format("%.2f", pos.avgEntryPrice)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${String.format("%,.2f", currentVal)}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(
                    text = "${if (pnl >= 0) "+" else ""}$${String.format("%.2f", pnl)} (${String.format("%.1f", pnlPct)}%)",
                    color = if (pnl >= 0) BullishGreen else BearishRed,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
