package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep

@Keep
public data class MemoryRawPerformanceMetrics(
    val availableRamGB: Float,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val isLowMemory: Boolean
)