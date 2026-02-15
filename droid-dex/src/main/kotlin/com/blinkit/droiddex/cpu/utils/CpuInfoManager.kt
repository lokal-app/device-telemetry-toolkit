package com.blinkit.droiddex.cpu.utils

import com.blinkit.droiddex.utils.Logger
import com.blinkit.droiddex.utils.average
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Integer.max
import java.util.regex.Pattern

internal class CpuInfoManager(private val logger: Logger) {

	private val coresFreqList = mutableListOf<CoreFreq>()

	// Previous sample for delta-based CPU usage calculation
	private var prevCpuTimeMs = 0L
	private var prevWallTimeMs = 0L

	val noOfCores: Int by lazy {
		max(
			runCatching {
				val files = File(CPU_INFO_PATH).listFiles { pathname -> Pattern.matches("cpu[0-9]+", pathname.name) }
				max(1, files?.size ?: 1)
			}.getOrDefault(1), Runtime.getRuntime().availableProcessors()
		)
	}

	val currentCpuUsage: Int
		get() = measureProcessCpuUsage().also { logger.logDebug("CURRENT USAGE: ${it}%") }

	private fun measureProcessCpuUsage(): Int {
		val currentCpuTimeMs = android.os.Process.getElapsedCpuTime()
		val currentWallTimeMs = android.os.SystemClock.elapsedRealtime()

		val usage = if (prevWallTimeMs > 0) {
			val cpuTimeDiff = currentCpuTimeMs - prevCpuTimeMs
			val wallTimeDiff = currentWallTimeMs - prevWallTimeMs
			if (wallTimeDiff > 0) {
				(cpuTimeDiff * 100 / (wallTimeDiff * noOfCores)).toInt().coerceIn(0, 100)
			} else 0
		} else {
			0 // First sample, no previous data to compare
		}

		prevCpuTimeMs = currentCpuTimeMs
		prevWallTimeMs = currentWallTimeMs
		return usage
	}

	val maxCpuFreqInMHz: Int
		get() = ((coresFreqList.map { it.max }.average()?.toLong()?.takeIf { it > 0 }?.div(1000)?.toInt())
			?: Int.MAX_VALUE).also { logger.logDebug("MAX CPU FREQUENCY: $it MHz") }

	val minCpuFreqInMHz: Int
		get() = ((coresFreqList.map { it.min }.average()?.toLong()?.takeIf { it > 0 }?.div(1000)?.toInt())
			?: Int.MAX_VALUE).also { logger.logDebug("MIN CPU FREQUENCY: $it MHz") }

	val currentCpuFreqInMHz: Int
		get() = ((coresFreqList.map { it.currentFreq }.average()?.toLong()?.takeIf { it > 0 }?.div(1000)?.toInt())
			?: 0).also { logger.logDebug("CURRENT CPU FREQUENCY: $it MHz") }

	init {
		for (i in 0 until noOfCores) coresFreqList.add(CoreFreq(i))
	}

	private class CoreFreq(private val index: Int) {

		var min = 0L
			get() = field.takeIf { it > 0L } ?: getMinFreq().also { field = it }
			private set

		var max = 0L
			get() = field.takeIf { it > 0L } ?: getMaxFreq().also { field = it }
			private set

		init {
			min = getMinFreq()
			max = getMaxFreq()
		}

		val currentFreq: Long
			get() = getCurFreq()

		private fun getCurFreq() = readFile("${CPU_INFO_PATH}cpu$index/cpufreq/scaling_cur_freq")

		private fun getMinFreq() = readFile("${CPU_INFO_PATH}cpu$index/cpufreq/cpuinfo_min_freq")

		private fun getMaxFreq() = readFile("${CPU_INFO_PATH}cpu$index/cpufreq/cpuinfo_max_freq")

		private fun readFile(path: String): Long = try {
			BufferedReader(FileReader(path)).use { reader -> reader.readLine()?.toLongOrNull() }
		} catch (_: Exception) {
			null
		} ?: 0
	}

	companion object {

		private const val CPU_INFO_PATH = "/sys/devices/system/cpu/"
	}
}
