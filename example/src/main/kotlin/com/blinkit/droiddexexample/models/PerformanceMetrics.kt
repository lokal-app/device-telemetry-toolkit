package com.blinkit.droiddexexample.models

import android.annotation.SuppressLint
import com.blinkit.droiddex.models.PerformanceLevel

sealed class PerformanceMetrics {
    abstract val performanceLevel: PerformanceLevel
    abstract fun getDisplayMetrics(): List<MetricItem>
}

data class MetricItem(
    val label: String,
    val value: String,
    val unit: String = ""
)

data class CpuMetrics(
    override val performanceLevel: PerformanceLevel,
    val coreCount: Int,
    val maxCpuFrequency: Float,
    val currentCpuFrequency: Float,
    val currentCpuUsagePercent: Int,
    val totalRamGB: Float,
    val androidVersion: Int,
    val mediaPerformanceClass: Int,
    val heapLimitMB: Float
) : PerformanceMetrics() {
    @SuppressLint("DefaultLocale")
    override fun getDisplayMetrics() = listOf(
        MetricItem("CPU Usage", currentCpuUsagePercent.toString(), "%"),
        MetricItem("Current CPU Freq", String.format("%.2f", currentCpuFrequency / 1000), "GHz"),
        MetricItem("Max CPU Freq", String.format("%.2f", maxCpuFrequency / 1000), "GHz"),
        MetricItem("Core Count", coreCount.toString()),
        MetricItem("Total RAM", String.format("%.1f", totalRamGB), "GB"),
        MetricItem("Android Version", androidVersion.toString()),
        MetricItem("Media Performance Class", if (mediaPerformanceClass == 0) "0" else mediaPerformanceClass.toString()),
        MetricItem("Heap Limit", String.format("%.0f", heapLimitMB), "MB")
    )
}

data class MemoryMetrics(
    override val performanceLevel: PerformanceLevel,
    val totalRamGB: Float,
    val availableRamGB: Float,
    val ramUsagePercent: Int,
    val heapLimitMB: Float,
    val heapUsedMB: Float,
    val heapRemainingMB: Float,
    val nativeHeapAllocatedMB: Float,
    val isLowMemory: Boolean
) : PerformanceMetrics() {
    @SuppressLint("DefaultLocale")
    override fun getDisplayMetrics() = listOf(
        MetricItem("RAM Usage", ramUsagePercent.toString(), "%"),
        MetricItem("Total RAM", String.format("%.1f", totalRamGB), "GB"),
        MetricItem("Available RAM", String.format("%.2f", availableRamGB), "GB"),
        MetricItem("Native Heap", String.format("%.1f", nativeHeapAllocatedMB), "MB"),
        MetricItem("Heap Used", String.format("%.2f", heapUsedMB), "MB"),
        MetricItem("Heap Remaining", String.format("%.1f", heapRemainingMB), "MB"),
        MetricItem("Heap Limit", String.format("%.1f", heapLimitMB), "MB"),
        MetricItem("Low Memory", if (isLowMemory) "Yes" else "No")
    )
}

data class NetworkMetrics(
    override val performanceLevel: PerformanceLevel,
    val bandwidthAverage: Double,
    val downloadSpeed: Int,
    val networkType: String,
    val signalLevel: Int,
    val signalStrength: Int,
    val isConnected: Boolean
) : PerformanceMetrics() {
    @SuppressLint("DefaultLocale")
    override fun getDisplayMetrics() = listOf(
        MetricItem("Bandwidth Avg", String.format("%.1f", bandwidthAverage), "Kb/s"),
        MetricItem("Download Speed", downloadSpeed.toString(), "Kb/s"),
        MetricItem("Network Type", networkType),
        MetricItem("Signal Level", signalLevel.toString()),
        MetricItem("Signal Strength", signalStrength.toString()),
        MetricItem("Connected", if (isConnected) "Yes" else "No")
    )
}

data class StorageMetrics(
    override val performanceLevel: PerformanceLevel,
    val totalStorageGB: Float,
    val availableStorageGB: Float
) : PerformanceMetrics() {
    @SuppressLint("DefaultLocale")
    override fun getDisplayMetrics() = listOf(
        MetricItem("Total Storage", String.format("%.2f", totalStorageGB), "GB"),
        MetricItem("Available Storage", String.format("%.2f", availableStorageGB), "GB")
    )
}

data class BatteryMetrics(
    override val performanceLevel: PerformanceLevel,
    val batteryPercentage: Float,
    val isCharging: Boolean,
    val batteryStatus: String,
    val temperature: Float,
    val voltage: Float
) : PerformanceMetrics() {
    @SuppressLint("DefaultLocale")
    override fun getDisplayMetrics() = listOf(
        MetricItem("Battery", String.format("%.1f", batteryPercentage), "%"),
        MetricItem("Charging", if (isCharging) "Yes" else "No"),
        MetricItem("Status", batteryStatus),
        MetricItem("Temperature", String.format("%.1f", temperature), "°C"),
        MetricItem("Voltage", String.format("%.2f", voltage), "V")
    )
}

