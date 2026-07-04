# v4.1.31 Cloud Priority Latency Report

- taskName: cloud_priority_latency_tuning
- versionName: 4.1.31
- versionCode: 450
- generatedAt: 2026-07-04T10:05:43.3690867+08:00
- currentOverallResult: CLOUD_PRIORITY_EMULATOR_PASS_PHONE_REQUIRED
- realDeviceSmokeResult: NOT_TESTED
- userNeedsPhoneThisRound: true

## Why v4.1.30 showed cloud unavailable

v4.1.30 capped cloud waiting at 5.5 seconds to prevent no-reaction. That was too short for GPT 5.5 relay calls. The app fell back to local before high-quality cloud answers could return.

## New Runtime Policy

- LAST_ME: still local WAIT immediately, cloudAttempted=false.
- LAST_OTHER: cloud priority, wait up to 90 seconds.
- Local fallback remains available only if cloud fails or times out.
- Immediate click ACK remains enabled, so the user still sees feedback quickly.

## Relay Latency Comparison

- GPT 5.5 PC relay smoke: PASS, latencyMs=18996, routeCount=5, contractValidation=PASS.
- GPT 5.4 PC relay smoke: PASS, latencyMs=14681, routeCount=5, contractValidation=PASS.
- Initial observation: 5.4 was faster in this run. Quality parity is not proven by latency or schema pass alone.

## Android Emulator Evidence

- MockChat LAST_OTHER with GPT 5.5: PASS
- clickAckLatencyMs: 4
- cloudSuccess: true
- decisionSource: CLOUD
- cloudContractValidationResult: PASS
- routeCount: 5
- approximate Android latency: 56181ms
- logcat: outputs/gpt_review_inbox/emulator_cloud_smoke/v4131_last_other_cloud_priority_90s_logcat.txt

- MockChat LAST_ME: PASS
- clickAckLatencyMs: 6
- cloudAttempted: false
- decisionSource: LOCAL_WAIT
- routeCount: 0
- logcat: outputs/gpt_review_inbox/emulator_cloud_smoke/v4131_last_me_cloud_priority_logcat.txt

## APK Delivery

- apkPath: outputs/huiyi-v4.1.31-debug.apk
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.31-debug.apk
- sha256: 82EE100187231B7508CA8BD4AF96AB52FFBEE0FD1F91C8DF2BBEE5B671FC8514
- apkCommittedToPublicGithub: false

## Phone Validation

User should update to v4.1.31 and test one LAST_OTHER real chat. Expect immediate ACK, then wait for cloud. A cloud answer may take around 20-60 seconds based on current relay measurements.
