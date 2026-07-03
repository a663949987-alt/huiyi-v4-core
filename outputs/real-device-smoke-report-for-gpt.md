# v4.1.7 Real Device Smoke Report

## Basic

- generatedAt: 2026-07-03 11:07:07 +08:00
- versionName: 4.1.7
- versionCode: 418
- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- scenarioName: last_me
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED
- apiCalled: false
- failReason: not_tested

## Scenario A Hard Acceptance

- scenarioName: last_me
- expectedLastSpeaker: ME
- actualLastSpeaker: NOT_TESTED
- expectedDecisionType: WAIT
- actualDecisionType: NOT_TESTED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: NOT_TESTED
- failureReason: not_tested

## Required Phone Result

When the user selects A last_me on a real Liaoqi chat window:

- actualLastSpeaker must be ME
- actualDecisionType must be WAIT
- actualRouteCount must be 0
- apiCalled must be false

If actualLastSpeaker=OTHER, the report must be FAIL with failureReason=last_speaker_mismatch.

## Parser Diagnostics

- LiaoqiRealParser priority: enabled for com.bajiao.im.liaoqi
- GenericVisualBubbleParser fallback: enabled only if LiaoqiRealParser produces no effective chat messages
- visual order table: added to current screen report
- bounds table: added to current screen report
- possible_speaker_conflict: added
- date metadata filtering: 07-02 and related formats now DATE/SYSTEM/non-effective
