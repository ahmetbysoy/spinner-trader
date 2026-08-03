package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderBookState
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceCharcoal

@Composable
fun OrderBookDepthChart(
    orderBookState: OrderBookState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(SurfaceCharcoal, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL 2 CUMULATIVE DEPTH CHART",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "BID ${(orderBookState.bidImbalanceRatio * 100).toInt()}% | ASK ${((1.0 - orderBookState.bidImbalanceRatio) * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midX = w / 2f

                val bids = orderBookState.bids
                val asks = orderBookState.asks

                if (bids.isEmpty() || asks.isEmpty()) return@Canvas

                val maxBidVol = bids.lastOrNull()?.totalVolume ?: 100.0
                val maxAskVol = asks.lastOrNull()?.totalVolume ?: 100.0
                val maxVol = maxOf(maxBidVol, maxAskVol).coerceAtLeast(1.0)

                // Draw Mid Price Line
                drawLine(
                    color = Color.Gray,
                    start = androidx.compose.ui.geometry.Offset(midX, 0f),
                    end = androidx.compose.ui.geometry.Offset(midX, h),
                    strokeWidth = 2f
                )

                // Draw Bids (Green curve left)
                val bidPath = Path()
                val bidFillPath = Path()
                bidPath.moveTo(midX, h)
                bidFillPath.moveTo(midX, h)

                val bidStepX = midX / bids.size
                bids.forEachIndexed { i, level ->
                    val x = midX - (i + 1) * bidStepX
                    val y = h - (level.totalVolume / maxVol * h).toFloat()
                    bidPath.lineTo(x, y)
                    bidFillPath.lineTo(x, y)
                }
                bidFillPath.lineTo(0f, h)
                bidFillPath.close()

                drawPath(
                    path = bidFillPath,
                    color = BullGreen.copy(alpha = 0.25f)
                )
                drawPath(
                    path = bidPath,
                    color = BullGreen,
                    style = Stroke(width = 3f)
                )

                // Draw Asks (Red curve right)
                val askPath = Path()
                val askFillPath = Path()
                askPath.moveTo(midX, h)
                askFillPath.moveTo(midX, h)

                val askStepX = midX / asks.size
                asks.forEachIndexed { i, level ->
                    val x = midX + (i + 1) * askStepX
                    val y = h - (level.totalVolume / maxVol * h).toFloat()
                    askPath.lineTo(x, y)
                    askFillPath.lineTo(x, y)
                }
                askFillPath.lineTo(w, h)
                askFillPath.close()

                drawPath(
                    path = askFillPath,
                    color = BearRed.copy(alpha = 0.25f)
                )
                drawPath(
                    path = askPath,
                    color = BearRed,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
