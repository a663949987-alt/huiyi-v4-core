# Real Device Current Screen Evidence Pack

- overall_result: FAIL
- generatedAt: 2026-07-02T22:45:00+08:00
- sample_source: unknown
- appPackage: unknown
- windowTitle: unknown
- screenWidth: 0
- screenHeight: 0
- serviceConnected: false
- rootAvailable: false
- capturedNodeCount: 0
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false

## 解析结果

- parsedMessageCount: 0
- meCount: 0
- otherCount: 0
- unknownCount: 0
- systemCount: 0
- voiceCount: 0
- imageCount: 0
- speakerReason 分布: none

## 最近 30 条解析消息

none

## LastSpeakerDecision

- lastEffectiveMessageId: none
- lastSpeaker: none
- shouldReply: false
- decisionType: CONTEXT_REQUIRED
- reason: 当前尚未在真机聊天 App 中完成无障碍 root 捕获。

## ContextAssembler

- contextCompleteness.score: 0
- canDeepAnalyze: false
- missingTypes: NOT_TESTED
- coCreationOpportunity.exists: false
- coCreationOpportunity.type: none
- unfinishedMeaning: none
- currentSceneSummary: none

## TacticalDecision

- decisionType: CONTEXT_REQUIRED
- situation: 未完成真机捕获。
- coreInsight: 需要在真实聊天 App 中点击悬浮球“下一句”后导出证据包。
- userLikelyMistake: none
- bestMove: 按 App 开发者页说明完成真机证据导出。
- avoidMoves: 不要用 local sample 代替真机报告。
- influenceIntensity: LOW
- riskLevel: MEDIUM
- riskWarning: sample_source 不是 real_device_accessibility，自动 FAIL。
- fallbackMove: 安装 debug APK 后进行真机测试。

## ReplyRoutes

- routes: empty
- reason: 未完成真机捕获。

## VoiceSummary

- voiceMessages: none

## Persistence

- message_nodes written count: 0
- chat_scenes written count: 0
- reply_attempt created count after copy: 0
- last error: none

## UI State

- FloatingTacticalPanel shown: false
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false

## 自动 FAIL 原因

- sample_source 不是 `real_device_accessibility`。
- 无真实 appPackage。
- 当前报告不是来自真机无障碍 root。
