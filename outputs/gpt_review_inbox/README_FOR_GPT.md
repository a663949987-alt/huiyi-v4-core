# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: cloud_soft_timeout_pending_panel_fix
- versionName: 4.1.52
- versionCode: 471
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- generatedAt: 2026-07-04T18:45:00+08:00
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: NOT_TESTED_AFTER_FIX

## What Changed
- Added pending cloud session reuse policy.
- If cloud reaches `SOFT_TIMEOUT_PENDING`, the floating panel now shows “云端还在分析”.
- If the user taps “下一句” again while the same cloud request is still pending, Huiyi reopens the existing waiting panel instead of starting a new analysis/API call.
- Late cloud result can still upgrade the same panel when it returns.

## Validation
- unitTests: PASS
- assembleDebug: PASS
- pendingSessionPolicyTest: PASS

## Delivery
- LAN latest.json: http://192.168.31.243:8787/latest.json
- LAN APK: http://192.168.31.243:8787/huiyi-v4.1.52-debug.apk
- local APK path: outputs/update_server/huiyi-v4.1.52-debug.apk
- apkSha256: D78ED8B9DA1E787EA26C58BBE5AD5C98004369FA7B5924A9E0C6B2160CB38FB4

## Safety
- apkCommittedToPublicGithub: false
- apiKeyInReports: false
- rawPrivateChatUploadedToGithub: false
- autoSend: false

## GPT Should Inspect
1. outputs/gpt_review_inbox/cloud-soft-timeout-pending-panel-report-for-gpt.md
2. outputs/gpt_review_inbox/cloud-soft-timeout-pending-panel-report.json
3. outputs/codex_to_gpt/result-manifest.json
