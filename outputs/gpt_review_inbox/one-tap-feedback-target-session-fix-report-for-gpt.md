# Huiyi v4.1.21 One Tap Feedback Target Session Fix

- project: Huiyi v4 Core
- taskName: one_tap_feedback_export_target_session_fix_before_cloud_contract
- versionName: 4.1.21
- versionCode: 439
- generatedAt: 2026-07-03 10:11:00 +0800
- overall_result: NOT_TESTED
- currentOverallResult: NOT_TESTED
- sample_source: not_tested
- real_device_smoke_result: NOT_TESTED

## 本轮结论

- feedback bound to original session: PASS
- feedback triggers new analysis: NO
- feedback recaptures current root: NO
- preAnalysis contamination detection: PASS
- report consistency checks: PASS
- LAST ME wait rule: PASS
- cloud contract implemented: TODO_ONLY
- cloud analysis attempted: false
- currentOverallResult: NOT_TESTED

## 关键实现

- 一键反馈优先绑定当前面板 `panelSessionId` 对应的原始 `NextSentenceFlightRecord`。
- 当前面板没有 session 时，才回退到最后一次完成的 NextSentenceSession。
- 如果两者都没有，反馈包不生成假报告，返回 `NO_TARGET_SESSION_FOR_FEEDBACK`。
- 反馈导出只写 `OneTapFeedback` trace，不重新运行 parser，不重新生成 decision，不重新判断 last speaker。
- `preAnalysisSnapshot` 在点击“下一句”时冻结，后续面板出现后不可覆盖。
- 检测到会意面板标题污染时，报告输出 `FAIL_CONTAMINATED_EXPORT`，不能把它当 LAST OTHER PASS。

## 新增字段

- feedbackClickedAt
- feedbackTargetSessionId
- feedbackTargetSessionTerminalState
- feedbackTargetSessionFound
- feedbackExportSource
- feedbackTriggeredNewAnalysis
- feedbackReCapturedCurrentRoot
- feedbackUsedOverlayStateAsPreAnalysis
- preAnalysisSnapshotFrozenAt
- preAnalysisSnapshotSource
- preAnalysisSnapshotMutableAfterPanel
- postPanelSnapshotCapturedAt
- postPanelSnapshotUsedForDecision
- preAnalysisSnapshotTrusted
- preAnalysisLooksLikeHuiyiPanel
- preAnalysisTextClaimsLastMeWait
- recordClaimsLastOtherRoutePanel
- windowTitleAndDecisionContradiction
- reportConsistencyResult
- cloudContractImplemented
- cloudConfigured
- cloudAnalysisAttempted

## 云端状态

- cloudContractImplemented: false
- cloudEnabled at runtime: false
- cloudAttempted expected: false
- cloudSkippedReason expected: CLOUD_NOT_CONFIGURED or LAST_SPEAKER_ME_WAIT
- docsOnlyContract: docs/HuiyiTacticalContract-v1.md

## 本地验证

- targeted tests: PASS
- unit tests: PASS (`:app:testDebugUnitTest`)
- assembleDebug: PASS
- LAN update publish: PASS
- LAN latest.json: PASS (`http://192.168.31.243:8787/latest.json`)

## APK

- apkPath: outputs/huiyi-v4.1.21-debug.apk
- updateServerApkPath: outputs/update_server/huiyi-v4.1.21-debug.apk
- sha256: 62BF264E5E0B38C022655E5E4E65DBB3B794159DDCBFA6F72BE29612E3295D0D

## 仍需真机验证

User should install v4.1.21, open Liaoqi, send `嗯嗯`, tap 下一句, and if the result looks wrong tap `这次不对，发给 GPT`.

The uploaded `phone/latest` must show:

- feedbackTargetSessionId is the same as the original panel session.
- feedbackExportSource is `BOUND_PANEL_SESSION` or `LAST_COMPLETED_NEXT_SENTENCE_SESSION`.
- feedbackTriggeredNewAnalysis is false.
- feedbackReCapturedCurrentRoot is false.
- If preAnalysis is contaminated by Huiyi panel text, `reportConsistencyResult=FAIL_CONTAMINATED_EXPORT`.

