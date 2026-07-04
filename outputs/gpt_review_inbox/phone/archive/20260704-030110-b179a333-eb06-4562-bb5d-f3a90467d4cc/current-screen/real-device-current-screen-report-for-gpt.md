# Real Device Current Screen Evidence Pack

- overall_result: PASS
- realDeviceFunctionalSmoke: PASS
- scenarioAssertionResult: PASS
- currentOverallResult: PASS
- generatedAt: 1783134068840
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
- capturedNodeCount: 48
- parserName: GenericVisualBubbleParser
- LiaoqiRealParserUsed: false
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
- cloudSkippedReason: UNSUPPORTED_APP
- decisionSource: LOCAL_FALLBACK
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
- visualSpeakerFallbackCount: 5
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
- rawParsedNodeCount: 9
- metadataFilteredCount: 2
- candidateChatMessageCount: 7
- unknownSpeakerCount: 1
- effectiveMessageCount: 1
- effectiveMeCount: 0
- effectiveOtherCount: 1
- parsedMessageCount: 9
- meCount: 2
- otherCount: 4
- unknownCount: 1
- unknownRatio: 0.11
- systemCount: 2
- voiceCount: 0
- imageCount: 0
- dateMetadataFilteredCount: 0
- messageStatusArtifactCount: 2
- readReceiptCount: 2
- deliveryStatusCount: 0
- lastMeDeliveryStatus: NONE
- lastMeReadStatus: READ
- statusArtifactsFilteredFromEffectiveMessages: true
- statusArtifactsAttachedToMessageCount: 0
- possible_speaker_conflict_count: 0
- lastEffectiveMessagePreview: 您好

### filteredMetadataSamples
- [READ_RECEIPT] 已读
- [READ_RECEIPT] 已读

### speakerReason 分布
- ambiguous_center_bounds: 1
- read_receipt_metadata: 2
- visual_projected_right: 2
- bubble_edge_left: 1
- visual_projected_left: 3

### UNKNOWN details
- id: bubble-n6832
  text: [REDACTED_PRIVATE_CHAT]
  textBounds: 454,193,629,234
  parentBounds: 428,111,656,240
  rowBounds: 428,111,656,240
  bubbleBounds: 428,111,656,240
  ancestorBoundsChain: 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240
  unknownReason: ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21

## 最近 30 条解析消息
[m001][unknown][unknown 30% ambiguous_center_bounds] 恩爱度14.9℃ rawNodeOrder=1 finalVisualOrder=1 rowBounds=428,111,656,240 textBounds=454,193,629,234 parentBounds=428,111,656,240 bubbleBounds=428,111,656,240 projectedBox=428,111,656,240 accessibilitySide=unknown visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=unknown sideMarginLeft=428 sideMarginRight=428 finalDecisionSource=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 unknownReason=ambiguous_center_or_balanced_bounds leftMargin=428 rightMargin=428 widthRatio=0.21 possible_speaker_conflict=false speakerReason=ambiguous_center_bounds
[m002][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=2 finalVisualOrder=2 rowBounds=24,242,1060,651 textBounds=173,567,251,609 parentBounds=24,242,1060,651 bubbleBounds=24,242,1060,651 projectedBox=24,242,1060,651 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m003][right][me 75% visual_projected_right] 看来你的时间是真按分钟算的。老师、医生、读博、副业老板，一个人干四份工作。以后我要是联系不上你，我就默认你是在救人或者改论文了。 rawNodeOrder=3 finalVisualOrder=3 rowBounds=275,242,904,621 textBounds=276,242,883,620 parentBounds=275,242,904,621 bubbleBounds=275,242,904,621 projectedBox=275,242,904,621 accessibilitySide=unknown visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=visual_projected_right unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_right
[m004][system][system 100% read_receipt_metadata] 已读 rawNodeOrder=4 finalVisualOrder=4 rowBounds=24,652,1060,1172 textBounds=173,1088,251,1130 parentBounds=24,652,1060,1172 bubbleBounds=24,652,1060,1172 projectedBox=24,652,1060,1172 accessibilitySide=left visualProjectedSide=unknown conflict=false conflictReason=none inferredSide=left sideMarginLeft=24 sideMarginRight=24 finalDecisionSource=metadata_filter unknownReason=none possible_speaker_conflict=false speakerReason=read_receipt_metadata
[m005][right][me 75% visual_projected_right] 你不用刻意找时间聊天，什么时候想起来了回我一句就行。我这边还有一个多月转业，这段时间正好慢慢认识，反而不用着急。 rawNodeOrder=5 finalVisualOrder=5 rowBounds=275,706,904,1142 textBounds=276,707,883,1141 parentBounds=275,706,904,1142 bubbleBounds=275,706,904,1142 projectedBox=275,706,904,1142 accessibilitySide=unknown visualProjectedSide=right conflict=false conflictReason=none inferredSide=right sideMarginLeft=275 sideMarginRight=180 finalDecisionSource=visual_projected_right unknownReason=ambiguous_center_or_balanced_bounds leftMargin=275 rightMargin=180 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_right
[m006][left][other 82% bubble_edge_left] 您好 rawNodeOrder=6 finalVisualOrder=6 rowBounds=180,1227,365,1365 textBounds=201,1229,363,1363 parentBounds=180,1227,365,1365 bubbleBounds=180,1227,365,1365 projectedBox=180,1227,365,1365 accessibilitySide=left visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=719 finalDecisionSource=bubble_edge_left unknownReason=none possible_speaker_conflict=false speakerReason=bubble_edge_left
[m007][left][other 75% visual_projected_left] 我不太喜欢这种聊天方式 rawNodeOrder=7 finalVisualOrder=7 rowBounds=180,1450,797,1588 textBounds=201,1452,795,1586 parentBounds=180,1450,797,1588 bubbleBounds=180,1450,797,1588 projectedBox=180,1450,797,1588 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=287 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=287 widthRatio=0.57 possible_speaker_conflict=false speakerReason=visual_projected_left
[m008][left][other 75% visual_projected_left] 我认为通话简单聊聊 可以见面就见个面，不能见面就算了 rawNodeOrder=8 finalVisualOrder=8 rowBounds=180,1673,810,1931 textBounds=201,1675,808,1929 parentBounds=180,1673,810,1931 bubbleBounds=180,1673,810,1931 projectedBox=180,1673,810,1931 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_left
[m009][left][other 75% visual_projected_left] 如果能等可以等到你专业再聊 rawNodeOrder=9 finalVisualOrder=9 rowBounds=180,2016,810,2214 textBounds=201,2018,808,2212 parentBounds=180,2016,810,2214 bubbleBounds=180,2016,810,2214 projectedBox=180,2016,810,2214 accessibilitySide=unknown visualProjectedSide=left conflict=false conflictReason=none inferredSide=left sideMarginLeft=180 sideMarginRight=274 finalDecisionSource=visual_projected_left unknownReason=ambiguous_center_or_balanced_bounds leftMargin=180 rightMargin=274 widthRatio=0.58 possible_speaker_conflict=false speakerReason=visual_projected_left

## Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 恩爱度14.9℃ | UNKNOWN | UNKNOWN | text | NONE | false | 454,193,629,234 | 428,111,656,240 | 428,111,656,240 | 428,111,656,240 | 0,0,1084,2412 > 308,111,776,240 > 428,111,656,240 | unknown | unknown | 428,111,656,240 | 30 | ambiguous_center_bounds | false | none | false | false |
| 2 | 2 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,567,251,609 | 24,242,1060,651 | 24,242,1060,651 | 24,242,1060,651 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,651 > 24,242,1060,651 | left | unknown | 24,242,1060,651 | 100 | read_receipt_metadata | false | none | false | false |
| 3 | 3 | 看来你的时间是真按分钟算的。老师、医生、读博、副业老板，一个人干四份工作。以后我要是联系不上你，我就默认你是在救人或者改论文了。 | ME | ME | text | NONE | false | 276,242,883,620 | 275,242,904,621 | 275,242,904,621 | 275,242,904,621 | 0,0,1084,2412 > 0,242,1084,2261 > 0,242,1084,651 > 24,242,1060,651 > 275,242,904,621 | unknown | right | 275,242,904,621 | 75 | visual_projected_right | false | none | true | false |
| 4 | 4 | 已读 | SYSTEM | SYSTEM | text | READ_RECEIPT | false | 173,1088,251,1130 | 24,652,1060,1172 | 24,652,1060,1172 | 24,652,1060,1172 | 0,0,1084,2412 > 0,242,1084,2261 > 0,652,1084,1172 > 24,652,1060,1172 | left | unknown | 24,652,1060,1172 | 100 | read_receipt_metadata | false | none | false | false |
| 5 | 5 | 你不用刻意找时间聊天，什么时候想起来了回我一句就行。我这边还有一个多月转业，这段时间正好慢慢认识，反而不用着急。 | ME | ME | text | NONE | false | 276,707,883,1141 | 275,706,904,1142 | 275,706,904,1142 | 275,706,904,1142 | 0,0,1084,2412 > 0,242,1084,2261 > 0,652,1084,1172 > 24,652,1060,1172 > 275,706,904,1142 | unknown | right | 275,706,904,1142 | 75 | visual_projected_right | false | none | true | false |
| 6 | 6 | 您好 | OTHER | OTHER | text | NONE | true | 201,1229,363,1363 | 180,1227,365,1365 | 180,1227,365,1365 | 180,1227,365,1365 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1173,1084,1395 > 24,1173,1060,1395 > 180,1227,365,1365 | left | left | 180,1227,365,1365 | 82 | bubble_edge_left | false | none | false | false |
| 7 | 7 | 我不太喜欢这种聊天方式 | OTHER | OTHER | text | NONE | false | 201,1452,795,1586 | 180,1450,797,1588 | 180,1450,797,1588 | 180,1450,797,1588 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1396,1084,1618 > 24,1396,1060,1618 > 180,1450,797,1588 | unknown | left | 180,1450,797,1588 | 75 | visual_projected_left | false | none | true | false |
| 8 | 8 | 我认为通话简单聊聊 可以见面就见个面，不能见面就算了 | OTHER | OTHER | text | NONE | false | 201,1675,808,1929 | 180,1673,810,1931 | 180,1673,810,1931 | 180,1673,810,1931 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1619,1084,1961 > 24,1619,1060,1961 > 180,1673,810,1931 | unknown | left | 180,1673,810,1931 | 75 | visual_projected_left | false | none | true | false |
| 9 | 9 | 如果能等可以等到你专业再聊 | OTHER | OTHER | text | NONE | false | 201,2018,808,2212 | 180,2016,810,2214 | 180,2016,810,2214 | 180,2016,810,2214 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1962,1084,2244 > 24,1962,1060,2244 > 180,2016,810,2214 | unknown | left | 180,2016,810,2214 | 75 | visual_projected_left | false | none | true | false |

## Effective Visual Order Table

| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 6 | 6 | 您好 | OTHER | OTHER | text | NONE | true | 201,1229,363,1363 | 180,1227,365,1365 | 180,1227,365,1365 | 180,1227,365,1365 | 0,0,1084,2412 > 0,242,1084,2261 > 0,1173,1084,1395 > 24,1173,1060,1395 > 180,1227,365,1365 | left | left | 180,1227,365,1365 | 82 | bubble_edge_left | false | none | false | false |

## LastSpeakerDecision
- lastRawNodeId: bubble-n6860
- lastEffectiveMessageId: bubble-n6848
- lastEffectiveMessageText: [REDACTED_PRIVATE_CHAT]
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: true
- decisionType: CONTEXT_REQUIRED
- reason: 最后一句是对方，可以生成下一句。

## ContextAssembler
- contextCompleteness.score: 64
- canDeepAnalyze: false
- missingTypes: NOT_ENOUGH_MESSAGES, PREVIOUS_TURN_MISSING
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
- message_nodes written count: 9
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: true
