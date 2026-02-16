package com.blinkit.droiddex

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.constants.PerformanceClass.Companion.name
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.factory.factory.PerformanceManagerFactory
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.models.PerformanceThresholds
import com.blinkit.droiddex.models.RawPerformanceDataResult
import com.blinkit.droiddex.models.WeightedPerformanceLevels
import com.blinkit.droiddex.utils.Logger
import com.blinkit.droiddex.utils.getPerformanceLevelLdWithWeights
import com.blinkit.droiddex.utils.getPerformanceLevelWithWeights
import kotlin.concurrent.Volatile

public object DroidDex {

	@Volatile
	private var performanceManagerFactory: PerformanceManagerFactory? = null

	private val logger: Logger by lazy { Logger() }

	public fun init(applicationContext: Context, thresholds: PerformanceThresholds? = null) {
		synchronized(this) {
			if (performanceManagerFactory != null) {
				logger.logError(IllegalStateException("Droid Dex is already initialized"))
				return
			}

			try {
				performanceManagerFactory = PerformanceManagerFactory(applicationContext, thresholds)
			} catch (e: Exception) {
				logger.logError(e)
			}
		}
	}

	/**
	 * Shuts down DroidDex, cancelling all monitoring coroutines and releasing resources.
	 * After calling this, init() must be called again before using any other API.
	 */
	public fun shutdown() {
		synchronized(this) {
			performanceManagerFactory?.shutdown()
			performanceManagerFactory = null
		}
	}

	/**
	 * @param classes spread list of performance classes
	 * @return Average PerformanceLevel for the input classes
	 *
	 * Steps for getting PerformanceLevel:
	 * - Check if the class has been initialized or not
	 * - Get the PerformanceLevel of individual PerformanceClass
	 * - Ignore the ones for which the PerformanceLevel is UNKNOWN
	 * - Take average of all these PerformanceClass
	 */
	public fun getPerformanceLevel(@PerformanceClass vararg classes: Int): PerformanceLevel =
		getWeightedPerformanceLevel(*classes.map { Pair(it, 1F) }.toTypedArray())

	/**
	 * @param classes spread list of performance classes
	 * @return LiveData of Average PerformanceLevel for the input classes
	 *
	 * Steps for getting PerformanceLevel:
	 * - Check if the class has been initialized or not
	 * - Get the PerformanceLevel of individual PerformanceClass
	 * - Ignore the ones for which the PerformanceLevel is UNKNOWN
	 * - Take average of all these PerformanceClass
	 */
	public fun getPerformanceLevelLd(@PerformanceClass vararg classes: Int): LiveData<PerformanceLevel> =
		getWeightedPerformanceLevelLd(*classes.map { Pair(it, 1F) }.toTypedArray())

	/**
	 * @param classes spread list of performance classes mapped with weight
	 * @return Weighted Average PerformanceLevel for the input classes
	 *
	 * Steps for getting PerformanceLevel:
	 * - Check if the class has been initialized or not
	 * - Get the PerformanceLevel of individual PerformanceClass
	 * - Ignore the ones for which the PerformanceLevel is UNKNOWN
	 * - Take weighted average of all these PerformanceClass
	 */
	public fun getWeightedPerformanceLevel(vararg classes: Pair<@PerformanceClass Int, Float>): PerformanceLevel {
		val factory = performanceManagerFactory
		if (factory == null) {
			val classesNames = classes.joinToString(", ") { it.first.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			return PerformanceLevel.UNKNOWN
		}
		return getPerformanceLevelWithWeights(classes.map {
			Pair(factory.getPerformanceLevel(it.first), it.second)
		}).also { logger.logPerformanceLevelResult(*classes, performanceLevel = it) }
	}

	/**
	 * @param classes spread list of performance classes mapped with weight
	 * @return LiveData of Weighted Average PerformanceLevel for the input classes
	 *
	 * Steps for getting PerformanceLevel:
	 * - Check if the class has been initialized or not
	 * - Get the PerformanceLevel of individual PerformanceClass
	 * - Ignore the ones for which the PerformanceLevel is UNKNOWN
	 * - Take weighted average of all these PerformanceClass
	 */
	public fun getWeightedPerformanceLevelLd(vararg classes: Pair<@PerformanceClass Int, Float>): LiveData<PerformanceLevel> {
		val factory = performanceManagerFactory
		if (factory == null) {
			val classesNames = classes.joinToString(", ") { it.first.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			return MutableLiveData(PerformanceLevel.UNKNOWN)
		}
		return getPerformanceLevelLdWithWeights(classes.map {
			Pair(factory.getPerformanceLevelLd(it.first), it.second)
		}) { logger.logPerformanceLevelResult(*classes, performanceLevel = it) }
	}

	/**
	 * @param performanceClass the performance class to get detailed metrics for
	 * @return LiveData of DetailedMetrics for the specified performance class, or empty LiveData if not initialized
	 */
	public fun getDetailedMetricsLd(@PerformanceClass performanceClass: Int): LiveData<DetailedMetrics> {
		val factory = performanceManagerFactory
		if (factory == null) {
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameter: ${performanceClass.name()}"))
			return MutableLiveData()
		}
		return factory.getDetailedMetricsLd(performanceClass)
	}

	/**
	 * Starts continuous collection of raw performance data for specified performance classes
	 * @param classes spread list of performance classes to monitor
	 * @param delaySeconds interval in seconds between data collection cycles
	 * @return LiveData of RawPerformanceDataResult containing raw metrics, or empty LiveData if not initialized
	 *
	 * Steps for raw data collection:
	 * - Check if the library has been initialized
	 * - Start periodic data collection for specified performance classes
	 * - Return LiveData that emits RawPerformanceDataResult at specified intervals
	 * - Continue until stopRawPerformanceDataCollection() is called
	 */
	public fun startRawPerformanceDataCollection(
		vararg classes: Int,
		delaySeconds: Int
	): LiveData<RawPerformanceDataResult> {
		val factory = performanceManagerFactory
		if (factory == null) {
			val classesNames = classes.joinToString(", ") { it.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			return MutableLiveData()
		}
		return factory.startRawPerformanceDataCollection(*classes, delaySeconds = delaySeconds)
	}

	/**
	 * Updates the active raw performance data collection with new classes and/or delay
	 * @param classes spread list of performance classes to monitor
	 * @param delaySeconds new interval in seconds between data collection cycles
	 * @return true if collection was updated, false if not initialized or no active collection
	 *
	 * Steps for updating collection:
	 * - Check if the library has been initialized
	 * - Cancel the current periodic job
	 * - Start a new periodic job with updated parameters (fires immediately)
	 * - Existing LiveData observers continue receiving data seamlessly
	 */
	public fun updateRawPerformanceDataCollection(
		vararg classes: Int,
		delaySeconds: Int
	): Boolean {
		val factory = performanceManagerFactory
		if (factory == null) {
			val classesNames = classes.joinToString(", ") { it.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			return false
		}
		return factory.updateRawPerformanceDataCollection(*classes, delaySeconds = delaySeconds)
	}

	/**
	 * Stops the ongoing raw performance data collection
	 *
	 * Steps for stopping collection:
	 * - Check if the library has been initialized
	 * - Stop the periodic data collection process
	 * - Clean up resources associated with data collection
	 */
	public fun stopRawPerformanceDataCollection() {
		val factory = performanceManagerFactory
		if (factory == null) {
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized"))
			return
		}
		factory.stopRawPerformanceDataCollection()
	}

	/**
	 * @param classes spread list of performance classes mapped with weight
	 * @return WeightedPerformanceLevels containing individual and overall performance levels
	 *
	 * Steps for getting weighted performance levels:
	 * - Check if the library has been initialized
	 * - Get the PerformanceLevel of individual PerformanceClass
	 * - Ignore the ones for which the PerformanceLevel is UNKNOWN
	 * - Calculate weighted average of all these PerformanceClass
	 * - Return structured result with both individual and overall levels
	 */
	public fun getWeightedPerformanceLevels(
		vararg classes: Pair<@PerformanceClass Int, Float>
	): WeightedPerformanceLevels {
		val factory = performanceManagerFactory
		if (factory == null) {
			val classesNames = classes.joinToString(", ") { it.first.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			return WeightedPerformanceLevels(
				overallPerformanceLevel = PerformanceLevel.UNKNOWN,
				cpu = null, memory = null, network = null, storage = null, battery = null
			)
		}
		return factory.getWeightedPerformanceLevels(*classes)
	}
}
