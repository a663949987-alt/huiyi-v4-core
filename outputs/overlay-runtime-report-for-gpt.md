# Overlay Runtime Report

## Basic

- project: Huiyi v4 Core
- versionName: 4.1.8a
- versionCode: 421
- generatedAt: 2026-07-03 11:55:00 +08:00
- overall_result: PARTIAL
- realDeviceOverlayTest: NOT_TESTED

## OverlayStateStore

- bubbleVisible: NOT_TESTED
- resultPanelVisible: NOT_TESTED
- errorPanelVisible: NOT_TESTED
- lastPanelType: NOT_TESTED
- lastBubbleClickAt: NOT_TESTED
- lastPanelShownAt: NOT_TESTED
- lastPanelDismissedAt: NOT_TESTED
- lastOverlayError: NOT_TESTED
- lastWindowManagerException: NOT_TESTED
- addViewSuccess: NOT_TESTED
- removeViewReason: NOT_TESTED
- floatingServiceRunning: NOT_TESTED
- lastPipelineException: NOT_TESTED
- serviceStoppedByUser: NOT_TESTED

## Guards Implemented

- FloatingBubbleController.addView: try/catch + WindowManager exception recorded
- FloatingBubbleController.removeView: try/catch + remove reason recorded
- FloatingResultPanelController.addView: try/catch + error recorded
- FloatingResultPanelController.removeView: try/catch + remove reason recorded
- Pipeline failure: shows overlay error panel and keeps bubble alive
- Bubble hide: only user-triggered hide marks user_hide

## Error Panel

- title: 这次分析失败，但悬浮球仍在。
- buttons: 重试 / 导出诊断 / 打开无障碍设置 / 隐藏悬浮球

## Local Validation

- unit tests: PASS
- debug build: PASS
