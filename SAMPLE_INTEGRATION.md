# Sample Integration Code

This document shows example code for integrating AdMob with this custom mediation adapter in your Android app.

## App-Level Integration

### 1. Dependencies (app/build.gradle)

```groovy
dependencies {
    // Google Mobile Ads SDK (AdMob)
    implementation 'com.google.android.gms:play-services-ads:22.5.0'

    // Your custom mediation adapter
    implementation 'com.yourcompany.ads:admob-adapter:1.0.0'

    // Other dependencies
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.core:core-ktx:1.12.0'
}
```

### 2. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">

        <!-- AdMob App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 3. MainActivity with Banner Ad

```kotlin
package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*

class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) { initStatus ->
            Log.d(TAG, "AdMob SDK initialized: ${initStatus.adapterStatusMap}")
        }

        // Load banner ad
        setupBannerAd()
    }

    private fun setupBannerAd() {
        // Create AdView
        adView = AdView(this)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test banner ad unit ID
        adView.setAdSize(AdSize.BANNER)

        // Add AdView to layout
        val adContainer = findViewById<FrameLayout>(R.id.ad_container)
        adContainer.addView(adView)

        // Set ad listener
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${error.message}")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner ad clicked")
            }

            override fun onAdOpened() {
                Log.d(TAG, "Banner ad opened")
            }

            override fun onAdClosed() {
                Log.d(TAG, "Banner ad closed")
            }
        }

        // Load ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}
```

### 4. Activity with Interstitial Ad

```kotlin
package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialActivity : AppCompatActivity() {

    private var interstitialAd: InterstitialAd? = null
    private val TAG = "InterstitialActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)

        // Initialize AdMob
        MobileAds.initialize(this)

        // Load interstitial ad
        loadInterstitialAd()

        // Show button
        findViewById<Button>(R.id.btn_show_ad).setOnClickListener {
            showInterstitialAd()
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // Test interstitial ad unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    setupAdCallbacks()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                }
            }
        )
    }

    private fun setupAdCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                loadInterstitialAd() // Load next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Interstitial ad clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial ad impression")
            }
        }
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd?.show(this)
        } else {
            Log.d(TAG, "Interstitial ad not ready")
            loadInterstitialAd()
        }
    }
}
```

### 5. Activity with Rewarded Ad

```kotlin
package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedActivity : AppCompatActivity() {

    private var rewardedAd: RewardedAd? = null
    private var coins = 0
    private val TAG = "RewardedActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewarded)

        // Initialize AdMob
        MobileAds.initialize(this)

        // Load rewarded ad
        loadRewardedAd()

        // Show button
        findViewById<Button>(R.id.btn_show_rewarded).setOnClickListener {
            showRewardedAd()
        }

        updateCoinsDisplay()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917", // Test rewarded ad unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
                    rewardedAd = ad
                    setupAdCallbacks()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                }
            }
        )
    }

    private fun setupAdCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                rewardedAd = null
                loadRewardedAd() // Load next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Rewarded ad clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Rewarded ad impression")
            }
        }
    }

    private fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(this) { reward ->
                // User earned reward
                val rewardAmount = reward.amount
                val rewardType = reward.type
                Log.d(TAG, "User earned reward: $rewardAmount $rewardType")

                coins += rewardAmount
                updateCoinsDisplay()
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready")
            loadRewardedAd()
        }
    }

    private fun updateCoinsDisplay() {
        findViewById<TextView>(R.id.tv_coins).text = "Coins: $coins"
    }
}
```

### 6. Layout Files

**activity_main.xml (Banner Ad):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="My App Content"
        android:padding="16dp"/>

    <!-- Ad Container -->
    <FrameLayout
        android:id="@+id/ad_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"/>

</LinearLayout>
```

**activity_interstitial.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Interstitial Ad Example"
        android:textSize="20sp"
        android:layout_marginBottom="32dp"/>

    <Button
        android:id="@+id/btn_show_ad"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Show Interstitial Ad"/>

</LinearLayout>
```

**activity_rewarded.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">

    <TextView
        android:id="@+id/tv_coins"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Coins: 0"
        android:textSize="24sp"
        android:layout_marginBottom="32dp"/>

    <Button
        android:id="@+id/btn_show_rewarded"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Watch Ad for Reward"/>

</LinearLayout>
```

## Testing with AdMob Test Ads

AdMob provides test ad unit IDs for development:

```kotlin
// Banner
"ca-app-pub-3940256099942544/6300978111"

// Interstitial
"ca-app-pub-3940256099942544/1033173712"

// Rewarded
"ca-app-pub-3940256099942544/5224354917"
```

Always use test ad unit IDs during development to avoid policy violations!

## Production Checklist

Before releasing to production:

- [ ] Replace test ad unit IDs with your production ad unit IDs
- [ ] Configure custom mediation adapter in AdMob dashboard
- [ ] Set appropriate eCPM for your mediation waterfall
- [ ] Test ad loading and display on multiple devices
- [ ] Verify ad callbacks are working correctly
- [ ] Check that your actual ad SDK is integrated (remove simulation code)
- [ ] Test ProGuard/R8 obfuscation
- [ ] Comply with Google's ad policies and GDPR/CCPA requirements

## GDPR Consent (European Users)

If your app serves European users, implement consent:

```kotlin
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

private fun requestConsent() {
    val params = ConsentRequestParameters.Builder().build()

    val consentInformation = UserMessagingPlatform.getConsentInformation(this)

    consentInformation.requestConsentInfoUpdate(
        this,
        params,
        {
            // Consent info updated successfully
            if (consentInformation.isConsentFormAvailable) {
                loadConsentForm()
            }
        },
        { error ->
            // Handle error
            Log.e(TAG, "Consent error: ${error.message}")
        }
    )
}
```

## Additional Resources

- [AdMob Mediation Guide](https://developers.google.com/admob/android/mediate)
- [AdMob Custom Events](https://developers.google.com/admob/android/custom-events)
- [Google Mobile Ads SDK](https://developers.google.com/admob/android/quick-start)
