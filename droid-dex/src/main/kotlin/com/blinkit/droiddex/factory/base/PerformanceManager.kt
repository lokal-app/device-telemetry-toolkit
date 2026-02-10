package com.blinkit.droiddex.factory.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.Logger
import com.blinkit.droiddex.utils.runAsyncPeriodically
import kotlin.concurrent.Volatile

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

	private var delay: Float = DEFAULT_DELAY_SECS

	internal fun setDelay(delay: Float) {
		this.delay = delay
	}

	fun init() {
		runAsyncPeriodically({
			try {
				measurePerformanceLevel().also {
					val hasPerformanceLevelChanged = performanceLevelLd.value != it
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
		}, delayInSecs = getDelayInSecs())
	}

	@PerformanceClass
	protected abstract fun getPerformanceClass(): Int

	fun getDelayInSecs(): Float = delay

	protected abstract fun measurePerformanceLevel(): PerformanceLevel

	protected abstract fun measureDetailedMetrics(): DetailedMetrics?

	internal abstract fun extractRawPerformanceMetrics(): Any?

	protected fun logInfo(message: String) = logger.logInfo(message)

	protected fun logDebug(message: String) = logger.logDebug(message)

	protected fun logError(throwable: Throwable) = logger.logError(throwable)

	companion object {
		private const val DEFAULT_DELAY_SECS = 15F
	}
}
