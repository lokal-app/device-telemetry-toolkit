# Droid-Dex Library - Claude Code Analysis

## Overview

**Droid-Dex** is an Android performance monitoring and classification library developed by Blinkit (formerly Grofers). It provides real-time device performance assessment across 5 key parameters to help developers make smart, device-aware decisions in their apps.

## Core Purpose & Architecture

**What it does:** Continuously monitors Android device performance and classifies it into levels (UNKNOWN, LOW, AVERAGE, HIGH, EXCELLENT) across 5 categories:

1. **CPU** - Core count, frequency, RAM, Android version, SoC model
2. **MEMORY** - Heap limit, heap remaining, available RAM  
3. **NETWORK** - Bandwidth strength, download speed, signal strength
4. **STORAGE** - Available storage space
5. **BATTERY** - Percentage remaining, charging status

## Key Components & APIs

### Main Entry Point
- **File**: `droid-dex/src/main/kotlin/com/blinkit/droiddex/DroidDex.kt:14`
- Singleton object providing public APIs
- **Initialization**: `DroidDex.init(applicationContext)`

### Core APIs
```kotlin
// Single/multiple parameter performance level
DroidDex.getPerformanceLevel(PerformanceClass.CPU, PerformanceClass.MEMORY)
DroidDex.getPerformanceLevelLd(params).observe(this) { /* handle changes */ }

// Weighted performance level  
DroidDex.getWeightedPerformanceLevel(PerformanceClass.NETWORK to 2F, PerformanceClass.MEMORY to 1F)
DroidDex.getWeightedPerformanceLevelLd(params).observe(this) { /* handle changes */ }
```

### Architecture Pattern
- **Factory Pattern**: `PerformanceManagerFactory.kt:14` creates appropriate managers
- **Abstract Base**: `PerformanceManager.kt:11` defines common behavior
- **Concrete Implementations**: Individual managers for each performance class
- **LiveData Pattern**: Reactive updates using Android Architecture Components

## Performance Managers Deep Dive

### 1. CPU Performance (`CpuPerformanceManager.kt:19`)
- Inspired by Telegram's CPU classification algorithm
- **Monitors**: Core count, CPU frequency, RAM, Android version, SoC model
- Special handling for known low-performance SoCs (hardcoded list)
- Uses Google Play Services Device Performance API
- **Update frequency**: 60 seconds

### 2. Memory Performance (`MemoryPerformanceManager.kt:13`)
- Checks Android's low memory flag first
- **Monitors**: Available RAM, heap limit, heap remaining
- Classification based on heap thresholds (64MB, 128MB, 256MB)
- **Update frequency**: 10 seconds

### 3. Network Performance (`NetworkPerformanceManager.kt:21`)
- Multi-factor analysis with weighted scoring
- **Monitors**: Bandwidth average, download speed, signal strength
- Supports WiFi and Cellular networks
- Network generation detection (2G/3G/4G/5G)
- Uses `BandwidthManager.kt:19` for speed sampling
- **Update frequency**: 2.5 seconds

### 4. Storage Performance (`StoragePerformanceManager.kt:13`)
- **Monitors**: Available storage space
- Simple threshold-based classification
- **Update frequency**: 60 seconds

### 5. Battery Performance (`BatteryPerformanceManager.kt:12`)
- **Monitors**: Battery percentage and charging status
- Higher thresholds when device is charging
- Uses Android's BatteryManager
- **Update frequency**: 60 seconds

## Key Algorithms & Logic

### Weighted Performance Calculation (`Utils.kt:25`)
```kotlin
// Calculates weighted average of performance levels
weightedSum += level.level * weight
totalWeight += weight
finalLevel = floor(weightedSum / totalWeight)
```

### Periodic Monitoring (`Utils.kt:55`)
- Uses ProcessLifecycleOwner for app lifecycle awareness
- Runs monitoring only when app is RESUMED
- Async execution on IO dispatcher
- Configurable delay per performance class

### LiveData Aggregation (`Utils.kt:37`)
- Uses MediatorLiveData for combining multiple LiveData sources
- Reactive updates when any monitored parameter changes
- Handles weighted calculations in real-time

## Integration Points

### Android System Integration
- `Context.getSystemService()` for various managers
- `TelephonyManager` for network info
- `ConnectivityManager` for network capabilities  
- `BatteryManager` for battery status
- `PlayServicesDevicePerformance` for Google's performance API

### Dependencies
- AndroidX Lifecycle components for reactive patterns
- Timber for logging
- Kotlin Coroutines for async operations
- Google Play Services for device performance

## Usage Patterns from Example

### Initialization (`ExampleApplication.kt:14`)
```kotlin
DroidDex.init(this) // In Application.onCreate()
```

### Individual Parameter Monitoring (`MainActivity.kt:63`)
```kotlin
DroidDex.getPerformanceLevelLd(PerformanceClass.CPU).observe(this) { level ->
    // Update UI based on performance level
}
```

### Combined Parameters (`MainActivity.kt:42`)
```kotlin
DroidDex.getPerformanceLevelLd(PerformanceClass.CPU, PerformanceClass.MEMORY)
```

### Weighted Combinations (`MainActivity.kt:48`)
```kotlin
DroidDex.getWeightedPerformanceLevelLd(
    PerformanceClass.MEMORY to 2F, 
    PerformanceClass.NETWORK to 1F
)
```

## Performance Level Enum (`PerformanceLevel.kt:6`)
```kotlin
enum class PerformanceLevel(public val level: Int) {
    UNKNOWN(0), LOW(1), AVERAGE(2), HIGH(3), EXCELLENT(4)
}
```

## Performance Class Constants (`PerformanceClass.kt:24`)
```kotlin
const val CPU: Int = 0
const val MEMORY: Int = 1
const val STORAGE: Int = 2
const val NETWORK: Int = 3
const val BATTERY: Int = 4
```

## Notable Implementation Details

1. **Thread Safety**: Uses `@Volatile` for performance level variables
2. **Error Handling**: Comprehensive try-catch with logging throughout
3. **Lifecycle Awareness**: Only monitors when app is active
4. **Caching**: Factory pattern caches manager instances
5. **Type Safety**: Uses `@IntDef` annotations for performance classes
6. **Logging**: Structured logging with performance level change tracking

## Build Configuration

### Module Structure
- **Main Library**: `droid-dex/` - Contains the core library code
- **Example App**: `example/` - Demo app showing usage patterns
- **Gradle Wrapper**: Uses Gradle 8.11.1 with local distribution workaround

### Dependencies (`droid-dex/build.gradle.kts`)
- Kotlin with explicit API mode
- AndroidX Core and Lifecycle
- Coroutines for async operations
- Timber for logging
- Google Play Services Performance

### Publishing
- Multi-variant library publishing to Maven Central
- GitHub Packages publishing for older versions
- Consumer ProGuard rules included

## Network Performance Thresholds (`NetworkPerformanceManager.kt:180-190`)
```kotlin
// Bandwidth thresholds (in Kbps)
EXCELLENT_BANDWIDTH_THRESHOLD = 2000F // 2 Mbps
HIGH_BANDWIDTH_THRESHOLD = 550F // 0.55 Mbps  
AVERAGE_BANDWIDTH_THRESHOLD = 150F // 0.15 Mbps

// Download speed thresholds (in Kbps)
EXCELLENT_DOWNLOAD_SPEED_THRESHOLD = 10000 // 10 Mbps
HIGH_DOWNLOAD_SPEED_THRESHOLD = 5000 // 5 Mbps
AVERAGE_DOWNLOAD_SPEED_THRESHOLD = 2000 // 2 Mbps

// Signal strength thresholds (0-4 scale)
EXCELLENT_SIGNAL_THRESHOLD = 4
HIGH_SIGNAL_THRESHOLD = 3
AVERAGE_SIGNAL_THRESHOLD = 2
```

## Known Issues & Workarounds

### Gradle Network Issues
- **Issue**: `java.net.BindException: Can't assign requested address` when downloading Gradle distribution
- **Cause**: Corporate firewall/proxy (Sophos, SCALE FUSION) blocking network requests
- **Solution**: Configure local Gradle distribution in `gradle/wrapper/gradle-wrapper.properties`:
  ```properties
  distributionUrl=file\:///Users/[user]/.gradle/wrapper/dists/gradle-8.11.1-all/gradle-8.11.1-all.zip
  ```

### Build Requirements
- Requires local Gradle distribution due to network restrictions
- Clean build required after network configuration changes
- Android SDK 21+ minimum, targets latest SDK

---

*Generated by Claude Code for reference in future sessions*