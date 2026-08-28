package com.example.data.repository

import com.example.data.model.AssetClass
import com.example.data.model.Candle
import com.example.data.model.Instrument
import com.example.data.model.MarketExchange
import com.example.data.model.MarketRegion
import com.example.data.model.OIBuildupItem
import com.example.data.model.OIBuildupType
import com.example.data.model.OptionChainData
import com.example.data.model.OptionChainStrikeRow
import com.example.data.model.OptionContract
import com.example.data.model.OptionGreeks
import com.example.data.model.OptionStrategyPayoff
import com.example.data.model.OptionStrategyType
import com.example.data.model.OptionType
import com.example.data.model.OrderSide
import com.example.data.model.SignalAlert
import com.example.data.model.StrategyLeg
import com.example.data.model.Timeframe
import com.example.engine.scanner.MarketScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MarketDataRepository {

    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())
    val instruments: Flow<List<Instrument>> = _instruments.asStateFlow()

    private val cachedCandles = mutableMapOf<String, List<Candle>>()

    init {
        loadInitialInstruments()
    }

    private fun loadInitialInstruments() {
        val list = listOf(
            // ===== INDIAN MARKET INDICES (ALL MAJOR INDICES) =====
            Instrument("NIFTY 50", "Nifty 50 Index", MarketExchange.NSE, AssetClass.INDEX, 24852.40, 182.10, 0.74, 24915.00, 24710.00, 24725.00, 24670.30, 245000000.0, "₹285 Lakh Cr", 22.8, 0.95, "Benchmark Index"),
            Instrument("BANKNIFTY", "Nifty Bank Index", MarketExchange.NSE, AssetClass.INDEX, 51340.80, 425.60, 0.84, 51520.00, 50980.00, 51040.00, 50915.20, 142000000.0, "₹110 Lakh Cr", 16.4, 1.18, "Banking Benchmark"),
            Instrument("FINNIFTY", "Nifty Financial Services", MarketExchange.NSE, AssetClass.INDEX, 23145.20, 168.40, 0.73, 23220.00, 22990.00, 23010.00, 22976.80, 68000000.0, "₹78 Lakh Cr", 17.2, 1.10, "Financial Sector"),
            Instrument("MIDCPNIFTY", "Nifty Midcap Select", MarketExchange.NSE, AssetClass.INDEX, 12890.50, 142.30, 1.12, 12940.00, 12760.00, 12780.00, 12748.20, 48000000.0, "₹32 Lakh Cr", 26.5, 1.25, "Midcap Benchmark"),
            Instrument("NIFTYNEXT50", "Nifty Next 50 (Junior Nifty)", MarketExchange.NSE, AssetClass.INDEX, 72450.00, 680.00, 0.95, 72680.00, 71800.00, 71920.00, 71770.00, 32000000.0, "₹45 Lakh Cr", 24.1, 1.05, "Large Cap Growth"),
            Instrument("NIFTYIT", "Nifty IT Index", MarketExchange.NSE, AssetClass.INDEX, 42180.00, 510.00, 1.22, 42350.00, 41720.00, 41800.00, 41670.00, 28000000.0, "₹38 Lakh Cr", 29.4, 0.85, "Information Tech"),
            Instrument("NIFTYAUTO", "Nifty Auto Index", MarketExchange.NSE, AssetClass.INDEX, 25840.00, 340.00, 1.33, 25980.00, 25520.00, 25580.00, 25500.00, 34000000.0, "₹22 Lakh Cr", 21.3, 1.30, "Automotive"),
            Instrument("NIFTYPHARMA", "Nifty Pharma Index", MarketExchange.NSE, AssetClass.INDEX, 22480.00, -85.00, -0.38, 22640.00, 22410.00, 22590.00, 22565.00, 19000000.0, "₹18 Lakh Cr", 32.1, 0.65, "Healthcare"),
            Instrument("NIFTYFMCG", "Nifty FMCG Index", MarketExchange.NSE, AssetClass.INDEX, 58420.00, 195.00, 0.33, 58650.00, 58240.00, 58300.00, 58225.00, 16000000.0, "₹25 Lakh Cr", 38.6, 0.55, "Consumer Goods"),
            Instrument("NIFTYMETAL", "Nifty Metal Index", MarketExchange.NSE, AssetClass.INDEX, 9420.00, 185.00, 2.00, 9480.00, 9250.00, 9280.00, 9235.00, 42000000.0, "₹16 Lakh Cr", 15.2, 1.45, "Metals & Mining"),
            Instrument("NIFTYENERGY", "Nifty Energy Index", MarketExchange.NSE, AssetClass.INDEX, 39650.00, 420.00, 1.07, 39820.00, 39280.00, 39350.00, 39230.00, 31000000.0, "₹35 Lakh Cr", 18.4, 0.92, "Oil, Gas & Power"),
            Instrument("SENSEX", "S&P BSE SENSEX", MarketExchange.BSE, AssetClass.INDEX, 81480.00, 595.00, 0.74, 81680.00, 80990.00, 81050.00, 80885.00, 180000000.0, "₹420 Lakh Cr", 23.4, 0.94, "BSE Benchmark 30"),
            Instrument("BANKEX", "S&P BSE BANKEX", MarketExchange.BSE, AssetClass.INDEX, 58240.00, 480.00, 0.83, 58450.00, 57820.00, 57900.00, 57760.00, 45000000.0, "₹105 Lakh Cr", 16.8, 1.15, "BSE Banking"),

            // ===== TOP INDIAN F&O STOCKS (OPTIONS ELIGIBLE) =====
            Instrument("RELIANCE", "Reliance Industries Ltd", MarketExchange.NSE, AssetClass.EQUITY, 3014.50, 44.00, 1.48, 3029.00, 2980.00, 2985.00, 2970.50, 12400000.0, "₹20.4 Lakh Cr", 26.3, 0.88, "Energy & Retail"),
            Instrument("HDFCBANK", "HDFC Bank Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1648.50, -6.50, -0.39, 1662.00, 1642.00, 1655.00, 1655.00, 18900000.0, "₹12.5 Lakh Cr", 18.9, 0.98, "Banking"),
            Instrument("ICICIBANK", "ICICI Bank Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1218.00, 14.50, 1.20, 1224.00, 1205.00, 1208.00, 1203.50, 16200000.0, "₹8.6 Lakh Cr", 17.5, 1.05, "Banking"),
            Instrument("INFY", "Infosys Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1895.00, 28.00, 1.50, 1908.00, 1870.00, 1875.00, 1867.00, 9400000.0, "₹7.8 Lakh Cr", 27.8, 0.78, "IT Services"),
            Instrument("TCS", "Tata Consultancy Services", MarketExchange.NSE, AssetClass.EQUITY, 4285.00, 40.50, 0.95, 4312.00, 4250.00, 4260.00, 4244.50, 4800000.0, "₹15.5 Lakh Cr", 31.2, 0.72, "IT Services"),
            Instrument("SBIN", "State Bank of India", MarketExchange.NSE, AssetClass.EQUITY, 824.50, 11.20, 1.38, 829.00, 814.00, 816.00, 813.30, 24500000.0, "₹7.3 Lakh Cr", 11.4, 1.35, "PSU Banking"),
            Instrument("BHARTIARTL", "Bharti Airtel Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1540.00, 18.00, 1.18, 1548.00, 1522.00, 1528.00, 1522.00, 8200000.0, "₹8.8 Lakh Cr", 45.2, 0.82, "Telecom"),
            Instrument("ITC", "ITC Ltd", MarketExchange.NSE, AssetClass.EQUITY, 502.50, 3.20, 0.64, 505.00, 499.00, 500.00, 499.30, 14200000.0, "₹6.2 Lakh Cr", 28.4, 0.60, "FMCG / Hotels"),
            Instrument("LT", "Larsen & Toubro Ltd", MarketExchange.NSE, AssetClass.EQUITY, 3680.00, 52.00, 1.43, 3705.00, 3635.00, 3640.00, 3628.00, 3900000.0, "₹5.1 Lakh Cr", 34.6, 1.10, "Infrastructure"),
            Instrument("TATAMOTORS", "Tata Motors Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1088.50, 32.70, 3.10, 1096.00, 1060.00, 1062.00, 1055.80, 21500000.0, "₹4.0 Lakh Cr", 14.5, 1.42, "Automobile"),
            Instrument("BAJFINANCE", "Bajaj Finance Ltd", MarketExchange.NSE, AssetClass.EQUITY, 7150.00, 110.00, 1.56, 7190.00, 7050.00, 7070.00, 7040.00, 2800000.0, "₹4.4 Lakh Cr", 29.2, 1.28, "NBFC"),
            Instrument("MARUTI", "Maruti Suzuki India", MarketExchange.NSE, AssetClass.EQUITY, 12420.00, 185.00, 1.51, 12510.00, 12250.00, 12300.00, 12235.00, 1200000.0, "₹3.9 Lakh Cr", 28.5, 0.85, "Automobile"),
            Instrument("SUNPHARMA", "Sun Pharmaceutical", MarketExchange.NSE, AssetClass.EQUITY, 1780.00, -12.00, -0.67, 1798.00, 1772.00, 1795.00, 1792.00, 3100000.0, "₹4.3 Lakh Cr", 36.8, 0.62, "Healthcare"),
            Instrument("TITAN", "Titan Company Ltd", MarketExchange.NSE, AssetClass.EQUITY, 3620.00, 48.00, 1.34, 3645.00, 3580.00, 3590.00, 3572.00, 2200000.0, "₹3.2 Lakh Cr", 82.4, 0.95, "Consumer Goods"),
            Instrument("AXISBANK", "Axis Bank Ltd", MarketExchange.NSE, AssetClass.EQUITY, 1182.00, 12.00, 1.03, 1190.00, 1172.00, 1175.00, 1170.00, 11500000.0, "₹3.6 Lakh Cr", 14.8, 1.12, "Banking"),
            Instrument("KOTAKBANK", "Kotak Mahindra Bank", MarketExchange.NSE, AssetClass.EQUITY, 1810.00, -5.00, -0.28, 1824.00, 1802.00, 1818.00, 1815.00, 6800000.0, "₹3.6 Lakh Cr", 19.5, 0.92, "Banking"),
            Instrument("ADANIENT", "Adani Enterprises", MarketExchange.NSE, AssetClass.EQUITY, 3050.00, 65.00, 2.18, 3085.00, 2990.00, 3000.00, 2985.00, 4500000.0, "₹3.5 Lakh Cr", 88.2, 1.75, "Conglomerate"),
            Instrument("ADANIPORTS", "Adani Ports & SEZ", MarketExchange.NSE, AssetClass.EQUITY, 1490.00, 26.00, 1.78, 1505.00, 1468.00, 1472.00, 1464.00, 5800000.0, "₹3.2 Lakh Cr", 32.4, 1.30, "Logistics"),
            Instrument("WIPRO", "Wipro Ltd", MarketExchange.NSE, AssetClass.EQUITY, 545.00, 8.50, 1.58, 549.00, 538.00, 540.00, 536.50, 12000000.0, "₹2.8 Lakh Cr", 24.5, 0.88, "IT Services"),
            Instrument("HINDUNILVR", "Hindustan Unilever", MarketExchange.NSE, AssetClass.EQUITY, 2740.00, 15.00, 0.55, 2755.00, 2728.00, 2732.00, 2725.00, 3400000.0, "₹6.4 Lakh Cr", 58.2, 0.52, "FMCG"),
            Instrument("TATASTEEL", "Tata Steel Ltd", MarketExchange.NSE, AssetClass.EQUITY, 156.40, 3.20, 2.09, 158.20, 153.50, 154.00, 153.20, 48000000.0, "₹1.9 Lakh Cr", 28.5, 1.48, "Metals"),
            Instrument("COALINDIA", "Coal India Ltd", MarketExchange.NSE, AssetClass.EQUITY, 512.00, 11.50, 2.30, 516.00, 502.00, 504.00, 500.50, 16500000.0, "₹3.1 Lakh Cr", 8.4, 0.85, "Mining / Energy"),
            Instrument("ONGC", "Oil & Natural Gas Corp", MarketExchange.NSE, AssetClass.EQUITY, 328.00, 6.40, 1.99, 331.00, 322.00, 324.00, 321.60, 22000000.0, "₹4.1 Lakh Cr", 7.8, 0.90, "Oil & Gas"),
            Instrument("NTPC", "NTPC Ltd", MarketExchange.NSE, AssetClass.EQUITY, 412.00, 7.80, 1.93, 415.00, 405.00, 407.00, 404.20, 18000000.0, "₹4.0 Lakh Cr", 17.6, 0.78, "Power Utilities"),
            Instrument("POWERGRID", "Power Grid Corp of India", MarketExchange.NSE, AssetClass.EQUITY, 342.00, 4.50, 1.33, 345.00, 338.00, 339.00, 337.50, 14000000.0, "₹3.2 Lakh Cr", 19.8, 0.65, "Power Transmission"),
            Instrument("ZOMATO", "Zomato Ltd", MarketExchange.NSE, AssetClass.EQUITY, 264.00, 9.50, 3.73, 268.00, 255.00, 258.00, 254.50, 62000000.0, "₹2.3 Lakh Cr", 112.0, 1.85, "Consumer Internet"),
            Instrument("JIOFIN", "Jio Financial Services", MarketExchange.NSE, AssetClass.EQUITY, 332.00, 6.20, 1.90, 336.00, 326.00, 328.00, 325.80, 29000000.0, "₹2.1 Lakh Cr", 78.4, 1.40, "Fintech & NBFC"),

            // ===== GLOBAL MARKETS (US, UK, JAPAN, CRYPTO, FOREX) =====
            Instrument("NVDA", "Nvidia Corp", MarketExchange.NASDAQ, AssetClass.EQUITY, 128.60, 4.20, 3.37, 130.20, 125.10, 125.80, 124.40, 68420000.0, "$3.16T", 46.2, 1.68, "Semiconductors"),
            Instrument("AAPL", "Apple Inc", MarketExchange.NASDAQ, AssetClass.EQUITY, 226.40, 1.85, 0.82, 227.80, 224.90, 225.10, 224.55, 45210000.0, "$3.45T", 33.4, 1.05, "Consumer Electronics"),
            Instrument("TSLA", "Tesla Inc", MarketExchange.NASDAQ, AssetClass.EQUITY, 214.20, -3.10, -1.43, 219.50, 212.80, 218.00, 217.30, 72340000.0, "$682B", 62.1, 2.14, "Automotive / AI"),
            Instrument("MSFT", "Microsoft Corp", MarketExchange.NASDAQ, AssetClass.EQUITY, 442.80, 5.40, 1.23, 444.50, 439.10, 439.80, 437.40, 22100000.0, "$3.28T", 35.8, 0.92, "Enterprise Software"),
            Instrument("SPY", "SPDR S&P 500 ETF Trust", MarketExchange.NYSE, AssetClass.ETF, 564.30, 3.80, 0.68, 565.40, 561.90, 562.10, 560.50, 58400000.0, "$580B", 24.1, 1.00, "Broad Market ETF"),
            Instrument("QQQ", "Invesco QQQ Trust", MarketExchange.NASDAQ, AssetClass.ETF, 486.50, 6.20, 1.29, 487.80, 482.10, 483.00, 480.30, 41200000.0, "$290B", 28.5, 1.18, "Tech ETF"),
            Instrument("BTC/USD", "Bitcoin Spot", MarketExchange.BINANCE, AssetClass.CRYPTO, 64250.0, 1420.0, 2.26, 64800.0, 62900.0, 63100.0, 62830.0, 482000.0, "$1.26T", 0.0, 2.40, "Digital Gold"),
            Instrument("ETH/USD", "Ethereum Spot", MarketExchange.BINANCE, AssetClass.CRYPTO, 2780.0, 85.0, 3.15, 2820.0, 2690.0, 2710.0, 2695.0, 1420000.0, "$335B", 0.0, 2.65, "Smart Contract Layer 1"),
            Instrument("EUR/USD", "Euro / US Dollar", MarketExchange.FOREX, AssetClass.FOREX, 1.0875, 0.0032, 0.29, 1.0895, 1.0840, 1.0845, 1.0843, 85000000.0, "N/A", 0.0, 0.35, "Major FX"),
            Instrument("XAU/USD", "Gold Spot (Troy Ounce)", MarketExchange.FOREX, AssetClass.COMMODITY, 2512.40, 18.60, 0.75, 2520.10, 2496.00, 2498.00, 2493.80, 4800000.0, "$16T", 0.0, 0.25, "Precious Metals")
        )

        val enriched = list.map { inst ->
            val candles = getHistoricalCandles(inst.symbol, Timeframe.H1, 60)
            val signals = MarketScanner.scanInstrument(inst, candles)
            inst.copy(primarySignal = signals.firstOrNull())
        }

        _instruments.value = enriched
    }

    fun rescanAllInstruments(): List<Instrument> {
        val updated = _instruments.value.map { inst ->
            val candles = getHistoricalCandles(inst.symbol, Timeframe.H1, 60)
            val signals = MarketScanner.scanInstrument(inst, candles)
            inst.copy(primarySignal = signals.firstOrNull())
        }
        _instruments.value = updated
        return updated
    }

    fun searchInstruments(query: String): List<Instrument> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return _instruments.value
        return _instruments.value.filter {
            it.symbol.lowercase().contains(trimmed) ||
            it.name.lowercase().contains(trimmed) ||
            it.sector.lowercase().contains(trimmed) ||
            it.exchange.code.lowercase().contains(trimmed) ||
            it.assetClass.name.lowercase().contains(trimmed) ||
            (it.primarySignal?.signalType?.title?.lowercase()?.contains(trimmed) == true)
        }
    }

    fun getIndianIndices(): List<Instrument> {
        return _instruments.value.filter { it.exchange.region == MarketRegion.INDIA && it.assetClass == AssetClass.INDEX }
    }

    fun getIndianFOStocks(): List<Instrument> {
        return _instruments.value.filter { it.exchange.region == MarketRegion.INDIA && it.assetClass == AssetClass.EQUITY }
    }

    fun getInstrumentsByRegion(region: MarketRegion?): List<Instrument> {
        val current = _instruments.value
        return if (region == null) current else current.filter { it.exchange.region == region }
    }

    fun getHistoricalCandles(symbol: String, timeframe: Timeframe, count: Int = 120): List<Candle> {
        val cacheKey = "$symbol-${timeframe.code}-$count"
        if (cachedCandles.containsKey(cacheKey)) {
            return cachedCandles[cacheKey]!!
        }

        val basePrice = when {
            symbol.contains("BTC") -> 64000.0
            symbol.contains("ETH") -> 2750.0
            symbol.contains("SENSEX") -> 81400.0
            symbol.contains("BANKEX") -> 58200.0
            symbol.contains("NIFTYNEXT") -> 72400.0
            symbol.contains("NIFTYFMCG") -> 58400.0
            symbol.contains("NIFTYIT") -> 42100.0
            symbol.contains("NIFTYENERGY") -> 39600.0
            symbol.contains("BANKNIFTY") -> 51300.0
            symbol.contains("NIFTYAUTO") -> 25800.0
            symbol.contains("NIFTY 50") || symbol == "NIFTY" -> 24850.0
            symbol.contains("FINNIFTY") -> 23140.0
            symbol.contains("NIFTYPHARMA") -> 22450.0
            symbol.contains("MIDCPNIFTY") -> 12890.0
            symbol.contains("NIFTYMETAL") -> 9420.0
            symbol.contains("MARUTI") -> 12400.0
            symbol.contains("BAJFINANCE") -> 7150.0
            symbol.contains("TCS") -> 4280.0
            symbol.contains("LT") -> 3680.0
            symbol.contains("TITAN") -> 3620.0
            symbol.contains("RELIANCE") || symbol.contains("ADANIENT") -> 3020.0
            symbol.contains("HINDUNILVR") -> 2740.0
            symbol.contains("GOLD") || symbol.contains("XAU") -> 2500.0
            symbol.contains("INFY") || symbol.contains("KOTAKBANK") || symbol.contains("SUNPHARMA") -> 1800.0
            symbol.contains("HDFCBANK") || symbol.contains("BHARTIARTL") || symbol.contains("ADANIPORTS") -> 1550.0
            symbol.contains("ICICIBANK") || symbol.contains("AXISBANK") || symbol.contains("TATAMOTORS") -> 1150.0
            symbol.contains("SBIN") -> 825.0
            symbol.contains("WIPRO") || symbol.contains("ITC") || symbol.contains("COALINDIA") -> 520.0
            symbol.contains("NTPC") || symbol.contains("POWERGRID") || symbol.contains("ONGC") || symbol.contains("JIOFIN") -> 350.0
            symbol.contains("ZOMATO") || symbol.contains("TATASTEEL") -> 200.0
            symbol.contains("EUR/USD") -> 1.0850
            symbol.contains("NVDA") -> 128.0
            symbol.contains("MSFT") -> 440.0
            symbol.contains("AAPL") -> 226.0
            else -> 180.0
        }

        val candles = mutableListOf<Candle>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() - (count * timeframe.minutes * 60 * 1000L)

        var currentPrice = basePrice * 0.85
        val volatility = when {
            symbol.contains("BTC") || symbol.contains("ETH") || symbol.contains("NVDA") || symbol.contains("ZOMATO") -> 0.024
            symbol.contains("EUR/USD") -> 0.003
            symbol.contains("NIFTY") || symbol.contains("SENSEX") || symbol.contains("SPY") -> 0.009
            else -> 0.015
        }

        val random = Random(symbol.hashCode())

        for (i in 0 until count) {
            val drift = (sin(i / 14.0) * 0.004) + 0.0008
            val shock = (random.nextDouble() - 0.48) * volatility
            val open = currentPrice
            val close = open * (1.0 + drift + shock)
            val high = max(open, close) * (1.0 + random.nextDouble() * (volatility * 0.7))
            val low = min(open, close) * (1.0 - random.nextDouble() * (volatility * 0.7))
            val vol = (random.nextDouble() * 500000.0 + 100000.0) * (if (abs(close - open) / open > volatility) 2.2 else 1.0)

            candles.add(
                Candle(
                    timestamp = calendar.timeInMillis,
                    open = (open * 100).toInt() / 100.0,
                    high = (high * 100).toInt() / 100.0,
                    low = (low * 100).toInt() / 100.0,
                    close = (close * 100).toInt() / 100.0,
                    volume = vol
                )
            )
            currentPrice = close
            calendar.add(Calendar.MINUTE, timeframe.minutes)
        }

        cachedCandles[cacheKey] = candles
        return candles
    }

    fun get10YearHistoricalCandles(symbol: String): List<Candle> {
        val cacheKey = "$symbol-10Y-D1"
        if (cachedCandles.containsKey(cacheKey)) {
            return cachedCandles[cacheKey]!!
        }

        val count = 2500
        val basePrice = when {
            symbol.contains("NVDA") -> 8.5
            symbol.contains("BTC") -> 450.0
            symbol.contains("NIFTY 50") || symbol == "NIFTY" -> 8200.0
            symbol.contains("BANKNIFTY") -> 17500.0
            symbol.contains("SENSEX") -> 27000.0
            symbol.contains("RELIANCE") -> 950.0
            symbol.contains("TCS") -> 1200.0
            symbol.contains("AAPL") -> 28.0
            symbol.contains("SPY") -> 205.0
            else -> 45.0
        }

        val targetEndPrice = when {
            symbol.contains("NVDA") -> 128.0
            symbol.contains("BTC") -> 64000.0
            symbol.contains("NIFTY 50") || symbol == "NIFTY" -> 24850.0
            symbol.contains("BANKNIFTY") -> 51340.0
            symbol.contains("SENSEX") -> 81480.0
            symbol.contains("RELIANCE") -> 3014.0
            symbol.contains("TCS") -> 4285.0
            symbol.contains("AAPL") -> 226.0
            symbol.contains("SPY") -> 564.0
            else -> 180.0
        }

        val candles = mutableListOf<Candle>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() - (10L * 365 * 24 * 3600 * 1000L)

        var currentPrice = basePrice
        val random = Random(symbol.hashCode() + 99)
        val compoundDailyFactor = (targetEndPrice / basePrice).let { Math.pow(it, 1.0 / count) }

        for (i in 0 until count) {
            val macroRegime = sin(i / 180.0) * 0.003
            val dailyNoise = (random.nextDouble() - 0.49) * 0.016
            val open = currentPrice
            val close = open * (compoundDailyFactor + macroRegime + dailyNoise)
            val high = max(open, close) * (1.0 + random.nextDouble() * 0.012)
            val low = min(open, close) * (1.0 - random.nextDouble() * 0.012)
            val vol = (random.nextDouble() * 2000000.0 + 500000.0)

            candles.add(
                Candle(
                    timestamp = calendar.timeInMillis,
                    open = (open * 100).toInt() / 100.0,
                    high = (high * 100).toInt() / 100.0,
                    low = (low * 100).toInt() / 100.0,
                    close = (close * 100).toInt() / 100.0,
                    volume = vol
                )
            )
            currentPrice = close
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        cachedCandles[cacheKey] = candles
        return candles
    }

    // ==========================================
    // OPTION CHAIN ENGINE & QUANT GREEKS
    // ==========================================

    fun getOptionChain(symbol: String, selectedExpiry: String? = null): OptionChainData {
        val inst = _instruments.value.find { it.symbol == symbol } ?: _instruments.value.first { it.symbol == "NIFTY 50" }
        val spotPrice = inst.currentPrice

        val (strikeStep, lotSize, expiries) = getOptionContractSpecs(symbol)
        val activeExpiry = selectedExpiry ?: expiries.first()

        // Calculate ATM strike
        val atmStrike = (Math.round(spotPrice / strikeStep) * strikeStep).toDouble()

        val numStrikesAboveBelow = 10
        val strikesList = mutableListOf<OptionChainStrikeRow>()

        var totalCallOI = 0L
        var totalPutOI = 0L
        val random = Random(symbol.hashCode() + activeExpiry.hashCode())

        val daysToExpiry = when {
            activeExpiry.contains("Weekly") || activeExpiry.contains("28-Aug") -> 4.0
            activeExpiry.contains("04-Sep") -> 11.0
            else -> 28.0
        }
        val tYears = max(daysToExpiry / 365.0, 0.01)
        val baseIv = 13.5 + (random.nextDouble() * 2.0)

        for (i in -numStrikesAboveBelow..numStrikesAboveBelow) {
            val strike = atmStrike + (i * strikeStep)
            val isATM = strike == atmStrike

            // Implied Volatility skew (Vol smile)
            val moneyness = (strike - spotPrice) / spotPrice
            val ivPercent = baseIv + (moneyness * moneyness * 120.0) + (if (strike < spotPrice) 1.2 else 0.4)
            val iv = ivPercent / 100.0

            // Black-Scholes Greeks approximation
            val d1 = (ln(spotPrice / strike) + (0.07 + (iv * iv / 2.0)) * tYears) / (iv * sqrt(tYears))
            val d2 = d1 - (iv * sqrt(tYears))

            val nd1 = standardNormalCdf(d1)
            val nd2 = standardNormalCdf(d2)
            val n_minus_d1 = standardNormalCdf(-d1)
            val n_minus_d2 = standardNormalCdf(-d2)

            val discountFactor = exp(-0.07 * tYears)

            // Theoretical Option Pricing
            var callPrice = (spotPrice * nd1) - (strike * discountFactor * nd2)
            var putPrice = (strike * discountFactor * n_minus_d2) - (spotPrice * n_minus_d1)

            callPrice = max(callPrice, if (spotPrice > strike) spotPrice - strike else 0.50)
            putPrice = max(putPrice, if (strike > spotPrice) strike - spotPrice else 0.50)

            // Greeks
            val callDelta = nd1
            val putDelta = nd1 - 1.0
            val gamma = standardNormalPdf(d1) / (spotPrice * iv * sqrt(tYears))
            val callTheta = -((spotPrice * standardNormalPdf(d1) * iv) / (2 * sqrt(tYears)) + 0.07 * strike * discountFactor * nd2) / 365.0
            val putTheta = -((spotPrice * standardNormalPdf(d1) * iv) / (2 * sqrt(tYears)) - 0.07 * strike * discountFactor * n_minus_d2) / 365.0
            val vega = spotPrice * sqrt(tYears) * standardNormalPdf(d1) / 100.0

            // Open Interest distribution
            val distFromAtm = abs(strike - atmStrike) / strikeStep
            val baseOiScale = (1200000.0 / (1.0 + distFromAtm * 0.45)).toLong()
            val callOi = (baseOiScale * (0.8 + random.nextDouble() * 0.6) * (if (strike >= atmStrike) 1.4 else 0.7)).toLong()
            val putOi = (baseOiScale * (0.8 + random.nextDouble() * 0.6) * (if (strike <= atmStrike) 1.5 else 0.65)).toLong()

            val callChgOi = (callOi * (random.nextDouble() * 0.22 - 0.08)).toLong()
            val putChgOi = (putOi * (random.nextDouble() * 0.24 - 0.06)).toLong()

            val callVol = (callOi * (0.6 + random.nextDouble() * 0.8)).toLong()
            val putVol = (putOi * (0.6 + random.nextDouble() * 0.8)).toLong()

            totalCallOI += callOi
            totalPutOI += putOi

            val callContract = OptionContract(
                symbol = "${symbol.replace(" ", "")} ${strike.toInt()} CE",
                underlyingSymbol = symbol,
                strikePrice = strike,
                optionType = OptionType.CALL,
                expiryDate = activeExpiry,
                ltp = (callPrice * 100).toInt() / 100.0,
                change = (random.nextDouble() * 8.0 - 2.5),
                changePercent = (random.nextDouble() * 22.0 - 6.0),
                bid = (callPrice * 0.995 * 100).toInt() / 100.0,
                ask = (callPrice * 1.005 * 100).toInt() / 100.0,
                volume = callVol,
                openInterest = callOi,
                changeInOI = callChgOi,
                greeks = OptionGreeks(callDelta, gamma, callTheta, vega, ivPercent),
                lotSize = lotSize,
                isATM = isATM,
                isITM = strike < spotPrice
            )

            val putContract = OptionContract(
                symbol = "${symbol.replace(" ", "")} ${strike.toInt()} PE",
                underlyingSymbol = symbol,
                strikePrice = strike,
                optionType = OptionType.PUT,
                expiryDate = activeExpiry,
                ltp = (putPrice * 100).toInt() / 100.0,
                change = (random.nextDouble() * 8.0 - 4.5),
                changePercent = (random.nextDouble() * 20.0 - 10.0),
                bid = (putPrice * 0.995 * 100).toInt() / 100.0,
                ask = (putPrice * 1.005 * 100).toInt() / 100.0,
                volume = putVol,
                openInterest = putOi,
                changeInOI = putChgOi,
                greeks = OptionGreeks(putDelta, gamma, putTheta, vega, ivPercent),
                lotSize = lotSize,
                isATM = isATM,
                isITM = strike > spotPrice
            )

            strikesList.add(
                OptionChainStrikeRow(
                    strikePrice = strike,
                    isATM = isATM,
                    call = callContract,
                    put = putContract
                )
            )
        }

        val pcr = if (totalCallOI > 0) (totalPutOI.toDouble() / totalCallOI.toDouble()).let { (it * 100).toInt() / 100.0 } else 1.0

        // Calculate Max Pain strike (strike where option buyers lose maximum money)
        var minTotalLoss = Double.MAX_VALUE
        var maxPain = atmStrike
        strikesList.forEach { targetRow ->
            val target = targetRow.strikePrice
            var cumulativeLoss = 0.0
            strikesList.forEach { row ->
                // Call buyer payoff if expires at target
                if (target > row.strikePrice) {
                    cumulativeLoss += (target - row.strikePrice) * row.call.openInterest
                }
                // Put buyer payoff if expires at target
                if (target < row.strikePrice) {
                    cumulativeLoss += (row.strikePrice - target) * row.put.openInterest
                }
            }
            if (cumulativeLoss < minTotalLoss) {
                minTotalLoss = cumulativeLoss
                maxPain = target
            }
        }

        return OptionChainData(
            underlyingSymbol = inst.symbol,
            underlyingName = inst.name,
            underlyingPrice = spotPrice,
            underlyingChange = inst.changeAmount,
            underlyingChangePercent = inst.changePercent,
            expiryDate = activeExpiry,
            availableExpiries = expiries,
            strikes = strikesList,
            totalCallOI = totalCallOI,
            totalPutOI = totalPutOI,
            pcr = pcr,
            maxPainStrike = maxPain,
            indiaVix = 13.45,
            lotSize = lotSize
        )
    }

    private fun getOptionContractSpecs(symbol: String): Triple<Double, Int, List<String>> {
        val expiries = listOf("28-Aug-2026 (Weekly)", "04-Sep-2026 (Next Wk)", "25-Sep-2026 (Monthly)")
        return when {
            symbol.contains("NIFTY 50") || symbol == "NIFTY" -> Triple(50.0, 75, expiries)
            symbol.contains("BANKNIFTY") -> Triple(100.0, 30, expiries)
            symbol.contains("FINNIFTY") -> Triple(50.0, 65, expiries)
            symbol.contains("MIDCPNIFTY") -> Triple(25.0, 120, expiries)
            symbol.contains("SENSEX") -> Triple(100.0, 20, expiries)
            symbol.contains("BANKEX") -> Triple(100.0, 30, expiries)
            symbol.contains("NIFTYNEXT") || symbol.contains("NIFTYIT") || symbol.contains("NIFTYFMCG") -> Triple(100.0, 50, expiries)
            symbol.contains("RELIANCE") || symbol.contains("TCS") || symbol.contains("LT") || symbol.contains("BAJFINANCE") -> Triple(20.0, 250, expiries)
            symbol.contains("MARUTI") -> Triple(100.0, 50, expiries)
            symbol.contains("HDFCBANK") || symbol.contains("ICICIBANK") || symbol.contains("INFY") || symbol.contains("TATAMOTORS") || symbol.contains("SBIN") -> Triple(10.0, 550, expiries)
            symbol.contains("ITC") || symbol.contains("WIPRO") || symbol.contains("COALINDIA") -> Triple(5.0, 1600, expiries)
            symbol.contains("ZOMATO") || symbol.contains("TATASTEEL") -> Triple(2.5, 3000, expiries)
            else -> Triple(10.0, 500, expiries)
        }
    }

    fun calculateStrategyPayoff(strategyType: OptionStrategyType, underlyingSymbol: String): OptionStrategyPayoff {
        val chain = getOptionChain(underlyingSymbol)
        val spotPrice = chain.underlyingPrice
        val (strikeStep, lotSize, _) = getOptionContractSpecs(underlyingSymbol)
        val atmStrike = (Math.round(spotPrice / strikeStep) * strikeStep).toDouble()

        val atmRow = chain.strikes.find { it.strikePrice == atmStrike } ?: chain.strikes[chain.strikes.size / 2]
        val otmCallRow = chain.strikes.find { it.strikePrice == atmStrike + strikeStep } ?: atmRow
        val farOtmCallRow = chain.strikes.find { it.strikePrice == atmStrike + (2 * strikeStep) } ?: otmCallRow
        val otmPutRow = chain.strikes.find { it.strikePrice == atmStrike - strikeStep } ?: atmRow
        val farOtmPutRow = chain.strikes.find { it.strikePrice == atmStrike - (2 * strikeStep) } ?: otmPutRow

        val legs = mutableListOf<StrategyLeg>()

        when (strategyType) {
            OptionStrategyType.LONG_CALL -> {
                legs.add(StrategyLeg(atmRow.call, OrderSide.BUY, 1))
            }
            OptionStrategyType.LONG_PUT -> {
                legs.add(StrategyLeg(atmRow.put, OrderSide.BUY, 1))
            }
            OptionStrategyType.BULL_CALL_SPREAD -> {
                legs.add(StrategyLeg(atmRow.call, OrderSide.BUY, 1))
                legs.add(StrategyLeg(otmCallRow.call, OrderSide.SELL, 1))
            }
            OptionStrategyType.BEAR_PUT_SPREAD -> {
                legs.add(StrategyLeg(atmRow.put, OrderSide.BUY, 1))
                legs.add(StrategyLeg(otmPutRow.put, OrderSide.SELL, 1))
            }
            OptionStrategyType.SHORT_STRADDLE -> {
                legs.add(StrategyLeg(atmRow.call, OrderSide.SELL, 1))
                legs.add(StrategyLeg(atmRow.put, OrderSide.SELL, 1))
            }
            OptionStrategyType.SHORT_STRANGLE -> {
                legs.add(StrategyLeg(otmCallRow.call, OrderSide.SELL, 1))
                legs.add(StrategyLeg(otmPutRow.put, OrderSide.SELL, 1))
            }
            OptionStrategyType.IRON_CONDOR -> {
                legs.add(StrategyLeg(farOtmPutRow.put, OrderSide.BUY, 1))
                legs.add(StrategyLeg(otmPutRow.put, OrderSide.SELL, 1))
                legs.add(StrategyLeg(otmCallRow.call, OrderSide.SELL, 1))
                legs.add(StrategyLeg(farOtmCallRow.call, OrderSide.BUY, 1))
            }
            OptionStrategyType.IRON_BUTTERFLY -> {
                legs.add(StrategyLeg(farOtmPutRow.put, OrderSide.BUY, 1))
                legs.add(StrategyLeg(atmRow.put, OrderSide.SELL, 1))
                legs.add(StrategyLeg(atmRow.call, OrderSide.SELL, 1))
                legs.add(StrategyLeg(farOtmCallRow.call, OrderSide.BUY, 1))
            }
        }

        // Net debit / credit in points & Rupees
        var netPoints = 0.0
        var netDelta = 0.0
        var netTheta = 0.0

        legs.forEach { leg ->
            val sign = if (leg.action == OrderSide.BUY) -1.0 else 1.0
            netPoints += sign * leg.optionContract.ltp * leg.lots
            netDelta += sign * leg.optionContract.greeks.delta * leg.lots * lotSize
            netTheta += sign * leg.optionContract.greeks.theta * leg.lots * lotSize
        }

        val netAmount = netPoints * lotSize

        // Generate Payoff curve across a range of expiry prices (spot ± 8%)
        val rangeMin = spotPrice * 0.92
        val rangeMax = spotPrice * 1.08
        val steps = 50
        val stepSize = (rangeMax - rangeMin) / steps

        val payoffPoints = mutableListOf<Pair<Double, Double>>()
        var maxProfit = -Double.MAX_VALUE
        var maxLoss = Double.MAX_VALUE
        val breakevens = mutableListOf<Double>()
        var prevPnl: Double? = null

        for (s in 0..steps) {
            val expiryPrice = rangeMin + (s * stepSize)
            var totalPnl = 0.0

            legs.forEach { leg ->
                val contract = leg.optionContract
                val payoffAtExpiry = if (contract.optionType == OptionType.CALL) {
                    max(0.0, expiryPrice - contract.strikePrice)
                } else {
                    max(0.0, contract.strikePrice - expiryPrice)
                }

                val pnlPerUnit = if (leg.action == OrderSide.BUY) {
                    payoffAtExpiry - contract.ltp
                } else {
                    contract.ltp - payoffAtExpiry
                }

                totalPnl += pnlPerUnit * leg.lots * lotSize
            }

            payoffPoints.add(Pair(expiryPrice, totalPnl))
            maxProfit = max(maxProfit, totalPnl)
            maxLoss = min(maxLoss, totalPnl)

            if (prevPnl != null) {
                if ((prevPnl < 0 && totalPnl >= 0) || (prevPnl > 0 && totalPnl <= 0)) {
                    breakevens.add(expiryPrice)
                }
            }
            prevPnl = totalPnl
        }

        val marginRequired = when (strategyType) {
            OptionStrategyType.LONG_CALL, OptionStrategyType.LONG_PUT -> abs(netAmount)
            OptionStrategyType.BULL_CALL_SPREAD, OptionStrategyType.BEAR_PUT_SPREAD -> abs(netAmount)
            OptionStrategyType.SHORT_STRADDLE, OptionStrategyType.SHORT_STRANGLE -> spotPrice * lotSize * 0.15
            OptionStrategyType.IRON_CONDOR, OptionStrategyType.IRON_BUTTERFLY -> strikeStep * lotSize
        }

        val riskReward = if (maxLoss != 0.0) "1 : ${String.format("%.2f", abs(maxProfit / maxLoss))}" else "Undefined"

        return OptionStrategyPayoff(
            strategyType = strategyType,
            underlyingSymbol = underlyingSymbol,
            legs = legs,
            netDebitOrCredit = netAmount,
            maxProfit = maxProfit,
            maxLoss = maxLoss,
            breakevens = breakevens.distinct(),
            riskReward = riskReward,
            netDelta = netDelta,
            netTheta = netTheta,
            marginRequired = marginRequired,
            payoffPoints = payoffPoints
        )
    }

    fun getOIBuildupScans(): List<OIBuildupItem> {
        val indianList = _instruments.value.filter { it.exchange.region == MarketRegion.INDIA }
        val random = Random(42)

        return indianList.mapIndexed { index, inst ->
            val isPriceUp = inst.changePercent >= 0
            val oiChange = (random.nextDouble() * 26.0 - 8.0)
            val isOiUp = oiChange >= 0

            val buildup = when {
                isPriceUp && isOiUp -> OIBuildupType.LONG_BUILDUP
                !isPriceUp && isOiUp -> OIBuildupType.SHORT_BUILDUP
                isPriceUp && !isOiUp -> OIBuildupType.SHORT_COVERING
                else -> OIBuildupType.LONG_UNWINDING
            }

            val pcr = (0.75 + random.nextDouble() * 0.85).let { (it * 100).toInt() / 100.0 }
            val oi = (random.nextLong(1500000, 24000000))

            OIBuildupItem(
                symbol = inst.symbol,
                name = inst.name,
                ltp = inst.currentPrice,
                changePercent = inst.changePercent,
                openInterest = oi,
                oiChangePercent = (oiChange * 10).toInt() / 10.0,
                pcr = pcr,
                buildupType = buildup,
                exchange = inst.exchange
            )
        }
    }

    private fun standardNormalPdf(x: Double): Double {
        return (1.0 / sqrt(2.0 * Math.PI)) * exp(-0.5 * x * x)
    }

    private fun standardNormalCdf(x: Double): Double {
        val b1 = 0.319381530
        val b2 = -0.356563782
        val b3 = 1.781477937
        val b4 = -1.821255978
        val b5 = 1.330274429
        val p = 0.2316419
        val c = 0.39894228

        if (x >= 0.0) {
            val t = 1.0 / (1.0 + p * x)
            return (1.0 - c * exp(-x * x / 2.0) * t * (t * (t * (t * (t * b5 + b4) + b3) + b2) + b1))
        } else {
            val t = 1.0 / (1.0 - p * x)
            return (c * exp(-x * x / 2.0) * t * (t * (t * (t * (t * b5 + b4) + b3) + b2) + b1))
        }
    }
}
