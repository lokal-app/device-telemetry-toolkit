package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep

@Keep
public data class CpuRawPerformanceMetrics(
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val totalRamGB: Float,
    val androidVersion: Int,
    val mediaPerformanceClass: Int,
    val heapLimitMB: Float
)