package com.blinkit.droiddex.cpu.models

import androidx.annotation.Keep

@Keep
public data class CpuThresholds(
    val excellent: CpuExcellentThresholds = CpuExcellentThresholds(),
    val low: CpuLowThresholds = CpuLowThresholds(),
    val average: CpuAverageThresholds = CpuAverageThresholds()
)

@Keep
public data class CpuExcellentThresholds(
    val mediaPerformanceClassThreshold: Int = 33, // Build.VERSION_CODES.TIRAMISU
    val totalRamGBThreshold: Float = 12f
)

@Keep
public data class CpuLowThresholds(
    val androidVersionThreshold: Int = 21,
    val coreCountThreshold: Int = 2,
    val heapLimitMBThreshold: Float = 100f,
    val maxCpuFrequencyThreshold1: Float = 1250f,
    val maxCpuFrequencyThreshold2: Float = 1600f,
    val maxCpuFrequencyThreshold3: Float = 1300f,
    val heapLimitMBThreshold2: Float = 128f,
    val androidVersionThreshold2: Int = 21,
    val androidVersionThreshold3: Int = 24,
    val totalRamGBThreshold: Float = 2f
)

@Keep
public data class CpuAverageThresholds(
    val coreCountThreshold: Int = 8,
    val heapLimitMBThreshold: Float = 160f,
    val maxCpuFrequencyThreshold: Float = 2055f,
    val androidVersionThreshold: Int = 23,
    val totalRamGBThreshold: Float = 6f
)