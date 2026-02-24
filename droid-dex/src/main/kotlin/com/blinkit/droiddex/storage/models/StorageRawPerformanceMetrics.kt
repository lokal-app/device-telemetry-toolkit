package com.blinkit.droiddex.storage.models

import androidx.annotation.Keep
import com.blinkit.droiddex.models.RawPerformanceMetrics
import com.blinkit.droiddex.utils.roundToPrecision

@Keep
public data class StorageRawPerformanceMetrics(
    val totalStorageGB: Float,
    val availableStorageGB: Float
) : RawPerformanceMetrics {
    override fun toMap(): Map<String, Any?> = mapOf(
        "totalStorageGB" to totalStorageGB.roundToPrecision(2),
        "availableStorageGB" to availableStorageGB.roundToPrecision(2)
    )
}
