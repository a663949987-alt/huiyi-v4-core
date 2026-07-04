# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- realDeviceFunctionalSmoke: FAIL
- scenarioAssertionResult: PASS
- currentOverallResult: FAIL
- generatedAt: 1783159580715
- scenarioName: real_device_last_unknown
- scenarioNameSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- expectedLastSpeaker: UNKNOWN
- expectedLastSpeakerSource: AUTO_FROM_PRE_ANALYSIS_SNAPSHOT
- actualLastSpeaker: UNKNOWN
- actualLastSpeakerFromPreAnalysisSnapshot: UNKNOWN
- actualLastSpeakerFromDecisionSnapshot: UNKNOWN
- actualLastSpeakerFromPostPanelSnapshot: UNKNOWN
- expectedDecisionType: CONTEXT_REQUIRED
- actualDecisionType: CONTEXT_REQUIRED
- expectedRouteCount: 0
- actualRouteCount: 0
- scenarioResult: PASS
- scenarioDefinitionTrusted: true
- scenarioFailureCategory: none
- scenarioDefinitionMismatchReason: none
- productDecisionConsistentWithActualLastSpeaker: false
- failureReason: api_called
- sample_source: real_device_accessibility
- appPackage: com.bajiao.im.liaoqi
- windowTitle: 聊起
- preAnalysisWindowTitle: 聊起
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
- capturedNodeCount: 102
- parserName: LiaoqiRealParser
- LiaoqiRealParserUsed: true
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
- apiCalled: true
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
- cloudLatencyMs: 12003
- cloudErrorCode: SOFT_TIMEOUT_PENDING
- cloudRequestId: none
- cloudContractVersion: HuiyiTacticalContract-v1
- cloudContractValidationResult: PENDING
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.bajiao.im.liaoqi
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
- VisualSpeakerFallbackUsed: false
- visualSpeakerFallbackCount: 0
- conflictCount: 0
- failureCategory: api_called
- userCorrectionProvided: false
- correctedLastSpeaker: none
- correctedMessageId: none

## Snapshot Phase Separation

- preAnalysisSnapshotAvailable: true
- preAnalysisWindowTitle: 聊起
- preAnalysisSnapshotTrusted: true
- preAnalysisWindowTitleSource: TARGET_PRE_ANALYSIS
- preAnalysisResultPanelVisible: false
- decisionSnapshotAvailable: true
- postPanelSnapshotAvailable: true
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false

## 解析结果
- rawParsedNodeCount: 29
- metadataFilteredCount: 16
- candidateChatMessageCount: 13
- unknownSpeakerCount: 6
- effectiveMessageCount: 13
- effectiveMeCount: 5
- effectiveOtherCount: 2
- parsedMessageCount: 29
- meCount: 5
- otherCount: 2
- unknownCount: 6
- unknownRatio: 0.21
- systemCount: 16
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 1
- messageStatusArtifactCount: 0
- readReceiptCount: 0
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: NONE
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 0
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 说你不喜欢贵州的女孩子，那我们两个就不聊。

### filteredMetadataSamples
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-04 08:09
- [TIME] 19:20
- [TIME] 19:20
- [TIME] 19:18
- [TIME] 19:18
- [DATE] 07-03
- [TIME] 19:20
- [TIME] 19:21
- [TIME] 19:19
- [TIME] 19:21
- [TIME] 19:19
- [TIME] 19:20
- [TIME] 19:22
- [TIME] 19:21
- [TIME] 19:21

### speakerReason 分布
- header_metadata: 1
- online_status_metadata: 1
- liaoqi_bubble_edge_right: 5
- time_metadata: 13
- liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68: 1
- liaoqi_bubble_edge_left: 2
- date_metadata: 1
- liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67: 3
- liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65: 2

### UNKNOWN details
- id: liaoqi-n7064
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 222,370,702,435
  parentBounds: 197,343,934,463
  rowBounds: 0,343,1084,463
  bubbleBounds: 197,343,934,463
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,340,1084,475 > 0,343,1084,463 > 197,343,934,463 > 197,343,934,463
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68
- id: liaoqi-n7100
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 205,1141,733,1269
  parentBounds: 150,1116,881,1294
  rowBounds: 0,1116,1084,1294
  bubbleBounds: 150,1116,881,1294
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,1104,1084,1306 > 0,1116,1084,1294 > 150,1116,881,1294 > 150,1116,881,1294
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67
- id: liaoqi-n7113
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 205,1503,733,1568
  parentBounds: 180,1476,881,1596
  rowBounds: 0,1476,1084,1596
  bubbleBounds: 180,1476,881,1596
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,1464,1084,1599 > 0,1476,1084,1596 > 150,1476,881,1596 > 180,1476,881,1596
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65
- id: liaoqi-n7120
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 205,1627,733,1755
  parentBounds: 150,1602,881,1780
  rowBounds: 0,1602,1084,1780
  bubbleBounds: 150,1602,881,1780
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,1599,1084,1792 > 0,1602,1084,1780 > 150,1602,881,1780 > 150,1602,881,1780
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67
- id: liaoqi-n7133
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 205,1975,733,2040
  parentBounds: 180,1948,881,2068
  rowBounds: 0,1948,1084,2068
  bubbleBounds: 180,1948,881,2068
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,1936,1084,2071 > 0,1948,1084,2068 > 150,1948,881,2068 > 180,1948,881,2068
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65
- id: liaoqi-n7140
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 205,2099,733,2227
  parentBounds: 150,2074,881,2246
  rowBounds: 0,2074,1084,2246
  bubbleBounds: 150,2074,881,2246
  ancestorBoundsChain: 0,0,1084,2412 > 0,264,1084,2246 > 0,2071,1084,2246 > 0,2074,1084,2246 > 150,2074,881,2246 > 150,2074,881,2246
  unknownReason: liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67

## 最近 30 条解析消息
[m001][system][system 100% header_metadata] 白云蓝天 rawNodeOrder=1 finalVisualOrder=1 rowBounds=0,0,1084,264 textBounds=327,135,519,200 parentBounds=0,0,1084,264 bubbleBounds=327,135,519,200 projectedBox=327,135,519,200 accessibilitySide=system visualProjectedSide=left conflict=false conflictReason=none inferredSide=system sideMarginLeft=327 sideMarginRight=565 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=header_metadata
[m002][system][system 100% online_status_metadata] 上次在线时间07-04 08:09 rawNodeOrder=2 finalVisualOrder=2 rowBounds=0,0,1084,264 textBounds=327,200,748,249 parentBounds=0,0,1084,264 bubbleBounds=327,200,748,249 projectedBox=327,200,748,249 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=327 sideMarginRight=336 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=online_status_metadata
[m003][right][me 84% liaoqi_bubble_edge_right] 奥 rawNodeOrder=3 finalVisualOrder=3 rowBounds=0,264,1084,337 textBounds=654,264,702,309 parentBounds=629,264,904,337 bubbleBounds=629,264,904,337 projectedBox=629,264,904,337 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=629 sideMarginRight=180 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m004][system][system 100% time_metadata] 19:20 rawNodeOrder=4 finalVisualOrder=4 rowBounds=0,264,1084,337 textBounds=717,264,810,306 parentBounds=629,264,904,337 bubbleBounds=629,264,904,337 projectedBox=629,264,904,337 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=629 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m005][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68] 我看着以为喷泉要开了 rawNodeOrder=5 finalVisualOrder=5 rowBounds=0,343,1084,463 textBounds=222,370,702,435 parentBounds=197,343,934,463 bubbleBounds=197,343,934,463 projectedBox=197,343,934,463 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=197 sideMarginRight=150 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68
[m006][system][system 100% time_metadata] 19:20 rawNodeOrder=6 finalVisualOrder=6 rowBounds=0,343,1084,463 textBounds=717,383,810,432 parentBounds=197,343,934,463 bubbleBounds=197,343,934,463 projectedBox=197,343,934,463 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=197 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m007][left][other 84% liaoqi_bubble_edge_left] 在贵州。 rawNodeOrder=7 finalVisualOrder=7 rowBounds=0,487,1084,607 textBounds=205,514,397,579 parentBounds=180,487,545,607 bubbleBounds=180,487,545,607 projectedBox=180,487,545,607 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=539 finalDecisionSource=liaoqi_bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_left
[m008][system][system 100% time_metadata] 19:18 rawNodeOrder=8 finalVisualOrder=8 rowBounds=0,487,1084,607 textBounds=412,530,505,579 parentBounds=180,487,545,607 bubbleBounds=180,487,545,607 projectedBox=180,487,545,607 accessibilitySide=system visualProjectedSide=left conflict=false conflictReason=none inferredSide=system sideMarginLeft=180 sideMarginRight=539 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m009][left][other 84% liaoqi_bubble_edge_left] 我在贵州老家。 rawNodeOrder=9 finalVisualOrder=9 rowBounds=0,613,1084,733 textBounds=205,640,541,705 parentBounds=150,613,689,733 bubbleBounds=150,613,689,733 projectedBox=150,613,689,733 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=150 sideMarginRight=395 finalDecisionSource=liaoqi_bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_left
[m010][system][system 100% time_metadata] 19:18 rawNodeOrder=10 finalVisualOrder=10 rowBounds=0,613,1084,733 textBounds=556,656,649,705 parentBounds=150,613,689,733 bubbleBounds=150,613,689,733 projectedBox=150,613,689,733 accessibilitySide=system visualProjectedSide=left conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=395 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m011][system][system 100% date_metadata] 07-03 rawNodeOrder=11 finalVisualOrder=11 rowBounds=0,745,1084,834 textBounds=473,757,610,822 parentBounds=0,745,1084,834 bubbleBounds=473,757,610,822 projectedBox=473,757,610,822 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=473 sideMarginRight=474 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=date_metadata
[m012][right][me 84% liaoqi_bubble_edge_right] 奥奥 rawNodeOrder=12 finalVisualOrder=12 rowBounds=0,846,1084,966 textBounds=606,873,702,938 parentBounds=581,846,904,966 bubbleBounds=581,846,904,966 projectedBox=581,846,904,966 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=581 sideMarginRight=180 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m013][system][system 100% time_metadata] 19:20 rawNodeOrder=13 finalVisualOrder=13 rowBounds=0,846,1084,966 textBounds=717,886,810,935 parentBounds=581,846,904,966 bubbleBounds=581,846,904,966 projectedBox=581,846,904,966 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=581 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m014][right][me 84% liaoqi_bubble_edge_right] 我有点乱了 rawNodeOrder=14 finalVisualOrder=14 rowBounds=0,972,1084,1092 textBounds=462,999,702,1064 parentBounds=437,972,934,1092 bubbleBounds=437,972,934,1092 projectedBox=437,972,934,1092 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=437 sideMarginRight=150 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m015][system][system 100% time_metadata] 19:21 rawNodeOrder=15 finalVisualOrder=15 rowBounds=0,972,1084,1092 textBounds=717,1012,810,1061 parentBounds=437,972,934,1092 bubbleBounds=437,972,934,1092 projectedBox=437,972,934,1092 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=437 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m016][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67] 我的店有人锁的，所以我就回老家待几天。 rawNodeOrder=16 finalVisualOrder=16 rowBounds=0,1116,1084,1294 textBounds=205,1141,733,1269 parentBounds=150,1116,881,1294 bubbleBounds=150,1116,881,1294 projectedBox=150,1116,881,1294 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67
[m017][system][system 100% time_metadata] 19:19 rawNodeOrder=17 finalVisualOrder=17 rowBounds=0,1116,1084,1294 textBounds=748,1220,841,1269 parentBounds=150,1116,881,1294 bubbleBounds=150,1116,881,1294 projectedBox=150,1116,881,1294 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m018][right][me 84% liaoqi_bubble_edge_right] 你不是天门的吗😂 rawNodeOrder=18 finalVisualOrder=18 rowBounds=0,1318,1084,1452 textBounds=287,1343,702,1427 parentBounds=262,1318,934,1452 bubbleBounds=262,1318,934,1452 projectedBox=262,1318,934,1452 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=262 sideMarginRight=150 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m019][system][system 100% time_metadata] 19:21 rawNodeOrder=19 finalVisualOrder=19 rowBounds=0,1318,1084,1452 textBounds=717,1375,810,1424 parentBounds=262,1318,934,1452 bubbleBounds=262,1318,934,1452 projectedBox=262,1318,934,1452 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=262 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m020][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65] 谁跟你说我家是天门的？ rawNodeOrder=20 finalVisualOrder=20 rowBounds=0,1476,1084,1596 textBounds=205,1503,733,1568 parentBounds=180,1476,881,1596 bubbleBounds=180,1476,881,1596 projectedBox=180,1476,881,1596 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=180 sideMarginRight=203 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65
[m021][system][system 100% time_metadata] 19:19 rawNodeOrder=21 finalVisualOrder=21 rowBounds=0,1476,1084,1596 textBounds=748,1519,841,1568 parentBounds=180,1476,881,1596 bubbleBounds=180,1476,881,1596 projectedBox=180,1476,881,1596 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=180 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m022][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67] 我家是贵州的，不是天门的，谁跟你说？ rawNodeOrder=22 finalVisualOrder=22 rowBounds=0,1602,1084,1780 textBounds=205,1627,733,1755 parentBounds=150,1602,881,1780 bubbleBounds=150,1602,881,1780 projectedBox=150,1602,881,1780 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67
[m023][system][system 100% time_metadata] 19:20 rawNodeOrder=23 finalVisualOrder=23 rowBounds=0,1602,1084,1780 textBounds=748,1706,841,1755 parentBounds=150,1602,881,1780 bubbleBounds=150,1602,881,1780 projectedBox=150,1602,881,1780 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m024][right][me 84% liaoqi_bubble_edge_right] 啊？ rawNodeOrder=24 finalVisualOrder=24 rowBounds=0,1804,1084,1924 textBounds=606,1831,702,1896 parentBounds=581,1804,934,1924 bubbleBounds=581,1804,934,1924 projectedBox=581,1804,934,1924 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=581 sideMarginRight=150 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m025][system][system 100% time_metadata] 19:22 rawNodeOrder=25 finalVisualOrder=25 rowBounds=0,1804,1084,1924 textBounds=717,1844,810,1893 parentBounds=581,1804,934,1924 bubbleBounds=581,1804,934,1924 projectedBox=581,1804,934,1924 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=581 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m026][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65] 你不喜欢贵州女孩子吗？ rawNodeOrder=26 finalVisualOrder=26 rowBounds=0,1948,1084,2068 textBounds=205,1975,733,2040 parentBounds=180,1948,881,2068 bubbleBounds=180,1948,881,2068 projectedBox=180,1948,881,2068 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=180 sideMarginRight=203 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65
[m027][system][system 100% time_metadata] 19:21 rawNodeOrder=27 finalVisualOrder=27 rowBounds=0,1948,1084,2068 textBounds=748,1991,841,2040 parentBounds=180,1948,881,2068 bubbleBounds=180,1948,881,2068 projectedBox=180,1948,881,2068 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=180 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m028][unknown][unknown 30% liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67] 说你不喜欢贵州的女孩子，那我们两个就不聊。 rawNodeOrder=28 finalVisualOrder=28 rowBounds=0,2074,1084,2246 textBounds=205,2099,733,2227 parentBounds=150,2074,881,2246 bubbleBounds=150,2074,881,2246 projectedBox=150,2074,881,2246 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 unknownReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 possible_speaker_conflict=false speakerReason=liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67
[m029][system][system 100% time_metadata] 19:21 rawNodeOrder=29 finalVisualOrder=29 rowBounds=0,2074,1084,2246 textBounds=748,2178,841,2227 parentBounds=150,2074,881,2246 bubbleBounds=150,2074,881,2246 projectedBox=150,2074,881,2246 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 白云蓝天 | SYSTEM | SYSTEM | text | HEADER | false | 327,135,519,200 | 0,0,1084,264 | 327,135,519,200 | 0,0,1084,264 | 0,0,1084,2412 > 0,0,1084,264 | system | left | 327,135,519,200 | 100 | header_metadata | false | none | false | false |
| 2 | 2 | 上次在线时间07-04 08:09 | SYSTEM | SYSTEM | text | ONLINE_STATUS | false | 327,200,748,249 | 0,0,1084,264 | 327,200,748,249 | 0,0,1084,264 | 0,0,1084,2412 > 0,0,1084,264 | system | unknown | 327,200,748,249 | 100 | online_status_metadata | false | none | false | false |
| 3 | 3 | 奥 | ME | ME | text | NONE | true | 654,264,702,309 | 0,264,1084,337 | 629,264,904,337 | 629,264,904,337 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,340 > 0,264,1084,337 > 629,264,934,337 > 629,264,904,337 | right | right | 629,264,904,337 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 4 | 4 | 19:20 | SYSTEM | SYSTEM | text | TIME | false | 717,264,810,306 | 0,264,1084,337 | 629,264,904,337 | 629,264,904,337 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,340 > 0,264,1084,337 > 629,264,934,337 > 629,264,904,337 | system | right | 629,264,904,337 | 100 | time_metadata | false | none | false | false |
| 5 | 5 | 我看着以为喷泉要开了 | UNKNOWN | UNKNOWN | text | NONE | true | 222,370,702,435 | 0,343,1084,463 | 197,343,934,463 | 197,343,934,463 | 0,0,1084,2412 > 0,264,1084,2246 > 0,340,1084,475 > 0,343,1084,463 > 197,343,934,463 > 197,343,934,463 | unknown | unknown | 197,343,934,463 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68 | false | none | false | false |
| 6 | 6 | 19:20 | SYSTEM | SYSTEM | text | TIME | false | 717,383,810,432 | 0,343,1084,463 | 197,343,934,463 | 197,343,934,463 | 0,0,1084,2412 > 0,264,1084,2246 > 0,340,1084,475 > 0,343,1084,463 > 197,343,934,463 > 197,343,934,463 | system | unknown | 197,343,934,463 | 100 | time_metadata | false | none | false | false |
| 7 | 7 | 在贵州。 | OTHER | OTHER | text | NONE | true | 205,514,397,579 | 0,487,1084,607 | 180,487,545,607 | 180,487,545,607 | 0,0,1084,2412 > 0,264,1084,2246 > 0,475,1084,610 > 0,487,1084,607 > 150,487,545,607 > 180,487,545,607 | left | left | 180,487,545,607 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 8 | 8 | 19:18 | SYSTEM | SYSTEM | text | TIME | false | 412,530,505,579 | 0,487,1084,607 | 180,487,545,607 | 180,487,545,607 | 0,0,1084,2412 > 0,264,1084,2246 > 0,475,1084,610 > 0,487,1084,607 > 150,487,545,607 > 180,487,545,607 | system | left | 180,487,545,607 | 100 | time_metadata | false | none | false | false |
| 9 | 9 | 我在贵州老家。 | OTHER | OTHER | text | NONE | true | 205,640,541,705 | 0,613,1084,733 | 150,613,689,733 | 150,613,689,733 | 0,0,1084,2412 > 0,264,1084,2246 > 0,610,1084,745 > 0,613,1084,733 > 150,613,689,733 > 150,613,689,733 | left | left | 150,613,689,733 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 10 | 10 | 19:18 | SYSTEM | SYSTEM | text | TIME | false | 556,656,649,705 | 0,613,1084,733 | 150,613,689,733 | 150,613,689,733 | 0,0,1084,2412 > 0,264,1084,2246 > 0,610,1084,745 > 0,613,1084,733 > 150,613,689,733 > 150,613,689,733 | system | left | 150,613,689,733 | 100 | time_metadata | false | none | false | false |
| 11 | 11 | 07-03 | SYSTEM | SYSTEM | text | DATE | false | 473,757,610,822 | 0,745,1084,834 | 473,757,610,822 | 0,745,1084,834 | 0,0,1084,2412 > 0,264,1084,2246 > 0,745,1084,834 | system | unknown | 473,757,610,822 | 100 | date_metadata | false | none | false | false |
| 12 | 12 | 奥奥 | ME | ME | text | NONE | true | 606,873,702,938 | 0,846,1084,966 | 581,846,904,966 | 581,846,904,966 | 0,0,1084,2412 > 0,264,1084,2246 > 0,834,1084,969 > 0,846,1084,966 > 581,846,934,966 > 581,846,904,966 | right | right | 581,846,904,966 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 13 | 13 | 19:20 | SYSTEM | SYSTEM | text | TIME | false | 717,886,810,935 | 0,846,1084,966 | 581,846,904,966 | 581,846,904,966 | 0,0,1084,2412 > 0,264,1084,2246 > 0,834,1084,969 > 0,846,1084,966 > 581,846,934,966 > 581,846,904,966 | system | right | 581,846,904,966 | 100 | time_metadata | false | none | false | false |
| 14 | 14 | 我有点乱了 | ME | ME | text | NONE | true | 462,999,702,1064 | 0,972,1084,1092 | 437,972,934,1092 | 437,972,934,1092 | 0,0,1084,2412 > 0,264,1084,2246 > 0,969,1084,1104 > 0,972,1084,1092 > 437,972,934,1092 > 437,972,934,1092 | right | right | 437,972,934,1092 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 15 | 15 | 19:21 | SYSTEM | SYSTEM | text | TIME | false | 717,1012,810,1061 | 0,972,1084,1092 | 437,972,934,1092 | 437,972,934,1092 | 0,0,1084,2412 > 0,264,1084,2246 > 0,969,1084,1104 > 0,972,1084,1092 > 437,972,934,1092 > 437,972,934,1092 | system | right | 437,972,934,1092 | 100 | time_metadata | false | none | false | false |
| 16 | 16 | 我的店有人锁的，所以我就回老家待几天。 | UNKNOWN | UNKNOWN | text | NONE | true | 205,1141,733,1269 | 0,1116,1084,1294 | 150,1116,881,1294 | 150,1116,881,1294 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1104,1084,1306 > 0,1116,1084,1294 > 150,1116,881,1294 > 150,1116,881,1294 | unknown | unknown | 150,1116,881,1294 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |
| 17 | 17 | 19:19 | SYSTEM | SYSTEM | text | TIME | false | 748,1220,841,1269 | 0,1116,1084,1294 | 150,1116,881,1294 | 150,1116,881,1294 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1104,1084,1306 > 0,1116,1084,1294 > 150,1116,881,1294 > 150,1116,881,1294 | system | unknown | 150,1116,881,1294 | 100 | time_metadata | false | none | false | false |
| 18 | 18 | 你不是天门的吗😂 | ME | ME | text | NONE | true | 287,1343,702,1427 | 0,1318,1084,1452 | 262,1318,934,1452 | 262,1318,934,1452 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1306,1084,1464 > 0,1318,1084,1452 > 262,1318,934,1452 > 262,1318,934,1452 | right | right | 262,1318,934,1452 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 19 | 19 | 19:21 | SYSTEM | SYSTEM | text | TIME | false | 717,1375,810,1424 | 0,1318,1084,1452 | 262,1318,934,1452 | 262,1318,934,1452 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1306,1084,1464 > 0,1318,1084,1452 > 262,1318,934,1452 > 262,1318,934,1452 | system | right | 262,1318,934,1452 | 100 | time_metadata | false | none | false | false |
| 20 | 20 | 谁跟你说我家是天门的？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1503,733,1568 | 0,1476,1084,1596 | 180,1476,881,1596 | 180,1476,881,1596 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1464,1084,1599 > 0,1476,1084,1596 > 150,1476,881,1596 > 180,1476,881,1596 | unknown | unknown | 180,1476,881,1596 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 | false | none | false | false |
| 21 | 21 | 19:19 | SYSTEM | SYSTEM | text | TIME | false | 748,1519,841,1568 | 0,1476,1084,1596 | 180,1476,881,1596 | 180,1476,881,1596 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1464,1084,1599 > 0,1476,1084,1596 > 150,1476,881,1596 > 180,1476,881,1596 | system | unknown | 180,1476,881,1596 | 100 | time_metadata | false | none | false | false |
| 22 | 22 | 我家是贵州的，不是天门的，谁跟你说？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1627,733,1755 | 0,1602,1084,1780 | 150,1602,881,1780 | 150,1602,881,1780 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1599,1084,1792 > 0,1602,1084,1780 > 150,1602,881,1780 > 150,1602,881,1780 | unknown | unknown | 150,1602,881,1780 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |
| 23 | 23 | 19:20 | SYSTEM | SYSTEM | text | TIME | false | 748,1706,841,1755 | 0,1602,1084,1780 | 150,1602,881,1780 | 150,1602,881,1780 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1599,1084,1792 > 0,1602,1084,1780 > 150,1602,881,1780 > 150,1602,881,1780 | system | unknown | 150,1602,881,1780 | 100 | time_metadata | false | none | false | false |
| 24 | 24 | 啊？ | ME | ME | text | NONE | true | 606,1831,702,1896 | 0,1804,1084,1924 | 581,1804,934,1924 | 581,1804,934,1924 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1792,1084,1936 > 0,1804,1084,1924 > 581,1804,934,1924 > 581,1804,934,1924 | right | right | 581,1804,934,1924 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 25 | 25 | 19:22 | SYSTEM | SYSTEM | text | TIME | false | 717,1844,810,1893 | 0,1804,1084,1924 | 581,1804,934,1924 | 581,1804,934,1924 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1792,1084,1936 > 0,1804,1084,1924 > 581,1804,934,1924 > 581,1804,934,1924 | system | right | 581,1804,934,1924 | 100 | time_metadata | false | none | false | false |
| 26 | 26 | 你不喜欢贵州女孩子吗？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1975,733,2040 | 0,1948,1084,2068 | 180,1948,881,2068 | 180,1948,881,2068 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1936,1084,2071 > 0,1948,1084,2068 > 150,1948,881,2068 > 180,1948,881,2068 | unknown | unknown | 180,1948,881,2068 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 | false | none | false | false |
| 27 | 27 | 19:21 | SYSTEM | SYSTEM | text | TIME | false | 748,1991,841,2040 | 0,1948,1084,2068 | 180,1948,881,2068 | 180,1948,881,2068 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1936,1084,2071 > 0,1948,1084,2068 > 150,1948,881,2068 > 180,1948,881,2068 | system | unknown | 180,1948,881,2068 | 100 | time_metadata | false | none | false | false |
| 28 | 28 | 说你不喜欢贵州的女孩子，那我们两个就不聊。 | UNKNOWN | UNKNOWN | text | NONE | true | 205,2099,733,2227 | 0,2074,1084,2246 | 150,2074,881,2246 | 150,2074,881,2246 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2071,1084,2246 > 0,2074,1084,2246 > 150,2074,881,2246 > 150,2074,881,2246 | unknown | unknown | 150,2074,881,2246 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |
| 29 | 29 | 19:21 | SYSTEM | SYSTEM | text | TIME | false | 748,2178,841,2227 | 0,2074,1084,2246 | 150,2074,881,2246 | 150,2074,881,2246 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2071,1084,2246 > 0,2074,1084,2246 > 150,2074,881,2246 > 150,2074,881,2246 | system | unknown | 150,2074,881,2246 | 100 | time_metadata | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 3 | 3 | 奥 | ME | ME | text | NONE | true | 654,264,702,309 | 0,264,1084,337 | 629,264,904,337 | 629,264,904,337 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,340 > 0,264,1084,337 > 629,264,934,337 > 629,264,904,337 | right | right | 629,264,904,337 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 5 | 5 | 我看着以为喷泉要开了 | UNKNOWN | UNKNOWN | text | NONE | true | 222,370,702,435 | 0,343,1084,463 | 197,343,934,463 | 197,343,934,463 | 0,0,1084,2412 > 0,264,1084,2246 > 0,340,1084,475 > 0,343,1084,463 > 197,343,934,463 > 197,343,934,463 | unknown | unknown | 197,343,934,463 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=197 rightMargin=150 widthRatio=0.68 | false | none | false | false |
| 7 | 7 | 在贵州。 | OTHER | OTHER | text | NONE | true | 205,514,397,579 | 0,487,1084,607 | 180,487,545,607 | 180,487,545,607 | 0,0,1084,2412 > 0,264,1084,2246 > 0,475,1084,610 > 0,487,1084,607 > 150,487,545,607 > 180,487,545,607 | left | left | 180,487,545,607 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 9 | 9 | 我在贵州老家。 | OTHER | OTHER | text | NONE | true | 205,640,541,705 | 0,613,1084,733 | 150,613,689,733 | 150,613,689,733 | 0,0,1084,2412 > 0,264,1084,2246 > 0,610,1084,745 > 0,613,1084,733 > 150,613,689,733 > 150,613,689,733 | left | left | 150,613,689,733 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 12 | 12 | 奥奥 | ME | ME | text | NONE | true | 606,873,702,938 | 0,846,1084,966 | 581,846,904,966 | 581,846,904,966 | 0,0,1084,2412 > 0,264,1084,2246 > 0,834,1084,969 > 0,846,1084,966 > 581,846,934,966 > 581,846,904,966 | right | right | 581,846,904,966 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 14 | 14 | 我有点乱了 | ME | ME | text | NONE | true | 462,999,702,1064 | 0,972,1084,1092 | 437,972,934,1092 | 437,972,934,1092 | 0,0,1084,2412 > 0,264,1084,2246 > 0,969,1084,1104 > 0,972,1084,1092 > 437,972,934,1092 > 437,972,934,1092 | right | right | 437,972,934,1092 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 16 | 16 | 我的店有人锁的，所以我就回老家待几天。 | UNKNOWN | UNKNOWN | text | NONE | true | 205,1141,733,1269 | 0,1116,1084,1294 | 150,1116,881,1294 | 150,1116,881,1294 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1104,1084,1306 > 0,1116,1084,1294 > 150,1116,881,1294 > 150,1116,881,1294 | unknown | unknown | 150,1116,881,1294 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |
| 18 | 18 | 你不是天门的吗😂 | ME | ME | text | NONE | true | 287,1343,702,1427 | 0,1318,1084,1452 | 262,1318,934,1452 | 262,1318,934,1452 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1306,1084,1464 > 0,1318,1084,1452 > 262,1318,934,1452 > 262,1318,934,1452 | right | right | 262,1318,934,1452 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 20 | 20 | 谁跟你说我家是天门的？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1503,733,1568 | 0,1476,1084,1596 | 180,1476,881,1596 | 180,1476,881,1596 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1464,1084,1599 > 0,1476,1084,1596 > 150,1476,881,1596 > 180,1476,881,1596 | unknown | unknown | 180,1476,881,1596 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 | false | none | false | false |
| 22 | 22 | 我家是贵州的，不是天门的，谁跟你说？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1627,733,1755 | 0,1602,1084,1780 | 150,1602,881,1780 | 150,1602,881,1780 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1599,1084,1792 > 0,1602,1084,1780 > 150,1602,881,1780 > 150,1602,881,1780 | unknown | unknown | 150,1602,881,1780 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |
| 24 | 24 | 啊？ | ME | ME | text | NONE | true | 606,1831,702,1896 | 0,1804,1084,1924 | 581,1804,934,1924 | 581,1804,934,1924 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1792,1084,1936 > 0,1804,1084,1924 > 581,1804,934,1924 > 581,1804,934,1924 | right | right | 581,1804,934,1924 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 26 | 26 | 你不喜欢贵州女孩子吗？ | UNKNOWN | UNKNOWN | text | NONE | true | 205,1975,733,2040 | 0,1948,1084,2068 | 180,1948,881,2068 | 180,1948,881,2068 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1936,1084,2071 > 0,1948,1084,2068 > 150,1948,881,2068 > 180,1948,881,2068 | unknown | unknown | 180,1948,881,2068 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=180 rightMargin=203 widthRatio=0.65 | false | none | false | false |
| 28 | 28 | 说你不喜欢贵州的女孩子，那我们两个就不聊。 | UNKNOWN | UNKNOWN | text | NONE | true | 205,2099,733,2227 | 0,2074,1084,2246 | 150,2074,881,2246 | 150,2074,881,2246 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2071,1084,2246 > 0,2074,1084,2246 > 150,2074,881,2246 > 150,2074,881,2246 | unknown | unknown | 150,2074,881,2246 | 30 | liaoqi_ambiguous_bubble_bounds liaoqi_ambiguous_bounds leftMargin=150 rightMargin=203 widthRatio=0.67 | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: liaoqi-n7141
- lastEffectiveMessageId: liaoqi-n7140
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: UNKNOWN
- lastSpeaker: UNKNOWN
- shouldReply: false
- decisionType: CONTEXT_REQUIRED
- reason: 我还没分清这句是谁说的，请切换我的气泡方向或补充。

## ContextAssembler
- contextCompleteness.score: 82
- canDeepAnalyze: true
- missingTypes: UNKNOWN_SPEAKER
- coCreationOpportunity.exists: true
- coCreationOpportunity.type: SHARED_EXPECTATION
- unfinishedMeaning: 双方正在试探一种共同节奏。
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
- message_nodes written count: 47
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: true
