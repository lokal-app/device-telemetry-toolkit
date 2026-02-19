package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.roundToTwoDecimals

@Keep
public data class BatteryDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
) : DetailedMetrics() {
    override fun toMap(): Map<String, Any?> = mapOf(
        "performanceLevel" to performanceLevel.name,
        "batteryPercentage" to batteryPercentage.roundToTwoDecimals(),
        "isCharging" to isCharging,
        "batteryStatus" to batteryStatus,
        "temperature" to temperature.roundToTwoDecimals(),
        "voltage" to voltage.roundToTwoDecimals()
    )
}