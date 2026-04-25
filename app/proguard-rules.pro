# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============ Ktor ============
# Keep kotlinx.serialization classes
-keepattributes InnerClasses
-keepattributes *Annotation*
-keep class kotlinx.serialization.json.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class *
-keep class com.example.leximaster.** { *; }

# ============ Koin ============
-keepattributes *Annotation*
-keep class com.example.leximaster.** { *; }
-dontwarn koin.**

# ============ Room ============
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**