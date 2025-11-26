# AdMob Mediation Adapter SDK

A custom mediation adapter for integrating your ad network with Google AdMob.

## Overview

This SDK provides a mediation adapter that allows you to serve ads from your ad network through Google AdMob's mediation platform. The adapter supports:

- ✅ Banner Ads
- ✅ Interstitial Ads
- ✅ Rewarded Ads
- ❌ Native Ads (not yet implemented)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.yourcompany.ads:admob-adapter:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.yourcompany.ads:admob-adapter:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.yourcompany.ads</groupId>
    <artifactId>admob-adapter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Integration Guide

### Step 1: Add the Dependency

Add the adapter library to your app's `build.gradle` file as shown above.

### Step 2: Configure Mediation in AdMob

1. Log in to your [AdMob account](https://apps.admob.com/)
2. Navigate to **Mediation** → **Mediation Groups**
3. Select or create a mediation group for your ad unit
4. Click **Add Custom Event**
5. Configure the custom event:

#### Class Name

```
com.yourcompany.ads.admob.CustomMediationAdapter
```

#### Custom Event Parameters

| Parameter Key | Description | Required | Example Value |
|--------------|-------------|----------|---------------|
| parameter | Unique ID for the ad placement | ✓ Yes | /YOUR_APP/banner/... |

**Important:** The parameter key must be exactly `parameter` (all lowercase).

### Step 3: Add ProGuard Rules (if using ProGuard/R8)

If your app uses code obfuscation, add these rules to your `proguard-rules.pro`:

```proguard
# Keep adapter classes
-keep class com.yourcompany.ads.admob.** { *; }
```

The adapter already includes consumer ProGuard rules that will be automatically applied.

## Configuration Examples

### Banner Ad Configuration

**AdMob Custom Event Settings:**
```
Class Name: com.yourcompany.ads.admob.CustomMediationAdapter
Parameter: /YOUR_APP/banner/home_screen
```

### Interstitial Ad Configuration

**AdMob Custom Event Settings:**
```
Class Name: com.yourcompany.ads.admob.CustomMediationAdapter
Parameter: /YOUR_APP/interstitial/level_complete
```

### Rewarded Ad Configuration

**AdMob Custom Event Settings:**
```
Class Name: com.yourcompany.ads.admob.CustomMediationAdapter
Parameter: /YOUR_APP/rewarded/extra_lives
```

## Testing

To test your integration:

1. Set up test ads in your AdMob account
2. Configure the mediation waterfall with your adapter
3. Run your app and trigger ad loads
4. Check logcat for adapter logs:
   ```bash
   adb logcat -s CustomMediationAdapter CustomBannerAdapter CustomInterstitialAdapter CustomRewardedAdapter
   ```

## Supported Ad Formats

| Ad Format | Status | Class |
|-----------|--------|-------|
| Banner | ✅ Supported | `CustomBannerAdapter` |
| Interstitial | ✅ Supported | `CustomInterstitialAdapter` |
| Rewarded | ✅ Supported | `CustomRewardedAdapter` |
| Native | ❌ Not Supported | - |
| Rewarded Interstitial | ❌ Not Supported | - |

## Requirements

- **Minimum SDK:** Android API 21 (Android 5.0)
- **Target SDK:** Android API 34 (Android 14)
- **Google Mobile Ads SDK:** 22.5.0 or higher
- **Kotlin:** 1.9.0 or higher

## Troubleshooting

### Common Issues

#### Issue: "Ad unit ID is missing"
**Solution:** Make sure you've configured the `parameter` field in AdMob custom event settings with your ad unit ID.

#### Issue: Ads not loading
**Solution:**
1. Check that your ad network SDK is properly initialized
2. Verify the ad unit ID is correct
3. Check logcat for error messages
4. Ensure your app has internet permission in `AndroidManifest.xml`

#### Issue: ClassNotFoundException
**Solution:**
1. Verify the adapter dependency is added to your `build.gradle`
2. Check that ProGuard rules are correctly configured
3. Clean and rebuild your project

### Enable Debug Logging

To see detailed logs from the adapter:

```bash
adb shell setprop log.tag.CustomMediationAdapter DEBUG
adb shell setprop log.tag.CustomBannerAdapter DEBUG
adb shell setprop log.tag.CustomInterstitialAdapter DEBUG
adb shell setprop log.tag.CustomRewardedAdapter DEBUG
```

## Development

### Building the Adapter

```bash
./gradlew :admob-adapter:assembleRelease
```

### Publishing to Maven

```bash
./gradlew :admob-adapter:publishReleasePublicationToMavenLocal
```

## Support

Need help? We're here for you.

- **Email:** support@yourcompany.com
- **Documentation:** https://docs.yourcompany.com
- **Issues:** https://github.com/yourcompany/admob-adapter/issues

## License

Copyright 2024 Your Company

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Version History

### 1.0.0 (Initial Release)
- Banner ad support
- Interstitial ad support
- Rewarded ad support
- AdMob mediation integration
