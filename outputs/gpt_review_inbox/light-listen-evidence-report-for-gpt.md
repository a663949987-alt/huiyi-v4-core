# Huiyi v4.1.42 Light Listen Evidence Report

## Basic Info

- project: Huiyi v4 Core
- taskName: unified_evidence_light_listen_timeline
- versionName: 4.1.42
- versionCode: 461
- generatedAt: 2026-07-04T12:00:00+08:00
- overallResult: LOCAL_BUILD_PASS_PHONE_EXPERIENCE_REQUIRED

## Goal

Build a unified evidence pipeline so cloud analysis can use:

- current screenshot as current-screen visual truth
- recent event-triggered visual checkpoints as previous visible context
- light-listen parsed text as auxiliary backfill
- persisted light-listen timeline as future chat profile material

## Evidence Authority

- CURRENT_SCREENSHOT
  - authority: HIGHEST_CURRENT_SCREEN
  - purpose: decide current visible last speaker
  - can be sent as high-detail image

- RECENT_VISUAL_CHECKPOINT
  - authority: HIGH_CONTEXT_ONLY
  - purpose: recover previous visible context
  - cannot override current screenshot
  - max retained in memory: 2

- ACCESSIBILITY_LIGHT_LISTEN
  - authority: AUXILIARY_TEXT_CONTEXT
  - mayContainParserError: true
  - cannot override current screenshot
  - cannot override current last speaker

## Implementation Summary

- Added `LightListenMemory` rolling cache.
- Hooked light-listen ingestion into `HuiyiAccessibilityService`.
- Added event-triggered visual checkpoints after stable accessibility events.
- Added `recentVisualEvidence` to cloud input.
- Added `evidencePackage` to cloud payload.
- Added `light_listen_messages` Room table.
- Added Room migration `MIGRATION_1_2`.
- Added local storage format `huiyi-history-message-v1`.

## Persistence

- table: light_listen_messages
- ordering: contactKey, observedAt, localSequence
- retention: 14 days
- original screenshots: not stored in DB
- cloudHistoryFormatJson: persisted per message

## Tests

- LightListenMemoryTest: PASS
- LightListenPersistenceTest: PASS
- PreconfiguredCloudRealUseMvpTest: PASS
- full `:app:testDebugUnitTest`: PASS
- `:app:assembleDebug`: PASS

## Delivery

- LAN latest: http://192.168.31.243:8787/latest.json
- LAN APK: http://192.168.31.243:8787/huiyi-v4.1.42-debug.apk
- apkSha256: 395277887DA2CF910670D789A7B313743C12E5E8FF72EB421711F97C287527E9
- apkCommittedToPublicGithub: false

## GPT Review Questions

1. Is the evidence priority model correct?
2. Should light-listen text be persisted exactly as parsed, or should cloud normalize it before profile storage?
3. Should visual checkpoints be retained only in memory, or should user-approved private storage be added later?
4. What is the best schema for future chat profile memory: preferences, taboos, rhythm, relationship status, and successful reply patterns?
