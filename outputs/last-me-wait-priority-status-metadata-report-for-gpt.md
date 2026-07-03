# Huiyi v4.1.17 LAST ME Wait Priority + Message Status Metadata Report

- project: huiyi-v4
- taskName: last_me_wait_priority_and_message_status_metadata_fix
- versionName: 4.1.17
- versionCode: 435
- generatedAt: 2026-07-03 16:38:29 +08:00
- overall_result: PASS_FOR_LOCAL_BUILD_AND_UNIT_TESTS
- real_device_result: NEEDS_USER_SMOKE_TEST
- apiCalled: false
- modelCalled: false

## Goal

- Fix LAST ME being routed to CONTEXT_REQUIRED after parser already identified actualLastSpeaker = ME.
- Ensure message status artifacts such as read receipts, delivery status, send status, and check icons are metadata, not effective chat messages.
- Keep phone one-tap feedback README useful for GPT review.

## Key Fixes

- CurrentScreenPipelineUseCase now prioritizes LAST ME before unknown/context guards.
- LAST ME result remains WAIT with routeCount = 0, apiCalled = false, modelCalled = false.
- RealDeviceScenarioValidator detects Huiyi overlay title contamination such as "下一句没有跑完", "正在上传 GitHub", "这次不对，发给 GPT", and "重试".
- MetadataMessageFilter classifies READ_RECEIPT, DELIVERY_STATUS, SEND_STATUS, and MESSAGE_STATUS_ICON.
- GenericVisualBubbleParser creates MessageStatusArtifact and attaches it to the latest ME message where possible.
- EvidencePackReportGenerator exports message status summary fields.
- OneTapFeedback README now includes latestSessionTerminalState, actualLastSpeaker, decisionTypeFamily, waitPanelShown, contextRequiredPanelShown, messageStatusArtifactCount, lastMeDeliveryStatus, lastMeReadStatus, and reportWindowTitleContaminatedByPanel.

## Required LAST ME Behavior

- actualLastSpeaker: ME
- decisionType: WAIT
- decisionTypeFamily: WAIT
- routeCount: 0
- apiCalled: false
- modelCalled: false
- terminalState: WAIT_PANEL
- waitPanelShown: true
- contextRequiredPanelShown: false

## Message Status Metadata

- messageStatusArtifactCount: exported in reports
- readReceiptCount: exported in reports
- deliveryStatusCount: exported in reports
- lastMeDeliveryStatus: exported in reports
- lastMeReadStatus: exported in reports
- statusArtifactsFilteredFromEffectiveMessages: exported in reports
- statusArtifactsAttachedToMessageCount: exported in reports

Status artifacts are not allowed to affect LastSpeakerDecision.

## Tests

- testDebugUnitTest: PASS
- added LastMeWaitPriorityAndStatusMetadataFixTest
- covered LAST ME priority over context/unknown guard
- covered pre-analysis title contamination detection
- covered read receipt and delivery status metadata classification
- covered status artifact attachment to previous ME message
- covered LAST ME with read receipt still WAIT
- covered status artifact not becoming effective message

## LAN Update

- publishedVersionName: 4.1.17
- publishedVersionCode: 435
- latestJsonLocal: http://127.0.0.1:8787/latest.json
- latestJsonLan: http://192.168.31.243:8787/latest.json
- apkPath: outputs/huiyi-v4.1.17-debug.apk
- updateServerApkPath: outputs/update_server/huiyi-v4.1.17-debug.apk
- sha256: 32E7158B17896B5DEA02A99E1D1BDCD98218859B81AF4F1FE73795AF055BE670

## User Smoke Test Needed

Only one real-device test is needed:

1. Open Liaoqi chat.
2. Send "嗯嗯".
3. Wait 1 second.
4. Tap "下一句".
5. Expected result: "你已经回过了，先等对方。"

Fail if the app shows "当前信息不足", "说话人或内容不确定", or generates 5 routes.

If it fails, tap "这次不对，发给 GPT". The phone one-tap feedback package should upload to GitHub and include the new summary fields.
