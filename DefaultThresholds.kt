package com.blinkit.droiddex

import com.blinkit.droiddex.models.*
import com.blinkit.droiddex.battery.models.*
import com.blinkit.droiddex.cpu.models.*
import com.blinkit.droiddex.memory.models.*
import com.blinkit.droiddex.network.models.*
import com.blinkit.droiddex.storage.models.*

/**
 * Default threshold values extracted from the original hardcoded implementations
 * Use these values as reference for backend API development and testing
 */
object DefaultThresholds {

    val DEFAULT_PERFORMANCE_THRESHOLDS = PerformanceThresholds(
        memory = MemoryThresholds(
            low = MemoryLowThresholds(
                approxHeapRemainingInMBThreshold = 64f,
                approxHeapLimitInMBThreshold = 128f
            ),
            average = MemoryAverageThresholds(
                approxHeapRemainingInMBThreshold = 128f,
                approxHeapLimitInMBThreshold = 256f,
                availableRamGBThreshold = 2f
            ),
            high = MemoryHighThresholds(
                approxHeapRemainingInMBThreshold = 256f,
                availableRamGBThreshold = 3f
            )
        ),
        
        battery = BatteryThresholds(
            excellent = BatteryExcellentThresholds(
                batteryPercentageThreshold = 80f,
                isChargingBatteryPercentageThreshold = 70f,
                temperatureThreshold = 30f // °C - Below this temp = EXCELLENT performance
            ),
            high = BatteryHighThresholds(
                batteryPercentageThreshold = 55f,
                isChargingBatteryPercentageThreshold = 50f,
                temperatureThreshold = 34f // °C - Below this temp = HIGH performance
            ),
            average = BatteryAverageThresholds(
                batteryPercentageThreshold = 40f,
                isChargingBatteryPercentageThreshold = 35f,
                temperatureThreshold = 38f // °C - Below this temp = AVERAGE performance
            )
        ),
        
        cpu = CpuThresholds(
            excellent = CpuExcellentThresholds(
                mediaPerformanceClassThreshold = 33, // Build.VERSION_CODES.TIRAMISU
                totalRamGBThreshold = 12f
            ),
            low = CpuLowThresholds(
                androidVersionThreshold = 21,
                coreCountThreshold = 2,
                heapLimitMBThreshold = 100f,
                maxCpuFrequencyThreshold1 = 1250f,
                maxCpuFrequencyThreshold2 = 1600f,
                maxCpuFrequencyThreshold3 = 1300f,
                heapLimitMBThreshold2 = 128f,
                androidVersionThreshold2 = 21,
                androidVersionThreshold3 = 24,
                totalRamGBThreshold = 2f
            ),
            average = CpuAverageThresholds(
                coreCountThreshold = 8,
                heapLimitMBThreshold = 160f,
                maxCpuFrequencyThreshold = 2055f,
                androidVersionThreshold = 23,
                totalRamGBThreshold = 6f
            )
        ),
        
        network = NetworkThresholds(
            low = NetworkLowThresholds(
                bandwidthAverageThreshold = 150.0 // 0.15 Mbps
            ),
            average = NetworkAverageThresholds(
                bandwidthAverageThreshold = 550.0, // 0.55 Mbps
                downloadSpeedThreshold = 2000, // 2 Mbps
                signalStrengthThreshold = 2
            ),
            high = NetworkHighThresholds(
                bandwidthAverageThreshold = 2000.0, // 2 Mbps
                downloadSpeedThreshold = 5000, // 5 Mbps
                signalStrengthThreshold = 3
            ),
            excellent = NetworkExcellentThresholds(
                downloadSpeedThreshold = 10000, // 10 Mbps
                signalStrengthThreshold = 4
            )
        ),
        
        storage = StorageThresholds(
            excellent = StorageExcellentThresholds(
                availableStorageGBThreshold = 16f
            ),
            high = StorageHighThresholds(
                availableStorageGBThreshold = 8f
            ),
            average = StorageAverageThresholds(
                availableStorageGBThreshold = 4f
            )
        )
    )
}