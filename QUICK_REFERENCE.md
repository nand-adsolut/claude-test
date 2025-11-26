# Quick Reference Guide

Fast lookup for common tasks and questions.

## How Do I...?

### Configure the Adapter in AdMob?

1. Go to [AdMob Console](https://apps.admob.com/)
2. Mediation → Mediation Groups → Add Custom Event
3. **Class Name:** `com.yourcompany.ads.admob.CustomMediationAdapter`
4. **Parameter:** `{"parameter": "YOUR_AD_UNIT_ID"}`

---

### Add the Adapter to My App?

```groovy
dependencies {
    implementation 'com.yourcompany.ads:admob-adapter:1.0.0'
    implementation 'com.google.android.gms:play-services-ads:22.5.0'
}
```

---

### Load a Banner Ad?

```kotlin
val adView = AdView(this)
adView.adUnitId = "ca-app-pub-XXX/YYY"
adView.setAdSize(AdSize.BANNER)
adView.loadAd(AdRequest.Builder().build())
```

AdMob handles mediation automatically!

---

### Load an Interstitial Ad?

```kotlin
InterstitialAd.load(
    this,
    "ca-app-pub-XXX/YYY",
    AdRequest.Builder().build(),
    object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
            // Later: ad.show(this@Activity)
        }
    }
)
```

---

### Integrate My Ad SDK?

Replace the TODOs in these files:

**1. CustomMediationAdapter.kt - Initialize:**
```kotlin
override fun initialize(...) {
    YourAdSDK.initialize(context, object : YourInitCallback {
        override fun onInitSuccess() {
            callback.onInitializationSucceeded()
        }
    })
}
```

**2. CustomBannerAdapter.kt - Load:**
```kotlin
private fun loadYourBannerAd(...) {
    bannerView = YourBannerAdView(context)
    bannerView?.setAdUnitId(adUnitId)
    bannerView?.setAdListener(object : YourAdListener {
        override fun onAdLoaded() {
            bannerAdCallback = callback.onSuccess(this@CustomBannerAdapter)
        }
    })
    bannerView?.loadAd()
}
```

---

### Debug Why Ads Aren't Loading?

**Check Logcat:**
```bash
adb logcat | grep "Custom.*Adapter"
```

**Look for:**
```
CustomMediationAdapter: Initializing adapter...
CustomMediationAdapter: Adapter initialized successfully
CustomBannerAdapter: Loading banner ad for unit: /app/banner/home
CustomBannerAdapter: Banner ad loaded successfully
```

**Common Issues:**
- ❌ No logs → Adapter not in mediation waterfall
- ❌ "Ad unit ID is missing" → Check server parameters in AdMob
- ❌ Logs stop after "Loading..." → Check your SDK integration

---

### Test with AdMob Test Ads?

Use these test ad unit IDs during development:

```kotlin
// Banner
"ca-app-pub-3940256099942544/6300978111"

// Interstitial
"ca-app-pub-3940256099942544/1033173712"

// Rewarded
"ca-app-pub-3940256099942544/5224354917"
```

Your adapter will still be called!

---

### Handle Ad Callbacks?

**Banner:**
```kotlin
adView.adListener = object : AdListener() {
    override fun onAdLoaded() { /* Ad ready */ }
    override fun onAdFailedToLoad(error: LoadAdError) { /* Failed */ }
    override fun onAdClicked() { /* User clicked */ }
}
```

**Interstitial:**
```kotlin
interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
    override fun onAdShowedFullScreenContent() { /* Shown */ }
    override fun onAdDismissedFullScreenContent() { /* Closed */ }
    override fun onAdClicked() { /* Clicked */ }
}
```

---

### Add ProGuard Rules?

Already included in the adapter! But if needed:

```proguard
-keep class com.yourcompany.ads.admob.** { *; }
```

---

### Build and Publish the Adapter?

**Build:**
```bash
./gradlew :admob-adapter:assembleRelease
```

**Publish to Maven Local (for testing):**
```bash
./gradlew :admob-adapter:publishReleasePublicationToMavenLocal
```

**Publish to Maven Central:**
1. Configure signing keys
2. Update `gradle.properties` with credentials
3. Run: `./gradlew :admob-adapter:publishReleasePublicationToSonatypeRepository`

---

## Common Error Codes

| Code | Name | Meaning | Fix |
|------|------|---------|-----|
| 0 | INTERNAL_ERROR | Unknown error | Check logs for details |
| 1 | INVALID_REQUEST | Bad request | Check ad unit ID and config |
| 2 | NETWORK_ERROR | Network issue | Check internet connection |
| 3 | NO_FILL | No ad available | Normal, try next network |
| 100 | Custom | Missing parameter | Configure in AdMob dashboard |

---

## Request Flow Summary

```
1. App calls adView.loadAd()
   ↓
2. AdMob checks mediation waterfall
   ↓
3. AdMob calls your adapter.loadBannerAd()
   ↓
4. Your adapter calls yourSDK.loadAd()
   ↓
5. Your SDK fetches ad from your server
   ↓
6. Your adapter calls callback.onSuccess()
   ↓
7. AdMob calls adapter.getView()
   ↓
8. App displays ad in UI
```

---

## File Structure Reference

```
admob-adapter/
├── src/main/java/com/yourcompany/ads/admob/
│   ├── CustomMediationAdapter.kt      ← Main adapter (initialize, route requests)
│   ├── CustomBannerAdapter.kt         ← Banner ads
│   ├── CustomInterstitialAdapter.kt   ← Interstitial ads
│   └── CustomRewardedAdapter.kt       ← Rewarded ads
├── build.gradle                       ← Dependencies and publishing
├── proguard-rules.pro                 ← ProGuard rules
└── consumer-rules.pro                 ← Auto-applied rules
```

---

## Key Classes to Modify

### For Your SDK Integration:

1. **CustomMediationAdapter.kt**
   - `initialize()` method → Initialize your SDK
   - Update `SDK_VERSION` constant

2. **CustomBannerAdapter.kt**
   - `loadYourBannerAd()` → Load banner from your SDK
   - `getView()` → Return your banner view

3. **CustomInterstitialAdapter.kt**
   - `loadYourInterstitialAd()` → Load interstitial
   - `showAd()` → Show interstitial

4. **CustomRewardedAdapter.kt**
   - `loadYourRewardedAd()` → Load rewarded ad
   - `showAd()` → Show rewarded ad with reward callback

---

## Callback Quick Reference

### Report Success
```kotlin
callback.onSuccess(this@CustomBannerAdapter)
```

### Report Failure
```kotlin
callback.onFailure(AdError(code, message, "com.yourcompany.ads"))
```

### Report Impression
```kotlin
adCallback?.reportAdImpression()
```

### Report Click
```kotlin
adCallback?.reportAdClicked()
adCallback?.onAdOpened()  // If opens browser
```

### Report Close
```kotlin
adCallback?.onAdClosed()
```

### Report Reward
```kotlin
val reward = object : RewardItem {
    override fun getType() = "coin"
    override fun getAmount() = 10
}
adCallback?.onUserEarnedReward(reward)
```

---

## AdMob Dashboard Configuration

### Mediation Group Settings

**Ad Format:** Banner / Interstitial / Rewarded

**Custom Event Name:** Your Ad Network

**Class Name:**
```
com.yourcompany.ads.admob.CustomMediationAdapter
```

**Parameter (JSON):**
```json
{
  "parameter": "/YOUR_APP/ad_format/placement_id"
}
```

**eCPM:** Set competitive value (e.g., $3.00)

---

## Testing Checklist

### Local Testing
- [ ] Adapter builds without errors
- [ ] Compiles with AdMob SDK
- [ ] ProGuard runs successfully

### Integration Testing
- [ ] Adapter initializes correctly
- [ ] Banner ads load and display
- [ ] Interstitial ads load and show
- [ ] Rewarded ads load, show, and grant rewards
- [ ] Click tracking works
- [ ] Impression tracking works
- [ ] Error handling works (no fill, network errors)

### Production Testing
- [ ] Test with small traffic percentage (1-5%)
- [ ] Monitor fill rate
- [ ] Monitor eCPM
- [ ] Monitor error rates
- [ ] Gradually increase traffic

---

## Support Resources

**Documentation:**
- README.md - Overview and quick start
- INTEGRATION_GUIDE.md - Detailed integration steps
- SAMPLE_INTEGRATION.md - Code examples
- REQUEST_FLOW.md - How requests work
- ARCHITECTURE.md - System architecture

**AdMob Resources:**
- [AdMob Mediation Guide](https://developers.google.com/admob/android/mediate)
- [Custom Events Documentation](https://developers.google.com/admob/android/custom-events)
- [AdMob SDK Reference](https://developers.google.com/admob/android/api/reference)

**Your Support:**
- Email: support@yourcompany.com
- Documentation: https://docs.yourcompany.com
- Issues: https://github.com/yourcompany/admob-adapter/issues

---

## Troubleshooting Quick Fixes

### "Ad unit ID is missing"
→ Add server parameter in AdMob: `{"parameter": "YOUR_AD_UNIT_ID"}`

### "Adapter not called"
→ Check eCPM, increase if too low to reach in waterfall

### "ClassNotFoundException"
→ Add dependency: `implementation 'com.yourcompany.ads:admob-adapter:1.0.0'`

### "Ads not showing"
→ Check `getView()` returns valid View and callbacks are called on main thread

### "App crashes on R8/ProGuard"
→ Add keep rule: `-keep class com.yourcompany.ads.admob.** { *; }`

---

## Version Information

**Current Version:** 1.0.0

**Supported AdMob SDK:** 22.5.0+

**Minimum Android SDK:** 21 (Android 5.0)

**Target Android SDK:** 34 (Android 14)

**Kotlin Version:** 1.9.0+

---

## Quick Commands

**Build:**
```bash
./gradlew :admob-adapter:build
```

**Run Tests:**
```bash
./gradlew :admob-adapter:test
```

**Generate AAR:**
```bash
./gradlew :admob-adapter:assembleRelease
# Output: admob-adapter/build/outputs/aar/admob-adapter-release.aar
```

**View Logs:**
```bash
adb logcat | grep -E "Custom.*Adapter|AdMob"
```

**Clear Logs:**
```bash
adb logcat -c
```
