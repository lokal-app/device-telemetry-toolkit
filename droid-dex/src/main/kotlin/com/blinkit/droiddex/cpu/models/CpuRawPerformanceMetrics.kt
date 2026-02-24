package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToPrecision

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
        "maxCpuFrequency" to maxCpuFrequency.roundToPrecision(2),
        "currentCpuFrequency" to currentCpuFrequency.roundToPrecision(2),
        "currentCpuUsagePercent" to currentCpuUsagePercent,
        "androidVersion" to androidVersion,
        "mediaPerformanceClass" to mediaPerformanceClass
    )
}
