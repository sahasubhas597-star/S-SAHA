package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MarketRegion(val displayName: String, val currency: String, val flag: String) {
    UNITED_STATES("United States", "USD", "🇺🇸"),
    INDIA("India", "INR", "🇮🇳"),
    UNITED_KINGDOM("United Kingdom", "GBP", "🇬🇧"),
    JAPAN("Japan", "JPY", "🇯🇵"),
    AUSTRALIA("Australia", "AUD", "🇦🇺"),
    CHINA_HK("China & HK", "HKD", "🇭🇰"),
    EUROPE("Europe", "EUR", "🇪🇺"),
    CRYPTO("Global Crypto", "USD", "🌐"),
    FOREX_COMMODITIES("Forex & Commodities", "USD", "⚡")
}

enum class MarketExchange(val code: String, val region: MarketRegion, val fullName: String) {
    NYSE("NYSE", MarketRegion.UNITED_STATES, "New York Stock Exchange"),
    NASDAQ("NASDAQ", MarketRegion.UNITED_STATES, "Nasdaq Global Select"),
    NSE("NSE", MarketRegion.INDIA, "National Stock Exchange of India"),
    BSE("BSE", MarketRegion.INDIA, "Bombay Stock Exchange"),
    LSE("LSE", MarketRegion.UNITED_KINGDOM, "London Stock Exchange"),
    TSE("TSE", MarketRegion.JAPAN, "Tokyo Stock Exchange"),
    ASX("ASX", MarketRegion.AUSTRALIA, "Australian Securities Exchange"),
    HKEX("HKEX", MarketRegion.CHINA_HK, "Hong Kong Stock Exchange"),
    SSE("SSE", MarketRegion.CHINA_HK, "Shanghai Stock Exchange"),
    EURONEXT("EURONEXT", MarketRegion.EUROPE, "Euronext Paris / Amsterdam"),
    BINANCE("BINANCE", MarketRegion.CRYPTO, "Binance Global Spot & Futures"),
    FOREX("FX_SPOT", MarketRegion.FOREX_COMMODITIES, "Interbank Foreign Exchange")
}

enum class AssetClass(val label: String) {
    EQUITY("Equity / Stocks"),
    INDEX("Market Index"),
    ETF("Exchange Traded Funds"),
    FUTURES("Derivatives / Futures"),
    OPTIONS("Options Contracts"),
    CRYPTO("Cryptocurrency"),
    COMMODITY("Commodity / Metals"),
    FOREX("Currency Pair")
}

enum class OptionType(val code: String, val label: String) {
    CALL("CE", "Call Option (CE)"),
    PUT("PE", "Put Option (PE)")
}

data class OptionGreeks(
    val delta: Double,
    val gamma: Double,
    val theta: Double, // Daily decay in ₹
    val vega: Double,
    val iv: Double // Implied Volatility %
)

data class OptionContract(
    val symbol: String, // e.g. "NIFTY 24850 CE"
    val underlyingSymbol: String,
    val strikePrice: Double,
    val optionType: OptionType,
    val expiryDate: String,
    val ltp: Double,
    val change: Double,
    val changePercent: Double,
    val bid: Double,
    val ask: Double,
    val volume: Long,
    val openInterest: Long,
    val changeInOI: Long,
    val greeks: OptionGreeks,
    val lotSize: Int = 50,
    val isATM: Boolean = false,
    val isITM: Boolean = false
)

data class OptionChainStrikeRow(
    val strikePrice: Double,
    val isATM: Boolean,
    val call: OptionContract,
    val put: OptionContract
)

data class OptionChainData(
    val underlyingSymbol: String,
    val underlyingName: String,
    val underlyingPrice: Double,
    val underlyingChange: Double,
    val underlyingChangePercent: Double,
    val expiryDate: String,
    val availableExpiries: List<String>,
    val strikes: List<OptionChainStrikeRow>,
    val totalCallOI: Long,
    val totalPutOI: Long,
    val pcr: Double,
    val maxPainStrike: Double,
    val indiaVix: Double = 13.40,
    val lotSize: Int = 50
)

enum class OptionStrategyType(val title: String, val outlook: String, val legsCount: Int, val description: String) {
    LONG_CALL("Long Call (Naked Bull)", "Bullish", 1, "Buy ATM/OTM Call for explosive upside with strictly limited capital risk."),
    LONG_PUT("Long Put (Naked Bear)", "Bearish", 1, "Buy ATM/OTM Put to profit from market drop with strictly limited capital risk."),
    BULL_CALL_SPREAD("Bull Call Spread", "Moderately Bullish", 2, "Buy ATM Call + Sell OTM Call. Lower cost, defined maximum profit & defined capped risk."),
    BEAR_PUT_SPREAD("Bear Put Spread", "Moderately Bearish", 2, "Buy ATM Put + Sell OTM Put. High probability downward directional strategy."),
    SHORT_STRADDLE("Short Straddle (Delta Neutral)", "Rangebound / Neutral", 2, "Sell ATM Call + Sell ATM Put. Maximum Theta decay profit if index remains in range."),
    SHORT_STRANGLE("Short Strangle (High Probability)", "Rangebound / Low Vol", 2, "Sell OTM Call + Sell OTM Put. Wide breakeven bands for consistent premium collection."),
    IRON_CONDOR("Iron Condor (Defined Risk Income)", "Neutral / Rangebound", 4, "Sell OTM Put Spread + Sell OTM Call Spread. Hedged wings prevent tail risk."),
    IRON_BUTTERFLY("Iron Butterfly (Pin Strategy)", "Neutral Pinning", 4, "Sell ATM Straddle + Buy OTM Protective Wings. Highest payout at exact ATM strike.");

    val displayName: String get() = title
}

data class StrategyLeg(
    val optionContract: OptionContract,
    val action: OrderSide,
    val lots: Int = 1
)

data class OptionStrategyPayoff(
    val strategyType: OptionStrategyType,
    val underlyingSymbol: String,
    val legs: List<StrategyLeg>,
    val netDebitOrCredit: Double,
    val maxProfit: Double,
    val maxLoss: Double,
    val breakevens: List<Double>,
    val riskReward: String,
    val netDelta: Double,
    val netTheta: Double,
    val marginRequired: Double,
    val payoffPoints: List<Pair<Double, Double>>
)

enum class OIBuildupType(val label: String, val badgeColor: Long, val interpretation: String) {
    LONG_BUILDUP("Long Build-up", 0xFF10B981, "Price Up ▲ + OI Up ▲ (Aggressive Buying)"),
    SHORT_BUILDUP("Short Build-up", 0xFFEF4444, "Price Down ▼ + OI Up ▲ (Aggressive Shorting)"),
    SHORT_COVERING("Short Covering", 0xFF06B6D4, "Price Up ▲ + OI Down ▼ (Sellers Exiting)"),
    LONG_UNWINDING("Long Unwinding", 0xFFF59E0B, "Price Down ▼ + OI Down ▼ (Buyers Exiting)");

    val title: String get() = label
}

data class OIBuildupItem(
    val symbol: String,
    val name: String,
    val ltp: Double,
    val changePercent: Double,
    val openInterest: Long,
    val oiChangePercent: Double,
    val pcr: Double,
    val buildupType: OIBuildupType,
    val exchange: MarketExchange = MarketExchange.NSE
)

enum class Timeframe(val code: String, val label: String, val minutes: Int) {
    M1("1m", "1 Min", 1),
    M5("5m", "5 Min", 5),
    M15("15m", "15 Min", 15),
    M30("30m", "30 Min", 30),
    H1("1h", "1 Hour", 60),
    H4("4h", "4 Hours", 240),
    D1("1D", "Daily", 1440),
    W1("1W", "Weekly", 10080),
    MN("1M", "Monthly", 43200)
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val vwap: Double = (high + low + close) / 3.0
) {
    val isBullish: Boolean get() = close >= open
    val bodyHeight: Double get() = kotlin.math.abs(close - open)
    val upperWick: Double get() = high - kotlin.math.max(open, close)
    val lowerWick: Double get() = kotlin.math.min(open, close) - low
}

data class Instrument(
    val symbol: String,
    val name: String,
    val exchange: MarketExchange,
    val assetClass: AssetClass,
    val currentPrice: Double,
    val changeAmount: Double,
    val changePercent: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val dayOpen: Double,
    val prevClose: Double,
    val volume: Double,
    val marketCap: String,
    val peRatio: Double = 0.0,
    val beta: Double = 1.0,
    val sector: String = "General",
    val primarySignal: SignalAlert? = null
)

enum class SignalType(val title: String, val isBullish: Boolean, val badgeColorHex: Long) {
    EARLY_ENTRY("Early Accumulation Entry", true, 0xFF10B981),
    BREAKOUT("Resistance Breakout", true, 0xFF06B6D4),
    BREAKDOWN("Support Breakdown", false, 0xFFEF4444),
    SUPPORT_BOUNCE("Dynamic Support Bounce", true, 0xFF3B82F6),
    RESISTANCE_REJECTION("Supply Zone Rejection", false, 0xFFF59E0B),
    TRAPPED_BUYERS("Trapped Buyers (Bull Trap)", false, 0xFFE11D48),
    TRAPPED_SELLERS("Trapped Sellers (Bear Trap / Short Squeeze)", true, 0xFF10B981),
    RSI_OVERSOLD_DIVERGENCE("RSI Bullish Divergence", true, 0xFF8B5CF6),
    RSI_OVERBOUGHT_DIVERGENCE("RSI Bearish Divergence", false, 0xFFEC4899),
    VWAP_CROSSOVER("Institutional VWAP Cross", true, 0xFF14B8A6),
    MACD_BULLISH_CROSS("MACD Golden Cross", true, 0xFF22C55E),
    EXIT_TAKE_PROFIT("Target 1 Profit Taker", false, 0xFFF59E0B),
    EXIT_STOP_LOSS("Defensive Stop Trigger", false, 0xFFEF4444)
}

data class SignalAlert(
    val id: String,
    val symbol: String,
    val exchange: MarketExchange,
    val signalType: SignalType,
    val priceTrigger: Double,
    val stopLossLevel: Double,
    val takeProfitLevel: Double,
    val riskRewardRatio: Double,
    val rationale: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeframe: Timeframe = Timeframe.H1,
    val confidenceScore: Int = 85
)

enum class ExecutionMode(val label: String, val badgeColor: Long) {
    BACKTEST("Historical Backtest", 0xFF6366F1),
    PAPER_TRADING("Paper Trading (Simulated)", 0xFFF59E0B),
    LIVE_BROKER("Live Broker Execution", 0xFF10B981)
}

enum class OrderSide { BUY, SELL }
enum class OrderType { MARKET, LIMIT, STOP_LIMIT, TRAILING_STOP, OCO }
enum class OrderStatus { PENDING, FILLED, CANCELLED, REJECTED, EXPIRED }
