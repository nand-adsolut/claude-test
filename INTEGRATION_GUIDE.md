# Integration Guide: AdMob Mediation Adapter

Complete guide for integrating your ad network with AdMob using this custom mediation adapter.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Step-by-Step Integration](#step-by-step-integration)
3. [Implementing Your Ad SDK](#implementing-your-ad-sdk)
4. [Testing](#testing)
5. [Advanced Configuration](#advanced-configuration)

## Prerequisites

Before you begin, ensure you have:

- ✅ An active AdMob account
- ✅ An Android app with AdMob SDK integrated
- ✅ Your ad network SDK and credentials
- ✅ Basic knowledge of Android development

## Step-by-Step Integration

### 1. Add the Adapter Dependency

Add the adapter to your app's `build.gradle`:

**Kotlin DSL (build.gradle.kts):**
```kotlin
dependencies {
    implementation("com.yourcompany.ads:admob-adapter:1.0.0")

    // Make sure you have AdMob SDK
    implementation("com.google.android.gms:play-services-ads:22.5.0")
}
```

**Groovy (build.gradle):**
```groovy
dependencies {
    implementation 'com.yourcompany.ads:admob-adapter:1.0.0'

    // Make sure you have AdMob SDK
    implementation 'com.google.android.gms:play-services-ads:22.5.0'
}
```

### 2. Configure Mediation in AdMob Dashboard

#### 2.1 Create/Select Ad Unit

1. Go to [AdMob Console](https://apps.admob.com/)
2. Select your app
3. Navigate to **Ad units**
4. Select an existing ad unit or create a new one

#### 2.2 Set Up Mediation

1. Go to **Mediation** → **Mediation groups**
2. Click **Create mediation group**
3. Select your ad format (Banner, Interstitial, or Rewarded)
4. Select your ad unit(s)
5. Click **Add custom event**

#### 2.3 Configure Custom Event

Fill in the following details:

**Label:** Your Ad Network (e.g., "My Custom Ad Network")

**Class Name:**
```
com.yourcompany.ads.admob.CustomMediationAdapter
```

**Parameter (Server Parameters):**
```json
{
  "parameter": "/YOUR_APP/ad_format/placement_id"
}
```

⚠️ **Important Notes:**
- The key must be exactly `parameter` (case-sensitive)
- The value should be your ad network's placement/ad unit ID
- Use different IDs for different placements

#### Example Configurations:

**Banner Ad:**
```json
{
  "parameter": "/myapp/banner/home_screen"
}
```

**Interstitial Ad:**
```json
{
  "parameter": "/myapp/interstitial/game_over"
}
```

**Rewarded Ad:**
```json
{
  "parameter": "/myapp/rewarded/bonus_coins"
}
```

### 3. Add Required Permissions

Make sure your `AndroidManifest.xml` includes:

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application>
        <!-- Your app configuration -->
    </application>
</manifest>
```

### 4. ProGuard Configuration

If using ProGuard/R8, the adapter includes consumer rules that are applied automatically. However, you may need to add rules for your underlying ad SDK:

```proguard
# Your ad SDK rules
-keep class com.yoursdk.** { *; }
-dontwarn com.yoursdk.**
```

## Implementing Your Ad SDK

The adapter comes with placeholder code. You need to integrate your actual ad SDK:

### 1. Update CustomMediationAdapter.kt

Find the `initialize` method in `/admob-adapter/src/main/java/com/yourcompany/ads/admob/CustomMediationAdapter.kt`:

```kotlin
// Replace this:
isInitialized = true
callback.onInitializationSucceeded()

// With your SDK initialization:
YourAdSDK.initialize(context, object : YourInitCallback {
    override fun onInitSuccess() {
        isInitialized = true
        callback.onInitializationSucceeded()
    }

    override fun onInitFailed(error: YourError) {
        callback.onInitializationFailed(error.message)
    }
})
```

### 2. Update Banner Adapter

In `CustomBannerAdapter.kt`, replace the TODO sections:

```kotlin
private fun loadYourBannerAd(...) {
    // Initialize your banner view
    bannerView = YourBannerAdView(context)
    bannerView?.setAdUnitId(adUnitId)
    bannerView?.setAdSize(adSize.width, adSize.height)

    // Set up callbacks
    bannerView?.setAdListener(object : YourAdListener {
        override fun onAdLoaded() {
            bannerAdCallback = callback.onSuccess(this@CustomBannerAdapter)
        }

        override fun onAdFailedToLoad(error: YourAdError) {
            callback.onFailure(AdError(error.code, error.message, "com.yourcompany.ads"))
        }

        override fun onAdClicked() {
            bannerAdCallback?.reportAdClicked()
        }

        override fun onAdImpression() {
            bannerAdCallback?.reportAdImpression()
        }
    })

    // Load the ad
    bannerView?.loadAd()
}
```

### 3. Update Interstitial Adapter

In `CustomInterstitialAdapter.kt`:

```kotlin
private fun loadYourInterstitialAd(...) {
    YourAdSDK.loadInterstitial(context, adUnitId, object : YourInterstitialListener {
        override fun onAdLoaded(ad: YourInterstitialAd) {
            interstitialAd = ad
            interstitialAdCallback = callback.onSuccess(this@CustomInterstitialAdapter)
        }

        override fun onAdFailedToLoad(error: YourAdError) {
            callback.onFailure(AdError(error.code, error.message, "com.yourcompany.ads"))
        }
    })
}

override fun showAd(context: Context) {
    (interstitialAd as? YourInterstitialAd)?.show(context, object : YourShowListener {
        override fun onAdShown() {
            interstitialAdCallback?.onAdOpened()
            interstitialAdCallback?.reportAdImpression()
        }

        override fun onAdClicked() {
            interstitialAdCallback?.reportAdClicked()
        }

        override fun onAdClosed() {
            interstitialAdCallback?.onAdClosed()
        }
    })
}
```

### 4. Update Rewarded Adapter

In `CustomRewardedAdapter.kt`, follow a similar pattern and include reward handling:

```kotlin
override fun onUserEarnedReward(reward: YourReward) {
    val rewardItem = object : RewardItem {
        override fun getType(): String = reward.type
        override fun getAmount(): Int = reward.amount
    }
    rewardedAdCallback?.onUserEarnedReward(rewardItem)
}
```

## Testing

### Test Mode

1. **Enable Test Ads in AdMob:**
   - Go to your AdMob account
   - Navigate to Settings → Test devices
   - Add your test device ID

2. **Check Logs:**
   ```bash
   adb logcat | grep "Custom.*Adapter"
   ```

3. **Expected Log Flow:**
   ```
   CustomMediationAdapter: Initializing adapter...
   CustomMediationAdapter: Adapter initialized successfully
   CustomBannerAdapter: Loading banner ad for unit: /app/banner/home
   CustomBannerAdapter: Banner ad loaded successfully
   ```

### Integration Checklist

- [ ] Adapter dependency added to build.gradle
- [ ] AdMob custom event configured with correct class name
- [ ] Server parameters include "parameter" key with ad unit ID
- [ ] Your ad SDK is properly integrated in adapter code
- [ ] ProGuard rules added (if applicable)
- [ ] Internet permission in AndroidManifest.xml
- [ ] Test ads loading successfully
- [ ] Ad callbacks working (clicks, impressions, closes)

## Advanced Configuration

### Custom Parameter Parsing

If you need multiple parameters, modify the adapter to parse JSON:

```kotlin
private fun parseParameters(serverParameters: Bundle): AdConfig {
    val jsonString = serverParameters.getString("parameter") ?: ""
    return try {
        // Parse JSON and extract multiple values
        val json = JSONObject(jsonString)
        AdConfig(
            adUnitId = json.getString("ad_unit_id"),
            placementId = json.getString("placement_id"),
            customParam = json.optString("custom_param", "")
        )
    } catch (e: Exception) {
        // Fallback to treating it as a simple string
        AdConfig(adUnitId = jsonString)
    }
}
```

### Error Code Mapping

Map your SDK's error codes to AdMob error codes:

```kotlin
private fun mapErrorCode(yourErrorCode: Int): Int {
    return when (yourErrorCode) {
        YOUR_NO_FILL -> 3  // AdMob NO_FILL
        YOUR_NETWORK_ERROR -> 2  // AdMob NETWORK_ERROR
        YOUR_INVALID_REQUEST -> 1  // AdMob INVALID_REQUEST
        else -> 0  // AdMob INTERNAL_ERROR
    }
}
```

### Supporting Additional Ad Formats

To add native ad support:

1. Create `CustomNativeAdapter.kt`
2. Implement `UnifiedNativeAdMapper`
3. Update `loadNativeAd` in `CustomMediationAdapter.kt`

## Troubleshooting

### Issue: Adapter not being called

**Possible causes:**
1. Incorrect class name in AdMob settings
2. Adapter not in mediation waterfall
3. eCPM too low (adapter not reached in waterfall)

**Solution:**
- Double-check the class name: `com.yourcompany.ads.admob.CustomMediationAdapter`
- Set a high eCPM for testing
- Check mediation reports in AdMob

### Issue: "Ad unit ID is missing"

**Solution:**
Ensure your server parameters in AdMob are formatted correctly:
```
{"parameter": "your_ad_unit_id"}
```

### Issue: Adapter crashes

**Solution:**
1. Check logcat for stack traces
2. Verify your ad SDK is properly initialized
3. Ensure all callbacks are called on the main thread
4. Check ProGuard rules aren't stripping necessary classes

## Support

For additional help:
- Email: support@yourcompany.com
- Documentation: https://docs.yourcompany.com
- GitHub Issues: https://github.com/yourcompany/admob-adapter/issues
