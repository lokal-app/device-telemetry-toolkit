package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToTwoDecimals

@Keep
public data class CpuRawPerformanceMetrics(
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val currentCpuFrequency: Float,
    val currentCpuUsagePercent: Int,
    val androidVersion: Int,
    val mediaPerformanceClass: Int
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "coreCount" to coreCount,
        "maxCpuFrequency" to maxCpuFrequency.roundToTwoDecimals(),
        "currentCpuFrequency" to currentCpuFrequency.roundToTwoDecimals(),
        "currentCpuUsagePercent" to currentCpuUsagePercent,
        "androidVersion" to androidVersion,
        "mediaPerformanceClass" to mediaPerformanceClass
    )
}
