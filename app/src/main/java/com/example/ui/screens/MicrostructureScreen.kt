package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import com.example.data.model.OrderBookLevel
import com.example.data.model.OrderBookState
import com.example.data.model.TradeEvent
import com.example.ui.components.CvdChart
import com.example.ui.components.DirectorKPIHeader
import com.example.ui.components.OrderBookDepthChart
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedBg
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenBg
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.QuantGold
import com.example.ui.theme.SurfaceCharcoal
import com.example.ui.viewmodel.MarketViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MicrostructureScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val orderBook by viewModel.orderBook.collectAsStateWithLifecycle()
    val trades by viewModel.trades.collectAsStateWithLifecycle()
    val cvdHistory by viewModel.cvdHistory.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
    val whaleAlerts by viewModel.whaleAlerts.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Depth & CVD, 1: Level 2 Book, 2: Live Trades

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
                activeAlertsCount = whaleAlerts.size
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sub Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = SurfaceCharcoal,
                contentColor = CyanAccent,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("DEPTH & CVD", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("L2 ORDER BOOK", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("TICK PRINTS (${trades.size})", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (activeSubTab) {
            0 -> {
                item {
                    OrderBookDepthChart(orderBookState = orderBook)
                    Spacer(modifier = Modifier.height(12.dp))
                    CvdChart(cvdPoints = cvdHistory)
                    Spacer(modifier = Modifier.height(12.dp))
                    WhaleAlertStreamCard(trades = trades.filter { it.isWhale })
                }
            }

            1 -> {
                item {
                    L2OrderBookView(orderBookState = orderBook)
                }
            }

            2 -> {
                item {
                    LiveTradeStreamView(trades = trades)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun L2OrderBookView(orderBookState: OrderBookState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SIZE (BTC)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                Text("BID PRICE", fontSize = 10.sp, color = BullGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("ASK PRICE", fontSize = 10.sp, color = BearRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("SIZE (BTC)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
            }

            val bids = orderBookState.bids.take(8)
            val asks = orderBookState.asks.take(8)
            val rows = maxOf(bids.size, asks.size)

            for (i in 0 until rows) {
                val bid = bids.getOrNull(i)
                val ask = asks.getOrNull(i)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bid Size
                    Text(
                        text = bid?.size?.let { String.format("%.3f", it) } ?: "-",
                        fontSize = 11.sp,
                        color = if (bid?.isWhale == true) QuantGold else MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (bid?.isWhale == true) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    // Bid Price
                    Text(
                        text = bid?.price?.let { String.format("%.2f", it) } ?: "-",
                        fontSize = 11.sp,
                        color = BullGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Ask Price
                    Text(
                        text = ask?.price?.let { String.format("%.2f", it) } ?: "-",
                        fontSize = 11.sp,
                        color = BearRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Ask Size
                    Text(
                        text = ask?.size?.let { String.format("%.3f", it) } ?: "-",
                        fontSize = 11.sp,
                        color = if (ask?.isWhale == true) QuantGold else MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (ask?.isWhale == true) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SPREAD: ${orderBookState.spread} USD", fontSize = 10.sp, color = QuantGold, fontFamily = FontFamily.Monospace)
                Text("IMBALANCE: ${String.format("%.2f", orderBookState.bidImbalanceRatio)} OBI", fontSize = 10.sp, color = CyanAccent, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun LiveTradeStreamView(trades: List<TradeEvent>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = "REAL-TIME EXECUTION PRINTS (TICK FLOW)",
                fontSize = 11.sp,
                color = CyanAccent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            trades.take(15).forEach { trade ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            if (trade.isWhale) QuantGold.copy(alpha = 0.1f)
                            else if (trade.isBuyerMaker) BearRedBg
                            else BullGreenBg,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (trade.isWhale) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = QuantGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (trade.isBuyerMaker) "SELL" else "BUY",
                            fontSize = 10.sp,
                            color = if (trade.isBuyerMaker) BearRed else BullGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$${String.format("%.2f", trade.price)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "${String.format("%.3f", trade.size)} BTC",
                        fontSize = 11.sp,
                        color = if (trade.isWhale) QuantGold else MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "${trade.latencyMs}ms",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun WhaleAlertStreamCard(trades: List<TradeEvent>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = QuantGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WHALE & INSTITUTIONAL FLOW TRACKER",
                    fontSize = 11.sp,
                    color = QuantGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (trades.isEmpty()) {
                Text(
                    text = "No whale orders (>4 BTC / ~$380k) detected in current 30s window.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                trades.take(5).forEach { whaleTrade ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(QuantGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WHALE ${if (whaleTrade.isBuyerMaker) "ASK ABSORPTION" else "BID AGGRESSION"}",
                                fontSize = 10.sp,
                                color = if (whaleTrade.isBuyerMaker) BearRed else BullGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Price: $${String.format("%.2f", whaleTrade.price)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "$${String.format("%.0f", whaleTrade.usdValue)}",
                            fontSize = 12.sp,
                            color = QuantGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
