package com.blinkit.droiddex.network.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.roundToPrecision

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
) : DetailedMetrics() {
    override fun toMap(): Map<String, Any?> = mapOf(
        "performanceLevel" to performanceLevel.name,
        "bandwidthAverage" to bandwidthAverage.roundToPrecision(2),
        "downloadSpeed" to downloadSpeed,
        "uploadSpeed" to uploadSpeed,
        "networkType" to networkType,
        "cellularType" to cellularType,
        "carrierName" to carrierName,
        "signalStrength" to signalStrength,
        "isConnected" to isConnected
    )
}