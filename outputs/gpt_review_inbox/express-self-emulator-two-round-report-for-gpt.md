# Express Self Emulator Two Round Report

- taskName: express_self_ui_loop_emulator_two_round_smoke
- versionName: 4.1.56
- versionCode: 475
- emulatorDetected: true
- emulatorSerial: emulator-5554
- accessibilityEnabled: true
- accessibilityService: com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService
- overlayPermissionGranted: true
- mockchatScenario: WECHAT_LIKE / last_other
- overallResult: PASS
- userCanStartRealDeviceTest: true

## Round 1

- result: PASS
- clickAckVisible: true
- clickAckLatencyMs: 6
- terminalState: EXPRESS_SELF_PANEL
- panelShown: true
- activePackageAtClick: com.huiyi.mockchat
- activeWindowTitleAtClick: MockChatLab
- rootAvailableAtClick: true
- serviceConnected: true
- accessibilityRuntimeCategory: CONNECTED_AND_READY
- cloudAttempted: false
- screenshot: outputs/gpt_review_inbox/express_self_emulator_smoke/round1_valid2_panel.png
- sessionJson: outputs/gpt_review_inbox/express_self_emulator_smoke/round1_valid2_session.json

Visible panel checks:

- 表达我: shown
- 本轮动作: shown
- 她给的窗口: shown
- 适合露出的你: shown
- 建议句: shown
- 别说过头: shown
- 人物弧光 route: shown
- 像我 / 不像我 / 太油 / 太重 feedback: shown

## Round 2

- result: PASS
- clickAckVisible: true
- clickAckLatencyMs: 3
- terminalState: EXPRESS_SELF_PANEL
- panelShown: true
- activePackageAtClick: com.huiyi.mockchat
- activeWindowTitleAtClick: MockChatLab
- rootAvailableAtClick: true
- serviceConnected: true
- accessibilityRuntimeCategory: CONNECTED_AND_READY
- cloudAttempted: false
- screenshot: outputs/gpt_review_inbox/express_self_emulator_smoke/round2_valid_panel.png
- sessionJson: outputs/gpt_review_inbox/express_self_emulator_smoke/round2_valid_session.json

Visible panel checks:

- 表达我: shown
- 本轮动作: shown
- 她给的窗口: shown
- 适合露出的你: shown
- 建议句: shown
- 别说过头: shown
- 人物弧光 route: shown
- 像我 / 不像我 / 太油 / 太重 feedback: shown

## Notes

- Earlier invalid attempts were not counted because the emulator accessibility service was not connected after a force-stop and one tap used a wrong menu coordinate.
- The two rounds above were run after accessibility was bound and overlay permission was confirmed.
- This smoke only validates the Express Self UI loop on MockChat. It does not claim real device PASS.
