package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToTwoDecimals

@Keep
public data class BatteryRawPerformanceMetrics(
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "batteryPercentage" to batteryPercentage.roundToTwoDecimals(),
        "isCharging" to isCharging,
        "batteryStatus" to batteryStatus,
        "temperature" to temperature.roundToTwoDecimals(),
        "voltage" to voltage.roundToTwoDecimals()
    )
}
