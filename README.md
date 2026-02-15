# Device Telemetry Toolkit

[![License](https://img.shields.io/badge/License-GPL--2.0-blue.svg)](https://opensource.org/licenses/GPL-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8+-purple.svg)](https://kotlinlang.org/)
[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

Android library for real-time device performance monitoring. Classifies device capability across CPU, Memory, Network, Storage, and Battery into actionable performance levels.

## Installation

```gradle
implementation 'com.lokalapps:device-telemetry-toolkit:1.0.0'
```

**Requirements:** Android API 24+ | Kotlin 1.8+ | AGP 8.0+

## Setup

```kotlin
// Initialize with default thresholds
DroidDex.init(applicationContext)

// Or with custom thresholds
DroidDex.init(applicationContext, PerformanceThresholds(
    memory = MemoryThresholds(...),
    battery = BatteryThresholds(...)
))
```

## API

### Performance Classification

```kotlin
// Single category
val cpuLevel = DroidDex.getPerformanceLevel(PerformanceClass.CPU)

// Multiple categories (averaged)
val level = DroidDex.getPerformanceLevel(PerformanceClass.CPU, PerformanceClass.MEMORY)

// Weighted
val weighted = DroidDex.getWeightedPerformanceLevel(
    PerformanceClass.CPU to 2.0f,
    PerformanceClass.MEMORY to 1.0f
)

// Reactive (LiveData)
DroidDex.getPerformanceLevelLd(PerformanceClass.BATTERY).observe(this) { level ->
    adjustBehavior(level)
}
```

### Weighted Performance Analysis

Returns individual + overall performance levels in a single structured result.

```kotlin
val result = DroidDex.getWeightedPerformanceLevels(
    PerformanceClass.CPU to 2.0f,
    PerformanceClass.MEMORY to 1.5f,
    PerformanceClass.NETWORK to 1.0f
)

result.overallPerformanceLevel  // Weighted average
result.cpu                      // Individual CPU level
result.memory                   // Individual Memory level

// Serialization for bridge layers
val map = result.toMap()
```

### Continuous Raw Data Collection

Streams raw device metrics at a configurable interval. Executes immediately on start, then repeats.

```kotlin
// Start collection (delaySeconds must be > 0)
val stream = DroidDex.startRawPerformanceDataCollection(
    PerformanceClass.CPU,
    PerformanceClass.MEMORY,
    PerformanceClass.NETWORK,
    PerformanceClass.STORAGE,
    PerformanceClass.BATTERY,
    delaySeconds = 15
)

stream.observe(this) { rawData ->
    rawData.cpu?.coreCount
    rawData.memory?.availableRamGB
    rawData.network?.bandwidthAverage
    rawData.nativeExecutionDurationMs  // Collection cycle timing

    // Serialization for bridge layers
    val map = rawData.toMap()
}

// Stop when done
DroidDex.stopRawPerformanceDataCollection()
```

### Lifecycle

```kotlin
// Clean shutdown — cancels all monitoring coroutines and releases resources.
// Required before re-initialization.
DroidDex.shutdown()
```

## Reference

### Performance Classes

| Constant | Value | Metrics |
|----------|-------|---------|
| `PerformanceClass.CPU` | 0 | Core count, max/current frequency, CPU usage %, Android version |
| `PerformanceClass.MEMORY` | 1 | Total/available RAM, RAM usage %, heap limit/used/remaining, native heap |
| `PerformanceClass.NETWORK` | 3 | Bandwidth, download/upload speed, signal strength, cellular type, carrier |
| `PerformanceClass.STORAGE` | 2 | Total/available storage |
| `PerformanceClass.BATTERY` | 4 | Percentage, charging status, temperature, voltage |

### Performance Levels

| Level | Value | Description |
|-------|-------|-------------|
| `UNKNOWN` | 0 | Unable to determine |
| `LOW` | 1 | Entry-level device |
| `AVERAGE` | 2 | Mid-tier device |
| `HIGH` | 3 | High-end device |
| `EXCELLENT` | 4 | Top-tier device |

### Public API

| Method | Returns |
|--------|---------|
| `init(context, thresholds?)` | `Unit` |
| `shutdown()` | `Unit` |
| `getPerformanceLevel(vararg classes)` | `PerformanceLevel` |
| `getPerformanceLevelLd(vararg classes)` | `LiveData<PerformanceLevel>` |
| `getWeightedPerformanceLevel(vararg pairs)` | `PerformanceLevel` |
| `getWeightedPerformanceLevelLd(vararg pairs)` | `LiveData<PerformanceLevel>` |
| `getDetailedMetricsLd(class)` | `LiveData<DetailedMetrics>` |
| `getWeightedPerformanceLevels(vararg pairs)` | `WeightedPerformanceLevels` |
| `startRawPerformanceDataCollection(vararg classes, delaySeconds)` | `LiveData<RawPerformanceDataResult>` |
| `stopRawPerformanceDataCollection()` | `Unit` |

All methods return non-null safe defaults when the SDK is not initialized.

## Thread Safety

- `init()` and `shutdown()` are synchronized
- `startRawPerformanceDataCollection()` and `stopRawPerformanceDataCollection()` are synchronized
- Shared state uses `@Volatile`, `ConcurrentHashMap`, and atomic immutable config objects
- Monitoring runs only while the app is in the foreground (`RESUMED` state)

## License

GNU General Public License v2.0 — see [LICENSE](LICENSE).

## Acknowledgments

Fork of [grofers/droid-dex](https://github.com/grofers/droid-dex) with continuous telemetry collection, weighted analysis, structured serialization, and lifecycle management.
