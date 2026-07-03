# Contamination Hard Stop + Cloud Network Visibility Report

- taskName: contamination_hard_stop_and_cloud_network_visibility
- versionName: 4.1.27
- versionCode: 446
- generatedAt: 2026-07-03 22:27:50 +08:00
- overallResult: LOCAL_TEST_PASS_PRIVATE_LAN_APK_DELIVERED_NEEDS_USER_SMOKE

## APK Delivery
- privateApkDelivered: true
- apkDeliveredOutOfBand: true
- lanUpdateDelivered: true
- apkArtifactName: huiyi-v4.1.27-contamination-network-fix-internal.apk
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- userReceivedApk: unknown
- userInstallApkAbsolutePath: C:\Users\fbjdf\Downloads\Huiyi\huiyi-v4.1.27-contamination-network-fix-internal.apk
- lanLatestJsonUrl: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.27-debug.apk
- apkSha256: 338611e30fc0c99a02f09c3eebdf2a77b3ef6d76de83082816bf9bd037fda5e5

## Behavior
- preAnalysisLooksLikeHuiyiPanel blocks analysis: true
- feedbackUsedOverlayStateAsPreAnalysis blocks routes in export: true
- reportConsistencyResult FAIL_CONTAMINATED_EXPORT blocks routes in export: true
- contaminated terminalState: CONTROLLED_FAIL
- contaminated decisionType: PRE_ANALYSIS_CONTAMINATED
- contaminated routeCount: 0
- contaminated cloudAttempted: false
- contaminated routePanelShown: false
- clean LAST_OTHER may route: true
- clean LAST_ME waits: true

## Cloud Network
- androidInternetPermissionPresent: true
- cleartextTrafficAllowed: true
- relayPathUsesChatCompletions: true
- authorizationHeaderUsesBearer: true
- baseUrlJoinDoesNotDuplicateV1: true
- cloudNetworkFailureVisibleToUser: true
- cloudRequestActuallySentFieldAdded: true
- cloudFailureLikelyCauseFieldAdded: true

## Tests
- :app:testDebugUnitTest: PASS
- :app:assembleDebug: PASS
- ContaminatedPreAnalysisDoesNotRouteTest: PASS
- ContaminatedPreAnalysisSkipsCloudTest: PASS
- HuiyiPanelWindowTitleBlocksAnalysisTest: PASS
- ChatWindowNotFoundShowsControlledFailNotRoutesTest: PASS
- CloudNetworkFailureShowsVisibleFallbackReasonTest: PASS
- LastOtherRoutesOnlyWhenPreAnalysisCleanTest: PASS
- LastMeWaitOnlyWhenPreAnalysisCleanTest: PASS
- InternetPermissionPresentForCloudTest: PASS
- BaseUrlJoinDoesNotDuplicateV1Test: PASS

## User Smoke
User should update through private LAN or install the local artifact, open a real Liaoqi chat page, tap Next Sentence, and verify:
1. If Huiyi panel is sampled, it shows the chat-page prompt and no routes.
2. If LAST OTHER is clean, it shows cloud result or local fallback with visible network failure.
3. If LAST ME is clean, it shows the wait message.
