# Emulator Late Cloud Smoke Report

## Basic Info
- taskName: late_cloud_result_return_after_soft_timeout_fix
- versionName: 4.1.56
- versionCode: 475
- emulatorLateCloudSmokeResult: PASS
- generatedAt: 2026-07-04T20:05:45+08:00

## Environment
- emulatorDetected: true
- emulatorSerial: emulator-5554
- avdName: Huawei_nova_11_API_36
- huiyiInstalled: true
- mockchatInstalled: true
- accessibilityEnabled: true
- overlayPermissionGranted: true
- realDeviceSmokeResult: NOT_TESTED_AFTER_FIX

## LAST OTHER Smoke
- scenario: MockChat last_other / wechat_like
- clickAck: true
- initialResult: PASS
- initialLastSpeaker: OTHER
- initialCloudAttempted: true
- initialCloudErrorCode: SOFT_TIMEOUT_PENDING
- initialDecisionSource: LOCAL_FALLBACK
- initialRouteCount: 5
- lateCloudResult: PASS
- lateCloudAppliedLog: late_cloud_result_applied
- lateDecisionSource: CLOUD
- lateRouteCount: 5
- previousDiscardReproducedBeforeFix: FOREGROUND_PACKAGE_CHANGED

## LAST ME Smoke
- scenario: MockChat last_me / wechat_like
- lastSpeaker: ME
- decision: WAIT
- routeCount: 0
- cloudAttempted: false
- decisionSource: LOCAL_WAIT
- result: PASS

## Evidence Files
- outputs/gpt_review_inbox/emulator_cloud_smoke/v4156_last_other_after_15s_2_logcat.txt
- outputs/gpt_review_inbox/emulator_cloud_smoke/v4156_last_other_after_95s_2_logcat.txt
- outputs/gpt_review_inbox/emulator_cloud_smoke/v4156_current_screen.png
- outputs/gpt_review_inbox/emulator_cloud_smoke/v4156_last_me_wait_final_logcat.txt
- outputs/gpt_review_inbox/emulator_cloud_smoke/v4156_last_me_wait_final.png

## Conclusion
The emulator reproduced the user symptom and verified the fix. A valid late cloud result after soft timeout is now applied instead of being discarded by foreground package drift.
