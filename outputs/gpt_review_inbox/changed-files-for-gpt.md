# Changed Files For GPT

## Files changed this round

- path: app/src/main/java/com/huiyi/v4/runtime/OneTapFeedbackExporter.kt
  - reason: Bind one-tap feedback to an existing target session, add feedback trace fields, freeze pre-analysis snapshot metadata, and add contaminated export consistency checks.
  - risk: Medium; this is the main one-tap feedback export contract.
- path: app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - reason: Select target record by panel session before export, fail with `NO_TARGET_SESSION_FOR_FEEDBACK` when no target exists, and keep runtime cloud disabled until the contract is implemented.
  - risk: Medium; runtime feedback flow changed.
- path: app/src/test/java/com/huiyi/v4/OneTapFeedbackExportTest.kt
  - reason: Add regression tests for bound panel session, no new analysis, no recapture, contaminated export detection, LAST ME WAIT, and cloud contract TODO status.
  - risk: Low; tests only.
- path: docs/HuiyiTacticalContract-v1.md
  - reason: Add cloud HUD contract skeleton as TODO-only documentation.
  - risk: Low; not enabled.
- path: app/build.gradle.kts
  - reason: Bump to 4.1.21 / 439 for LAN update detection.
  - risk: Low.

## Important logic changes

1. Tapping `这次不对，发给 GPT` exports the original `NextSentenceFlightRecord` bound to the result panel session.
2. One-tap feedback does not run parser again, does not create a new next-sentence session, and does not recapture the current root.
3. If pre-analysis title looks like Huiyi's own panel and the record claims OTHER + ROUTE_PANEL, `reportConsistencyResult=FAIL_CONTAMINATED_EXPORT`.
4. Cloud HUD contract remains TODO only; runtime cloud analysis is disabled.

## Tests

- targeted one-tap feedback tests: PASS
- `:app:testDebugUnitTest`: PASS
- `:app:assembleDebug`: PASS

## Remaining validation

- Real phone one-tap feedback flow is still NOT_TESTED for v4.1.21.
- User should update to v4.1.21, run one Liaoqi LAST ME flow, and tap `这次不对，发给 GPT` only if the result looks wrong.
