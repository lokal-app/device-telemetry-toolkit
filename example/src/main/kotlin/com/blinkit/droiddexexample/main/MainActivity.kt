package com.blinkit.droiddexexample.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.blinkit.droiddex.DroidDex
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
import timber.log.Timber

private const val LOGO_HEIGHT_DP = 64

class MainActivity: AppCompatActivity() {

	private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
	private lateinit var stressTestManager: StressTestManager
	private var rawDataObserver: Observer<RawPerformanceDataResult>? = null
	private var isRawDataCollectionActive = false

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
		DroidDex.getDetailedMetricsLd(performanceClass)?.observe(this) { detailedMetrics ->
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
			"Start Raw Data Collection (15s intervals)"
		}
	}
	
	private fun initializeDroidDexWithCustomThresholds() {
		// Initialize with custom thresholds to demonstrate Flow 3
		val customThresholds = PerformanceThresholds()
		DroidDex.init(this, customThresholds)
		Timber.d("DroidDex initialized with custom thresholds")
	}
	
	private fun startRawDataCollection() {
		val liveData = DroidDex.startRawPerformanceDataCollection(
			PerformanceClass.CPU,
			PerformanceClass.MEMORY,
			PerformanceClass.NETWORK,
			PerformanceClass.STORAGE,
			PerformanceClass.BATTERY,
			delay = 15
		)

		rawDataObserver = Observer { rawData ->
			displayRawData(rawData)
		}

		liveData?.observe(this, rawDataObserver!!)
		isRawDataCollectionActive = true
		updateNewFlowsButtonText()
		
		Timber.d("Raw data collection started")
	}
	
	private fun stopRawDataCollection() {
		DroidDex.stopRawPerformanceDataCollection()
		
		rawDataObserver?.let { observer ->
			// Remove observer if we have reference to LiveData
			// Note: In production, you'd want to store LiveData reference to properly clean up
		}
		rawDataObserver = null
		isRawDataCollectionActive = false
		updateNewFlowsButtonText()
		
		Timber.d("Raw data collection stopped")
	}
	
	private fun displayRawData(rawData: RawPerformanceDataResult) {
		// Update timestamp display
		val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(rawData.timestamp))
		binding.tvTimestamp.text = "Last updated: $timestamp"
		
		Timber.d("Raw performance data received at: %s", timestamp)
	}
	
	
	
	
	
	
	
	override fun onDestroy() {
		super.onDestroy()
		// Stop raw data collection when activity is destroyed
		if (isRawDataCollectionActive) {
			DroidDex.stopRawPerformanceDataCollection()
		}
	}
}
