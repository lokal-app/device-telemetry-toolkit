package com.blinkit.droiddex.factory.factory

import android.content.Context
import androidx.lifecycle.LiveData
import com.blinkit.droiddex.battery.BatteryPerformanceManager
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.cpu.CpuPerformanceManager
import com.blinkit.droiddex.factory.base.PerformanceManager
import com.blinkit.droiddex.memory.MemoryPerformanceManager
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.models.PerformanceThresholds
import com.blinkit.droiddex.models.RawPerformanceDataResult
import com.blinkit.droiddex.models.WeightedPerformanceLevels
import com.blinkit.droiddex.cpu.models.CpuRawPerformanceMetrics
import com.blinkit.droiddex.memory.models.MemoryRawPerformanceMetrics
import com.blinkit.droiddex.network.models.NetworkRawPerformanceMetrics
import com.blinkit.droiddex.storage.models.StorageRawPerformanceMetrics
import com.blinkit.droiddex.battery.models.BatteryRawPerformanceMetrics
import com.blinkit.droiddex.battery.models.BatteryThresholds
import com.blinkit.droiddex.cpu.models.CpuThresholds
import com.blinkit.droiddex.memory.models.MemoryThresholds
import com.blinkit.droiddex.network.models.NetworkThresholds
import com.blinkit.droiddex.storage.models.StorageThresholds
import com.blinkit.droiddex.network.NetworkPerformanceManager
import com.blinkit.droiddex.storage.StoragePerformanceManager
import com.blinkit.droiddex.utils.Logger
import androidx.lifecycle.MutableLiveData
import com.blinkit.droiddex.utils.getPerformanceLevelWithWeights
import com.blinkit.droiddex.utils.runAsyncPeriodically
import android.os.Build

internal class PerformanceManagerFactory(
    private val applicationContext: Context,
    private val thresholds: PerformanceThresholds? = null
) {

	private val performanceManagerMap = mutableMapOf<@PerformanceClass Int, PerformanceManager>()
	private val logger = Logger()

	// Raw data collection management
	private var rawPerformanceDataCollectionActive = false
	private val _rawPerformanceDataLiveData = MutableLiveData<RawPerformanceDataResult>()
	private var rawPerformanceDataClasses: IntArray = intArrayOf()
	private var rawPerformanceDataDelay: Int = 15

	init {
		PerformanceClass.values().forEach { getOrPut(it) }
	}

	fun getPerformanceLevel(@PerformanceClass performanceClass: Int): PerformanceLevel =
		getOrPut(performanceClass).performanceLevel

	fun getPerformanceLevelLd(@PerformanceClass performanceClass: Int): LiveData<PerformanceLevel> =
		getOrPut(performanceClass).performanceLevelLd

	fun getDetailedMetricsLd(@PerformanceClass performanceClass: Int): LiveData<DetailedMetrics> =
		getOrPut(performanceClass).detailedMetricsLd

	/**
	 * Starts continuous collection of raw performance data for specified performance classes (Flow 1)
	 * @param classes vararg list of performance classes to monitor
	 * @param delay interval in seconds between data collection cycles
	 * @return LiveData of RawPerformanceDataResult containing raw metrics from all requested classes
	 *
	 * Implementation details:
	 * - Sets collection flag and stores parameters for periodic execution
	 * - Configures delay for all requested performance managers
	 * - Initiates periodic data collection using runAsyncPeriodically
	 * - Emits RawPerformanceDataResult with timestamp, device info, and raw metrics
	 * - Continues until stopRawPerformanceDataCollection() is called
	 */
	fun startRawPerformanceDataCollection(
		vararg classes: Int,
		delay: Int
	): LiveData<RawPerformanceDataResult> {
		rawPerformanceDataCollectionActive = true
		rawPerformanceDataClasses = classes
		rawPerformanceDataDelay = delay

		// Set unified delay for all requested performance managers
		classes.forEach { performanceClass ->
			getOrPut(performanceClass).setDelay(delay.toFloat())
		}

		// Start periodic raw data collection
		startPeriodicRawDataCollection()

		return _rawPerformanceDataLiveData
	}

	private fun startPeriodicRawDataCollection() {
		runAsyncPeriodically({
			if (rawPerformanceDataCollectionActive) {
				try {
					val rawData = collectRawPerformanceData()
					_rawPerformanceDataLiveData.postValue(rawData)
				} catch (e: Exception) {
					logger.logError(e)
				}
			}
		}, delayInSecs = rawPerformanceDataDelay.toFloat())
	}

	private fun collectRawPerformanceData(): RawPerformanceDataResult {
		return RawPerformanceDataResult(
			timestamp = System.currentTimeMillis(),
			deviceName = Build.MODEL,
			cpu = if (PerformanceClass.CPU in rawPerformanceDataClasses) extractCpuRawMetrics() else null,
			memory = if (PerformanceClass.MEMORY in rawPerformanceDataClasses) extractMemoryRawMetrics() else null,
			network = if (PerformanceClass.NETWORK in rawPerformanceDataClasses) extractNetworkRawMetrics() else null,
			storage = if (PerformanceClass.STORAGE in rawPerformanceDataClasses) extractStorageRawMetrics() else null,
			battery = if (PerformanceClass.BATTERY in rawPerformanceDataClasses) extractBatteryRawMetrics() else null
		)
	}

	private fun extractCpuRawMetrics() =
		getOrPut(PerformanceClass.CPU).extractRawPerformanceMetrics() as? CpuRawPerformanceMetrics

	private fun extractMemoryRawMetrics() =
		getOrPut(PerformanceClass.MEMORY).extractRawPerformanceMetrics() as? MemoryRawPerformanceMetrics

	private fun extractNetworkRawMetrics() =
		getOrPut(PerformanceClass.NETWORK).extractRawPerformanceMetrics() as? NetworkRawPerformanceMetrics

	private fun extractStorageRawMetrics() =
		getOrPut(PerformanceClass.STORAGE).extractRawPerformanceMetrics() as? StorageRawPerformanceMetrics

	private fun extractBatteryRawMetrics() =
		getOrPut(PerformanceClass.BATTERY).extractRawPerformanceMetrics() as? BatteryRawPerformanceMetrics

	fun stopRawPerformanceDataCollection() {
		rawPerformanceDataCollectionActive = false
		rawPerformanceDataClasses = intArrayOf()
	}

	/**
	 * Calculates weighted performance levels for specified performance classes (Flow 2)
	 * @param classes vararg list of performance class and weight pairs
	 * @return WeightedPerformanceLevels containing individual levels and weighted overall level
	 *
	 * Implementation details:
	 * - Measures current performance level for each requested class
	 * - Stores individual performance levels for each class
	 * - Filters out UNKNOWN performance levels from weighted calculation
	 * - Calculates weighted average using getPerformanceLevelWithWeights utility
	 * - Returns structured result with both individual and overall performance levels
	 */
	fun getWeightedPerformanceLevels(
		vararg classes: Pair<Int, Float>
	): WeightedPerformanceLevels {
		val individualPerformanceLevels = mutableMapOf<Int, PerformanceLevel>()
		val weightedPairs = mutableListOf<Pair<PerformanceLevel, Float>>()

		classes.forEach { (performanceClass, weight) ->
			val level = getPerformanceLevel(performanceClass)
			individualPerformanceLevels[performanceClass] = level
			if (level != PerformanceLevel.UNKNOWN) {
				weightedPairs.add(Pair(level, weight))
			}
		}

		val overallLevel = getPerformanceLevelWithWeights(weightedPairs)

		return WeightedPerformanceLevels(
			overallPerformanceLevel = overallLevel,
			cpu = individualPerformanceLevels[PerformanceClass.CPU],
			memory = individualPerformanceLevels[PerformanceClass.MEMORY],
			network = individualPerformanceLevels[PerformanceClass.NETWORK],
			storage = individualPerformanceLevels[PerformanceClass.STORAGE],
			battery = individualPerformanceLevels[PerformanceClass.BATTERY]
		)
	}

	private fun getOrPut(@PerformanceClass performanceClass: Int): PerformanceManager =
		performanceManagerMap.getOrPut(performanceClass) {
			when (performanceClass) {
				PerformanceClass.CPU -> createCpuPerformanceManager()
				PerformanceClass.MEMORY -> createMemoryPerformanceManager()
				PerformanceClass.STORAGE -> createStoragePerformanceManager()
				PerformanceClass.NETWORK -> createNetworkPerformanceManager()
				PerformanceClass.BATTERY -> createBatteryPerformanceManager()
				else -> throw IllegalArgumentException("NO SUCH PERFORMANCE CLASS EXISTS: $performanceClass")
			}.apply { init() }
		}

	private fun createCpuPerformanceManager(): PerformanceManager =
		CpuPerformanceManager(applicationContext, thresholds?.cpu ?: CpuThresholds())

	private fun createMemoryPerformanceManager(): PerformanceManager =
		MemoryPerformanceManager(applicationContext, thresholds?.memory ?: MemoryThresholds())

	private fun createStoragePerformanceManager(): PerformanceManager =
		StoragePerformanceManager( thresholds?.storage ?: StorageThresholds())

	private fun createNetworkPerformanceManager(): PerformanceManager =
		NetworkPerformanceManager(applicationContext, thresholds?.network ?: NetworkThresholds())

	private fun createBatteryPerformanceManager(): PerformanceManager =
		BatteryPerformanceManager(applicationContext, thresholds?.battery ?: BatteryThresholds())
}
