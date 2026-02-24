package com.blinkit.droiddex.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.factory.base.PerformanceManager
import com.blinkit.droiddex.factory.providers.PerformanceManagerProvider
import com.blinkit.droiddex.battery.models.BatteryDetailedMetrics
import com.blinkit.droiddex.battery.models.BatteryRawPerformanceMetrics
import com.blinkit.droiddex.battery.models.BatteryThresholds
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.getPerformanceLevelWithWeights

internal class BatteryPerformanceManager(
    private val applicationContext: Context,
    private val thresholds: BatteryThresholds = BatteryThresholds()
): PerformanceManager() {

	override fun getPerformanceClass() = PerformanceClass.BATTERY

	override fun measurePerformanceLevel(): PerformanceLevel {
		val batteryMetrics = getBatteryRawMetrics() ?: return PerformanceLevel.UNKNOWN

		val batteryPercentageLevel = getBatteryPercentageLevel(batteryMetrics.batteryPercentage, batteryMetrics.isCharging)
		val temperatureLevel = getTemperatureLevel(batteryMetrics.temperature)

		return getPerformanceLevelWithWeights(listOf(
			Pair(batteryPercentageLevel, 2F),
			Pair(temperatureLevel, 1F)
		))
	}

	fun measurePerformanceLevel(rawMetrics: BatteryRawPerformanceMetrics): PerformanceLevel {
		val batteryPercentageLevel = getBatteryPercentageLevel(rawMetrics.batteryPercentage, rawMetrics.isCharging)
		val temperatureLevel = getTemperatureLevel(rawMetrics.temperature)

		return getPerformanceLevelWithWeights(
			listOf(
				Pair(batteryPercentageLevel, 2F), Pair(temperatureLevel, 1F)
			)
		)
	}

	private fun getBatteryPercentageLevel(batteryPercentage: Float, isCharging: Boolean): PerformanceLevel {
		return when {
			batteryPercentage >= thresholds.excellent.batteryPercentageThreshold ||
			(isCharging && batteryPercentage >= thresholds.excellent.isChargingBatteryPercentageThreshold) -> PerformanceLevel.EXCELLENT

			batteryPercentage >= thresholds.high.batteryPercentageThreshold ||
			(isCharging && batteryPercentage >= thresholds.high.isChargingBatteryPercentageThreshold) -> PerformanceLevel.HIGH

			batteryPercentage >= thresholds.average.batteryPercentageThreshold ||
			(isCharging && batteryPercentage >= thresholds.average.isChargingBatteryPercentageThreshold) -> PerformanceLevel.AVERAGE

			else -> PerformanceLevel.LOW
		}
	}

	private fun getTemperatureLevel(temperature: Float): PerformanceLevel {
		return when {
			temperature < thresholds.excellent.temperatureThreshold -> PerformanceLevel.EXCELLENT
			temperature < thresholds.high.temperatureThreshold -> PerformanceLevel.HIGH
			temperature < thresholds.average.temperatureThreshold -> PerformanceLevel.AVERAGE
			else -> PerformanceLevel.LOW
		}
	}

	private fun getBatteryRawMetrics(): BatteryRawPerformanceMetrics? {
		val batteryStatusIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
			applicationContext.registerReceiver(null, it)
		} ?: return null

		val status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
		val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

		val batteryPercentage = batteryStatusIntent.let { intent ->
			val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
			val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
			if (level != -1 && scale != -1) level * 100f / scale else 0f
		}

		val statusString = when (status) {
			BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
			BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
			BatteryManager.BATTERY_STATUS_FULL -> "Full"
			BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
			else -> "Unknown"
		}

		val temperature = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
		val voltage = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f

		return BatteryRawPerformanceMetrics(batteryPercentage, isCharging, statusString, temperature, voltage)
	}


	override fun measureDetailedMetrics(): DetailedMetrics {
		val batteryMetrics = extractRawPerformanceMetrics()

		return BatteryDetailedMetrics(
			performanceLevel = measurePerformanceLevel(batteryMetrics),
			batteryPercentage = batteryMetrics.batteryPercentage,
			isCharging = batteryMetrics.isCharging,
			batteryStatus = batteryMetrics.batteryStatus,
			temperature = batteryMetrics.temperature,
			voltage = batteryMetrics.voltage
		)
	}

	override fun extractRawPerformanceMetrics(): BatteryRawPerformanceMetrics {
		return getBatteryRawMetrics() ?: BatteryRawPerformanceMetrics(
			batteryPercentage = 0f,
			isCharging = false,
			batteryStatus = "Unknown",
			temperature = 0f,
			voltage = 0f
		)
	}

	companion object: PerformanceManagerProvider {
		override fun create(applicationContext: Context): PerformanceManager =
			BatteryPerformanceManager(applicationContext)
	}
}
