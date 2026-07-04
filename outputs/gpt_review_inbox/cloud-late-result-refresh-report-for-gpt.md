# Cloud Late Result Dynamic Refresh Report

## Basic Info
- taskName: cloud_late_result_dynamic_refresh
- versionName: 4.1.50
- versionCode: 469
- generatedAt: 2026-07-04T17:54:15+08:00
- result: PASS

## Behavior
- softTimeoutShowsLocalFallback: true
- cloudRequestContinuesAfterSoftTimeout: true
- lateCloudSuccessRefreshesSameSessionPanel: true
- lateCloudRequiresSameSessionId: true
- lateCloudRequiresSameSnapshotId: true
- lateCloudRequiresSameChatPackage: true
- lateCloudRequiresSameChatWindowHash: true
- staleLateCloudDiscarded: true
- contaminatedPanelNotUpgraded: true
- manuallyDismissedPanelNotReopened: true

## User-Facing UX
- pendingStatusText: 云端还在后台等，回来会自动刷新。
- cloudSuccessTitle: 会意云端分析
- localFallbackStillAvailable: true
- lastMeSafetyGatePreserved: true

## Verification
- unitTests: PASS
- assembleDebug: PASS
- apkReadyForLanUpdate: true
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.50-debug.apk

## Notes
The private APK is delivered through LAN update only because it can contain local relay configuration. It is not committed to public GitHub.
