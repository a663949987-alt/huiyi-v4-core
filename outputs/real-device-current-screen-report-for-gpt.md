# Real Device Current Screen Evidence Pack

- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- generatedAt: 2026-07-03 11:30:00 +08:00
- versionName: 4.1.8
- versionCode: 420
- scenarioName: last_me
- expectedLastSpeaker: ME
- actualLastSpeaker: NOT_TESTED
- expectedDecisionType: WAIT
- actualDecisionType: NOT_TESTED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: NOT_TESTED
- failureReason: not_tested
- failureCategory: visual_projection_unavailable
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED
- apiCalled: false
- overlayShownInTargetApp: NOT_TESTED
- mainActivityOpened: NOT_TESTED
- parserName: NOT_TESTED
- LiaoqiRealParserUsed: NOT_TESTED
- GenericVisualBubbleParserFallbackUsed: NOT_TESTED
- VisualSpeakerFallbackUsed: NOT_TESTED

## Visual Debug

- screenshotCaptured: false
- screenshotUnavailable: true
- reason: local_build_no_physical_device
- screenshotPath: none
- overlayImagePath: outputs/real_device_visual_debug/current_screen_overlay.png
- screenshotWidth: 1080
- screenshotHeight: 2400
- accessibilityBoundsProjected: false
- ocrUsed: false
- visualTruthAvailable: false
- conflictCount: NOT_TESTED
- userCorrectionProvided: false
- correctedLastSpeaker: none
- correctedMessageId: none

## Visual Order Table

| messageId | text | rawNodeOrder | finalVisualOrder | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | finalSpeaker | speakerConfidence | speakerReason | conflict | conflictReason | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |

## Parser Calibration

- LiaoqiRealParser priority: enabled for com.bajiao.im.liaoqi
- VisualTruthAligner Layer A: Accessibility bounds projected to screenshot coordinates
- VisualTruthAligner Layer B: OCR interface reserved, ocrUsed=false in this round
- VisualSpeakerFallback: enabled for UNKNOWN with clear projected side and semantic conflict with right projected side
- Scenario A hard validation: retained
- Date metadata filtering: 07-02 must be DATE / SYSTEM / non-effective

## Failure Categories

If real-device scenario A still fails, report must classify one of:

- accessibility_bounds_wrong
- visual_projection_unavailable
- visual_order_wrong
- user_selected_wrong_scenario
- parser_side_conflict
- metadata_leak
- unknown_too_high
