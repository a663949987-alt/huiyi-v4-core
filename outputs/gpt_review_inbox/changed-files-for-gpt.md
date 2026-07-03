# Changed Files For GPT

## Files changed this round
- app/src/main/java/com/huiyi/v4/runtime/OneTapFeedbackExporter.kt
  - Adds sessionImmutableAfterTerminalState to exported records and reports session freeze/binding fields.
- app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - Keeps one-tap feedback bound to the current panel session id.
- app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceScenario.kt
  - Treats Huiyi overlay title markers as contaminated preAnalysis.
- app/src/test/java/com/huiyi/v4/LocalStateChainClosureTest.kt
  - Adds local state chain closure tests.
- app/build.gradle.kts
  - Publishes current local closure APK as versionName 4.1.23 / versionCode 443.
- outputs/gpt_review_inbox/session-binding-report-for-gpt.md
  - Current round GPT evidence.
- outputs/gpt_review_inbox/session-binding-report.json
  - Machine-readable current round evidence.

## Verification
- LocalStateChainClosureTest: PASS
- OneTapFeedbackExportTest: PASS
- LastMeWaitPriorityAndStatusMetadataFixTest: PASS
- CloudAnalysisMvpSafetyGateTest: PASS
- SimulationFirstValidationTest: PASS
- :mockchat:assembleDebug: PASS
- :app:assembleDebug: PASS

## Honest status
- emulatorUiSmokeResult: NOT_RUN
- realDeviceSmokeResult: NOT_TESTED
- userNeedsPhoneThisRound: false
