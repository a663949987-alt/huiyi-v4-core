# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: cloud_timeout_escalation_and_github_delivery_sync
- versionName: 4.1.44
- versionCode: 463
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- userNeedsPhoneThisRound: true

## Latest Phone Finding
- latestPhoneSessionId: b789be05-8c26-40ce-a528-b7f168de2893
- appVersionName: 4.1.43
- appVersionCode: 462
- terminalState: ROUTE_PANEL
- appPackage: com.bajiao.im.liaoqi
- actualLastSpeaker: OTHER
- decisionType: NORMAL_REPLY
- routeCount: 5
- accessibilityRuntimeCategory: CONNECTED_AND_READY
- preAnalysisSnapshotCaptured: true
- preAnalysisLooksLikeHuiyiPanel: false
- reportConsistencyResult: PASS
- cloudAttempted: true
- cloudSuccess: false
- cloudErrorCode: TIMEOUT
- cloudRequestActuallySent: true
- cloudPrimaryModel: gpt-5.4
- cloudFinalModel: gpt-5.4
- cloudTotalLatencyMs: 39063
- decisionSource: LOCAL_FALLBACK

## Fix In 4.1.44
- gpt-5.4 primary timeout window capped to 10 seconds for non-visual current-screen cloud calls.
- TIMEOUT now triggers escalation to gpt-5.5 before local fallback.
- NETWORK with likelyCause=TIMEOUT also triggers escalation to gpt-5.5.
- gpt-5.5 escalation timeout capped to 32 seconds.
- v4.1.43 accessibility service reconnect guard is retained.
- outputs/update_server/latest.json updated to 4.1.44 / 463.

## APK Delivery
- apkDeliveredOutOfBand: true
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.44-debug.apk
- publicGithubApkIncluded: false

## Verification
- :app:testDebugUnitTest PASS
- :app:assembleDebug PASS
- targeted cloud escalation tests PASS
- latestJsonLanReachable: true
- apkLanReachable: true

## GPT Should Inspect
1. outputs/gpt_review_inbox/README_FOR_GPT.md
2. outputs/gpt_review_inbox/phone/latest/README_FOR_GPT.md
3. outputs/gpt_review_inbox/phone/latest/latest-session/next-sentence-flight-record.json
4. outputs/update_server/latest.json
