# Huiyi v4.1.19 Cloud Analysis MVP + Local LAST ME Safety Gate Report

- project: huiyi-v4
- taskName: cloud_analysis_mvp_with_local_safety_gate
- versionName: 4.1.19
- versionCode: 437
- generatedAt: 2026-07-03 17:06:00 +08:00
- overall_result: CONTROLLED_LOCAL_PASS
- cloud_analysis_mvp: CONTROLLED_LOCAL_PASS
- cloud_endpoint_configured: NO
- real_device_result: NEEDS_USER_SMOKE_TEST
- api_key_in_apk: false
- api_key_in_repo: false

## Scope

- LAST ME stays local and returns WAIT before any cloud path.
- LAST OTHER may call Huiyi Cloud Gateway only when a cloud endpoint is configured.
- Cloud failure or missing config falls back to local 5-route suggestions.
- Android does not call the model provider directly for cloud analysis and does not embed provider keys.

## Implementation

- Added CloudAnalysisConfig.
- Added CloudAnalysisClient.
- Added CloudAnalysisRepository.
- Added CloudTacticalDecisionMapper.
- Added CloudAnalysisTrace.
- CurrentScreenPipelineUseCase now applies the local safety gate before cloud.
- CurrentScreenPipelineResult carries CloudAnalysisTrace.
- Evidence reports include cloudAttempted/cloudSuccess/cloudSkippedReason/decisionSource/cloudFallbackUsed/cloudLatencyMs/cloudErrorCode.
- One-tap feedback README and flight-record JSON include the same cloud fields.
- Floating panel and Compose panel show cloud/fallback labels when applicable.

## LAST ME Safety Gate

Expected and enforced:

- actualLastSpeaker: ME
- decisionType: WAIT
- decisionTypeFamily: WAIT
- terminalState: WAIT_PANEL
- routeCount: 0
- apiCalled: false
- modelCalled: false
- cloudAttempted: false
- cloudSkippedReason: LAST_SPEAKER_ME_WAIT
- decisionSource: LOCAL_WAIT

## LAST OTHER Cloud Behavior

When endpoint is configured:

- cloudAttempted: true
- cloudSuccess: true if schema is valid and 5 routes are returned
- decisionSource: CLOUD
- routeCount: 5
- apiCalled: true
- modelCalled: true

When endpoint is missing or cloud fails:

- cloudSkippedReason: CLOUD_NOT_CONFIGURED, or cloudErrorCode: TIMEOUT/NETWORK/SCHEMA_INVALID/SERVER_ERROR
- decisionSource: LOCAL_FALLBACK
- cloudFallbackUsed: true for attempted failures
- routeCount: 5 when local route generation is allowed

## Tests

- testDebugUnitTest: PASS
- assembleDebug: PASS
- LastMeSkipsCloudAndReturnsWaitTest: PASS
- LastMeCannotBecomeContextRequiredEvenWhenCloudEnabledTest: PASS
- LastOtherCallsCloudWhenEnabledTest: PASS
- LastOtherCloudSuccessShowsCloudRoutesTest: PASS
- LastOtherCloudTimeoutFallsBackToLocalTest: PASS
- CloudSchemaInvalidFallsBackToLocalTest: PASS
- CloudNotConfiguredUsesLocalRoutesTest: PASS
- ApiKeyNotPresentInApkOrRepoTest: PASS
- CloudTraceWrittenToFlightRecordTest: PASS
- CloudResultRouteCountMustBeFiveTest: PASS
- CloudFailureDoesNotShowGenericAnalysisFailedTest: PASS

## LAN Update

- latestVersionName: 4.1.19
- latestVersionCode: 437
- latestJsonLan: http://192.168.31.243:8787/latest.json
- apkPath: outputs/huiyi-v4.1.19-debug.apk
- updateServerApkPath: outputs/update_server/huiyi-v4.1.19-debug.apk
- sha256: ED2006BD89A5B82F59DF71D75B2EAA0AC6217B0BF2B2977E055FAFB84C9452EA

## User Testing Needed

1. LAST ME:
   - Send "嗯嗯".
   - Tap "下一句".
   - Expected: "你已经回过了，先等对方。"
   - No cloud analyzing text, no context required text, no 5 routes.

2. LAST OTHER:
   - Open a chat where the last effective message is from the other person.
   - Tap "下一句".
   - Because cloud endpoint is not configured on this build, expected result is local fallback routes with cloudSkippedReason = CLOUD_NOT_CONFIGURED.

## Result

- LAST ME local safety gate: PASS
- cloud analysis MVP: CONTROLLED_LOCAL_PASS
- cloud endpoint configured: NO
- last OTHER cloud route: LOCAL_FALLBACK
- API key not in APK/repo: PASS
- GitHub phone latest report fields: PASS
