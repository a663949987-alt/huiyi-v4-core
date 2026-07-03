# Changed Files For GPT

## Files changed this round

- path: app/build.gradle.kts
  - reason: Keep versionName 4.1.23 but raise versionCode to 442 so LAN update can detect the new package.
- path: app/src/main/java/com/huiyi/v4/runtime/OneTapFeedbackExporter.kt
  - reason: If panelSessionId exists, feedback must bind that original session or fail.
- path: app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - reason: Pass the panel-bound session id into one-tap feedback export.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceScenario.kt
  - reason: Treat Huiyi overlay title markers, including 先等对方, as contaminated preAnalysis.
- path: app/src/test/java/com/huiyi/v4/OneTapFeedbackExportTest.kt
  - reason: Adds tests for no stale fallback and wait-phrase contamination.
- path: app/src/test/java/com/huiyi/v4/LastMeWaitPriorityAndStatusMetadataFixTest.kt
  - reason: Adds real-device scenario validation for 先等对方 contamination.
- path: outputs/gpt_review_inbox/phone/latest/*
  - reason: Current phone latest placeholder now points to 4.1.23 / 442 and no longer exposes v4.1.20 as latest.
- path: outputs/update_server/latest.json
  - reason: LAN update now serves 4.1.23 / 442.

## Verification

- OneTapFeedbackExportTest: PASS
- LastMeWaitPriorityAndStatusMetadataFixTest: PASS
- CloudAnalysisMvpSafetyGateTest: PASS
- assembleDebug: PASS
- LAN latest.json: PASS

## Honest Status

- realDeviceSmokeResult: NOT_TESTED
- No physical phone LAST ME smoke was run in this Codex environment.
