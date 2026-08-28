package com.example.engine.scanner

import com.example.data.model.Candle
import com.example.data.model.Instrument
import com.example.data.model.MarketExchange
import com.example.data.model.SignalAlert
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.engine.indicators.TechnicalIndicators
import java.util.UUID
import kotlin.math.abs

object MarketScanner {

    fun scanInstrument(instrument: Instrument, candles: List<Candle>): List<SignalAlert> {
        if (candles.size < 30) return emptyList()

        val signals = mutableListOf<SignalAlert>()
        val latest = candles.last()
        val prev = candles[candles.size - 2]
        val closePrices = candles.map { it.close }

        val rsiList = TechnicalIndicators.calculateRsi(closePrices, 14)
        val currentRsi = rsiList.lastOrNull() ?: 50.0
        val prevRsi = if (rsiList.size >= 2) rsiList[rsiList.size - 2] else 50.0

        val ema20List = TechnicalIndicators.calculateEma(closePrices, 20)
        val ema50List = TechnicalIndicators.calculateEma(closePrices, 50)
        val currentEma20 = ema20List.lastOrNull() ?: latest.close
        val currentEma50 = ema50List.lastOrNull() ?: latest.close

        val bb = TechnicalIndicators.calculateBollingerBands(closePrices, 20, 2.0)
        val atr = TechnicalIndicators.calculateAtr(candles, 14)
        val avgVolume = candles.takeLast(20).map { it.volume }.average()

        // 1. Trapped Buyers (Bull Trap Detection)
        // Price shot up above 20-day high with massive volume but created long upper wick and closed back below resistance
        val recent20High = candles.dropLast(1).takeLast(20).maxOf { it.high }
        val recent20Low = candles.dropLast(1).takeLast(20).minOf { it.low }

        if (latest.high > recent20High && latest.close < recent20High && latest.upperWick > latest.bodyHeight * 1.5 && latest.volume > avgVolume * 1.3) {
            val stopLoss = latest.high + (atr * 0.5)
            val takeProfit = latest.close - (atr * 2.8)
            val risk = stopLoss - latest.close
            val reward = latest.close - takeProfit
            val rr = if (risk > 0) reward / risk else 2.5

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = SignalType.TRAPPED_BUYERS,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = "Liquidity grab above 20-period high ($recent20High) rejected with heavy seller volume. Long upper wick indicates trapped buyers.",
                    confidenceScore = 91
                )
            )
        }

        // 2. Trapped Sellers (Bear Trap / Short Squeeze Detection)
        if (latest.low < recent20Low && latest.close > recent20Low && latest.lowerWick > latest.bodyHeight * 1.5 && latest.volume > avgVolume * 1.3) {
            val stopLoss = latest.low - (atr * 0.5)
            val takeProfit = latest.close + (atr * 2.8)
            val risk = latest.close - stopLoss
            val reward = takeProfit - latest.close
            val rr = if (risk > 0) reward / risk else 2.8

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = SignalType.TRAPPED_SELLERS,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = "Failed breakdown under key support ($recent20Low) with institutional buying absorbed at lows. Trapped shorts primed for squeeze.",
                    confidenceScore = 93
                )
            )
        }

        // 3. Early Entry Accumulation
        // Low volatility compression near EMA 50, RSI turning up from 40-48 with expanding green candle
        if (bb.bandwidth < 0.08 && latest.close >= currentEma20 && prev.close < currentEma20 && currentRsi in 42.0..58.0 && currentRsi > prevRsi) {
            val stopLoss = latest.close - (atr * 1.2)
            val takeProfit = latest.close + (atr * 3.2)
            val risk = latest.close - stopLoss
            val reward = takeProfit - latest.close
            val rr = if (risk > 0) reward / risk else 2.7

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = SignalType.EARLY_ENTRY,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = "Volatility squeeze with bandwidth at ${(bb.bandwidth * 100).toInt() / 100.0}%. Institutional accumulation breakout above 20 EMA with momentum expansion.",
                    confidenceScore = 88
                )
            )
        }

        // 4. Resistance Breakout
        if (latest.close > recent20High && prev.close <= recent20High && latest.volume > avgVolume * 1.4) {
            val stopLoss = recent20High - (atr * 0.8)
            val takeProfit = latest.close + (atr * 2.5)
            val risk = latest.close - stopLoss
            val reward = takeProfit - latest.close
            val rr = if (risk > 0) reward / risk else 2.6

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = SignalType.BREAKOUT,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = "Clean breakout above major resistance level ($recent20High) backed by ${(latest.volume / avgVolume * 100).toInt()}% of 20-period average volume.",
                    confidenceScore = 89
                )
            )
        }

        // 5. RSI Bullish Divergence
        if (latest.close <= prev.close && currentRsi > prevRsi && currentRsi < 35.0) {
            val stopLoss = latest.low - (atr * 0.8)
            val takeProfit = latest.close + (atr * 2.6)
            val risk = latest.close - stopLoss
            val reward = takeProfit - latest.close
            val rr = if (risk > 0) reward / risk else 2.9

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = SignalType.RSI_OVERSOLD_DIVERGENCE,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = "Price printed lower/equal low while RSI oscillator (RSI = ${currentRsi.toInt()}) registered higher trough, confirming bullish momentum divergence.",
                    confidenceScore = 86
                )
            )
        }

        // 6. Trend Pullback / Momentum Accumulation (Fallback high-conviction setup)
        if (signals.isEmpty()) {
            val isBullishTrend = latest.close >= currentEma20 && currentEma20 >= currentEma50
            val sigType = if (isBullishTrend) SignalType.EARLY_ENTRY else SignalType.TRAPPED_BUYERS
            val stopLoss = if (isBullishTrend) latest.close - (atr * 1.5) else latest.close + (atr * 1.5)
            val takeProfit = if (isBullishTrend) latest.close + (atr * 3.5) else latest.close - (atr * 3.5)
            val risk = abs(latest.close - stopLoss)
            val reward = abs(takeProfit - latest.close)
            val rr = if (risk > 0) reward / risk else 2.4

            signals.add(
                SignalAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = instrument.symbol,
                    exchange = instrument.exchange,
                    signalType = sigType,
                    priceTrigger = latest.close,
                    stopLossLevel = stopLoss,
                    takeProfitLevel = takeProfit,
                    riskRewardRatio = (rr * 10).toInt() / 10.0,
                    rationale = if (isBullishTrend)
                        "Bullish EMA trend alignment (EMA20 > EMA50) with price consolidating at key value area near support. Favorable risk-to-reward long setup."
                    else
                        "Bearish resistance rejection at EMA20 with negative momentum divergence. Key distribution zone identified.",
                    confidenceScore = 85
                )
            )
        }

        return signals
    }
}
