# Add project specific ProGuard rules here.
-keep class com.athens.lifeguide.data.api.** { *; }
-keep class com.athens.lifeguide.data.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
