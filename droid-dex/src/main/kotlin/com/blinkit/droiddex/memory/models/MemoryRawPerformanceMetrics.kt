package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToTwoDecimals

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
        "totalRamGB" to totalRamGB.roundToTwoDecimals(),
        "availableRamGB" to availableRamGB.roundToTwoDecimals(),
        "ramUsagePercent" to ramUsagePercent,
        "heapLimitMB" to heapLimitMB.roundToTwoDecimals(),
        "heapUsedMB" to heapUsedMB.roundToTwoDecimals(),
        "heapRemainingMB" to heapRemainingMB.roundToTwoDecimals(),
        "nativeHeapAllocatedMB" to nativeHeapAllocatedMB.roundToTwoDecimals(),
        "isLowMemory" to isLowMemory
    )
}
