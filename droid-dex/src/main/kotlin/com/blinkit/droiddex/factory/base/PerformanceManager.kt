package com.blinkit.droiddex.factory.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.Logger
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.runAsyncPeriodically
import kotlin.concurrent.Volatile
import kotlinx.coroutines.Job

internal abstract class PerformanceManager {

	@Volatile
	var performanceLevel = PerformanceLevel.UNKNOWN
		private set

	private val _performanceLevelLd = MutableLiveData(PerformanceLevel.UNKNOWN)
	val performanceLevelLd: LiveData<PerformanceLevel>
		get() = _performanceLevelLd

	private val _detailedMetricsLd = MutableLiveData<DetailedMetrics>()
	val detailedMetricsLd: LiveData<DetailedMetrics>
		get() = _detailedMetricsLd

	protected val logger by lazy { Logger(getPerformanceClass()) }

	private var monitoringJob: Job? = null

	fun init() {
		monitoringJob = runAsyncPeriodically({
			refreshPerformanceLevel()
		}, delaySeconds = DEFAULT_DELAY_SECS)
	}

	@Synchronized
	internal fun refreshPerformanceLevel() {
		try {
			measurePerformanceLevel().also {
				val hasPerformanceLevelChanged = performanceLevel != it
				if (hasPerformanceLevelChanged) {
					performanceLevel = it
					_performanceLevelLd.postValue(it)
				}
				logger.logPerformanceLevelChange(it, hasPerformanceLevelChanged)
			}

			try {
				measureDetailedMetrics()?.let { detailedMetrics ->
					_detailedMetricsLd.postValue(detailedMetrics)
				}
			} catch (e: Exception) {
				logger.logError(e)
			}
		} catch (e: Exception) {
			logger.logError(e)
		}
	}

	internal fun destroy() {
		monitoringJob?.cancel()
		monitoringJob = null
	}

	@PerformanceClass
	protected abstract fun getPerformanceClass(): Int

	protected abstract fun measurePerformanceLevel(): PerformanceLevel

	protected abstract fun measureDetailedMetrics(): DetailedMetrics?

	internal abstract fun extractRawPerformanceMetrics(): RawPerformanceMetrics?

	protected fun logInfo(message: String) = logger.logInfo(message)

	protected fun logDebug(message: String) = logger.logDebug(message)

	protected fun logError(throwable: Throwable) = logger.logError(throwable)

	companion object {
		private const val DEFAULT_DELAY_SECS = 15F
	}
}
