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

# 保留日志调用（注释掉 assumenosideeffects 以便在debug构建中保留日志）
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
#     public static *** w(...);
#     public static *** e(...);
# }

# 保留协程和序列化相关
-keepnames class kotlinx.serialization.internal.** { *; }
-keepattributes *Annotation*
-keepclassmembers class kotlinx.** {
    *;
}

# 保留文章相关类
-keep class com.quanneng.memory.features.articles.** { *; }
-keepclassmembers class com.quanneng.memory.features.articles.** { *; }
