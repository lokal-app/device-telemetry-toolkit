package com.blinkit.droiddex.network.models

import androidx.annotation.Keep

@Keep
public data class NetworkThresholds(
    val low: NetworkLowThresholds = NetworkLowThresholds(),
    val average: NetworkAverageThresholds = NetworkAverageThresholds(),
    val high: NetworkHighThresholds = NetworkHighThresholds(),
    val excellent: NetworkExcellentThresholds = NetworkExcellentThresholds()
)

@Keep
public data class NetworkLowThresholds(
    val bandwidthAverageThreshold: Double = 150.0 // 0.15 Mbps
)

@Keep
public data class NetworkAverageThresholds(
    val bandwidthAverageThreshold: Double = 550.0, // 0.55 Mbps
    val downloadSpeedThreshold: Int = 2000, // 2 Mbps
    val signalStrengthThreshold: Int = 2
)

@Keep
public data class NetworkHighThresholds(
    val bandwidthAverageThreshold: Double = 2000.0, // 2 Mbps
    val downloadSpeedThreshold: Int = 5000, // 5 Mbps
    val signalStrengthThreshold: Int = 3
)

@Keep
public data class NetworkExcellentThresholds(
    val downloadSpeedThreshold: Int = 10000, // 10 Mbps
    val signalStrengthThreshold: Int = 4
)