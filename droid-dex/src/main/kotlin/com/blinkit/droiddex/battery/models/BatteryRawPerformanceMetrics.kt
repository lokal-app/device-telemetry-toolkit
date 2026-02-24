package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToPrecision

@Keep
public data class BatteryRawPerformanceMetrics(
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "batteryPercentage" to batteryPercentage.roundToPrecision(2),
        "isCharging" to isCharging,
        "batteryStatus" to batteryStatus,
        "temperature" to temperature.roundToPrecision(2),
        "voltage" to voltage.roundToPrecision(2)
    )
}
