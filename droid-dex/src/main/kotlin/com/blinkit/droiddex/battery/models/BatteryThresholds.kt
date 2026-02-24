package com.blinkit.droiddex.battery.models

import androidx.annotation.Keep

@Keep
public data class BatteryThresholds(
    val excellent: BatteryExcellentThresholds = BatteryExcellentThresholds(),
    val high: BatteryHighThresholds = BatteryHighThresholds(),
    val average: BatteryAverageThresholds = BatteryAverageThresholds()
)

@Keep
public data class BatteryExcellentThresholds(
    val batteryPercentageThreshold: Float = 80f,
    val isChargingBatteryPercentageThreshold: Float = 70f,
    val temperatureThreshold: Float = 30f
)

@Keep
public data class BatteryHighThresholds(
    val batteryPercentageThreshold: Float = 55f,
    val isChargingBatteryPercentageThreshold: Float = 50f,
    val temperatureThreshold: Float = 34f
)

@Keep
public data class BatteryAverageThresholds(
    val batteryPercentageThreshold: Float = 40f,
    val isChargingBatteryPercentageThreshold: Float = 35f,
    val temperatureThreshold: Float = 38f
)
