package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics

@Keep
public data class MemoryRawPerformanceMetrics(
    val totalRamGB: Float,
    val availableRamGB: Float,
    val ramUsagePercent: Int,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val nativeHeapAllocatedMB: Float,
    val isLowMemory: Boolean
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "totalRamGB" to totalRamGB,
        "availableRamGB" to availableRamGB,
        "ramUsagePercent" to ramUsagePercent,
        "heapLimitMB" to heapLimitMB,
        "heapUsedMB" to heapUsedMB,
        "heapRemainingMB" to heapRemainingMB,
        "nativeHeapAllocatedMB" to nativeHeapAllocatedMB,
        "isLowMemory" to isLowMemory
    )
}
