# MockChat Current Screen Report

- 模拟器是否可跑: 已创建并启动 `Huawei_nova_11_API_36`，分辨率 `1084x2412`，密度 `395`。
- mockchat 是否已安装: 已安装，包名 `com.huiyi.mockchat`。
- 无障碍是否能读取 mockchat: 已确认。MockChatLab 的标题、在线状态、时间戳、左右气泡、图片占位、输入栏都能作为真实无障碍节点读取。
- overlay 是否显示在 mockchat 上方: 已确认。点击会意悬浮球“下一句”后，结果面板显示在 MockChatLab 上方，当前前台仍是 `com.huiyi.mockchat/.MainActivity`。
- 是否仍需真机验证: 仍需。mockchat 用于稳定复现解析问题，不能替代真实聊天 App 兼容性。

## 模拟器端到端实测

- 设备: `Huawei_nova_11_API_36`
- 场景: `B last_other`
- 当前前台: `com.huiyi.mockchat/.MainActivity`
- 结果: PASS，LastSpeakerDecision = OTHER，TacticalDecision = NORMAL_REPLY，ReplyRoutes = 5
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: `com.huiyi.mockchat`
- huiyiActivityOpened: false
- 截图: `outputs/mockchat_screenshots/mockchat_end_to_end_last_other.png`

## scenarioName: last_me

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/last_me
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 12
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 12
- metadataFilteredCount: 10
- effectiveMessageCount: 2
- effectiveMeCount: 1
- effectiveOtherCount: 1
- parsedMessageCount: 12
- meCount: 1
- otherCount: 1
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 10
- voiceCount: 0
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] A last_me
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 1
- bubble_edge_left: 1
- bubble_edge_right: 1

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] A last_me rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other 82% bubble_edge_left] 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][right][me 82% bubble_edge_right] 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。 rowBounds=380,356,1080,464 textBounds=470,356,1020,464 inferredSide=right speakerReason=bubble_edge_right
[m009][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m010][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m011][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m012][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-m1
- lastEffectiveMessageText: 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。
- lastEffectiveSpeaker: ME
- lastSpeaker: ME
- shouldReply: false
- decisionType: WAIT
- reason: 最后一句是你发的，先等她回，不要继续补话。

## ContextAssembler
- contextCompleteness.score: 64
- canDeepAnalyze: false
- missingTypes: NOT_ENOUGH_MESSAGES, PREVIOUS_TURN_MISSING
- coCreationOpportunity.exists: false
- coCreationOpportunity.type: NO_OPPORTUNITY
- unfinishedMeaning: none
- currentSceneSummary: 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… | 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。

## TacticalDecision
- decisionType: WAIT
- situation: 最后一句是我。
- coreInsight: 现在继续补话会稀释表达。
- userLikelyMistake: 忍不住继续解释或加码承诺。
- bestMove: 等对方回复，不要继续补话。
- avoidMoves: 不要追问 / 不要追加保证 / 不要解释太多
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: none
- fallbackMove: 如果很久没回，再发一条轻生活关心。

## ReplyRoutes
- routes: empty
- reason: WAIT or blocked by missing voice/context/unknown speaker.

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 12
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: true
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false


## scenarioName: last_other

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/last_other
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 19
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 19
- metadataFilteredCount: 13
- effectiveMessageCount: 6
- effectiveMeCount: 1
- effectiveOtherCount: 5
- parsedMessageCount: 19
- meCount: 1
- otherCount: 5
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 13
- voiceCount: 0
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] B last_other
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [TIME] 11:00
- [TIME] 10:58
- [TIME] 10:59
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 4
- bubble_edge_left: 5
- bubble_edge_right: 1

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] B last_other rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other 82% bubble_edge_left] 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][right][me 82% bubble_edge_right] 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。 rowBounds=380,356,1080,464 textBounds=470,356,1020,464 inferredSide=right speakerReason=bubble_edge_right
[m009][system][system 100% time_metadata] 11:00 rowBounds=0,482,1080,524 textBounds=455,482,625,524 inferredSide=unknown speakerReason=time_metadata
[m010][left][other 82% bubble_edge_left] 是啊，我离婚是10年了呀。 rowBounds=0,542,700,650 textBounds=56,542,620,650 inferredSide=left speakerReason=bubble_edge_left
[m011][system][system 100% time_metadata] 10:58 rowBounds=0,668,1080,710 textBounds=455,668,625,710 inferredSide=unknown speakerReason=time_metadata
[m012][left][other 82% bubble_edge_left] 小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过…… rowBounds=0,728,700,836 textBounds=56,728,620,836 inferredSide=left speakerReason=bubble_edge_left
[m013][left][other 82% bubble_edge_left] 图片占位 rowBounds=0,854,700,962 textBounds=56,854,620,962 inferredSide=left speakerReason=bubble_edge_left
[m014][system][system 100% time_metadata] 10:59 rowBounds=0,980,1080,1022 textBounds=455,980,625,1022 inferredSide=unknown speakerReason=time_metadata
[m015][left][other 82% bubble_edge_left] 本来我的过去我不想再提离婚，都10年了，孩子也舍不得…… rowBounds=0,1040,700,1148 textBounds=56,1040,620,1148 inferredSide=left speakerReason=bubble_edge_left
[m016][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m017][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m018][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m019][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-o4
- lastEffectiveMessageText: 本来我的过去我不想再提离婚，都10年了，孩子也舍不得……
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
- currentSceneSummary: 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… | 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。 | 是啊，我离婚是10年了呀。 小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过…… 图片占位 本来我的过去我不想再提离婚，都10年了，孩子也舍不得……

## TacticalDecision
- decisionType: NORMAL_REPLY
- situation: 普通聊天推进。
- coreInsight: 保持稳定、接生活、轻轻推进。
- userLikelyMistake: 回复太用力或太空。
- bestMove: 接住她当前内容，给一个轻问题。
- avoidMoves: 不要突然升浓度 / 不要连续讲自己
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: none
- fallbackMove: 那你先忙，我晚点再找你。

## ReplyRoutes
- route id: route-992722
  name: 稳妥
  routeType: STABLE
  message: 嗯，我懂你的意思。那你现在是更想先休息，还是想继续说说？
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-1165155
  name: 轻松
  routeType: STABLE
  message: 听起来今天不太省心，先给你记一笔辛苦分。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-703425
  name: 反问
  routeType: CO_CREATION
  message: 那你希望我现在怎么接你，会更舒服一点？
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-25471585
  name: 接生活
  routeType: STABLE
  message: 你先把手头的事弄完，别让自己一直绷着。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-21135424
  name: 升一点
  routeType: WARM_UP
  message: 我还挺喜欢你愿意跟我说这些的。
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: 这条更主动，注意观察对方是否后撤。
  fallbackMove: none

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


## scenarioName: metadata_trap

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/metadata_trap
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 20
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 20
- metadataFilteredCount: 14
- effectiveMessageCount: 6
- effectiveMeCount: 1
- effectiveOtherCount: 5
- parsedMessageCount: 20
- meCount: 1
- otherCount: 5
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 14
- voiceCount: 0
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] C metadata_trap
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [TIME] 11:00
- [TIME] 10:58
- [TIME] 10:59
- [TIME] 11:02
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 5
- bubble_edge_left: 5
- bubble_edge_right: 1

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] C metadata_trap rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other 82% bubble_edge_left] 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][right][me 82% bubble_edge_right] 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。 rowBounds=380,356,1080,464 textBounds=470,356,1020,464 inferredSide=right speakerReason=bubble_edge_right
[m009][system][system 100% time_metadata] 11:00 rowBounds=0,482,1080,524 textBounds=455,482,625,524 inferredSide=unknown speakerReason=time_metadata
[m010][left][other 82% bubble_edge_left] 是啊，我离婚是10年了呀。 rowBounds=0,542,700,650 textBounds=56,542,620,650 inferredSide=left speakerReason=bubble_edge_left
[m011][system][system 100% time_metadata] 10:58 rowBounds=0,668,1080,710 textBounds=455,668,625,710 inferredSide=unknown speakerReason=time_metadata
[m012][left][other 82% bubble_edge_left] 小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过…… rowBounds=0,728,700,836 textBounds=56,728,620,836 inferredSide=left speakerReason=bubble_edge_left
[m013][left][other 82% bubble_edge_left] 图片占位 rowBounds=0,854,700,962 textBounds=56,854,620,962 inferredSide=left speakerReason=bubble_edge_left
[m014][system][system 100% time_metadata] 10:59 rowBounds=0,980,1080,1022 textBounds=455,980,625,1022 inferredSide=unknown speakerReason=time_metadata
[m015][left][other 82% bubble_edge_left] 本来我的过去我不想再提离婚，都10年了，孩子也舍不得…… rowBounds=0,1040,700,1148 textBounds=56,1040,620,1148 inferredSide=left speakerReason=bubble_edge_left
[m016][system][system 100% time_metadata] 11:02 rowBounds=0,1166,1080,1208 textBounds=455,1166,625,1208 inferredSide=unknown speakerReason=time_metadata
[m017][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m018][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m019][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m020][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-o4
- lastEffectiveMessageText: 本来我的过去我不想再提离婚，都10年了，孩子也舍不得……
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
- currentSceneSummary: 孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费…… | 你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。 | 是啊，我离婚是10年了呀。 小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过…… 图片占位 本来我的过去我不想再提离婚，都10年了，孩子也舍不得……

## TacticalDecision
- decisionType: NORMAL_REPLY
- situation: 普通聊天推进。
- coreInsight: 保持稳定、接生活、轻轻推进。
- userLikelyMistake: 回复太用力或太空。
- bestMove: 接住她当前内容，给一个轻问题。
- avoidMoves: 不要突然升浓度 / 不要连续讲自己
- influenceIntensity: LOW
- riskLevel: LOW
- riskWarning: none
- fallbackMove: 那你先忙，我晚点再找你。

## ReplyRoutes
- route id: route-992722
  name: 稳妥
  routeType: STABLE
  message: 嗯，我懂你的意思。那你现在是更想先休息，还是想继续说说？
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-1165155
  name: 轻松
  routeType: STABLE
  message: 听起来今天不太省心，先给你记一笔辛苦分。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-703425
  name: 反问
  routeType: CO_CREATION
  message: 那你希望我现在怎么接你，会更舒服一点？
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-25471585
  name: 接生活
  routeType: STABLE
  message: 你先把手头的事弄完，别让自己一直绷着。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-21135424
  name: 升一点
  routeType: WARM_UP
  message: 我还挺喜欢你愿意跟我说这些的。
  intensity: MEDIUM
  riskLevel: MEDIUM
  riskWarning: 这条更主动，注意观察对方是否后撤。
  fallbackMove: none

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 20
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: false


## scenarioName: voice_last_other

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/voice_last_other
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 13
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 13
- metadataFilteredCount: 10
- effectiveMessageCount: 3
- effectiveMeCount: 1
- effectiveOtherCount: 2
- parsedMessageCount: 13
- meCount: 1
- otherCount: 2
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 10
- voiceCount: 2
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] D voice_last_other
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 1
- bubble_edge_left: 2
- bubble_edge_right: 1

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] D voice_last_other rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other voice missing] [语音 ?秒] rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][right][me 82% bubble_edge_right] 好，我听。 rowBounds=380,356,1080,464 textBounds=470,356,1020,464 inferredSide=right speakerReason=bubble_edge_right
[m009][left][other voice missing] [语音 18秒] rowBounds=0,482,520,558 textBounds=56,482,360,558 inferredSide=left speakerReason=bubble_edge_left
[m010][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m011][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m012][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m013][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-voice1
- lastEffectiveMessageText: none
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: false
- decisionType: VOICE_SUMMARY_REQUIRED
- reason: 最后一条是未转写语音，需要先补摘要。

## ContextAssembler
- contextCompleteness.score: 46
- canDeepAnalyze: false
- missingTypes: NOT_ENOUGH_MESSAGES, PREVIOUS_TURN_MISSING, VOICE_WITHOUT_TRANSCRIPT
- coCreationOpportunity.exists: false
- coCreationOpportunity.type: NO_OPPORTUNITY
- unfinishedMeaning: none
- currentSceneSummary:  | 好，我听。 | 

## TacticalDecision
- decisionType: VOICE_SUMMARY_REQUIRED
- situation: 关键最后一条是未转写语音。
- coreInsight: 不知道语音内容时不能猜测她的意思。
- userLikelyMistake: 根据气氛硬猜，生成高风险回复。
- bestMove: 听完后补一句摘要，我再帮你判断怎么回。
- avoidMoves: 不要猜语音内容 / 不要强行深度分析
- influenceIntensity: LOW
- riskLevel: MEDIUM
- riskWarning: 缺少语音内容，深度判断不可靠。
- fallbackMove: 我先听一下，听完认真回你。

## ReplyRoutes
- route id: route-666596
  name: 先听
  routeType: WAIT
  message: 我先听一下，听完认真回你。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-34386766
  name: 补摘要
  routeType: WAIT
  message: 听完后补一句摘要，再判断。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-648879
  name: 不猜
  routeType: WAIT
  message: 不猜语音内容。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-36289969
  name: 轻确认
  routeType: STABLE
  message: 我听完再回你，不敷衍。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-1002844
  name: 等待
  routeType: WAIT
  message: 先不要生成深度回复。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none

## VoiceSummary
- voiceMessageId: bubble-o1
  speaker: OTHER
  duration: unknown
  transcriptStatus: MISSING
  whether VoiceSummaryCard shown: true
  userSummary: none
- voiceMessageId: bubble-voice1
  speaker: OTHER
  duration: 18
  transcriptStatus: MISSING
  whether VoiceSummaryCard shown: true
  userSummary: none

## Persistence
- message_nodes written count: 13
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: true
- ContextRequiredCard shown: false


## scenarioName: unknown_bounds

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/unknown_bounds
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 13
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 13
- metadataFilteredCount: 11
- effectiveMessageCount: 2
- effectiveMeCount: 0
- effectiveOtherCount: 2
- parsedMessageCount: 13
- meCount: 0
- otherCount: 2
- unknownCount: 1
- unknownRatio: 0.08
- systemCount: 10
- voiceCount: 0
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] E unknown_bounds
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [NONE] 我也不知道这句应该算谁在说。
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 1
- bubble_edge_left: 2
- ambiguous_center_bounds: 1

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] E unknown_bounds rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other 82% bubble_edge_left] 这个事情我有点不知道怎么说。 rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][left][other 82% bubble_edge_left] 你先别急着回。 rowBounds=0,356,700,464 textBounds=56,356,620,464 inferredSide=left speakerReason=bubble_edge_left
[m009][unknown][unknown 30% ambiguous_center_bounds] 我也不知道这句应该算谁在说。 rowBounds=350,482,730,574 textBounds=415,482,665,574 inferredSide=unknown speakerReason=ambiguous_center_bounds
[m010][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m011][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m012][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m013][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-o2
- lastEffectiveMessageText: 你先别急着回。
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
- currentSceneSummary: 这个事情我有点不知道怎么说。 你先别急着回。

## TacticalDecision
- decisionType: CONTEXT_REQUIRED
- situation: 说话人不确定。
- coreInsight: 当前屏幕存在边界不清的聊天气泡，不允许高置信度生成。
- userLikelyMistake: 在没分清是谁说的时候生成回复。
- bestMove: 切换我的气泡方向，或补充这句是谁说的。
- avoidMoves: 不要调用模型 / 不要猜说话人
- influenceIntensity: LOW
- riskLevel: MEDIUM
- riskWarning: 说话人不确定，判断可能反向。
- fallbackMove: 先确认这句是谁说的。

## ReplyRoutes
- routes: empty
- reason: WAIT or blocked by missing voice/context/unknown speaker.

## VoiceSummary
- voiceMessages: none

## Persistence
- message_nodes written count: 13
- chat_scenes written count: 1
- reply_attempt created count after copy: not measured in this capture
- last error: none

## UI State
- FloatingTacticalPanel shown: true
- WAIT panel shown: false
- VoiceSummaryCard shown: false
- ContextRequiredCard shown: true


## scenarioName: low_expression

# Real Device Current Screen Evidence Pack

- overall_result: PASS
- generatedAt: 1783008000000
- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- windowTitle: MockChatLab/low_expression
- screenWidth: 1080
- screenHeight: 2400
- serviceConnected: true
- rootAvailable: true
- capturedNodeCount: 15
- parserName: GenericVisualBubbleParser
- parserFallbackUsed: false
- currentBubbleSideRule: right=me
- modelCalled: false
- apiCalled: false
- overlayShownInTargetApp: true
- foregroundPackageWhenPanelShown: com.huiyi.mockchat
- huiyiActivityOpened: false
- userStayedInChatApp: true
- resultShownAsOverlay: true
- mainActivityOpened: false

## 解析结果
- rawParsedNodeCount: 15
- metadataFilteredCount: 10
- effectiveMessageCount: 5
- effectiveMeCount: 0
- effectiveOtherCount: 5
- parsedMessageCount: 15
- meCount: 0
- otherCount: 5
- unknownCount: 0
- unknownRatio: 0.00
- systemCount: 10
- voiceCount: 0
- imageCount: 0

### filteredMetadataSamples
- [UI_CONTROL] 返回
- [HEADER] 白云蓝天
- [ONLINE_STATUS] 上次在线时间07-02 18:06
- [UI_CONTROL] F low_expression
- [UI_CONTROL] 导出截图
- [TIME] 10:56
- [UI_CONTROL] 语音
- [UI_CONTROL] 输入框
- [UI_CONTROL] 表情
- [UI_CONTROL] 发送

### speakerReason 分布
- ui_control_metadata: 7
- header_metadata: 1
- online_status_metadata: 1
- time_metadata: 1
- bubble_edge_left: 5

## 最近 30 条解析消息
[m001][system][system 100% ui_control_metadata] 返回 rowBounds=24,12,116,70 textBounds=24,12,116,70 inferredSide=left speakerReason=ui_control_metadata
[m002][system][system 100% header_metadata] 白云蓝天 rowBounds=0,12,1080,62 textBounds=380,12,700,62 inferredSide=unknown speakerReason=header_metadata
[m003][system][system 100% online_status_metadata] 上次在线时间07-02 18:06 rowBounds=0,64,1080,96 textBounds=380,64,700,96 inferredSide=unknown speakerReason=online_status_metadata
[m004][system][system 100% ui_control_metadata] F low_expression rowBounds=30,102,330,140 textBounds=30,102,330,140 inferredSide=left speakerReason=ui_control_metadata
[m005][system][system 100% ui_control_metadata] 导出截图 rowBounds=860,102,1040,150 textBounds=860,102,1040,150 inferredSide=right speakerReason=ui_control_metadata
[m006][system][system 100% time_metadata] 10:56 rowBounds=0,170,1080,212 textBounds=455,170,625,212 inferredSide=unknown speakerReason=time_metadata
[m007][left][other 82% bubble_edge_left] 嗯 rowBounds=0,230,700,338 textBounds=56,230,620,338 inferredSide=left speakerReason=bubble_edge_left
[m008][left][other 82% bubble_edge_left] 好 rowBounds=0,356,700,464 textBounds=56,356,620,464 inferredSide=left speakerReason=bubble_edge_left
[m009][left][other 82% bubble_edge_left] 没事 rowBounds=0,482,700,590 textBounds=56,482,620,590 inferredSide=left speakerReason=bubble_edge_left
[m010][left][other 82% bubble_edge_left] 忙 rowBounds=0,608,700,716 textBounds=56,608,620,716 inferredSide=left speakerReason=bubble_edge_left
[m011][left][other 82% bubble_edge_left] 晚点说 rowBounds=0,734,700,842 textBounds=56,734,620,842 inferredSide=left speakerReason=bubble_edge_left
[m012][system][system 100% ui_control_metadata] 语音 rowBounds=20,2240,120,2310 textBounds=20,2240,120,2310 inferredSide=left speakerReason=ui_control_metadata
[m013][system][system 100% ui_control_metadata] 输入框 rowBounds=140,2240,760,2310 textBounds=140,2240,760,2310 inferredSide=left speakerReason=ui_control_metadata
[m014][system][system 100% ui_control_metadata] 表情 rowBounds=780,2240,880,2310 textBounds=780,2240,880,2310 inferredSide=right speakerReason=ui_control_metadata
[m015][system][system 100% ui_control_metadata] 发送 rowBounds=900,2240,1040,2310 textBounds=900,2240,1040,2310 inferredSide=right speakerReason=ui_control_metadata

## LastSpeakerDecision
- lastRawNodeId: bubble-input-send
- lastEffectiveMessageId: bubble-o5
- lastEffectiveMessageText: 晚点说
- lastEffectiveSpeaker: OTHER
- lastSpeaker: OTHER
- shouldReply: true
- decisionType: BOUNDARY_RESPECT
- reason: 最后一句是对方，可以生成下一句。

## ContextAssembler
- contextCompleteness.score: 82
- canDeepAnalyze: true
- missingTypes: PREVIOUS_TURN_MISSING
- coCreationOpportunity.exists: false
- coCreationOpportunity.type: NO_OPPORTUNITY
- unfinishedMeaning: none
- currentSceneSummary: 嗯 好 没事 忙 晚点说

## TacticalDecision
- decisionType: BOUNDARY_RESPECT
- situation: 对方在收住话题。
- coreInsight: 继续追问会把压力推回给她。
- userLikelyMistake: 急着解释、证明或继续承诺。
- bestMove: 收住，给具体关心，留出空间。
- avoidMoves: 不要继续追问 / 不要继续承诺 / 不要把话题拉回自己
- influenceIntensity: LOW
- riskLevel: MEDIUM
- riskWarning: 对方已经后撤，强推会失分。
- fallbackMove: 你先忙，晚点我再看你状态。

## ReplyRoutes
- route id: route-823513
  name: 收住
  routeType: COOL_DOWN
  message: 好，我先不追着你说。你先忙/先缓一缓，我晚点再看你状态。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-657119
  name: 修复
  routeType: REPAIR
  message: 刚才我可能有点急了，不想让你有压力。你不用马上回应我。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-37868224
  name: 降浓度
  routeType: STABLE
  message: 我明白，你先把自己的节奏放前面。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-36241512
  name: 转生活
  routeType: STABLE
  message: 那你先去忙，记得吃点东西，别一直硬撑。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none
- route id: route-1002844
  name: 等待
  routeType: WAIT
  message: 先不发，等她下一句。
  intensity: LOW
  riskLevel: LOW
  riskWarning: none
  fallbackMove: none

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
