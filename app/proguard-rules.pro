# -----------------------------------------------------------------------------
# Net Speed Indicator - Comprehensive Production R8 & ProGuard Optimization Rules
# -----------------------------------------------------------------------------

# 1. Stack Trace & Line Number Preservation
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**

# 2. Kotlin Standard Library, Coroutines & Object Singletons
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve Kotlin `object` singleton INSTANCE fields across the app
-keepclassmembers class * {
    public static final ** INSTANCE;
}

# 3. AndroidX Navigation & UI Navigation Destinations (Crucial for Screen routes & tabs)
-dontwarn androidx.navigation.**
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.** { *; }

-keep class com.aistudio.netspeedindicator.ui.navigation.** { *; }
-keepclassmembers class com.aistudio.netspeedindicator.ui.navigation.** {
    public static final ** INSTANCE;
    public static final ** Companion;
    public <fields>;
    public <methods>;
}

# 4. Android Jetpack Lifecycle, Activity & ViewModel
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    <init>(android.app.Application);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    <init>(android.app.Application);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
    <init>(...);
}

# 5. Jetpack Compose Compiler & Runtime Optimization
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.foundation.** { *; }

-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
    @androidx.compose.runtime.NonRestartableComposable *;
    @androidx.compose.runtime.Immutable *;
    @androidx.compose.runtime.Stable *;
}
-keep class * implements androidx.compose.runtime.State { *; }
-keep class androidx.compose.runtime.snapshots.** { *; }

# 6. Application Models, Enums, Singletons & Repositories
-keep class com.aistudio.netspeedindicator.** { *; }
-keepclassmembers class com.aistudio.netspeedindicator.** { *; }
-keep interface com.aistudio.netspeedindicator.** { *; }

# Keep all Enums and their methods (vital for SharedPreferences string-to-enum resolution)
-keep enum com.aistudio.netspeedindicator.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# Keep Data classes copy, getters, and constructors
-keepclassmembers class com.aistudio.netspeedindicator.model.** {
    public <fields>;
    public <methods>;
}

# 7. Core Android Components (Activities, Services, Receivers, Widgets, Application)
-keep public class com.aistudio.netspeedindicator.MainActivity {
    public <init>();
    *;
}
-keep public class com.aistudio.netspeedindicator.NetSpeedApp {
    public <init>();
    *;
}
-keep public class com.aistudio.netspeedindicator.service.** {
    public <init>();
    *;
}
-keep public class com.aistudio.netspeedindicator.receiver.** {
    public <init>();
    *;
}
-keep public class com.aistudio.netspeedindicator.widget.** {
    public <init>();
    *;
}

# 8. System API Reflection & Notification Builder (Android 16 Promoted Ongoing & Chips)
-dontwarn android.app.Notification$Builder
-dontwarn android.app.Notification
-keepclassmembers class android.app.Notification$Builder {
    public *;
}
-keepclassmembers class android.app.Notification {
    public *;
}

# 9. RemoteViews & Drawables
-keepclassmembers class * extends android.appwidget.AppWidgetProvider {
    public *;
}
