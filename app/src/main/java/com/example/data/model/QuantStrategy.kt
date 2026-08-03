package com.example.data.model

data class QuantStrategy(
    val id: String,
    val name: String,
    val priorityTier: String, // "P0", "P1", "P2"
    val description: String,
    val targetObiThreshold: Double,
    val minVolumeUsd: Double,
    val stopLossPercent: Double,
    val takeProfitPercent: Double,
    val maxLatencyAllowedMs: Long,
    val winRate: Double,
    val sharpeRatio: Double,
    val expectedLatencyMs: Long,
    val isActive: Boolean = false
)
