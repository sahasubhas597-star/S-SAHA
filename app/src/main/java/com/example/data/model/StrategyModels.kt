package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class IndicatorType(val label: String, val category: String) {
    EMA("Exponential Moving Average", "Trend"),
    SMA("Simple Moving Average", "Trend"),
    RSI("Relative Strength Index", "Momentum"),
    MACD("Moving Average Convergence Divergence", "Momentum"),
    BOLLINGER_BANDS("Bollinger Bands (20, 2.0)", "Volatility"),
    ATR("Average True Range (14)", "Volatility"),
    VWAP("Volume Weighted Average Price", "Institutional"),
    SUPERTREND("Supertrend (10, 3)", "Trend"),
    VOLUME_PROFILE("Volume Profile (POC & Value Area)", "Order Flow"),
    ORDER_FLOW_IMBALANCE("Order Flow Delta Imbalance", "Microstructure")
}

enum class ConditionOperator(val symbol: String, val label: String) {
    CROSSES_ABOVE("crosses above", "Crosses Above"),
    CROSSES_BELOW("crosses below", "Crosses Below"),
    GREATER_THAN(">", "Greater Than"),
    LESS_THAN("<", "Less Than"),
    EQUALS("==", "Equals"),
    BOUNCES_OFF("bounces off", "Bounces Off Level"),
    REJECTS("rejects", "Rejects Level")
}

data class StrategyRule(
    val id: String,
    val indicatorA: IndicatorType,
    val periodA: Int = 14,
    val operator: ConditionOperator,
    val indicatorB: IndicatorType? = null,
    val periodB: Int = 50,
    val thresholdValue: Double = 0.0,
    val useStaticThreshold: Boolean = false
)

@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val targetAssetClass: String = "EQUITY",
    val defaultTimeframe: String = "1h",
    val stopLossPercent: Double = 2.0,
    val takeProfitPercent: Double = 5.0,
    val trailingStopPercent: Double = 1.5,
    val maxRiskPerTradePercent: Double = 1.0,
    val maxOpenPositions: Int = 5,
    val entryRulesJson: String = "",
    val exitRulesJson: String = "",
    val isAutoTradingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TradingStrategy(
    val id: String,
    val name: String,
    val description: String,
    val targetAssetClass: AssetClass = AssetClass.EQUITY,
    val defaultTimeframe: Timeframe = Timeframe.H1,
    val stopLossPercent: Double = 2.0,
    val takeProfitPercent: Double = 5.0,
    val trailingStopPercent: Double = 1.5,
    val maxRiskPerTradePercent: Double = 1.0,
    val maxOpenPositions: Int = 5,
    val entryRules: List<StrategyRule> = emptyList(),
    val exitRules: List<StrategyRule> = emptyList(),
    val isAutoTradingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class PerformanceMetrics(
    val initialCapital: Double,
    val finalCapital: Double,
    val netProfitAmount: Double,
    val netProfitPercent: Double,
    val cagr: Double,
    val winRate: Double,
    val profitFactor: Double,
    val expectancy: Double,
    val sharpeRatio: Double,
    val sortinoRatio: Double,
    val calmarRatio: Double,
    val recoveryFactor: Double,
    val maxDrawdownAmount: Double,
    val maxDrawdownPercent: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val avgWin: Double,
    val avgLoss: Double,
    val riskRewardRatio: Double,
    val maxConsecutiveWins: Int,
    val maxConsecutiveLosses: Int,
    val slippageImpactAmount: Double,
    val totalFeesAndTaxes: Double,
    val outOfSampleWinRate: Double,
    val walkForwardEfficiency: Double,
    val monteCarloVaR95: Double,
    val strategyDegradationScore: Double
)

data class TradeExecution(
    val id: String,
    val symbol: String,
    val exchange: MarketExchange,
    val side: OrderSide,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val entryTime: Long,
    val exitTime: Long,
    val pnlAmount: Double,
    val pnlPercent: Double,
    val isWin: Boolean,
    val exitReason: String,
    val fees: Double,
    val mode: ExecutionMode = ExecutionMode.BACKTEST
)

data class BacktestResult(
    val strategyId: String,
    val strategyName: String,
    val symbol: String,
    val timeframe: Timeframe,
    val startDate: String,
    val endDate: String,
    val yearsCovered: Double,
    val metrics: PerformanceMetrics,
    val trades: List<TradeExecution>,
    val equityCurve: List<Pair<Long, Double>>,
    val drawdownCurve: List<Pair<Long, Double>>,
    val monteCarloSimulations: List<List<Double>> = emptyList()
)
