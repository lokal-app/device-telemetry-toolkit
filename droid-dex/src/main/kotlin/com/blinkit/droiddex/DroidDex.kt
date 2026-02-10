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

public object DroidDex {

	private lateinit var performanceManagerFactory: PerformanceManagerFactory

	private val logger: Logger by lazy { Logger() }

	public fun init(applicationContext: Context, thresholds: PerformanceThresholds? = null) {
		if (::performanceManagerFactory.isInitialized) {
			logger.logError(IllegalStateException("Droid Dex is already initialized"))
			return
		}

		try {
			performanceManagerFactory = PerformanceManagerFactory(applicationContext, thresholds)
		} catch (e: Exception) {
			logger.logError(e)
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
	public fun getWeightedPerformanceLevel(vararg classes: Pair<@PerformanceClass Int, Float>): PerformanceLevel =
		checkInitialized(*classes.map { it.first }.toIntArray()) ?: getPerformanceLevelWithWeights(classes.map {
			Pair(performanceManagerFactory.getPerformanceLevel(it.first), it.second)
		}).also { logger.logPerformanceLevelResult(*classes, performanceLevel = it) }

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
	public fun getWeightedPerformanceLevelLd(vararg classes: Pair<@PerformanceClass Int, Float>): LiveData<PerformanceLevel> =
		checkInitialized(*classes.map { it.first }.toIntArray())?.let { MutableLiveData(it) }
			?: getPerformanceLevelLdWithWeights(classes.map {
				Pair(performanceManagerFactory.getPerformanceLevelLd(it.first), it.second)
			}) { logger.logPerformanceLevelResult(*classes, performanceLevel = it) }

	/**
	 * @param performanceClass the performance class to get detailed metrics for
	 * @return LiveData of DetailedMetrics for the specified performance class
	 */
	public fun getDetailedMetricsLd(@PerformanceClass performanceClass: Int): LiveData<DetailedMetrics>? =
		if (::performanceManagerFactory.isInitialized) {
			performanceManagerFactory.getDetailedMetricsLd(performanceClass)
		} else {
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameter: ${performanceClass.name()}"))
			null
		}

	/**
	 * Starts continuous collection of raw performance data for specified performance classes
	 * @param classes spread list of performance classes to monitor
	 * @param delay interval in seconds between data collection cycles
	 * @return LiveData of RawPerformanceDataResult containing raw metrics, or null if not initialized
	 *
	 * Steps for raw data collection:
	 * - Check if the library has been initialized
	 * - Start periodic data collection for specified performance classes
	 * - Return LiveData that emits RawPerformanceDataResult at specified intervals
	 * - Continue until stopRawPerformanceDataCollection() is called
	 */
	public fun startRawPerformanceDataCollection(
		vararg classes: Int,
		delay: Int
	): LiveData<RawPerformanceDataResult>? =
		if (::performanceManagerFactory.isInitialized) {
			performanceManagerFactory.startRawPerformanceDataCollection(*classes, delay = delay)
		} else {
			val classesNames = classes.joinToString(", ") { it.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			null
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
		if (::performanceManagerFactory.isInitialized) {
			performanceManagerFactory.stopRawPerformanceDataCollection()
		} else {
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized"))
		}
	}

	/**
	 * @param classes spread list of performance classes mapped with weight
	 * @return WeightedPerformanceLevels containing individual and overall performance levels, or null if not initialized
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
	): WeightedPerformanceLevels? =
		if (::performanceManagerFactory.isInitialized) {
			performanceManagerFactory.getWeightedPerformanceLevels(*classes)
		} else {
			val classesNames = classes.joinToString(", ") { it.first.name() }
			logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
			null
		}

	private fun checkInitialized(vararg classes: Int): PerformanceLevel? {
		if (::performanceManagerFactory.isInitialized) return null

		val classesNames = classes.joinToString(", ") { it.name() }
		logger.logError(UninitializedPropertyAccessException("Droid Dex is not initialized for parameters: $classesNames"))
		return PerformanceLevel.UNKNOWN
	}
}
