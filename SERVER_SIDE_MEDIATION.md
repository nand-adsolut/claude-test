# Server-Side Mediation with Google Demand

Understanding how your ad server can use Google demand as a backend source.

---

## The Question

**"What if my ad-server itself is Google demand - does it work then?"**

## Short Answer

**✅ YES! This is a legitimate and common architecture pattern.**

This is called **server-side mediation** or **server-side header bidding**.

---

## The Key Difference

### ❌ WRONG: Calling AdMob SDK from Adapter (Client-Side Circular Dependency)

```
App
 ↓
AdMob SDK (client-side mediation)
 ↓
YOUR Custom Adapter
 ↓
AdMob SDK again ← CIRCULAR DEPENDENCY! ❌
```

**Problem:** Circular dependency in client code.

### ✅ CORRECT: Your Ad Server Uses Google Demand (Server-Side Integration)

```
App
 ↓
AdMob SDK (client-side mediation)
 ↓
YOUR Custom Adapter
 ↓
YOUR Ad Server (server-side mediation)
 ├─ YOUR ad inventory
 ├─ Partner Network A
 ├─ Partner Network B
 └─ Google Ad Exchange/DV360 ← Google demand on SERVER SIDE ✅
      ↓
     Returns winning ad to adapter
```

**No Problem:** Your server aggregates multiple demand sources on the backend.

---

## How This Works

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT SIDE (Mobile App)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  App → AdMob SDK → Your Custom Adapter                          │
│                          ↓                                      │
│                    HTTP POST /ads/request                       │
│                    {                                            │
│                      "ad_unit": "banner_home",                  │
│                      "size": "320x50",                          │
│                      "user_id": "...",                          │
│                      "device_info": {...}                       │
│                    }                                            │
│                                                                 │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              │ HTTPS
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                 SERVER SIDE (Your Ad Server)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Your Ad Server (Node.js/Java/Python/etc.)             │    │
│  │                                                         │    │
│  │  1. Receives ad request from adapter                   │    │
│  │  2. Runs server-side auction/mediation                 │    │
│  │  3. Calls multiple demand sources in parallel          │    │
│  │  4. Picks highest bid                                  │    │
│  │  5. Returns winning ad                                 │    │
│  └────────────────┬───────────────────────────────────────┘    │
│                   │                                             │
│                   │ Parallel server-to-server calls:            │
│                   │                                             │
│      ┌────────────┼─────────────┬──────────────┬──────────┐    │
│      ▼            ▼             ▼              ▼          ▼    │
│  ┌────────┐  ┌────────┐  ┌──────────┐  ┌──────────┐  ┌─────┐ │
│  │ Your   │  │Partner │  │ Google   │  │ Google   │  │Other│ │
│  │Direct  │  │Network │  │   Ad     │  │ Display  │  │DSPs │ │
│  │  Ads   │  │   A    │  │Exchange  │  │& Video   │  │     │ │
│  │        │  │        │  │  (AdX)   │  │  360     │  │     │ │
│  └────────┘  └────────┘  └──────────┘  └──────────┘  └─────┘ │
│      │            │             │              │          │    │
│      └────────────┴─────────────┴──────────────┴──────────┘    │
│                                   │                             │
│                            Server picks winner                  │
│                          (highest bid wins)                     │
│                                   │                             │
│                                   ▼                             │
│               Returns ad creative + metadata                    │
│               {                                                 │
│                 "ad_markup": "<html>...</html>",                │
│                 "price": 3.50,                                  │
│                 "source": "google_adx"                          │
│               }                                                 │
│                                                                 │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT SIDE (Mobile App)                     │
│                                                                 │
│  Your Custom Adapter receives ad                                │
│   ↓                                                             │
│  Renders ad in app                                              │
│   ↓                                                             │
│  Reports impression back to your server                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Why This Works

### 1. **No Circular Dependency**

Your adapter talks to **your server**, not AdMob SDK:

```kotlin
// Your adapter code
class CustomMediationAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // Call YOUR server (not AdMob SDK)
        yourAdServer.requestAd(
            endpoint = "https://your-ad-server.com/ads/request",
            adUnit = adUnitId,
            callback = { ad ->
                // Your server already picked the best ad
                // (maybe from Google, maybe from elsewhere)
                callback.onSuccess(this)
            }
        )
    }
}
```

### 2. **Server-Side Aggregation**

Your server can call multiple demand sources including Google:

```python
# Your ad server (Python example)
@app.route('/ads/request', methods=['POST'])
def handle_ad_request():
    ad_request = request.json

    # Parallel bidding requests
    bids = []

    # 1. Your direct inventory
    your_bid = check_your_inventory(ad_request)
    if your_bid:
        bids.append(your_bid)

    # 2. Partner networks
    partner_bid = request_from_partner_network(ad_request)
    if partner_bid:
        bids.append(partner_bid)

    # 3. Google Ad Exchange (server-to-server)
    google_bid = request_from_google_adx(ad_request)
    if google_bid:
        bids.append(google_bid)

    # Pick highest bid
    winning_bid = max(bids, key=lambda x: x['price'])

    # Return winning ad
    return jsonify({
        'ad_markup': winning_bid['creative'],
        'price': winning_bid['price'],
        'source': winning_bid['source']
    })
```

### 3. **Google Provides Server-Side APIs**

Google offers server-to-server integrations:

- **Google Ad Exchange (AdX)** - Real-time bidding
- **Display & Video 360 (DV360)** - Programmatic guaranteed
- **Google Ads API** - Direct campaigns
- **Open Bidding** - Server-side header bidding

These are **different from AdMob SDK** - they're server APIs!

---

## Real-World Example: How Ad Mediation Platforms Work

### Companies Using This Architecture

**ironSource (Unity LevelPlay):**
```
App → ironSource SDK → ironSource Server
                            ├─ ironSource demand
                            ├─ Google Ad Exchange
                            ├─ Facebook Audience Network
                            └─ Other DSPs
```

**AppLovin MAX:**
```
App → MAX SDK → AppLovin Server
                    ├─ AppLovin demand
                    ├─ Google bidding
                    ├─ ironSource
                    └─ Other networks
```

**Your Custom Solution (What You're Building):**
```
App → AdMob SDK → Your Adapter → Your Server
                                      ├─ Your inventory
                                      ├─ Google AdX
                                      ├─ Partner networks
                                      └─ Other DSPs
```

---

## Implementation Guide

### Step 1: Set Up Your Ad Server

Your server needs to:
1. Receive ad requests from mobile adapter
2. Make parallel requests to demand sources
3. Run auction logic
4. Return winning ad

**Example using Node.js:**

```javascript
// ad-server.js
const express = require('express');
const app = express();

app.post('/ads/request', async (req, res) => {
    const { ad_unit, size, user_info } = req.body;

    // Parallel requests to demand sources
    const [yourAds, googleAds, partnerAds] = await Promise.all([
        fetchYourInventory(ad_unit),
        fetchGoogleAdExchange(ad_unit, size, user_info),
        fetchPartnerNetwork(ad_unit)
    ]);

    // Combine all bids
    const allBids = [...yourAds, ...googleAds, ...partnerAds];

    // Pick winner (highest CPM)
    const winner = allBids.reduce((max, bid) =>
        bid.cpm > max.cpm ? bid : max
    );

    // Return winning ad
    res.json({
        ad_markup: winner.creative,
        cpm: winner.cpm,
        source: winner.source,
        tracking_urls: winner.tracking
    });
});

// Google Ad Exchange integration
async function fetchGoogleAdExchange(adUnit, size, userInfo) {
    // Server-to-server call to Google AdX
    const response = await fetch('https://googleads.g.doubleclick.net/pagead/ads', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${GOOGLE_ADX_TOKEN}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            // Google AdX bid request format
            ad_unit: adUnit,
            size: size,
            user: userInfo
        })
    });

    const adxResponse = await response.json();

    return adxResponse.bids.map(bid => ({
        creative: bid.ad_markup,
        cpm: bid.price,
        source: 'google_adx'
    }));
}
```

### Step 2: Update Your Mobile Adapter

Your adapter calls your server (not AdMob SDK):

```kotlin
// CustomBannerAdapter.kt
class CustomBannerAdapter : MediationBannerAd {

    private var bannerView: View? = null

    fun loadAd(
        adConfiguration: MediationBannerAdConfiguration,
        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
    ) {
        val context = adConfiguration.context
        val adUnitId = adConfiguration.serverParameters.getString("parameter")
        val adSize = adConfiguration.adSize

        // Call YOUR server (not AdMob SDK!)
        requestAdFromYourServer(
            adUnitId = adUnitId,
            width = adSize.width,
            height = adSize.height,
            onSuccess = { adMarkup, cpm, source ->
                // Create view from returned ad
                bannerView = createViewFromMarkup(context, adMarkup)

                Log.d(TAG, "Ad loaded from source: $source, CPM: $cpm")

                // Tell AdMob we got an ad
                callback.onSuccess(this)
            },
            onFailure = { error ->
                callback.onFailure(AdError(3, error, "com.yourcompany.ads"))
            }
        )
    }

    private fun requestAdFromYourServer(
        adUnitId: String,
        width: Int,
        height: Int,
        onSuccess: (String, Double, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // HTTP request to YOUR server
        val url = "https://your-ad-server.com/ads/request"

        val requestBody = JSONObject().apply {
            put("ad_unit", adUnitId)
            put("size", "${width}x${height}")
            put("device_info", getDeviceInfo())
        }

        // Make HTTP call
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody())
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")

                val adMarkup = json.getString("ad_markup")
                val cpm = json.getDouble("cpm")
                val source = json.getString("source")

                // Source could be "google_adx", "your_inventory", "partner_network", etc.

                Handler(Looper.getMainLooper()).post {
                    onSuccess(adMarkup, cpm, source)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    onFailure(e.message ?: "Network error")
                }
            }
        })
    }

    override fun getView(): View {
        return bannerView ?: View(null)
    }
}
```

### Step 3: Integrate with Google Ad Exchange

To use Google demand on your server, you need:

#### Option A: Google Ad Exchange (AdX)

**Requirements:**
- Google Ad Exchange account (requires approval)
- Publisher ID and network code
- Server-to-server integration

**Integration:**
```python
# Python example for Google AdX
import requests

def fetch_google_adx_bid(ad_request):
    adx_endpoint = "https://googleads.g.doubleclick.net/gampad/ads"

    params = {
        'iu': f'/YOUR_NETWORK_CODE/{ad_request["ad_unit"]}',
        'sz': ad_request['size'],
        'url': ad_request.get('app_url', ''),
        # ... other AdX parameters
    }

    response = requests.get(adx_endpoint, params=params)

    # Parse AdX response
    # Returns ad creative or no-fill
    return parse_adx_response(response)
```

#### Option B: Display & Video 360 (DV360)

**Requirements:**
- DV360 account
- API access credentials
- Programmatic guaranteed deals

#### Option C: Open Bidding

**Requirements:**
- Participate in Google's Open Bidding
- Approved bidder status
- Server-side bidding integration

---

## Comparison: Client-Side vs Server-Side

### Client-Side Circular Call (❌ Wrong)

```kotlin
// In your adapter - WRONG!
class CustomAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // Calling AdMob SDK from within AdMob mediation
        val adView = AdView(context)  // ❌ Circular!
        adView.loadAd(...)
    }
}
```

**Problems:**
- Circular dependency
- Client-side conflict
- AdMob managing AdMob
- Policy violation

### Server-Side Integration (✅ Correct)

```kotlin
// In your adapter - CORRECT!
class CustomAdapter : Adapter() {
    override fun loadBannerAd(...) {
        // Calling YOUR server
        yourServer.requestAd(...)  // ✅ Your server handles Google
    }
}
```

**Benefits:**
- No circular dependency
- Server handles aggregation
- Client sees unified interface
- Proper architecture

---

## Benefits of Server-Side Mediation

### 1. **Unified Auction**

All demand sources compete in real-time:
```
Your Server Auction:
├─ Your inventory:     $4.00
├─ Google AdX:         $3.80  ← Google participating!
├─ Partner Network:    $3.50
└─ Other DSP:          $2.00

Winner: Your inventory ($4.00)
```

### 2. **Better Control**

You control:
- Auction logic
- Floor prices
- Prioritization
- Reporting
- A/B testing

### 3. **Lower SDK Overhead**

App only has one SDK (your adapter), not multiple SDKs:
```
❌ Traditional multi-SDK:
App
 ├─ AdMob SDK (2MB)
 ├─ Unity SDK (3MB)
 ├─ Meta SDK (4MB)
 ├─ ironSource SDK (2MB)
 └─ Your SDK (1MB)
Total: 12MB

✅ Server-side mediation:
App
 └─ Your adapter only (500KB)
Total: 500KB

Server handles all other integrations!
```

### 4. **Faster Updates**

Server-side changes don't require app updates:
```
Change auction logic → Update server → Live immediately
Add new demand source → Update server → No app release needed
```

---

## Requirements for Google Demand Integration

### To Use Google Ad Exchange on Your Server:

1. **Google Ad Manager Account**
   - Sign up for Google Ad Manager
   - Get approved for Ad Exchange

2. **Network Code**
   - Receive your publisher network code
   - Example: `/6355419/travel/europe/france/paris`

3. **API Credentials**
   - Set up service account
   - Get OAuth tokens
   - Configure server authentication

4. **Compliance**
   - Follow Google Ad Exchange policies
   - Implement required bid response formats
   - Handle privacy regulations (GDPR/CCPA)

5. **Technical Integration**
   - Implement AdX bid request format
   - Parse bid responses
   - Handle ad rendering
   - Track impressions/clicks

---

## Example: Complete Flow

### 1. App Requests Ad

```kotlin
// App code (unchanged)
val adView = AdView(context)
adView.adUnitId = "ca-app-pub-XXX/YYY"
adView.loadAd(AdRequest.Builder().build())
```

### 2. AdMob Calls Your Adapter

```kotlin
// Your adapter
override fun loadBannerAd(...) {
    POST https://your-ad-server.com/ads/request
    {
        "ad_unit": "banner_home",
        "size": "320x50"
    }
}
```

### 3. Your Server Runs Auction

```python
# Your server makes parallel requests:

# Request 1: Your inventory
your_bid = check_database_for_direct_ads()
# Result: $4.00 CPM

# Request 2: Google Ad Exchange
google_bid = request_google_adx()
# Result: $3.80 CPM

# Request 3: Partner network
partner_bid = request_partner_network()
# Result: $3.50 CPM

# Pick winner
winner = max([your_bid, google_bid, partner_bid], key=lambda x: x.cpm)
# Winner: your_bid ($4.00)

return winner
```

### 4. Adapter Receives Ad

```kotlin
// Your adapter receives response
{
    "ad_markup": "<html>...</html>",
    "cpm": 4.00,
    "source": "your_inventory"
}

// Render and report success
callback.onSuccess(this)
```

### 5. User Sees Ad

App displays the winning ad (which came from your inventory, but could have been from Google if Google won the auction).

---

## Summary

### ❌ WRONG: Client-Side Circular Call
```
Adapter → AdMob SDK (circular dependency)
```

### ✅ CORRECT: Server-Side Mediation
```
Adapter → Your Server → Google AdX (server-to-server)
                    └→ Other demand sources
```

### The Key Difference

- **Client-side:** Your adapter calls AdMob SDK directly (circular)
- **Server-side:** Your adapter calls your server, which calls Google APIs (not circular)

### This Architecture Works Because:

1. No circular dependency (adapter → server, not adapter → AdMob SDK)
2. Server uses Google's server APIs (AdX, DV360), not AdMob SDK
3. Proper separation: client mediation (AdMob) vs server aggregation (your server)
4. Your server acts as unified demand aggregator
5. Google is just one of many demand sources on your server

---

## Next Steps

If you want to implement this:

1. ✅ Use this adapter SDK (already created)
2. ✅ Build your ad server with server-side mediation
3. ✅ Integrate Google Ad Exchange on your server
4. ✅ Configure AdMob waterfall with your adapter
5. ✅ Your server competes Google demand with other sources

**This is a legitimate and powerful architecture!** Many major ad platforms work this way.
