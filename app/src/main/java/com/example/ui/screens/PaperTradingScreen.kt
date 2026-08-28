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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionMode
import com.example.data.model.PaperTradeEntity
import com.example.ui.components.FinancialSafetyBanner
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PaperTradingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val paperTrades by viewModel.paperTrades.collectAsState()

    val openTrades = remember(paperTrades) { paperTrades.filter { it.status == "OPEN" } }
    val totalUnrealized = remember(openTrades) { openTrades.sumOf { it.unrealizedPnl } }
    val startingCapital = 100000.0
    val totalEquity = startingCapital + totalUnrealized

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("paper_trading_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Safety Banner
        item {
            FinancialSafetyBanner(currentMode = ExecutionMode.PAPER_TRADING)
        }

        // Account Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VIRTUAL PAPER TRADING ACCOUNT",
                        color = BrightGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%,.2f", totalEquity)}",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Unrealized P&L: ",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${if (totalUnrealized >= 0.0) "+" else ""}$${String.format("%,.2f", totalUnrealized)}",
                            color = if (totalUnrealized >= 0.0) BullishGreen else BearishRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE POSITIONS (${openTrades.size})",
                    color = BrightGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricIndigo.copy(alpha = 0.2f))
                            .border(1.dp, ElectricIndigo.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.setTab(4) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("+ Sandbox Order", color = ElectricIndigo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (openTrades.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearPaperTrades() },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BearishRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", color = BearishRed, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Active Trades List
        if (openTrades.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(TerminalSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active paper trades open.", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Execute orders from Market Workstation or Scanner", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(openTrades) { trade ->
                PaperTradeCard(
                    trade = trade,
                    onCloseTrade = { viewModel.closePaperTrade(trade.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PaperTradeCard(
    trade: PaperTradeEntity,
    onCloseTrade: () -> Unit
) {
    val isBuy = trade.side == "BUY"
    val isProfitable = trade.unrealizedPnl >= 0

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BullishGreen else BearishRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trade.side,
                            color = if (isBuy) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trade.symbol,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${trade.quantity.toInt()} units",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (isProfitable) "+" else ""}$${String.format("%.2f", trade.unrealizedPnl)}",
                            color = if (isProfitable) BullishGreen else BearishRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCloseTrade,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BearishRed, modifier = Modifier.size(16.dp))
                    }
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
                Text("Entry: $${String.format("%.2f", trade.entryPrice)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("SL: $${String.format("%.2f", trade.stopLoss)}", color = BearishRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("TP: $${String.format("%.2f", trade.takeProfit)}", color = BullishGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trade.exchangeName} • ${trade.strategyName}",
                    color = TextTertiary,
                    fontSize = 9.sp
                )
                Text(
                    text = "Type: ${trade.orderType}",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
