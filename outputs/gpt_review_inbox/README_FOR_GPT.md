# Huiyi v4 GPT Review Inbox

## Current Task

- taskName: unified_evidence_light_listen_timeline
- versionName: 4.1.42
- versionCode: 461
- currentOverallResult: LOCAL_BUILD_PASS_PHONE_EXPERIENCE_REQUIRED
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: NOT_TESTED_THIS_ROUND
- gptShouldReview: true

## What Changed

- Added unified evidence package for cloud analysis.
- Current screenshot is marked as highest authority for current visible last speaker.
- Recent visual checkpoints are event-triggered screenshots used only for previous context.
- Light-listen backfill is marked as auxiliary text context and cannot override current screenshot.
- Light-listen messages are persisted locally in a timeline table for future chat profile memory.
- Room database migrated from version 1 to 2 without destructive migration.

## Important Rules

- CURRENT_SCREENSHOT decides the current visible last speaker.
- RECENT_VISUAL_CHECKPOINT images are context only.
- ACCESSIBILITY_LIGHT_LISTEN text may contain parser errors and is context only.
- Light-listen content is persisted locally by contact key and time order.
- Raw visual checkpoint images are kept only in short-term memory, not stored in the database.
- Private relay API key is not included in reports or GitHub.

## Main Files For GPT

1. outputs/gpt_review_inbox/light-listen-evidence-report-for-gpt.md
2. outputs/gpt_review_inbox/light-listen-evidence-report.json
3. outputs/codex_to_gpt/result-manifest.json
4. outputs/codex_to_gpt/changed-files-for-gpt.md

## Build And Delivery

- local APK: outputs/huiyi-v4.1.42-debug.apk
- LAN latest: http://192.168.31.243:8787/latest.json
- LAN APK: http://192.168.31.243:8787/huiyi-v4.1.42-debug.apk
- apkSha256: 395277887DA2CF910670D789A7B313743C12E5E8FF72EB421711F97C287527E9
- apkCommittedToPublicGithub: false

## Validation

- unitTests: PASS
- assembleDebug: PASS
- relayTextSmoke: PASS
- phoneSmokeThisRound: NOT_TESTED

## Next Discussion

Please review whether the evidence authority model is correct before adding deeper chat profile generation:

- current screenshot as visual truth
- recent screenshots as visual context
- light-listen text as auxiliary context
- persisted history format as future profile input
