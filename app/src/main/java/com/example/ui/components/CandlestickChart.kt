package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.engine.indicators.TechnicalIndicators
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.ChartBandFill
import com.example.ui.theme.ChartEma20Color
import com.example.ui.theme.ChartEma50Color
import com.example.ui.theme.ChartGridColor
import com.example.ui.theme.ChartVwapColor
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    showEma: Boolean = true,
    showBollinger: Boolean = true,
    showVwap: Boolean = true,
    selectedSignalPrice: Double? = null,
    stopLossPrice: Double? = null,
    takeProfitPrice: Double? = null
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(TerminalSurface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Awaiting Market Feed...", color = TextSecondary, fontSize = 13.sp)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val displayCandles = remember(candles) { candles.takeLast(45) }
    val closePrices = remember(displayCandles) { displayCandles.map { it.close } }

    val ema20 = remember(closePrices) { TechnicalIndicators.calculateEma(closePrices, 10) }
    val ema50 = remember(closePrices) { TechnicalIndicators.calculateEma(closePrices, 20) }

    val activeCandle = selectedIndex?.let { displayCandles.getOrNull(it) } ?: displayCandles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalSurface, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("candlestick_chart_container")
    ) {
        // Dynamic Chart Header / HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeCandle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "O: ${String.format("%.2f", activeCandle.open)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "H: ${String.format("%.2f", activeCandle.high)}",
                        color = BullishGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "L: ${String.format("%.2f", activeCandle.low)}",
                        color = BearishRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "C: ${String.format("%.2f", activeCandle.close)}",
                        color = if (activeCandle.isBullish) BullishGreen else BearishRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showEma) {
                    Text("EMA20", color = ChartEma20Color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EMA50", color = ChartEma50Color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (showVwap) {
                    Text("VWAP", color = ChartVwapColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayCandles) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                val x = change.position.x
                                val candleWidth = size.width / displayCandles.size
                                val index = (x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                                selectedIndex = index
                            },
                            onDragEnd = { selectedIndex = null },
                            onDragCancel = { selectedIndex = null }
                        )
                    }
                    .pointerInput(displayCandles) {
                        detectTapGestures { offset ->
                            val candleWidth = size.width / displayCandles.size
                            val index = (offset.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val numCandles = displayCandles.size

                val candleWidth = chartWidth / numCandles
                val bodyWidth = candleWidth * 0.70f

                var minPrice = displayCandles.minOf { it.low }
                var maxPrice = displayCandles.maxOf { it.high }

                if (stopLossPrice != null) minPrice = min(minPrice, stopLossPrice)
                if (takeProfitPrice != null) maxPrice = max(maxPrice, takeProfitPrice)

                val priceRange = max(0.01, maxPrice - minPrice) * 1.08
                val adjustedMinPrice = minPrice - (priceRange * 0.04)

                fun priceToY(price: Double): Float {
                    val normalized = (price - adjustedMinPrice) / priceRange
                    return (chartHeight * (1.0 - normalized)).toFloat()
                }

                // Draw Horizontal Grid Lines
                val numGrids = 4
                for (g in 0..numGrids) {
                    val gridPrice = adjustedMinPrice + (priceRange * (g.toDouble() / numGrids))
                    val y = priceToY(gridPrice)
                    drawLine(
                        color = ChartGridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Target Profit Line
                if (takeProfitPrice != null) {
                    val tpY = priceToY(takeProfitPrice)
                    drawLine(
                        color = BullishGreen,
                        start = Offset(0f, tpY),
                        end = Offset(chartWidth, tpY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Draw Stop Loss Line
                if (stopLossPrice != null) {
                    val slY = priceToY(stopLossPrice)
                    drawLine(
                        color = BearishRed,
                        start = Offset(0f, slY),
                        end = Offset(chartWidth, slY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Draw Candlesticks
                for (i in displayCandles.indices) {
                    val candle = displayCandles[i]
                    val centerX = (i * candleWidth) + (candleWidth / 2f)
                    val candleColor = if (candle.isBullish) BullishGreen else BearishRed

                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)
                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)

                    // Draw Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 2f
                    )

                    // Draw Candle Body
                    val topY = min(openY, closeY)
                    val height = max(2f, kotlin.math.abs(closeY - openY))
                    val left = centerX - (bodyWidth / 2f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(left, topY),
                        size = Size(bodyWidth, height)
                    )
                }

                // Draw EMA 20 Path
                if (showEma && ema20.isNotEmpty()) {
                    val emaPath = Path()
                    val offset = displayCandles.size - ema20.size
                    for (i in ema20.indices) {
                        val candleIdx = i + offset
                        val x = (candleIdx * candleWidth) + (candleWidth / 2f)
                        val y = priceToY(ema20[i])
                        if (i == 0) emaPath.moveTo(x, y) else emaPath.lineTo(x, y)
                    }
                    drawPath(emaPath, color = ChartEma20Color, style = Stroke(width = 2.5f))
                }

                // Draw EMA 50 Path
                if (showEma && ema50.isNotEmpty()) {
                    val ema50Path = Path()
                    val offset = displayCandles.size - ema50.size
                    for (i in ema50.indices) {
                        val candleIdx = i + offset
                        val x = (candleIdx * candleWidth) + (candleWidth / 2f)
                        val y = priceToY(ema50[i])
                        if (i == 0) ema50Path.moveTo(x, y) else ema50Path.lineTo(x, y)
                    }
                    drawPath(ema50Path, color = ChartEma50Color, style = Stroke(width = 2.5f))
                }

                // Draw Crosshair on touch
                if (selectedIndex != null) {
                    val idx = selectedIndex!!
                    val crossX = (idx * candleWidth) + (candleWidth / 2f)
                    val candle = displayCandles[idx]
                    val crossY = priceToY(candle.close)

                    drawLine(
                        color = TextPrimary.copy(alpha = 0.6f),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, chartHeight),
                        strokeWidth = 1.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawLine(
                        color = TextPrimary.copy(alpha = 0.6f),
                        start = Offset(0f, crossY),
                        end = Offset(chartWidth, crossY),
                        strokeWidth = 1.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawCircle(color = NeonCyan, radius = 5f, center = Offset(crossX, crossY))
                }
            }
        }
    }
}
