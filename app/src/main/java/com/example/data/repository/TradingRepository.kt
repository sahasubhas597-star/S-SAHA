package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BrokerAccountEntity
import com.example.data.model.BrokerType
import com.example.data.model.PaperTradeEntity
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.StrategyEntity
import com.example.data.model.TradingStrategy
import com.example.data.model.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TradingRepository(private val database: AppDatabase) {

    val strategies: Flow<List<StrategyEntity>> = database.strategyDao().getAllStrategies()
    val paperTrades: Flow<List<PaperTradeEntity>> = database.paperTradeDao().getAllTrades()
    val portfolioPositions: Flow<List<PortfolioPositionEntity>> = database.portfolioDao().getAllPositions()
    val brokers: Flow<List<BrokerAccountEntity>> = database.brokerDao().getAllBrokers()
    val watchlist: Flow<List<WatchlistItemEntity>> = database.watchlistDao().getWatchlist()

    suspend fun saveStrategy(strategy: StrategyEntity) {
        database.strategyDao().insertStrategy(strategy)
    }

    suspend fun deleteStrategy(id: String) {
        database.strategyDao().deleteStrategyById(id)
    }

    suspend fun placePaperTrade(trade: PaperTradeEntity) {
        database.paperTradeDao().insertTrade(trade)
    }

    suspend fun closePaperTrade(id: String, exitPrice: Double) {
        val all = database.paperTradeDao().getAllTrades()
        // Simple update
        database.paperTradeDao().deleteTradeById(id)
    }

    suspend fun clearPaperTrades() {
        database.paperTradeDao().clearAllTrades()
    }

    suspend fun savePortfolioPositions(positions: List<PortfolioPositionEntity>) {
        database.portfolioDao().insertPositions(positions)
    }

    suspend fun saveBroker(broker: BrokerAccountEntity) {
        database.brokerDao().insertBroker(broker)
    }

    suspend fun updateBrokerStatus(id: String, isConnected: Boolean, latency: Long) {
        database.brokerDao().updateConnectionStatus(id, isConnected, latency)
    }

    suspend fun deleteBroker(id: String) {
        database.brokerDao().deleteBroker(id)
    }

    suspend fun toggleWatchlist(symbol: String, name: String, exchange: String, assetClass: String) {
        database.watchlistDao().addToWatchlist(
            WatchlistItemEntity(symbol, exchange, name, assetClass)
        )
    }

    suspend fun removeFromWatchlist(symbol: String) {
        database.watchlistDao().removeFromWatchlist(symbol)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Seed default institutional strategies
        database.strategyDao().insertStrategy(
            StrategyEntity(
                id = "strat_momentum_alpha",
                name = "Adaptive Momentum Alpha v4",
                description = "EMA 20/50 dynamic trend alignment with RSI filter (48-75) and ATR trailing profit lock.",
                targetAssetClass = "EQUITY",
                defaultTimeframe = "1h",
                stopLossPercent = 2.0,
                takeProfitPercent = 5.5,
                trailingStopPercent = 1.5,
                maxRiskPerTradePercent = 1.0,
                maxOpenPositions = 5,
                isAutoTradingEnabled = true
            )
        )
        database.strategyDao().insertStrategy(
            StrategyEntity(
                id = "strat_trapped_trader",
                name = "Institutional Trapped Trader Pro",
                description = "Exploits liquidity sweeps and failed breakouts at 20-period swing highs/lows with volume confirmation.",
                targetAssetClass = "EQUITY",
                defaultTimeframe = "15m",
                stopLossPercent = 1.8,
                takeProfitPercent = 4.8,
                trailingStopPercent = 1.2,
                maxRiskPerTradePercent = 1.5,
                maxOpenPositions = 4,
                isAutoTradingEnabled = false
            )
        )
        database.strategyDao().insertStrategy(
            StrategyEntity(
                id = "strat_vol_mean_reversion",
                name = "Bollinger Volatility Mean Reversion",
                description = "Enters on 2.0-sigma band touches with RSI extreme divergence; exits at dynamic VWAP / SMA 20 baseline.",
                targetAssetClass = "FUTURES",
                defaultTimeframe = "5m",
                stopLossPercent = 1.5,
                takeProfitPercent = 3.2,
                trailingStopPercent = 1.0,
                maxRiskPerTradePercent = 1.0,
                maxOpenPositions = 3,
                isAutoTradingEnabled = true
            )
        )

        // Seed default portfolio positions
        database.portfolioDao().insertPositions(
            listOf(
                PortfolioPositionEntity("NVDA", "Nvidia Corp", "NASDAQ", "EQUITY", 150.0, 114.20, 128.60, 20.0, 19.3, "Semiconductors", 1.68),
                PortfolioPositionEntity("RELIANCE", "Reliance Industries", "NSE", "EQUITY", 200.0, 2890.0, 3012.50, 15.0, 16.2, "Energy & Conglomerate", 0.88),
                PortfolioPositionEntity("BTC/USD", "Bitcoin Spot", "BINANCE", "CRYPTO", 0.45, 58200.0, 64250.0, 12.0, 14.5, "Crypto", 2.40),
                PortfolioPositionEntity("SPY", "SPDR S&P 500 ETF", "NYSE", "ETF", 60.0, 535.0, 564.30, 25.0, 24.1, "Broad ETF", 1.00),
                PortfolioPositionEntity("TCS", "Tata Consultancy", "NSE", "EQUITY", 80.0, 3950.0, 4280.00, 10.0, 11.4, "IT Services", 0.72),
                PortfolioPositionEntity("XAU/USD", "Gold Spot", "FOREX", "COMMODITY", 12.0, 2380.0, 2512.40, 10.0, 10.1, "Precious Metals", 0.25)
            )
        )

        // Seed default paper trades
        val now = System.currentTimeMillis()
        database.paperTradeDao().insertTrade(
            PaperTradeEntity(
                id = UUID.randomUUID().toString(),
                symbol = "NVDA",
                exchangeName = "NASDAQ",
                side = "BUY",
                orderType = "MARKET",
                quantity = 50.0,
                entryPrice = 124.50,
                currentPrice = 128.60,
                stopLoss = 121.80,
                takeProfit = 132.00,
                status = "OPEN",
                unrealizedPnl = (128.60 - 124.50) * 50.0,
                realizedPnl = 0.0,
                openedAt = now - 3600000L * 4,
                strategyName = "Adaptive Momentum Alpha v4"
            )
        )
        database.paperTradeDao().insertTrade(
            PaperTradeEntity(
                id = UUID.randomUUID().toString(),
                symbol = "NIFTY 50",
                exchangeName = "NSE",
                side = "BUY",
                orderType = "LIMIT",
                quantity = 75.0,
                entryPrice = 24680.0,
                currentPrice = 24850.0,
                stopLoss = 24450.0,
                takeProfit = 25100.0,
                status = "OPEN",
                unrealizedPnl = (24850.0 - 24680.0) * 75.0,
                realizedPnl = 0.0,
                openedAt = now - 3600000L * 7,
                strategyName = "Institutional Trapped Trader Pro"
            )
        )

        // Seed default brokers
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_zerodha",
                brokerType = BrokerType.ZERODHA.name,
                accountName = "Zerodha Kite #ZF8192",
                apiKeyMasked = "kite_••••••••••••4431",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 5000000.0,
                pingLatencyMs = 24,
                lastSyncTime = now - 120000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_groww",
                brokerType = BrokerType.GROWW.name,
                accountName = "Groww Trading #GW7182",
                apiKeyMasked = "gw_live_••••••••••••9102",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 3000000.0,
                pingLatencyMs = 28,
                lastSyncTime = now - 180000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_dhan",
                brokerType = BrokerType.DHAN.name,
                accountName = "Dhan Lightning #DH1109",
                apiKeyMasked = "dhan_••••••••••••3318",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 5000000.0,
                pingLatencyMs = 16,
                lastSyncTime = now - 60000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_angel",
                brokerType = BrokerType.ANGEL_ONE.name,
                accountName = "Angel One SmartAPI #A9941",
                apiKeyMasked = "angel_••••••••••••6629",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 4000000.0,
                pingLatencyMs = 22,
                lastSyncTime = now - 90000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_upstox",
                brokerType = BrokerType.UPSTOX.name,
                accountName = "Upstox Pro API #UP5521",
                apiKeyMasked = "upstox_••••••••••••1102",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 3500000.0,
                pingLatencyMs = 20,
                lastSyncTime = now - 150000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_ibkr",
                brokerType = BrokerType.INTERACTIVE_BROKERS.name,
                accountName = "IBKR Pro Institutional #U992811",
                apiKeyMasked = "ib_live_••••••••••••8912",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 250000.0,
                pingLatencyMs = 21,
                lastSyncTime = now
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_alpaca",
                brokerType = BrokerType.ALPACA.name,
                accountName = "Alpaca Paper & Algo #PK991",
                apiKeyMasked = "pk_test_••••••••••••7721",
                environment = "SANDBOX",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 100000.0,
                pingLatencyMs = 15,
                lastSyncTime = now - 60000L
            )
        )
        database.brokerDao().insertBroker(
            BrokerAccountEntity(
                id = "broker_binance",
                brokerType = BrokerType.BINANCE.name,
                accountName = "Binance Futures API #BN204",
                apiKeyMasked = "binance_••••••••••••8819",
                environment = "PRODUCTION",
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 150000.0,
                pingLatencyMs = 12,
                lastSyncTime = now - 30000L
            )
        )
    }

    suspend fun addNewBroker(broker: BrokerAccountEntity) {
        database.brokerDao().insertBroker(broker)
    }
}
