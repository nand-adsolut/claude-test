# AdMob Mediation Request Flow

Complete explanation of how ad requests flow from the client app through AdMob mediation to your custom adapter.

## Table of Contents
1. [High-Level Overview](#high-level-overview)
2. [Detailed Flow Diagrams](#detailed-flow-diagrams)
3. [Step-by-Step Explanation](#step-by-step-explanation)
4. [Code Examples with Flow](#code-examples-with-flow)

---

## High-Level Overview

```
┌─────────────────┐
│   Client App    │  1. App requests ad
│  (Your Game/   │  ───────────────────┐
│     App)        │                     │
└─────────────────┘                     │
                                        ▼
┌─────────────────────────────────────────────────────────┐
│              AdMob SDK (Google)                         │
│  2. AdMob checks mediation waterfall                    │
│  3. Tries ad networks in eCPM order                     │
└─────────────────────────────────────────────────────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
            ┌─────────────┐     ┌─────────────┐    ┌─────────────┐
            │  AdMob Ads  │     │   Network   │    │ YOUR CUSTOM │
            │  (Google)   │     │      A      │    │   ADAPTER   │
            └─────────────┘     └─────────────┘    └─────────────┘
                                                            │
                                                            ▼
                                                    ┌─────────────┐
                                                    │  Your Ad    │
                                                    │    SDK      │
                                                    └─────────────┘
```

---

## Detailed Flow Diagrams

### 1. Banner Ad Request Flow

```
┌──────────┐          ┌──────────┐          ┌──────────────┐          ┌──────────────┐          ┌──────────┐
│   App    │          │  AdMob   │          │   Custom     │          │   Custom     │          │ Your Ad  │
│          │          │   SDK    │          │  Mediation   │          │   Banner     │          │   SDK    │
│          │          │          │          │   Adapter    │          │   Adapter    │          │          │
└────┬─────┘          └────┬─────┘          └──────┬───────┘          └──────┬───────┘          └────┬─────┘
     │                     │                       │                         │                       │
     │ 1. Load Banner Ad   │                       │                         │                       │
     │ adView.loadAd()     │                       │                         │                       │
     ├────────────────────>│                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 2. Check Mediation    │                         │                       │
     │                     │    Waterfall          │                         │                       │
     │                     ├───────┐               │                         │                       │
     │                     │       │               │                         │                       │
     │                     │<──────┘               │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 3. Initialize Adapter │                         │                       │
     │                     │    (if not initialized)                         │                       │
     │                     ├──────────────────────>│                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │ 4. initialize()         │                       │
     │                     │                       ├─────────┐               │                       │
     │                     │                       │         │               │                       │
     │                     │                       │ 5. Init Your SDK        │                       │
     │                     │                       ├─────────────────────────┼──────────────────────>│
     │                     │                       │         │               │                       │
     │                     │                       │<────────┼───────────────┼───────────────────────┤
     │                     │                       │         │               │       Success         │
     │                     │                       │<────────┘               │                       │
     │                     │                       │                         │                       │
     │                     │<──────────────────────┤                         │                       │
     │                     │   Initialized         │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 6. loadBannerAd()     │                         │                       │
     │                     ├──────────────────────>│                         │                       │
     │                     │   + config params     │                         │                       │
     │                     │   + callback          │                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │ 7. Create Banner        │                       │
     │                     │                       │    Adapter Instance     │                       │
     │                     │                       ├────────────────────────>│                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 8. Load Ad from SDK   │
     │                     │                       │                         ├──────────────────────>│
     │                     │                       │                         │   (ad unit ID,        │
     │                     │                       │                         │    size, etc.)        │
     │                     │                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 9. onAdLoaded()       │
     │                     │                       │                         │<──────────────────────┤
     │                     │                       │                         │                       │
     │                     │                       │ 10. callback.onSuccess()│                       │
     │                     │                       │<────────────────────────┤                       │
     │                     │                       │     + banner view       │                       │
     │                     │                       │                         │                       │
     │                     │<──────────────────────┤                         │                       │
     │                     │   Ad Loaded           │                         │                       │
     │                     │   + View              │                         │                       │
     │                     │                       │                         │                       │
     │ 11. onAdLoaded()    │                       │                         │                       │
     │<────────────────────┤                       │                         │                       │
     │                     │                       │                         │                       │
     │ 12. Display Banner  │                       │                         │                       │
     │     in Layout       │                       │                         │                       │
     ├─────────┐           │                       │                         │                       │
     │         │           │                       │                         │                       │
     │<────────┘           │                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │ 13. User Sees Ad        │                       │
     │                     │                       │                         │ 14. reportImpression()│
     │                     │                       │<────────────────────────┼───────────────────────┤
     │                     │                       │                         │                       │
     │                     │<──────────────────────┤                         │                       │
     │                     │  Impression reported  │                         │                       │
     │                     │                       │                         │                       │
```

### 2. Interstitial Ad Request Flow

```
┌──────────┐          ┌──────────┐          ┌──────────────┐          ┌──────────────┐          ┌──────────┐
│   App    │          │  AdMob   │          │   Custom     │          │ Interstitial │          │ Your Ad  │
│          │          │   SDK    │          │   Adapter    │          │   Adapter    │          │   SDK    │
└────┬─────┘          └────┬─────┘          └──────┬───────┘          └──────┬───────┘          └────┬─────┘
     │                     │                       │                         │                       │
     │ PHASE 1: LOADING    │                       │                         │                       │
     │═════════════════════│                       │                         │                       │
     │ 1. Load Interstitial│                       │                         │                       │
     │ InterstitialAd.load()                       │                         │                       │
     ├────────────────────>│                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 2. Mediation Check    │                         │                       │
     │                     │    Your adapter turn  │                         │                       │
     │                     ├───────┐               │                         │                       │
     │                     │<──────┘               │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 3. loadInterstitialAd()                         │                       │
     │                     ├──────────────────────>│                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │ 4. Create Instance      │                       │
     │                     │                       ├────────────────────────>│                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 5. Load from Your SDK │
     │                     │                       │                         ├──────────────────────>│
     │                     │                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 6. onAdLoaded()       │
     │                     │                       │                         │<──────────────────────┤
     │                     │                       │                         │   + ad object         │
     │                     │                       │                         │                       │
     │                     │                       │ 7. callback.onSuccess() │                       │
     │                     │                       │<────────────────────────┤                       │
     │                     │                       │                         │                       │
     │                     │<──────────────────────┤                         │                       │
     │                     │   Ad Ready            │                         │                       │
     │                     │                       │                         │                       │
     │ 8. onAdLoaded()     │                       │                         │                       │
     │<────────────────────┤                       │                         │                       │
     │  (Ad ready to show) │                       │                         │                       │
     │                     │                       │                         │                       │
     │ App stores ad       │                       │                         │                       │
     │ object for later    │                       │                         │                       │
     ├─────────┐           │                       │                         │                       │
     │<────────┘           │                       │                         │                       │
     │                     │                       │                         │                       │
     │ ... Time passes ... │                       │                         │                       │
     │ ... User completes  │                       │                         │                       │
     │ ... a level ...     │                       │                         │                       │
     │                     │                       │                         │                       │
     │ PHASE 2: SHOWING    │                       │                         │                       │
     │═════════════════════│                       │                         │                       │
     │ 9. Show Interstitial│                       │                         │                       │
     │ interstitialAd.show()                       │                         │                       │
     ├────────────────────>│                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │ 10. showAd(context)   │                         │                       │
     │                     ├──────────────────────>│                         │                       │
     │                     │                       ├────────────────────────>│                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 11. Show via Your SDK │
     │                     │                       │                         ├──────────────────────>│
     │                     │                       │                         │                       │
     │                     │                       │                         │                       │
     │                     │                       │                         │ 12. onAdShown()       │
     │                     │                       │                         │<──────────────────────┤
     │                     │                       │                         │                       │
     │                     │                       │ 13. onAdOpened()        │                       │
     │                     │                       │<────────────────────────┤                       │
     │                     │                       │     reportImpression()  │                       │
     │                     │                       │                         │                       │
     │                     │<──────────────────────┤                         │                       │
     │ 14. onAdShowed()    │                       │                         │                       │
     │<────────────────────┤                       │                         │                       │
     │                     │                       │                         │                       │
     │ USER SEES FULL      │                       │                         │                       │
     │ SCREEN AD           │                       │                         │                       │
     │                     │                       │                         │                       │
     │ ... User clicks ... │                       │                         │                       │
     │                     │                       │                         │ 15. onAdClicked()     │
     │                     │                       │                         │<──────────────────────┤
     │                     │                       │ 16. reportAdClicked()   │                       │
     │                     │                       │<────────────────────────┤                       │
     │                     │<──────────────────────┤                         │                       │
     │ 17. onAdClicked()   │                       │                         │                       │
     │<────────────────────┤                       │                         │                       │
     │                     │                       │                         │                       │
     │ ... User closes ... │                       │                         │                       │
     │                     │                       │                         │ 18. onAdClosed()      │
     │                     │                       │                         │<──────────────────────┤
     │                     │                       │ 19. onAdClosed()        │                       │
     │                     │                       │<────────────────────────┤                       │
     │                     │<──────────────────────┤                         │                       │
     │ 20. onAdClosed()    │                       │                         │                       │
     │<────────────────────┤                       │                         │                       │
     │                     │                       │                         │                       │
     │ App resumes         │                       │                         │                       │
     │ normal flow         │                       │                         │                       │
     │                     │                       │                         │                       │
```

---

## Step-by-Step Explanation

### Phase 1: App Initialization

#### Step 1: App Starts
```kotlin
// In your app's Application class or MainActivity
MobileAds.initialize(this) { initStatus ->
    Log.d(TAG, "AdMob initialized")
}
```

**What happens:**
- AdMob SDK initializes
- Reads mediation configuration from AdMob servers
- Downloads waterfall configuration (which ad networks to try and in what order)
- Your custom adapter is NOT initialized yet

---

### Phase 2: Ad Request Initiated

#### Step 2: App Requests an Ad
```kotlin
// App code - Banner example
val adView = AdView(this)
adView.adUnitId = "ca-app-pub-XXXXX/YYYYY"  // Your AdMob ad unit
adView.setAdSize(AdSize.BANNER)

val adRequest = AdRequest.Builder().build()
adView.loadAd(adRequest)  // ← THIS TRIGGERS THE FLOW
```

**What happens:**
- AdMob SDK receives the ad request
- AdMob contacts its servers with:
  - Ad unit ID
  - Device info (for targeting)
  - User consent info (GDPR/CCPA)
  - App info

---

### Phase 3: AdMob Mediation Waterfall

#### Step 3: AdMob Checks Mediation Configuration

AdMob server responds with a **waterfall** (priority list of ad networks):

```
┌─────────────────────────────────────┐
│    AdMob Mediation Waterfall        │
├─────────────────────────────────────┤
│ 1. AdMob (Google) - eCPM: $5.00     │  ← Try first (highest eCPM)
│ 2. YOUR ADAPTER   - eCPM: $3.50     │  ← Try if #1 fails
│ 3. Network A      - eCPM: $2.00     │  ← Try if #2 fails
│ 4. Network B      - eCPM: $1.00     │  ← Fallback
└─────────────────────────────────────┘
```

**What happens:**
- AdMob tries to get an ad from Google's own network first
- If Google has no ad (no fill), AdMob moves to the next network
- When it's YOUR ADAPTER's turn, the adapter gets called

---

### Phase 4: Adapter Initialization

#### Step 4: First Time - Initialize Your Adapter

```kotlin
// CustomMediationAdapter.kt - This gets called first time only
override fun initialize(
    context: Context,
    callback: InitializationCompleteCallback,
    serverParameters: MutableList<MediationConfiguration>
) {
    // Your code here: Initialize your ad SDK
    YourAdSDK.initialize(context, object : YourInitCallback {
        override fun onInitSuccess() {
            isInitialized = true
            callback.onInitializationSucceeded()  // ← Tell AdMob we're ready
        }
    })
}
```

**What happens:**
- AdMob calls `initialize()` on your adapter (only once per app session)
- Your adapter initializes your underlying ad SDK
- When init completes, you call `callback.onInitializationSucceeded()`
- AdMob proceeds to load the ad

**Note:** Subsequent ad requests skip this step (already initialized)

---

### Phase 5: Ad Loading

#### Step 5: AdMob Calls Your Adapter to Load Ad

```kotlin
// CustomMediationAdapter.kt
override fun loadBannerAd(
    adConfiguration: MediationBannerAdConfiguration,
    callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
) {
    // Extract parameters from AdMob
    val serverParameters = adConfiguration.serverParameters
    val adUnitId = serverParameters.getString("parameter")  // Your ad unit ID
    val adSize = adConfiguration.adSize                      // Size (320x50, etc.)
    val context = adConfiguration.context

    // Create banner adapter and load
    val bannerAdapter = CustomBannerAdapter()
    bannerAdapter.loadAd(adConfiguration, callback)  // ← Continues to next step
}
```

**What AdMob provides:**
- `context`: Android context
- `serverParameters`: Contains your "parameter" from AdMob dashboard config
- `adSize`: Requested ad size
- `callback`: Where you report success/failure

---

#### Step 6: Your Adapter Loads from Your Ad SDK

```kotlin
// CustomBannerAdapter.kt
private fun loadYourBannerAd(...) {
    // Create your ad view
    bannerView = YourBannerAdView(context)
    bannerView?.setAdUnitId(adUnitId)  // From server parameters
    bannerView?.setAdSize(adSize.width, adSize.height)

    // Set callbacks
    bannerView?.setAdListener(object : YourAdListener {
        override fun onAdLoaded() {
            // SUCCESS! Tell AdMob
            bannerAdCallback = callback.onSuccess(this@CustomBannerAdapter)
        }

        override fun onAdFailedToLoad(error: YourAdError) {
            // FAILURE! Tell AdMob to try next network
            callback.onFailure(AdError(error.code, error.message, "..."))
        }
    })

    // Request ad from your SDK
    bannerView?.loadAd()  // ← Your SDK makes HTTP request to your ad server
}
```

**What happens:**
- Your adapter creates an ad view using your SDK
- Your SDK makes an HTTP request to your ad server
- Your ad server returns ad creative (image, HTML, video, etc.)
- Your SDK loads the creative into the ad view

---

### Phase 6: Success/Failure Reporting

#### Scenario A: Ad Loaded Successfully

```kotlin
// Your SDK calls this callback
override fun onAdLoaded() {
    // Tell AdMob we got an ad
    bannerAdCallback = callback.onSuccess(this@CustomBannerAdapter)
    //                           ↑
    //                This passes the adapter instance back to AdMob
}
```

**What happens:**
- AdMob calls `getView()` on your adapter to get the ad view
- AdMob adds the view to the app's layout
- User sees your ad! 🎉

```kotlin
override fun getView(): View {
    return bannerView  // Return the loaded ad view
}
```

#### Scenario B: Ad Failed to Load

```kotlin
override fun onAdFailedToLoad(error: YourAdError) {
    // Tell AdMob we failed
    callback.onFailure(AdError(
        error.code,
        error.message,
        "com.yourcompany.ads"
    ))
}
```

**What happens:**
- AdMob marks your adapter as "no fill"
- AdMob tries the NEXT network in the waterfall
- If all networks fail, app gets `onAdFailedToLoad()`

---

### Phase 7: Ad Display and Callbacks

#### User Sees Ad (Impression)

```kotlin
// Your SDK detects ad is visible
override fun onAdImpression() {
    // Tell AdMob about the impression
    bannerAdCallback?.reportAdImpression()
}
```

**What happens:**
- AdMob records the impression for reporting
- Revenue tracking starts
- AdMob reports this to its analytics

---

#### User Clicks Ad

```kotlin
// Your SDK detects click
override fun onAdClicked() {
    // Tell AdMob about the click
    bannerAdCallback?.reportAdClicked()
    bannerAdCallback?.onAdOpened()  // If it opens a browser
}
```

**What happens:**
- AdMob records the click
- Browser opens (if applicable)
- App gets `onAdClicked()` callback
- App can pause music, game, etc.

---

## Code Examples with Flow

### Example 1: Complete Banner Flow in App

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // STEP 1: App requests ad
        Log.d(TAG, "→ App requesting banner ad")

        adView = AdView(this)
        adView.adUnitId = "ca-app-pub-XXX/YYY"
        adView.setAdSize(AdSize.BANNER)

        adView.adListener = object : AdListener() {
            // STEP 8: Ad loaded successfully (from your adapter)
            override fun onAdLoaded() {
                Log.d(TAG, "← App received ad (loaded via your adapter!)")
            }

            // OR: All networks failed
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "← No ad available from any network")
            }

            // STEP 9: User clicked ad
            override fun onAdClicked() {
                Log.d(TAG, "← User clicked ad from your network")
            }
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        // ↓
        // This triggers the entire mediation flow:
        // AdMob → Waterfall → Your Adapter → Your SDK → Your Ad Server
    }
}
```

---

### Example 2: Interstitial Flow

```kotlin
class GameActivity : AppCompatActivity() {

    private var interstitialAd: InterstitialAd? = null

    fun loadInterstitial() {
        // PHASE 1: LOAD
        Log.d(TAG, "→ Loading interstitial")

        InterstitialAd.load(
            this,
            "ca-app-pub-XXX/YYY",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "← Interstitial loaded (via your adapter)")
                    interstitialAd = ad
                    setupCallbacks()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "← Load failed: ${error.message}")
                }
            }
        )
        // ↓
        // AdMob → Mediation → Your Adapter → Load → Store ad object
        // Ad is NOT shown yet, just loaded into memory
    }

    fun onLevelComplete() {
        // PHASE 2: SHOW (later, when game level completes)
        Log.d(TAG, "→ Showing interstitial")

        interstitialAd?.show(this)
        // ↓
        // AdMob → Your Adapter.showAd() → Your SDK.show() → Full screen ad
    }

    private fun setupCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "← Ad shown full screen")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "← User closed ad, resume game")
                resumeGame()
            }

            override fun onAdClicked() {
                Log.d(TAG, "← User clicked ad")
            }
        }
    }
}
```

---

## Key Concepts

### 1. **Waterfall Mediation**
AdMob tries ad networks in order of eCPM (revenue) until one succeeds:
```
Try Network 1 (eCPM $5.00) → No fill
Try Network 2 (eCPM $3.50) → No fill
Try YOUR NETWORK (eCPM $3.00) → SUCCESS! ✓
(Stop here, don't try remaining networks)
```

### 2. **Lazy Initialization**
Your adapter's `initialize()` is called only once per app session, not for every ad request.

### 3. **Load vs Show**
- **Banner:** Load and show are combined (shown immediately when loaded)
- **Interstitial/Rewarded:** Load first, show later (two separate phases)

### 4. **Callbacks are Critical**
You MUST call either:
- `callback.onSuccess(adapter)` - Ad loaded successfully
- `callback.onFailure(error)` - Failed, try next network

If you don't call either, AdMob will hang waiting for your adapter!

### 5. **Server Parameters**
AdMob passes your configuration via `serverParameters` bundle:
```json
{
  "parameter": "/myapp/banner/home"
}
```
This is configured in AdMob dashboard for each custom event.

---

## Troubleshooting Flow Issues

### Issue: Adapter never gets called

**Check:**
1. Is your adapter in the mediation waterfall?
2. Is its eCPM high enough to be reached?
3. Are networks above it returning ads (stopping the waterfall)?

**Debug:**
```kotlin
Log.d(TAG, "Adapter initialize() called")  // Should see this once
Log.d(TAG, "loadBannerAd() called")        // Should see this for each request
```

### Issue: Ads don't show even though adapter is called

**Check:**
1. Are you calling `callback.onSuccess()` or `callback.onFailure()`?
2. Is `getView()` returning a valid View?
3. Is your SDK actually loading ads?

**Debug:**
```kotlin
Log.d(TAG, "Requesting ad from SDK for: $adUnitId")
Log.d(TAG, "SDK returned success: $success")
Log.d(TAG, "Calling callback.onSuccess()")
```

---

## Summary

The complete flow in simple terms:

1. **App asks AdMob** for an ad
2. **AdMob checks its waterfall** (list of ad networks sorted by revenue)
3. **AdMob tries networks one by one** until one succeeds
4. **When it's your turn**, AdMob calls your `CustomMediationAdapter`
5. **Your adapter** calls your ad SDK to load an ad
6. **Your SDK** fetches the ad from your ad server
7. **Your adapter tells AdMob** success or failure
8. **If successful**, AdMob displays your ad in the app
9. **User interactions** (impressions, clicks) flow back through your adapter to AdMob

The adapter is essentially a **translator** between AdMob's interface and your ad SDK's interface!
