# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: cloud_late_result_dynamic_refresh
- versionName: 4.1.50
- versionCode: 469
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- generatedAt: 2026-07-04T17:54:15+08:00
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: NOT_TESTED_AFTER_FIX

## What Changed
- Cloud relay requests now use a soft UI timeout instead of being abandoned.
- If local fallback is shown first, the original cloud request keeps listening in the background.
- A late cloud success can refresh the same panel only when sessionId, snapshotId, appPackage, and chatWindowHash still match.
- Stale, switched-chat, contaminated, or manually dismissed panels do not get overwritten by late cloud results.
- The panel status now says cloud is still waiting in the background instead of incorrectly saying cloud is unavailable.

## Delivery
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.50-debug.apk
- localApkPath: outputs/update_server/huiyi-v4.1.50-debug.apk
- apkSha256: 0017E1DA90E3EA97382E3CCD41820669D9B1D537E236B8D30C8A69538CEF0028
- apkDeliveredOutOfBand: true
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true

## Tests
- unitTests: PASS (:app:testDebugUnitTest)
- assembleDebug: PASS
- lateCloudSoftTimeoutKeepsListening: PASS
- sameSessionLateCloudRefresh: PASS

## GPT Should Inspect
1. outputs/gpt_review_inbox/cloud-late-result-refresh-report-for-gpt.md
2. outputs/gpt_review_inbox/cloud-late-result-refresh-report.json
3. outputs/codex_to_gpt/result-manifest.json
4. outputs/update_server/latest.json
