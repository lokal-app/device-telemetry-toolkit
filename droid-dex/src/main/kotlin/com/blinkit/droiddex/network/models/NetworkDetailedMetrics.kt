package com.blinkit.droiddex.network.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics

@Keep
public data class NetworkDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val bandwidthAverage: Double,
    val downloadSpeed: Int,
    val uploadSpeed: Int,
    val networkType: String,
    val cellularType: String,
    val carrierName: String,
    val signalStrength: Int,
    val isConnected: Boolean
) : DetailedMetrics()