package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.roundToPrecision

@Keep
public data class CpuDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val currentCpuFrequency: Float,
    val currentCpuUsagePercent: Int,
    val androidVersion: Int,
    val mediaPerformanceClass: Int
) : DetailedMetrics() {
    override fun toMap(): Map<String, Any?> = mapOf(
        "performanceLevel" to performanceLevel.name,
        "coreCount" to coreCount,
        "maxCpuFrequency" to maxCpuFrequency.roundToPrecision(2),
        "currentCpuFrequency" to currentCpuFrequency.roundToPrecision(2),
        "currentCpuUsagePercent" to currentCpuUsagePercent,
        "androidVersion" to androidVersion,
        "mediaPerformanceClass" to mediaPerformanceClass
    )
}