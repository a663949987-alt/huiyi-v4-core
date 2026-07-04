# Real Device Current Screen Evidence Pack

- overall_result: PASS
- realDeviceFunctionalSmoke: PASS
- scenarioAssertionResult: PASS
- currentOverallResult: PASS
- generatedAt: 1783155024734
- scenarioName: real_device_last_other
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: OTHER
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: OTHER
- actualLastSpeakerFromPreAnalysisSnapshot: OTHER
- actualLastSpeakerFromDecisionSnapshot: OTHER
- actualLastSpeakerFromPostPanelSnapshot: OTHER
- expectedDecisionType: NO_FIXED_EXPECTATION
- actualDecisionType: CONTEXT_REQUIRED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: none
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: true
- failureReason: none
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
- capturedNodeCount: 57
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
- cloudLatencyMs: 32421
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
- visualSpeakerFallbackCount: 2
- conflictCount: 0
- failureCategory: none
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
- rawParsedNodeCount: 14
- metadataFilteredCount: 4
- candidateChatMessageCount: 10
- unknownSpeakerCount: 1
- effectiveMessageCount: 7
- effectiveMeCount: 4
- effectiveOtherCount: 3
- parsedMessageCount: 14
- meCount: 4
- otherCount: 5
- unknownCount: 1
- unknownRatio: 0.07
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
- lastEffectiveMessagePreview: 我没有工作

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 1
- visual_projected_left: 2
- read_receipt_metadata: 4
- bubble_edge_right: 4
- bubble_edge_left: 3

### UNKNOWN details
- id: bubble-n220
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 463,193,621,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度5.6℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=463,193,621,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=428,111,656,240 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][left][other 75% visual_projected_left] 我一个女儿，女儿有三个小孩 rawNodeOrder=2 finalVisualOrder=2 rowBounds=180,242,810,401 textBounds=201,242,808,399 parentBounds=180,242,810,401 bubbleBounds=180,242,810,401 projectedBox=180,242,810,401 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_left
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,432,1060,653 textBounds=422,569,500,611 parentBounds=24,432,1060,653 bubbleBounds=24,432,1060,653 projectedBox=24,432,1060,653 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] 三胞胎吗？[惊恐] rawNodeOrder=4 finalVisualOrder=4 rowBounds=524,486,904,623 textBounds=525,487,883,622 parentBounds=524,486,904,623 bubbleBounds=524,486,904,623 projectedBox=524,486,904,623 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=524 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][left][other 82% bubble_edge_left] 一个男儿两个女孩子 rawNodeOrder=5 finalVisualOrder=5 rowBounds=180,708,701,846 textBounds=201,710,699,844 parentBounds=180,708,701,846 bubbleBounds=180,708,701,846 projectedBox=180,708,701,846 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=383 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m006][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=6 finalVisualOrder=6 rowBounds=24,877,1060,1098 textBounds=374,1014,452,1056 parentBounds=24,877,1060,1098 bubbleBounds=24,877,1060,1098 projectedBox=24,877,1060,1098 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m007][right][me 82% bubble_edge_right] 奥，我了解了[呲牙] rawNodeOrder=7 finalVisualOrder=7 rowBounds=476,931,904,1068 textBounds=477,932,883,1067 parentBounds=476,931,904,1068 bubbleBounds=476,931,904,1068 projectedBox=476,931,904,1068 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=476 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m008][left][other 82% bubble_edge_left] 双胞胎姐妹 rawNodeOrder=8 finalVisualOrder=8 rowBounds=180,1153,509,1291 textBounds=201,1155,507,1289 parentBounds=180,1153,509,1291 bubbleBounds=180,1153,509,1291 projectedBox=180,1153,509,1291 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=575 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m009][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=9 finalVisualOrder=9 rowBounds=24,1322,1060,1542 textBounds=426,1458,504,1500 parentBounds=24,1322,1060,1542 bubbleBounds=24,1322,1060,1542 projectedBox=24,1322,1060,1542 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m010][right][me 82% bubble_edge_right] 哦哦，挺好的 rawNodeOrder=10 finalVisualOrder=10 rowBounds=528,1376,904,1512 textBounds=529,1377,883,1511 parentBounds=528,1376,904,1512 bubbleBounds=528,1376,904,1512 projectedBox=528,1376,904,1512 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=528 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m011][left][other 75% visual_projected_left] 男孩子11岁，女孩子2岁半 rawNodeOrder=11 finalVisualOrder=11 rowBounds=180,1597,810,1795 textBounds=201,1599,808,1793 parentBounds=180,1597,810,1795 bubbleBounds=180,1597,810,1795 projectedBox=180,1597,810,1795 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_left
[m012][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=12 finalVisualOrder=12 rowBounds=24,1826,1060,2047 textBounds=217,1963,295,2005 parentBounds=24,1826,1060,2047 bubbleBounds=24,1826,1060,2047 projectedBox=24,1826,1060,2047 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m013][right][me 82% bubble_edge_right] 外孙子 外孙女都有了[呲牙] rawNodeOrder=13 finalVisualOrder=13 rowBounds=319,1880,904,2017 textBounds=320,1881,883,2016 parentBounds=319,1880,904,2017 bubbleBounds=319,1880,904,2017 projectedBox=319,1880,904,2017 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=319 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m014][left][other 82% bubble_edge_left] 我没有工作 rawNodeOrder=14 finalVisualOrder=14 rowBounds=180,2102,509,2240 textBounds=201,2104,507,2238 parentBounds=180,2102,509,2240 bubbleBounds=180,2102,509,2240 projectedBox=180,2102,509,2240 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=575 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度5.6℃ | UNKNOWN | UNKNOWN | text | NONE | false | 463,193,621,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | 428,111,656,240 | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 我一个女儿，女儿有三个小孩 | OTHER | OTHER | text | NONE | false | 201,242,808,399 | 180,242,810,401 | 180,242,810,401 | 180,242,810,401 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,431 > 24,242,1060,431 > 180,242,810,401 | unknown | left | 180,242,810,401 | 75 | visual_projected_left | false | none | true | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 422,569,500,611 | 24,432,1060,653 | 24,432,1060,653 | 24,432,1060,653 | 0,0,1084,2412 > 0,242,1084,2261 > 0,432,1084,653 > 24,432,1060,653 | left | unknown | 24,432,1060,653 | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | 三胞胎吗？[惊恐] | ME | ME | text | NONE | true | 525,487,883,622 | 524,486,904,623 | 524,486,904,623 | 524,486,904,623 | 0,0,1084,2412 > 0,242,1084,2261 > 0,432,1084,653 > 24,432,1060,653 > 524,486,904,623 | right | right | 524,486,904,623 | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 一个男儿两个女孩子 | OTHER | OTHER | text | NONE | true | 201,710,699,844 | 180,708,701,846 | 180,708,701,846 | 180,708,701,846 | 0,0,1084,2412 > 0,242,1084,2261 > 0,654,1084,876 > 24,654,1060,876 > 180,708,701,846 | left | left | 180,708,701,846 | 82 | bubble_edge_left | false | none | false | false |
| 6 | 6 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 374,1014,452,1056 | 24,877,1060,1098 | 24,877,1060,1098 | 24,877,1060,1098 | 0,0,1084,2412 > 0,242,1084,2261 > 0,877,1084,1098 > 24,877,1060,1098 | left | unknown | 24,877,1060,1098 | 100 | read_receipt_metadata | false | none | false | false |
| 7 | 7 | 奥，我了解了[呲牙] | ME | ME | text | NONE | true | 477,932,883,1067 | 476,931,904,1068 | 476,931,904,1068 | 476,931,904,1068 | 0,0,1084,2412 > 0,242,1084,2261 > 0,877,1084,1098 > 24,877,1060,1098 > 476,931,904,1068 | right | right | 476,931,904,1068 | 82 | bubble_edge_right | false | none | false | false |
| 8 | 8 | 双胞胎姐妹 | OTHER | OTHER | text | NONE | true | 201,1155,507,1289 | 180,1153,509,1291 | 180,1153,509,1291 | 180,1153,509,1291 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1099,1084,1321 > 24,1099,1060,1321 > 180,1153,509,1291 | left | left | 180,1153,509,1291 | 82 | bubble_edge_left | false | none | false | false |
| 9 | 9 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 426,1458,504,1500 | 24,1322,1060,1542 | 24,1322,1060,1542 | 24,1322,1060,1542 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1322,1084,1542 > 24,1322,1060,1542 | left | unknown | 24,1322,1060,1542 | 100 | read_receipt_metadata | false | none | false | false |
| 10 | 10 | 哦哦，挺好的 | ME | ME | text | NONE | true | 529,1377,883,1511 | 528,1376,904,1512 | 528,1376,904,1512 | 528,1376,904,1512 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1322,1084,1542 > 24,1322,1060,1542 > 528,1376,904,1512 | right | right | 528,1376,904,1512 | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 男孩子11岁，女孩子2岁半 | OTHER | OTHER | text | NONE | false | 201,1599,808,1793 | 180,1597,810,1795 | 180,1597,810,1795 | 180,1597,810,1795 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1543,1084,1825 > 24,1543,1060,1825 > 180,1597,810,1795 | unknown | left | 180,1597,810,1795 | 75 | visual_projected_left | false | none | true | false |
| 12 | 12 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 217,1963,295,2005 | 24,1826,1060,2047 | 24,1826,1060,2047 | 24,1826,1060,2047 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1826,1084,2047 > 24,1826,1060,2047 | left | unknown | 24,1826,1060,2047 | 100 | read_receipt_metadata | false | none | false | false |
| 13 | 13 | 外孙子 外孙女都有了[呲牙] | ME | ME | text | NONE | true | 320,1881,883,2016 | 319,1880,904,2017 | 319,1880,904,2017 | 319,1880,904,2017 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1826,1084,2047 > 24,1826,1060,2047 > 319,1880,904,2017 | right | right | 319,1880,904,2017 | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | 我没有工作 | OTHER | OTHER | text | NONE | true | 201,2104,507,2238 | 180,2102,509,2240 | 180,2102,509,2240 | 180,2102,509,2240 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2048,1084,2261 > 24,2048,1060,2261 > 180,2102,509,2240 | left | left | 180,2102,509,2240 | 82 | bubble_edge_left | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 4 | 4 | 三胞胎吗？[惊恐] | ME | ME | text | NONE | true | 525,487,883,622 | 524,486,904,623 | 524,486,904,623 | 524,486,904,623 | 0,0,1084,2412 > 0,242,1084,2261 > 0,432,1084,653 > 24,432,1060,653 > 524,486,904,623 | right | right | 524,486,904,623 | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 一个男儿两个女孩子 | OTHER | OTHER | text | NONE | true | 201,710,699,844 | 180,708,701,846 | 180,708,701,846 | 180,708,701,846 | 0,0,1084,2412 > 0,242,1084,2261 > 0,654,1084,876 > 24,654,1060,876 > 180,708,701,846 | left | left | 180,708,701,846 | 82 | bubble_edge_left | false | none | false | false |
| 7 | 7 | 奥，我了解了[呲牙] | ME | ME | text | NONE | true | 477,932,883,1067 | 476,931,904,1068 | 476,931,904,1068 | 476,931,904,1068 | 0,0,1084,2412 > 0,242,1084,2261 > 0,877,1084,1098 > 24,877,1060,1098 > 476,931,904,1068 | right | right | 476,931,904,1068 | 82 | bubble_edge_right | false | none | false | false |
| 8 | 8 | 双胞胎姐妹 | OTHER | OTHER | text | NONE | true | 201,1155,507,1289 | 180,1153,509,1291 | 180,1153,509,1291 | 180,1153,509,1291 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1099,1084,1321 > 24,1099,1060,1321 > 180,1153,509,1291 | left | left | 180,1153,509,1291 | 82 | bubble_edge_left | false | none | false | false |
| 10 | 10 | 哦哦，挺好的 | ME | ME | text | NONE | true | 529,1377,883,1511 | 528,1376,904,1512 | 528,1376,904,1512 | 528,1376,904,1512 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1322,1084,1542 > 24,1322,1060,1542 > 528,1376,904,1512 | right | right | 528,1376,904,1512 | 82 | bubble_edge_right | false | none | false | false |
| 13 | 13 | 外孙子 外孙女都有了[呲牙] | ME | ME | text | NONE | true | 320,1881,883,2016 | 319,1880,904,2017 | 319,1880,904,2017 | 319,1880,904,2017 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1826,1084,2047 > 24,1826,1060,2047 > 319,1880,904,2017 | right | right | 319,1880,904,2017 | 82 | bubble_edge_right | false | none | false | false |
| 14 | 14 | 我没有工作 | OTHER | OTHER | text | NONE | true | 201,2104,507,2238 | 180,2102,509,2240 | 180,2102,509,2240 | 180,2102,509,2240 | 0,0,1084,2412 > 0,242,1084,2261 > 0,2048,1084,2261 > 24,2048,1060,2261 > 180,2102,509,2240 | left | left | 180,2102,509,2240 | 82 | bubble_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n262
- lastEffectiveMessageId: bubble-n262
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: true
- decisionType: CONTEXT_REQUIRED
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
- decisionType: CONTEXT_REQUIRED
- situation: [REDACTED_PRIVATE_CHAT]
- coreInsight: [REDACTED_PRIVATE_CHAT]
- userLikelyMistake: 在没有分清内容来源或含义时生成回复。
- bestMove: [REDACTED_PRIVATE_CHAT]
- avoidMoves: 不要调用模型 / 不要猜内容 / 不要强行深聊
- influenceIntensity: LOW
- riskLevel: MEDIUM
- riskWarning: 当前屏幕存在边界不清的聊天气泡，不允许高置信度生成。
- fallbackMove: 先确认这条内容是什么意思。

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
- ContextRequiredCard shown: true
