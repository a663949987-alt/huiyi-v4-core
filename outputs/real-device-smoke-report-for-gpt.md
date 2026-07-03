# v4.1.5 Real Device Smoke Report

## Basic

- generatedAt: 2026-07-03 10:12:00 +0800
- versionName: 4.1.5
- taskName: Font Scale / Real Device Visual Calibration
- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- failReason: No physical Android device was connected in this Codex environment.
- preferredAppPackage: com.bajiao.im.liaoqi
- apiCalled: false

## Device Availability

- physicalDeviceConnected: false
- emulatorConnected: NOT_USED_FOR_REAL_DEVICE_SMOKE
- smokeTestPolicy: Real chat App smoke must use a physical Android device and `real_device_accessibility`. MockChat and emulator validation do not count as real-device smoke.

## Scenario A: last_me

- result: NOT_TESTED
- scenarioName: last_me
- sample_source: NOT_TESTED
- requiredSampleSource: real_device_accessibility
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED
- actualLastSpeaker: NOT_TESTED
- decisionType: NOT_TESTED
- routeCount: NOT_TESTED
- apiCalled: false
- candidateChatMessageCount: NOT_TESTED
- effectiveMessageCount: NOT_TESTED
- unknownSpeakerCount: NOT_TESTED
- overlayShownInTargetApp: NOT_TESTED
- mainActivityOpened: NOT_TESTED
- reason: No physical device / real chat window was available.

## Font Scale Calibration Status

- detectedRealDeviceFontScale: NOT_TESTED
- mockchatCoversLargeFont: true
- parserAdjustedForFontScale: true
- dynamicBoundsRuleEnabled: true
- naturalLanguageMetadataGuard: true
- disclaimer: 本轮 MockChat 大字体回归通过，但 Real Device Smoke 未执行，不代表真实聊天 App 已通过。

## Required Next Real Device Action

1. Install `outputs/huiyi-v4.1.5-debug.apk` on the physical Android phone.
2. Open the real chat App scenario A where the last effective message is the user's right-side message.
3. Tap Huiyi floating bubble "下一句".
4. Return to Huiyi developer settings and tap "导出真机验收包".
5. Send `Downloads/Huiyi/review/huiyi-v4-review-for-gpt.md` to GPT for acceptance.
