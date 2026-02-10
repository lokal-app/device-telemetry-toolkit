package com.blinkit.droiddex.storage.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics

@Keep
public data class StorageDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val totalStorageGB: Float,
    val availableStorageGB: Float
) : DetailedMetrics()