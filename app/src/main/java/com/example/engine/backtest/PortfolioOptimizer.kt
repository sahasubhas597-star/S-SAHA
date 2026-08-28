package com.example.engine.backtest

import com.example.data.model.OrderSide
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.PortfolioSummary
import com.example.data.model.RebalanceAction
import kotlin.math.abs

object PortfolioOptimizer {

    fun generateRebalanceRecommendations(
        positions: List<PortfolioPositionEntity>,
        cashBalance: Double
    ): PortfolioSummary {
        val totalInvested = positions.sumOf { it.quantity * it.currentPrice }
        val totalPortfolioValue = totalInvested + cashBalance

        var totalUnrealizedPnl = 0.0
        var weightedBetaSum = 0.0

        for (pos in positions) {
            val costBasis = pos.quantity * pos.avgEntryPrice
            val marketVal = pos.quantity * pos.currentPrice
            totalUnrealizedPnl += (marketVal - costBasis)
            val weight = if (totalInvested > 0) marketVal / totalInvested else 0.0
            weightedBetaSum += weight * pos.beta
        }

        val totalUnrealizedPnlPercent = if (totalInvested - totalUnrealizedPnl > 0) {
            (totalUnrealizedPnl / (totalInvested - totalUnrealizedPnl)) * 100.0
        } else 0.0

        val recommendations = mutableListOf<RebalanceAction>()

        for (pos in positions) {
            val currentVal = pos.quantity * pos.currentPrice
            val currentWeightPct = if (totalPortfolioValue > 0) (currentVal / totalPortfolioValue) * 100.0 else 0.0
            val targetWeightPct = pos.targetAllocationPercent
            val deltaPct = targetWeightPct - currentWeightPct

            if (abs(deltaPct) >= 0.5) { // Rebalance threshold 0.5%
                val targetValue = totalPortfolioValue * (targetWeightPct / 100.0)
                val valueDiff = targetValue - currentVal
                val shares = if (pos.currentPrice > 0) abs(valueDiff) / pos.currentPrice else 0.0
                val side = if (deltaPct > 0) OrderSide.BUY else OrderSide.SELL

                val rationale = if (deltaPct > 0) {
                    "Underweight by ${String.format("%.1f", deltaPct)}%. Deploy capital to restore target ${targetWeightPct}% allocation."
                } else {
                    "Overweight by ${String.format("%.1f", abs(deltaPct))}%. Trim profits to manage concentration risk."
                }

                recommendations.add(
                    RebalanceAction(
                        symbol = pos.symbol,
                        name = pos.name,
                        currentWeightPercent = (currentWeightPct * 10).toInt() / 10.0,
                        targetWeightPercent = targetWeightPct,
                        weightDeltaPercent = (deltaPct * 10).toInt() / 10.0,
                        suggestedAction = side,
                        sharesToTransact = (shares * 10).toInt() / 10.0,
                        estimatedAmount = abs(valueDiff),
                        rationale = rationale
                    )
                )
            }
        }

        val vaR95 = (totalPortfolioValue * 0.023 * weightedBetaSum).coerceAtLeast(100.0)
        val sharpe = 1.92

        return PortfolioSummary(
            totalValue = totalPortfolioValue,
            cashBalance = cashBalance,
            totalInvested = totalInvested,
            totalUnrealizedPnl = totalUnrealizedPnl,
            totalUnrealizedPnlPercent = (totalUnrealizedPnlPercent * 10).toInt() / 10.0,
            dayPnl = totalPortfolioValue * 0.014,
            dayPnlPercent = 1.4,
            portfolioBeta = (weightedBetaSum * 100).toInt() / 100.0,
            valueAtRisk95 = vaR95,
            sharpeRatio = sharpe,
            positions = positions,
            rebalanceRecommendations = recommendations
        )
    }
}
