# Visual Diagrams

Visual reference for understanding the AdMob mediation adapter architecture and flow.

## 1. Custom Adapter Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│                      YOUR CUSTOM ADAPTER                         │
│                                                                  │
│              Implements AdMob Adapter API                        │
│                                                                  │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             │ Calls
                             ▼
              ┌──────────────────────────────┐
              │                              │
              │   CustomMediationAdapter     │
              │                              │
              │   • Initialize               │
              │   • Load ads                 │
              │   • Report callbacks         │
              │                              │
              └──────────────┬───────────────┘
                             │
                             │ Calls
                             ▼
              ┌──────────────────────────────┐
              │                              │
              │      YOUR AD SDK             │
              │                              │
              │   • Load ads                 │
              │   • Show ads                 │
              │   • Track events             │
              │                              │
              └──────────────┬───────────────┘
                             │
                             │ HTTP Requests
                             ▼
              ┌──────────────────────────────┐
              │                              │
              │    YOUR AD SERVER            │
              │                              │
              │   • Ad inventory             │
              │   • Targeting                │
              │   • Analytics                │
              │                              │
              └──────────────────────────────┘
```

---

## 2. Complete Integration Stack

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APP LAYER                            │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  App UI Code (Activity/Fragment)                             │  │
│  │                                                               │  │
│  │  • Banner in ScrollView                                      │  │
│  │  • Interstitial after game level                             │  │
│  │  • Rewarded video for premium currency                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           │ AdView.loadAd()
                           │ InterstitialAd.load()
                           │ RewardedAd.load()
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ADMOB SDK LAYER                                │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Google Mobile Ads SDK                                       │  │
│  │                                                               │  │
│  │  • Mediation waterfall management                            │  │
│  │  • Ad network routing                                        │  │
│  │  • Analytics and reporting                                   │  │
│  │  • GDPR/CCPA compliance                                      │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└───────┬──────────────────┬──────────────────┬───────────────────────┘
        │                  │                  │
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐
│   AdMob      │  │   Other      │  │   YOUR CUSTOM            │
│   Network    │  │   Networks   │  │   ADAPTER                │
│  (Google)    │  │  (Unity,     │  │                          │
│              │  │   Meta, etc) │  │  ┌────────────────────┐  │
└──────────────┘  └──────────────┘  │  │ CustomMediation    │  │
                                    │  │ Adapter            │  │
                                    │  │                    │  │
                                    │  │ Implements:        │  │
                                    │  │ • initialize()     │  │
                                    │  │ • loadBannerAd()   │  │
                                    │  │ • loadInterstitial │  │
                                    │  │   Ad()             │  │
                                    │  │ • loadRewardedAd() │  │
                                    │  └─────────┬──────────┘  │
                                    │            │             │
                                    └────────────┼─────────────┘
                                                 │
                                                 │ Adapter calls SDK
                                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      YOUR AD SDK LAYER                              │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Your Ad SDK Library                                         │  │
│  │                                                               │  │
│  │  • Ad loading logic                                          │  │
│  │  • Creative rendering (images, HTML5, video)                 │  │
│  │  • Click tracking                                            │  │
│  │  • Impression tracking                                       │  │
│  │  • Caching and optimization                                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           │ HTTPS POST/GET
                           │ /api/ads/request
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      YOUR AD SERVER LAYER                           │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Ad Server Backend                                           │  │
│  │                                                               │  │
│  │  • Ad inventory management                                   │  │
│  │  • User targeting and segmentation                           │  │
│  │  • Real-time bidding (optional)                              │  │
│  │  • Campaign management                                       │  │
│  │  • Analytics and reporting                                   │  │
│  │  • Fraud detection                                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Mediation Waterfall Flow

```
App Requests Ad
       │
       ▼
┌─────────────────────────────────────────┐
│   AdMob Mediation Waterfall             │
│                                          │
│   Sorted by eCPM (highest to lowest)    │
└─────────────────────────────────────────┘
       │
       ├─► 1. AdMob Network (eCPM: $5.00)
       │      ├─ Request sent
       │      └─ No fill ❌
       │
       ├─► 2. YOUR CUSTOM ADAPTER (eCPM: $3.50)
       │      ├─ Request sent
       │      ├─ YOUR ADAPTER CALLED HERE
       │      │   │
       │      │   ├─ initialize() (if needed)
       │      │   ├─ loadBannerAd()
       │      │   ├─ Your SDK loads ad
       │      │   └─ callback.onSuccess() ✓
       │      │
       │      └─ Ad loaded! SUCCESS! ✅
       │
       │   (Waterfall stops here - ad found)
       │
       ├─► 3. Unity Ads (eCPM: $2.00)
       │      └─ Not called (already got ad)
       │
       └─► 4. Fallback Network (eCPM: $1.00)
              └─ Not called (already got ad)
```

**Key Points:**
- AdMob tries networks in order of eCPM (expected revenue)
- Stops at first successful ad load
- If YOUR ADAPTER fails, moves to next network
- If all fail, app receives `onAdFailedToLoad()`

---

## 4. Adapter Initialization Flow

```
App Starts
    │
    ▼
MobileAds.initialize()
    │
    │ (AdMob SDK downloads mediation config)
    ▼
AdMob discovers YOUR ADAPTER in config
    │
    │ (First ad request happens)
    ▼
┌────────────────────────────────────────┐
│ CustomMediationAdapter.initialize()    │
│                                         │
│  Called ONCE per app session           │
└───────────────┬────────────────────────┘
                │
                ▼
    ┌────────────────────────────┐
    │  Your SDK Initialization   │
    │                            │
    │  YourAdSDK.initialize(     │
    │    context,                │
    │    apiKey,                 │
    │    callback                │
    │  )                         │
    └───────────┬────────────────┘
                │
                ▼
        ┌───────────────┐
        │  Initialize   │
        │  Your SDK     │
        │  Services:    │
        │               │
        │  • Network    │
        │  • Cache      │
        │  • Analytics  │
        └───────┬───────┘
                │
                ▼
    ┌─────────────────────────┐
    │ Callback.onInitSuccess()│
    └───────────┬─────────────┘
                │
                ▼
    ┌──────────────────────────────────┐
    │ AdMob marks adapter as READY     │
    │                                  │
    │ Future ad requests skip init     │
    └──────────────────────────────────┘
```

---

## 5. Banner Ad Complete Flow

```
┌───────────┐
│  App UI   │
└─────┬─────┘
      │
      │ 1. adView.loadAd()
      ▼
┌─────────────────┐
│   AdMob SDK     │
└─────┬───────────┘
      │
      │ 2. Check waterfall
      │ 3. Your adapter's turn
      ▼
┌──────────────────────────────────┐
│  CustomMediationAdapter          │
│                                  │
│  loadBannerAd(config, callback)  │
└────────┬─────────────────────────┘
         │
         │ 4. Extract ad unit ID from config
         │    serverParams.getString("parameter")
         ▼
┌──────────────────────────────────┐
│  CustomBannerAdapter             │
│                                  │
│  loadAd(config, callback)        │
└────────┬─────────────────────────┘
         │
         │ 5. Create banner view
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  bannerView = YourBannerView()   │
│  bannerView.setAdUnitId(id)      │
│  bannerView.setAdListener(...)   │
│  bannerView.loadAd()             │
└────────┬─────────────────────────┘
         │
         │ 6. HTTP Request
         ▼
┌──────────────────────────────────┐
│  Your Ad Server                  │
│                                  │
│  GET /ads?unit=banner_home       │
│  Response: { ad: "...", ... }    │
└────────┬─────────────────────────┘
         │
         │ 7. Ad creative returned
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  • Parse response                │
│  • Load creative into view       │
│  • Trigger onAdLoaded()          │
└────────┬─────────────────────────┘
         │
         │ 8. SDK callback
         ▼
┌──────────────────────────────────┐
│  CustomBannerAdapter             │
│                                  │
│  onAdLoaded() {                  │
│    callback.onSuccess(this)      │
│  }                               │
└────────┬─────────────────────────┘
         │
         │ 9. Success reported
         ▼
┌──────────────────────────────────┐
│  AdMob SDK                       │
│                                  │
│  • Calls adapter.getView()       │
│  • Gets loaded banner view       │
└────────┬─────────────────────────┘
         │
         │ 10. Banner view
         ▼
┌──────────────────────────────────┐
│  App UI                          │
│                                  │
│  • onAdLoaded() called           │
│  • Banner displayed in layout    │
│  • User sees ad! 🎉              │
└──────────────────────────────────┘
```

---

## 6. Interstitial Ad Flow (Two Phases)

### Phase 1: LOAD

```
┌───────────┐
│  App      │
└─────┬─────┘
      │
      │ InterstitialAd.load()
      ▼
┌─────────────────┐
│   AdMob SDK     │
└─────┬───────────┘
      │
      ▼
┌──────────────────────────────────┐
│  CustomInterstitialAdapter       │
│                                  │
│  loadAd(config, callback)        │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  loadInterstitial(adUnitId)      │
└────────┬─────────────────────────┘
         │
         │ HTTP Request
         ▼
┌──────────────────────────────────┐
│  Your Ad Server                  │
│                                  │
│  Returns interstitial creative   │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  • Ad loaded into memory         │
│  • onAdLoaded() callback         │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  CustomInterstitialAdapter       │
│                                  │
│  callback.onSuccess(this)        │
│  interstitialAd = ad             │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  App                             │
│                                  │
│  onAdLoaded(interstitialAd)      │
│  • Stores ad for later           │
│  • Waits for trigger event       │
└──────────────────────────────────┘

   ... Time passes, user completes level ...
```

### Phase 2: SHOW

```
┌──────────────────────────────────┐
│  App                             │
│                                  │
│  User completed level            │
│  interstitialAd.show(context)    │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  AdMob SDK                       │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  CustomInterstitialAdapter       │
│                                  │
│  showAd(context)                 │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  interstitialAd.show(context)    │
│  • Display full screen           │
│  • onAdShown() callback          │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  CustomInterstitialAdapter       │
│                                  │
│  adCallback.onAdOpened()         │
│  adCallback.reportAdImpression() │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  User sees full screen ad        │
│                                  │
│  ... User clicks or closes ...   │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Your Ad SDK                     │
│                                  │
│  onAdClosed() callback           │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  CustomInterstitialAdapter       │
│                                  │
│  adCallback.onAdClosed()         │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  App                             │
│                                  │
│  onAdClosed()                    │
│  • Resume game                   │
│  • Load next interstitial        │
└──────────────────────────────────┘
```

---

## 7. Data Flow - Server Parameters

### Configuration in AdMob Dashboard

```
┌────────────────────────────────────────────┐
│        AdMob Web Dashboard                 │
│                                            │
│  Custom Event Configuration:               │
│                                            │
│  Class Name:                               │
│  ┌──────────────────────────────────────┐ │
│  │ com.yourcompany.ads.admob.           │ │
│  │ CustomMediationAdapter               │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  Parameter (JSON):                         │
│  ┌──────────────────────────────────────┐ │
│  │ {                                    │ │
│  │   "parameter": "/myapp/banner/home"  │ │
│  │ }                                    │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  eCPM: $3.50                               │
│                                            │
└────────────────┬───────────────────────────┘
                 │
                 │ Saved to AdMob servers
                 ▼
┌────────────────────────────────────────────┐
│         AdMob Cloud Services               │
│                                            │
│  Stores mediation configuration            │
└────────────────┬───────────────────────────┘
                 │
                 │ Downloaded at app startup
                 ▼
┌────────────────────────────────────────────┐
│         App Runtime (AdMob SDK)            │
│                                            │
│  Waterfall configuration cached locally    │
└────────────────┬───────────────────────────┘
                 │
                 │ When loading ad
                 ▼
┌────────────────────────────────────────────┐
│    CustomMediationAdapter.loadBannerAd()   │
│                                            │
│    val adUnitId = config.serverParameters  │
│        .getString("parameter")             │
│                                            │
│    // adUnitId = "/myapp/banner/home"      │
│                                            │
│    yourSDK.loadAd(adUnitId)                │
└────────────────────────────────────────────┘
```

---

## 8. Callback Flow

```
Your Ad SDK               Your Adapter           AdMob SDK            App
    │                         │                      │                 │
    │  onAdLoaded()           │                      │                 │
    ├────────────────────────>│                      │                 │
    │                         │                      │                 │
    │                         │  callback.onSuccess()│                 │
    │                         ├─────────────────────>│                 │
    │                         │                      │                 │
    │                         │                      │  onAdLoaded()   │
    │                         │                      ├────────────────>│
    │                         │                      │                 │
    │                         │  getView()           │                 │
    │                         │<─────────────────────┤                 │
    │                         │                      │                 │
    │                         │  return bannerView   │                 │
    │                         ├─────────────────────>│                 │
    │                         │                      │                 │
    │                         │                      │  Display ad     │
    │                         │                      ├────────────────>│
    │                         │                      │                 │
    │  onAdImpression()       │                      │                 │
    ├────────────────────────>│                      │                 │
    │                         │                      │                 │
    │                         │  reportAdImpression()│                 │
    │                         ├─────────────────────>│                 │
    │                         │                      │                 │
    │  onAdClicked()          │                      │                 │
    ├────────────────────────>│                      │                 │
    │                         │                      │                 │
    │                         │  reportAdClicked()   │                 │
    │                         ├─────────────────────>│                 │
    │                         │                      │                 │
    │                         │                      │  onAdClicked()  │
    │                         │                      ├────────────────>│
    │                         │                      │                 │
```

---

## 9. Error Handling Flow

```
┌─────────────────────────────────────────────────────────────┐
│  Your Ad SDK                                                │
│                                                             │
│  Ad request failed                                          │
│  • Network timeout                                          │
│  • No fill from server                                      │
│  • Invalid ad unit ID                                       │
│  • SDK not initialized                                      │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ onAdFailedToLoad(error)
                 ▼
┌─────────────────────────────────────────────────────────────┐
│  CustomBannerAdapter                                        │
│                                                             │
│  onAdFailedToLoad(error: YourAdError) {                     │
│    val adError = AdError(                                   │
│      error.code,                                            │
│      error.message,                                         │
│      "com.yourcompany.ads"                                  │
│    )                                                        │
│    callback.onFailure(adError)  ────────────────────────┐   │
│  }                                                      │   │
└─────────────────────────────────────────────────────────┼───┘
                                                          │
                                                          ▼
┌─────────────────────────────────────────────────────────────┐
│  AdMob SDK                                                  │
│                                                             │
│  Adapter failed, try next network in waterfall             │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ├─► Try Unity Ads
                 ├─► Try Meta Ads
                 └─► Try Fallback Network
                     │
                     │ If all fail
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  App                                                        │
│                                                             │
│  onAdFailedToLoad(error: LoadAdError)                       │
│  • Log error                                                │
│  • Show fallback content                                    │
│  • Retry after delay                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. Threading Model

```
┌─────────────────────────────────────────────────────────────┐
│                     MAIN THREAD (UI Thread)                 │
└─────────────────────────────────────────────────────────────┘
         │                                │
         │ AdMob always calls            │ You must call
         │ adapter on main thread        │ callbacks on main thread
         │                                │
         ▼                                ▼
┌──────────────────────┐        ┌──────────────────────┐
│  CustomMediation     │        │  Adapter callbacks   │
│  Adapter methods:    │        │  back to AdMob:      │
│                      │        │                      │
│  • initialize()      │        │  • onSuccess()       │
│  • loadBannerAd()    │        │  • onFailure()       │
│  • showAd()          │        │  • reportImpression()│
└──────────┬───────────┘        └──────────┬───────────┘
           │                               │
           │ Can use background            │
           │ threads internally            │
           ▼                               │
┌─────────────────────────────────────────┐│
│      BACKGROUND THREAD                  ││
│                                         ││
│  ┌────────────────────────────────────┐ ││
│  │  Your Ad SDK (network calls)       │ ││
│  │                                    │ ││
│  │  • HTTP requests                   │ ││
│  │  • Image decoding                  │ ││
│  │  • Cache operations                │ ││
│  └──────────────┬─────────────────────┘ ││
│                 │                       ││
│                 │ Post back to main     ││
│                 │ thread                ││
│                 └───────────────────────┼┘
│                                         │
└─────────────────────────────────────────┘
                  │
                  ▼
          Handler.post() or
          runOnUiThread()
                  │
                  ▼
┌─────────────────────────────────────────┐
│      MAIN THREAD (UI Thread)            │
│                                         │
│  Callbacks executed here                │
│  • callback.onSuccess()                 │
│  • adCallback.reportImpression()        │
└─────────────────────────────────────────┘
```

---

## Summary

These diagrams show:

1. **Adapter Architecture** - How your adapter sits between AdMob and your SDK
2. **Integration Stack** - All layers from app to ad server
3. **Waterfall Flow** - How mediation tries networks in order
4. **Initialization** - One-time setup process
5. **Banner Flow** - Complete banner ad request lifecycle
6. **Interstitial Flow** - Two-phase load and show process
7. **Data Flow** - How configuration flows from dashboard to code
8. **Callback Flow** - Event reporting between components
9. **Error Handling** - What happens when ads fail
10. **Threading Model** - Main vs background thread usage

Your custom adapter acts as a **bridge** that makes your ad network compatible with AdMob's mediation system!
