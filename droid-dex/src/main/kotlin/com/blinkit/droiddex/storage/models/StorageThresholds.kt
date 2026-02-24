package com.blinkit.droiddex.storage.models

import androidx.annotation.Keep

@Keep
public data class StorageThresholds(
    val excellent: StorageExcellentThresholds = StorageExcellentThresholds(),
    val high: StorageHighThresholds = StorageHighThresholds(),
    val average: StorageAverageThresholds = StorageAverageThresholds()
)

@Keep
public data class StorageExcellentThresholds(
    val availableStorageGBThreshold: Float = 16f
)

@Keep
public data class StorageHighThresholds(
    val availableStorageGBThreshold: Float = 8f
)

@Keep
public data class StorageAverageThresholds(
    val availableStorageGBThreshold: Float = 4f
)