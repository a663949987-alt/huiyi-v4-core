# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: solo_character_arc_validation_loop
- versionName: 4.1.51
- versionCode: 470
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- generatedAt: 2026-07-04T18:15:00+08:00
- userNeedsPhoneThisRound: true
- realDeviceSmokeResult: NOT_TESTED_NOT_REQUIRED

## What Changed
- Added CharacterArcAutoJudge for 60 synthetic/anonymous scenarios and 180 candidates.
- Added CharacterArcActiveSampler so the user only sees high-value samples.
- Added SoloReviewLab blind A/B/C flow in "我的底色".
- Added lightweight feedback buttons on reply routes: 像我 / 不像我 / 太油 / 太重 / 太空 / 可发.
- Added CharacterArcPreferenceStore and CharacterArcPreferenceProfile.

## Safety
- longTermRawPrivateChatSaved: false
- rawPrivateChatUploadedToGithub: false
- autoSend: false
- routeFeedbackStoresRedactedTextOnly: true
- apkCommittedToPublicGithub: false

## Validation
- unitTests: PASS
- assembleDebug: PASS
- autoJudgedScenarioCount: 60
- candidateCount: 180
- userReviewNeededCount: 20
- firstRoundReviewLimit: 20
- dailyReviewLimit: 5

## Delivery
- LAN latest.json: http://192.168.31.243:8787/latest.json
- LAN APK: http://192.168.31.243:8787/huiyi-v4.1.51-debug.apk
- local APK path: outputs/update_server/huiyi-v4.1.51-debug.apk

## GPT Should Inspect
1. outputs/gpt_review_inbox/character-arc-solo-validation-report-for-gpt.md
2. outputs/gpt_review_inbox/character-arc-solo-validation-report.json
3. outputs/codex_to_gpt/result-manifest.json
