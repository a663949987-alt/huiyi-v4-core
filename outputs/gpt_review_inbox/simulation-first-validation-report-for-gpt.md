# Simulation-First Validation Report

- taskName: simulation_first_acceptance_system
- overall_result: PASS
- simulationFirstResult: PASS
- fixtureReplayResult: PASS
- mockChatBuildResult: NOT_ASSERTED_BY_THIS_JVM_TEST
- emulatorUiSmokeResult: NOT_RUN
- emulatorUiSmokeReason: NO_EMULATOR_UI_SMOKE_EXECUTED_BY_JVM_TEST
- realDeviceSmokeResult: NOT_TESTED
- fixtureCount: 6
- syntheticCorpusCount: 200
- cloudContractReplay: PASS
- mockchatMatrix: fixture/JVM coverage only; not emulator UI smoke
- realDeviceSmokePolicy: 3 only

## Acceptance Boundary

- fixtureReplayResult only means offline fixture / JVM parser replay passed.
- mockChatBuildResult only means the MockChat APK builds; it does not prove UI/accessibility behavior.
- emulatorUiSmokeResult may be PASS only when an Android emulator is detected, Huiyi and MockChat are installed, accessibility is enabled, overlay permission is granted, Next Sentence is clicked, and the overlay result is observed.
- realDeviceSmokeResult may be PASS only when a physical phone smoke test is completed.
- This report must not use fixtureReplayResult to imply emulatorUiSmokeResult.

## Fixture Replay
- liaoqi_last_other_pass/liaoqi_last_other_pass: PASS
  lastSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  panelState: ROUTE_PANEL
  parserName: LiaoqiRealParser
  failReason: none
- liaoqi_last_me_wait/liaoqi_last_me_wait: PASS
  lastSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  panelState: WAIT_PANEL
  parserName: LiaoqiRealParser
  failReason: none
- liaoqi_post_panel_contaminated/liaoqi_post_panel_contaminated: PASS
  lastSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  panelState: WAIT_PANEL
  parserName: LiaoqiRealParser
  failReason: none
- liaoqi_read_receipt_status/liaoqi_read_receipt_status: PASS
  lastSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  panelState: WAIT_PANEL
  parserName: LiaoqiRealParser
  failReason: none
- generic_time_metadata_trap/generic_time_metadata_trap: PASS
  lastSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  panelState: CONTEXT_REQUIRED_PANEL
  parserName: GenericVisualBubbleParser
  failReason: none
- unsupported_app_no_chat_rows/unsupported_app_no_chat_rows: PASS
  lastSpeaker: null
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  panelState: UNSUPPORTED_APP_PANEL
  parserName: NoChatRows
  failReason: none

## Fixture Categories
- liaoqi_last_other_pass: covered
- liaoqi_last_me_wait: covered
- liaoqi_post_panel_contaminated: covered
- liaoqi_read_receipt_status: covered
- generic_time_metadata_trap: covered
- unsupported_app_no_chat_rows: covered

## MockChat Extension Coverage
- LAST_ME: covered
- LAST_OTHER: covered
- read/unread/checkmark: covered by read_receipt_status
- send failed: covered by send_failed
- long text: covered
- voice/image: covered
- font scale: covered by MockChatFontScaleMatrixReportTest
- screen width variation: covered by fixture bounds and emulator profile policy
- Huiyi overlay contamination: covered

## Synthetic Corpus
- blind_date: 29
- ambiguous_flirting: 29
- cold_reply: 29
- pressure: 29
- life_share: 29
- read_no_reply: 29
- user_multi_send: 26

## Real Device Smoke
- liaoqi LAST_ME: ME -> WAIT
- liaoqi LAST_OTHER: OTHER -> routes
- unsupported app: show unsupported prompt and export adapter bundle

## User Burden Reduction
- userDoesNotNeedRepeatedPrivateChatValidation: true
- userDoesNotNeedMultipleFiles: true
- userDoesNotNeedAllScenarios: true
