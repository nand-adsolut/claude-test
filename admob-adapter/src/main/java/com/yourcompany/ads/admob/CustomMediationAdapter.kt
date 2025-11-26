package com.yourcompany.ads.admob

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.VersionInfo
import com.google.android.gms.ads.mediation.*

/**
 * Custom Mediation Adapter for AdMob
 *
 * This is the main adapter class that AdMob uses to load and display ads from your ad network.
 * It implements the Mediation Adapter interface and handles initialization and ad loading.
 *
 * To use this adapter in AdMob:
 * 1. Add it as a custom event in your AdMob mediation waterfall
 * 2. Set the class name: com.yourcompany.ads.admob.CustomMediationAdapter
 * 3. Add server parameters with key "parameter" and your ad unit ID as the value
 */
class CustomMediationAdapter : Adapter() {

    companion object {
        private const val TAG = "CustomMediationAdapter"
        private const val ADAPTER_VERSION = "1.0.0"
        private const val SDK_VERSION = "1.0.0"  // Your underlying SDK version
        private const val PARAM_AD_UNIT_ID = "parameter"

        @Volatile
        private var isInitialized = false
    }

    /**
     * Returns the adapter version
     */
    override fun getVersionInfo(): VersionInfo {
        val splits = ADAPTER_VERSION.split("\\.").map { it.toIntOrNull() ?: 0 }
        return VersionInfo(
            splits.getOrElse(0) { 0 },
            splits.getOrElse(1) { 0 },
            splits.getOrElse(2) { 0 }
        )
    }

    /**
     * Returns the underlying SDK version
     */
    override fun getSDKVersionInfo(): VersionInfo {
        val splits = SDK_VERSION.split("\\.").map { it.toIntOrNull() ?: 0 }
        return VersionInfo(
            splits.getOrElse(0) { 0 },
            splits.getOrElse(1) { 0 },
            splits.getOrElse(2) { 0 }
        )
    }

    /**
     * Initializes the adapter and the underlying ad SDK
     */
    override fun initialize(
        context: Context,
        callback: InitializationCompleteCallback,
        serverParameters: MutableList<MediationConfiguration>
    ) {
        if (isInitialized) {
            Log.d(TAG, "Adapter already initialized")
            callback.onInitializationSucceeded()
            return
        }

        Log.d(TAG, "Initializing adapter...")

        synchronized(this) {
            if (isInitialized) {
                callback.onInitializationSucceeded()
                return
            }

            try {
                // TODO: Initialize your ad SDK here
                // YourAdSDK.initialize(context, object : YourInitCallback {
                //     override fun onInitSuccess() {
                //         Log.d(TAG, "SDK initialized successfully")
                //         isInitialized = true
                //         callback.onInitializationSucceeded()
                //     }
                //
                //     override fun onInitFailed(error: YourError) {
                //         Log.e(TAG, "SDK initialization failed: ${error.message}")
                //         callback.onInitializationFailed(error.message)
                //     }
                // })

                // For demonstration, mark as initialized immediately
                isInitialized = true
                Log.d(TAG, "Adapter initialized successfully")
                callback.onInitializationSucceeded()

            } catch (e: Exception) {
                Log.e(TAG, "Exception during initialization", e)
                callback.onInitializationFailed("Exception: ${e.message}")
            }
        }
    }

    /**
     * Loads a banner ad
     */
    override fun loadBannerAd(
        adConfiguration: MediationBannerAdConfiguration,
        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
    ) {
        Log.d(TAG, "Loading banner ad...")

        if (!validateAdConfiguration(adConfiguration.serverParameters, callback)) {
            return
        }

        val bannerAdapter = CustomBannerAdapter()
        bannerAdapter.loadAd(adConfiguration, callback)
    }

    /**
     * Loads an interstitial ad
     */
    override fun loadInterstitialAd(
        adConfiguration: MediationInterstitialAdConfiguration,
        callback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
    ) {
        Log.d(TAG, "Loading interstitial ad...")

        if (!validateAdConfiguration(adConfiguration.serverParameters, callback)) {
            return
        }

        val interstitialAdapter = CustomInterstitialAdapter()
        interstitialAdapter.loadAd(adConfiguration, callback)
    }

    /**
     * Loads a rewarded ad
     */
    override fun loadRewardedAd(
        adConfiguration: MediationRewardedAdConfiguration,
        callback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>
    ) {
        Log.d(TAG, "Loading rewarded ad...")

        if (!validateAdConfiguration(adConfiguration.serverParameters, callback)) {
            return
        }

        val rewardedAdapter = CustomRewardedAdapter()
        rewardedAdapter.loadAd(adConfiguration, callback)
    }

    /**
     * Validates the ad configuration parameters
     */
    private fun <T, U> validateAdConfiguration(
        serverParameters: Bundle,
        callback: MediationAdLoadCallback<T, U>
    ): Boolean {
        val adUnitId = serverParameters.getString(PARAM_AD_UNIT_ID)

        if (TextUtils.isEmpty(adUnitId)) {
            val error = AdError(
                400,
                "Ad unit ID is missing. Please configure the 'parameter' field in AdMob custom event settings.",
                "com.yourcompany.ads"
            )
            Log.e(TAG, error.message)
            callback.onFailure(error)
            return false
        }

        return true
    }

    /**
     * Loads a native ad (optional - implement if you support native ads)
     */
    override fun loadNativeAd(
        adConfiguration: MediationNativeAdConfiguration,
        callback: MediationAdLoadCallback<UnifiedNativeAdMapper, MediationNativeAdCallback>
    ) {
        Log.d(TAG, "Native ads not supported")
        callback.onFailure(
            AdError(401, "Native ads are not supported", "com.yourcompany.ads")
        )
    }

    /**
     * Loads a rewarded interstitial ad (optional)
     */
    override fun loadRewardedInterstitialAd(
        adConfiguration: MediationRewardedAdConfiguration,
        callback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>
    ) {
        Log.d(TAG, "Rewarded interstitial ads not supported")
        callback.onFailure(
            AdError(402, "Rewarded interstitial ads are not supported", "com.yourcompany.ads")
        )
    }
}
