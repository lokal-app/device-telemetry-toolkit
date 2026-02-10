package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics

@Keep
public data class MemoryDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val availableRamGB: Float,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val isLowMemory: Boolean
) : DetailedMetrics()