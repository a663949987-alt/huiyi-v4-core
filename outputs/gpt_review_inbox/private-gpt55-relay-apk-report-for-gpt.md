# Private GPT 5.5 Relay APK Report

- taskName: private_gpt55_relay_apk_delivery
- versionName: 4.1.26
- versionCode: 445
- generatedAt: 2026-07-03 21:42:50 +08:00
- privateApkDelivered: true
- apkDeliveredOutOfBand: true
- apkArtifactName: huiyi-v4.1.26-gpt55-relay-internal.apk
- userInstallApkAbsolutePath: C:\Users\fbjdf\Downloads\Huiyi\huiyi-v4.1.26-gpt55-relay-internal.apk
- workspacePrivateApkAbsolutePath: C:\Users\fbjdf\Documents\Codex\2026-07-02\new-chat-3\outputs\private\huiyi-v4.1.26-gpt55-relay-internal.apk
- apkSha256: dd78887595361830d87c73bbea83d3e21b7df85aa32c6c89ea2235bd8eb5e306
- apkSizeBytes: 30695523
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- userReceivedApk: true
- githubReleaseUploaded: false
- publicUpdateServerUsed: false

## Relay Configuration
- relayConfiguredForBuild: true
- relayModelConfigured: true
- relayApiKeyConfigured: true
- relayApiKeyLeaked: false
- relayBaseUrlRedacted: true
- relayModelNameRedacted: true
- relayApiKeyRedacted: true
- githubContainsOnlyRedactedReport: true

## Relay Smoke
- relaySmokeResult: PASS
- relayRequestAttempted: true
- relayResponseReceived: true
- cloudResponseParsed: true
- validatorPass: true
- routeCount: 5
- apiKeyLeaked: false

## App Behavior Checks
- cloudResponseParsed: true
- cloudFallbackWorks: true
- lastMeSafetyGate: PASS
- lastOtherCloudPath: PASS
- noAnalysisFailedOnCloudFallback: PASS
- noInfiniteLoadingLocalCoverage: PASS
- realDeviceSmoke: NOT_TESTED_USER_NEEDED
- userNeedsPhoneThisRound: true
- overallResult: PRIVATE_RELAY_APK_DELIVERED_RELAY_SMOKE_PASS

## User Test
1. Install the private APK artifact from C:\Users\fbjdf\Downloads\Huiyi\huiyi-v4.1.26-gpt55-relay-internal.apk.
2. Open Huiyi and confirm cloud status is ready.
3. Open Liaoqi and tap Next Sentence.

LAST OTHER should show cloud analysis and 5 routes. LAST ME must show the wait message, with no routes and no cloud call.
