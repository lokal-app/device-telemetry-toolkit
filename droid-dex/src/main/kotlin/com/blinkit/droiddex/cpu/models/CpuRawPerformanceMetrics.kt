package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics

@Keep
public data class CpuRawPerformanceMetrics(
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val currentCpuFrequency: Float,
    val currentCpuUsagePercent: Int,
    val totalRamGB: Float,
    val androidVersion: Int,
    val mediaPerformanceClass: Int,
    val heapLimitMB: Float
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "coreCount" to coreCount,
        "maxCpuFrequency" to maxCpuFrequency,
        "currentCpuFrequency" to currentCpuFrequency,
        "currentCpuUsagePercent" to currentCpuUsagePercent,
        "totalRamGB" to totalRamGB,
        "androidVersion" to androidVersion,
        "mediaPerformanceClass" to mediaPerformanceClass,
        "heapLimitMB" to heapLimitMB
    )
}
