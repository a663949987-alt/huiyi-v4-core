# Changed Files For GPT

## Files changed this round
- path: app/build.gradle.kts
  - reason: Bump app version to 4.1.23 / 441 so LAN update and phone/latest cannot keep serving v4.1.20.
  - risk: Low.
- path: app/src/main/java/com/huiyi/v4/runtime/PhoneGptReviewBundleExporter.kt
  - reason: Missing safe natural LAST_ME is now reported as NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO instead of a fake PASS/FAIL.
  - risk: Low; phone review export wording and summary only.
- path: app/src/test/java/com/huiyi/v4/PhoneGptReviewBundleExporterTest.kt
  - reason: Added coverage for the explicit no-safe-LAST_ME result.
  - risk: Low.
- path: scripts/review_upload_gateway.py
  - reason: Reject stale phone bundles, reject one-tap feedback that reruns/recaptures instead of binding the original NextSentenceSession, and keep cloud TODO disabled.
  - risk: Medium; upload gate behavior changed intentionally.
- path: scripts/generate_gpt_review_inbox.py
  - reason: Preserve phone/latest unless a new phone bundle is explicitly provided.
  - risk: Low; packaging only.
- path: outputs/gpt_review_inbox/phone/latest/*
  - reason: Replaced old v4.1.20 polluted latest evidence with current v4.1.23 placeholder state.
  - risk: Low; review evidence only.
- path: outputs/phone-real-device-closure-report-for-gpt.md
  - reason: Current round acceptance report for phone latest closure, 3-smoke scope, and cloud TODO status.
  - risk: Low; report output only.

## Important logic changes
1. `phone/latest` is current-version only: 4.1.23 / 441.
2. One-tap feedback must reference the original NextSentenceSession and must not rerun parser or recapture the Huiyi panel.
3. If the user does not have a safe natural LAST_ME scene, report `NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO`.
4. Real-device validation is reduced to exactly 3 smoke checks: Liaoqi LAST_ME, Liaoqi LAST_OTHER, Unsupported App.
5. Cloud tactical analysis remains TODO / disabled; any phone bundle claiming cloud execution is rejected.

## Tests added / updated
- `PhoneGptReviewBundleExporterTest`: PASS
- `OneTapFeedbackExportTest`: PASS
- upload gateway stale/session/cloud guard: PASS
- `testDebugUnitTest`: PASS
- `assembleDebug`: PASS
- LAN latest.json published as 4.1.23 / 441: PASS

## Known risk areas
- This local run cannot execute a physical-phone smoke test by itself.
- User should install the current APK through LAN update and run only the 3 phone smoke checks when safe.
- If there is no safe natural LAST_ME scene, keep `lastMeRealDeviceResult=NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO`.
