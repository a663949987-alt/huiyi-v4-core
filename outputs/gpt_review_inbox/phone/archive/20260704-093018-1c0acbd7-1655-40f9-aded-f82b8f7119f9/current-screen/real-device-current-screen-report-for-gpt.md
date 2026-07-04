# Real Device Current Screen Evidence Pack

- overall_result: PASS
- realDeviceFunctionalSmoke: PASS
- scenarioAssertionResult: PASS
- currentOverallResult: PASS
- generatedAt: 1783157416706
- scenarioName: real_device_last_other
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: OTHER
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: OTHER
- actualLastSpeakerFromPreAnalysisSnapshot: OTHER
- actualLastSpeakerFromDecisionSnapshot: OTHER
- actualLastSpeakerFromPostPanelSnapshot: OTHER
- expectedDecisionType: NO_FIXED_EXPECTATION
- actualDecisionType: NORMAL_REPLY
- expectedRouteCount: 5
- actualRouteCount: 5
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: none
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: true
- failureReason: none
- sample_source: real_device_accessibility
- appPackage: com.xiaoenai.app
- windowTitle: 小恩爱
- preAnalysisWindowTitle: 小恩爱
- preAnalysisSnapshotTrusted: true
- preAnalysisWindowTitleSource: TARGET_PRE_ANALYSIS
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false
- screenWidth: 1084
- screenHeight: 2302
- density: 3.0
- scaledDensity: 3.0
- fontScale: 1.0
- fontScaleEstimate: 1.0
- smallestScreenWidthDp: 361
- displaySizeCategory: 268435810
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 50
- parserName: GenericVisualBubbleParser
- LiaoqiRealParserUsed: false
- GenericVisualBubbleParserFallbackUsed: false
- parserFallbackUsed: false
- currentBubbleSideRule: right=me

deviceVisualConfig:
  screenWidth: 1084
  screenHeight: 2302
  density: 3.0
  scaledDensity: 3.0
  fontScale: 1.0
  fontScaleEstimate: 1.0
  smallestScreenWidthDp: 361
  displaySizeCategory: 268435810
- modelCalled: false
- apiCalled: false
- cloudEnabled: true
- cloudEndpointConfigured: true
- providerType: OPENAI_COMPATIBLE_RELAY
- relayBaseUrlConfigured: true
- relayApiKeyConfigured: true
- relayApiKeyStoredSecurely: true
- relayApiKeyExposedInRepo: false
- relayApiKeyExposedInApk: false
- cloudAttempted: true
- cloudSuccess: false
- cloudSkippedReason: none
- decisionSource: LOCAL_FALLBACK
- cloudFallbackUsed: true
- cloudLatencyMs: 32556
- cloudErrorCode: TIMEOUT
- cloudRequestId: none
- cloudContractVersion: HuiyiTacticalContract-v1
- cloudContractValidationResult: NOT_RUN
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.xiaoenai.app
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## Visual Debug

- screenshotCaptured: false
- screenshotUnavailable: true
- screenshotDiagnosticStatus: OPTIONAL_FAILED
- screenshotFailureBlocksMainPath: false
- secondaryDiagnosticErrorCode: SCREENSHOT_CAPABILITY_MISSING
- reason: REAL_USE_FAST_PATH
- screenshotPath: none
- overlayImagePath: none
- screenshotWidth: 1084
- screenshotHeight: 2302
- accessibilityBoundsProjected: true
- ocrUsed: false
- visualTruthAvailable: false
- visualTruthSource: NONE
- accessibilityProjectionAvailable: true
- visualProjectionSource: ACCESSIBILITY_BOUNDS_ONLY
- VisualSpeakerFallbackUsed: true
- visualSpeakerFallbackCount: 4
- conflictCount: 0
- failureCategory: none
- userCorrectionProvided: false
- correctedLastSpeaker: none
- correctedMessageId: none

## Snapshot Phase Separation

- preAnalysisSnapshotAvailable: true
- preAnalysisWindowTitle: 小恩爱
- preAnalysisSnapshotTrusted: true
- preAnalysisWindowTitleSource: TARGET_PRE_ANALYSIS
- preAnalysisResultPanelVisible: false
- decisionSnapshotAvailable: true
- postPanelSnapshotAvailable: true
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false

## 解析结果
- rawParsedNodeCount: 13
- metadataFilteredCount: 4
- candidateChatMessageCount: 9
- unknownSpeakerCount: 3
- effectiveMessageCount: 6
- effectiveMeCount: 4
- effectiveOtherCount: 2
- parsedMessageCount: 13
- meCount: 4
- otherCount: 2
- unknownCount: 3
- unknownRatio: 0.23
- systemCount: 4
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 0
- messageStatusArtifactCount: 4
- readReceiptCount: 4
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: NONE
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 3
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 在家看孩子的人，没有人追的

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 3
- read_receipt_metadata: 4
- bubble_edge_right: 2
- visual_projected_right: 2
- visual_projected_left: 2

### UNKNOWN details
- id: bubble-n512
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 463,193,621,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n517
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,243,709,296
  parentBounds: 24,242,1060,504
  rowBounds: 24,242,1060,504
  bubbleBounds: 24,242,1060,504
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,504 > 24,242,1060,504
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n526
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,965,709,1018
  parentBounds: 24,935,1060,1222
  rowBounds: 24,935,1060,1222
  bubbleBounds: 24,935,1060,1222
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,935,1084,1222 > 24,935,1060,1222
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度8.0℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=463,193,621,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=428,111,656,240 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][unknown][unknown 30% ambiguous_center_bounds] 2026年07月04日 16:47 rawNodeOrder=2 finalVisualOrder=2 rowBounds=24,242,1060,504 textBounds=375,243,709,296 parentBounds=24,242,1060,504 bubbleBounds=24,242,1060,504 projectedBox=24,242,1060,504 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,242,1060,504 textBounds=570,420,648,462 parentBounds=24,242,1060,504 bubbleBounds=24,242,1060,504 projectedBox=24,242,1060,504 accessibilitySide=right visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] 看看你 rawNodeOrder=4 finalVisualOrder=4 rowBounds=672,338,904,474 textBounds=673,339,883,473 parentBounds=672,338,904,474 bubbleBounds=672,338,904,474 projectedBox=672,338,904,474 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=672 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][unknown][unknown 30% ambiguous_center_bounds] 2026年07月04日 16:53 rawNodeOrder=5 finalVisualOrder=5 rowBounds=24,935,1060,1222 textBounds=375,965,709,1018 parentBounds=24,935,1060,1222 bubbleBounds=24,935,1060,1222 projectedBox=24,935,1060,1222 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m006][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=6 finalVisualOrder=6 rowBounds=24,935,1060,1222 textBounds=662,1129,740,1171 parentBounds=24,935,1060,1222 bubbleBounds=24,935,1060,1222 projectedBox=24,935,1060,1222 accessibilitySide=right visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m007][right][me 82% bubble_edge_right] [惊恐] rawNodeOrder=7 finalVisualOrder=7 rowBounds=764,1060,904,1183 textBounds=765,1061,883,1182 parentBounds=764,1060,904,1183 bubbleBounds=764,1060,904,1183 projectedBox=764,1060,904,1183 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=764 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m008][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=8 finalVisualOrder=8 rowBounds=24,1223,1060,1503 textBounds=173,1419,251,1461 parentBounds=24,1223,1060,1503 bubbleBounds=24,1223,1060,1503 projectedBox=24,1223,1060,1503 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m009][right][me 75% visual_projected_right] 冒昧问一下，平时没人追你吗 rawNodeOrder=9 finalVisualOrder=9 rowBounds=275,1277,904,1473 textBounds=276,1278,883,1472 parentBounds=275,1277,904,1473 bubbleBounds=275,1277,904,1473 projectedBox=275,1277,904,1473 accessibilitySide=unknown visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=visual_projected_right unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_right
[m010][left][other 75% visual_projected_left] 天天在家看孩子，不出去 rawNodeOrder=10 finalVisualOrder=10 rowBounds=180,1558,797,1696 textBounds=201,1560,795,1694 parentBounds=180,1558,797,1696 bubbleBounds=180,1558,797,1696 projectedBox=180,1558,797,1696 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=287 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=287 widthRatio=0.57 possible_speaker_conflict=false speakerReason=visual_projected_left
[m011][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=11 finalVisualOrder=11 rowBounds=24,1727,1060,2014 textBounds=173,1930,251,1972 parentBounds=24,1727,1060,2014 bubbleBounds=24,1727,1060,2014 projectedBox=24,1727,1060,2014 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m012][right][me 75% visual_projected_right] 嗯，两个孙女现在是最黏人的时候[呲牙] rawNodeOrder=12 finalVisualOrder=12 rowBounds=275,1781,904,1984 textBounds=276,1782,883,1983 parentBounds=275,1781,904,1984 bubbleBounds=275,1781,904,1984 projectedBox=275,1781,904,1984 accessibilitySide=unknown visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=visual_projected_right unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_right
[m013][left][other 75% visual_projected_left] 在家看孩子的人，没有人追的 rawNodeOrder=13 finalVisualOrder=13 rowBounds=180,2069,810,2261 textBounds=201,2071,808,2261 parentBounds=180,2069,810,2261 bubbleBounds=180,2069,810,2261 projectedBox=180,2069,810,2261 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_left

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度8.0℃ | UNKNOWN | UNKNOWN | text | NONE | false | 463,193,621,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | 428,111,656,240 | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 2026年07月04日 16:47 | UNKNOWN | UNKNOWN | text | NONE | false | 375,243,709,296 | 24,242,1060,504 | 24,242,1060,504 | 24,242,1060,504 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,504 > 24,242,1060,504 | unknown | unknown | 24,242,1060,504 | 30 | ambiguous_center_bounds | false | none | false | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 570,420,648,462 | 24,242,1060,504 | 24,242,1060,504 | 24,242,1060,504 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,504 > 24,242,1060,504 | right | unknown | 24,242,1060,504 | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | 看看你 | ME | ME | text | NONE | true | 673,339,883,473 | 672,338,904,474 | 672,338,904,474 | 672,338,904,474 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,504 > 24,242,1060,504 > 672,338,904,474 | right | right | 672,338,904,474 | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 2026年07月04日 16:53 | UNKNOWN | UNKNOWN | text | NONE | false | 375,965,709,1018 | 24,935,1060,1222 | 24,935,1060,1222 | 24,935,1060,1222 | 0,0,1084,2412 > 0,242,1084,2261 > 0,935,1084,1222 > 24,935,1060,1222 | unknown | unknown | 24,935,1060,1222 | 30 | ambiguous_center_bounds | false | none | false | false |
| 6 | 6 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 662,1129,740,1171 | 24,935,1060,1222 | 24,935,1060,1222 | 24,935,1060,1222 | 0,0,1084,2412 > 0,242,1084,2261 > 0,935,1084,1222 > 24,935,1060,1222 | right | unknown | 24,935,1060,1222 | 100 | read_receipt_metadata | false | none | false | false |
| 7 | 7 | [惊恐] | ME | ME | text | NONE | true | 765,1061,883,1182 | 764,1060,904,1183 | 764,1060,904,1183 | 764,1060,904,1183 | 0,0,1084,2412 > 0,242,1084,2261 > 0,935,1084,1222 > 24,935,1060,1222 > 764,1060,904,1183 | right | right | 764,1060,904,1183 | 82 | bubble_edge_right | false | none | false | false |
| 8 | 8 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,1419,251,1461 | 24,1223,1060,1503 | 24,1223,1060,1503 | 24,1223,1060,1503 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1223,1084,1503 > 24,1223,1060,1503 | left | unknown | 24,1223,1060,1503 | 100 | read_receipt_metadata | false | none | false | false |
| 9 | 9 | 冒昧问一下，平时没人追你吗 | ME | ME | text | NONE | true | 276,1278,883,1472 | 275,1277,904,1473 | 275,1277,904,1473 | 275,1277,904,1473 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1223,1084,1503 > 24,1223,1060,1503 > 275,1277,904,1473 | unknown | right | 275,1277,904,1473 | 75 | visual_projected_right | false | none | true | false |
| 10 | 10 | 天天在家看孩子，不出去 | OTHER | OTHER | text | NONE | true | 201,1560,795,1694 | 180,1558,797,1696 | 180,1558,797,1696 | 180,1558,797,1696 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1504,1084,1726 > 24,1504,1060,1726 > 180,1558,797,1696 | unknown | left | 180,1558,797,1696 | 75 | visual_projected_left | false | none | true | false |
| 11 | 11 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,1930,251,1972 | 24,1727,1060,2014 | 24,1727,1060,2014 | 24,1727,1060,2014 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1727,1084,2014 > 24,1727,1060,2014 | left | unknown | 24,1727,1060,2014 | 100 | read_receipt_metadata | false | none | false | false |
| 12 | 12 | 嗯，两个孙女现在是最黏人的时候[呲牙] | ME | ME | text | NONE | true | 276,1782,883,1983 | 275,1781,904,1984 | 275,1781,904,1984 | 275,1781,904,1984 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1727,1084,2014 > 24,1727,1060,2014 > 275,1781,904,1984 | unknown | right | 275,1781,904,1984 | 75 | visual_projected_right | false | none | true | false |
| 13 | 13 | 在家看孩子的人，没有人追的 | OTHER | OTHER | text | NONE | true | 201,2071,808,2261 | 180,2069,810,2261 | 180,2069,810,2261 | 180,2069,810,2261 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2015,1084,2261 > 24,2015,1060,2261 > 180,2069,810,2261 | unknown | left | 180,2069,810,2261 | 75 | visual_projected_left | false | none | true | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 4 | 4 | 看看你 | ME | ME | text | NONE | true | 673,339,883,473 | 672,338,904,474 | 672,338,904,474 | 672,338,904,474 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,504 > 24,242,1060,504 > 672,338,904,474 | right | right | 672,338,904,474 | 82 | bubble_edge_right | false | none | false | false |
| 7 | 7 | [惊恐] | ME | ME | text | NONE | true | 765,1061,883,1182 | 764,1060,904,1183 | 764,1060,904,1183 | 764,1060,904,1183 | 0,0,1084,2412 > 0,242,1084,2261 > 0,935,1084,1222 > 24,935,1060,1222 > 764,1060,904,1183 | right | right | 764,1060,904,1183 | 82 | bubble_edge_right | false | none | false | false |
| 9 | 9 | 冒昧问一下，平时没人追你吗 | ME | ME | text | NONE | true | 276,1278,883,1472 | 275,1277,904,1473 | 275,1277,904,1473 | 275,1277,904,1473 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1223,1084,1503 > 24,1223,1060,1503 > 275,1277,904,1473 | unknown | right | 275,1277,904,1473 | 75 | visual_projected_right | false | none | true | false |
| 10 | 10 | 天天在家看孩子，不出去 | OTHER | OTHER | text | NONE | true | 201,1560,795,1694 | 180,1558,797,1696 | 180,1558,797,1696 | 180,1558,797,1696 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1504,1084,1726 > 24,1504,1060,1726 > 180,1558,797,1696 | unknown | left | 180,1558,797,1696 | 75 | visual_projected_left | false | none | true | false |
| 12 | 12 | 嗯，两个孙女现在是最黏人的时候[呲牙] | ME | ME | text | NONE | true | 276,1782,883,1983 | 275,1781,904,1984 | 275,1781,904,1984 | 275,1781,904,1984 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1727,1084,2014 > 24,1727,1060,2014 > 275,1781,904,1984 | unknown | right | 275,1781,904,1984 | 75 | visual_projected_right | false | none | true | false |
| 13 | 13 | 在家看孩子的人，没有人追的 | OTHER | OTHER | text | NONE | true | 201,2071,808,2261 | 180,2069,810,2261 | 180,2069,810,2261 | 180,2069,810,2261 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2015,1084,2261 > 24,2015,1060,2261 > 180,2069,810,2261 | unknown | left | 180,2069,810,2261 | 75 | visual_projected_left | false | none | true | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n547
- lastEffectiveMessageId: bubble-n547
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: true
- decisionType: NORMAL_REPLY
- reason: 最后一句是对方，可以生成下一句。

## ContextAssembler
- contextCompleteness.score: 100
- canDeepAnalyze: true
- missingTypes: 
- coCreationOpportunity.exists: false
- coCreationOpportunity.type: NO_OPPORTUNITY
- unfinishedMeaning: none
- currentSceneSummary: [REDACTED_PRIVATE_CHAT]

## TacticalDecision
- decisionType: NORMAL_REPLY
- situation: [REDACTED_PRIVATE_CHAT]
- coreInsight: [REDACTED_PRIVATE_CHAT]
- userLikelyMistake: Waiting indefinitely for cloud or closing the panel because no result appears.
- bestMove: [REDACTED_PRIVATE_CHAT]
- avoidMoves: do not keep loading / do not show analysis failed / do not route ME or UNKNOWN
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: cloud_timeout_local_fallback
- fallbackMove: Retry cloud later.

## ReplyRoutes
- route id: route-990076
  name: 稳住
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-25314602
  name: 接情绪
  routeType: EMPATHY
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-36001916
  name: 轻反问
  routeType: CO_CREATION
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-920766892
  name: 生活关心
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-24227824
  name: 微升温
  routeType: WARM_UP
  message: [REDACTED_PRIVATE_CHAT]
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: 这条更主动，注意观察对方是否后撤。
  fallbackMove: none

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 13
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
