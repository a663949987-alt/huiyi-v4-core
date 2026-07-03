# Accessibility Click Diagnostic Report

## Basic

- project: Huiyi v4 Core
- versionName: 4.1.8a
- versionCode: 421
- generatedAt: 2026-07-03 11:55:00 +08:00
- overall_result: PARTIAL
- realDeviceClickTest: NOT_TESTED

## Runtime State Model

- systemAccessibilityEnabled: NOT_TESTED
- serviceConnected: NOT_TESTED
- rootAvailable: NOT_TESTED
- currentPackage: NOT_TESTED
- currentWindowTitle: NOT_TESTED
- activeServiceInstanceId: NOT_TESTED
- overlayVisible: NOT_TESTED
- floatingServiceRunning: NOT_TESTED
- system_enabled_but_service_not_connected: NOT_TESTED

## Click Samples

| sample | systemAccessibilityEnabled | serviceConnected | rootAvailable | currentPackage | currentWindowTitle | overlayVisible | floatingServiceRunning | activeServiceInstanceId | lastError |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| beforeClick | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |
| afterClick_100ms | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |
| afterClick_500ms | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |
| afterClick_1000ms | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |
| afterClick_3000ms | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED | NOT_TESTED |

## Failure Guards

- wrongNoPermissionTextShown: false
- pipelineException: NOT_TESTED
- windowManagerException: NOT_TESTED
- floatingBubbleDisappearReason: NOT_TESTED
- errorCardText: 这次分析失败，但悬浮球仍在。

## Local Validation

- AccessibilityEnabledButServiceDisconnectedTextTest: PASS
- RootUnavailableTextTest: PASS
- NextClickExceptionKeepsBubbleVisibleTest: PASS
- WindowManagerExceptionLoggedTest: PASS
- OverlayHiddenOnlyByUserTest: PASS
- unit tests: PASS
- debug build: PASS
