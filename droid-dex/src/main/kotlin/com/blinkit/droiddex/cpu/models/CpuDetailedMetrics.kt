package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics

@Keep
public data class CpuDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val currentCpuFrequency: Float,
    val currentCpuUsagePercent: Int,
    val totalRamGB: Float,
    val androidVersion: Int,
    val mediaPerformanceClass: Int,
    val heapLimitMB: Float
) : DetailedMetrics()