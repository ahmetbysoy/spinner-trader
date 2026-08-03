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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CvdPoint
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.QuantGold
import com.example.ui.theme.SurfaceCharcoal

@Composable
fun CvdChart(
    cvdPoints: List<CvdPoint>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
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
                    text = "CVD (CUMULATIVE VOLUME DELTA) OSCILLATOR",
                    fontSize = 11.sp,
                    color = QuantGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                val lastDelta = cvdPoints.lastOrNull()?.cvdValue ?: 0.0
                Text(
                    text = "DELTA: ${if (lastDelta >= 0) "+" else ""}${String.format("%.1f", lastDelta)} BTC",
                    fontSize = 10.sp,
                    color = if (lastDelta >= 0) BullGreen else BearRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (cvdPoints.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height
                val midY = h / 2f

                val maxAbsCvd = cvdPoints.maxOfOrNull { kotlin.math.abs(it.cvdValue) }?.coerceAtLeast(10.0) ?: 10.0

                // Zero line
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.5f
                )

                val barWidth = (w / cvdPoints.size.coerceAtLeast(10)).coerceAtLeast(4f)
                val linePath = Path()

                cvdPoints.forEachIndexed { i, pt ->
                    val x = i * (w / (cvdPoints.size - 1).coerceAtLeast(1))
                    val cvdNorm = (pt.cvdValue / maxAbsCvd * (h / 2f)).toFloat()
                    val y = midY - cvdNorm

                    if (i == 0) {
                        linePath.moveTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                    }

                    // Delta Bar
                    val deltaNorm = (pt.deltaVolume / maxAbsCvd * (h / 2f)).toFloat()
                    val barColor = if (pt.deltaVolume >= 0) BullGreen.copy(alpha = 0.5f) else BearRed.copy(alpha = 0.5f)
                    
                    if (pt.deltaVolume >= 0) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x - barWidth / 2f, midY - deltaNorm),
                            size = Size(barWidth * 0.7f, deltaNorm)
                        )
                    } else {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x - barWidth / 2f, midY),
                            size = Size(barWidth * 0.7f, -deltaNorm)
                        )
                    }
                }

                // Draw CVD line overlay
                drawPath(
                    path = linePath,
                    color = CyanAccent,
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }
}
