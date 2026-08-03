package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuantStrategy
import com.example.ui.components.DirectorKPIHeader
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.QuantGold
import com.example.ui.theme.SurfaceCharcoal
import com.example.ui.viewmodel.MarketViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun QuantStrategyScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val strategies by viewModel.strategies.collectAsStateWithLifecycle()
    val isBacktesting by viewModel.isBacktesting.collectAsStateWithLifecycle()
    val lastBacktestResult by viewModel.lastBacktestResult.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()

    var selectedStrategy by remember { mutableStateOf<QuantStrategy?>(null) }
    var obiThreshold by remember { mutableStateOf(0.60f) }
    var maxLatencyAllowed by remember { mutableStateOf(8f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            DirectorKPIHeader(
                selectedSymbol = selectedSymbol,
                activeAlertsCount = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Text(
                text = "STRATEGY BACKTEST BENCHMARK & DIRECTOR VERDICTS",
                fontSize = 12.sp,
                color = CyanAccent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Backtest Control Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = QuantGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STRATEGY CONFIGURATOR :: ${selectedStrategy?.name ?: "Select Below"}",
                            fontSize = 11.sp,
                            color = QuantGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Target OBI Threshold: ${String.format("%.2f", obiThreshold)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = obiThreshold,
                        onValueChange = { obiThreshold = it },
                        valueRange = 0.30f..0.90f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Max Allowed Tick-to-Signal Latency: ${maxLatencyAllowed.toInt()}ms",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = maxLatencyAllowed,
                        onValueChange = { maxLatencyAllowed = it },
                        valueRange = 3f..20f,
                        colors = SliderDefaults.colors(thumbColor = QuantGold, activeTrackColor = QuantGold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val strat = selectedStrategy ?: strategies.first()
                            viewModel.runBacktest(strat.copy(
                                targetObiThreshold = obiThreshold.toDouble(),
                                maxLatencyAllowedMs = maxLatencyAllowed.toLong()
                            ))
                        },
                        enabled = !isBacktesting,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isBacktesting) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUNNING 10,000 TICK BENCHMARK...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXECUTE STRATEGY BACKTEST", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = lastBacktestResult != null) {
                        lastBacktestResult?.let { res ->
                            val isApp = res.contains("APPROVED")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(if (isApp) BullGreen.copy(alpha = 0.15f) else BearRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isApp) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isApp) BullGreen else BearRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res,
                                    fontSize = 11.sp,
                                    color = if (isApp) BullGreen else BearRed,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available Strategies List
        item {
            Text(
                text = "REGISTERED QUANT STRATEGIES & ACCEPTANCE CRITERIA",
                fontSize = 12.sp,
                color = QuantGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(strategies) { strat ->
            StrategyCardItem(
                strategy = strat,
                isSelected = selectedStrategy?.id == strat.id,
                onSelect = {
                    selectedStrategy = strat
                    obiThreshold = strat.targetObiThreshold.toFloat()
                    maxLatencyAllowed = strat.maxLatencyAllowedMs.toFloat()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StrategyCardItem(
    strategy: QuantStrategy,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) CyanAccent.copy(alpha = 0.12f) else SurfaceCharcoal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (strategy.priorityTier) {
                                    "P0" -> BearRed
                                    "P1" -> QuantGold
                                    else -> CyanAccent
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strategy.priorityTier,
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strategy.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strategy.description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("WIN RATE: ${strategy.winRate}%", fontSize = 10.sp, color = BullGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("SHARPE: ${strategy.sharpeRatio}", fontSize = 10.sp, color = QuantGold, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("MAX LATENCY: ${strategy.maxLatencyAllowedMs}ms", fontSize = 10.sp, color = CyanAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) CyanAccent else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSelected) "SELECTED FOR BENCHMARK" else "CONFIGURE & BENCHMARK",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
