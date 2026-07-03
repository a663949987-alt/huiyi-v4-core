# Huiyi v4 Simulation-First Acceptance

## Goal

Huiyi v4 should not depend on repeated private real-device validation for every parser or tactical change.

Default validation order:

1. Unit tests.
2. AccessibilityNodeFixture replay.
3. MockChatLab matrix.
4. Synthetic relationship corpus.
5. HuiyiTacticalContract v1 replay.
6. Real-device smoke tests only.

## Fixture Replay

Accessibility node fixtures are built from either raw node dumps or `real-device-current-screen-report.json`.
The fixture is fed back into the same parser path used by the app:

- `LiaoqiRealParser` for `com.bajiao.im.liaoqi`
- `GenericVisualBubbleParser` for generic layouts and MockChatLab

Each replay asserts:

- `lastSpeaker`
- `decisionType`
- `routeCount`
- `panelState`

Required fixture categories:

- `liaoqi_last_other_pass`
- `liaoqi_last_me_wait`
- `liaoqi_post_panel_contaminated`
- `liaoqi_read_receipt_status`
- `generic_time_metadata_trap`
- `unsupported_app_no_chat_rows`

## MockChatLab

MockChatLab remains the main emulator environment. It covers:

- LAST_ME
- LAST_OTHER
- read / unread / checkmark status
- send failed status
- long text
- voice and image
- font scale changes
- screen-width-sensitive bounds
- Huiyi overlay contamination

## Synthetic Corpus

The generator creates at least 200 labeled relationship chat samples across:

- blind date
- ambiguous flirting
- cold reply
- pressure
- life sharing
- read no reply
- user multi-send

Each sample includes:

- `expectedDecisionType`
- `coCreationPoint`
- `userLikelyMistake`
- `intensity`
- `risk`
- `fallback`

## Cloud Contract Replay

`HuiyiTacticalContract v1` output must include:

- `coCreationPoint`
- `userLikelyMistake`
- `intensityPolicy`
- `riskWarning`
- `fallbackMove`
- exactly `routes[5]`

Invalid cloud output is rejected with `SCHEMA_INVALID` and the app falls back locally.

## Real Device Smoke

Real-device testing is reduced to three smoke tests:

- Liaoqi LAST_ME: `ME -> WAIT`
- Liaoqi LAST_OTHER: `OTHER -> routes`
- Unsupported App: show unsupported prompt and export adapter bundle
