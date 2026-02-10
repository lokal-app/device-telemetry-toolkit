package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep

@Keep
public data class MemoryThresholds(
    val low: MemoryLowThresholds = MemoryLowThresholds(),
    val average: MemoryAverageThresholds = MemoryAverageThresholds(),
    val high: MemoryHighThresholds = MemoryHighThresholds()
)

@Keep
public data class MemoryLowThresholds(
    val approxHeapRemainingInMBThreshold: Float = 64f,
    val approxHeapLimitInMBThreshold: Float = 128f
)

@Keep
public data class MemoryAverageThresholds(
    val approxHeapRemainingInMBThreshold: Float = 128f,
    val approxHeapLimitInMBThreshold: Float = 256f,
    val availableRamGBThreshold: Float = 2f
)

@Keep
public data class MemoryHighThresholds(
    val approxHeapRemainingInMBThreshold: Float = 256f,
    val availableRamGBThreshold: Float = 3f
)