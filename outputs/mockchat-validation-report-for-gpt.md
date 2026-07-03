# MockChat Validation Report

- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- 模拟器是否可跑: 已创建并启动 `Huawei_nova_11_API_36`，分辨率 `1084x2412`，密度 `395`。
- mockchat 是否已安装: 已安装，包名 `com.huiyi.mockchat`。
- 无障碍是否能读取 mockchat: 已确认。nova 11 模拟器中会意无障碍服务已启用，MockChatLab 的标题、时间戳、左右气泡、图片占位、输入栏都能作为真实无障碍节点读取。
- overlay 是否显示在 mockchat 上方: 已确认。点击悬浮球“下一句”后，结果面板显示在 `com.huiyi.mockchat/.MainActivity` 上方，未跳回会意 MainActivity。
- 是否仍需真机验证: 仍需。MockChatLab 已稳定复现当前屏幕解析链路，但真实聊天 App 兼容性还要用真机补测。

## 模拟器端到端实测

- 设备: `Huawei_nova_11_API_36`
- 分辨率/密度: `1084x2412` / `395`
- 当前场景: `B last_other`
- 当前前台: `com.huiyi.mockchat/.MainActivity`
- 会意悬浮窗: active，系统窗口中可见 `com.huiyi.v4` alert window
- 操作: 打开 MockChatLab -> 点击会意悬浮球 -> 点击“下一句”
- 实测结果: PASS，结果面板覆盖在 MockChatLab 上方，生成 5 条回复路线
- 截图: `outputs/mockchat_screenshots/mockchat_end_to_end_last_other.png`
- MockChat 节点截图: `outputs/mockchat_screenshots/mockchat_visible_messages_final.png`

## 手动模拟器测试说明

1. 安装会意 App：`outputs/huiyi-v4.1.2-debug.apk`
2. 安装 MockChatLab：`outputs/mockchat-debug.apk`
3. 或连接设备后运行：`scripts/install-mockchat-lab.ps1 -Scenario last_other`
4. 在模拟器/手机设置里开启会意无障碍服务。
5. 开启会意悬浮窗权限。
6. 打开 MockChatLab 对应场景。
7. 点击会意悬浮球“下一句”。
8. 确认结果浮层显示在 MockChatLab 上方，而不是打开会意 MainActivity。
9. 在会意 App 中导出当前屏幕证据包。

- 场景 A last_me: PASS
  lastSpeaker: ME
  decisionType: WAIT
  routesCount: 0
- 场景 B last_other: PASS
  lastSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routesCount: 5
- 场景 C metadata_trap: PASS
  lastSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routesCount: 5
- 场景 D voice_last_other: PASS
  lastSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routesCount: 5
- 场景 E unknown_bounds: PASS
  lastSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routesCount: 0
- 场景 F low_expression: PASS
  lastSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routesCount: 5

## 自动 FAIL 条件覆盖
- sample_source 仍是 local_validation_sample: PASS，报告为 emulator_mock_chat_accessibility。
- appPackage 不是 com.huiyi.mockchat: PASS。
- 场景 C 时间戳/昵称/在线状态污染 LastSpeakerDecision: PASS，全部进入 MetadataMessageFilter。
- 场景 A 最后一句是我却生成 routes: PASS，routes empty。
- 场景 B 最后一句是对方却 WAIT: PASS。
- 场景 D 语音未转写却当普通文本分析: PASS，VOICE_SUMMARY_REQUIRED。
- 浮层结果打开会意 MainActivity: PASS，huiyiActivityOpened=false。
