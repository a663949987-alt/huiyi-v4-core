# Real Device Current Screen Evidence Pack

- overall_result: PASS
- realDeviceFunctionalSmoke: PASS
- scenarioAssertionResult: PASS
- currentOverallResult: PASS
- generatedAt: 1783070420710
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
- appPackage: com.bajiao.im.liaoqi
- windowTitle: 华为桌面
- preAnalysisWindowTitle: 华为桌面
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
- capturedNodeCount: 101
- parserName: LiaoqiRealParser
- LiaoqiRealParserUsed: true
- GenericVisualBubbleParserFallbackUsed: false
- parserFallbackUsed: true
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
- cloudEnabled: false
- cloudEndpointConfigured: false
- cloudAttempted: false
- cloudSuccess: false
- cloudSkippedReason: CLOUD_NOT_CONFIGURED
- decisionSource: LOCAL_FALLBACK
- cloudFallbackUsed: false
- cloudLatencyMs: null
- cloudErrorCode: none
- cloudRequestId: none
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
- reason: Services don't have the capability of taking the screenshot.
- screenshotPath: none
- overlayImagePath: /data/user/0/com.huiyi.v4/files/debug/real_device_visual_debug/current_screen_overlay.png
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
- failureCategory: none
- userCorrectionProvided: false
- correctedLastSpeaker: none
- correctedMessageId: none

## Snapshot Phase Separation

- preAnalysisSnapshotAvailable: true
- preAnalysisWindowTitle: 华为桌面
- preAnalysisResultPanelVisible: false
- decisionSnapshotAvailable: true
- postPanelSnapshotAvailable: true
- postPanelWindowTitle: none
- reportWindowTitleContaminatedByPanel: false
- postPanelStateUsedForScenarioExpectation: false

## 解析结果
- rawParsedNodeCount: 26
- metadataFilteredCount: 14
- candidateChatMessageCount: 12
- unknownSpeakerCount: 0
- effectiveMessageCount: 12
- effectiveMeCount: 4
- effectiveOtherCount: 8
- parsedMessageCount: 26
- meCount: 4
- otherCount: 8
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 14
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 0
- messageStatusArtifactCount: 0
- readReceiptCount: 0
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: NONE
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 0
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 嗯嗯，那生意好才行☺️

### filteredMetadataSamples
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-03 15:37
- [TIME] 11:11
- [TIME] 11:13
- [TIME] 11:11
- [TIME] 11:14
- [TIME] 11:14
- [TIME] 11:15
- [TIME] 11:15
- [TIME] 11:12
- [TIME] 11:15
- [TIME] 11:13
- [TIME] 11:16
- [TIME] 11:16

### speakerReason 分布
- header_metadata: 1
- online_status_metadata: 1
- liaoqi_bubble_edge_left: 1
- time_metadata: 12
- liaoqi_text_edge_left: 7
- liaoqi_bubble_edge_right: 4

### UNKNOWN details
- none

## 最近 30 条解析消息
[m001][system][system 100% header_metadata] 白云蓝天 rawNodeOrder=1 finalVisualOrder=1 rowBounds=0,0,1084,264 textBounds=327,135,519,200 parentBounds=0,0,1084,264 bubbleBounds=327,135,519,200 projectedBox=327,135,519,200 accessibilitySide=system visualProjectedSide=left conflict=false conflictReason=none inferredSide=system sideMarginLeft=327 sideMarginRight=565 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=header_metadata
[m002][system][system 100% online_status_metadata] 上次在线时间07-03 15:37 rawNodeOrder=2 finalVisualOrder=2 rowBounds=0,0,1084,264 textBounds=327,200,748,249 parentBounds=0,0,1084,264 bubbleBounds=327,200,748,249 projectedBox=327,200,748,249 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=327 sideMarginRight=336 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=online_status_metadata
[m003][left][other 84% liaoqi_bubble_edge_left] 我去给客户去送衣服去了。 rawNodeOrder=3 finalVisualOrder=3 rowBounds=0,264,1084,386 textBounds=205,264,685,361 parentBounds=150,264,833,386 bubbleBounds=150,264,833,386 projectedBox=150,264,833,386 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=150 sideMarginRight=251 finalDecisionSource=liaoqi_bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_left
[m004][system][system 100% time_metadata] 11:11 rawNodeOrder=4 finalVisualOrder=4 rowBounds=0,264,1084,386 textBounds=700,312,793,361 parentBounds=150,264,833,386 bubbleBounds=150,264,833,386 projectedBox=150,264,833,386 accessibilitySide=system visualProjectedSide=left conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=251 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m005][left][other 78% liaoqi_text_edge_left] 好，我过会也去吃饭了 rawNodeOrder=5 finalVisualOrder=5 rowBounds=0,410,1084,530 textBounds=222,437,702,502 parentBounds=197,410,934,530 bubbleBounds=197,410,934,530 projectedBox=197,410,934,530 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=197 sideMarginRight=150 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m006][system][system 100% time_metadata] 11:13 rawNodeOrder=6 finalVisualOrder=6 rowBounds=0,410,1084,530 textBounds=717,450,810,499 parentBounds=197,410,934,530 bubbleBounds=197,410,934,530 projectedBox=197,410,934,530 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=197 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m007][left][other 78% liaoqi_text_edge_left] 在这边订的衣服，我要去跟他送过去。 rawNodeOrder=7 finalVisualOrder=7 rowBounds=0,554,1084,732 textBounds=205,579,733,707 parentBounds=150,554,881,732 bubbleBounds=150,554,881,732 projectedBox=150,554,881,732 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m008][system][system 100% time_metadata] 11:11 rawNodeOrder=8 finalVisualOrder=8 rowBounds=0,554,1084,732 textBounds=748,658,841,707 parentBounds=150,554,881,732 bubbleBounds=150,554,881,732 projectedBox=150,554,881,732 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m009][right][me 84% liaoqi_bubble_edge_right] 空了再聊 rawNodeOrder=9 finalVisualOrder=9 rowBounds=0,756,1084,876 textBounds=510,783,702,848 parentBounds=485,756,904,876 bubbleBounds=485,756,904,876 projectedBox=485,756,904,876 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=485 sideMarginRight=180 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m010][system][system 100% time_metadata] 11:14 rawNodeOrder=10 finalVisualOrder=10 rowBounds=0,756,1084,876 textBounds=717,796,810,845 parentBounds=485,756,904,876 bubbleBounds=485,756,904,876 projectedBox=485,756,904,876 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=485 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m011][left][other 78% liaoqi_text_edge_left] 你要记得吃饭，照顾好自己 rawNodeOrder=11 finalVisualOrder=11 rowBounds=0,882,1084,1060 textBounds=222,907,702,1035 parentBounds=197,882,904,1060 bubbleBounds=197,882,904,1060 projectedBox=197,882,904,1060 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=197 sideMarginRight=180 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m012][system][system 100% time_metadata] 11:14 rawNodeOrder=12 finalVisualOrder=12 rowBounds=0,882,1084,1060 textBounds=717,983,810,1032 parentBounds=197,882,904,1060 bubbleBounds=197,882,904,1060 projectedBox=197,882,904,1060 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=197 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m013][left][other 78% liaoqi_text_edge_left] 忙完空下来找我聊聊 rawNodeOrder=13 finalVisualOrder=13 rowBounds=0,1066,1084,1186 textBounds=270,1093,702,1158 parentBounds=245,1066,904,1186 bubbleBounds=245,1066,904,1186 projectedBox=245,1066,904,1186 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=245 sideMarginRight=180 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m014][system][system 100% time_metadata] 11:15 rawNodeOrder=14 finalVisualOrder=14 rowBounds=0,1066,1084,1186 textBounds=717,1106,810,1155 parentBounds=245,1066,904,1186 bubbleBounds=245,1066,904,1186 projectedBox=245,1066,904,1186 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=245 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m015][right][me 84% liaoqi_bubble_edge_right] 路上慢点 rawNodeOrder=15 finalVisualOrder=15 rowBounds=0,1192,1084,1312 textBounds=510,1219,702,1284 parentBounds=485,1192,934,1312 bubbleBounds=485,1192,934,1312 projectedBox=485,1192,934,1312 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=485 sideMarginRight=150 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m016][system][system 100% time_metadata] 11:15 rawNodeOrder=16 finalVisualOrder=16 rowBounds=0,1192,1084,1312 textBounds=717,1232,810,1281 parentBounds=485,1192,934,1312 bubbleBounds=485,1192,934,1312 projectedBox=485,1192,934,1312 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=485 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m017][left][other 78% liaoqi_text_edge_left] 谢谢你已经吃过饭了。我们都是在店里面吃的。 rawNodeOrder=17 finalVisualOrder=17 rowBounds=0,1336,1084,1514 textBounds=205,1361,733,1489 parentBounds=150,1336,881,1514 bubbleBounds=150,1336,881,1514 projectedBox=150,1336,881,1514 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m018][system][system 100% time_metadata] 11:12 rawNodeOrder=18 finalVisualOrder=18 rowBounds=0,1336,1084,1514 textBounds=748,1440,841,1489 parentBounds=150,1336,881,1514 bubbleBounds=150,1336,881,1514 projectedBox=150,1336,881,1514 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m019][right][me 84% liaoqi_bubble_edge_right] 😛吃的这么早 rawNodeOrder=19 finalVisualOrder=19 rowBounds=0,1538,1084,1672 textBounds=383,1563,702,1647 parentBounds=358,1538,934,1672 bubbleBounds=358,1538,934,1672 projectedBox=358,1538,934,1672 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=358 sideMarginRight=150 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m020][system][system 100% time_metadata] 11:15 rawNodeOrder=20 finalVisualOrder=20 rowBounds=0,1538,1084,1672 textBounds=717,1595,810,1644 parentBounds=358,1538,934,1672 bubbleBounds=358,1538,934,1672 projectedBox=358,1538,934,1672 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=358 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m021][left][other 78% liaoqi_text_edge_left] 因为今天有很多顾客来买衣服，吃饭，吃早一点呐。 rawNodeOrder=21 finalVisualOrder=21 rowBounds=0,1696,1084,1937 textBounds=205,1721,733,1912 parentBounds=150,1696,881,1937 bubbleBounds=150,1696,881,1937 projectedBox=150,1696,881,1937 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m022][system][system 100% time_metadata] 11:13 rawNodeOrder=22 finalVisualOrder=22 rowBounds=0,1696,1084,1937 textBounds=748,1863,841,1912 parentBounds=150,1696,881,1937 bubbleBounds=150,1696,881,1937 projectedBox=150,1696,881,1937 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=150 sideMarginRight=203 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m023][right][me 84% liaoqi_bubble_edge_right] 店里面吃的？ rawNodeOrder=23 finalVisualOrder=23 rowBounds=0,1961,1084,2081 textBounds=414,1988,702,2053 parentBounds=389,1961,904,2081 bubbleBounds=389,1961,904,2081 projectedBox=389,1961,904,2081 accessibilitySide=right visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=389 sideMarginRight=180 finalDecisionSource=liaoqi_bubble_edge_right unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_bubble_edge_right
[m024][system][system 100% time_metadata] 11:16 rawNodeOrder=24 finalVisualOrder=24 rowBounds=0,1961,1084,2081 textBounds=717,2001,810,2050 parentBounds=389,1961,904,2081 bubbleBounds=389,1961,904,2081 projectedBox=389,1961,904,2081 accessibilitySide=system visualProjectedSide=right conflict=false conflictReason=none inferredSide=system sideMarginLeft=389 sideMarginRight=180 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata
[m025][left][other 78% liaoqi_text_edge_left] 嗯嗯，那生意好才行☺️ rawNodeOrder=25 finalVisualOrder=25 rowBounds=0,2087,1084,2221 textBounds=191,2112,702,2196 parentBounds=166,2087,934,2221 bubbleBounds=166,2087,934,2221 projectedBox=166,2087,934,2221 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=166 sideMarginRight=150 finalDecisionSource=liaoqi_text_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=liaoqi_text_edge_left
[m026][system][system 100% time_metadata] 11:16 rawNodeOrder=26 finalVisualOrder=26 rowBounds=0,2087,1084,2221 textBounds=717,2144,810,2193 parentBounds=166,2087,934,2221 bubbleBounds=166,2087,934,2221 projectedBox=166,2087,934,2221 accessibilitySide=system visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=system sideMarginLeft=166 sideMarginRight=150 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=time_metadata

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 白云蓝天 | SYSTEM | SYSTEM | text | HEADER | false | 327,135,519,200 | 0,0,1084,264 | 327,135,519,200 | 0,0,1084,264 | 0,0,1084,2412 > 0,0,1084,264 | system | left | 327,135,519,200 | 100 | header_metadata | false | none | false | false |
| 2 | 2 | 上次在线时间07-03 15:37 | SYSTEM | SYSTEM | text | ONLINE_STATUS | false | 327,200,748,249 | 0,0,1084,264 | 327,200,748,249 | 0,0,1084,264 | 0,0,1084,2412 > 0,0,1084,264 | system | unknown | 327,200,748,249 | 100 | online_status_metadata | false | none | false | false |
| 3 | 3 | 我去给客户去送衣服去了。 | OTHER | OTHER | text | NONE | true | 205,264,685,361 | 0,264,1084,386 | 150,264,833,386 | 150,264,833,386 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,398 > 0,264,1084,386 > 150,264,833,386 > 150,264,833,386 | left | left | 150,264,833,386 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 4 | 4 | 11:11 | SYSTEM | SYSTEM | text | TIME | false | 700,312,793,361 | 0,264,1084,386 | 150,264,833,386 | 150,264,833,386 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,398 > 0,264,1084,386 > 150,264,833,386 > 150,264,833,386 | system | left | 150,264,833,386 | 100 | time_metadata | false | none | false | false |
| 5 | 5 | 好，我过会也去吃饭了 | OTHER | OTHER | text | NONE | true | 222,437,702,502 | 0,410,1084,530 | 197,410,934,530 | 197,410,934,530 | 0,0,1084,2412 > 0,264,1084,2246 > 0,398,1084,542 > 0,410,1084,530 > 197,410,934,530 > 197,410,934,530 | left | unknown | 197,410,934,530 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 6 | 6 | 11:13 | SYSTEM | SYSTEM | text | TIME | false | 717,450,810,499 | 0,410,1084,530 | 197,410,934,530 | 197,410,934,530 | 0,0,1084,2412 > 0,264,1084,2246 > 0,398,1084,542 > 0,410,1084,530 > 197,410,934,530 > 197,410,934,530 | system | unknown | 197,410,934,530 | 100 | time_metadata | false | none | false | false |
| 7 | 7 | 在这边订的衣服，我要去跟他送过去。 | OTHER | OTHER | text | NONE | true | 205,579,733,707 | 0,554,1084,732 | 150,554,881,732 | 150,554,881,732 | 0,0,1084,2412 > 0,264,1084,2246 > 0,542,1084,744 > 0,554,1084,732 > 150,554,881,732 > 150,554,881,732 | left | unknown | 150,554,881,732 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 8 | 8 | 11:11 | SYSTEM | SYSTEM | text | TIME | false | 748,658,841,707 | 0,554,1084,732 | 150,554,881,732 | 150,554,881,732 | 0,0,1084,2412 > 0,264,1084,2246 > 0,542,1084,744 > 0,554,1084,732 > 150,554,881,732 > 150,554,881,732 | system | unknown | 150,554,881,732 | 100 | time_metadata | false | none | false | false |
| 9 | 9 | 空了再聊 | ME | ME | text | NONE | true | 510,783,702,848 | 0,756,1084,876 | 485,756,904,876 | 485,756,904,876 | 0,0,1084,2412 > 0,264,1084,2246 > 0,744,1084,879 > 0,756,1084,876 > 485,756,934,876 > 485,756,904,876 | right | right | 485,756,904,876 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 10 | 10 | 11:14 | SYSTEM | SYSTEM | text | TIME | false | 717,796,810,845 | 0,756,1084,876 | 485,756,904,876 | 485,756,904,876 | 0,0,1084,2412 > 0,264,1084,2246 > 0,744,1084,879 > 0,756,1084,876 > 485,756,934,876 > 485,756,904,876 | system | right | 485,756,904,876 | 100 | time_metadata | false | none | false | false |
| 11 | 11 | 你要记得吃饭，照顾好自己 | OTHER | OTHER | text | NONE | true | 222,907,702,1035 | 0,882,1084,1060 | 197,882,904,1060 | 197,882,904,1060 | 0,0,1084,2412 > 0,264,1084,2246 > 0,879,1084,1063 > 0,882,1084,1060 > 197,882,934,1060 > 197,882,904,1060 | left | unknown | 197,882,904,1060 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 12 | 12 | 11:14 | SYSTEM | SYSTEM | text | TIME | false | 717,983,810,1032 | 0,882,1084,1060 | 197,882,904,1060 | 197,882,904,1060 | 0,0,1084,2412 > 0,264,1084,2246 > 0,879,1084,1063 > 0,882,1084,1060 > 197,882,934,1060 > 197,882,904,1060 | system | unknown | 197,882,904,1060 | 100 | time_metadata | false | none | false | false |
| 13 | 13 | 忙完空下来找我聊聊 | OTHER | OTHER | text | NONE | true | 270,1093,702,1158 | 0,1066,1084,1186 | 245,1066,904,1186 | 245,1066,904,1186 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1063,1084,1189 > 0,1066,1084,1186 > 245,1066,934,1186 > 245,1066,904,1186 | left | unknown | 245,1066,904,1186 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 14 | 14 | 11:15 | SYSTEM | SYSTEM | text | TIME | false | 717,1106,810,1155 | 0,1066,1084,1186 | 245,1066,904,1186 | 245,1066,904,1186 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1063,1084,1189 > 0,1066,1084,1186 > 245,1066,934,1186 > 245,1066,904,1186 | system | unknown | 245,1066,904,1186 | 100 | time_metadata | false | none | false | false |
| 15 | 15 | 路上慢点 | ME | ME | text | NONE | true | 510,1219,702,1284 | 0,1192,1084,1312 | 485,1192,934,1312 | 485,1192,934,1312 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1189,1084,1324 > 0,1192,1084,1312 > 485,1192,934,1312 > 485,1192,934,1312 | right | right | 485,1192,934,1312 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 16 | 16 | 11:15 | SYSTEM | SYSTEM | text | TIME | false | 717,1232,810,1281 | 0,1192,1084,1312 | 485,1192,934,1312 | 485,1192,934,1312 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1189,1084,1324 > 0,1192,1084,1312 > 485,1192,934,1312 > 485,1192,934,1312 | system | right | 485,1192,934,1312 | 100 | time_metadata | false | none | false | false |
| 17 | 17 | 谢谢你已经吃过饭了。我们都是在店里面吃的。 | OTHER | OTHER | text | NONE | true | 205,1361,733,1489 | 0,1336,1084,1514 | 150,1336,881,1514 | 150,1336,881,1514 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1324,1084,1526 > 0,1336,1084,1514 > 150,1336,881,1514 > 150,1336,881,1514 | left | unknown | 150,1336,881,1514 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 18 | 18 | 11:12 | SYSTEM | SYSTEM | text | TIME | false | 748,1440,841,1489 | 0,1336,1084,1514 | 150,1336,881,1514 | 150,1336,881,1514 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1324,1084,1526 > 0,1336,1084,1514 > 150,1336,881,1514 > 150,1336,881,1514 | system | unknown | 150,1336,881,1514 | 100 | time_metadata | false | none | false | false |
| 19 | 19 | 😛吃的这么早 | ME | ME | text | NONE | true | 383,1563,702,1647 | 0,1538,1084,1672 | 358,1538,934,1672 | 358,1538,934,1672 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1526,1084,1684 > 0,1538,1084,1672 > 358,1538,934,1672 > 358,1538,934,1672 | right | right | 358,1538,934,1672 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 20 | 20 | 11:15 | SYSTEM | SYSTEM | text | TIME | false | 717,1595,810,1644 | 0,1538,1084,1672 | 358,1538,934,1672 | 358,1538,934,1672 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1526,1084,1684 > 0,1538,1084,1672 > 358,1538,934,1672 > 358,1538,934,1672 | system | right | 358,1538,934,1672 | 100 | time_metadata | false | none | false | false |
| 21 | 21 | 因为今天有很多顾客来买衣服，吃饭，吃早一点呐。 | OTHER | OTHER | text | NONE | true | 205,1721,733,1912 | 0,1696,1084,1937 | 150,1696,881,1937 | 150,1696,881,1937 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1684,1084,1949 > 0,1696,1084,1937 > 150,1696,881,1937 > 150,1696,881,1937 | left | unknown | 150,1696,881,1937 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 22 | 22 | 11:13 | SYSTEM | SYSTEM | text | TIME | false | 748,1863,841,1912 | 0,1696,1084,1937 | 150,1696,881,1937 | 150,1696,881,1937 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1684,1084,1949 > 0,1696,1084,1937 > 150,1696,881,1937 > 150,1696,881,1937 | system | unknown | 150,1696,881,1937 | 100 | time_metadata | false | none | false | false |
| 23 | 23 | 店里面吃的？ | ME | ME | text | NONE | true | 414,1988,702,2053 | 0,1961,1084,2081 | 389,1961,904,2081 | 389,1961,904,2081 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1949,1084,2084 > 0,1961,1084,2081 > 389,1961,934,2081 > 389,1961,904,2081 | right | right | 389,1961,904,2081 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 24 | 24 | 11:16 | SYSTEM | SYSTEM | text | TIME | false | 717,2001,810,2050 | 0,1961,1084,2081 | 389,1961,904,2081 | 389,1961,904,2081 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1949,1084,2084 > 0,1961,1084,2081 > 389,1961,934,2081 > 389,1961,904,2081 | system | right | 389,1961,904,2081 | 100 | time_metadata | false | none | false | false |
| 25 | 25 | 嗯嗯，那生意好才行☺️ | OTHER | OTHER | text | NONE | true | 191,2112,702,2196 | 0,2087,1084,2221 | 166,2087,934,2221 | 166,2087,934,2221 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2084,1084,2233 > 0,2087,1084,2221 > 166,2087,934,2221 > 166,2087,934,2221 | left | unknown | 166,2087,934,2221 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 26 | 26 | 11:16 | SYSTEM | SYSTEM | text | TIME | false | 717,2144,810,2193 | 0,2087,1084,2221 | 166,2087,934,2221 | 166,2087,934,2221 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2084,1084,2233 > 0,2087,1084,2221 > 166,2087,934,2221 > 166,2087,934,2221 | system | unknown | 166,2087,934,2221 | 100 | time_metadata | false | none | false | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 3 | 3 | 我去给客户去送衣服去了。 | OTHER | OTHER | text | NONE | true | 205,264,685,361 | 0,264,1084,386 | 150,264,833,386 | 150,264,833,386 | 0,0,1084,2412 > 0,264,1084,2246 > 0,264,1084,398 > 0,264,1084,386 > 150,264,833,386 > 150,264,833,386 | left | left | 150,264,833,386 | 84 | liaoqi_bubble_edge_left | false | none | false | false |
| 5 | 5 | 好，我过会也去吃饭了 | OTHER | OTHER | text | NONE | true | 222,437,702,502 | 0,410,1084,530 | 197,410,934,530 | 197,410,934,530 | 0,0,1084,2412 > 0,264,1084,2246 > 0,398,1084,542 > 0,410,1084,530 > 197,410,934,530 > 197,410,934,530 | left | unknown | 197,410,934,530 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 7 | 7 | 在这边订的衣服，我要去跟他送过去。 | OTHER | OTHER | text | NONE | true | 205,579,733,707 | 0,554,1084,732 | 150,554,881,732 | 150,554,881,732 | 0,0,1084,2412 > 0,264,1084,2246 > 0,542,1084,744 > 0,554,1084,732 > 150,554,881,732 > 150,554,881,732 | left | unknown | 150,554,881,732 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 9 | 9 | 空了再聊 | ME | ME | text | NONE | true | 510,783,702,848 | 0,756,1084,876 | 485,756,904,876 | 485,756,904,876 | 0,0,1084,2412 > 0,264,1084,2246 > 0,744,1084,879 > 0,756,1084,876 > 485,756,934,876 > 485,756,904,876 | right | right | 485,756,904,876 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 11 | 11 | 你要记得吃饭，照顾好自己 | OTHER | OTHER | text | NONE | true | 222,907,702,1035 | 0,882,1084,1060 | 197,882,904,1060 | 197,882,904,1060 | 0,0,1084,2412 > 0,264,1084,2246 > 0,879,1084,1063 > 0,882,1084,1060 > 197,882,934,1060 > 197,882,904,1060 | left | unknown | 197,882,904,1060 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 13 | 13 | 忙完空下来找我聊聊 | OTHER | OTHER | text | NONE | true | 270,1093,702,1158 | 0,1066,1084,1186 | 245,1066,904,1186 | 245,1066,904,1186 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1063,1084,1189 > 0,1066,1084,1186 > 245,1066,934,1186 > 245,1066,904,1186 | left | unknown | 245,1066,904,1186 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 15 | 15 | 路上慢点 | ME | ME | text | NONE | true | 510,1219,702,1284 | 0,1192,1084,1312 | 485,1192,934,1312 | 485,1192,934,1312 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1189,1084,1324 > 0,1192,1084,1312 > 485,1192,934,1312 > 485,1192,934,1312 | right | right | 485,1192,934,1312 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 17 | 17 | 谢谢你已经吃过饭了。我们都是在店里面吃的。 | OTHER | OTHER | text | NONE | true | 205,1361,733,1489 | 0,1336,1084,1514 | 150,1336,881,1514 | 150,1336,881,1514 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1324,1084,1526 > 0,1336,1084,1514 > 150,1336,881,1514 > 150,1336,881,1514 | left | unknown | 150,1336,881,1514 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 19 | 19 | 😛吃的这么早 | ME | ME | text | NONE | true | 383,1563,702,1647 | 0,1538,1084,1672 | 358,1538,934,1672 | 358,1538,934,1672 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1526,1084,1684 > 0,1538,1084,1672 > 358,1538,934,1672 > 358,1538,934,1672 | right | right | 358,1538,934,1672 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 21 | 21 | 因为今天有很多顾客来买衣服，吃饭，吃早一点呐。 | OTHER | OTHER | text | NONE | true | 205,1721,733,1912 | 0,1696,1084,1937 | 150,1696,881,1937 | 150,1696,881,1937 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1684,1084,1949 > 0,1696,1084,1937 > 150,1696,881,1937 > 150,1696,881,1937 | left | unknown | 150,1696,881,1937 | 78 | liaoqi_text_edge_left | false | none | false | false |
| 23 | 23 | 店里面吃的？ | ME | ME | text | NONE | true | 414,1988,702,2053 | 0,1961,1084,2081 | 389,1961,904,2081 | 389,1961,904,2081 | 0,0,1084,2412 > 0,264,1084,2246 > 0,1949,1084,2084 > 0,1961,1084,2081 > 389,1961,934,2081 > 389,1961,904,2081 | right | right | 389,1961,904,2081 | 84 | liaoqi_bubble_edge_right | false | none | false | false |
| 25 | 25 | 嗯嗯，那生意好才行☺️ | OTHER | OTHER | text | NONE | true | 191,2112,702,2196 | 0,2087,1084,2221 | 166,2087,934,2221 | 166,2087,934,2221 | 0,0,1084,2412 > 0,264,1084,2246 > 0,2084,1084,2233 > 0,2087,1084,2221 > 166,2087,934,2221 > 166,2087,934,2221 | left | unknown | 166,2087,934,2221 | 78 | liaoqi_text_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: liaoqi-n1989
- lastEffectiveMessageId: liaoqi-n1988
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
- coCreationOpportunity.exists: true
- coCreationOpportunity.type: SHARED_EXPECTATION
- unfinishedMeaning: 双方正在试探一种共同节奏。
- currentSceneSummary: [REDACTED_PRIVATE_CHAT]

## TacticalDecision
- decisionType: NORMAL_REPLY
- situation: [REDACTED_PRIVATE_CHAT]
- coreInsight: [REDACTED_PRIVATE_CHAT]
- userLikelyMistake: 回复太用力或太空。
- bestMove: [REDACTED_PRIVATE_CHAT]
- avoidMoves: 不要突然升浓度 / 不要连续讲自己
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: none
- fallbackMove: 那你先忙，我晚点再找你。

## ReplyRoutes
- route id: route-992722
  name: 稳妥
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-1165155
  name: 轻松
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-703425
  name: 反问
  routeType: CO_CREATION
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-25471585
  name: 接生活
  routeType: STABLE
  message: [REDACTED_PRIVATE_CHAT]
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-21135424
  name: 升一点
  routeType: WARM_UP
  message: [REDACTED_PRIVATE_CHAT]
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: 这条更主动，注意观察对方是否后撤。
  fallbackMove: none

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 26
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false
