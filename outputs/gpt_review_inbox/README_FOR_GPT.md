# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: late_cloud_result_return_after_soft_timeout_fix
- versionName: 4.1.56
- versionCode: 475
- currentOverallResult: EMULATOR_LATE_CLOUD_PASS_LAN_APK_READY
- generatedAt: 2026-07-04T20:05:45+08:00
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: NOT_TESTED_AFTER_FIX
- emulatorLateCloudSmokeResult: PASS

## User Report Being Fixed
- latestPhoneObservedVersion: 4.1.52
- symptom: relay station consumed tokens, but the phone did not receive a cloud result.
- observedCloudAttempted: true
- observedCloudSuccess: false
- observedCloudErrorCode: SOFT_TIMEOUT_PENDING
- observedCloudLatencyMs: 12008
- observedCloudFinalModel: gpt-5.4
- observedDecisionSource: LOCAL_FALLBACK

## Root Cause
- The app used local fallback after the 12s soft timeout.
- Late cloud callbacks were only emitted for success plus non-empty routes.
- Late cloud failure or contract failure could be swallowed silently.
- Emulator reproduced the second blocker: the late cloud result returned but was discarded as FOREGROUND_PACKAGE_CHANGED.

## What Changed
- Late cloud attempts now notify runtime when they complete, including late failure or contract violation.
- Late cloud success after soft timeout can refresh the current panel when the session is still valid.
- Late cloud failure after soft timeout updates the fallback trace instead of disappearing.
- Hidden panel no longer discards an otherwise valid late cloud result.
- Foreground package change no longer discards a late cloud result when session, snapshot, chat package, and window hash still match.
- Session, snapshot, package, window hash, and contamination guards are still preserved.

## Emulator Evidence
- emulatorDetected: true
- emulatorSerial: emulator-5554
- huiyiInstalled: true
- mockchatInstalled: true
- accessibilityEnabled: true
- overlayPermissionGranted: true
- lastOtherInitialResult: SOFT_TIMEOUT_PENDING + LOCAL_FALLBACK + routes=5
- lastOtherLateCloudResult: late_cloud_result_applied + decisionSource=CLOUD + routes=5
- lastMeWaitResult: WAIT + routes=0 + cloudAttempted=false + decisionSource=LOCAL_WAIT
- evidenceDir: outputs/gpt_review_inbox/emulator_cloud_smoke

## GPT Should Inspect
1. outputs/gpt_review_inbox/late-cloud-result-report-for-gpt.md
2. outputs/gpt_review_inbox/late-cloud-result-report.json
3. outputs/gpt_review_inbox/emulator-late-cloud-smoke-report-for-gpt.md
4. outputs/gpt_review_inbox/emulator-late-cloud-smoke-report.json
5. outputs/codex_to_gpt/result-manifest.json
6. outputs/update_server/latest.json

## APK Delivery
- apkDeliveredOutOfBand: true
- apkCommittedToPublicGithub: false
- localLanApkPath: outputs/update_server/huiyi-v4.1.56-debug.apk
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.56-debug.apk
- apkSha256: A48E30DEF6FB77F80CFAF14C2871B81C84494A5B4497B9BCF07211FED458B4B3

## Test Result
- unitTests: PASS
- assembleDebug: PASS
- LateCloudFailureAfterSoftTimeoutStillReportsFallbackToRuntimeTest: PASS
- emulatorLateCloudSmokeResult: PASS
- serverDownloadVerified: true
