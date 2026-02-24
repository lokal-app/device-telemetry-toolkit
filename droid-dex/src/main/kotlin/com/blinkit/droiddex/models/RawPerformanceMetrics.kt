package com.blinkit.droiddex.models

import androidx.annotation.Keep

// toMap() enables direct conversion to ReadableMap/WritableMap for the React Native bridge layer
@Keep
public interface RawPerformanceMetrics {
    public fun toMap(): Map<String, Any?>
}
