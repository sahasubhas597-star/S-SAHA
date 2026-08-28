package com.example.engine.pinescript

import com.example.data.model.TradingStrategy

object PineScriptGenerator {

    fun generatePineScriptV5(strategy: TradingStrategy): String {
        val safeName = strategy.name.replace("\"", "\\\"")
        return """
//@version=5
strategy(
    title = "$safeName [Global AI Algo Hub]",
    shorttitle = "${strategy.name.take(12)}",
    overlay = true,
    initial_capital = 100000,
    default_qty_type = strategy.percent_of_equity,
    default_qty_value = ${strategy.maxRiskPerTradePercent * 5},
    commission_type = strategy.commission.percent,
    commission_value = 0.03,
    slippage = 2
)

// === INPUT PARAMETERS ===
stopLossPct    = input.float(${strategy.stopLossPercent}, title="Stop Loss %", minval=0.1, step=0.1)
takeProfitPct  = input.float(${strategy.takeProfitPercent}, title="Take Profit %", minval=0.5, step=0.5)
trailStopPct   = input.float(${strategy.trailingStopPercent}, title="Trailing Stop %", minval=0.2, step=0.1)
useRsiFilter   = input.bool(true, title="Enable RSI Momentum Filter")
rsiPeriod      = input.int(14, title="RSI Period")
emaFastPeriod  = input.int(20, title="Fast EMA Period")
emaSlowPeriod  = input.int(50, title="Slow EMA Period")

// === INDICATORS ===
fastEma = ta.ema(close, emaFastPeriod)
slowEma = ta.ema(close, emaSlowPeriod)
rsiVal  = ta.rsi(close, rsiPeriod)
bbBasis = ta.sma(close, 20)
bbDev   = 2.0 * ta.stdev(close, 20)
bbUpper = bbBasis + bbDev
bbLower = bbBasis - bbDev
atrVal  = ta.atr(14)

// === PLOTTING ===
plot(fastEma, color=color.new(#10B981, 0), title="EMA Fast (20)", linewidth=2)
plot(slowEma, color=color.new(#F59E0B, 0), title="EMA Slow (50)", linewidth=2)
p1 = plot(bbUpper, color=color.new(#6366F1, 60), title="BB Upper")
p2 = plot(bbLower, color=color.new(#6366F1, 60), title="BB Lower")
fill(p1, p2, color=color.new(#6366F1, 92), title="BB Cloud")

// === ENTRY CONDITIONS ===
longCondition = (ta.crossover(fastEma, slowEma) or (close > fastEma and fastEma > slowEma)) and (not useRsiFilter or rsiVal > 48)
shortCondition = ta.crossunder(fastEma, slowEma) and (not useRsiFilter or rsiVal < 45)

// === STRATEGY EXECUTION ===
if (longCondition and strategy.position_size == 0)
    strategy.entry("Long", strategy.long, comment="AI_ENTRY_LONG")
    alert("{\"action\": \"BUY\", \"ticker\": \"" + syminfo.ticker + "\", \"price\": " + str.tostring(close) + ", \"sl\": " + str.tostring(close * (1 - stopLossPct/100)) + ", \"tp\": " + str.tostring(close * (1 + takeProfitPct/100)) + "}", alert.freq_once_per_bar_close)

// === RISK MANAGEMENT: STOP LOSS & TAKE PROFIT ===
longSl = strategy.position_avg_price * (1 - (stopLossPct / 100.0))
longTp = strategy.position_avg_price * (1 + (takeProfitPct / 100.0))

if (strategy.position_size > 0)
    strategy.exit("Exit Long", from_entry="Long", stop=longSl, limit=longTp, trail_points=close * (trailStopPct/100) / syminfo.mintick, trail_offset=0, comment="AI_RISK_EXIT")

// === TRADINGVIEW WEBHOOK ALERTS ===
alertcondition(longCondition, title="AI Algo Long Alert", message="{\"symbol\":\"{{ticker}}\", \"action\":\"BUY\", \"price\":\"{{close}}\", \"strategy\":\"${strategy.name}\"}")
alertcondition(ta.crossunder(rsiVal, 75), title="AI Algo Exit Alert", message="{\"symbol\":\"{{ticker}}\", \"action\":\"SELL\", \"price\":\"{{close}}\", \"strategy\":\"${strategy.name}\"}")
""".trimIndent()
    }

    fun generateWebhookPayload(symbol: String, action: String, price: Double, strategyName: String): String {
        return """
{
  "event": "TRADE_SIGNAL",
  "symbol": "$symbol",
  "action": "$action",
  "price": $price,
  "strategy": "$strategyName",
  "timestamp": ${System.currentTimeMillis()},
  "source": "Global AI Algo Trading Hub",
  "secret_auth": "tv_wh_live_auth_token_8892"
}
""".trimIndent()
    }
}
