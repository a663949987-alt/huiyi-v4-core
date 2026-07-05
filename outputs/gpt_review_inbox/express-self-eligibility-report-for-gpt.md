# Express Self Eligibility / HOLD_BACK Fix Report

## Basic Info
- taskName: express_self_eligibility_and_hold_back_fix
- versionName: 4.1.62
- versionCode: 481
- generatedAt: 2026-07-05T17:19:36.4405442+08:00
- branch: main
- baseCommitBeforeThisReport: 72d39931aafcdd484bad26f59c502dd6d22aec7f
- overallResult: LOCAL_TEST_PASS_APK_GENERATED
- apkPath: outputs/update_server/huiyi-v4.1.62-debug.apk
- apkSha256: 4355D08810DBE1B176FE754C0E946F82FD4297E2F097758056DBD5BA1F6C6247

## What Changed
- Added ExpressSelfEligibility and ExpressSelfEligibilityEvaluator.
- Express Self now blocks unsupported, desktop/launcher/Huiyi-panel, untrusted snapshot, recent LAST_ME, repeated self-expression, and high repeat-risk states.
- Blocked Express Self cannot return NORMAL_REPLY, ROUTE_PANEL, 5 routes, cloudAttempted=true, or background cloud refresh.
- HOLD_BACK panel is used for recent self-message / over-expression states.
- Unsupported or untrusted states show a controlled fail style Express Self panel.
- One-tap feedback now exports Express Self eligibility fields.

## Required Bug Fixture
Input:
- appPackage: com.xiaoenai.app
- windowTitlePreAnalysisRedacted: Huawei desktop
- targetAppSupported: false
- preAnalysisSnapshotSource: LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL
- actualLastSpeaker: ME
- shouldReply: false

Expected / implemented:
- eligible=false
- mode=BLOCK_UNTRUSTED_SNAPSHOT or BLOCK_UNSUPPORTED_CONTEXT
- decisionType != NORMAL_REPLY
- routeCount=0
- routePanelShown=false
- cloudAttempted=false

## Acceptance Matrix
- unsupported context blocked: PASS
- Huawei desktop snapshot blocked: PASS
- untrusted last-stable snapshot blocked: PASS
- recent LAST_ME hold back: PASS
- recent self-expression hold back: PASS
- cold start after >=30min allowed: PASS
- LAST_OTHER planning/reality/stability arc reveal allowed: PASS
- blocked Express Self never returns NORMAL_REPLY: PASS
- blocked Express Self routeCount=0: PASS
- report fields added: PASS

## Tests
- :app:testDebugUnitTest --tests com.huiyi.v4.ExpressSelfEligibilityTest: PASS
- :app:testDebugUnitTest --tests com.huiyi.v4.DynamicPlaybookEngineTest --tests com.huiyi.v4.ExpressSelfUiLoopTest --tests com.huiyi.v4.ExpressSelfEligibilityTest: PASS
- :app:assembleDebug: PASS
- :app:testDebugUnitTest: PASS

## User Test Needed
Yes, but only Express Self:
1. Just sent own message -> should show HOLD_BACK, no 5 active routes.
2. Cold start after a while -> can show low-pressure START_TOPIC.
3. Other person talks planning/reality/stability -> can show ARC_REVEAL / Express Self.
4. Desktop or unsupported app -> should block and say chat state is not stable.


