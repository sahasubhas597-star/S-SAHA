package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paper_trades")
data class PaperTradeEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val exchangeName: String,
    val side: String, // BUY, SELL
    val orderType: String, // MARKET, LIMIT, STOP_LIMIT
    val quantity: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val status: String, // OPEN, CLOSED, PENDING
    val unrealizedPnl: Double,
    val realizedPnl: Double,
    val openedAt: Long,
    val closedAt: Long? = null,
    val strategyName: String = "Manual Execution",
    val notes: String = ""
)

@Entity(tableName = "portfolio_positions")
data class PortfolioPositionEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val exchange: String,
    val assetClass: String,
    val quantity: Double,
    val avgEntryPrice: Double,
    val currentPrice: Double,
    val targetAllocationPercent: Double,
    val currentAllocationPercent: Double,
    val sector: String,
    val beta: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PortfolioSummary(
    val totalValue: Double,
    val cashBalance: Double,
    val totalInvested: Double,
    val totalUnrealizedPnl: Double,
    val totalUnrealizedPnlPercent: Double,
    val dayPnl: Double,
    val dayPnlPercent: Double,
    val portfolioBeta: Double,
    val valueAtRisk95: Double,
    val sharpeRatio: Double,
    val positions: List<PortfolioPositionEntity>,
    val rebalanceRecommendations: List<RebalanceAction>
)

data class RebalanceAction(
    val symbol: String,
    val name: String,
    val currentWeightPercent: Double,
    val targetWeightPercent: Double,
    val weightDeltaPercent: Double,
    val suggestedAction: OrderSide,
    val sharesToTransact: Double,
    val estimatedAmount: Double,
    val rationale: String
)

enum class BrokerCategory(val title: String) {
    INDIA_DISCOUNT("India Discount (Zero-Brok / Algo)"),
    INDIA_FULL_SERVICE("India Full-Service & Banking"),
    US_GLOBAL("US & Global Equities / Options"),
    CRYPTO_EXCHANGES("Crypto Spot & Derivatives"),
    FOREX_CFD_FUTURES("Forex, CFDs & Futures")
}

enum class BrokerHealthStatus(val label: String, val isHealthy: Boolean) {
    OPERATIONAL("Operational (All Systems Normal)", true),
    HIGH_TRAFFIC("High Order Queue (Minor Lag)", true),
    DEGRADED("Degraded / API Glitch", false),
    DOWNTIME_ALERT("Server Down / Login Blocked", false)
}

enum class BrokerType(
    val displayName: String,
    val supportedMarkets: String,
    val iconLabel: String,
    val webLoginUrl: String,
    val defaultLatencyMs: Long = 25L,
    val uptimePercent: Double = 99.8,
    val category: BrokerCategory = BrokerCategory.INDIA_DISCOUNT,
    val androidPackageName: String = "",
    val callAndTradeNumber: String = "1800-000-0000",
    val backupWebUrl: String = "",
    val defaultHealthStatus: BrokerHealthStatus = BrokerHealthStatus.OPERATIONAL
) {
    // --- INDIA DISCOUNT & ALGO BROKERS ---
    ZERODHA(
        "Zerodha Kite",
        "NSE, BSE, NFO, MCX, CDS",
        "KITE",
        "https://kite.zerodha.com",
        22L,
        99.85,
        BrokerCategory.INDIA_DISCOUNT,
        "com.zerodha.kite3",
        "080-47181888",
        "https://kite.trade"
    ),
    GROWW(
        "Groww Invest & Trade",
        "NSE, BSE, F&O, Direct Mutual Funds",
        "GROWW",
        "https://groww.in/stocks",
        26L,
        99.72,
        BrokerCategory.INDIA_DISCOUNT,
        "com.nextbillion.groww",
        "080-69033300",
        "https://groww.in/login"
    ),
    ANGEL_ONE(
        "Angel One SmartAPI",
        "NSE, BSE, MCX, NCDEX, Currency",
        "ANGEL",
        "https://trade.angelone.in",
        20L,
        99.90,
        BrokerCategory.INDIA_DISCOUNT,
        "com.msf.angelmobile",
        "080-47480048",
        "https://smartapi.angelbroking.com"
    ),
    UPSTOX(
        "Upstox Pro v2",
        "NSE, BSE, MCX, Currency, F&O",
        "UPSTOX",
        "https://pro.upstox.com",
        19L,
        99.88,
        BrokerCategory.INDIA_DISCOUNT,
        "in.upstox.app",
        "022-41792999",
        "https://service.upstox.com"
    ),
    DHAN(
        "Dhan Lightning HQ",
        "NSE, BSE, MCX, Direct F&O & TradingView",
        "DHAN",
        "https://web.dhan.co",
        15L,
        99.96,
        BrokerCategory.INDIA_DISCOUNT,
        "co.dhan",
        "022-48906666",
        "https://dhanhq.co"
    ),
    FYERS(
        "Fyers Web v3",
        "NSE, BSE, MCX, Advanced TV Charts & Algo",
        "FYERS",
        "https://trade.fyers.in",
        21L,
        99.82,
        BrokerCategory.INDIA_DISCOUNT,
        "in.fyers.markets",
        "080-66251111",
        "https://myapi.fyers.in"
    ),
    KOTAK_NEO(
        "Kotak Neo (Zero Brok F&O)",
        "NSE, BSE, F&O, Zero Brokerage Intraday",
        "NEO",
        "https://neo.kotaksecurities.com",
        24L,
        99.78,
        BrokerCategory.INDIA_DISCOUNT,
        "com.kotak.neo",
        "1800-209-9191",
        "https://www.kotaksecurities.com"
    ),
    HDFC_SKY(
        "HDFC Sky",
        "NSE, BSE, US Stocks, F&O, Mutual Funds",
        "SKY",
        "https://hdfcsky.com",
        28L,
        99.70,
        BrokerCategory.INDIA_DISCOUNT,
        "com.hdfc.sky",
        "022-68468700",
        "https://invest.hdfcsky.com"
    ),
    FINVASIA_SHOONYA(
        "Shoonya by Finvasia",
        "Lifetime Zero Brokerage, API Algo Trading",
        "SHOONYA",
        "https://shoonya.finvasia.com",
        27L,
        99.65,
        BrokerCategory.INDIA_DISCOUNT,
        "com.finvasia.shoonya",
        "0172-6668700",
        "https://prism.finvasia.com"
    ),
    FIVE_PAISA(
        "5Paisa Pro",
        "NSE, BSE, MCX, Flat Fee Broking & APIs",
        "5PAISA",
        "https://trade.5paisa.com",
        31L,
        99.55,
        BrokerCategory.INDIA_DISCOUNT,
        "com.fivepaisa.invest",
        "022-37413535",
        "https://www.5paisa.com"
    ),
    PAYTM_MONEY(
        "Paytm Money Stocks",
        "NSE, BSE, F&O, IPOs, ETFs",
        "PAYTM",
        "https://www.paytmmoney.com/stocks",
        25L,
        99.75,
        BrokerCategory.INDIA_DISCOUNT,
        "com.paytmmoney",
        "080-46642290",
        "https://dashboard.paytmmoney.com"
    ),
    ALICE_BLUE(
        "Alice Blue (ANT)",
        "NSE, BSE, MCX, Direct Algo Trading",
        "ALICE",
        "https://ant.aliceblueonline.com",
        29L,
        99.60,
        BrokerCategory.INDIA_DISCOUNT,
        "com.aliceblue.ant",
        "080-35215000",
        "https://bot.aliceblueonline.com"
    ),
    FLATTRADE(
        "Flattrade (Fortune)",
        "Zero Brokerage, Multi-Exchange Algo Desk",
        "FLAT",
        "https://web.flattrade.in",
        30L,
        99.62,
        BrokerCategory.INDIA_DISCOUNT,
        "in.flattrade.app",
        "044-45609696",
        "https://flattrade.in"
    ),
    TRADEJINI(
        "Tradejini Cube",
        "NSE, BSE, MCX, Currency Derivatives",
        "JINI",
        "https://cube.tradejini.com",
        32L,
        99.58,
        BrokerCategory.INDIA_DISCOUNT,
        "com.tradejini.cube",
        "080-40204020",
        "https://tradejini.com"
    ),
    SAMCO(
        "SAMCO StockNote",
        "NSE, BSE, MCX, KyaTrade Algo",
        "SAMCO",
        "https://trade.samco.in",
        33L,
        99.55,
        BrokerCategory.INDIA_DISCOUNT,
        "com.samco.stocknote",
        "022-22227777",
        "https://www.samco.in"
    ),

    // --- INDIA FULL-SERVICE & BANKING BROKERS ---
    ICICI_DIRECT(
        "ICICI Direct Breeze",
        "NSE, BSE, F&O, Global, Institutional Desk",
        "ICICI",
        "https://www.icicidirect.com",
        30L,
        99.70,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.icicidirect.markets",
        "1800-572-8888",
        "https://secure.icicidirect.com"
    ),
    MOTILAL_OSWAL(
        "Motilal Oswal Rise",
        "NSE, BSE, Advisory, Institutional Research",
        "MOSWAL",
        "https://www.motilaloswal.com",
        32L,
        99.65,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.motilaloswal.moweb",
        "022-71881000",
        "https://invest.motilaloswal.com"
    ),
    IIFL_SECURITIES(
        "IIFL Securities TTWeb",
        "NSE, BSE, MCX, Full-Service Equities",
        "IIFL",
        "https://ttweb.indiainfoline.com",
        30L,
        99.68,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.iifl.markets",
        "022-40071000",
        "https://www.indiainfoline.com"
    ),
    SBI_SECURITIES(
        "SBI Securities (SBICAP)",
        "NSE, BSE, Banking 3-in-1 Direct Account",
        "SBICAP",
        "https://www.sbismart.com",
        35L,
        99.60,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.sbicap.smart",
        "1800-209-9345",
        "https://trade.sbismart.com"
    ),
    SHAREKHAN(
        "Sharekhan TradeTiger / Espresso",
        "NSE, BSE, MCX, Technical Charting & Derivatives",
        "SKHAN",
        "https://www.sharekhan.com",
        28L,
        99.72,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.sharekhan.android",
        "1800-22-7500",
        "https://newtrade.sharekhan.com"
    ),
    AXIS_DIRECT(
        "Axis Direct (Ring)",
        "NSE, BSE, 3-in-1 Axis Bank Linked",
        "AXIS",
        "https://simpleha.axisdirect.in",
        34L,
        99.60,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.axis.direct",
        "022-40508080",
        "https://trade.axisdirect.in"
    ),
    GEOJIT(
        "Geojit Selfie",
        "NSE, BSE, MCX, Global & Gulf NRI Trading",
        "GEOJIT",
        "https://selfie.geojit.net",
        35L,
        99.55,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.geojit.selfie",
        "1800-425-5501",
        "https://www.geojit.com"
    ),
    CHOICE_BROKING(
        "Choice Broking (Jiffy)",
        "NSE, BSE, MCX, Advisory & Algo",
        "CHOICE",
        "https://jiffy.choicebroking.in",
        33L,
        99.58,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.choicebroking.jiffy",
        "022-67079999",
        "https://choiceindia.com"
    ),
    MASTERTRUST(
        "Mastertrust MasterMobile",
        "NSE, BSE, MCX, Custom Algo APIs",
        "MASTER",
        "https://mastermobile.mastertrust.co.in",
        34L,
        99.55,
        BrokerCategory.INDIA_FULL_SERVICE,
        "com.mastertrust.mastermobile",
        "011-42111888",
        "https://mastertrust.co.in"
    ),

    // --- US & GLOBAL EQUITIES & OPTIONS ---
    INTERACTIVE_BROKERS(
        "Interactive Brokers (IBKR)",
        "US, EU, UK, HK, Japan (150+ Global Markets)",
        "IBKR",
        "https://www.interactivebrokers.com/portal",
        18L,
        99.98,
        BrokerCategory.US_GLOBAL,
        "com.interactivebrokers.android.tws",
        "+1-877-442-2757",
        "https://ndcdyn.interactivebrokers.com"
    ),
    ALPACA(
        "Alpaca Markets API",
        "US Equities, ETFs, Fractional Shares & Crypto",
        "ALPACA",
        "https://app.alpaca.markets",
        14L,
        99.96,
        BrokerCategory.US_GLOBAL,
        "markets.alpaca.android",
        "+1-855-257-2221",
        "https://alpaca.markets/login"
    ),
    ROBINHOOD(
        "Robinhood",
        "US Equities, Zero-Commission Options & Crypto",
        "ROBIN",
        "https://robinhood.com",
        20L,
        99.85,
        BrokerCategory.US_GLOBAL,
        "com.robinhood.android",
        "+1-650-940-2700",
        "https://robinhood.com/login"
    ),
    WEBULL(
        "Webull Trading",
        "US Equities, Extended Hours, Options & ETFs",
        "WEBULL",
        "https://app.webull.com",
        17L,
        99.90,
        BrokerCategory.US_GLOBAL,
        "org.dayup.stocks",
        "+1-888-828-0918",
        "https://trade.webull.com"
    ),
    CHARLES_SCHWAB(
        "Charles Schwab / thinkorswim",
        "US Equities, Options, Futures & thinkorswim Suite",
        "SCHWAB",
        "https://client.schwab.com",
        22L,
        99.92,
        BrokerCategory.US_GLOBAL,
        "com.schwab.mobile",
        "+1-800-435-4000",
        "https://trade.thinkorswim.com"
    ),
    ETRADE(
        "E*TRADE by Morgan Stanley",
        "US Equities, Power E*TRADE Options & Futures",
        "ETRADE",
        "https://us.etrade.com",
        23L,
        99.88,
        BrokerCategory.US_GLOBAL,
        "com.etrade.mobilepro.activity",
        "+1-800-387-2331",
        "https://trading.etrade.com"
    ),
    FIDELITY(
        "Fidelity Investments",
        "US Equities, Active Trader Pro, Retirement & ETFs",
        "FIDELITY",
        "https://www.fidelity.com",
        24L,
        99.94,
        BrokerCategory.US_GLOBAL,
        "com.fidelity.android",
        "+1-800-343-3548",
        "https://digital.fidelity.com"
    ),
    TRADESTATION(
        "TradeStation Global",
        "US Stocks, Options, Futures & EasyLanguage Algos",
        "TSTATION",
        "https://www.tradestation.com",
        22L,
        99.89,
        BrokerCategory.US_GLOBAL,
        "com.tradestation.client.phone",
        "+1-800-822-0512",
        "https://tradestation.com/login"
    ),
    TASTYTRADE(
        "tastytrade (tastyworks)",
        "High-Probability Options, Multi-Leg Spreads & Crypto",
        "TASTY",
        "https://tastytrade.com",
        19L,
        99.92,
        BrokerCategory.US_GLOBAL,
        "com.tastytrade.mobile",
        "+1-888-297-3867",
        "https://trade.tastytrade.com"
    ),

    // --- CRYPTO EXCHANGES (SPOT & DERIVATIVES) ---
    BINANCE(
        "Binance Global",
        "Crypto Spot, Perpetual Futures, Options & Staking",
        "BINANCE",
        "https://www.binance.com/en/trade",
        11L,
        99.99,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.binance.dev",
        "+1-800-BINANCE",
        "https://accounts.binance.com"
    ),
    BYBIT(
        "Bybit Unified Trading",
        "Crypto Futures, Inverse Perpetuals & Spot",
        "BYBIT",
        "https://www.bybit.com/trade",
        13L,
        99.95,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.bybit.app",
        "+1-800-BYBIT",
        "https://www.bybit.com/login"
    ),
    OKX(
        "OKX Pro Exchange",
        "Web3, Crypto Derivatives, CeFi & DeFi Trading",
        "OKX",
        "https://www.okx.com/trade-spot",
        14L,
        99.94,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.okinc.okex.gp",
        "+1-800-OKX",
        "https://www.okx.com/account/login"
    ),
    COINBASE(
        "Coinbase Advanced Trade",
        "Institutional Crypto Spot, Staking & USD Pairs",
        "COINBASE",
        "https://www.coinbase.com/advanced-trade",
        16L,
        99.95,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.coinbase.android",
        "+1-888-908-7930",
        "https://login.coinbase.com"
    ),
    KRAKEN(
        "Kraken Pro",
        "Deep Liquidity Crypto Spot & Futures",
        "KRAKEN",
        "https://pro.kraken.com",
        15L,
        99.97,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.kraken.invest.app",
        "+1-888-837-8818",
        "https://trade.kraken.com"
    ),
    KUCOIN(
        "KuCoin Exchange",
        "Global Altcoins, Spot & Futures Trading Desk",
        "KUCOIN",
        "https://www.kucoin.com/trade",
        18L,
        99.90,
        BrokerCategory.CRYPTO_EXCHANGES,
        "com.kubi.kucoin",
        "+1-800-KUCOIN",
        "https://www.kucoin.com/ucenter/signin"
    ),

    // --- FOREX, CFDS & FUTURES PLATFORMS ---
    METATRADER5(
        "MetaTrader 5 (MT5 Bridge)",
        "Global Forex, Commodities, CFDs & EAs",
        "MT5",
        "https://web.metatrader5.com",
        20L,
        99.85,
        BrokerCategory.FOREX_CFD_FUTURES,
        "net.metaquotes.metatrader5",
        "+1-800-META5",
        "https://www.mql5.com"
    ),
    METATRADER4(
        "MetaTrader 4 (MT4 Bridge)",
        "Forex Currencies, Gold & Expert Advisors",
        "MT4",
        "https://web.metatrader4.com",
        22L,
        99.80,
        BrokerCategory.FOREX_CFD_FUTURES,
        "net.metaquotes.metatrader4",
        "+1-800-META4",
        "https://www.metaquotes.net"
    ),
    EXNESS(
        "Exness Trade & Terminal",
        "Zero-Spread Forex, Indices & Gold CFDs",
        "EXNESS",
        "https://my.exness.com",
        18L,
        99.92,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.exness.invest",
        "+1-800-EXNESS",
        "https://webterminal.exness.com"
    ),
    TRADOVATE(
        "Tradovate / NinjaTrader",
        "CME, CBOT, NYMEX, Micro E-mini Futures",
        "TRADO",
        "https://trader.tradovate.com",
        16L,
        99.90,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.tradovate.trader",
        "+1-844-283-8300",
        "https://auth.tradovate.com"
    ),
    IG_MARKETS(
        "IG Markets & Trading",
        "Global CFDs, Forex, DMA Equities & Indices",
        "IG",
        "https://www.ig.com",
        24L,
        99.88,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.iggroup.android.cfd",
        "+44-207-896-0079",
        "https://deal.ig.com"
    ),
    SAXO_BANK(
        "Saxo Bank (SaxoTraderGO)",
        "Institutional Multi-Asset, Global Bonds & FX",
        "SAXO",
        "https://www.saxotrader.com",
        25L,
        99.93,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.saxobank.saxotrader",
        "+45-3977-4000",
        "https://login.saxo"
    ),
    PLUS500(
        "Plus500 CFD & Trading",
        "Global CFDs on Shares, Indices & Commodities",
        "PLUS500",
        "https://app.plus500.com",
        23L,
        99.86,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.plus500",
        "+44-203-885-7180",
        "https://www.plus500.com/login"
    ),
    XM_GLOBAL(
        "XM Global Trading",
        "Ultra Low Spread Forex & Metals",
        "XM",
        "https://my.xm.com",
        26L,
        99.82,
        BrokerCategory.FOREX_CFD_FUTURES,
        "com.xm.global",
        "+357-25-029900",
        "https://webtrader.xm.com"
    )
}

@Entity(tableName = "broker_accounts")
data class BrokerAccountEntity(
    @PrimaryKey val id: String,
    val brokerType: String,
    val accountName: String,
    val apiKeyMasked: String,
    val environment: String, // SANDBOX, PRODUCTION
    val isConnected: Boolean,
    val allowAutomatedExecution: Boolean,
    val maxOrderValueLimit: Double,
    val pingLatencyMs: Long,
    val lastSyncTime: Long
)

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val symbol: String,
    val exchange: String,
    val name: String,
    val assetClass: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class DispatchedSandboxOrder(
    val orderId: String,
    val brokerName: String,
    val symbol: String,
    val side: String, // BUY, SELL
    val orderType: String, // MARKET, LIMIT, SL-LMT
    val productType: String, // CNC, MIS, NRML
    val quantity: Double,
    val price: Double,
    val triggerPrice: Double = 0.0,
    val stopLoss: Double = 0.0,
    val takeProfit: Double = 0.0,
    val status: String, // FILLED, REJECTED, CANCELLED
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val statusMessage: String
)
