package com.example.data.model

data class OrderBookLevel(
    val price: Double,
    val size: Double,
    val totalVolume: Double,
    val isWhale: Boolean = false
)

data class OrderBookState(
    val bids: List<OrderBookLevel> = emptyList(),
    val asks: List<OrderBookLevel> = emptyList(),
    val spread: Double = 0.0,
    val bidImbalanceRatio: Double = 0.5, // 0.0 = 100% Ask, 1.0 = 100% Bid
    val topBid: Double = 0.0,
    val topAsk: Double = 0.0
)
