# v4.1.8 Real Device Smoke Report

## Basic

- generatedAt: 2026-07-03 11:30:00 +08:00
- versionName: 4.1.8
- versionCode: 420
- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- scenarioName: last_me
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED
- apiCalled: false
- failReason: not_tested
- failureCategory: visual_projection_unavailable

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

## Visual Debug

- screenshotCaptured: false
- screenshotUnavailable: true
- overlayImagePath: outputs/real_device_visual_debug/current_screen_overlay.png
- accessibilityBoundsProjected: false
- ocrUsed: false
- visualTruthAvailable: false
- VisualSpeakerFallbackUsed: NOT_TESTED
- conflictCount: NOT_TESTED

## Phone Acceptance Rule

If scenarioName=last_me and actualLastSpeaker=OTHER, the result must be FAIL with failureReason=last_speaker_mismatch.

If scenario A still fails in v4.1.8, the exported visual overlay image must show why the final effective message was classified as OTHER.
