# Architecture Overview

Visual guide to understanding how the AdMob mediation adapter fits into the overall architecture.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                           YOUR ANDROID APP                                  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────┐     │
│  │                                                                    │     │
│  │   App UI Code (Activities, Fragments)                             │     │
│  │                                                                    │     │
│  │   • Banner in RecyclerView                                        │     │
│  │   • Interstitial after level complete                             │     │
│  │   • Rewarded video for bonus coins                                │     │
│  │                                                                    │     │
│  └────────────────────────┬──────────────────────────────────────────┘     │
│                           │                                                 │
│                           │ Uses AdMob API                                  │
│                           │ (AdView, InterstitialAd, RewardedAd)            │
│                           ▼                                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                                                                      │   │
│  │         Google Mobile Ads SDK (AdMob)                               │   │
│  │                                                                      │   │
│  │  • Manages mediation waterfall                                      │   │
│  │  • Tries ad networks in eCPM order                                  │   │
│  │  • Handles ad lifecycle                                             │   │
│  │  • Reports analytics                                                │   │
│  │                                                                      │   │
│  └──────────┬────────────────────┬────────────────────┬─────────────────┘   │
│             │                    │                    │                     │
│             ▼                    ▼                    ▼                     │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐            │
│  │  AdMob Network   │ │  Other Networks  │ │  YOUR CUSTOM     │            │
│  │  (Google's Ads)  │ │  (Meta, Unity)   │ │  ADAPTER         │            │
│  └──────────────────┘ └──────────────────┘ └────────┬─────────┘            │
│                                                      │                      │
│                                                      │ Implements           │
│                                                      │ AdMob Adapter API    │
│                                                      ▼                      │
│                                           ┌─────────────────────┐           │
│                                           │ CustomMediation     │           │
│                                           │ Adapter             │           │
│                                           │                     │           │
│                                           │ • Initialize        │           │
│                                           │ • Load ads          │           │
│                                           │ • Report callbacks  │           │
│                                           └──────────┬──────────┘           │
│                                                      │                      │
│                                                      │ Calls                │
│                                                      ▼                      │
│                                           ┌─────────────────────┐           │
│                                           │   YOUR AD SDK       │           │
│                                           │                     │           │
│                                           │ • Load ads          │           │
│                                           │ • Show ads          │           │
│                                           │ • Track events      │           │
│                                           └──────────┬──────────┘           │
│                                                      │                      │
└──────────────────────────────────────────────────────┼──────────────────────┘
                                                       │
                                                       │ HTTP Requests
                                                       ▼
                                            ┌─────────────────────┐
                                            │   YOUR AD SERVER    │
                                            │                     │
                                            │ • Ad inventory      │
                                            │ • Targeting         │
                                            │ • Bidding           │
                                            │ • Analytics         │
                                            └─────────────────────┘
```

---

## Component Responsibilities

### 1. Your Android App
**Responsibility:** Display ads in the user interface

**Code Example:**
```kotlin
// App just uses standard AdMob API
val adView = AdView(context)
adView.adUnitId = "ca-app-pub-XXX/YYY"
adView.loadAd(AdRequest.Builder().build())
```

**Doesn't know:**
- Which ad network will serve the ad
- How mediation works
- That your custom adapter exists

---

### 2. Google Mobile Ads SDK (AdMob)
**Responsibility:** Mediation orchestration and ad lifecycle management

**What it does:**
- Receives ad request from app
- Checks mediation waterfall configuration
- Tries ad networks in priority order
- Manages adapter initialization
- Routes callbacks back to app
- Reports analytics to AdMob dashboard

**Configuration:** Managed in AdMob web dashboard

---

### 3. Your Custom Adapter (This SDK)
**Responsibility:** Bridge between AdMob API and your ad SDK API

**What it does:**
- Implements AdMob's `Adapter` interface
- Translates AdMob requests into your SDK's API calls
- Converts your SDK's responses into AdMob callbacks
- Handles parameter extraction from AdMob config
- Maps error codes between systems

**Files:**
- `CustomMediationAdapter.kt` - Main entry point
- `CustomBannerAdapter.kt` - Banner translation layer
- `CustomInterstitialAdapter.kt` - Interstitial translation layer
- `CustomRewardedAdapter.kt` - Rewarded translation layer

---

### 4. Your Ad SDK
**Responsibility:** Load and display ads from your ad server

**What it does:**
- Makes HTTP requests to your ad server
- Handles ad rendering (images, videos, HTML)
- Tracks impressions and clicks
- Manages ad caching
- Reports events

**Examples:** Could be your proprietary SDK, or an existing SDK you're wrapping

---

### 5. Your Ad Server
**Responsibility:** Ad inventory and serving

**What it does:**
- Stores ad creatives
- Handles targeting and bidding
- Returns ad content
- Tracks delivery and performance
- Manages campaigns

**Examples:** Could be Google Ad Manager, custom server, etc.

---

## Data Flow

### Configuration Data Flow

```
┌─────────────────┐
│ AdMob Dashboard │
│                 │
│ Custom Event:   │
│ ├─ Class Name   │──────────────────────────┐
│ ├─ Label        │                          │
│ └─ Parameters   │                          │ Saved to AdMob servers
│    {"parameter":│                          │
│     "ad_unit"}  │                          │
└─────────────────┘                          │
                                             ▼
                                   ┌───────────────────┐
                                   │  AdMob Servers    │
                                   │                   │
                                   │  Mediation        │
                                   │  Configuration    │
                                   └─────────┬─────────┘
                                             │
                                             │ Downloaded when
                                             │ app initializes
                                             ▼
                                   ┌───────────────────┐
                                   │   App Runtime     │
                                   │                   │
                                   │  Waterfall with   │
                                   │  your adapter     │
                                   │  config           │
                                   └───────────────────┘
```

### Ad Request Data Flow

```
App                AdMob SDK           Your Adapter        Your SDK        Your Server
 │                    │                     │                  │                │
 │ 1. loadAd()        │                     │                  │                │
 ├───────────────────>│                     │                  │                │
 │                    │                     │                  │                │
 │                    │ 2. loadBannerAd()   │                  │                │
 │                    │    + config params  │                  │                │
 │                    ├────────────────────>│                  │                │
 │                    │                     │                  │                │
 │                    │                     │ 3. loadAd()      │                │
 │                    │                     │    + ad unit ID  │                │
 │                    │                     ├─────────────────>│                │
 │                    │                     │                  │                │
 │                    │                     │                  │ 4. HTTP GET    │
 │                    │                     │                  │    /ads?unit=X │
 │                    │                     │                  ├───────────────>│
 │                    │                     │                  │                │
 │                    │                     │                  │ 5. Ad JSON     │
 │                    │                     │                  │<───────────────┤
 │                    │                     │                  │                │
 │                    │                     │ 6. onAdLoaded()  │                │
 │                    │                     │    + ad view     │                │
 │                    │                     │<─────────────────┤                │
 │                    │                     │                  │                │
 │                    │ 7. onSuccess()      │                  │                │
 │                    │    + adapter        │                  │                │
 │                    │<────────────────────┤                  │                │
 │                    │                     │                  │                │
 │ 8. onAdLoaded()    │ 9. getView()        │                  │                │
 │<───────────────────┤────────────────────>│                  │                │
 │                    │    returns ad view  │                  │                │
 │                    │<────────────────────┤                  │                │
 │                    │                     │                  │                │
 │ 10. Display ad     │                     │                  │                │
 │    in UI           │                     │                  │                │
 │                    │                     │                  │                │
```

---

## Adapter Interface Contract

Your adapter MUST implement these methods:

### Initialization
```kotlin
interface Adapter {
    // Called once per app session
    fun initialize(
        context: Context,
        callback: InitializationCompleteCallback,
        serverParameters: MutableList<MediationConfiguration>
    )
}
```

### Banner Ads
```kotlin
interface Adapter {
    // Called for each banner ad request
    fun loadBannerAd(
        adConfiguration: MediationBannerAdConfiguration,
        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
    )
}

interface MediationBannerAd {
    // Called to get the ad view to display
    fun getView(): View
}
```

### Interstitial Ads
```kotlin
interface Adapter {
    // Called to load interstitial
    fun loadInterstitialAd(
        adConfiguration: MediationInterstitialAdConfiguration,
        callback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
    )
}

interface MediationInterstitialAd {
    // Called when app wants to show the ad
    fun showAd(context: Context)
}
```

### Rewarded Ads
```kotlin
interface Adapter {
    // Called to load rewarded ad
    fun loadRewardedAd(
        adConfiguration: MediationRewardedAdConfiguration,
        callback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>
    )
}

interface MediationRewardedAd {
    // Called when app wants to show the ad
    fun showAd(context: Context)
}
```

---

## Callback Contract

You MUST call these callbacks to inform AdMob:

### Load Callbacks
```kotlin
// Success - you got an ad
callback.onSuccess(adapterInstance)

// Failure - no ad available
callback.onFailure(AdError(code, message, domain))
```

### Event Callbacks (during ad display)
```kotlin
// Ad displayed
adCallback.onAdOpened()
adCallback.reportAdImpression()

// User clicked
adCallback.reportAdClicked()

// Ad closed
adCallback.onAdClosed()

// Failed to show
adCallback.onAdFailedToShow(AdError(...))

// User earned reward (rewarded ads only)
adCallback.onUserEarnedReward(rewardItem)
```

---

## Threading Model

### AdMob's Guarantees
- All adapter methods are called on the **main thread (UI thread)**
- All callbacks should be called on the **main thread**

### Your Responsibilities
If your SDK uses background threads:

```kotlin
// ❌ WRONG - callback on background thread
thread {
    val ad = yourSDK.loadAd()
    callback.onSuccess(adapter)  // May crash!
}

// ✓ CORRECT - callback on main thread
thread {
    val ad = yourSDK.loadAd()
    runOnUiThread {
        callback.onSuccess(adapter)  // Safe!
    }
}
```

Or using Handler:
```kotlin
val handler = Handler(Looper.getMainLooper())

thread {
    val ad = yourSDK.loadAd()
    handler.post {
        callback.onSuccess(adapter)
    }
}
```

---

## Error Handling

### When to Call onFailure

```kotlin
// Case 1: Invalid configuration
if (adUnitId.isNullOrEmpty()) {
    callback.onFailure(AdError(100, "Missing ad unit ID", domain))
    return
}

// Case 2: SDK not initialized
if (!YourSDK.isInitialized()) {
    callback.onFailure(AdError(101, "SDK not initialized", domain))
    return
}

// Case 3: No ad available (from your SDK)
override fun onNoFill() {
    callback.onFailure(AdError(3, "No fill", domain))  // 3 = NO_FILL
}

// Case 4: Network error
override fun onNetworkError(error: Exception) {
    callback.onFailure(AdError(2, "Network error: ${error.message}", domain))
}
```

### AdMob Error Codes

```kotlin
companion object {
    const val INTERNAL_ERROR = 0       // Unspecified error
    const val INVALID_REQUEST = 1      // Invalid ad request
    const val NETWORK_ERROR = 2        // Network connectivity issue
    const val NO_FILL = 3              // No ad to show
}
```

---

## Performance Considerations

### Adapter Initialization
- Called **once** per app session
- Should be **fast** (< 1 second ideal)
- Can be **async** (call callback when done)
- Failure here prevents ALL ads from this adapter

### Ad Loading
- Called **frequently** (every ad request)
- Should be **very fast** (< 3 seconds ideal)
- Must be **async** (network request)
- Timeout triggers onFailure and next network is tried

### Memory Management
```kotlin
class CustomBannerAdapter {
    private var bannerView: YourAdView? = null

    // Clean up when ad is destroyed
    fun destroy() {
        bannerView?.destroy()
        bannerView = null
    }
}
```

---

## Testing Architecture

### Local Testing
```kotlin
// Use AdMob test ad unit IDs
"ca-app-pub-3940256099942544/6300978111"  // Test banner

// Your adapter will still be called!
```

### Integration Testing
```
App (Test Mode)
  ↓
AdMob SDK (Test Ads Enabled)
  ↓
Your Adapter (Logs everything)
  ↓
Your SDK (Staging server)
  ↓
Your Test Ad Server
```

### Production Testing
```
App (Release)
  ↓
AdMob SDK
  ↓
Mediation Waterfall
  ├─ Google Ads (70% traffic)
  ├─ Your Adapter (20% traffic) ← Gradual rollout
  └─ Fallback (10% traffic)
```

---

## Deployment Checklist

- [ ] Adapter tested with AdMob test ads
- [ ] All ad formats working (banner, interstitial, rewarded)
- [ ] Callbacks properly implemented (impressions, clicks, closes)
- [ ] Error handling for all failure cases
- [ ] Threading model correct (main thread callbacks)
- [ ] Memory leaks tested and fixed
- [ ] ProGuard rules tested
- [ ] Published to Maven repository
- [ ] Configured in AdMob dashboard
- [ ] Tested with live traffic (small percentage first)
- [ ] Monitoring and analytics set up

---

## Monitoring

### What to Track

**Adapter Level:**
- Initialization success/failure rate
- Ad request count
- Fill rate (successful loads / requests)
- Load time (p50, p95, p99)
- Error rates by error code

**AdMob Dashboard:**
- Impressions
- Clicks
- eCPM
- Revenue
- Fill rate in waterfall

**Your Server:**
- Ad requests received
- Ads served
- Latency
- Error rates

---

## Summary

The adapter architecture is a **translation layer** between two systems:

**AdMob Side:**
- Standard Google interface
- Mediation orchestration
- App integration

**Your Side:**
- Your ad SDK
- Your ad server
- Your business logic

The adapter makes your ad network "speak AdMob" so it can participate in Google's mediation ecosystem!
