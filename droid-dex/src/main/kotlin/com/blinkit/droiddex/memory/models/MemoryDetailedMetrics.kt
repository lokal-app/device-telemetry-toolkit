package com.blinkit.droiddex.memory.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.roundToTwoDecimals

@Keep
public data class MemoryDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val totalRamGB: Float,
    val availableRamGB: Float,
    val ramUsagePercent: Int,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val nativeHeapAllocatedMB: Float,
    val isLowMemory: Boolean
) : DetailedMetrics() {
    override fun toMap(): Map<String, Any?> = mapOf(
        "performanceLevel" to performanceLevel.name,
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