package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep

@Keep
public data class BatteryRawPerformanceMetrics(
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
)