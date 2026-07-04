# Codex To GPT

- taskName: cloud_timeout_escalation_and_github_delivery_sync
- versionName: 4.1.44
- versionCode: 463
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: LATEST_PHONE_4_1_43_ROUTE_PANEL_BUT_CLOUD_TIMEOUT
- gptShouldReview: true

## What Happened

- Latest phone feedback was from v4.1.43 / 462.
- Accessibility was connected and current chat capture succeeded.
- Last speaker was OTHER and 5 routes were shown.
- Cloud request was actually sent to gpt-5.4 but timed out after 39063ms.
- The app fell back to local routes.

## What Was Fixed

- gpt-5.4 primary cloud call now has a short 10s max window for non-visual calls.
- TIMEOUT escalates to gpt-5.5 before local fallback.
- NETWORK with likelyCause=TIMEOUT also escalates to gpt-5.5.
- gpt-5.5 escalation timeout is capped at 32s.
- v4.1.43 accessibility service reconnect guard remains in place.

## APK Delivery

- LAN latest.json points to 4.1.44 / 463.
- LAN APK: http://192.168.31.243:8787/huiyi-v4.1.44-debug.apk
- apkDeliveredOutOfBand: true
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true

## Review Entry

Please start with:

1. outputs/gpt_review_inbox/README_FOR_GPT.md
2. outputs/codex_to_gpt/result-manifest.json
3. outputs/update_server/latest.json
4. outputs/gpt_review_inbox/phone/latest/README_FOR_GPT.md
5. outputs/gpt_review_inbox/phone/latest/latest-session/next-sentence-flight-record.json

## Validation

- :app:testDebugUnitTest PASS
- :app:assembleDebug PASS
- PreconfiguredCloudRealUseMvpTest PASS
- CloudAnalysisMvpSafetyGateTest PASS
