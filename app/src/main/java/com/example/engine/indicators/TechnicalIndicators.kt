package com.example.engine.indicators

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class BollingerBandsResult(
    val middle: Double,
    val upper: Double,
    val lower: Double,
    val bandwidth: Double,
    val percentB: Double
)

data class MacdResult(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double
)

data class SupertrendResult(
    val supertrend: Double,
    val isBullish: Boolean,
    val upperBand: Double,
    val lowerBand: Double
)

data class VolumeProfileLevel(
    val price: Double,
    val volume: Double,
    val isPoc: Boolean = false,
    val inValueArea: Boolean = false
)

object TechnicalIndicators {

    fun calculateSma(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()
        val result = mutableListOf<Double>()
        var sum = 0.0
        for (i in 0 until period) {
            sum += prices[i]
        }
        result.add(sum / period)
        for (i in period until prices.size) {
            sum += prices[i] - prices[i - period]
            result.add(sum / period)
        }
        return result
    }

    fun calculateEma(prices: List<Double>, period: Int): List<Double> {
        if (prices.isEmpty()) return emptyList()
        if (prices.size < period) {
            return List(prices.size) { prices.last() }
        }
        val k = 2.0 / (period + 1.0)
        val result = ArrayList<Double>(prices.size)
        // seed with SMA
        var seed = 0.0
        for (i in 0 until period) {
            seed += prices[i]
        }
        seed /= period
        result.add(seed)

        var prevEma = seed
        for (i in period until prices.size) {
            val currentEma = (prices[i] * k) + (prevEma * (1.0 - k))
            result.add(currentEma)
            prevEma = currentEma
        }
        return result
    }

    fun calculateRsi(prices: List<Double>, period: Int = 14): List<Double> {
        if (prices.size <= period) return emptyList()
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1 until prices.size) {
            val change = prices[i] - prices[i - 1]
            if (change >= 0) {
                gains.add(change)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(abs(change))
            }
        }

        val rsiList = mutableListOf<Double>()
        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()

        var rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
        rsiList.add(100.0 - (100.0 / (1.0 + rs)))

        for (i in period until gains.size) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period
            rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList.add(rsi.coerceIn(0.0, 100.0))
        }
        return rsiList
    }

    fun calculateMacd(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MacdResult {
        if (prices.size < slowPeriod + signalPeriod) {
            return MacdResult(0.0, 0.0, 0.0)
        }
        val fastEma = calculateEma(prices, fastPeriod)
        val slowEma = calculateEma(prices, slowPeriod)

        val macdSeries = mutableListOf<Double>()
        val offset = fastEma.size - slowEma.size
        for (i in slowEma.indices) {
            val fIndex = i + offset
            if (fIndex in fastEma.indices) {
                macdSeries.add(fastEma[fIndex] - slowEma[i])
            }
        }

        if (macdSeries.isEmpty()) return MacdResult(0.0, 0.0, 0.0)
        val signalSeries = calculateEma(macdSeries, signalPeriod)
        val latestMacd = macdSeries.lastOrNull() ?: 0.0
        val latestSignal = signalSeries.lastOrNull() ?: 0.0
        val histogram = latestMacd - latestSignal

        return MacdResult(latestMacd, latestSignal, histogram)
    }

    fun calculateBollingerBands(
        prices: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollingerBandsResult {
        if (prices.size < period) {
            val last = prices.lastOrNull() ?: 0.0
            return BollingerBandsResult(last, last * 1.02, last * 0.98, 0.04, 0.5)
        }
        val window = prices.takeLast(period)
        val sma = window.average()
        val variance = window.sumOf { (it - sma) * (it - sma) } / period
        val stdDev = sqrt(variance)

        val upper = sma + (multiplier * stdDev)
        val lower = sma - (multiplier * stdDev)
        val bandwidth = if (sma > 0) (upper - lower) / sma else 0.0
        val currentPrice = prices.last()
        val percentB = if (upper != lower) (currentPrice - lower) / (upper - lower) else 0.5

        return BollingerBandsResult(sma, upper, lower, bandwidth, percentB)
    }

    fun calculateAtr(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < 2) return 1.0
        val trList = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val high = candles[i].high
            val low = candles[i].low
            val prevClose = candles[i - 1].close
            val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
            trList.add(tr)
        }
        return if (trList.size >= period) {
            trList.takeLast(period).average()
        } else {
            trList.average()
        }
    }

    fun calculateSupertrend(candles: List<Candle>, period: Int = 10, multiplier: Double = 3.0): SupertrendResult {
        if (candles.size < period) {
            val close = candles.lastOrNull()?.close ?: 100.0
            return SupertrendResult(close, true, close * 1.03, close * 0.97)
        }
        val atr = calculateAtr(candles, period)
        val latest = candles.last()
        val hl2 = (latest.high + latest.low) / 2.0
        val upperBand = hl2 + (multiplier * atr)
        val lowerBand = hl2 - (multiplier * atr)

        val isBullish = latest.close > lowerBand
        val supertrend = if (isBullish) lowerBand else upperBand

        return SupertrendResult(supertrend, isBullish, upperBand, lowerBand)
    }

    fun calculateVolumeProfile(candles: List<Candle>, numBuckets: Int = 12): List<VolumeProfileLevel> {
        if (candles.isEmpty()) return emptyList()
        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val step = (maxPrice - minPrice) / numBuckets
        if (step <= 0) return emptyList()

        val bucketVolumes = DoubleArray(numBuckets)
        val bucketPrices = DoubleArray(numBuckets) { minPrice + (it + 0.5) * step }

        for (c in candles) {
            val avgP = (c.high + c.low + c.close) / 3.0
            val bucketIdx = ((avgP - minPrice) / step).toInt().coerceIn(0, numBuckets - 1)
            bucketVolumes[bucketIdx] += c.volume
        }

        val totalVolume = bucketVolumes.sum()
        val maxVolIdx = bucketVolumes.indices.maxByOrNull { bucketVolumes[it] } ?: 0

        // 70% value area around POC
        val targetVaVol = totalVolume * 0.70
        var currentVaVol = bucketVolumes[maxVolIdx]
        val inVa = BooleanArray(numBuckets)
        inVa[maxVolIdx] = true
        var left = maxVolIdx - 1
        var right = maxVolIdx + 1

        while (currentVaVol < targetVaVol && (left >= 0 || right < numBuckets)) {
            val leftVol = if (left >= 0) bucketVolumes[left] else -1.0
            val rightVol = if (right < numBuckets) bucketVolumes[right] else -1.0
            if (leftVol >= rightVol && left >= 0) {
                currentVaVol += leftVol
                inVa[left] = true
                left--
            } else if (right < numBuckets) {
                currentVaVol += rightVol
                inVa[right] = true
                right++
            } else if (left >= 0) {
                currentVaVol += leftVol
                inVa[left] = true
                left--
            } else {
                break
            }
        }

        return bucketPrices.mapIndexed { idx, price ->
            VolumeProfileLevel(
                price = price,
                volume = bucketVolumes[idx],
                isPoc = (idx == maxVolIdx),
                inValueArea = inVa[idx]
            )
        }
    }
}
