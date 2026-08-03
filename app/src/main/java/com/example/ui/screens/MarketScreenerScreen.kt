package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketTicker
import com.example.ui.components.DirectorKPIHeader
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.QuantGold
import com.example.ui.theme.SurfaceCharcoal
import com.example.ui.viewmodel.MarketViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MarketScreenerScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val tickers by viewModel.tickers.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()

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
                text = "MICROSTRUCTURE MARKET SCREENER & PAIR FLOW",
                fontSize = 12.sp,
                color = CyanAccent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(tickers) { ticker ->
            TickerCardItem(
                ticker = ticker,
                isSelected = ticker.symbol == selectedSymbol,
                onSelect = { viewModel.selectSymbol(ticker.symbol) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TickerCardItem(
    ticker: MarketTicker,
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
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = ticker.symbol,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ticker.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${ticker.lastPrice}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (ticker.change24hPercent >= 0) "+" else ""}${ticker.change24hPercent}%",
                        fontSize = 10.sp,
                        color = if (ticker.change24hPercent >= 0) BullGreen else BearRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("24H VOLUME", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                    Text("$${String.format("%.1f", ticker.volume24hUsd / 1000000.0)}M", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
                }

                Column {
                    Text("ORDER BOOK IMBALANCE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                    Text(
                        text = "${if (ticker.obiRatio >= 0) "+" else ""}${ticker.obiRatio} OBI",
                        fontSize = 11.sp,
                        color = if (ticker.obiRatio >= 0) BullGreen else BearRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("CVD DELTA", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                    Text(
                        text = "${if (ticker.cvdDelta >= 0) "+" else ""}${ticker.cvdDelta} BTC",
                        fontSize = 11.sp,
                        color = if (ticker.cvdDelta >= 0) BullGreen else BearRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
