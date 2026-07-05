# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783241595493
- scenarioName: real_device_last_me
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: ME
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: ME
- actualLastSpeakerFromPreAnalysisSnapshot: ME
- actualLastSpeakerFromDecisionSnapshot: ME
- actualLastSpeakerFromPostPanelSnapshot: ME
- expectedDecisionType: WAIT
- actualDecisionType: NORMAL_REPLY
- expectedRouteCount: 0
- actualRouteCount: 5
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: FUNCTIONAL_PASS_ASSERTION_FAIL
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: false
- failureReason: decision_type_mismatch
- sample_source: real_device_accessibility
- appPackage: com.xiaoenai.app
- windowTitle: 华为桌面
- preAnalysisWindowTitle: 华为桌面
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
- failureCategory: decision_type_mismatch
- userCorrectionProvided: false
- correctedLastSpeaker: none
- correctedMessageId: none

## Snapshot Phase Separation

- preAnalysisSnapshotAvailable: true
- preAnalysisWindowTitle: 华为桌面
- preAnalysisSnapshotTrusted: true
- preAnalysisWindowTitleSource: TARGET_PRE_ANALYSIS
- preAnalysisResultPanelVisible: false
- decisionSnapshotAvailable: true
- postPanelSnapshotAvailable: true
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false

## 解析结果
- rawParsedNodeCount: 19
- metadataFilteredCount: 3
- candidateChatMessageCount: 16
- unknownSpeakerCount: 8
- effectiveMessageCount: 8
- effectiveMeCount: 6
- effectiveOtherCount: 2
- parsedMessageCount: 19
- meCount: 6
- otherCount: 2
- unknownCount: 8
- unknownRatio: 0.42
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
- statusArtifactsAttachedToMessageCount: 2
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 我这会工作都忙完了[Yeah]

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 8
- read_receipt_metadata: 3
- bubble_edge_right: 6
- bubble_edge_left: 2

### UNKNOWN details
- id: bubble-n4951
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n4956
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,242,709,267
  parentBounds: 24,242,1060,471
  rowBounds: 24,242,1060,471
  bubbleBounds: 24,242,1060,471
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,471 > 24,242,1060,471
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n4964
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 276,527,883,721
  parentBounds: 275,526,904,722
  rowBounds: 275,526,904,722
  bubbleBounds: 275,526,904,722
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,472,1084,752 > 24,472,1060,752 > 275,526,904,722
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58
- id: bubble-n4967
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,783,709,836
  parentBounds: 24,753,1060,1044
  rowBounds: 24,753,1060,1044
  bubbleBounds: 24,753,1060,1044
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,753,1084,1044 > 24,753,1060,1044
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n4973
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1075,709,1128
  parentBounds: 24,1045,1060,1338
  rowBounds: 24,1045,1060,1338
  bubbleBounds: 24,1045,1060,1338
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1045,1084,1338 > 24,1045,1060,1338
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n4979
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1369,709,1422
  parentBounds: 24,1339,1060,1626
  rowBounds: 24,1339,1060,1626
  bubbleBounds: 24,1339,1060,1626
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n4985
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 474,1763,552,1805
  parentBounds: 24,1627,1060,1847
  rowBounds: 24,1627,1060,1847
  bubbleBounds: 24,1627,1060,1847
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1627,1084,1847 > 24,1627,1060,1847
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96
- id: bubble-n4990
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 375,1878,709,1931
  parentBounds: 24,1848,1060,2140
  rowBounds: 24,1848,1060,2140
  bubbleBounds: 24,1848,1060,2140
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度11.1℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][unknown][unknown 30% ambiguous_center_bounds] 2026年07月05日 11:53 rawNodeOrder=2 finalVisualOrder=2 rowBounds=24,242,1060,471 textBounds=375,242,709,267 parentBounds=24,242,1060,471 bubbleBounds=24,242,1060,471 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,242,1060,471 textBounds=610,378,688,420 parentBounds=24,242,1060,471 bubbleBounds=24,242,1060,471 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] [憨笑][呲牙] rawNodeOrder=4 finalVisualOrder=4 rowBounds=712,309,904,432 textBounds=713,310,883,431 parentBounds=712,309,904,432 bubbleBounds=712,309,904,432 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=712 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=5 finalVisualOrder=5 rowBounds=24,472,1060,752 textBounds=173,668,251,710 parentBounds=24,472,1060,752 bubbleBounds=24,472,1060,752 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m006][unknown][unknown 30% ambiguous_center_bounds] 那你现在是属于一个外人带3个娃？ rawNodeOrder=6 finalVisualOrder=6 rowBounds=275,526,904,722 textBounds=276,527,883,721 parentBounds=275,526,904,722 bubbleBounds=275,526,904,722 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m007][unknown][unknown 30% ambiguous_center_bounds] 2026年07月05日 12:01 rawNodeOrder=7 finalVisualOrder=7 rowBounds=24,753,1060,1044 textBounds=375,783,709,836 parentBounds=24,753,1060,1044 bubbleBounds=24,753,1060,1044 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m008][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=8 finalVisualOrder=8 rowBounds=24,753,1060,1044 textBounds=420,960,498,1002 parentBounds=24,753,1060,1044 bubbleBounds=24,753,1060,1044 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m009][right][me 82% bubble_edge_right] 1个人带3个娃 rawNodeOrder=9 finalVisualOrder=9 rowBounds=522,878,904,1014 textBounds=523,879,883,1013 parentBounds=522,878,904,1014 bubbleBounds=522,878,904,1014 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=522 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m010][unknown][unknown 30% ambiguous_center_bounds] 2026年07月05日 13:09 rawNodeOrder=10 finalVisualOrder=10 rowBounds=24,1045,1060,1338 textBounds=375,1075,709,1128 parentBounds=24,1045,1060,1338 bubbleBounds=24,1045,1060,1338 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m011][left][other 82% bubble_edge_left] 一个人带大小5个人 rawNodeOrder=11 finalVisualOrder=11 rowBounds=180,1170,680,1308 textBounds=201,1172,678,1306 parentBounds=180,1170,680,1308 bubbleBounds=180,1170,680,1308 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=404 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m012][unknown][unknown 30% ambiguous_center_bounds] 2026年07月05日 15:32 rawNodeOrder=12 finalVisualOrder=12 rowBounds=24,1339,1060,1626 textBounds=375,1369,709,1422 parentBounds=24,1339,1060,1626 bubbleBounds=24,1339,1060,1626 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m013][right][me 82% bubble_edge_right] 送达 rawNodeOrder=13 finalVisualOrder=13 rowBounds=24,1339,1060,1626 textBounds=662,1533,740,1575 parentBounds=24,1339,1060,1626 bubbleBounds=24,1339,1060,1626 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m014][right][me 82% bubble_edge_right] {1f631} rawNodeOrder=14 finalVisualOrder=14 rowBounds=764,1464,904,1587 textBounds=765,1465,883,1586 parentBounds=764,1464,904,1587 bubbleBounds=764,1464,904,1587 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=764 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m015][unknown][unknown 30% ambiguous_center_bounds] 送达 rawNodeOrder=15 finalVisualOrder=15 rowBounds=24,1627,1060,1847 textBounds=474,1763,552,1805 parentBounds=24,1627,1060,1847 bubbleBounds=24,1627,1060,1847 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m016][right][me 82% bubble_edge_right] 你够辛苦的 rawNodeOrder=16 finalVisualOrder=16 rowBounds=576,1681,904,1817 textBounds=577,1682,883,1816 parentBounds=576,1681,904,1817 bubbleBounds=576,1681,904,1817 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=576 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m017][unknown][unknown 30% ambiguous_center_bounds] 2026年07月05日 15:38 rawNodeOrder=17 finalVisualOrder=17 rowBounds=24,1848,1060,2140 textBounds=375,1878,709,1931 parentBounds=24,1848,1060,2140 bubbleBounds=24,1848,1060,2140 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=24 rightMargin=24 widthRatio=0.96 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m018][left][other 82% bubble_edge_left] 送达 rawNodeOrder=18 finalVisualOrder=18 rowBounds=24,1848,1060,2140 textBounds=230,2056,308,2098 parentBounds=24,1848,1060,2140 bubbleBounds=24,1848,1060,2140 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m019][right][me 82% bubble_edge_right] 我这会工作都忙完了[Yeah] rawNodeOrder=19 finalVisualOrder=19 rowBounds=332,1973,904,2110 textBounds=333,1974,883,2109 parentBounds=332,1973,904,2110 bubbleBounds=332,1973,904,2110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=332 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度11.1℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 2026年07月05日 11:53 | UNKNOWN | UNKNOWN | text | NONE | false | 375,242,709,267 | 24,242,1060,471 | 24,242,1060,471 | 24,242,1060,471 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,471 > 24,242,1060,471 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 610,378,688,420 | 24,242,1060,471 | 24,242,1060,471 | 24,242,1060,471 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,471 > 24,242,1060,471 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | [憨笑][呲牙] | ME | ME | text | NONE | true | 713,310,883,431 | 712,309,904,432 | 712,309,904,432 | 712,309,904,432 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,471 > 24,242,1060,471 > 712,309,904,432 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,668,251,710 | 24,472,1060,752 | 24,472,1060,752 | 24,472,1060,752 | 0,0,1084,2412 > 0,242,1084,2261 > 0,472,1084,752 > 24,472,1060,752 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 6 | 6 | 那你现在是属于一个外人带3个娃？ | UNKNOWN | UNKNOWN | text | NONE | false | 276,527,883,721 | 275,526,904,722 | 275,526,904,722 | 275,526,904,722 | 0,0,1084,2412 > 0,242,1084,2261 > 0,472,1084,752 > 24,472,1060,752 > 275,526,904,722 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 7 | 7 | 2026年07月05日 12:01 | UNKNOWN | UNKNOWN | text | NONE | false | 375,783,709,836 | 24,753,1060,1044 | 24,753,1060,1044 | 24,753,1060,1044 | 0,0,1084,2412 > 0,242,1084,2261 > 0,753,1084,1044 > 24,753,1060,1044 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 8 | 8 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 420,960,498,1002 | 24,753,1060,1044 | 24,753,1060,1044 | 24,753,1060,1044 | 0,0,1084,2412 > 0,242,1084,2261 > 0,753,1084,1044 > 24,753,1060,1044 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 9 | 9 | 1个人带3个娃 | ME | ME | text | NONE | true | 523,879,883,1013 | 522,878,904,1014 | 522,878,904,1014 | 522,878,904,1014 | 0,0,1084,2412 > 0,242,1084,2261 > 0,753,1084,1044 > 24,753,1060,1044 > 522,878,904,1014 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 10 | 10 | 2026年07月05日 13:09 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1075,709,1128 | 24,1045,1060,1338 | 24,1045,1060,1338 | 24,1045,1060,1338 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1045,1084,1338 > 24,1045,1060,1338 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 11 | 11 | 一个人带大小5个人 | OTHER | OTHER | text | NONE | true | 201,1172,678,1306 | 180,1170,680,1308 | 180,1170,680,1308 | 180,1170,680,1308 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1045,1084,1338 > 24,1045,1060,1338 > 180,1170,680,1308 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 12 | 12 | 2026年07月05日 15:32 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1369,709,1422 | 24,1339,1060,1626 | 24,1339,1060,1626 | 24,1339,1060,1626 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 13 | 13 | 送达 | ME | ME | text | NONE | true | 662,1533,740,1575 | 24,1339,1060,1626 | 24,1339,1060,1626 | 24,1339,1060,1626 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | {1f631} | ME | ME | text | NONE | true | 765,1465,883,1586 | 764,1464,904,1587 | 764,1464,904,1587 | 764,1464,904,1587 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626 > 764,1464,904,1587 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 15 | 15 | 送达 | UNKNOWN | UNKNOWN | text | NONE | false | 474,1763,552,1805 | 24,1627,1060,1847 | 24,1627,1060,1847 | 24,1627,1060,1847 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1627,1084,1847 > 24,1627,1060,1847 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 16 | 16 | 你够辛苦的 | ME | ME | text | NONE | true | 577,1682,883,1816 | 576,1681,904,1817 | 576,1681,904,1817 | 576,1681,904,1817 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1627,1084,1847 > 24,1627,1060,1847 > 576,1681,904,1817 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 17 | 17 | 2026年07月05日 15:38 | UNKNOWN | UNKNOWN | text | NONE | false | 375,1878,709,1931 | 24,1848,1060,2140 | 24,1848,1060,2140 | 24,1848,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 18 | 18 | 送达 | OTHER | OTHER | text | NONE | true | 230,2056,308,2098 | 24,1848,1060,2140 | 24,1848,1060,2140 | 24,1848,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 19 | 19 | 我这会工作都忙完了[Yeah] | ME | ME | text | NONE | true | 333,1974,883,2109 | 332,1973,904,2110 | 332,1973,904,2110 | 332,1973,904,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140 > 332,1973,904,2110 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 4 | 4 | [憨笑][呲牙] | ME | ME | text | NONE | true | 713,310,883,431 | 712,309,904,432 | 712,309,904,432 | 712,309,904,432 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,471 > 24,242,1060,471 > 712,309,904,432 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 9 | 9 | 1个人带3个娃 | ME | ME | text | NONE | true | 523,879,883,1013 | 522,878,904,1014 | 522,878,904,1014 | 522,878,904,1014 | 0,0,1084,2412 > 0,242,1084,2261 > 0,753,1084,1044 > 24,753,1060,1044 > 522,878,904,1014 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 一个人带大小5个人 | OTHER | OTHER | text | NONE | true | 201,1172,678,1306 | 180,1170,680,1308 | 180,1170,680,1308 | 180,1170,680,1308 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1045,1084,1338 > 24,1045,1060,1338 > 180,1170,680,1308 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 13 | 13 | 送达 | ME | ME | text | NONE | true | 662,1533,740,1575 | 24,1339,1060,1626 | 24,1339,1060,1626 | 24,1339,1060,1626 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | {1f631} | ME | ME | text | NONE | true | 765,1465,883,1586 | 764,1464,904,1587 | 764,1464,904,1587 | 764,1464,904,1587 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1339,1084,1626 > 24,1339,1060,1626 > 764,1464,904,1587 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 16 | 16 | 你够辛苦的 | ME | ME | text | NONE | true | 577,1682,883,1816 | 576,1681,904,1817 | 576,1681,904,1817 | 576,1681,904,1817 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1627,1084,1847 > 24,1627,1060,1847 > 576,1681,904,1817 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 18 | 18 | 送达 | OTHER | OTHER | text | NONE | true | 230,2056,308,2098 | 24,1848,1060,2140 | 24,1848,1060,2140 | 24,1848,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 19 | 19 | 我这会工作都忙完了[Yeah] | ME | ME | text | NONE | true | 333,1974,883,2109 | 332,1973,904,2110 | 332,1973,904,2110 | 332,1973,904,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1848,1084,2140 > 24,1848,1060,2140 > 332,1973,904,2110 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n4993
- lastEffectiveMessageId: bubble-n4993
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: ME
- lastSpeaker: ME
- shouldReply: false
- decisionType: NORMAL_REPLY
- reason: 最后一句是你发的，先等她回，不要继续补话。

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
- riskLevel: MEDIUM
- riskWarning: none
- fallbackMove: 说短一点，不要连续解释自己。

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
- route id: playbook-co-create
  name: 共创升维
  routeType: CO_CREATION
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: 保持轻一点，让对方容易接。
- route id: playbook-safe-withdraw
  name: 撤退
  routeType: COOL_DOWN
  message: [REDACTED_PRIVATE_CHAT]
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: none
  fallbackMove: 说短一点，不要连续解释自己。

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 19
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
