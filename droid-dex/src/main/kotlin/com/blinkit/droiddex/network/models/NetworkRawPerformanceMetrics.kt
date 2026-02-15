package com.blinkit.droiddex.network.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics

@Keep
public data class NetworkRawPerformanceMetrics(
    val bandwidthAverage: Double,
    val downloadSpeed: Int,
    val uploadSpeed: Int,
    val networkType: String,
    val cellularType: String,
    val carrierName: String,
    val signalStrength: Int,
    val isConnected: Boolean
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "bandwidthAverage" to bandwidthAverage,
        "downloadSpeed" to downloadSpeed,
        "uploadSpeed" to uploadSpeed,
        "networkType" to networkType,
        "cellularType" to cellularType,
        "carrierName" to carrierName,
        "signalStrength" to signalStrength,
        "isConnected" to isConnected
    )
}
