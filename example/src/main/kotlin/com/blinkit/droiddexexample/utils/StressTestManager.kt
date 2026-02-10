package com.blinkit.droiddexexample.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlin.random.Random

/**
 * Manages stress testing functionality for DroidDex performance monitoring validation.
 * Contains CPU and memory stress tests to generate measurable performance impacts.
 */
class StressTestManager(private val context: Context) {

	private val memoryStressData = mutableListOf<ByteArray>()
	private val handler = Handler(Looper.getMainLooper())

	/**
	 * Generates heavy memory load to test DroidDex memory monitoring capabilities.
	 * 
	 * This function causes memory spikes by:
	 * 1. Creating large byte arrays (5MB each) filled with random data to prevent JVM optimization
	 * 2. Building complex nested data structures (Maps of Lists of Maps) to increase object count
	 * 3. Performing heavy string concatenation to pressure the string pool and heap
	 * 4. Allocating memory progressively over time (20 iterations, 500ms apart) to simulate real load
	 * 5. Keeping all allocated objects in memory simultaneously to maximize heap usage
	 * 
	 * Expected effects on memory metrics:
	 * - Heap Used: Will increase from ~50MB to 150MB+ 
	 * - Available RAM: Will decrease as heap grows
	 * - Memory Performance Level: Should change from EXCELLENT/HIGH to AVERAGE/LOW
	 * - GC Activity: Will trigger frequent garbage collection attempts
	 */
	fun generateMemoryLoad() {
		Toast.makeText(context, "Generating memory load...", Toast.LENGTH_SHORT).show()
		Log.d("MemoryStress", "Starting memory stress test")

		// Clear existing stress data
		memoryStressData.clear()

		// Generate heavy objects over time to gradually increase heap usage
		var iteration = 0
		val maxIterations = 20

		val stressRunnable = object : Runnable {
			override fun run() {
				if (iteration < maxIterations) {
					try {
						// Create large byte arrays (each ~5MB)
						val largeArray = ByteArray(5 * 1024 * 1024)
						// Fill with random data to prevent optimization
						Random.nextBytes(largeArray)
						memoryStressData.add(largeArray)

						// Create complex nested data structures
						val complexData = createComplexDataStructure()
						memoryStressData.add(complexData)

						// Create string concatenation pressure
						createStringPressure()

						iteration++
						val totalMemoryMB = memoryStressData.size * 5
						Log.d("MemoryStress", "Iteration $iteration/$maxIterations - Allocated ~${totalMemoryMB}MB")

						// Schedule next iteration
						handler.postDelayed(this, 500)
					} catch (e: OutOfMemoryError) {
						Log.w("MemoryStress", "OutOfMemoryError reached - test successful")
						Toast.makeText(context, "Memory limit reached!", Toast.LENGTH_LONG).show()
					}
				} else {
					Log.d("MemoryStress", "Memory stress test completed")
					Toast.makeText(context, "Memory stress test completed", Toast.LENGTH_SHORT).show()
					// Keep data in memory for a while, then gradually release
					handler.postDelayed({
						releaseMemoryGradually()
					}, 3000)
				}
			}
		}

		// Start the stress test
		stressRunnable.run()
	}

	/**
	 * Generates heavy CPU load to test DroidDex CPU monitoring capabilities.
	 * 
	 * This function causes CPU spikes by:
	 * 1. Running intensive mathematical calculations (prime number generation, matrix multiplication)
	 * 2. Performing complex string operations and regex processing
	 * 3. Creating nested loops with heavy computational work
	 * 4. Using multiple threads to max out all CPU cores
	 * 5. Running continuous calculations for sustained CPU load
	 * 
	 * Expected effects on CPU metrics:
	 * - CPU Usage: Will spike to 80-100% across all cores
	 * - CPU Frequency: Should increase to maximum available frequency  
	 * - CPU Performance Level: Should change from EXCELLENT/HIGH to AVERAGE/LOW
	 * - Core temperature: May increase due to sustained load
	 */
	fun generateCpuLoad() {
		Toast.makeText(context, "Generating CPU load...", Toast.LENGTH_SHORT).show()
		Log.d("CpuStress", "Starting CPU stress test")

		val numberOfCores = Runtime.getRuntime().availableProcessors()
		Log.d("CpuStress", "Using $numberOfCores CPU cores")

		// Run CPU intensive tasks on multiple threads
		repeat(numberOfCores) { coreIndex ->
			Thread {
				val startTime = System.currentTimeMillis()
				val durationMs = 15000 // Run for 15 seconds

				while (System.currentTimeMillis() - startTime < durationMs) {
					// Intensive mathematical calculations
					performPrimeCalculations()
					performMatrixMultiplication()
					performStringOperations()
					performRegexOperations()
				}
				Log.d("CpuStress", "Core $coreIndex finished stress test")
			}.start()
		}

		// Show completion message after stress test duration
		handler.postDelayed({
			Toast.makeText(context, "CPU stress test completed", Toast.LENGTH_SHORT).show()
			Log.d("CpuStress", "CPU stress test completed")
		}, 15000)
	}

	private fun createComplexDataStructure(): ByteArray {
		// Create nested maps and lists to increase object allocation
		val complexMap = mutableMapOf<String, MutableList<MutableMap<Int, String>>>()
		repeat(100) { i ->
			val innerList = mutableListOf<MutableMap<Int, String>>()
			repeat(50) { j ->
				val innerMap = mutableMapOf<Int, String>()
				repeat(20) { k ->
					innerMap[k] = "Complex data structure item $i-$j-$k with some additional text to increase memory usage"
				}
				innerList.add(innerMap)
			}
			complexMap["key_$i"] = innerList
		}

		// Convert to byte array to ensure it's retained
		return complexMap.toString().toByteArray()
	}

	private fun createStringPressure() {
		// Create large strings through concatenation to stress string pool
		var largeString = ""
		repeat(1000) { i ->
			largeString += "This is string number $i with some additional content to make it longer and consume more memory. "
		}
		// Convert to byte array and store
		memoryStressData.add(largeString.toByteArray())
	}

	private fun releaseMemoryGradually() {
		val releaseRunnable = object : Runnable {
			override fun run() {
				if (memoryStressData.isNotEmpty()) {
					// Remove 25% of data
					val removeCount = (memoryStressData.size * 0.25).toInt().coerceAtLeast(1)
					repeat(removeCount.coerceAtMost(memoryStressData.size)) {
						memoryStressData.removeAt(0)
					}
					Log.d("MemoryStress", "Released some memory, ${memoryStressData.size} chunks remaining")
					// Continue releasing
					handler.postDelayed(this, 1000)
				} else {
					Log.d("MemoryStress", "All stress memory released")
					Toast.makeText(context, "Memory released", Toast.LENGTH_SHORT).show()
					// Force garbage collection
					System.gc()
				}
			}
		}
		releaseRunnable.run()
	}

	private fun performPrimeCalculations() {
		// Calculate prime numbers up to 1000 (CPU intensive)
		var count = 0
		for (num in 2..1000) {
			var isPrime = true
			for (i in 2..kotlin.math.sqrt(num.toDouble()).toInt()) {
				if (num % i == 0) {
					isPrime = false
					break
				}
			}
			if (isPrime) count++
		}
	}

	private fun performMatrixMultiplication() {
		// Create and multiply 50x50 matrices
		val size = 50
		val matrix1 = Array(size) { Array(size) { Random.nextDouble() } }
		val matrix2 = Array(size) { Array(size) { Random.nextDouble() } }
		val result = Array(size) { Array(size) { 0.0 } }

		for (i in 0 until size) {
			for (j in 0 until size) {
				for (k in 0 until size) {
					result[i][j] += matrix1[i][k] * matrix2[k][j]
				}
			}
		}
	}

	private fun performStringOperations() {
		// Heavy string concatenation and manipulation
		var text = "Initial text"
		repeat(1000) { i ->
			text += " Added text number $i with some additional content"
			// Reverse and manipulate string
			text = text.reversed().take(text.length / 2) + text.drop(text.length / 2)
		}
	}

	private fun performRegexOperations() {
		// Complex regex operations
		val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
				"Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
				"Ut enim ad minim veniam, quis nostrud exercitation ullamco."
		
		val regexPatterns = listOf(
			"[a-zA-Z]+".toRegex(),
			"\\b\\w{4,}\\b".toRegex(),
			"[aeiou]+".toRegex(),
			"\\s+".toRegex()
		)

		repeat(500) {
			regexPatterns.forEach { regex ->
				regex.findAll(text).count()
				regex.replace(text) { it.value.uppercase() }
			}
		}
	}
}