package com.example.data.model

data class MarketTicker(
    val symbol: String,
    val name: String,
    val lastPrice: Double,
    val change24hPercent: Double,
    val volume24hUsd: Double,
    val obiRatio: Double, // Order Book Imbalance (-1.0 to +1.0)
    val cvdDelta: Double,  // Cumulative Volume Delta
    val volatilityIndex: Double, // 0-100 scale
    val fundingRatePercent: Double,
    val isSelected: Boolean = false
)
