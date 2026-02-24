package com.blinkit.droiddex.memory

import android.content.Context
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.factory.base.PerformanceManager
import com.blinkit.droiddex.factory.providers.PerformanceManagerProvider
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.memory.models.MemoryDetailedMetrics
import com.blinkit.droiddex.memory.models.MemoryRawPerformanceMetrics
import com.blinkit.droiddex.memory.models.MemoryThresholds
import com.blinkit.droiddex.utils.convertBytesToMB
import com.blinkit.droiddex.utils.getApproxHeapLimitInMB
import com.blinkit.droiddex.utils.getApproxHeapRemainingInMB
import com.blinkit.droiddex.utils.getAvailableRamInGB
import com.blinkit.droiddex.utils.getMemoryInfo
import com.blinkit.droiddex.utils.getTotalRamInGB

internal class MemoryPerformanceManager(
    private val applicationContext: Context,
    private val thresholds: MemoryThresholds = MemoryThresholds()
): PerformanceManager() {

	override fun getPerformanceClass() = PerformanceClass.MEMORY

	override fun measurePerformanceLevel(): PerformanceLevel {
		if (getMemoryInfo(applicationContext, logger).lowMemory) {
			logInfo("DEVICE HAS LOW MEMORY")
			return PerformanceLevel.LOW
		}

		val availableRamInGB = getAvailableRamInGB(applicationContext, logger)
		val approxHeapLimitInMB = getApproxHeapLimitInMB(logger)
		val approxHeapRemainingInMB = getApproxHeapRemainingInMB(logger)

		return when {
			approxHeapRemainingInMB <= thresholds.low.approxHeapRemainingInMBThreshold ||
			approxHeapLimitInMB < thresholds.low.approxHeapLimitInMBThreshold -> PerformanceLevel.LOW

			approxHeapRemainingInMB <= thresholds.average.approxHeapRemainingInMBThreshold ||
			approxHeapLimitInMB < thresholds.average.approxHeapLimitInMBThreshold ||
			availableRamInGB <= thresholds.average.availableRamGBThreshold -> PerformanceLevel.AVERAGE

			approxHeapRemainingInMB <= thresholds.high.approxHeapRemainingInMBThreshold ||
			availableRamInGB <= thresholds.high.availableRamGBThreshold -> PerformanceLevel.HIGH

			else -> PerformanceLevel.EXCELLENT
		}
	}

	fun measurePerformanceLevel(rawMetrics: MemoryRawPerformanceMetrics): PerformanceLevel {
		if (rawMetrics.isLowMemory) {
			logInfo("DEVICE HAS LOW MEMORY")
			return PerformanceLevel.LOW
		}

		val availableRamInGB = rawMetrics.availableRamGB
		val approxHeapLimitInMB = rawMetrics.heapLimitMB
		val approxHeapRemainingInMB = rawMetrics.heapRemainingMB

		return when {
			approxHeapRemainingInMB <= thresholds.low.approxHeapRemainingInMBThreshold ||
			approxHeapLimitInMB < thresholds.low.approxHeapLimitInMBThreshold -> PerformanceLevel.LOW

			approxHeapRemainingInMB <= thresholds.average.approxHeapRemainingInMBThreshold ||
			approxHeapLimitInMB < thresholds.average.approxHeapLimitInMBThreshold ||
			availableRamInGB <= thresholds.average.availableRamGBThreshold -> PerformanceLevel.AVERAGE

			approxHeapRemainingInMB <= thresholds.high.approxHeapRemainingInMBThreshold ||
			availableRamInGB <= thresholds.high.availableRamGBThreshold -> PerformanceLevel.HIGH

			else -> PerformanceLevel.EXCELLENT
		}
	}

	override fun measureDetailedMetrics(): DetailedMetrics {
		val rawMetrics = extractRawPerformanceMetrics()

		return MemoryDetailedMetrics(
			performanceLevel = measurePerformanceLevel(rawMetrics),
			totalRamGB = rawMetrics.totalRamGB,
			availableRamGB = rawMetrics.availableRamGB,
			ramUsagePercent = rawMetrics.ramUsagePercent,
			heapLimitMB = rawMetrics.heapLimitMB,
			heapUsedMB = rawMetrics.heapUsedMB,
			heapRemainingMB = rawMetrics.heapRemainingMB,
			nativeHeapAllocatedMB = rawMetrics.nativeHeapAllocatedMB,
			isLowMemory = rawMetrics.isLowMemory
		)
	}

	 override fun extractRawPerformanceMetrics(): MemoryRawPerformanceMetrics {
		val memInfo = getMemoryInfo(applicationContext, logger)
		val totalRamInGB = getTotalRamInGB(applicationContext, logger)
		val availableRamInGB = getAvailableRamInGB(applicationContext, logger)
		val ramUsagePercent = calculateRamUsagePercent(memInfo)
		val approxHeapLimitInMB = getApproxHeapLimitInMB(logger)
		val approxHeapRemainingInMB = getApproxHeapRemainingInMB(logger)
		val heapUsedMB = approxHeapLimitInMB - approxHeapRemainingInMB
		val nativeHeapAllocatedMB = convertBytesToMB(android.os.Debug.getNativeHeapAllocatedSize())

		return MemoryRawPerformanceMetrics(
			totalRamGB = totalRamInGB,
			availableRamGB = availableRamInGB,
			ramUsagePercent = ramUsagePercent,
			heapLimitMB = approxHeapLimitInMB,
			heapUsedMB = heapUsedMB,
			heapRemainingMB = approxHeapRemainingInMB,
			nativeHeapAllocatedMB = nativeHeapAllocatedMB,
			isLowMemory = memInfo.lowMemory
		)
	}

	private fun calculateRamUsagePercent(memInfo: android.app.ActivityManager.MemoryInfo): Int {
		val totalMem = memInfo.totalMem
		if (totalMem <= 0) return 0
		return ((totalMem - memInfo.availMem) * 100 / totalMem).toInt().coerceIn(0, 100)
	}

	companion object: PerformanceManagerProvider {
		override fun create(applicationContext: Context): PerformanceManager =
			MemoryPerformanceManager(applicationContext)
	}
}
