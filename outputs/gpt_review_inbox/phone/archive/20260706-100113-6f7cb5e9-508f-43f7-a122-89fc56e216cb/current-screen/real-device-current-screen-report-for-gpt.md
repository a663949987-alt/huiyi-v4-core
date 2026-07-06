# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783332072017
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
- actualRouteCount: 3
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: FUNCTIONAL_PASS_ASSERTION_FAIL
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: false
- failureReason: route_count_mismatch
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
- capturedNodeCount: 56
- parserName: DynamicPlaybookStableSnapshot
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
- cloudAttempted: false
- cloudSuccess: false
- cloudSkippedReason: CLOUD_REFRESH_BACKGROUND_OPTIONAL
- decisionSource: CLOUD_ENHANCED_PLAYBOOK
- cloudFallbackUsed: false
- cloudLatencyMs: null
- cloudErrorCode: none
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
- screenshotDiagnosticStatus: NOT_ATTEMPTED
- screenshotFailureBlocksMainPath: false
- secondaryDiagnosticErrorCode: none
- reason: visual_projection_only_or_not_captured
- screenshotPath: none
- overlayImagePath: none
- screenshotWidth: 1084
- screenshotHeight: 2302
- accessibilityBoundsProjected: false
- ocrUsed: false
- visualTruthAvailable: false
- visualTruthSource: NONE
- accessibilityProjectionAvailable: false
- visualProjectionSource: ACCESSIBILITY_BOUNDS_ONLY
- VisualSpeakerFallbackUsed: false
- visualSpeakerFallbackCount: 0
- conflictCount: 0
- failureCategory: route_count_mismatch
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
- rawParsedNodeCount: 17
- metadataFilteredCount: 4
- candidateChatMessageCount: 13
- unknownSpeakerCount: 6
- effectiveMessageCount: 7
- effectiveMeCount: 4
- effectiveOtherCount: 3
- parsedMessageCount: 17
- meCount: 4
- otherCount: 3
- unknownCount: 6
- unknownRatio: 0.35
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
- lastEffectiveMessagePreview: 米饭

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 6
- read_receipt_metadata: 4
- bubble_edge_right: 4
- bubble_edge_left: 3

### UNKNOWN details
- id: bubble-n21030
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n21035
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,262,709,315
  parentBounds: 24,242,1060,524
  rowBounds: 24,242,1060,524
  bubbleBounds: 24,242,1060,524
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,524 > 24,242,1060,524
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n21041
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,555,709,608
  parentBounds: 24,525,1060,818
  rowBounds: 24,525,1060,818
  bubbleBounds: 24,525,1060,818
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,525,1084,818 > 24,525,1060,818
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n21046
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,849,709,902
  parentBounds: 24,819,1060,1110
  rowBounds: 24,819,1060,1110
  bubbleBounds: 24,819,1060,1110
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,819,1084,1110 > 24,819,1060,1110
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n21057
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1362,709,1415
  parentBounds: 24,1332,1060,1625
  rowBounds: 24,1332,1060,1625
  bubbleBounds: 24,1332,1060,1625
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1332,1084,1625 > 24,1332,1060,1625
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n21067
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1877,709,1930
  parentBounds: 24,1847,1060,2140
  rowBounds: 24,1847,1060,2140
  bubbleBounds: 24,1847,1060,2140
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1847,1084,2140 > 24,1847,1060,2140
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度19.3℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 15:37 rawNodeOrder=2 finalVisualOrder=2 rowBounds=24,242,1060,524 textBounds=375,262,709,315 parentBounds=24,242,1060,524 bubbleBounds=24,242,1060,524 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,242,1060,524 textBounds=422,440,500,482 parentBounds=24,242,1060,524 bubbleBounds=24,242,1060,524 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] 忙完回来了[呲牙] rawNodeOrder=4 finalVisualOrder=4 rowBounds=524,357,904,494 textBounds=525,358,883,493 parentBounds=524,357,904,494 bubbleBounds=524,357,904,494 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=524 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 17:06 rawNodeOrder=5 finalVisualOrder=5 rowBounds=24,525,1060,818 textBounds=375,555,709,608 parentBounds=24,525,1060,818 bubbleBounds=24,525,1060,818 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m006][left][other 82% bubble_edge_left] 下班了 rawNodeOrder=6 finalVisualOrder=6 rowBounds=180,650,413,788 textBounds=201,652,411,786 parentBounds=180,650,413,788 bubbleBounds=180,650,413,788 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=671 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m007][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 17:12 rawNodeOrder=7 finalVisualOrder=7 rowBounds=24,819,1060,1110 textBounds=375,849,709,902 parentBounds=24,819,1060,1110 bubbleBounds=24,819,1060,1110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m008][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=8 finalVisualOrder=8 rowBounds=24,819,1060,1110 textBounds=570,1026,648,1068 parentBounds=24,819,1060,1110 bubbleBounds=24,819,1060,1110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m009][right][me 82% bubble_edge_right] 嗯算吧 rawNodeOrder=9 finalVisualOrder=9 rowBounds=672,944,904,1080 textBounds=673,945,883,1079 parentBounds=672,944,904,1080 bubbleBounds=672,944,904,1080 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=672 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m010][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=10 finalVisualOrder=10 rowBounds=24,1111,1060,1331 textBounds=426,1247,504,1289 parentBounds=24,1111,1060,1331 bubbleBounds=24,1111,1060,1331 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m011][right][me 82% bubble_edge_right] 准备吃晚饭啦 rawNodeOrder=11 finalVisualOrder=11 rowBounds=528,1165,904,1301 textBounds=529,1166,883,1300 parentBounds=528,1165,904,1301 bubbleBounds=528,1165,904,1301 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=528 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m012][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 17:46 rawNodeOrder=12 finalVisualOrder=12 rowBounds=24,1332,1060,1625 textBounds=375,1362,709,1415 parentBounds=24,1332,1060,1625 bubbleBounds=24,1332,1060,1625 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m013][left][other 82% bubble_edge_left] 我也准备做饭了 rawNodeOrder=13 finalVisualOrder=13 rowBounds=180,1457,605,1595 textBounds=201,1459,603,1593 parentBounds=180,1457,605,1595 bubbleBounds=180,1457,605,1595 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=479 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m014][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=14 finalVisualOrder=14 rowBounds=24,1626,1060,1846 textBounds=330,1762,408,1804 parentBounds=24,1626,1060,1846 bubbleBounds=24,1626,1060,1846 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m015][right][me 82% bubble_edge_right] 那你做啥好吃的？ rawNodeOrder=15 finalVisualOrder=15 rowBounds=432,1680,904,1816 textBounds=433,1681,883,1815 parentBounds=432,1680,904,1816 bubbleBounds=432,1680,904,1816 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=432 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m016][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 17:55 rawNodeOrder=16 finalVisualOrder=16 rowBounds=24,1847,1060,2140 textBounds=375,1877,709,1930 parentBounds=24,1847,1060,2140 bubbleBounds=24,1847,1060,2140 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m017][left][other 82% bubble_edge_left] 米饭 rawNodeOrder=17 finalVisualOrder=17 rowBounds=180,1972,365,2110 textBounds=201,1974,363,2108 parentBounds=180,1972,365,2110 bubbleBounds=180,1972,365,2110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=719 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度19.3℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 2026年07月06日 15:37 | UNKNOWN | UNKNOWN | text | NONE | false | 375,262,709,315 | 24,242,1060,524 | 24,242,1060,524 | 24,242,1060,524 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,524 > 24,242,1060,524 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 422,440,500,482 | 24,242,1060,524 | 24,242,1060,524 | 24,242,1060,524 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,524 > 24,242,1060,524 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | 忙完回来了[呲牙] | ME | ME | text | NONE | true | 525,358,883,493 | 524,357,904,494 | 524,357,904,494 | 524,357,904,494 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,524 > 24,242,1060,524 > 524,357,904,494 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 2026年07月06日 17:06 | UNKNOWN | UNKNOWN | text | NONE | false | 375,555,709,608 | 24,525,1060,818 | 24,525,1060,818 | 24,525,1060,818 | 0,0,1084,2412 > 0,242,1084,2261 > 0,525,1084,818 > 24,525,1060,818 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 6 | 6 | 下班了 | OTHER | OTHER | text | NONE | true | 201,652,411,786 | 180,650,413,788 | 180,650,413,788 | 180,650,413,788 | 0,0,1084,2412 > 0,242,1084,2261 > 0,525,1084,818 > 24,525,1060,818 > 180,650,413,788 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 7 | 7 | 2026年07月06日 17:12 | UNKNOWN | UNKNOWN | text | NONE | false | 375,849,709,902 | 24,819,1060,1110 | 24,819,1060,1110 | 24,819,1060,1110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,819,1084,1110 > 24,819,1060,1110 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 8 | 8 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 570,1026,648,1068 | 24,819,1060,1110 | 24,819,1060,1110 | 24,819,1060,1110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,819,1084,1110 > 24,819,1060,1110 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 9 | 9 | 嗯算吧 | ME | ME | text | NONE | true | 673,945,883,1079 | 672,944,904,1080 | 672,944,904,1080 | 672,944,904,1080 | 0,0,1084,2412 > 0,242,1084,2261 > 0,819,1084,1110 > 24,819,1060,1110 > 672,944,904,1080 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 10 | 10 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 426,1247,504,1289 | 24,1111,1060,1331 | 24,1111,1060,1331 | 24,1111,1060,1331 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1111,1084,1331 > 24,1111,1060,1331 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 11 | 11 | 准备吃晚饭啦 | ME | ME | text | NONE | true | 529,1166,883,1300 | 528,1165,904,1301 | 528,1165,904,1301 | 528,1165,904,1301 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1111,1084,1331 > 24,1111,1060,1331 > 528,1165,904,1301 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 12 | 12 | 2026年07月06日 17:46 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1362,709,1415 | 24,1332,1060,1625 | 24,1332,1060,1625 | 24,1332,1060,1625 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1332,1084,1625 > 24,1332,1060,1625 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 13 | 13 | 我也准备做饭了 | OTHER | OTHER | text | NONE | true | 201,1459,603,1593 | 180,1457,605,1595 | 180,1457,605,1595 | 180,1457,605,1595 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1332,1084,1625 > 24,1332,1060,1625 > 180,1457,605,1595 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 14 | 14 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 330,1762,408,1804 | 24,1626,1060,1846 | 24,1626,1060,1846 | 24,1626,1060,1846 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1626,1084,1846 > 24,1626,1060,1846 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 15 | 15 | 那你做啥好吃的？ | ME | ME | text | NONE | true | 433,1681,883,1815 | 432,1680,904,1816 | 432,1680,904,1816 | 432,1680,904,1816 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1626,1084,1846 > 24,1626,1060,1846 > 432,1680,904,1816 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 16 | 16 | 2026年07月06日 17:55 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1877,709,1930 | 24,1847,1060,2140 | 24,1847,1060,2140 | 24,1847,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1847,1084,2140 > 24,1847,1060,2140 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 17 | 17 | 米饭 | OTHER | OTHER | text | NONE | true | 201,1974,363,2108 | 180,1972,365,2110 | 180,1972,365,2110 | 180,1972,365,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1847,1084,2140 > 24,1847,1060,2140 > 180,1972,365,2110 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 4 | 4 | 忙完回来了[呲牙] | ME | ME | text | NONE | true | 525,358,883,493 | 524,357,904,494 | 524,357,904,494 | 524,357,904,494 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,524 > 24,242,1060,524 > 524,357,904,494 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 6 | 6 | 下班了 | OTHER | OTHER | text | NONE | true | 201,652,411,786 | 180,650,413,788 | 180,650,413,788 | 180,650,413,788 | 0,0,1084,2412 > 0,242,1084,2261 > 0,525,1084,818 > 24,525,1060,818 > 180,650,413,788 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 9 | 9 | 嗯算吧 | ME | ME | text | NONE | true | 673,945,883,1079 | 672,944,904,1080 | 672,944,904,1080 | 672,944,904,1080 | 0,0,1084,2412 > 0,242,1084,2261 > 0,819,1084,1110 > 24,819,1060,1110 > 672,944,904,1080 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 准备吃晚饭啦 | ME | ME | text | NONE | true | 529,1166,883,1300 | 528,1165,904,1301 | 528,1165,904,1301 | 528,1165,904,1301 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1111,1084,1331 > 24,1111,1060,1331 > 528,1165,904,1301 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 13 | 13 | 我也准备做饭了 | OTHER | OTHER | text | NONE | true | 201,1459,603,1593 | 180,1457,605,1595 | 180,1457,605,1595 | 180,1457,605,1595 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1332,1084,1625 > 24,1332,1060,1625 > 180,1457,605,1595 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 15 | 15 | 那你做啥好吃的？ | ME | ME | text | NONE | true | 433,1681,883,1815 | 432,1680,904,1816 | 432,1680,904,1816 | 432,1680,904,1816 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1626,1084,1846 > 24,1626,1060,1846 > 432,1680,904,1816 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 17 | 17 | 米饭 | OTHER | OTHER | text | NONE | true | 201,1974,363,2108 | 180,1972,365,2110 | 180,1972,365,2110 | 180,1972,365,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1847,1084,2140 > 24,1847,1060,2140 > 180,1972,365,2110 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n21069
- lastEffectiveMessageId: bubble-n21069
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
- userLikelyMistake: Waiting for cloud before showing usable wording.
- bestMove: [REDACTED_PRIVATE_CHAT]
- avoidMoves: do not block on cloud / do not mix active persona feedback into passive panel
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: 
- fallbackMove: 保持轻一点，让对方容易接。

## ReplyRoutes
- route id: cloud-playbook-passive-1
  name: 0
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 换个问菜的方式
- route id: cloud-playbook-passive-2
  name: 1
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 改为夸赞对方
- route id: cloud-playbook-passive-3
  name: 2
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 转移到其他话题

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 17
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
