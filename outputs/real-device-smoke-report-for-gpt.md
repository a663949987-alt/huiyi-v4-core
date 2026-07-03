# v4.1.4 Real Device Smoke Report

## Basic

- generatedAt: 2026-07-03 09:05:00 +0800
- versionName: 4.1.4
- taskName: Review Freshness + Real Device Smoke Test
- overall_result: NOT_TESTED
- failReason: No physical Android device was connected. `adb devices -l` only showed `emulator-5556`.
- preferredAppPackage: com.bajiao.im.liaoqi
- apiCalled: false

## Device Availability

- physicalDeviceConnected: false
- emulatorConnected: true
- detectedDevices:
  - emulator-5556 device product:sdk_gphone64_x86_64 model:sdk_gphone64_x86_64
- smokeTestPolicy: Real chat App smoke must use a physical device and `real_device_accessibility`. Emulator and MockChat are not counted as real-device smoke.

## Scenario Results

### A. last_me

- result: NOT_TESTED
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- lastEffectiveSpeaker: NOT_TESTED
- decisionType: NOT_TESTED
- routeCount: NOT_TESTED
- apiCalled: false
- resultShownAsOverlay: NOT_TESTED
- mainActivityOpened: NOT_TESTED
- reason: No physical device / real chat window was available.

### B. last_other

- result: NOT_TESTED
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- lastEffectiveSpeaker: NOT_TESTED
- shouldReply: NOT_TESTED
- routeCount: NOT_TESTED
- resultShownAsOverlay: NOT_TESTED
- mainActivityOpened: NOT_TESTED
- reason: No physical device / real chat window was available.

### C. metadata_trap

- result: NOT_TESTED
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- metadataFilteredCount: NOT_TESTED
- filteredMetadataSamples: NOT_TESTED
- LastSpeakerDecisionPollutedByTime: NOT_TESTED
- ContextAssemblerSummaryPollutedByHeaderOrTime: NOT_TESTED
- reason: No physical device / real chat window was available.

## Auto FAIL Conditions

- current round report polluted by historical FAIL: PASS, handled by Review Bundle current/historical separation.
- currentSampleSources contains unknown: PASS, current sample sources exclude historical `unknown`.
- real smoke sample_source is not real_device_accessibility: NOT_TESTED, smoke did not run because no physical device was connected.
- appPackage = local.validation.sample: NOT_TESTED.
- last_me generated routes: NOT_TESTED.
- last_other WAIT unexpectedly: NOT_TESTED.
- timestamp participated in LastSpeakerDecision: NOT_TESTED.
- nickname/online status participated in LastSpeakerDecision: NOT_TESTED.
- result requires switching back to Huiyi App: NOT_TESTED.
- mainActivityOpened=true: NOT_TESTED.

## Conclusion

- realDeviceSmokePass: NOT_TESTED
- shouldContinueBroaderRealDeviceTesting: true
- nextRequiredAction: Connect a physical Android phone, open a real chat window in `com.bajiao.im.liaoqi` or another real chat App, enable Huiyi accessibility and overlay permissions, then run scenarios A/B/C.
