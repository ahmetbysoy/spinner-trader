package com.example.data.model

data class TradeEvent(
    val id: String,
    val timestamp: Long,
    val price: Double,
    val size: Double,
    val usdValue: Double,
    val isBuyerMaker: Boolean, // True = Sell market order, False = Buy market order
    val isWhale: Boolean = false,
    val latencyMs: Long = 4
)

data class CvdPoint(
    val timestamp: Long,
    val cvdValue: Double,
    val deltaVolume: Double
)
