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
    val deviceId: String,
    val cpu: CpuRawPerformanceMetrics?,
    val memory: MemoryRawPerformanceMetrics?,
    val network: NetworkRawPerformanceMetrics?,
    val storage: StorageRawPerformanceMetrics?,
    val battery: BatteryRawPerformanceMetrics?,
    val nativeExecutionStartMs: Long,
    val nativeExecutionEndMs: Long,
    val nativeExecutionDurationMs: Long
) {
    public fun toMap(): Map<String, Any?> = mapOf(
        "timestamp" to timestamp,
        "deviceName" to deviceName,
        "deviceId" to deviceId,
        "cpu" to cpu?.toMap(),
        "memory" to memory?.toMap(),
        "network" to network?.toMap(),
        "storage" to storage?.toMap(),
        "battery" to battery?.toMap(),
        "nativeExecutionStartMs" to nativeExecutionStartMs,
        "nativeExecutionEndMs" to nativeExecutionEndMs,
        "nativeExecutionDurationMs" to nativeExecutionDurationMs
    )
}

@Keep
public data class WeightedPerformanceLevels(
    val overallPerformanceLevel: PerformanceLevel,
    val cpu: PerformanceLevel?,
    val memory: PerformanceLevel?,
    val network: PerformanceLevel?,
    val storage: PerformanceLevel?,
    val battery: PerformanceLevel?
) {
    public fun toMap(): Map<String, Any?> = mapOf(
        "overallPerformanceLevel" to overallPerformanceLevel.name,
        "cpu" to cpu?.name,
        "memory" to memory?.name,
        "network" to network?.name,
        "storage" to storage?.name,
        "battery" to battery?.name
    )
}
