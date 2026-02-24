-keepattributes SourceFile,LineNumberTable

# Keep all public model classes and their toMap() methods for RN bridge serialization
-keep class com.blinkit.droiddex.models.** { *; }
-keep class com.blinkit.droiddex.cpu.models.** { *; }
-keep class com.blinkit.droiddex.memory.models.** { *; }
-keep class com.blinkit.droiddex.network.models.** { *; }
-keep class com.blinkit.droiddex.storage.models.** { *; }
-keep class com.blinkit.droiddex.battery.models.** { *; }
