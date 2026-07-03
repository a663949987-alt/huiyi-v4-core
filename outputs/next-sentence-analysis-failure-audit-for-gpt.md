# Next Sentence Analysis Failure Audit

## Basic
- versionName: 4.1.8b
- versionCode: 422
- taskName: next_sentence_analysis_failure_diagnosis
- real_device_next_sentence_diagnostics: NOT_TESTED
- currentOverallResult: NOT_TESTED

## 1. “这次分析失败”从哪里显示出来
- 旧路径：`HuiyiRuntime.runNextSentence()` 捕获 pipeline 失败后写入 `lastError=friendlyPipelineError(error)`。
- 旧路径：`FloatingResultPanelController.showError()` 固定显示 “这次分析失败，但悬浮球仍在。”。
- 本轮修复：新增 `NextSentenceErrorCode`、`NextSentenceStage`、`NextSentenceSessionTrace`；非 `UNKNOWN_EXCEPTION` 使用具体用户文案。
- 本轮修复：`FloatingResultPanelController.show()` 现在 `lastError` 优先于旧 `latestPipelineResult`，避免旧成功结果盖住新失败。

## 2. 哪些异常被吞掉了
- 旧 capture 失败：`rootInActiveWindow 为空` 会变成普通 `IllegalStateException`，最终只显示泛化失败。
- 旧 parser 空：`当前屏幕未识别到聊天消息。` 没有 errorCode。
- 本轮修复：root null => `ROOT_UNAVAILABLE`；own overlay => `ROOT_IS_OWN_OVERLAY`；System UI => `ROOT_IS_SYSTEM_UI`；parser empty => `CHAT_MESSAGE_PARSE_EMPTY`。

## 3. 哪些地方没有写 errorCode
- 旧 `CurrentScreenCaptureUseCase.capture()` 没有 errorCode。
- 旧 `HuiyiRuntime.runNextSentence()` 没有 failedStage。
- 本轮修复：失败统一转为 `NextSentenceException(code, failedStage, trace)`。

## 4. 哪些地方失败后只恢复 UI、没有记录失败原因
- 旧浮层失败只记录 `OverlayStateStore.recordPipelineException(error)`。
- 本轮修复：运行时写出 `latest-next-sentence-failure.md/json`，并复制到手机 `Downloads/Huiyi/review/`。

## 5. 点击“下一句”是否可能导致 active root 变成会意自己的 overlay
- 是。任务书判断成立。
- 本轮修复：`HuiyiAccessibilityService` 维护 `LastStableForeignWindowSnapshot`。
- 本轮修复：`CurrentScreenCaptureUseCase` 在 current root 为会意自身/System UI/root null 且 5 秒内有稳定聊天快照时，使用 `LAST_STABLE_CHAT_SNAPSHOT`。

## 6. 单次 NextSentenceSession 失败是否会 stop service 或 remove 掉唯一悬浮球
- 代码审计未发现 `runNextSentence()` 调用 `stopSelf` / `stopService` / `disableSelf`。
- `FloatingBubbleService.onDestroy()` 仍会 hide bubble，但下一句失败路径不主动 destroy service。
- 本轮失败处理会保留 `OverlayStateStore.bubbleVisible`，并在 failure report 中记录 `bubbleVisibleAfterFailure`。

## 7. 当前权限判断是否还会把 root null 误判为无障碍没开
- 本轮保留 v4.1.8a 的三段判断：system disabled / service not connected / root unavailable。
- `ROOT_UNAVAILABLE` 文案为“当前窗口暂时不可读取”，不再说“无障碍未开启”。

## Current known limitation
- 本机未连接物理 Android，真实聊起 App 点击后 errorCode 尚未实测。
