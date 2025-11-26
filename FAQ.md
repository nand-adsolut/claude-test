# Frequently Asked Questions (FAQ)

Common questions about AdMob mediation adapter development.

---

## Can I Call Google Demand (AdMob Ads) from Within My Custom Adapter?

### Short Answer
**No, you should not.** This creates a circular dependency and is not the intended use case.

### Why Not?

#### 1. Circular Dependency Problem
```
App
 ↓
AdMob SDK (managing mediation)
 ↓
YOUR Custom Adapter
 ↓
AdMob SDK (trying to load ads again) ← ❌ CIRCULAR!
```

This creates confusion:
- AdMob is already managing the mediation waterfall
- Your adapter calling AdMob back creates a loop
- AdMob doesn't know it's being called from within itself

#### 2. AdMob Network Already in Waterfall

AdMob mediation waterfall typically looks like:

```
┌─────────────────────────────────────────┐
│   AdMob Mediation Waterfall             │
├─────────────────────────────────────────┤
│ 1. AdMob Network (Google)  $5.00        │ ← Google demand already here!
│ 2. YOUR Custom Adapter     $3.50        │ ← Your adapter
│ 3. Unity Ads               $2.00        │
│ 4. Meta Audience Network   $1.00        │
└─────────────────────────────────────────┘
```

**AdMob Network** (position 1) already serves Google's ad demand:
- Google Ads
- AdSense
- Ad Exchange
- Google's demand partners

**If your adapter also calls Google ads**, you'd have:
- Google ads tried twice (position 1 AND position 2)
- This is redundant and inefficient
- Revenue optimization breaks down

#### 3. Defeats Purpose of Mediation

Custom adapters are meant for **your own ad network** or **third-party networks not supported by AdMob**:

```
✅ CORRECT USE:
App → AdMob → YOUR Adapter → YOUR Ad SDK → YOUR Ad Server

❌ INCORRECT USE:
App → AdMob → YOUR Adapter → AdMob SDK ← Circular!
```

---

## What IS the Custom Adapter For?

### Your Own Ad Network
```
Custom Adapter → Your Ad SDK → Your Ad Server
                              (yourcompany.com/ads)
```

**Example:**
- You run an ad network
- You have your own ad server
- You have your own ad inventory
- You want to compete in AdMob's mediation waterfall

### Third-Party SDK Not Supported by AdMob
```
Custom Adapter → Third Party SDK → Third Party Server
                 (e.g., regional ad network)
```

**Example:**
- Using a regional ad network (e.g., InMobi, AppLovin Custom)
- Using a niche ad network
- Using an enterprise ad solution

---

## What If I Want to Use Google Demand?

You have several **proper** ways to use Google's ad demand:

### Option 1: Use AdMob Network (Already Included)

AdMob automatically includes Google demand:

```kotlin
// In AdMob Dashboard:
// Mediation → Add Ad Source → AdMob Network

// No custom adapter needed!
// AdMob serves Google ads automatically
```

**Includes:**
- Google Ads
- AdSense for Mobile Apps
- Google Ad Exchange (AdX)
- Google demand partners

### Option 2: Use Google Ad Manager (GAM)

If you need advanced features, use GAM instead of AdMob:

```kotlin
// Google Ad Manager SDK (different from AdMob)
dependencies {
    implementation 'com.google.android.gms:play-services-ads:22.5.0'
}

// Load ads via Ad Manager
val adManagerAdView = AdManagerAdView(context)
adManagerAdView.adUnitId = "/networkCode/adUnit"
adManagerAdView.loadAd(AdManagerAdRequest.Builder().build())
```

**When to use GAM:**
- Direct sold campaigns
- Programmatic guaranteed
- Private marketplace deals
- Advanced targeting
- Multiple demand sources in one platform

### Option 3: Bidding Adapter (Advanced)

For real-time bidding with Google:

```kotlin
// Implement MediationAdapter with bidding support
class CustomBiddingAdapter : Adapter(), MediationBannerAd {
    override fun collectSignals(
        configuration: MediationConfiguration,
        callback: SignalCallbacks
    ) {
        // Collect bidding signals
        // Send to your bidding endpoint
        // Your endpoint can participate in Google's auction
    }
}
```

**Use case:**
- Real-time bidding (RTB)
- Header bidding on mobile
- Compete in AdMob's unified auction

---

## Common Scenarios

### Scenario 1: "I want both my ads AND Google ads"

**Solution:** Use mediation waterfall properly

```
AdMob Waterfall:
├─ AdMob Network (Google's demand)     ← AdMob adds this
├─ YOUR Custom Adapter (your demand)   ← You add this
└─ Other networks                      ← Optional
```

**Code:**
```kotlin
// App just uses AdMob API
val adView = AdView(context)
adView.adUnitId = "ca-app-pub-XXX/YYY"
adView.loadAd(AdRequest.Builder().build())

// AdMob handles mediation:
// 1. Tries Google ads first
// 2. If no fill, tries your adapter
// 3. If no fill, tries other networks
```

### Scenario 2: "I want to backfill with Google ads"

**Solution:** Put AdMob Network at the bottom of waterfall

```
AdMob Dashboard Configuration:

Mediation Waterfall:
├─ YOUR Custom Adapter     eCPM: $5.00  ← Try your ads first
├─ Unity Ads               eCPM: $3.00
└─ AdMob Network (Google)  eCPM: $1.00  ← Backfill with Google
```

AdMob tries your network first, falls back to Google if needed.

### Scenario 3: "I want to use Google Bidding in my adapter"

**Solution:** Make your ad server participate in Google's auction

```
Your Architecture:

App
 ↓
AdMob SDK
 ↓
YOUR Bidding Adapter (collects signals)
 ↓
YOUR Ad Server (participates in auction)
 ↓
Google Ad Exchange (via authorized buyer)
```

This requires:
- Authorized Buyer account with Google
- Your ad server integrated with Google's bidding
- Implementing bidding adapter (advanced)

---

## Technical Deep Dive: What Happens If You Try It Anyway?

### If you add AdMob SDK to your adapter:

```kotlin
// ❌ DON'T DO THIS
class CustomMediationAdapter : Adapter() {
    override fun loadBannerAd(
        adConfiguration: MediationBannerAdConfiguration,
        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
    ) {
        // Trying to call AdMob from within the adapter
        val adView = AdView(adConfiguration.context)
        adView.adUnitId = "ca-app-pub-XXX/YYY"

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                callback.onSuccess(/* ... */)  // ❌ Wrong approach
            }
        }

        adView.loadAd(AdRequest.Builder().build())
    }
}
```

### Problems:

1. **Dependency Conflict**
```gradle
// Your adapter already depends on AdMob SDK
dependencies {
    implementation 'com.google.android.gms:play-services-ads:22.5.0'
}

// But you're using it incorrectly (calling it recursively)
```

2. **AdMob Policy Violation**
- AdMob's terms may prohibit this
- Could get your account flagged
- Not a supported use case

3. **Performance Issues**
- Extra network calls
- Increased latency
- Duplicate ad requests to Google
- Wastes user's bandwidth

4. **Reporting Confusion**
```
AdMob Dashboard shows:
├─ AdMob Network: 1000 requests, 700 impressions
└─ Your Adapter:  300 requests, 200 impressions
                  (but these are ALSO Google ads!)

Your actual fill rate and eCPM calculations become meaningless
```

5. **Waterfall Breaks**
```
AdMob tries to optimize:
- Tries network with highest eCPM first
- If your adapter calls Google, and Google is also in waterfall,
  Google could be tried twice with different eCPMs
- Revenue optimization fails
```

---

## What Should You Put in Your Custom Adapter?

### ✅ Your Own Ad Network

```kotlin
class CustomMediationAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // Call YOUR ad SDK
        val bannerView = YourAdSDK.createBanner(context)
        bannerView.load(adUnitId)

        // Loads from YOUR ad server
        // URL: https://your-ad-server.com/ads
    }
}
```

### ✅ Third-Party Network SDK

```kotlin
class CustomMediationAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // Call third-party SDK (not already in AdMob)
        val bannerView = ThirdPartySDK.createBanner(context)
        bannerView.load(adUnitId)

        // Loads from third-party server
        // URL: https://thirdparty.com/ads
    }
}
```

### ❌ AdMob/Google Ads

```kotlin
class CustomMediationAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // ❌ DON'T DO THIS
        val adView = AdView(context)  // Calling AdMob from within AdMob!
        adView.loadAd(...)
    }
}
```

---

## Proper Architecture

### What AdMob Expects:

```
┌─────────────────────────────────────────────────────────────┐
│                    AdMob Mediation                          │
│                                                             │
│  Manages multiple ad sources:                               │
│  ├─ Google's own ads (AdMob Network)                        │
│  ├─ Supported networks (Unity, Meta, etc.)                  │
│  └─ Custom adapters (YOUR ad network)                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                                │
                                ├──────────────────┬──────────────────┬─────────────
                                ▼                  ▼                  ▼
                    ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
                    │  AdMob Network  │  │  Unity Ads      │  │  YOUR Adapter   │
                    │  (Google's ads) │  │  (Unity's ads)  │  │  (Your ads)     │
                    └────────┬────────┘  └────────┬────────┘  └────────┬────────┘
                             │                    │                     │
                             ▼                    ▼                     ▼
                    ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
                    │  Google Ad      │  │  Unity Ad       │  │  YOUR Ad        │
                    │  Server         │  │  Server         │  │  Server         │
                    └─────────────────┘  └─────────────────┘  └─────────────────┘
```

Each adapter connects to a **different** ad server!

---

## Exception: Server-Side Mediation

There's ONE case where you might involve Google demand in your adapter:

### Server-Side Mediation (Advanced)

```
App
 ↓
AdMob SDK
 ↓
YOUR Custom Adapter
 ↓
YOUR Ad Server (server-side mediation)
 ├─ Calls YOUR ad inventory
 ├─ Calls Partner Network A
 ├─ Calls Partner Network B
 └─ Calls Google Ad Exchange (via authorized buyer)
      ↓
     Returns highest paying ad
```

**Requirements:**
- You operate an ad server with server-side mediation
- You're an authorized Google Ad Exchange buyer
- Your server makes server-to-server calls (not client SDK)
- You have proper agreements and technical integration

This is **not the same** as calling AdMob SDK from your adapter!

---

## Summary

### ❌ Don't Do:
- Call AdMob SDK from within your custom adapter
- Try to serve Google ads through your custom adapter
- Create circular dependencies

### ✅ Do Instead:
- Use AdMob Network in the waterfall for Google demand
- Use your custom adapter for YOUR ad network only
- Let AdMob manage mediation between networks
- Use Google Ad Manager (GAM) for advanced Google features
- Implement server-side mediation if you need complex setups

### The Rule:
**One adapter = One ad source**

Your custom adapter should connect to **your** ad infrastructure, not back to Google's!

---

## Need Help?

If you want to:
- Serve your own ads → Custom adapter ✓ (this SDK)
- Serve Google ads → AdMob Network ✓ (built-in)
- Advanced Google features → Google Ad Manager ✓
- Real-time bidding → Bidding adapter ✓ (advanced)
- Multiple demand sources → Server-side mediation ✓ (advanced)

For questions about Google Ad Manager or bidding adapters, check:
- [Google Ad Manager Documentation](https://developers.google.com/ad-manager/mobile-ads-sdk)
- [AdMob Bidding Guide](https://developers.google.com/admob/android/bidding)
