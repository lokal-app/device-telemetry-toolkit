package com.blinkit.droiddexexample.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.blinkit.droiddex.DroidDex
import androidx.lifecycle.LiveData
import com.blinkit.droiddex.constants.PerformanceClass
import com.blinkit.droiddex.constants.PerformanceClass.Companion.name
import com.blinkit.droiddex.models.PerformanceThresholds
import com.blinkit.droiddex.models.RawPerformanceDataResult
import com.blinkit.droiddexexample.R
import com.blinkit.droiddexexample.databinding.ActivityMainBinding
import com.blinkit.droiddexexample.utils.dpToPx
import com.blinkit.droiddexexample.views.DetailedItemView
import com.blinkit.droiddexexample.views.ItemView
import com.blinkit.droiddexexample.utils.toExampleMetrics
import com.blinkit.droiddexexample.utils.StressTestManager
import android.os.Handler
import android.os.Looper
import timber.log.Timber

private const val LOGO_HEIGHT_DP = 64

class MainActivity: AppCompatActivity() {

	private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
	private lateinit var stressTestManager: StressTestManager
	private var rawDataObserver: Observer<RawPerformanceDataResult>? = null
	private var rawPerformanceDataLiveData: LiveData<RawPerformanceDataResult>? = null
	private var isRawDataCollectionActive = false

	// Timing tracking variables
	private var collectionStartTime: Long = 0L
	private var lastEventTime: Long = 0L
	private var delaySeconds = 15
	private var eventCount = 0

	// Call simulation state
	private val handler = Handler(Looper.getMainLooper())
	private var callResumeRunnable: Runnable? = null
	private var isCallSimulationActive = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		// Initialize DroidDex with custom thresholds
		initializeDroidDexWithCustomThresholds()

		stressTestManager = StressTestManager(this)

		binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
			binding.logo.elevation = (if (scrollY > 0.dpToPx()) 2 else 0).dpToPx()
		}

		binding.headingIndividual.setTextAppearance(R.style.TextAppearanceBold)
		binding.headingWeightedAggregate.setTextAppearance(R.style.TextAppearanceBold)

		setupDetailedClasses(binding.cpu, PerformanceClass.CPU)
		setupDetailedClasses(binding.memory, PerformanceClass.MEMORY)
		setupDetailedClasses(binding.network, PerformanceClass.NETWORK)
		setupDetailedClasses(binding.storage, PerformanceClass.STORAGE)
		setupDetailedClasses(binding.battery, PerformanceClass.BATTERY)

		setupWeightedClasses(
			binding.memoryAndNetworkWeighted, PerformanceClass.MEMORY to 2F, PerformanceClass.NETWORK to 1F
		)
		setupWeightedClasses(
			binding.cpuAndBatteryWeighted, PerformanceClass.CPU to 2F, PerformanceClass.BATTERY to 3F
		)
		setupWeightedClasses(
			binding.comboWeighted, PerformanceClass.NETWORK to 3F, PerformanceClass.MEMORY to 2F,
			PerformanceClass.CPU to 0.5F, PerformanceClass.BATTERY to 0.5F
		)

		configureEdgeToEdge()
		setupStressTestButtons()
	}

	private fun setupDetailedClasses(item: DetailedItemView, @PerformanceClass performanceClass: Int) {
		// Set up performance level observation
		DroidDex.getPerformanceLevelLd(performanceClass).observe(this) { level ->
			item.set(level, getClassName(performanceClass))
		}

		// Set up detailed metrics observation
		DroidDex.getDetailedMetricsLd(performanceClass).observe(this) { detailedMetrics ->
			val metrics = detailedMetrics.toExampleMetrics()
			// Update with detailed metrics
			item.set(metrics.performanceLevel, getClassName(performanceClass), metrics)
		}
	}

	private fun setupWeightedClasses(
		item: ItemView, vararg performanceClassesWithWeights: Pair<@PerformanceClass Int, Float>
	) {
		DroidDex.getWeightedPerformanceLevelLd(*performanceClassesWithWeights).observe(this) { level ->
			item.set(level, performanceClassesWithWeights.joinToString("\n") {
				"${getClassName(it.first)} × ${it.second}"
			})
		}
	}

	private fun getClassName(@PerformanceClass performanceClass: Int) = when (performanceClass) {
		PerformanceClass.CPU -> performanceClass.name()
		else -> performanceClass.name().lowercase().replaceFirstChar { it.uppercase() }
	}

	private fun configureEdgeToEdge() {
		enableEdgeToEdge(window)
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
			val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
			adjustLogoForStatusBar(insets.top)
			adjustScrollContentForNavigationBar(insets.bottom)
			windowInsets
		}
	}

	private fun adjustLogoForStatusBar(statusBarHeight: Int) {
		binding.logo.apply {
			updateLayoutParams { height = (LOGO_HEIGHT_DP.dpToPx() + statusBarHeight).toInt() }
			setPadding(paddingLeft, statusBarHeight, paddingRight, paddingBottom)
		}
	}

	private fun adjustScrollContentForNavigationBar(navigationBarHeight: Int) {
		binding.contentRoot.apply {
			setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + navigationBarHeight)
		}
	}

	private fun setupStressTestButtons() {
		binding.btnMemoryStress.setOnClickListener {
			stressTestManager.generateMemoryLoad()
		}

		binding.btnCpuStress.setOnClickListener {
			stressTestManager.generateCpuLoad()
		}

		binding.btnNewFlows.setOnClickListener {
			toggleRawDataCollection()
		}

		binding.btnSimulateCall.setOnClickListener {
			simulateCallStart()
		}

		updateNewFlowsButtonText()
	}

	private fun toggleRawDataCollection() {
		if (isRawDataCollectionActive) {
			stopRawDataCollection()
		} else {
			startRawDataCollection()
		}
	}

	private fun updateNewFlowsButtonText() {
		binding.btnNewFlows.text = if (isRawDataCollectionActive) {
			"Stop Raw Data Collection"
		} else {
			"Start Raw Data Collection (10s intervals) - TIMING TEST"
		}
		// Simulate Call button is only enabled when raw collection is active and no call is in progress
		binding.btnSimulateCall.isEnabled = isRawDataCollectionActive && !isCallSimulationActive
	}

	private fun initializeDroidDexWithCustomThresholds() {
		// Initialize with custom thresholds to demonstrate Flow 3
		val customThresholds = PerformanceThresholds()
		DroidDex.init(this, customThresholds)
		Timber.d("DroidDex initialized with custom thresholds")
	}

	private fun startRawDataCollection() {
		collectionStartTime = System.currentTimeMillis()
		lastEventTime = 0L
		eventCount = 0
		delaySeconds = 10
		rawPerformanceDataLiveData = DroidDex.startRawPerformanceDataCollection(
			PerformanceClass.CPU,
			PerformanceClass.MEMORY,
			PerformanceClass.NETWORK,
			PerformanceClass.STORAGE,
			PerformanceClass.BATTERY,
			delaySeconds = delaySeconds
		)

		rawDataObserver = Observer { rawData ->
			rawData.let { displayRawData(it) }
		}

		rawPerformanceDataLiveData?.observe(this, rawDataObserver!!)
		isRawDataCollectionActive = true
		updateNewFlowsButtonText()

		Timber.d("Raw data collection started")
	}

	private fun stopRawDataCollection() {
		// Cancel any pending call simulation
		cancelCallSimulation()

		DroidDex.stopRawPerformanceDataCollection()

		rawDataObserver?.let { observer ->
			rawPerformanceDataLiveData?.removeObserver(observer)
		}
		rawDataObserver = null
		rawPerformanceDataLiveData = null
		isRawDataCollectionActive = false
		updateNewFlowsButtonText()

		Timber.d("Raw data collection stopped")
	}

	private fun simulateCallStart() {
		if (!isRawDataCollectionActive || isCallSimulationActive) return

		isCallSimulationActive = true
		val callStartTime = System.currentTimeMillis()
		val intensiveDelaySeconds = 3
		val intensiveDurationMs = 20_000L
		val globalDelaySeconds = 10

		// Phase 1: Switch to intensive 3s collection with all classes
		val updated = DroidDex.updateRawPerformanceDataCollection(
			delaySeconds = intensiveDelaySeconds
		)

		if (!updated) {
			Timber.e("Failed to update collection for call simulation")
			isCallSimulationActive = false
			return
		}

		delaySeconds = intensiveDelaySeconds
		binding.btnSimulateCall.isEnabled = false
		binding.btnSimulateCall.text = "Call Active — Intensive (${intensiveDelaySeconds}s) for ${intensiveDurationMs / 1000}s..."
		Timber.d("CALL SIMULATION: Phase 1 started — ${intensiveDelaySeconds}s intervals for ${intensiveDurationMs / 1000}s")

		// Phase 2: After intensiveDurationMs, switch back to global 10s collection
		callResumeRunnable = Runnable {
			DroidDex.updateRawPerformanceDataCollection(
				delaySeconds = globalDelaySeconds
			)
			delaySeconds = globalDelaySeconds
			isCallSimulationActive = false
			binding.btnSimulateCall.isEnabled = true
			binding.btnSimulateCall.text = "Simulate Call (3s for 20s, then back to 10s)"
			val elapsed = System.currentTimeMillis() - callStartTime
			Timber.d("CALL SIMULATION: Phase 2 — resumed ${globalDelaySeconds}s intervals (call lasted ${elapsed}ms)")
		}
		handler.postDelayed(callResumeRunnable!!, intensiveDurationMs)
	}

	private fun cancelCallSimulation() {
		callResumeRunnable?.let { handler.removeCallbacks(it) }
		callResumeRunnable = null
		isCallSimulationActive = false
	}

	private fun displayRawData(rawData: RawPerformanceDataResult) {
		val currentTime = System.currentTimeMillis()
		eventCount++

		// Calculate timing metrics
		val actualResponseTime = if (lastEventTime > 0) {
			currentTime - lastEventTime
		} else {
			currentTime - collectionStartTime
		}

		// Expected time is delay in milliseconds (+ small buffer for processing)
		val expectedDelayMs = delaySeconds * 1000L
		val processingTime = actualResponseTime - expectedDelayMs

		// Update timestamp display
		val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(rawData.timestamp))
		binding.tvTimestamp.text = "Event #$eventCount: $timestamp\nProcessing time: ${processingTime}ms"

		// Store for next calculation
		lastEventTime = currentTime

		Timber.d("Raw performance data received at: %s", timestamp)
	}

	override fun onDestroy() {
		cancelCallSimulation()
		super.onDestroy()
		DroidDex.shutdown()
	}
}
