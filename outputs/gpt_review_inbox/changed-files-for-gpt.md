# Changed Files For GPT

## Files changed this round

- path: app/src/main/java/com/huiyi/v4/domain/pipeline/CurrentScreenPipelineUseCase.kt
  - reason: Hard-gate LAST ME to local WAIT before context completeness, uncertainty guards, routes, or cloud analysis.
  - risk: Medium; this is the main decision priority path.
- path: app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - reason: Normalize rendered terminal states to `WAIT_PANEL`, `CONTEXT_REQUIRED_PANEL`, and `ROUTE_PANEL`.
  - risk: Low; report/runtime wording alignment.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceScenario.kt
  - reason: Add pre-analysis title trust/source fields and explicit Huiyi overlay title contamination markers.
  - risk: Low; validation/report metadata.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/EvidencePackReportGenerator.kt
  - reason: Export `preAnalysisSnapshotTrusted` and `preAnalysisWindowTitleSource` in real-device md/json.
  - risk: Low; report output only.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceReviewBundleGenerator.kt
  - reason: Include the new snapshot trust/source fields in review summaries.
  - risk: Low; report output only.
- path: app/src/test/java/com/huiyi/v4/CloudAnalysisMvpSafetyGateTest.kt
  - reason: Add regression coverage for visual LAST ME being overridden by context ordering.
  - risk: Low; tests only.
- path: app/build.gradle.kts
  - reason: Bump to 4.1.20 / 438 for LAN update detection.
  - risk: Low.

## Important logic changes

1. If `LastSpeakerDecision=ME`, product decision must be `WAIT`.
2. LAST ME returns zero routes, `apiCalled=false`, `modelCalled=false`, `cloudAttempted=false`.
3. LAST ME cloud trace reports `cloudSkippedReason=LAST_SPEAKER_ME_WAIT` and `decisionSource=LOCAL_WAIT`.
4. Huiyi overlay titles such as `下一句没有跑完`, `正在上传 GitHub`, and `当前信息不足` are marked as contaminated panel titles.

## Tests

- targeted LAST ME/cloud/report tests: PASS
- `:app:testDebugUnitTest`: PASS
- `:app:assembleDebug`: PASS

## Remaining validation

- Real phone LAST ME smoke is still NOT_TESTED for v4.1.20.
- User should install/update v4.1.20 through LAN update and run one LAST ME scenario in the real Liaoqi chat.
