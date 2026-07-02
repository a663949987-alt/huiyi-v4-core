# MockChat Validation Report

- sample_source: emulator_mock_chat_accessibility
- appPackage: com.huiyi.mockchat
- 模拟器是否可跑: 已创建并启动 `Huawei_nova_11_API_36`，分辨率 `1084x2412`，密度 `395`。
- mockchat 是否已安装: 已安装，包名 `com.huiyi.mockchat`。
- 无障碍是否能读取 mockchat: 会意无障碍服务已在模拟器中启用，当前前台为 `com.huiyi.mockchat/.MainActivity`，仍需点击悬浮球做一次端到端确认。
- overlay 是否显示在 mockchat 上方: 悬浮窗权限已允许，代码路径保持悬浮层，不打开会意 MainActivity；仍需点击悬浮球确认窗口层级。
- 是否仍需真机验证: 需要。

## 手动模拟器测试说明

1. 安装会意 App：`outputs/huiyi-v4.1.3-debug.apk`
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
