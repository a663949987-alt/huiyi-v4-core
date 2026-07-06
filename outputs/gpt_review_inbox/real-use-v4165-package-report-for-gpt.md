# Real Use v4.1.65 Package Report

## Basic Info

- taskName: real_use_v4165_package_delivery
- versionName: 4.1.65
- versionCode: 484
- generatedAt: 2026-07-06T10:09:16+08:00
- currentOverallResult: READY_FOR_PHONE_TEST_PRIVATE_LAN_APK
- userNeedsPhoneThisRound: true

## Package

- apkGenerated: true
- apkPath: outputs/update_server/huiyi-v4.1.65-debug.apk
- apkSizeBytes: 31547567
- apkSha256: E9339A2B8E1C7B0B15DF33E4EEDA25A7E403EF4707F1D222066CCBFD61BE4139
- apkDeliveredOutOfBand: true
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- lanUpdateAvailable: true
- latestJsonUpdated: true
- latestJsonPath: outputs/update_server/latest.json
- latestJsonVersionName: 4.1.65
- latestJsonVersionCode: 484
- latestJsonApkUrl: huiyi-v4.1.65-debug.apk

## Included Fixes

- includesV4163DynamicPlaybookCombinedPackage: true
- noLocalPassiveRoutes: true
- passiveWaitPanel: true
- expressSelfSimpleMode: true
- expressSelfRepeatClickStable: true
- xiaoenaiGenericTrial: true

## Previous Validation Carried Into This Package

- v4164EmulatorSmoke: PASS
- nextSentenceCloudOnly: true
- passiveWaitPanelShown: true
- localPassiveRoutesShownToUser: false
- expressSelfFeedbackCollapsed: true
- xiaoenaiHandled: GENERIC_TRIAL

## Build Verification

- assembleDebug: PASS
- apkCopiedToUpdateServer: true
- latestJsonPointsToExistingApk: true
- secretScanResult: PRIVATE_RELAY_KEY_PRESENT_IN_APK_NOT_COMMITTED_TO_PUBLIC_GITHUB

## User Test Scope

User can now phone-test this round after updating through LAN. The APK is intentionally not committed to public GitHub because it contains private relay configuration.

Expected behavior:
- 下一句 LAST_OTHER with no cloud/cache verified playbook: show passive wait panel, no local fallback routes.
- 下一句 LAST_ME: show wait message.
- 表达我: simplified panel, at most 3 default routes, feedback collapsed by default.
- 表达我 repeated click: stable result reuse.
- 小恩爱: handled as Generic Trial when snapshot is stable.
