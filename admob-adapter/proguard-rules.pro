# ProGuard rules for AdMob Mediation Adapter

# Keep all adapter classes
-keep class com.yourcompany.ads.admob.** { *; }

# Keep Google Mobile Ads classes
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep mediation adapter classes from being obfuscated
-keepclassmembers class com.yourcompany.ads.admob.CustomMediationAdapter {
    public *;
}

-keepclassmembers class com.yourcompany.ads.admob.CustomBannerAdapter {
    public *;
}

-keepclassmembers class com.yourcompany.ads.admob.CustomInterstitialAdapter {
    public *;
}

-keepclassmembers class com.yourcompany.ads.admob.CustomRewardedAdapter {
    public *;
}

# TODO: Add ProGuard rules for your underlying ad SDK
# Example:
# -keep class com.yoursdk.** { *; }
# -dontwarn com.yoursdk.**
