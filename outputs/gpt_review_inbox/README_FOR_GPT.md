# Huiyi v4 GPT Review Inbox

- taskName: contamination_hard_stop_and_cloud_network_visibility
- versionName: 4.1.27
- versionCode: 446
- currentOverallResult: LOCAL_TEST_PASS_PRIVATE_LAN_APK_DELIVERED_NEEDS_USER_SMOKE
- privateApkDelivered: true
- apkDeliveredOutOfBand: true
- lanUpdateDelivered: true
- apkArtifactName: huiyi-v4.1.27-contamination-network-fix-internal.apk
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- userReceivedApk: unknown
- userNeedsPhoneThisRound: true

## Private APK Delivery
- deliveryMethod: CODEX_LOCAL_ARTIFACT_AND_PRIVATE_LAN
- userInstallApkAbsolutePath: C:\Users\fbjdf\Downloads\Huiyi\huiyi-v4.1.27-contamination-network-fix-internal.apk
- workspacePrivateApkAbsolutePath: C:\Users\fbjdf\Documents\Codex\2026-07-02\new-chat-3\outputs\private\huiyi-v4.1.27-contamination-network-fix-internal.apk
- lanLatestJsonUrl: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.27-debug.apk
- apkSha256: 338611e30fc0c99a02f09c3eebdf2a77b3ef6d76de83082816bf9bd037fda5e5
- apkSizeBytes: 31059077

## Fix Summary
- contaminatedPreAnalysisHardStop: true
- contaminatedPreAnalysisDecisionType: PRE_ANALYSIS_CONTAMINATED
- contaminatedPreAnalysisTerminalState: CONTROLLED_FAIL
- contaminatedPreAnalysisRouteCount: 0
- contaminatedPreAnalysisCloudAttempted: false
- contaminatedPreAnalysisRoutePanelShown: false
- userFacingMessage: 没读到聊天页，请点一下聊起聊天窗口后再试。
- cloudNetworkFailureVisibleToUser: true
- cloudFailureLikelyCauseReported: true
- internetPermissionPresent: true
- baseUrlJoinDoesNotDuplicateV1: true

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

## GPT Verification Rule
The APK contains private relay configuration, so GitHub only contains redacted reports. The installable APK is delivered out of band via local Codex artifact and private LAN update.
