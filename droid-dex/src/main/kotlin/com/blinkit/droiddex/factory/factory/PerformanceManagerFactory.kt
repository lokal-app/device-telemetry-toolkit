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
import com.blinkit.droiddex.models.DetailedPerformanceDataResult
import com.blinkit.droiddex.models.PerformanceThresholds
import com.blinkit.droiddex.models.RawPerformanceDataResult
import com.blinkit.droiddex.models.WeightedPerformanceLevels
import com.blinkit.droiddex.cpu.models.CpuDetailedMetrics
import com.blinkit.droiddex.memory.models.MemoryDetailedMetrics
import com.blinkit.droiddex.network.models.NetworkDetailedMetrics
import com.blinkit.droiddex.storage.models.StorageDetailedMetrics
import com.blinkit.droiddex.battery.models.BatteryDetailedMetrics
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
	private val androidId: String = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID) ?: ""

	private data class CollectionConfig(val classes: Set<Int>, val delaySeconds: Int)

	private class CollectionState<T> {
		@Volatile var config: CollectionConfig? = null
		var liveData: MutableLiveData<T> = MutableLiveData()
		var job: Job? = null
	}

	private val rawCollectionState = CollectionState<RawPerformanceDataResult>()
	private val detailedCollectionState = CollectionState<DetailedPerformanceDataResult>()

	init {
		PerformanceClass.values().forEach { getOrPut(it) }
	}

	fun getPerformanceLevel(@PerformanceClass performanceClass: Int): PerformanceLevel =
		getOrPut(performanceClass).performanceLevel

	fun getPerformanceLevelLd(@PerformanceClass performanceClass: Int): LiveData<PerformanceLevel> =
		getOrPut(performanceClass).performanceLevelLd

	fun getDetailedMetricsLd(@PerformanceClass performanceClass: Int): LiveData<DetailedMetrics> =
		getOrPut(performanceClass).detailedMetricsLd

	private fun <T> startCollection(
		state: CollectionState<T>,
		classes: Set<Int>,
		delaySeconds: Int,
		collector: (CollectionConfig) -> T,
		label: String
	): LiveData<T> {
		require(delaySeconds > 0) { "delaySeconds must be positive, was $delaySeconds" }
		if (state.config != null) {
			logger.logInfo("$label data collection is already active. Stop it first to restart with new parameters.")
			return state.liveData
		}

		val config = CollectionConfig(classes, delaySeconds)
		state.liveData = MutableLiveData()
		state.config = config

		startPeriodicCollection(state, collector)
		return state.liveData
	}

	private fun <T> startPeriodicCollection(
		state: CollectionState<T>,
		collector: (CollectionConfig) -> T
	) {
		state.job?.cancel()

		val delaySeconds = state.config?.delaySeconds ?: return

		state.job = runAsyncPeriodically({
			val currentConfig = state.config
			if (currentConfig != null) {
				try {
					val result = collector(currentConfig)
					state.liveData.postValue(result)
				} catch (e: Exception) {
					logger.logError(e)
				}
			}
		}, delaySeconds = delaySeconds.toFloat())
	}

	private fun <T> updateCollection(
		state: CollectionState<T>,
		delaySeconds: Int,
		collector: (CollectionConfig) -> T,
		label: String
	): Boolean {
		require(delaySeconds > 0) { "delaySeconds must be positive, was $delaySeconds" }
		if (state.config == null) {
			logger.logInfo("No active $label data collection to update. Start it first.")
			return false
		}

		state.config = state.config?.copy(delaySeconds = delaySeconds)
		state.config?.let {
			startPeriodicCollection(state, collector)
		}
		return true
	}

	private fun <T> stopCollection(state: CollectionState<T>) {
		state.job?.cancel()
		state.job = null
		state.config = null
		state.liveData = MutableLiveData()
	}

	/**
	 * Starts continuous collection of raw performance data for specified performance classes.
	 * @param classes performance classes to monitor
	 * @param delaySeconds interval in seconds between data collection cycles
	 * @return LiveData that emits [RawPerformanceDataResult] on each collection cycle
	 */
	@Synchronized
	fun startRawPerformanceDataCollection(
		vararg classes: Int,
		delaySeconds: Int
	): LiveData<RawPerformanceDataResult> =
		startCollection(rawCollectionState, classes.toSet(), delaySeconds, ::collectRawPerformanceData, "Raw performance")

	/**
	 * Updates the interval on the active raw performance data collection.
	 * Preserves the original set of performance classes.
	 * @param delaySeconds new interval in seconds between data collection cycles
	 * @return true if updated, false if no active collection exists
	 */
	@Synchronized
	fun updateRawPerformanceDataCollection(
		delaySeconds: Int
	): Boolean =
		updateCollection(rawCollectionState, delaySeconds, ::collectRawPerformanceData, "Raw performance")

	@Synchronized
	fun stopRawPerformanceDataCollection() {
		stopCollection(rawCollectionState)
	}

	/**
	 * Starts continuous collection of detailed performance data for specified performance classes.
	 * Independent from raw data collection — both can run simultaneously.
	 * @param classes performance classes to monitor
	 * @param delaySeconds interval in seconds between data collection cycles
	 * @return LiveData that emits [DetailedPerformanceDataResult] on each collection cycle
	 */
	@Synchronized
	fun startDetailedPerformanceDataCollection(
		vararg classes: Int,
		delaySeconds: Int
	): LiveData<DetailedPerformanceDataResult> =
		startCollection(detailedCollectionState, classes.toSet(), delaySeconds, ::collectDetailedPerformanceData, "Detailed performance")

	/**
	 * Updates the interval on the active detailed performance data collection.
	 * Preserves the original set of performance classes.
	 * @param delaySeconds new interval in seconds between data collection cycles
	 * @return true if updated, false if no active collection exists
	 */
	@Synchronized
	fun updateDetailedPerformanceDataCollection(
		delaySeconds: Int
	): Boolean =
		updateCollection(detailedCollectionState, delaySeconds, ::collectDetailedPerformanceData, "detailed performance")

	@Synchronized
	fun stopDetailedPerformanceDataCollection() {
		stopCollection(detailedCollectionState)
	}

	private fun collectRawPerformanceData(config: CollectionConfig): RawPerformanceDataResult {
		val executionStartTime = System.currentTimeMillis()
		val executionStartNanos = System.nanoTime()

		val classes = config.classes
		val cpuData = if (PerformanceClass.CPU in classes) getOrPut(PerformanceClass.CPU).extractRawPerformanceMetrics() as? CpuRawPerformanceMetrics else null
		val memoryData = if (PerformanceClass.MEMORY in classes) getOrPut(PerformanceClass.MEMORY).extractRawPerformanceMetrics() as? MemoryRawPerformanceMetrics else null
		val networkData = if (PerformanceClass.NETWORK in classes) getOrPut(PerformanceClass.NETWORK).extractRawPerformanceMetrics() as? NetworkRawPerformanceMetrics else null
		val storageData = if (PerformanceClass.STORAGE in classes) getOrPut(PerformanceClass.STORAGE).extractRawPerformanceMetrics() as? StorageRawPerformanceMetrics else null
		val batteryData = if (PerformanceClass.BATTERY in classes) getOrPut(PerformanceClass.BATTERY).extractRawPerformanceMetrics() as? BatteryRawPerformanceMetrics else null

		val executionEndTime = System.currentTimeMillis()
		val executionDurationMs = (System.nanoTime() - executionStartNanos) / 1_000_000

		return RawPerformanceDataResult(
			timestamp = executionEndTime,
			deviceName = Build.MODEL,
			androidId = androidId,
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

	private fun collectDetailedPerformanceData(config: CollectionConfig): DetailedPerformanceDataResult {
		val executionStartTime = System.currentTimeMillis()
		val executionStartNanos = System.nanoTime()

		val classes = config.classes
		val cpuData = if (PerformanceClass.CPU in classes) getOrPut(PerformanceClass.CPU).measureDetailedMetrics() as? CpuDetailedMetrics else null
		val memoryData = if (PerformanceClass.MEMORY in classes) getOrPut(PerformanceClass.MEMORY).measureDetailedMetrics() as? MemoryDetailedMetrics else null
		val networkData = if (PerformanceClass.NETWORK in classes) getOrPut(PerformanceClass.NETWORK).measureDetailedMetrics() as? NetworkDetailedMetrics else null
		val storageData = if (PerformanceClass.STORAGE in classes) getOrPut(PerformanceClass.STORAGE).measureDetailedMetrics() as? StorageDetailedMetrics else null
		val batteryData = if (PerformanceClass.BATTERY in classes) getOrPut(PerformanceClass.BATTERY).measureDetailedMetrics() as? BatteryDetailedMetrics else null

		val executionEndTime = System.currentTimeMillis()
		val executionDurationMs = (System.nanoTime() - executionStartNanos) / 1_000_000

		return DetailedPerformanceDataResult(
			timestamp = executionEndTime,
			deviceName = Build.MODEL,
			androidId = androidId,
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

	/**
	 * Calculates weighted performance levels for specified performance classes.
	 * @param classes pairs of (performance class, weight)
	 * @return [WeightedPerformanceLevels] with individual levels and weighted overall level
	 */
	fun getWeightedPerformanceLevels(
		vararg classes: Pair<Int, Float>
	): WeightedPerformanceLevels {
		val individualPerformanceLevels = mutableMapOf<Int, PerformanceLevel>()
		val weightedPairs = mutableListOf<Pair<PerformanceLevel, Float>>()

		classes.forEach { (performanceClass, weight) ->
			val manager = getOrPut(performanceClass)
			manager.refreshPerformanceLevel()
			val level = manager.performanceLevel
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
		stopDetailedPerformanceDataCollection()
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
