package com.blinkit.droiddex.models

import androidx.annotation.Keep
import com.blinkit.droiddex.battery.models.BatteryRawPerformanceMetrics
import com.blinkit.droiddex.cpu.models.CpuRawPerformanceMetrics
import com.blinkit.droiddex.memory.models.MemoryRawPerformanceMetrics
import com.blinkit.droiddex.network.models.NetworkRawPerformanceMetrics
import com.blinkit.droiddex.storage.models.StorageRawPerformanceMetrics

@Keep
public abstract class DetailedMetrics {
    public abstract val performanceLevel: PerformanceLevel
}

@Keep
public data class RawPerformanceDataResult(
    val timestamp: Long,
    val deviceName: String,
    val cpu: CpuRawPerformanceMetrics?,
    val memory: MemoryRawPerformanceMetrics?,
    val network: NetworkRawPerformanceMetrics?,
    val storage: StorageRawPerformanceMetrics?,
    val battery: BatteryRawPerformanceMetrics?
)

@Keep
public data class WeightedPerformanceLevels(
    val overallPerformanceLevel: PerformanceLevel,
    val cpu: PerformanceLevel?,
    val memory: PerformanceLevel?,
    val network: PerformanceLevel?,
    val storage: PerformanceLevel?,
    val battery: PerformanceLevel?
)

