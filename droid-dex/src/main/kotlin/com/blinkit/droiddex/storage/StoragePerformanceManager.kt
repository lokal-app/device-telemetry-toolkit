package com.blinkit.droiddex.storage

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.factory.base.PerformanceManager
import com.blinkit.droiddex.factory.providers.PerformanceManagerProvider
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.storage.models.StorageDetailedMetrics
import com.blinkit.droiddex.storage.models.StorageRawPerformanceMetrics
import com.blinkit.droiddex.storage.models.StorageThresholds
import com.blinkit.droiddex.utils.convertBytesToGB
import kotlin.concurrent.Volatile

internal class StoragePerformanceManager(
    private val thresholds: StorageThresholds = StorageThresholds()
): PerformanceManager() {

    // Storage caching - data rarely changes during app session
    @Volatile
    private var cachedStorageData: StorageData? = null
    @Volatile
    private var lastMeasurementTime = 0L

    private data class StorageData(
        val totalStorageGB: Float,
        val availableStorageGB: Float
    )

	override fun getPerformanceClass() = PerformanceClass.STORAGE

	override fun measurePerformanceLevel(): PerformanceLevel {
		val storageData = getCachedOrFreshStorageData()
		val availableStorage = storageData.availableStorageGB

		return when {
			availableStorage >= thresholds.excellent.availableStorageGBThreshold -> PerformanceLevel.EXCELLENT
			availableStorage >= thresholds.high.availableStorageGBThreshold -> PerformanceLevel.HIGH
			availableStorage >= thresholds.average.availableStorageGBThreshold -> PerformanceLevel.AVERAGE
			availableStorage > 0 -> PerformanceLevel.LOW
			else -> PerformanceLevel.UNKNOWN
		}
	}

	private fun getCachedOrFreshStorageData(): StorageData {
		val currentTime = System.currentTimeMillis()
		val cacheValidFor = 30 * 60 * 1000L // 30 minutes cache

		// Return cached data if available and still valid
		cachedStorageData?.let { cached ->
			if (currentTime - lastMeasurementTime < cacheValidFor) {
				return cached
			}
		}

		val freshData = measureStorageData()
		cachedStorageData = freshData
		lastMeasurementTime = currentTime
		return freshData
	}

	@SuppressLint("UsableSpace")
	private fun measureStorageData(): StorageData {
		val externalDir = Environment.getExternalStorageDirectory()
		val totalBytes = try { externalDir.totalSpace } catch (e: SecurityException) { logError(e); 0L }
		val availableBytes = try { externalDir.usableSpace } catch (e: SecurityException) { logError(e); 0L }
		val totalGB = convertBytesToGB(totalBytes)
		val availableGB = convertBytesToGB(availableBytes)

		return StorageData(
			totalStorageGB = totalGB,
			availableStorageGB = availableGB
		)
	}

	override fun measureDetailedMetrics(): DetailedMetrics {
		val storageData = getCachedOrFreshStorageData()
		return StorageDetailedMetrics(
			performanceLevel = measurePerformanceLevel(),
			totalStorageGB = storageData.totalStorageGB,
			availableStorageGB = storageData.availableStorageGB
		)
	}

	override fun extractRawPerformanceMetrics(): StorageRawPerformanceMetrics {
		val storageData = getCachedOrFreshStorageData()
		return StorageRawPerformanceMetrics(
			totalStorageGB = storageData.totalStorageGB,
			availableStorageGB = storageData.availableStorageGB
		)
	}

	companion object: PerformanceManagerProvider {
		override fun create(applicationContext: Context): PerformanceManager =
			StoragePerformanceManager()
	}
}
