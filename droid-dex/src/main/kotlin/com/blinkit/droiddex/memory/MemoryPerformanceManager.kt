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
import com.blinkit.droiddex.utils.getApproxHeapLimitInMB
import com.blinkit.droiddex.utils.getApproxHeapRemainingInMB
import com.blinkit.droiddex.utils.getAvailableRamInGB
import com.blinkit.droiddex.utils.getMemoryInfo

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

	override fun measureDetailedMetrics(): DetailedMetrics {
		val memInfo = getMemoryInfo(applicationContext, logger)
		val availableRamInGB = getAvailableRamInGB(applicationContext, logger)
		val approxHeapLimitInMB = getApproxHeapLimitInMB(logger)
		val approxHeapRemainingInMB = getApproxHeapRemainingInMB(logger)
		val heapUsedMB = approxHeapLimitInMB - approxHeapRemainingInMB

		return MemoryDetailedMetrics(
			performanceLevel = measurePerformanceLevel(),
			availableRamGB = availableRamInGB,
			heapLimitMB = approxHeapLimitInMB,
			heapUsedMB = heapUsedMB,
			heapRemainingMB = approxHeapRemainingInMB,
			isLowMemory = memInfo.lowMemory
		)
	}

	 override fun extractRawPerformanceMetrics(): MemoryRawPerformanceMetrics {
		val memInfo = getMemoryInfo(applicationContext, logger)
		val availableRamInGB = getAvailableRamInGB(applicationContext, logger)
		val approxHeapLimitInMB = getApproxHeapLimitInMB(logger)
		val approxHeapRemainingInMB = getApproxHeapRemainingInMB(logger)
		val heapUsedMB = approxHeapLimitInMB - approxHeapRemainingInMB

		return MemoryRawPerformanceMetrics(
			availableRamGB = availableRamInGB,
			heapLimitMB = approxHeapLimitInMB,
			heapUsedMB = heapUsedMB,
			heapRemainingMB = approxHeapRemainingInMB,
			isLowMemory = memInfo.lowMemory
		)
	}

	companion object: PerformanceManagerProvider {
		override fun create(applicationContext: Context): PerformanceManager =
			MemoryPerformanceManager(applicationContext)
	}
}
