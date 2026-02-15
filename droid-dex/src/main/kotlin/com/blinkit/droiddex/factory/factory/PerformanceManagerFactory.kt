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
import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Job
import kotlin.concurrent.Volatile
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("HardwareIds")
internal class PerformanceManagerFactory(
    private val applicationContext: Context,
    private val thresholds: PerformanceThresholds? = null
) {

	private val performanceManagerMap = ConcurrentHashMap<Int, PerformanceManager>()
	private val logger = Logger()
	private val deviceId: String = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID) ?: ""

	// Atomic collection config — replaces separate volatile fields for thread safety
	private data class CollectionConfig(val classes: Set<Int>, val delaySeconds: Int)

	@Volatile
	private var collectionConfig: CollectionConfig? = null

	@Volatile
	private var rawPerformanceDataLiveData = MutableLiveData<RawPerformanceDataResult>()

	@Volatile
	private var periodicCollectionJob: Job? = null

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
	 * @param delaySeconds interval in seconds between data collection cycles
	 * @return LiveData of RawPerformanceDataResult containing raw metrics from all requested classes
	 *
	 * Implementation details:
	 * - Synchronized to prevent TOCTOU race on concurrent start/stop calls
	 * - Guards against double-start (returns existing LiveData if already active)
	 * - Creates fresh LiveData to avoid sticky stale values from previous sessions
	 * - Uses atomic CollectionConfig to prevent partial state visibility on IO threads
	 * - Continues until stopRawPerformanceDataCollection() is called
	 */
	@Synchronized
	fun startRawPerformanceDataCollection(
		vararg classes: Int,
		delaySeconds: Int
	): LiveData<RawPerformanceDataResult> {
		require(delaySeconds > 0) { "delaySeconds must be positive, was $delaySeconds" }
		if (collectionConfig != null) {
			logger.logInfo("Raw performance data collection is already active. Call stopRawPerformanceDataCollection() first to restart with new parameters.")
			return rawPerformanceDataLiveData
		}

		val config = CollectionConfig(classes.toSet(), delaySeconds)

		// Fresh LiveData to avoid sticky stale values from previous sessions
		rawPerformanceDataLiveData = MutableLiveData()

		// Set config AFTER setup, BEFORE starting collection — atomic visibility for IO threads
		collectionConfig = config

		startPeriodicRawDataCollection(config)
		return rawPerformanceDataLiveData
	}

	private fun startPeriodicRawDataCollection(config: CollectionConfig) {
		periodicCollectionJob?.cancel()

		periodicCollectionJob = runAsyncPeriodically({
			val currentConfig = collectionConfig
			if (currentConfig != null) {
				try {
					val rawData = collectRawPerformanceData(currentConfig)
					rawPerformanceDataLiveData.postValue(rawData)
				} catch (e: Exception) {
					logger.logError(e)
				}
			}
		}, delaySeconds = config.delaySeconds.toFloat())
	}

	private fun collectRawPerformanceData(config: CollectionConfig): RawPerformanceDataResult {
		val executionStartTime = System.currentTimeMillis()
		val executionStartNanos = System.nanoTime()

		// Perform all data collection using the atomic config snapshot
		val cpuData = if (PerformanceClass.CPU in config.classes) extractCpuRawMetrics() else null
		val memoryData = if (PerformanceClass.MEMORY in config.classes) extractMemoryRawMetrics() else null
		val networkData = if (PerformanceClass.NETWORK in config.classes) extractNetworkRawMetrics() else null
		val storageData = if (PerformanceClass.STORAGE in config.classes) extractStorageRawMetrics() else null
		val batteryData = if (PerformanceClass.BATTERY in config.classes) extractBatteryRawMetrics() else null

		val executionEndTime = System.currentTimeMillis()
		val executionDurationMs = (System.nanoTime() - executionStartNanos) / 1_000_000

		return RawPerformanceDataResult(
			timestamp = executionEndTime,
			deviceName = Build.MODEL,
			deviceId = deviceId,
			cpu = cpuData,
			memory = memoryData,
			network = networkData,
			storage = storageData,
			battery = batteryData,
			nativeExecutionStartMs = executionStartTime,
			nativeExecutionEndMs = executionEndTime,
			nativeExecutionDurationMs = executionDurationMs
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

	@Synchronized
	fun stopRawPerformanceDataCollection() {
		// Cancel job FIRST to prevent racing with the collection coroutine
		periodicCollectionJob?.cancel()
		periodicCollectionJob = null

		collectionConfig = null

		// Clear LiveData to signal completion — prevents observers from holding stale values
		rawPerformanceDataLiveData = MutableLiveData()
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

	fun shutdown() {
		stopRawPerformanceDataCollection()
		performanceManagerMap.values.forEach { it.destroy() }
		performanceManagerMap.clear()
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
