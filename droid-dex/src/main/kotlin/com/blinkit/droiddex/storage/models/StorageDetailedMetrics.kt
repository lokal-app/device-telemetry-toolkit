package com.blinkit.droiddex.storage.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddex.models.DetailedMetrics
import com.blinkit.droiddex.utils.roundToPrecision

@Keep
public data class StorageDetailedMetrics(
    override val performanceLevel: PerformanceLevel,
    val totalStorageGB: Float,
    val availableStorageGB: Float
) : DetailedMetrics() {
    override fun toMap(): Map<String, Any?> = mapOf(
        "performanceLevel" to performanceLevel.name,
        "totalStorageGB" to totalStorageGB.roundToPrecision(2),
        "availableStorageGB" to availableStorageGB.roundToPrecision(2)
    )
}