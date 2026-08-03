package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backtest_results")
data class BacktestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val strategyName: String,
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val netProfitPercent: Double,
    val totalTrades: Int,
    val winRatePercent: Double,
    val maxDrawdownPercent: Double,
    val sharpeRatio: Double,
    val p50LatencyMs: Long,
    val p99LatencyMs: Long,
    val verdict: String, // "APPROVED" or "REJECTED"
    val directorNotes: String
)

@Entity(tableName = "whale_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "WHALE_BUY", "WHALE_SELL", "ICEBERG_DETECTED", "OBI_SPIKE"
    val price: Double,
    val sizeUsd: Double,
    val note: String
)
