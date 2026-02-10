package com.blinkit.droiddex.network.models

import androidx.annotation.Keep

@Keep
public data class NetworkRawPerformanceMetrics(
    val bandwidthAverage: Double,
    val downloadSpeed: Int,
    val networkType: String,
    val signalLevel: Int,
    val signalStrength: Int,
    val isConnected: Boolean
)