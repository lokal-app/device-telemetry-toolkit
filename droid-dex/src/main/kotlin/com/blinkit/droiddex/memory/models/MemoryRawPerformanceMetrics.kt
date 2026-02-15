package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics

@Keep
public data class MemoryRawPerformanceMetrics(
    val availableRamGB: Float,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val isLowMemory: Boolean
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "availableRamGB" to availableRamGB,
        "heapLimitMB" to heapLimitMB,
        "heapUsedMB" to heapUsedMB,
        "heapRemainingMB" to heapRemainingMB,
        "isLowMemory" to isLowMemory
    )
}
