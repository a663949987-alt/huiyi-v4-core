# Cloud Soft Timeout Pending Panel Report

## Summary
- taskName: cloud_soft_timeout_pending_panel_fix
- versionName: 4.1.52
- versionCode: 471
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- generatedAt: 2026-07-04T18:45:00+08:00

## Fix
- softTimeoutPendingPanel: true
- softTimeoutPanelTitle: 云端还在分析
- softTimeoutPanelMessage: 中转站还在返回结果，回来后我会自动刷新到这里。
- secondNextTapReusesPendingSession: true
- secondNextTapStartsNewAnalysis: false
- secondNextTapStartsNewApiCall: false
- lateCloudResultCanStillUpgradePanel: true

## Why
The previous phone upload was v4.1.50. It sent the cloud request, then reached `SOFT_TIMEOUT_PENDING` after 12 seconds. If the user tapped 下一句 again, the app could start a new session and make the old cloud response stale. This fix keeps the pending session alive and reopens the same pending cloud panel.

## Validation
- unitTests: PASS
- assembleDebug: PASS
- pendingSessionPolicyTest: PASS
- lanApkReady: true

## Safety
- apiKeyInReports: false
- apkCommittedToPublicGithub: false
- rawPrivateChatUploadedToGithub: false
- autoSend: false

## User Verification
- userNeedsPhoneThisRound: true
- expectedBehavior: 点下一句后如果 12 秒还没云端结果，会显示“云端还在分析”；再次点下一句会重新显示同一个等待面板，不会重新打一次 API。
