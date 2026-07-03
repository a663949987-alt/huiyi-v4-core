# Changed Files For GPT

## Files changed this round

- path: app/src/main/java/com/huiyi/v4/domain/simulation/AccessibilityNodeFixture.kt
  - reason: Adds fixture replay from node dump / real-device report JSON into the real parser path.
  - risk: Low; new validation layer.
- path: app/src/main/java/com/huiyi/v4/domain/simulation/SimulationFixtures.kt
  - reason: Adds required fixture categories for Liaoqi, metadata trap, read receipts, overlay contamination, and unsupported app.
  - risk: Low; deterministic test fixtures.
- path: app/src/main/java/com/huiyi/v4/domain/simulation/SyntheticRelationshipCorpus.kt
  - reason: Adds 200-sample synthetic relationship corpus generator with expected tactical labels.
  - risk: Low; local validation data only.
- path: app/src/main/java/com/huiyi/v4/domain/cloud/CloudAnalysis.kt
  - reason: Enforces HuiyiTacticalContract v1 fields and rejects invalid cloud output to local fallback.
  - risk: Medium; cloud schema is stricter.
- path: app/src/main/java/com/huiyi/v4/domain/capture/MetadataMessageFilter.kt
  - reason: Filters Huiyi overlay text as metadata so it cannot pollute LastSpeakerDecision.
  - risk: Low.
- path: mockchat/src/main/java/com/huiyi/mockchat/MainActivity.kt
  - reason: Adds MockChat scenarios for read/unread/checkmark, send failed, and Huiyi overlay contamination.
  - risk: Low; test app only.
- path: app/src/test/java/com/huiyi/v4/SimulationFirstValidationTest.kt
  - reason: Verifies fixture replay, report JSON fixture generation, synthetic corpus, and cloud contract replay.
  - risk: Low.
- path: docs/SimulationFirstAcceptance.md
  - reason: Documents simulation-first acceptance policy and reduced real-device smoke scope.
  - risk: Low.
- path: app/build.gradle.kts
  - reason: Bumps app version to 4.1.22 / 440 for LAN update detection.
  - risk: Low.

## Test results

- `:app:testDebugUnitTest`: PASS
- `:app:assembleDebug :mockchat:assembleDebug`: PASS
- LAN update publish: PASS, latest.json is 4.1.22 / 440

## Current acceptance posture

- simulationFirstValidation: PASS
- fixtureReplay: PASS
- syntheticCorpus: PASS
- cloudContractReplay: PASS
- realDeviceSmoke: NOT_TESTED, intentionally reduced to 3 smoke checks
