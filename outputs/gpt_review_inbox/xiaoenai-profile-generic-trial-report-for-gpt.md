# Xiaoenai Profile / Generic Trial Report

## Basic Info
- taskName: xiaoenai_profile_generic_trial_before_phone
- versionName: 4.1.68
- versionCode: 487
- generatedAt: 2026-07-06T13:46:11+08:00
- currentOverallResult: XIAOENAI_GENERIC_TRIAL_FIXTURE_PASS
- userNeedsPhoneThisRound: false

## Scope
- Do not change cloud, character arc, parser architecture, session isolation, or light listening.
- Only fix Xiaoenai normal chat recognition vs Huawei desktop blocking.
- Use fixture/unit coverage before asking for another phone test.

## Implementation Result
- Xiaoenai Generic Trial exists: true
- Xiaoenai AppProfile full adapter: false
- Generic Trial requires `appPackage=com.xiaoenai.app`: true
- Generic Trial requires `windowTitle=小恩爱`: true
- Generic Trial requires effectiveMessageCount >= 3: true
- Generic Trial requires parserConfidence >= 70: true
- `windowTitle=华为桌面` blocks even when appPackage is Xiaoenai: true
- Last stable snapshot is not used to continue analysis when current window is desktop: true

## Scenario Matrix

### XIAOENAI_NORMAL_CHAT_LAST_OTHER
- appPackage: `com.xiaoenai.app`
- windowTitle: `小恩爱`
- expected: normal usable branch
- actualLastSpeaker: OTHER
- decisionType: PASSIVE_NOT_READY
- passiveWaitPanelShown: true
- cloudRefreshRecommended: true
- result: PASS

### XIAOENAI_NORMAL_CHAT_EXPRESS_SELF_PLANNING
- appPackage: `com.xiaoenai.app`
- windowTitle: `小恩爱`
- effectiveMessageCount: >= 3
- parserConfidence: 82
- expected: ALLOW_GENERIC_TRIAL or XIAOENAI_PROFILE
- actual: ALLOW_GENERIC_TRIAL
- source: GENERIC_TRIAL
- routeCount: > 0
- result: PASS

### XIAOENAI_DESKTOP_BLOCK
- appPackage: `com.xiaoenai.app`
- windowTitle: `华为桌面`
- expected: BLOCK
- actual: BLOCK_UNTRUSTED_SNAPSHOT
- blockReason: WINDOW_IS_DESKTOP_OR_LAUNCHER
- routeCount: 0
- cloudAttempted: false
- result: PASS

### XIAOENAI_DESKTOP_LAST_STABLE_SNAPSHOT_BLOCK
- appPackage: `com.xiaoenai.app`
- windowTitle: `华为桌面`
- preAnalysisSnapshotSource: LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL
- expected: do not continue analysis from old stable snapshot
- actual: BLOCK_UNTRUSTED_SNAPSHOT
- routeCount: 0
- cloudAttempted: false
- result: PASS

### XIAOENAI_LOW_CONFIDENCE_BLOCK
- appPackage: `com.xiaoenai.app`
- windowTitle: `小恩爱`
- parserConfidence: 55
- expected: BLOCK
- actual: BLOCK_UNSUPPORTED_CONTEXT
- blockReason: LOW_GENERIC_CONFIDENCE
- result: PASS

## Test Result
- targetedBranchTests: PASS
- unitTests: PASS
- assembleDebug: PASS
- emulatorSmokeResult: NOT_RUN_FIXTURE_BRANCH_COVERAGE_USED

## User Testing Gate
- userNeedsPhoneThisRound: false
- phoneTestAllowedAfterThisFixturePass: true
- note: next user phone test should use a package built after this v4.1.68 fix.
