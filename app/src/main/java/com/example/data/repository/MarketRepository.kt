package com.example.data.repository

import com.example.data.local.AlertDao
import com.example.data.local.AlertEntity
import com.example.data.local.BacktestDao
import com.example.data.local.BacktestEntity
import com.example.data.model.CvdPoint
import com.example.data.model.MarketTicker
import com.example.data.model.OrderBookLevel
import com.example.data.model.OrderBookState
import com.example.data.model.QuantStrategy
import com.example.data.model.TradeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

class MarketRepository(
    private val backtestDao: BacktestDao,
    private val alertDao: AlertDao,
    private val externalScope: CoroutineScope
) {
    // Tickers
    private val _tickers = MutableStateFlow(
        listOf(
            MarketTicker("BTC/USDT", "Bitcoin", 94820.0, 3.42, 1420000000.0, 0.28, +142.5, 42.0, 0.0100, isSelected = true),
            MarketTicker("ETH/USDT", "Ethereum", 3280.5, -1.15, 840000000.0, -0.12, -38.2, 58.0, 0.0085),
            MarketTicker("SOL/USDT", "Solana", 184.2, 8.65, 620000000.0, 0.65, +210.8, 76.0, 0.0150),
            MarketTicker("AVAX/USDT", "Avalanche", 36.8, 2.10, 180000000.0, 0.05, +12.4, 38.0, 0.0090),
            MarketTicker("LINK/USDT", "Chainlink", 22.4, 4.30, 210000000.0, 0.35, +45.0, 48.0, 0.0110),
            MarketTicker("BNB/USDT", "Binance Coin", 645.0, 0.80, 310000000.0, -0.05, -5.2, 28.0, 0.0050)
        )
    )
    val tickers: StateFlow<List<MarketTicker>> = _tickers.asStateFlow()

    // Active Selected Ticker
    private val _selectedSymbol = MutableStateFlow("BTC/USDT")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    // Order Book
    private val _orderBook = MutableStateFlow(OrderBookState())
    val orderBook: StateFlow<OrderBookState> = _orderBook.asStateFlow()

    // Trades Stream
    private val _trades = MutableStateFlow<List<TradeEvent>>(emptyList())
    val trades: StateFlow<List<TradeEvent>> = _trades.asStateFlow()

    // CVD Stream
    private val _cvdHistory = MutableStateFlow<List<CvdPoint>>(emptyList())
    val cvdHistory: StateFlow<List<CvdPoint>> = _cvdHistory.asStateFlow()

    // Available Quant Strategies
    private val _strategies = MutableStateFlow(
        listOf(
            QuantStrategy(
                id = "strat_obi_scalp",
                name = "Order Book Imbalance (OBI) High-Frequency Scalper",
                priorityTier = "P0",
                description = "Captures micro-liquidity shifts when top 5 level bid/ask imbalance exceeds +0.6 or -0.6. Targets < 6ms tick-to-order execution.",
                targetObiThreshold = 0.60,
                minVolumeUsd = 250000.0,
                stopLossPercent = 0.25,
                takeProfitPercent = 0.60,
                maxLatencyAllowedMs = 8,
                winRate = 71.4,
                sharpeRatio = 2.85,
                expectedLatencyMs = 4,
                isActive = true
            ),
            QuantStrategy(
                id = "strat_cvd_absorption",
                name = "CVD Volume Delta Absorption Spike Engine",
                priorityTier = "P1",
                description = "Detects aggressive market sell/buy volume absorption near support/resistance walls before price reversal triggers.",
                targetObiThreshold = 0.45,
                minVolumeUsd = 500000.0,
                stopLossPercent = 0.40,
                takeProfitPercent = 1.10,
                maxLatencyAllowedMs = 12,
                winRate = 66.2,
                sharpeRatio = 2.15,
                expectedLatencyMs = 6,
                isActive = false
            ),
            QuantStrategy(
                id = "strat_iceberg_detector",
                name = "Iceberg & Hidden Order Sweeper",
                priorityTier = "P1",
                description = "Monitors repeated limit refilling at single price levels. Flags hidden institutional liquidity reload.",
                targetObiThreshold = 0.50,
                minVolumeUsd = 1000000.0,
                stopLossPercent = 0.30,
                takeProfitPercent = 0.85,
                maxLatencyAllowedMs = 10,
                winRate = 68.8,
                sharpeRatio = 2.40,
                expectedLatencyMs = 5,
                isActive = true
            ),
            QuantStrategy(
                id = "strat_liquidity_sweep",
                name = "Liquidity Sweep Arbitrage Bot",
                priorityTier = "P2",
                description = "Front-runs stop-loss cascades during volatility spikes by identifying bid depth thinning ahead of liquidations.",
                targetObiThreshold = 0.70,
                minVolumeUsd = 750000.0,
                stopLossPercent = 0.50,
                takeProfitPercent = 1.40,
                maxLatencyAllowedMs = 15,
                winRate = 62.5,
                sharpeRatio = 1.95,
                expectedLatencyMs = 8,
                isActive = false
            )
        )
    )
    val strategies: StateFlow<List<QuantStrategy>> = _strategies.asStateFlow()

    // Room DB Flows
    val savedBacktests: Flow<List<BacktestEntity>> = backtestDao.getAllBacktests()
    val whaleAlerts: Flow<List<AlertEntity>> = alertDao.getRecentAlerts()

    // Cumulative CVD tracking
    private var cumulativeDelta = 120.0
    private var currentPrice = 94820.0

    init {
        startSimulatedMarketFeed()
    }

    fun selectTicker(symbol: String) {
        _selectedSymbol.value = symbol
        _tickers.update { list ->
            list.map { it.copy(isSelected = it.symbol == symbol) }
        }
        val sel = _tickers.value.find { it.symbol == symbol }
        if (sel != null) {
            currentPrice = sel.lastPrice
        }
        generateOrderBookSnapshot()
    }

    private fun startSimulatedMarketFeed() {
        externalScope.launch(Dispatchers.Default) {
            var step = 0
            while (true) {
                delay(350) // High frequency tick update
                step++

                // Update Price & Tickers
                val currentTicker = _tickers.value.find { it.symbol == _selectedSymbol.value } ?: _tickers.value.first()
                val priceChange = (Random.nextDouble() - 0.48) * (currentTicker.lastPrice * 0.0008)
                currentPrice = (currentPrice + priceChange).coerceAtLeast(0.1)

                // Update Ticker list
                _tickers.update { list ->
                    list.map { ticker ->
                        if (ticker.symbol == _selectedSymbol.value) {
                            val newChange = ticker.change24hPercent + (priceChange / ticker.lastPrice * 100)
                            val newObi = (ticker.obiRatio + (Random.nextDouble() - 0.5) * 0.1).coerceIn(-0.9, 0.9)
                            ticker.copy(
                                lastPrice = round(currentPrice * 100) / 100,
                                change24hPercent = round(newChange * 100) / 100,
                                obiRatio = round(newObi * 100) / 100
                            )
                        } else {
                            val pDiff = (Random.nextDouble() - 0.5) * (ticker.lastPrice * 0.0004)
                            ticker.copy(lastPrice = round((ticker.lastPrice + pDiff) * 100) / 100)
                        }
                    }
                }

                // Generate Order Book
                generateOrderBookSnapshot()

                // Generate Trade Print
                val isBuyerMaker = Random.nextBoolean()
                val size = if (Random.nextDouble() > 0.88) Random.nextDouble(5.0, 35.0) else Random.nextDouble(0.1, 3.5)
                val isWhale = size > 4.0
                val usdVal = size * currentPrice
                val latency = Random.nextLong(2, 7)

                val newTrade = TradeEvent(
                    id = UUID.randomUUID().toString().take(8),
                    timestamp = System.currentTimeMillis(),
                    price = currentPrice,
                    size = round(size * 1000) / 1000,
                    usdValue = round(usdVal * 10) / 10,
                    isBuyerMaker = isBuyerMaker,
                    isWhale = isWhale,
                    latencyMs = latency
                )

                _trades.update { current ->
                    (listOf(newTrade) + current).take(40)
                }

                // Update CVD
                val delta = if (isBuyerMaker) -size else +size
                cumulativeDelta += delta
                val cvdPoint = CvdPoint(System.currentTimeMillis(), cumulativeDelta, delta)
                _cvdHistory.update { history ->
                    (history + cvdPoint).takeLast(30)
                }

                // Trigger Whale Alert if whale trade detected
                if (isWhale) {
                    val alertType = if (isBuyerMaker) "WHALE_SELL" else "WHALE_BUY"
                    val alert = AlertEntity(
                        symbol = _selectedSymbol.value,
                        type = alertType,
                        price = currentPrice,
                        sizeUsd = usdVal,
                        note = "Large ${if (isBuyerMaker) "Ask Absorption" else "Bid Aggression"} detected ($size BTC / $${round(usdVal)})"
                    )
                    alertDao.insertAlert(alert)
                }
            }
        }
    }

    private fun generateOrderBookSnapshot() {
        val basePrice = currentPrice
        val stepPrice = basePrice * 0.0003
        var totalBidVol = 0.0
        var totalAskVol = 0.0

        val bids = (1..10).map { i ->
            val p = basePrice - (i * stepPrice)
            val sz = Random.nextDouble(0.5, 12.0) * (1.0 + sin(i * 0.5))
            val isWhale = sz > 10.0
            totalBidVol += sz
            OrderBookLevel(round(p * 100) / 100, round(sz * 1000) / 1000, round(totalBidVol * 100) / 100, isWhale)
        }

        val asks = (1..10).map { i ->
            val p = basePrice + (i * stepPrice)
            val sz = Random.nextDouble(0.5, 12.0) * (1.0 + sin(i * 0.5))
            val isWhale = sz > 10.0
            totalAskVol += sz
            OrderBookLevel(round(p * 100) / 100, round(sz * 1000) / 1000, round(totalAskVol * 100) / 100, isWhale)
        }

        val spread = round((asks.first().price - bids.first().price) * 100) / 100
        val imbalance = if (totalBidVol + totalAskVol > 0) totalBidVol / (totalBidVol + totalAskVol) else 0.5

        _orderBook.value = OrderBookState(
            bids = bids,
            asks = asks,
            spread = spread,
            bidImbalanceRatio = round(imbalance * 100) / 100,
            topBid = bids.first().price,
            topAsk = asks.first().price
        )
    }

    suspend fun runStrategyBacktest(strategy: QuantStrategy, symbol: String): BacktestEntity {
        delay(1200) // Simulate processing 1,000,000 tick updates

        val winRate = (strategy.winRate + (Random.nextDouble() - 0.4) * 4.0).coerceIn(50.0, 92.0)
        val profit = (winRate * 0.8 - Random.nextDouble(5.0, 15.0)).coerceIn(-5.0, 42.0)
        val sharpe = (strategy.sharpeRatio + (Random.nextDouble() - 0.5) * 0.3).coerceAtLeast(0.8)
        val p50 = Random.nextLong(2, 5)
        val p99 = Random.nextLong(p50 + 2, strategy.maxLatencyAllowedMs + 4)

        val isApproved = p99 <= strategy.maxLatencyAllowedMs && sharpe >= 1.8 && profit > 0
        val verdict = if (isApproved) "APPROVED" else "REJECTED"
        val notes = if (isApproved) {
            "✅ ACCEPTANCE CRITERIA MET: Latency p99 (${p99}ms <= ${strategy.maxLatencyAllowedMs}ms target). Sharpe ratio ($sharpe) verified across 10k tick window."
        } else {
            "❌ REJECTED BY TRADING DIRECTOR: Latency spike p99 (${p99}ms exceeded max ${strategy.maxLatencyAllowedMs}ms) or Sharpe ($sharpe) below 1.8 limit."
        }

        val backtest = BacktestEntity(
            strategyName = strategy.name,
            symbol = symbol,
            netProfitPercent = round(profit * 100) / 100,
            totalTrades = Random.nextInt(450, 1800),
            winRatePercent = round(winRate * 10) / 10,
            maxDrawdownPercent = round(Random.nextDouble(1.5, 6.2) * 10) / 10,
            sharpeRatio = round(sharpe * 100) / 100,
            p50LatencyMs = p50,
            p99LatencyMs = p99,
            verdict = verdict,
            directorNotes = notes
        )

        backtestDao.insertBacktest(backtest)
        return backtest
    }

    suspend fun clearAlerts() {
        alertDao.clearAlerts()
    }

    suspend fun clearBacktests() {
        backtestDao.clearAll()
    }
}
