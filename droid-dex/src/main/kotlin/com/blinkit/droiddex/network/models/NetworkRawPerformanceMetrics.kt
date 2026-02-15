package com.blinkit.droiddex.network.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics

@Keep
public data class NetworkRawPerformanceMetrics(
    val bandwidthAverage: Double,
    val downloadSpeed: Int,
    val networkType: String,
    val signalLevel: Int,
    val signalStrength: Int,
    val isConnected: Boolean
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "bandwidthAverage" to bandwidthAverage,
        "downloadSpeed" to downloadSpeed,
        "networkType" to networkType,
        "signalLevel" to signalLevel,
        "signalStrength" to signalStrength,
        "isConnected" to isConnected
    )
}
