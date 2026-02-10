package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics

@Keep
public data class BatteryDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
) : DetailedMetrics()