# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783298774918
- scenarioName: real_device_last_me
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: ME
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: ME
- actualLastSpeakerFromPreAnalysisSnapshot: ME
- actualLastSpeakerFromDecisionSnapshot: ME
- actualLastSpeakerFromPostPanelSnapshot: ME
- expectedDecisionType: WAIT
- actualDecisionType: PRE_ANALYSIS_CONTAMINATED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: FUNCTIONAL_PASS_ASSERTION_FAIL
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: false
- failureReason: decision_type_mismatch
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
- capturedNodeCount: 58
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
- cloudSkippedReason: EXPRESS_SELF_BLOCK_UNTRUSTED_SNAPSHOT
- decisionSource: EXPRESS_SELF_ELIGIBILITY_BLOCKED
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
- failureCategory: decision_type_mismatch
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
- rawParsedNodeCount: 15
- metadataFilteredCount: 3
- candidateChatMessageCount: 12
- unknownSpeakerCount: 5
- effectiveMessageCount: 7
- effectiveMeCount: 4
- effectiveOtherCount: 3
- parsedMessageCount: 15
- meCount: 4
- otherCount: 3
- unknownCount: 5
- unknownRatio: 0.33
- systemCount: 3
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 0
- messageStatusArtifactCount: 3
- readReceiptCount: 3
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: NONE
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 1
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 天气预报说下午有雨

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 5
- bubble_edge_left: 3
- read_receipt_metadata: 3
- bubble_edge_right: 4

### UNKNOWN details
- id: bubble-n5880
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n5887
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 201,242,808,426
  parentBounds: 180,242,810,428
  rowBounds: 180,242,810,428
  bubbleBounds: 180,242,810,428
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,458 > 24,242,1060,458 > 180,242,810,428
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58
- id: bubble-n5896
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 276,737,883,931
  parentBounds: 275,736,904,932
  rowBounds: 275,736,904,932
  bubbleBounds: 275,736,904,932
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,682,1084,962 > 24,682,1060,962 > 275,736,904,932
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58
- id: bubble-n5909
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1435,709,1488
  parentBounds: 24,1405,1060,1698
  rowBounds: 24,1405,1060,1698
  bubbleBounds: 24,1405,1060,1698
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1405,1084,1698 > 24,1405,1060,1698
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n5915
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 522,1835,600,1877
  parentBounds: 24,1699,1060,1919
  rowBounds: 24,1699,1060,1919
  bubbleBounds: 24,1699,1060,1919
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1699,1084,1919 > 24,1699,1060,1919
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度13.7℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][unknown][unknown 30% ambiguous_center_bounds] 部队是你第二个家了，吃早餐了没 rawNodeOrder=2 finalVisualOrder=2 rowBounds=180,242,810,428 textBounds=201,242,808,426 parentBounds=180,242,810,428 bubbleBounds=180,242,810,428 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m003][left][other 82% bubble_edge_left] 我们这里又下雨了 rawNodeOrder=3 finalVisualOrder=3 rowBounds=180,513,653,651 textBounds=201,515,651,649 parentBounds=180,513,653,651 bubbleBounds=180,513,653,651 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=431 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m004][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=4 finalVisualOrder=4 rowBounds=24,682,1060,962 textBounds=173,878,251,920 parentBounds=24,682,1060,962 bubbleBounds=24,682,1060,962 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m005][unknown][unknown 30% ambiguous_center_bounds] 是的，每年年假也就三四十天回去 rawNodeOrder=5 finalVisualOrder=5 rowBounds=275,736,904,932 textBounds=276,737,883,931 parentBounds=275,736,904,932 bubbleBounds=275,736,904,932 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m006][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=6 finalVisualOrder=6 rowBounds=24,963,1060,1183 textBounds=255,1099,333,1141 parentBounds=24,963,1060,1183 bubbleBounds=24,963,1060,1183 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m007][right][me 82% bubble_edge_right] 吃过了，我们7点早饭 rawNodeOrder=7 finalVisualOrder=7 rowBounds=357,1017,904,1153 textBounds=358,1018,883,1152 parentBounds=357,1017,904,1153 bubbleBounds=357,1017,904,1153 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=357 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m008][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=8 finalVisualOrder=8 rowBounds=24,1184,1060,1404 textBounds=378,1320,456,1362 parentBounds=24,1184,1060,1404 bubbleBounds=24,1184,1060,1404 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m009][right][me 82% bubble_edge_right] 下雨天适合赖床 rawNodeOrder=9 finalVisualOrder=9 rowBounds=480,1238,904,1374 textBounds=481,1239,883,1373 parentBounds=480,1238,904,1374 bubbleBounds=480,1238,904,1374 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=480 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m010][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 08:38 rawNodeOrder=10 finalVisualOrder=10 rowBounds=24,1405,1060,1698 textBounds=375,1435,709,1488 parentBounds=24,1405,1060,1698 bubbleBounds=24,1405,1060,1698 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m011][left][other 82% bubble_edge_left] 你哪里下雨吗 rawNodeOrder=11 finalVisualOrder=11 rowBounds=180,1530,557,1668 textBounds=201,1532,555,1666 parentBounds=180,1530,557,1668 bubbleBounds=180,1530,557,1668 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=527 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m012][unknown][unknown 30% ambiguous_center_bounds] 送达 rawNodeOrder=12 finalVisualOrder=12 rowBounds=24,1699,1060,1919 textBounds=522,1835,600,1877 parentBounds=24,1699,1060,1919 bubbleBounds=24,1699,1060,1919 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m013][right][me 82% bubble_edge_right] 现在多云 rawNodeOrder=13 finalVisualOrder=13 rowBounds=624,1753,904,1889 textBounds=625,1754,883,1888 parentBounds=624,1753,904,1889 bubbleBounds=624,1753,904,1889 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=624 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m014][left][other 82% bubble_edge_left] 送达 rawNodeOrder=14 finalVisualOrder=14 rowBounds=24,1920,1060,2140 textBounds=282,2056,360,2098 parentBounds=24,1920,1060,2140 bubbleBounds=24,1920,1060,2140 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m015][right][me 82% bubble_edge_right] 天气预报说下午有雨 rawNodeOrder=15 finalVisualOrder=15 rowBounds=384,1974,904,2110 textBounds=385,1975,883,2109 parentBounds=384,1974,904,2110 bubbleBounds=384,1974,904,2110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=384 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度13.7℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 部队是你第二个家了，吃早餐了没 | UNKNOWN | UNKNOWN | text | NONE | false | 201,242,808,426 | 180,242,810,428 | 180,242,810,428 | 180,242,810,428 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,458 > 24,242,1060,458 > 180,242,810,428 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 3 | 3 | 我们这里又下雨了 | OTHER | OTHER | text | NONE | true | 201,515,651,649 | 180,513,653,651 | 180,513,653,651 | 180,513,653,651 | 0,0,1084,2412 > 0,242,1084,2261 > 0,459,1084,681 > 24,459,1060,681 > 180,513,653,651 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 4 | 4 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,878,251,920 | 24,682,1060,962 | 24,682,1060,962 | 24,682,1060,962 | 0,0,1084,2412 > 0,242,1084,2261 > 0,682,1084,962 > 24,682,1060,962 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 5 | 5 | 是的，每年年假也就三四十天回去 | UNKNOWN | UNKNOWN | text | NONE | false | 276,737,883,931 | 275,736,904,932 | 275,736,904,932 | 275,736,904,932 | 0,0,1084,2412 > 0,242,1084,2261 > 0,682,1084,962 > 24,682,1060,962 > 275,736,904,932 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 6 | 6 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 255,1099,333,1141 | 24,963,1060,1183 | 24,963,1060,1183 | 24,963,1060,1183 | 0,0,1084,2412 > 0,242,1084,2261 > 0,963,1084,1183 > 24,963,1060,1183 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 7 | 7 | 吃过了，我们7点早饭 | ME | ME | text | NONE | true | 358,1018,883,1152 | 357,1017,904,1153 | 357,1017,904,1153 | 357,1017,904,1153 | 0,0,1084,2412 > 0,242,1084,2261 > 0,963,1084,1183 > 24,963,1060,1183 > 357,1017,904,1153 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 8 | 8 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 378,1320,456,1362 | 24,1184,1060,1404 | 24,1184,1060,1404 | 24,1184,1060,1404 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1184,1084,1404 > 24,1184,1060,1404 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 9 | 9 | 下雨天适合赖床 | ME | ME | text | NONE | true | 481,1239,883,1373 | 480,1238,904,1374 | 480,1238,904,1374 | 480,1238,904,1374 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1184,1084,1404 > 24,1184,1060,1404 > 480,1238,904,1374 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 10 | 10 | 2026年07月06日 08:38 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1435,709,1488 | 24,1405,1060,1698 | 24,1405,1060,1698 | 24,1405,1060,1698 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1405,1084,1698 > 24,1405,1060,1698 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 11 | 11 | 你哪里下雨吗 | OTHER | OTHER | text | NONE | true | 201,1532,555,1666 | 180,1530,557,1668 | 180,1530,557,1668 | 180,1530,557,1668 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1405,1084,1698 > 24,1405,1060,1698 > 180,1530,557,1668 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 12 | 12 | 送达 | UNKNOWN | UNKNOWN | text | NONE | false | 522,1835,600,1877 | 24,1699,1060,1919 | 24,1699,1060,1919 | 24,1699,1060,1919 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1699,1084,1919 > 24,1699,1060,1919 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 13 | 13 | 现在多云 | ME | ME | text | NONE | true | 625,1754,883,1888 | 624,1753,904,1889 | 624,1753,904,1889 | 624,1753,904,1889 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1699,1084,1919 > 24,1699,1060,1919 > 624,1753,904,1889 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | 送达 | OTHER | OTHER | text | NONE | true | 282,2056,360,2098 | 24,1920,1060,2140 | 24,1920,1060,2140 | 24,1920,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1920,1084,2140 > 24,1920,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 15 | 15 | 天气预报说下午有雨 | ME | ME | text | NONE | true | 385,1975,883,2109 | 384,1974,904,2110 | 384,1974,904,2110 | 384,1974,904,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1920,1084,2140 > 24,1920,1060,2140 > 384,1974,904,2110 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 3 | 3 | 我们这里又下雨了 | OTHER | OTHER | text | NONE | true | 201,515,651,649 | 180,513,653,651 | 180,513,653,651 | 180,513,653,651 | 0,0,1084,2412 > 0,242,1084,2261 > 0,459,1084,681 > 24,459,1060,681 > 180,513,653,651 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 7 | 7 | 吃过了，我们7点早饭 | ME | ME | text | NONE | true | 358,1018,883,1152 | 357,1017,904,1153 | 357,1017,904,1153 | 357,1017,904,1153 | 0,0,1084,2412 > 0,242,1084,2261 > 0,963,1084,1183 > 24,963,1060,1183 > 357,1017,904,1153 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 9 | 9 | 下雨天适合赖床 | ME | ME | text | NONE | true | 481,1239,883,1373 | 480,1238,904,1374 | 480,1238,904,1374 | 480,1238,904,1374 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1184,1084,1404 > 24,1184,1060,1404 > 480,1238,904,1374 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 你哪里下雨吗 | OTHER | OTHER | text | NONE | true | 201,1532,555,1666 | 180,1530,557,1668 | 180,1530,557,1668 | 180,1530,557,1668 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1405,1084,1698 > 24,1405,1060,1698 > 180,1530,557,1668 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 13 | 13 | 现在多云 | ME | ME | text | NONE | true | 625,1754,883,1888 | 624,1753,904,1889 | 624,1753,904,1889 | 624,1753,904,1889 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1699,1084,1919 > 24,1699,1060,1919 > 624,1753,904,1889 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | 送达 | OTHER | OTHER | text | NONE | true | 282,2056,360,2098 | 24,1920,1060,2140 | 24,1920,1060,2140 | 24,1920,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1920,1084,2140 > 24,1920,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 15 | 15 | 天气预报说下午有雨 | ME | ME | text | NONE | true | 385,1975,883,2109 | 384,1974,904,2110 | 384,1974,904,2110 | 384,1974,904,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1920,1084,2140 > 24,1920,1060,2140 > 384,1974,904,2110 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n5922
- lastEffectiveMessageId: bubble-n5922
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: ME
- lastSpeaker: ME
- shouldReply: false
- decisionType: PRE_ANALYSIS_CONTAMINATED
- reason: 最后一句是你发的，先等她回，不要继续补话。

## ContextAssembler
- contextCompleteness.score: 100
- canDeepAnalyze: true
- missingTypes: 
- coCreationOpportunity.exists: true
- coCreationOpportunity.type: SHARED_EXPECTATION
- unfinishedMeaning: 双方正在试探一种共同节奏。
- currentSceneSummary: [REDACTED_PRIVATE_CHAT]

## TacticalDecision
- decisionType: PRE_ANALYSIS_CONTAMINATED
- situation: [REDACTED_PRIVATE_CHAT]
- coreInsight: [REDACTED_PRIVATE_CHAT]
- userLikelyMistake: Forcing self-expression from an unsupported, stale, or recent LAST_ME state.
- bestMove: [REDACTED_PRIVATE_CHAT]
- avoidMoves: do not show active routes / do not call cloud / do not reuse stale snapshot
- influenceIntensity: MEDIUM
- riskLevel: MEDIUM
- riskWarning: none
- fallbackMove: 说短一点，不要连续解释自己。

## ReplyRoutes
- routes: empty
- reason: WAIT or blocked by missing voice/context/unknown speaker.

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 15
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
