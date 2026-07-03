# Preconfigured Cloud MVP Report

- taskName: preconfigured_cloud_real_use_mvp
- versionName: 4.1.25
- versionCode: 444
- generatedAt: 2026-07-03 21:11:04 +0800
- apkPath: outputs/huiyi-v4.1.25-cloud-preconfigured.apk
- apkSha256: ad92676e76589353beee632d30b0302b25cfe402dfd1bc71f33e93b9f6ecc945
- preconfiguredCloudApkGenerated: true
- relayConfiguredForBuild: true
- relayBaseUrlConfigured: true
- relayModelConfigured: true
- relayApiKeyConfigured: true
- relayApiKeyRedacted: true
- relayApiKeyLeaked: false
- cloudResponseParsed: true
- cloudFallbackWorks: true
- lastMeSafetyGate: PASS
- lastOtherCloudPath: PASS
- realDeviceSmoke: NOT_TESTED
- userNeedsPhoneThisRound: true
- overallResult: PRECONFIGURED_CLOUD_LOCAL_PASS_NEEDS_USER_PHONE_SMOKE

## User Test
1. Install outputs/huiyi-v4.1.25-cloud-preconfigured.apk.
2. Open the app and check cloud status.
3. Open Liaoqi real chat and tap Next Sentence.

LAST OTHER should use cloud when reachable and fall back to 5 local routes if cloud fails. LAST ME must show the wait-for-other message with no routes and no cloud attempt.
