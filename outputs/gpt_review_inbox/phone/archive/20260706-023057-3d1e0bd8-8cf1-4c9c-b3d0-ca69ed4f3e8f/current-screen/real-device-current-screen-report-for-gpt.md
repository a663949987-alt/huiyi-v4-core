# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783305056833
- scenarioName: real_device_last_other
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: OTHER
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: OTHER
- actualLastSpeakerFromPreAnalysisSnapshot: OTHER
- actualLastSpeakerFromDecisionSnapshot: OTHER
- actualLastSpeakerFromPostPanelSnapshot: OTHER
- expectedDecisionType: NO_FIXED_EXPECTATION
- actualDecisionType: PRE_ANALYSIS_CONTAMINATED
- expectedRouteCount: 5
- actualRouteCount: 0
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: FUNCTIONAL_PASS_ASSERTION_FAIL
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: false
- failureReason: route_count_mismatch
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
- failureCategory: route_count_mismatch
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
- unknownSpeakerCount: 4
- effectiveMessageCount: 6
- effectiveMeCount: 4
- effectiveOtherCount: 2
- parsedMessageCount: 14
- meCount: 4
- otherCount: 2
- unknownCount: 4
- unknownRatio: 0.29
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
- lastEffectiveMessagePreview: 送达

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 4
- read_receipt_metadata: 4
- bubble_edge_right: 4
- bubble_edge_left: 2

### UNKNOWN details
- id: bubble-n656
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21
- id: bubble-n662
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 201,243,808,437
  parentBounds: 180,242,810,439
  rowBounds: 180,242,810,439
  bubbleBounds: 180,242,810,439
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,469 > 24,242,1060,469 > 180,242,810,439
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58
- id: bubble-n675
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 201,970,808,1164
  parentBounds: 180,968,810,1166
  rowBounds: 180,968,810,1166
  bubbleBounds: 180,968,810,1166
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,914,1084,1196 > 24,914,1060,1196 > 180,968,810,1166
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58
- id: bubble-n696
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 276,1915,883,2109
  parentBounds: 275,1914,904,2110
  rowBounds: 275,1914,904,2110
  bubbleBounds: 275,1914,904,2110
  ancestorBoundsChain: 0,0,1084,2412 > 0,242,1084,2261 > 0,1860,1084,2140 > 24,1860,1060,2140 > 275,1914,904,2110
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度16.1℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][unknown][unknown 30% ambiguous_center_bounds] 原来在一个学校读书，不是同学 rawNodeOrder=2 finalVisualOrder=2 rowBounds=180,242,810,439 textBounds=201,243,808,437 parentBounds=180,242,810,439 bubbleBounds=180,242,810,439 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m003][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=3 finalVisualOrder=3 rowBounds=24,470,1060,690 textBounds=330,606,408,648 parentBounds=24,470,1060,690 bubbleBounds=24,470,1060,690 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m004][right][me 82% bubble_edge_right] 奥，两个人是校友 rawNodeOrder=4 finalVisualOrder=4 rowBounds=432,524,904,660 textBounds=433,525,883,659 parentBounds=432,524,904,660 bubbleBounds=432,524,904,660 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=432 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m005][left][other 82% bubble_edge_left] 是的🙂‍↕️ rawNodeOrder=5 finalVisualOrder=5 rowBounds=180,745,425,883 textBounds=201,747,423,881 parentBounds=180,745,425,883 bubbleBounds=180,745,425,883 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=659 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m006][unknown][unknown 30% ambiguous_center_bounds] 你女儿多大了，是在上大学吗 rawNodeOrder=6 finalVisualOrder=6 rowBounds=180,968,810,1166 textBounds=201,970,808,1164 parentBounds=180,968,810,1166 bubbleBounds=180,968,810,1166 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m007][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=7 finalVisualOrder=7 rowBounds=24,1197,1060,1417 textBounds=468,1333,546,1375 parentBounds=24,1197,1060,1417 bubbleBounds=24,1197,1060,1417 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m008][right][me 82% bubble_edge_right] 25在读硕士 rawNodeOrder=8 finalVisualOrder=8 rowBounds=570,1251,904,1387 textBounds=571,1252,883,1386 parentBounds=570,1251,904,1387 bubbleBounds=570,1251,904,1387 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=570 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m009][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=9 finalVisualOrder=9 rowBounds=24,1418,1060,1638 textBounds=474,1554,552,1596 parentBounds=24,1418,1060,1638 bubbleBounds=24,1418,1060,1638 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m010][right][me 82% bubble_edge_right] 那也蛮好的 rawNodeOrder=10 finalVisualOrder=10 rowBounds=576,1472,904,1608 textBounds=577,1473,883,1607 parentBounds=576,1472,904,1608 bubbleBounds=576,1472,904,1608 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=576 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m011][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=11 finalVisualOrder=11 rowBounds=24,1639,1060,1859 textBounds=330,1775,408,1817 parentBounds=24,1639,1060,1859 bubbleBounds=24,1639,1060,1859 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m012][right][me 82% bubble_edge_right] 你家就是你比较累 rawNodeOrder=12 finalVisualOrder=12 rowBounds=432,1693,904,1829 textBounds=433,1694,883,1828 parentBounds=432,1693,904,1829 bubbleBounds=432,1693,904,1829 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=right sideMarginLeft=432 sideMarginRight=180 finalDecisionSource=bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_right
[m013][left][other 82% bubble_edge_left] 送达 rawNodeOrder=13 finalVisualOrder=13 rowBounds=24,1860,1060,2140 textBounds=173,2056,251,2098 parentBounds=24,1860,1060,2140 bubbleBounds=24,1860,1060,2140 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m014][unknown][unknown 30% ambiguous_center_bounds] 带小孩属于身心俱疲的事情，给别人带也不放心 rawNodeOrder=14 finalVisualOrder=14 rowBounds=275,1914,904,2110 textBounds=276,1915,883,2109 parentBounds=275,1914,904,2110 bubbleBounds=275,1914,904,2110 projectedBox=null accessibilitySide=none visualProjectedSide=none conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度16.1℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 原来在一个学校读书，不是同学 | UNKNOWN | UNKNOWN | text | NONE | false | 201,243,808,437 | 180,242,810,439 | 180,242,810,439 | 180,242,810,439 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,469 > 24,242,1060,469 > 180,242,810,439 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 3 | 3 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 330,606,408,648 | 24,470,1060,690 | 24,470,1060,690 | 24,470,1060,690 | 0,0,1084,2412 > 0,242,1084,2261 > 0,470,1084,690 > 24,470,1060,690 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 4 | 4 | 奥，两个人是校友 | ME | ME | text | NONE | true | 433,525,883,659 | 432,524,904,660 | 432,524,904,660 | 432,524,904,660 | 0,0,1084,2412 > 0,242,1084,2261 > 0,470,1084,690 > 24,470,1060,690 > 432,524,904,660 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 是的🙂‍↕️ | OTHER | OTHER | text | NONE | true | 201,747,423,881 | 180,745,425,883 | 180,745,425,883 | 180,745,425,883 | 0,0,1084,2412 > 0,242,1084,2261 > 0,691,1084,913 > 24,691,1060,913 > 180,745,425,883 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 6 | 6 | 你女儿多大了，是在上大学吗 | UNKNOWN | UNKNOWN | text | NONE | false | 201,970,808,1164 | 180,968,810,1166 | 180,968,810,1166 | 180,968,810,1166 | 0,0,1084,2412 > 0,242,1084,2261 > 0,914,1084,1196 > 24,914,1060,1196 > 180,968,810,1166 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |
| 7 | 7 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 468,1333,546,1375 | 24,1197,1060,1417 | 24,1197,1060,1417 | 24,1197,1060,1417 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1197,1084,1417 > 24,1197,1060,1417 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 8 | 8 | 25在读硕士 | ME | ME | text | NONE | true | 571,1252,883,1386 | 570,1251,904,1387 | 570,1251,904,1387 | 570,1251,904,1387 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1197,1084,1417 > 24,1197,1060,1417 > 570,1251,904,1387 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 9 | 9 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 474,1554,552,1596 | 24,1418,1060,1638 | 24,1418,1060,1638 | 24,1418,1060,1638 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1418,1084,1638 > 24,1418,1060,1638 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 10 | 10 | 那也蛮好的 | ME | ME | text | NONE | true | 577,1473,883,1607 | 576,1472,904,1608 | 576,1472,904,1608 | 576,1472,904,1608 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1418,1084,1638 > 24,1418,1060,1638 > 576,1472,904,1608 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 11 | 11 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 330,1775,408,1817 | 24,1639,1060,1859 | 24,1639,1060,1859 | 24,1639,1060,1859 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1639,1084,1859 > 24,1639,1060,1859 | unknown | unknown | none | 100 | read_receipt_metadata | false | none | false | false |
| 12 | 12 | 你家就是你比较累 | ME | ME | text | NONE | true | 433,1694,883,1828 | 432,1693,904,1829 | 432,1693,904,1829 | 432,1693,904,1829 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1639,1084,1859 > 24,1639,1060,1859 > 432,1693,904,1829 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 13 | 13 | 送达 | OTHER | OTHER | text | NONE | true | 173,2056,251,2098 | 24,1860,1060,2140 | 24,1860,1060,2140 | 24,1860,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1860,1084,2140 > 24,1860,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 14 | 14 | 带小孩属于身心俱疲的事情，给别人带也不放心 | UNKNOWN | UNKNOWN | text | NONE | false | 276,1915,883,2109 | 275,1914,904,2110 | 275,1914,904,2110 | 275,1914,904,2110 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1860,1084,2140 > 24,1860,1060,2140 > 275,1914,904,2110 | unknown | unknown | none | 30 | ambiguous_center_bounds | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 4 | 4 | 奥，两个人是校友 | ME | ME | text | NONE | true | 433,525,883,659 | 432,524,904,660 | 432,524,904,660 | 432,524,904,660 | 0,0,1084,2412 > 0,242,1084,2261 > 0,470,1084,690 > 24,470,1060,690 > 432,524,904,660 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 5 | 5 | 是的🙂‍↕️ | OTHER | OTHER | text | NONE | true | 201,747,423,881 | 180,745,425,883 | 180,745,425,883 | 180,745,425,883 | 0,0,1084,2412 > 0,242,1084,2261 > 0,691,1084,913 > 24,691,1060,913 > 180,745,425,883 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |
| 8 | 8 | 25在读硕士 | ME | ME | text | NONE | true | 571,1252,883,1386 | 570,1251,904,1387 | 570,1251,904,1387 | 570,1251,904,1387 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1197,1084,1417 > 24,1197,1060,1417 > 570,1251,904,1387 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 10 | 10 | 那也蛮好的 | ME | ME | text | NONE | true | 577,1473,883,1607 | 576,1472,904,1608 | 576,1472,904,1608 | 576,1472,904,1608 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1418,1084,1638 > 24,1418,1060,1638 > 576,1472,904,1608 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 12 | 12 | 你家就是你比较累 | ME | ME | text | NONE | true | 433,1694,883,1828 | 432,1693,904,1829 | 432,1693,904,1829 | 432,1693,904,1829 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1639,1084,1859 > 24,1639,1060,1859 > 432,1693,904,1829 | unknown | unknown | none | 82 | bubble_edge_right | false | none | false | false |
| 13 | 13 | 送达 | OTHER | OTHER | text | NONE | true | 173,2056,251,2098 | 24,1860,1060,2140 | 24,1860,1060,2140 | 24,1860,1060,2140 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1860,1084,2140 > 24,1860,1060,2140 | unknown | unknown | none | 82 | bubble_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n696
- lastEffectiveMessageId: bubble-n694
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: true
- decisionType: PRE_ANALYSIS_CONTAMINATED
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
- message_nodes written count: 14
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
