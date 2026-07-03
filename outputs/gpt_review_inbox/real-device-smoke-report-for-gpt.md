# v4.1.10 Real Device Scenario Truth Smoke Report

## Basic

- generatedAt: 2026-07-03 00:00:00 +08:00
- versionName: 4.1.10
- versionCode: 426
- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- realDeviceFunctionalSmoke: NOT_TESTED
- scenarioAssertionResult: NOT_TESTED
- currentOverallResult: NOT_TESTED
- failReason: local_build_no_physical_device
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED

## Scenario Assertion Diagnosis

- scenarioName: auto_from_screen
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: NO_FIXED_EXPECTATION
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: NOT_TESTED
- expectedDecisionType: NO_FIXED_EXPECTATION
- actualDecisionType: NOT_TESTED
- expectedRouteCount: NO_FIXED_EXPECTATION
- actualRouteCount: 0
- scenarioDefinitionTrusted: false
- scenarioFailureCategory: not_tested
- scenarioDefinitionMismatchReason: none

## Snapshot Phase Separation

- preAnalysisSnapshotAvailable: false
- preAnalysisWindowTitle: NOT_TESTED
- preAnalysisResultPanelVisible: false
- decisionSnapshotAvailable: false
- postPanelSnapshotAvailable: false
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false

## Functional Smoke Rules

- screenshotUnavailable = true does not fail the main path.
- If actualLastSpeaker = ME, expected product behavior is WAIT + 0 routes + apiCalled=false.
- If actualLastSpeaker = OTHER, expected product behavior is NORMAL_REPLY + 5 routes, or CONTEXT_REQUIRED when context is genuinely insufficient.
- If manual scenarioName conflicts with actual current-screen evidence, result is scenarioAssertionResult=MISMATCH and currentOverallResult=CONTROLLED_PASS_WITH_SCENARIO_MISMATCH, not parser failure.

## Local Status

- real device tests: NOT_TESTED
- reason: no physical Android device is attached to this Codex environment.
- next step: install v4.1.10 through LAN update, run one phone sample, then export the phone review bundle.
