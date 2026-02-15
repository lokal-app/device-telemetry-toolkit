package com.blinkit.droiddexexample.utils

import com.blinkit.droiddex.models.*
import com.blinkit.droiddex.battery.models.BatteryDetailedMetrics
import com.blinkit.droiddex.cpu.models.CpuDetailedMetrics
import com.blinkit.droiddex.memory.models.MemoryDetailedMetrics
import com.blinkit.droiddex.network.models.NetworkDetailedMetrics
import com.blinkit.droiddex.storage.models.StorageDetailedMetrics
import com.blinkit.droiddexexample.models.*

fun DetailedMetrics.toExampleMetrics(): PerformanceMetrics = when (this) {
    is CpuDetailedMetrics -> CpuMetrics(
        performanceLevel = performanceLevel,
        coreCount = coreCount,
        maxCpuFrequency = maxCpuFrequency,
        currentCpuFrequency = currentCpuFrequency,
        currentCpuUsagePercent = currentCpuUsagePercent,
        androidVersion = androidVersion,
        mediaPerformanceClass = mediaPerformanceClass
    )
    is MemoryDetailedMetrics -> MemoryMetrics(
        performanceLevel = performanceLevel,
        totalRamGB = totalRamGB,
        availableRamGB = availableRamGB,
        ramUsagePercent = ramUsagePercent,
        heapLimitMB = heapLimitMB,
        heapUsedMB = heapUsedMB,
        heapRemainingMB = heapRemainingMB,
        nativeHeapAllocatedMB = nativeHeapAllocatedMB,
        isLowMemory = isLowMemory
    )
    is NetworkDetailedMetrics -> NetworkMetrics(
        performanceLevel = performanceLevel,
        bandwidthAverage = bandwidthAverage,
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed,
        networkType = networkType,
        cellularType = cellularType,
        carrierName = carrierName,
        signalStrength = signalStrength,
        isConnected = isConnected
    )
    is StorageDetailedMetrics -> StorageMetrics(
        performanceLevel = performanceLevel,
        totalStorageGB = totalStorageGB,
        availableStorageGB = availableStorageGB
    )
    is BatteryDetailedMetrics -> BatteryMetrics(
        performanceLevel = performanceLevel,
        batteryPercentage = batteryPercentage,
        isCharging = isCharging,
        batteryStatus = batteryStatus,
        temperature = temperature,
        voltage = voltage
    )
    else -> throw IllegalArgumentException("Unsupported DetailedMetrics type: ${this::class}")
}