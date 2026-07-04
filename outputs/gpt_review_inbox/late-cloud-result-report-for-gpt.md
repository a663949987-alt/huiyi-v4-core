# Late Cloud Result Report

## Basic Info
- project: Huiyi v4
- taskName: late_cloud_result_return_after_soft_timeout_fix
- versionName: 4.1.56
- versionCode: 475
- generatedAt: 2026-07-04T20:05:45+08:00
- currentOverallResult: EMULATOR_LATE_CLOUD_PASS_LAN_APK_READY
- userNeedsPhoneThisRound: true

## Observed Phone Feedback
- latestPhoneObservedVersion: 4.1.52
- cloudAttempted: true
- cloudSuccess: false
- cloudErrorCode: SOFT_TIMEOUT_PENDING
- cloudContractValidationResult: PENDING
- decisionSource: LOCAL_FALLBACK
- cloudLatencyMs: 12008
- cloudFinalModel: gpt-5.4
- symptom: relay cost was consumed, but phone UI did not receive a cloud result.

## Root Cause
- The cloud request was sent.
- The first response path hit the 12s soft timeout and used local fallback.
- Late cloud callback only fired when the late result was success plus non-empty routes.
- Late cloud failure or contract failure was not surfaced back to runtime.
- Emulator reproduced a late discard with reason FOREGROUND_PACKAGE_CHANGED.

## Fix
- lateCloudSuccessAfterSoftTimeoutCanUpgradePanel: true
- lateCloudFailureAfterSoftTimeoutReportsFallback: true
- lateCloudResultNotDiscardedWhenPanelHidden: true
- lateCloudResultNotDiscardedWhenForegroundPackageChangesButSnapshotMatches: true
- softTimeoutPendingStillUsesLocalFallback: true
- sessionIsolationPreserved: true
- snapshotGuardPreserved: true
- packageAndWindowHashGuardPreserved: true
- contaminationGuardPreserved: true

## Emulator Smoke
- emulatorDetected: true
- emulatorSerial: emulator-5554
- lastOtherInitial: cloudAttempted=true, cloudErrorCode=SOFT_TIMEOUT_PENDING, decisionSource=LOCAL_FALLBACK, routes=5
- lastOtherLateCloud: late_cloud_result_applied, decisionSource=CLOUD, routes=5
- lastMeWait: lastSpeaker=ME, decision=WAIT, routes=0, cloudAttempted=false, decisionSource=LOCAL_WAIT

## Expected Phone Behavior
- If cloud returns within deadline: panel shows cloud routes.
- If cloud exceeds soft timeout: local routes remain visible first.
- If late cloud succeeds: current valid session refreshes to cloud routes.
- If late cloud fails or violates contract: phone report records the late failure/fallback instead of silently dropping it.
- If session changed, snapshot changed, chat package changed, or chat window hash changed: stale cloud result is still discarded.

## Tests
- unit tests: PASS
- assembleDebug: PASS
- LateCloudFailureAfterSoftTimeoutStillReportsFallbackToRuntimeTest: PASS
- emulator late cloud smoke: PASS

## APK / LAN Update
- localLanApkPath: outputs/update_server/huiyi-v4.1.56-debug.apk
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.56-debug.apk
- apkSha256: A48E30DEF6FB77F80CFAF14C2871B81C84494A5B4497B9BCF07211FED458B4B3
- serverDownloadVerified: true

## Phone Test Needed
Yes. User should update to 4.1.56, then test one LAST OTHER chat. If local routes appear first after soft timeout, cloud should refresh when the late result returns.
