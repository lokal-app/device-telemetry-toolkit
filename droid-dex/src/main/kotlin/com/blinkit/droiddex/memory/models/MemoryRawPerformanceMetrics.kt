package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToPrecision

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
        "totalRamGB" to totalRamGB.roundToPrecision(2),
        "availableRamGB" to availableRamGB.roundToPrecision(2),
        "ramUsagePercent" to ramUsagePercent,
        "heapLimitMB" to heapLimitMB.roundToPrecision(2),
        "heapUsedMB" to heapUsedMB.roundToPrecision(2),
        "heapRemainingMB" to heapRemainingMB.roundToPrecision(2),
        "nativeHeapAllocatedMB" to nativeHeapAllocatedMB.roundToPrecision(2),
        "isLowMemory" to isLowMemory
    )
}
