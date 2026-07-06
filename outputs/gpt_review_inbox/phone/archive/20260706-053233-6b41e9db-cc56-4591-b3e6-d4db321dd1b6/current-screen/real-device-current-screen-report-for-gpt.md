# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783315950602
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
- capturedNodeCount: 66
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
- decisionSource: LOCAL_PLAYBOOK_FALLBACK_ACTIVE_EXPRESSION
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
- rawParsedNodeCount: 16
- metadataFilteredCount: 5
- candidateChatMessageCount: 11
- unknownSpeakerCount: 2
- effectiveMessageCount: 9
- effectiveMeCount: 5
- effectiveOtherCount: 4
- parsedMessageCount: 16
- meCount: 5
- otherCount: 4
- unknownCount: 2
- unknownRatio: 0.13
- systemCount: 5
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 0
- messageStatusArtifactCount: 5
- readReceiptCount: 5
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: NONE
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 4
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 下午你要上班了吗

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 2
- bubble_edge_left: 4
- read_receipt_metadata: 5
- bubble_edge_right: 5

### UNKNOWN details
- id: bubble-n1251
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n1285
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1544,709,1597
  parentBounds: 24,1514,1060,1807
  rowBounds: 24,1514,1060,1807
  bubbleBounds: 24,1514,1060,1807
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1514,1084,1807 > 24,1514,1060,1807
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度18.5℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][left][other 82% bubble_edge_left] 吃中饭没 rawNodeOrder=2 finalVisualOrder=2 rowBounds=180,242,461,376 textBounds=201,242,459,374 parentBounds=180,242,461,376 bubbleBounds=180,242,461,376 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=623 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,407,1060,627 textBounds=522,543,600,585 parentBounds=24,407,1060,627 bubbleBounds=24,407,1060,627 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] 打了个盹 rawNodeOrder=4 finalVisualOrder=4 rowBounds=624,461,904,597 textBounds=625,462,883,596 parentBounds=624,461,904,597 bubbleBounds=624,461,904,597 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=624 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=5 finalVisualOrder=5 rowBounds=24,628,1060,848 textBounds=426,764,504,806 parentBounds=24,628,1060,848 bubbleBounds=24,628,1060,848 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m006][right][me 82% bubble_edge_right] 你们才吃完啊 rawNodeOrder=6 finalVisualOrder=6 rowBounds=528,682,904,818 textBounds=529,683,883,817 parentBounds=528,682,904,818 bubbleBounds=528,682,904,818 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=528 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m007][left][other 82% bubble_edge_left] 准备吃饭 rawNodeOrder=7 finalVisualOrder=7 rowBounds=180,903,461,1041 textBounds=201,905,459,1039 parentBounds=180,903,461,1041 bubbleBounds=180,903,461,1041 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=623 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m008][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=8 finalVisualOrder=8 rowBounds=24,1072,1060,1292 textBounds=666,1208,744,1250 parentBounds=24,1072,1060,1292 bubbleBounds=24,1072,1060,1292 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m009][right][me 82% bubble_edge_right] 好 rawNodeOrder=9 finalVisualOrder=9 rowBounds=768,1126,904,1262 textBounds=769,1127,883,1261 parentBounds=768,1126,904,1262 bubbleBounds=768,1126,904,1262 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=768 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m010][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=10 finalVisualOrder=10 rowBounds=24,1293,1060,1513 textBounds=474,1429,552,1471 parentBounds=24,1293,1060,1513 bubbleBounds=24,1293,1060,1513 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m011][right][me 82% bubble_edge_right] 你先吃饭吧 rawNodeOrder=11 finalVisualOrder=11 rowBounds=576,1347,904,1483 textBounds=577,1348,883,1482 parentBounds=576,1347,904,1483 bubbleBounds=576,1347,904,1483 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=576 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m012][unknown][unknown 30% ambiguous_center_bounds] 2026年07月06日 13:20 rawNodeOrder=12 finalVisualOrder=12 rowBounds=24,1514,1060,1807 textBounds=375,1544,709,1597 parentBounds=24,1514,1060,1807 bubbleBounds=24,1514,1060,1807 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m013][left][other 82% bubble_edge_left] 你中午要休息吗 rawNodeOrder=13 finalVisualOrder=13 rowBounds=180,1639,605,1777 textBounds=201,1641,603,1775 parentBounds=180,1639,605,1777 bubbleBounds=180,1639,605,1777 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=479 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m014][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=14 finalVisualOrder=14 rowBounds=24,1808,1060,2028 textBounds=522,1944,600,1986 parentBounds=24,1808,1060,2028 bubbleBounds=24,1808,1060,2028 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m015][right][me 82% bubble_edge_right] 休息过了 rawNodeOrder=15 finalVisualOrder=15 rowBounds=624,1862,904,1998 textBounds=625,1863,883,1997 parentBounds=624,1862,904,1998 bubbleBounds=624,1862,904,1998 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=624 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m016][left][other 82% bubble_edge_left] 下午你要上班了吗 rawNodeOrder=16 finalVisualOrder=16 rowBounds=180,2083,653,2221 textBounds=201,2085,651,2219 parentBounds=180,2083,653,2221 bubbleBounds=180,2083,653,2221 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=431 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度18.5℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 吃中饭没 | OTHER | OTHER | text | NONE | true | 201,242,459,374 | 180,242,461,376 | 180,242,461,376 | 180,242,461,376 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,406 > 24,242,1060,406 > 180,242,461,376 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 522,543,600,585 | 24,407,1060,627 | 24,407,1060,627 | 24,407,1060,627 | 0,0,1084,2412 > 0,242,1084,2261 > 0,407,1084,627 > 24,407,1060,627 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | 打了个盹 | ME | ME | text | NONE | true | 625,462,883,596 | 624,461,904,597 | 624,461,904,597 | 624,461,904,597 | 0,0,1084,2412 > 0,242,1084,2261 > 0,407,1084,627 > 24,407,1060,627 > 624,461,904,597 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 426,764,504,806 | 24,628,1060,848 | 24,628,1060,848 | 24,628,1060,848 | 0,0,1084,2412 > 0,242,1084,2261 > 0,628,1084,848 > 24,628,1060,848 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 6 | 6 | 你们才吃完啊 | ME | ME | text | NONE | true | 529,683,883,817 | 528,682,904,818 | 528,682,904,818 | 528,682,904,818 | 0,0,1084,2412 > 0,242,1084,2261 > 0,628,1084,848 > 24,628,1060,848 > 528,682,904,818 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 7 | 7 | 准备吃饭 | OTHER | OTHER | text | NONE | true | 201,905,459,1039 | 180,903,461,1041 | 180,903,461,1041 | 180,903,461,1041 | 0,0,1084,2412 > 0,242,1084,2261 > 0,849,1084,1071 > 24,849,1060,1071 > 180,903,461,1041 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 8 | 8 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 666,1208,744,1250 | 24,1072,1060,1292 | 24,1072,1060,1292 | 24,1072,1060,1292 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1072,1084,1292 > 24,1072,1060,1292 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 9 | 9 | 好 | ME | ME | text | NONE | true | 769,1127,883,1261 | 768,1126,904,1262 | 768,1126,904,1262 | 768,1126,904,1262 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1072,1084,1292 > 24,1072,1060,1292 > 768,1126,904,1262 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 10 | 10 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 474,1429,552,1471 | 24,1293,1060,1513 | 24,1293,1060,1513 | 24,1293,1060,1513 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1293,1084,1513 > 24,1293,1060,1513 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 11 | 11 | 你先吃饭吧 | ME | ME | text | NONE | true | 577,1348,883,1482 | 576,1347,904,1483 | 576,1347,904,1483 | 576,1347,904,1483 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1293,1084,1513 > 24,1293,1060,1513 > 576,1347,904,1483 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 12 | 12 | 2026年07月06日 13:20 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1544,709,1597 | 24,1514,1060,1807 | 24,1514,1060,1807 | 24,1514,1060,1807 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1514,1084,1807 > 24,1514,1060,1807 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 13 | 13 | 你中午要休息吗 | OTHER | OTHER | text | NONE | true | 201,1641,603,1775 | 180,1639,605,1777 | 180,1639,605,1777 | 180,1639,605,1777 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1514,1084,1807 > 24,1514,1060,1807 > 180,1639,605,1777 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 14 | 14 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 522,1944,600,1986 | 24,1808,1060,2028 | 24,1808,1060,2028 | 24,1808,1060,2028 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1808,1084,2028 > 24,1808,1060,2028 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 15 | 15 | 休息过了 | ME | ME | text | NONE | true | 625,1863,883,1997 | 624,1862,904,1998 | 624,1862,904,1998 | 624,1862,904,1998 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1808,1084,2028 > 24,1808,1060,2028 > 624,1862,904,1998 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 16 | 16 | 下午你要上班了吗 | OTHER | OTHER | text | NONE | true | 201,2085,651,2219 | 180,2083,653,2221 | 180,2083,653,2221 | 180,2083,653,2221 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2029,1084,2251 > 24,2029,1060,2251 > 180,2083,653,2221 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2 | 2 | 吃中饭没 | OTHER | OTHER | text | NONE | true | 201,242,459,374 | 180,242,461,376 | 180,242,461,376 | 180,242,461,376 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,406 > 24,242,1060,406 > 180,242,461,376 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 4 | 4 | 打了个盹 | ME | ME | text | NONE | true | 625,462,883,596 | 624,461,904,597 | 624,461,904,597 | 624,461,904,597 | 0,0,1084,2412 > 0,242,1084,2261 > 0,407,1084,627 > 24,407,1060,627 > 624,461,904,597 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 6 | 6 | 你们才吃完啊 | ME | ME | text | NONE | true | 529,683,883,817 | 528,682,904,818 | 528,682,904,818 | 528,682,904,818 | 0,0,1084,2412 > 0,242,1084,2261 > 0,628,1084,848 > 24,628,1060,848 > 528,682,904,818 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 7 | 7 | 准备吃饭 | OTHER | OTHER | text | NONE | true | 201,905,459,1039 | 180,903,461,1041 | 180,903,461,1041 | 180,903,461,1041 | 0,0,1084,2412 > 0,242,1084,2261 > 0,849,1084,1071 > 24,849,1060,1071 > 180,903,461,1041 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 9 | 9 | 好 | ME | ME | text | NONE | true | 769,1127,883,1261 | 768,1126,904,1262 | 768,1126,904,1262 | 768,1126,904,1262 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1072,1084,1292 > 24,1072,1060,1292 > 768,1126,904,1262 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 你先吃饭吧 | ME | ME | text | NONE | true | 577,1348,883,1482 | 576,1347,904,1483 | 576,1347,904,1483 | 576,1347,904,1483 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1293,1084,1513 > 24,1293,1060,1513 > 576,1347,904,1483 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 13 | 13 | 你中午要休息吗 | OTHER | OTHER | text | NONE | true | 201,1641,603,1775 | 180,1639,605,1777 | 180,1639,605,1777 | 180,1639,605,1777 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1514,1084,1807 > 24,1514,1060,1807 > 180,1639,605,1777 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 15 | 15 | 休息过了 | ME | ME | text | NONE | true | 625,1863,883,1997 | 624,1862,904,1998 | 624,1862,904,1998 | 624,1862,904,1998 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1808,1084,2028 > 24,1808,1060,2028 > 624,1862,904,1998 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 16 | 16 | 下午你要上班了吗 | OTHER | OTHER | text | NONE | true | 201,2085,651,2219 | 180,2083,653,2221 | 180,2083,653,2221 | 180,2083,653,2221 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2029,1084,2251 > 24,2029,1060,2251 > 180,2083,653,2221 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n1296
- lastEffectiveMessageId: bubble-n1296
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
- influenceIntensity: MEDIUM
- riskLevel: LOW
- riskWarning: none
- fallbackMove: 保持轻一点，让对方容易接。

## ReplyRoutes
- route id: playbook-start-topic
  name: 轻开场
  routeType: WARM_UP
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 保持轻一点，让对方容易接。
- route id: playbook-low-pressure-expression
  name: 低压表达
  routeType: SELF_STORY
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 保持轻一点，让对方容易接。
- route id: playbook-arc-reveal
  name: 人物弧光
  routeType: ARC_REVEAL
  message: [REDACTED_PRIVATE_CHAT]
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: none
  fallbackMove: 说短一点，不要连续解释自己。

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 16
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
