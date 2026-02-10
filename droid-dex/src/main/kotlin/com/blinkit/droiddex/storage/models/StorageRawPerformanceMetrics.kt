package com.blinkit.droiddex.storage.models

import androidx.annotation.Keep

@Keep
public data class StorageRawPerformanceMetrics(
    val totalStorageGB: Float,
    val availableStorageGB: Float
)