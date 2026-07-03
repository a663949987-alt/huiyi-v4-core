# Changed Files For GPT

## Files changed this round
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceScenario.kt
  - reason: Split real-device functional smoke, scenario assertion, and current overall result.
  - risk: Medium; acceptance verdict logic changed.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/EvidencePackReportGenerator.kt
  - reason: Add expected-vs-actual fields, snapshot phase separation, screenshot diagnostic status, and panel contamination fields.
  - risk: Low; report output only.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceReviewBundleGenerator.kt
  - reason: Review bundle now reports controlled scenario mismatch instead of product failure.
  - risk: Low; export/report output only.
- path: app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - reason: Default real-device scenario now derives from the current screen instead of legacy last_me.
  - risk: Medium; developer export default changed.
- path: app/build.gradle.kts
  - reason: Bump app version for LAN update detection.
  - risk: Low.
- path: scripts/generate_review_bundle.py and scripts/generate_gpt_review_inbox.py
  - reason: Include v4.1.10 result layers and fixed GPT inbox files.
  - risk: Low; packaging only.

## Important logic changes
1. `scenarioName=last_me` no longer overrides actual current-screen evidence.
2. `LastSpeakerDecision=OTHER` with `NORMAL_REPLY + 5 routes` is a functional PASS when the screen evidence supports OTHER.
3. Scenario mismatch is now reported as `CONTROLLED_PASS_WITH_SCENARIO_MISMATCH`.
4. Post-panel overlay title is flagged and cannot define scenario expectations.

## Tests added / updated
- `testDebugUnitTest`: PASS
- `assembleDebug`: PASS

## Known risk areas
- This local run cannot prove the next physical-phone sample; user still needs one real-device export after installing v4.1.10.
- If phone update cache still serves an older latest.json, restart the LAN update server or refresh the served folder.
