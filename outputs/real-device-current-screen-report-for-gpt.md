# Real Device Current Screen Evidence Pack

- overall_result: NOT_TESTED
- realDeviceSmoke: NOT_TESTED
- generatedAt: 2026-07-03 11:07:07 +08:00
- versionName: 4.1.7
- versionCode: 418
- scenarioName: last_me
- expectedLastSpeaker: ME
- actualLastSpeaker: NOT_TESTED
- expectedDecisionType: WAIT
- actualDecisionType: NOT_TESTED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: NOT_TESTED
- failureReason: not_tested
- sample_source: NOT_TESTED
- appPackage: NOT_TESTED
- windowTitle: NOT_TESTED
- apiCalled: false
- overlayShownInTargetApp: NOT_TESTED
- mainActivityOpened: NOT_TESTED
- parserName: NOT_TESTED
- LiaoqiRealParserUsed: NOT_TESTED
- GenericVisualBubbleParserFallbackUsed: NOT_TESTED
- dateMetadataFiltering: implemented
- dateMetadataPatterns: 07-02 / 06-30 / 7-02 / 7月2日 / 2026-07-02 / 今天 / 昨天 / 星期一

## Scenario Validation

- scenarioName: last_me
- expectedLastSpeaker: ME
- actualLastSpeaker: NOT_TESTED
- expectedDecisionType: WAIT
- actualDecisionType: NOT_TESTED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: NOT_TESTED
- failureReason: not_tested

## Last Effective Message Preview

- lastEffectiveMessagePreview: NOT_TESTED
- possible_speaker_conflict: NOT_TESTED

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds.left/top/right/bottom | rowBounds.left/top/right/bottom | bubbleBounds.left/top/right/bottom | parentBounds.left/top/right/bottom | inferredSide | speakerReason | sideMarginLeft | sideMarginRight | finalDecisionSource | possible_speaker_conflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds.left/top/right/bottom | rowBounds.left/top/right/bottom | bubbleBounds.left/top/right/bottom | parentBounds.left/top/right/bottom | inferredSide | speakerReason | sideMarginLeft | sideMarginRight | finalDecisionSource | possible_speaker_conflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |

## Parser Notes

- LiaoqiRealParser priority: appPackage=com.bajiao.im.liaoqi first.
- Fallback order: LiaoqiRealParser -> GenericVisualBubbleParser -> UNKNOWN.
- LastSpeakerDecision source: finalVisualOrder last effective message.
- Local machine did not run a physical phone smoke test in this build.
