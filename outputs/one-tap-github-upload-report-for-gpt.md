# One Tap GitHub Upload Report

## User Visible Result
- shown message: GitHub auto upload is not configured. The phone keeps the local zip and opens the system share fallback.
- upload success: false
- github url: none
- fallback zip path: generated on phone after the user taps `This is wrong, send to GPT`

## Privacy
- safeForPublicGitHub: true
- containsRawPrivateChat: false
- containsRawScreenshot: false

## Upload
- stage: FALLBACK_LOCAL_ONLY
- branch: none
- commit: none
- path: none
- errorCode: GITHUB_UPLOAD_DISABLED
- githubAutoUpload: NOT_AVAILABLE_GATEWAY_NOT_CONFIGURED
- localZipFallback: PASS
- versionName: 4.1.14
- versionCode: 432
- taskName: one_tap_feedback_auto_push_to_github

## Verification
- unit tests: PASS
- debug build: PASS
- LAN update: PASS
- LAN update version: 4.1.14 (432)
- token not embedded in APK: PASS
- Gateway deployed: NOT_CONFIGURED
