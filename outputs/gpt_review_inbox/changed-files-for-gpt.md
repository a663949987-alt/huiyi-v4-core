# Changed Files For GPT

## Files changed this round

- path: scripts/run-emulator-mockchat-smoke.ps1
  - reason: Adds an adb-based emulator MockChat UI smoke runner that produces NOT_RUN when no emulator is available.
  - risk: Low; script/report only.
- path: app/src/test/java/com/huiyi/v4/SimulationFirstValidationTest.kt
  - reason: Separates fixtureReplayResult, mockChatBuildResult, emulatorUiSmokeResult, and realDeviceSmokeResult in the simulation-first report.
  - risk: Low; test/report output only.
- path: docs/SimulationFirstAcceptance.md
  - reason: Documents that fixture replay PASS must not be treated as emulator UI PASS.
  - risk: Low; documentation only.
- path: outputs/emulator-mockchat-smoke-report-for-gpt.md
  - reason: Current emulator smoke evidence; result is NOT_RUN because no emulator is available.
  - risk: Low; report output only.
- path: outputs/emulator-mockchat-smoke-report.json
  - reason: Machine-readable emulator smoke evidence.
  - risk: Low; report output only.
- path: outputs/simulation-first-validation-report-for-gpt.md
  - reason: Regenerated with explicit result layers.
  - risk: Low; report output only.

## Important logic changes

1. fixtureReplayResult only means offline fixture / JVM replay.
2. mockChatBuildResult only means MockChat APK build.
3. emulatorUiSmokeResult is PASS only after real emulator UI/accessibility evidence.
4. realDeviceSmokeResult is PASS only after physical phone evidence.
5. Current emulatorUiSmokeResult is NOT_RUN, reason NO_EMULATOR_AVAILABLE.

## Verification

- SimulationFirstValidationTest: PASS
- MockChat assembleDebug: PASS
- Emulator MockChat smoke script: NOT_RUN / NO_EMULATOR_AVAILABLE

## Residual risk

- No emulator UI smoke evidence exists in this Codex run because adb reported no emulator device.
