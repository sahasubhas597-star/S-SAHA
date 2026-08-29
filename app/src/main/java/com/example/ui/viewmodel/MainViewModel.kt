package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AssetClass
import com.example.data.model.BacktestResult
import com.example.data.model.BrokerAccountEntity
import com.example.data.model.BrokerType
import com.example.data.model.Candle
import com.example.data.model.DispatchedSandboxOrder
import com.example.data.model.ExecutionMode
import com.example.data.model.Instrument
import com.example.data.model.MarketExchange
import com.example.data.model.MarketRegion
import com.example.data.model.OIBuildupItem
import com.example.data.model.OIBuildupType
import com.example.data.model.OptionChainData
import com.example.data.model.OptionContract
import com.example.data.model.OptionStrategyPayoff
import com.example.data.model.OptionStrategyType
import com.example.data.model.OptionType
import com.example.data.model.OrderSide
import com.example.data.model.OrderType
import com.example.data.model.PaperTradeEntity
import com.example.data.model.PortfolioPositionEntity
import com.example.data.model.PortfolioSummary
import com.example.data.model.SignalAlert
import com.example.data.model.SignalType
import com.example.data.model.StrategyEntity
import com.example.data.model.StrategyLeg
import com.example.data.model.Timeframe
import com.example.data.model.TradingStrategy
import com.example.data.repository.AiCopilotRepository
import com.example.data.repository.MarketDataRepository
import com.example.data.repository.TradingRepository
import com.example.engine.backtest.BacktestEngine
import com.example.engine.backtest.PortfolioOptimizer
import com.example.engine.scanner.MarketScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val marketRepo = MarketDataRepository()
    private val tradingRepo = TradingRepository(database)
    private val aiRepo = AiCopilotRepository()

    // Navigation & View State
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedRegion = MutableStateFlow<MarketRegion?>(null)
    val selectedRegion: StateFlow<MarketRegion?> = _selectedRegion.asStateFlow()

    private val _selectedInstrument = MutableStateFlow<Instrument>(
        Instrument("NVDA", "Nvidia Corp", MarketExchange.NASDAQ, AssetClass.EQUITY, 128.60, 4.20, 3.37, 130.20, 125.10, 125.80, 124.40, 68420000.0, "3.16T", 46.2, 1.68, "Semiconductors")
    )
    val selectedInstrument: StateFlow<Instrument> = _selectedInstrument.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.H1)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    // Market Instruments & Candles
    val instruments: StateFlow<List<Instrument>> = marketRepo.instruments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCandles = MutableStateFlow<List<Candle>>(emptyList())
    val currentCandles: StateFlow<List<Candle>> = _currentCandles.asStateFlow()

    private val _detectedSignals = MutableStateFlow<List<SignalAlert>>(emptyList())
    val detectedSignals: StateFlow<List<SignalAlert>> = _detectedSignals.asStateFlow()

    // Strategies
    val strategies: StateFlow<List<StrategyEntity>> = tradingRepo.strategies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeStrategy = MutableStateFlow(
        TradingStrategy(
            id = "strat_momentum_alpha",
            name = "Adaptive Momentum Alpha v4",
            description = "EMA 20/50 dynamic trend alignment with RSI filter (48-75) and ATR trailing profit lock.",
            targetAssetClass = AssetClass.EQUITY,
            defaultTimeframe = Timeframe.H1,
            stopLossPercent = 2.0,
            takeProfitPercent = 5.5,
            trailingStopPercent = 1.5,
            maxRiskPerTradePercent = 1.0,
            maxOpenPositions = 5,
            isAutoTradingEnabled = true
        )
    )
    val activeStrategy: StateFlow<TradingStrategy> = _activeStrategy.asStateFlow()

    // Backtest
    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult: StateFlow<BacktestResult?> = _backtestResult.asStateFlow()

    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    // Paper Trading & Portfolio
    val paperTrades: StateFlow<List<PaperTradeEntity>> = tradingRepo.paperTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioPositions: StateFlow<List<PortfolioPositionEntity>> = tradingRepo.portfolioPositions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _portfolioSummary = MutableStateFlow<PortfolioSummary?>(null)
    val portfolioSummary: StateFlow<PortfolioSummary?> = _portfolioSummary.asStateFlow()

    val brokers: StateFlow<List<BrokerAccountEntity>> = tradingRepo.brokers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Broker Master Session State
    private val _isLiveBrokerSessionActive = MutableStateFlow(true)
    val isLiveBrokerSessionActive: StateFlow<Boolean> = _isLiveBrokerSessionActive.asStateFlow()

    private val _liveSessionToken = MutableStateFlow("ZX26-AUTH-LIVE-992102")
    val liveSessionToken: StateFlow<String> = _liveSessionToken.asStateFlow()

    private val _liveSessionStatusMessage = MutableStateFlow("OMS WebSocket Handshake Verified • Real-Time Order Routing Active")
    val liveSessionStatusMessage: StateFlow<String> = _liveSessionStatusMessage.asStateFlow()

    private val _isSessionConnecting = MutableStateFlow(false)
    val isSessionConnecting: StateFlow<Boolean> = _isSessionConnecting.asStateFlow()

    // AI Copilot
    private val _aiReport = MutableStateFlow("")
    val aiReport: StateFlow<String> = _aiReport.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Screener filter & Search State
    private val _scannerFilterExchange = MutableStateFlow<MarketExchange?>(null)
    val scannerFilterExchange: StateFlow<MarketExchange?> = _scannerFilterExchange.asStateFlow()

    private val _scannerFilterSignalType = MutableStateFlow<SignalType?>(null)
    val scannerFilterSignalType: StateFlow<SignalType?> = _scannerFilterSignalType.asStateFlow()

    private val _scannerSearchQuery = MutableStateFlow("")
    val scannerSearchQuery: StateFlow<String> = _scannerSearchQuery.asStateFlow()

    private val _feedSearchQuery = MutableStateFlow("")
    val feedSearchQuery: StateFlow<String> = _feedSearchQuery.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Indian Derivatives & Option Chain State
    private val _selectedOptionUnderlying = MutableStateFlow("NIFTY 50")
    val selectedOptionUnderlying: StateFlow<String> = _selectedOptionUnderlying.asStateFlow()

    private val _selectedOptionExpiry = MutableStateFlow("28-Aug-2026 (Weekly)")
    val selectedOptionExpiry: StateFlow<String> = _selectedOptionExpiry.asStateFlow()

    private val _optionChainData = MutableStateFlow<OptionChainData?>(null)
    val optionChainData: StateFlow<OptionChainData?> = _optionChainData.asStateFlow()

    private val _selectedOptionStrategyType = MutableStateFlow(OptionStrategyType.BATMAN_STRATEGY)
    val selectedOptionStrategyType: StateFlow<OptionStrategyType> = _selectedOptionStrategyType.asStateFlow()

    private val _optionStrategyPayoff = MutableStateFlow<OptionStrategyPayoff?>(null)
    val optionStrategyPayoff: StateFlow<OptionStrategyPayoff?> = _optionStrategyPayoff.asStateFlow()

    private val _oiBuildupScans = MutableStateFlow<List<OIBuildupItem>>(emptyList())
    val oiBuildupScans: StateFlow<List<OIBuildupItem>> = _oiBuildupScans.asStateFlow()

    private val _oiBuildupFilter = MutableStateFlow<OIBuildupType?>(null)
    val oiBuildupFilter: StateFlow<OIBuildupType?> = _oiBuildupFilter.asStateFlow()

    private val _dispatchedOrders = MutableStateFlow<List<DispatchedSandboxOrder>>(emptyList())
    val dispatchedOrders: StateFlow<List<DispatchedSandboxOrder>> = _dispatchedOrders.asStateFlow()

    private val _lastDispatchedOrderResult = MutableStateFlow<DispatchedSandboxOrder?>(null)
    val lastDispatchedOrderResult: StateFlow<DispatchedSandboxOrder?> = _lastDispatchedOrderResult.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.seedInitialDataIfEmpty()
            seedInitialDispatchedOrders()
            loadCandlesForSelected()
            recalculatePortfolioSummary()
            runInitialBacktest()
            refreshOptionChain()
            refreshOIBuildups()
        }
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setRegion(region: MarketRegion?) {
        _selectedRegion.value = region
    }

    fun setScannerFilterExchange(ex: MarketExchange?) {
        _scannerFilterExchange.value = ex
    }

    fun setScannerFilterSignalType(signal: SignalType?) {
        _scannerFilterSignalType.value = signal
    }

    fun setFeedSearchQuery(query: String) {
        _feedSearchQuery.value = query
    }

    fun clearFeedSearchQuery() {
        _feedSearchQuery.value = ""
    }

    fun setScannerSearchQuery(query: String) {
        _scannerSearchQuery.value = query
    }

    fun clearScannerSearchQuery() {
        _scannerSearchQuery.value = ""
    }

    fun searchAndSelectInstrument(query: String): Boolean {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return false
        val match = instruments.value.find {
            it.symbol.equals(trimmed, ignoreCase = true)
        } ?: instruments.value.find {
            it.symbol.lowercase().contains(trimmed) || it.name.lowercase().contains(trimmed)
        }

        if (match != null) {
            selectInstrument(match)
            _feedSearchQuery.value = match.symbol
            return true
        }
        return false
    }

    fun triggerFullMarketScan() {
        viewModelScope.launch(Dispatchers.Default) {
            _isScanning.value = true
            kotlinx.coroutines.delay(600) // Visual feedback for scanner computation
            marketRepo.rescanAllInstruments()
            loadCandlesForSelected()
            _isScanning.value = false
        }
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        loadCandlesForSelected()
    }

    fun setTimeframe(tf: Timeframe) {
        _selectedTimeframe.value = tf
        loadCandlesForSelected()
    }

    private fun loadCandlesForSelected() {
        val symbol = _selectedInstrument.value.symbol
        val tf = _selectedTimeframe.value
        val candles = marketRepo.getHistoricalCandles(symbol, tf, 80)
        _currentCandles.value = candles

        val signals = MarketScanner.scanInstrument(_selectedInstrument.value, candles)
        _detectedSignals.value = signals
    }

    fun updateActiveStrategy(strategy: TradingStrategy) {
        _activeStrategy.value = strategy
    }

    fun saveStrategy(strategy: TradingStrategy) {
        _activeStrategy.value = strategy
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.saveStrategy(
                StrategyEntity(
                    id = strategy.id,
                    name = strategy.name,
                    description = strategy.description,
                    targetAssetClass = strategy.targetAssetClass.name,
                    defaultTimeframe = strategy.defaultTimeframe.code,
                    stopLossPercent = strategy.stopLossPercent,
                    takeProfitPercent = strategy.takeProfitPercent,
                    trailingStopPercent = strategy.trailingStopPercent,
                    maxRiskPerTradePercent = strategy.maxRiskPerTradePercent,
                    maxOpenPositions = strategy.maxOpenPositions,
                    isAutoTradingEnabled = strategy.isAutoTradingEnabled
                )
            )
        }
    }

    fun selectStrategy(entity: StrategyEntity) {
        val strat = TradingStrategy(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            targetAssetClass = try { AssetClass.valueOf(entity.targetAssetClass) } catch (e: Exception) { AssetClass.EQUITY },
            defaultTimeframe = Timeframe.values().firstOrNull { it.code == entity.defaultTimeframe } ?: Timeframe.H1,
            stopLossPercent = entity.stopLossPercent,
            takeProfitPercent = entity.takeProfitPercent,
            trailingStopPercent = entity.trailingStopPercent,
            maxRiskPerTradePercent = entity.maxRiskPerTradePercent,
            maxOpenPositions = entity.maxOpenPositions,
            isAutoTradingEnabled = entity.isAutoTradingEnabled
        )
        _activeStrategy.value = strat
    }

    fun runBacktest10Years(
        initialCapital: Double = 100000.0,
        slippagePercent: Double = 0.05,
        feePerTradePercent: Double = 0.03
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _isBacktesting.value = true
            try {
                val instrument = _selectedInstrument.value
                val strategy = _activeStrategy.value
                val candles = marketRepo.get10YearHistoricalCandles(instrument.symbol)
                val result = BacktestEngine.runBacktest(
                    strategy = strategy,
                    instrument = instrument,
                    historicalCandles = candles,
                    initialCapital = initialCapital,
                    slippagePercent = slippagePercent,
                    feePerTradePercent = feePerTradePercent
                )
                _backtestResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isBacktesting.value = false
            }
        }
    }

    fun executeBacktest(
        instrument: Instrument? = null,
        strategy: TradingStrategy? = null,
        initialCapital: Double = 100000.0,
        slippagePercent: Double = 0.05,
        feePerTradePercent: Double = 0.03
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _isBacktesting.value = true
            try {
                val targetInst = instrument ?: _selectedInstrument.value
                val targetStrat = strategy ?: _activeStrategy.value
                _selectedInstrument.value = targetInst
                _activeStrategy.value = targetStrat
                val candles = marketRepo.get10YearHistoricalCandles(targetInst.symbol)
                val result = BacktestEngine.runBacktest(
                    strategy = targetStrat,
                    instrument = targetInst,
                    historicalCandles = candles,
                    initialCapital = initialCapital,
                    slippagePercent = slippagePercent,
                    feePerTradePercent = feePerTradePercent
                )
                _backtestResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isBacktesting.value = false
            }
        }
    }

    private fun runInitialBacktest() {
        try {
            val instrument = _selectedInstrument.value
            val strategy = _activeStrategy.value
            val candles = marketRepo.get10YearHistoricalCandles(instrument.symbol)
            val result = BacktestEngine.runBacktest(strategy, instrument, candles, 100000.0)
            _backtestResult.value = result
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun executePaperTrade(
        symbol: String,
        side: OrderSide,
        orderType: OrderType,
        quantity: Double,
        stopLoss: Double,
        takeProfit: Double,
        strategyName: String = "Manual Execution"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = instruments.value.find { it.symbol == symbol } ?: _selectedInstrument.value
            val entryPrice = inst.currentPrice
            val trade = PaperTradeEntity(
                id = UUID.randomUUID().toString(),
                symbol = symbol,
                exchangeName = inst.exchange.code,
                side = side.name,
                orderType = orderType.name,
                quantity = quantity,
                entryPrice = entryPrice,
                currentPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                status = "OPEN",
                unrealizedPnl = 0.0,
                realizedPnl = 0.0,
                openedAt = System.currentTimeMillis(),
                strategyName = strategyName
            )
            tradingRepo.placePaperTrade(trade)
        }
    }

    fun closePaperTrade(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.closePaperTrade(id, 0.0)
        }
    }

    fun clearPaperTrades() {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.clearPaperTrades()
        }
    }

    private fun seedInitialDispatchedOrders() {
        val now = System.currentTimeMillis()
        val initialOrders = listOf(
            DispatchedSandboxOrder(
                orderId = "ORD-NSE-89102",
                brokerName = "Zerodha Kite Connect",
                symbol = "NIFTY 50",
                side = "BUY",
                orderType = "MARKET",
                productType = "MIS",
                quantity = 50.0,
                price = 24840.50,
                stopLoss = 24650.0,
                takeProfit = 25100.0,
                status = "FILLED",
                latencyMs = 24,
                timestamp = now - 1800000L,
                statusMessage = "Executed via Kite OMS Sandbox • Tag: ALGO_MOMENTUM"
            ),
            DispatchedSandboxOrder(
                orderId = "ORD-NASDAQ-44129",
                brokerName = "Alpaca Markets API",
                symbol = "NVDA",
                side = "BUY",
                orderType = "LIMIT",
                productType = "CNC",
                quantity = 25.0,
                price = 126.80,
                stopLoss = 122.0,
                takeProfit = 134.50,
                status = "FILLED",
                latencyMs = 18,
                timestamp = now - 3600000L * 2,
                statusMessage = "Limit order matched on NASDAQ paper route"
            ),
            DispatchedSandboxOrder(
                orderId = "ORD-BINANCE-33101",
                brokerName = "Binance Futures API",
                symbol = "BTC/USD",
                side = "SELL",
                orderType = "MARKET",
                productType = "NRML",
                quantity = 0.25,
                price = 64180.0,
                stopLoss = 65500.0,
                takeProfit = 61800.0,
                status = "FILLED",
                latencyMs = 14,
                timestamp = now - 3600000L * 5,
                statusMessage = "Futures short leg filled • Isolated 5x"
            )
        )
        _dispatchedOrders.value = initialOrders
    }

    fun dispatchSandboxOrder(
        brokerName: String,
        symbol: String,
        side: OrderSide,
        orderType: String,
        productType: String,
        quantity: Double,
        price: Double,
        triggerPrice: Double = 0.0,
        stopLoss: Double = 0.0,
        takeProfit: Double = 0.0
    ): DispatchedSandboxOrder {
        val inst = instruments.value.find { it.symbol == symbol } ?: _selectedInstrument.value
        val execPrice = if (orderType == "MARKET" || price <= 0.0) {
            if (side == OrderSide.BUY) inst.currentPrice * 1.0002 else inst.currentPrice * 0.9998
        } else {
            price
        }

        val latency = when {
            brokerName.contains("Dhan", true) -> 16L
            brokerName.contains("Alpaca", true) -> 15L
            brokerName.contains("Binance", true) -> 12L
            brokerName.contains("Bybit", true) -> 14L
            brokerName.contains("Upstox", true) -> 20L
            brokerName.contains("Fyers", true) -> 21L
            brokerName.contains("Angel", true) -> 22L
            brokerName.contains("Kite", true) || brokerName.contains("Zerodha", true) -> 24L
            brokerName.contains("Interactive", true) || brokerName.contains("IBKR", true) -> 21L
            brokerName.contains("Kotak", true) -> 26L
            brokerName.contains("Groww", true) -> 28L
            brokerName.contains("Shoonya", true) -> 29L
            brokerName.contains("Sky", true) -> 30L
            brokerName.contains("ICICI", true) -> 32L
            else -> 25L
        }

        val orderId = "ORD-${inst.exchange.code}-${System.currentTimeMillis().toString().takeLast(6)}"
        val order = DispatchedSandboxOrder(
            orderId = orderId,
            brokerName = brokerName,
            symbol = symbol,
            side = side.name,
            orderType = orderType,
            productType = productType,
            quantity = quantity,
            price = execPrice,
            triggerPrice = triggerPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            status = "FILLED",
            latencyMs = latency,
            timestamp = System.currentTimeMillis(),
            statusMessage = "Order successfully routed, acknowledged & filled by $brokerName sandbox matching engine."
        )

        _dispatchedOrders.value = listOf(order) + _dispatchedOrders.value
        _lastDispatchedOrderResult.value = order

        // Also record as active paper trade position
        viewModelScope.launch(Dispatchers.IO) {
            val trade = PaperTradeEntity(
                id = UUID.randomUUID().toString(),
                symbol = symbol,
                exchangeName = inst.exchange.code,
                side = side.name,
                orderType = orderType,
                quantity = quantity,
                entryPrice = execPrice,
                currentPrice = execPrice,
                stopLoss = if (stopLoss > 0.0) stopLoss else (if (side == OrderSide.BUY) execPrice * 0.98 else execPrice * 1.02),
                takeProfit = if (takeProfit > 0.0) takeProfit else (if (side == OrderSide.BUY) execPrice * 1.05 else execPrice * 0.95),
                status = "OPEN",
                unrealizedPnl = 0.0,
                realizedPnl = 0.0,
                openedAt = System.currentTimeMillis(),
                strategyName = "Sandbox $brokerName ($productType)"
            )
            tradingRepo.placePaperTrade(trade)
        }

        return order
    }

    fun addCustomBrokerAccount(
        brokerType: BrokerType,
        accountName: String,
        apiKey: String,
        environment: String = "PRODUCTION"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBroker = BrokerAccountEntity(
                id = "broker_${brokerType.name.lowercase()}_${System.currentTimeMillis().toString().takeLast(4)}",
                brokerType = brokerType.name,
                accountName = accountName.ifBlank { "${brokerType.displayName} User" },
                apiKeyMasked = if (apiKey.length > 4) "${apiKey.take(3)}••••${apiKey.takeLast(3)}" else "key_••••••••9901",
                environment = environment,
                isConnected = true,
                allowAutomatedExecution = true,
                maxOrderValueLimit = 5000000.0,
                pingLatencyMs = brokerType.defaultLatencyMs,
                lastSyncTime = System.currentTimeMillis()
            )
            tradingRepo.addNewBroker(newBroker)
        }
    }

    fun cancelDispatchedOrder(orderId: String) {
        _dispatchedOrders.value = _dispatchedOrders.value.map {
            if (it.orderId == orderId) it.copy(status = "CANCELLED", statusMessage = "Order cancelled by user") else it
        }
    }

    private fun recalculatePortfolioSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            val positions = listOf(
                PortfolioPositionEntity("NVDA", "Nvidia Corp", "NASDAQ", "EQUITY", 150.0, 114.20, 128.60, 20.0, 19.3, "Semiconductors", 1.68),
                PortfolioPositionEntity("RELIANCE", "Reliance Industries", "NSE", "EQUITY", 200.0, 2890.0, 3012.50, 15.0, 16.2, "Energy & Conglomerate", 0.88),
                PortfolioPositionEntity("BTC/USD", "Bitcoin Spot", "BINANCE", "CRYPTO", 0.45, 58200.0, 64250.0, 12.0, 14.5, "Crypto", 2.40),
                PortfolioPositionEntity("SPY", "SPDR S&P 500 ETF", "NYSE", "ETF", 60.0, 535.0, 564.30, 25.0, 24.1, "Broad ETF", 1.00),
                PortfolioPositionEntity("TCS", "Tata Consultancy", "NSE", "EQUITY", 80.0, 3950.0, 4280.00, 10.0, 11.4, "IT Services", 0.72),
                PortfolioPositionEntity("XAU/USD", "Gold Spot", "FOREX", "COMMODITY", 12.0, 2380.0, 2512.40, 10.0, 10.1, "Precious Metals", 0.25)
            )
            val summary = PortfolioOptimizer.generateRebalanceRecommendations(positions, 35000.0)
            _portfolioSummary.value = summary
        }
    }

    fun applyRebalance() {
        viewModelScope.launch(Dispatchers.IO) {
            recalculatePortfolioSummary()
        }
    }

    fun toggleBrokerConnection(id: String, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.updateBrokerStatus(id, !currentStatus, if (!currentStatus) 22L else 0L)
        }
    }

    fun toggleMasterLiveSession(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSessionConnecting.value = true
            _isLiveBrokerSessionActive.value = enable
            if (enable) {
                _liveSessionToken.value = "ZX26-AUTH-LIVE-${(100000..999999).random()}"
                _liveSessionStatusMessage.value = "OMS WebSocket Handshake Verified • All Gateways Synchronized"
                tradingRepo.updateAllBrokersStatus(true, 18L)
            } else {
                _liveSessionStatusMessage.value = "Live Session Terminated • Sandbox Fallback Mode Active"
                tradingRepo.updateAllBrokersStatus(false, 0L)
            }
            _isSessionConnecting.value = false
        }
    }

    fun establishSingleBrokerSession(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.updateBrokerStatus(id, true, 18L)
            _isLiveBrokerSessionActive.value = true
        }
    }

    fun terminateSingleBrokerSession(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tradingRepo.updateBrokerStatus(id, false, 0L)
        }
    }

    fun requestAiMarketAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiLoading.value = true
            val res = aiRepo.analyzeMarketSetup(_selectedInstrument.value, _selectedTimeframe.value.code, _activeStrategy.value)
            _aiReport.value = res
            _isAiLoading.value = false
        }
    }

    fun requestAiStressTest() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiLoading.value = true
            val metrics = _backtestResult.value?.metrics
            if (metrics != null) {
                val res = aiRepo.stressTestStrategy(metrics, _activeStrategy.value.name)
                _aiReport.value = res
            }
            _isAiLoading.value = false
        }
    }

    // Option Chain & Derivatives Engine Methods
    fun selectOptionUnderlying(symbol: String) {
        _selectedOptionUnderlying.value = symbol
        refreshOptionChain()
    }

    fun selectOptionExpiry(expiry: String) {
        _selectedOptionExpiry.value = expiry
        refreshOptionChain()
    }

    fun selectOptionStrategyType(type: OptionStrategyType) {
        _selectedOptionStrategyType.value = type
        refreshStrategyPayoff()
    }

    fun setOIBuildupFilter(filter: OIBuildupType?) {
        _oiBuildupFilter.value = filter
    }

    fun refreshOptionChain() {
        viewModelScope.launch(Dispatchers.Default) {
            val chain = marketRepo.getOptionChain(
                symbol = _selectedOptionUnderlying.value,
                selectedExpiry = _selectedOptionExpiry.value
            )
            _optionChainData.value = chain
            refreshStrategyPayoff()
        }
    }

    fun refreshStrategyPayoff() {
        viewModelScope.launch(Dispatchers.Default) {
            val payoff = marketRepo.calculateStrategyPayoff(
                strategyType = _selectedOptionStrategyType.value,
                underlyingSymbol = _selectedOptionUnderlying.value
            )
            _optionStrategyPayoff.value = payoff
        }
    }

    fun refreshOIBuildups() {
        viewModelScope.launch(Dispatchers.Default) {
            val buildups = marketRepo.getOIBuildupScans()
            _oiBuildupScans.value = buildups
        }
    }

    fun executeOptionLegTrade(contract: OptionContract, side: OrderSide, lots: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val qty = lots * contract.lotSize.toDouble()
            val trade = PaperTradeEntity(
                id = UUID.randomUUID().toString(),
                symbol = "${contract.underlyingSymbol} ${contract.strikePrice.toInt()} ${contract.optionType.name} (${contract.expiryDate})",
                exchangeName = "NSE-FO",
                side = side.name,
                orderType = "MARKET",
                quantity = qty,
                entryPrice = contract.ltp,
                currentPrice = contract.ltp,
                stopLoss = if (side == OrderSide.BUY) contract.ltp * 0.70 else contract.ltp * 1.30,
                takeProfit = if (side == OrderSide.BUY) contract.ltp * 1.60 else contract.ltp * 0.40,
                status = "OPEN",
                unrealizedPnl = 0.0,
                realizedPnl = 0.0,
                openedAt = System.currentTimeMillis(),
                strategyName = "Options Scalp Leg"
            )
            tradingRepo.placePaperTrade(trade)
        }
    }

    fun executeMultiLegStrategy(payoff: OptionStrategyPayoff) {
        viewModelScope.launch(Dispatchers.IO) {
            payoff.legs.forEach { leg ->
                val contract = leg.optionContract
                val qty = leg.lots * contract.lotSize.toDouble()
                val trade = PaperTradeEntity(
                    id = UUID.randomUUID().toString(),
                    symbol = "${contract.underlyingSymbol} ${contract.strikePrice.toInt()} ${contract.optionType.name}",
                    exchangeName = "NSE-FO",
                    side = leg.action.name,
                    orderType = "MARKET",
                    quantity = qty,
                    entryPrice = contract.ltp,
                    currentPrice = contract.ltp,
                    stopLoss = 0.0,
                    takeProfit = 0.0,
                    status = "OPEN",
                    unrealizedPnl = 0.0,
                    realizedPnl = 0.0,
                    openedAt = System.currentTimeMillis(),
                    strategyName = payoff.strategyType.displayName
                )
                tradingRepo.placePaperTrade(trade)
            }
        }
    }
}
