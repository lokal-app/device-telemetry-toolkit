package com.blinkit.droiddex.models

import androidx.annotation.Keep
import com.blinkit.droiddex.battery.models.BatteryThresholds
import com.blinkit.droiddex.cpu.models.CpuThresholds
import com.blinkit.droiddex.memory.models.MemoryThresholds
import com.blinkit.droiddex.network.models.NetworkThresholds
import com.blinkit.droiddex.storage.models.StorageThresholds

@Keep
public data class PerformanceThresholds(
    val memory: MemoryThresholds = MemoryThresholds(),
    val cpu: CpuThresholds = CpuThresholds(),
    val network: NetworkThresholds = NetworkThresholds(),
    val storage: StorageThresholds = StorageThresholds(),
    val battery: BatteryThresholds = BatteryThresholds()
)





