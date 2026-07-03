# Emulator MockChat Smoke Report

- taskName: emulator_mockchat_ui_smoke
- emulatorUiSmokeResult: NOT_RUN
- reason: NO_EMULATOR_AVAILABLE
- fixtureReplayResult: UNKNOWN
- mockChatBuildResult: PASS
- realDeviceSmokeResult: NOT_TESTED
- emulatorDetected: False
- emulatorSerial: none
- huiyiInstalled: False
- mockchatInstalled: False
- accessibilityEnabled: False
- overlayPermissionGranted: False
- lastMeResult: NOT_RUN
- lastOtherResult: NOT_RUN
- readReceiptResult: NOT_RUN
- overlayContaminationResult: NOT_RUN
- oneTapFeedbackTargetSessionResult: NOT_RUN
- screenshotsPath: C:\Users\fbjdf\Documents\Codex\2026-07-02\new-chat-3\outputs\emulator_mockchat_smoke
- logcatPath: none

## Acceptance Boundary

- fixtureReplayResult only means offline fixture / JVM parser replay passed.
- mockChatBuildResult only means the MockChat APK exists or builds.
- emulatorUiSmokeResult=PASS is allowed only after an emulator is detected, both APKs install, accessibility is enabled, overlay permission is granted, MockChat scenarios are opened, Next Sentence is triggered, and overlay results are observed.
- realDeviceSmokeResult=PASS is allowed only after a physical Android phone smoke test.

## Required Emulator UI Checks

1. MockChat LAST_ME -> WAIT_PANEL: NOT_RUN
2. MockChat LAST_OTHER -> ROUTE_PANEL / routes=5: NOT_RUN
3. read receipt / checkmark not effective: NOT_RUN
4. Huiyi overlay contamination does not pollute preAnalysis: NOT_RUN
5. one tap feedback binds original session: NOT_RUN

## Notes

No emulator UI smoke evidence was produced.
