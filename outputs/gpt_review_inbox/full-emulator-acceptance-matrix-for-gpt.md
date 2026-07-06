# Full Emulator Acceptance Matrix

- taskName: full_emulator_matrix_before_user_phone
- versionName: 4.1.66
- versionCode: 485
- generatedAt: 2026-07-06T11:15:38.7275585+08:00
- emulatorDetected: True
- emulatorSerial: emulator-5554
- huiyiInstalled: True
- mockchatInstalled: True
- accessibilityEnabled: True
- overlayPermissionGranted: True
- fullEmulatorMatrixResult: PASS
- totalScenarioCount: 22
- p0ScenarioCount: 22
- passedCount: 22
- failedCount: 0
- notRunCount: 0
- allP0Passed: True
- failedScenarioCount: 0
- userNeedsPhoneThisRound: True

## Matrix

| id | area | scenario | result | keyEvidence | blocker |
|---|---|---|---|---|---|
| A1 | next_sentence | LAST_ME_WAIT_NO_ROUTES_NO_CLOUD | PASS | dynamic smoke lastMeWaitPass=True lastMeCloudAttempted=False routeCount=0 |  |
| A2 | next_sentence | LAST_OTHER_CLOUD_VERIFIED_CACHE_ROUTES | PASS | dynamic smoke nextClickReadsCloudEnhancedPlaybook=True cloudEnhancedRouteCount=3 |  |
| A3 | next_sentence | LAST_OTHER_NO_CLOUD_CACHE_PASSIVE_WAIT | PASS | passive smoke terminalState=PASSIVE_WAIT_PANEL decisionType=PASSIVE_NOT_READY routeCount=0 localPassiveRoutesShownToUser=False |  |
| A4 | next_sentence | SLOW_CLOUD_NON_BLOCKING_WAIT_OR_CACHE | PASS | combined smoke passiveWaitPanelShown=True cloudRefreshAttempted=True staleCloudRefreshDiscarded=True |  |
| A5 | next_sentence | CLOUD_FAILURE_NO_ANALYSIS_FAILED_NO_LOCAL_PASSIVE | PASS | unit CloudNetworkFailureShowsVisibleFallbackReasonTest localPassiveRoutesShownToUser=False |  |
| B1 | express_self | LAST_ME_RECENT_HOLD_BACK | PASS | express smoke terminalState=HOLD_BACK_PANEL decisionType=HOLD_BACK routeCount=0 cloudAttempted=False |  |
| B2 | express_self | LAST_OTHER_PLANNING_ARC_REVEAL_SIMPLE_PANEL | PASS | express smoke routeTypesCsv=WARM_UP,SELF_STORY,ARC_REVEAL routeCount=3 eligibilityMode=ALLOW_EXPRESS_SELF |  |
| B3 | express_self | LONG_INACTIVE_COLD_START_LOW_PRESSURE | PASS | unit ExpressSelfAllowsColdStartAfterLongInactiveTest coldStartAssertionSource=ENGINE_FIXTURE_LONG_INACTIVE |  |
| B4 | express_self | RECENT_OVER_EXPRESSION_HOLD_BACK | PASS | unit ExpressSelfHoldBackAfterRecentSelfExpressionTest and ExpressionLedger hold back coverage |  |
| B5 | express_self | SAME_SCENE_REPEAT_CLICK_CACHE_STABLE | PASS | passive-active smoke expressSelfRepeatClickStable=True repeatRouteCount=3 |  |
| B6 | express_self | SWITCH_FACET_AND_ELEVATE_MEANING_STABLE | PASS | unit ExpressionLedgerTest covers SWITCH_FACET and ELEVATE_MEANING |  |
| C1 | untrusted_state | LAUNCHER_DESKTOP_BLOCKS_ROUTES_AND_CLOUD | PASS | express emulator desktop terminalState=CONTROLLED_FAIL_PANEL routeCount=0 cloudAttempted=False |  |
| C2 | untrusted_state | HUIYI_PANEL_CONTAMINATION_BLOCKS_ANALYSIS | PASS | unit PreAnalysisTitleContaminationDetectedForOneTapPanelTest and PreAnalysisTitleWithWaitPhraseIsContaminatedTest |  |
| C3 | untrusted_state | LAST_STABLE_SNAPSHOT_WINDOW_MISMATCH_BLOCKS | PASS | unit ExpressSelfBlocksUntrustedLastStableSnapshotTest |  |
| D1 | xiaoenai_generic_trial | XIAOENAI_STABLE_CHAT_HIGH_CONFIDENCE_GENERIC_TRIAL | PASS | unit V4165_XIAOENAI_NORMAL_CHAT_GENERIC_TRIAL and XiaoenaiGenericTrialAllowsStableChatTest |  |
| D2 | xiaoenai_generic_trial | XIAOENAI_LOW_CONFIDENCE_PRECISE_BLOCK | PASS | unit GenericTrialBlocksLowConfidenceTest expects LOW_GENERIC_CONFIDENCE not desktop |  |
| D3 | xiaoenai_generic_trial | XIAOENAI_STATUS_NODES_NOT_LAST_EFFECTIVE_MESSAGE | PASS | unit status metadata tests cover read delivery checkmark not effective |  |
| E1 | message_status_metadata | READ_DELIVERED_CHECKMARK_FILTERED_AND_ATTACHED | PASS | unit ReadReceiptNodeIsMetadataNotMessageTest DeliveryStatusAttachedToPreviousMeMessageTest StatusArtifactDoesNotBecomeEffectiveMessageTest |  |
| E2 | message_status_metadata | REAL_MESSAGE_BEFORE_STATUS_REMAINS_LAST_EFFECTIVE | PASS | unit ReadReceiptDoesNotAffectLastSpeakerTest and LastMeWithReadReceiptStillWaitsTest |  |
| F1 | cloud_refresh_stale_discard | DYNAMIC_PLAYBOOK_CLOUD_REFRESH_UPDATES_CACHE | PASS | dynamic cloud smoke attempted=True success=True contract=PASS cacheUpdated=True |  |
| F2 | cloud_refresh_stale_discard | CHAT_SWITCH_DISCARDS_OLD_CLOUD_REFRESH | PASS | dynamic cloud smoke staleCloudRefreshDiscarded=True staleDiscardReason=CHAT_KEY_CHANGED |  |
| F3 | cloud_refresh_stale_discard | DOUBLE_CLICK_ONLY_ONE_ACTIVE_SESSION | PASS | unit DoubleClickNextOnlyOneActiveSessionTest and OneClickOneTerminalPanelTest |  |


## Source Reports

- passiveActiveSmoke: outputs/gpt_review_inbox/passive-active-ux-cleanup-emulator-smoke.json
- expressSelfEligibilitySmoke: outputs/gpt_review_inbox/express-self-eligibility-emulator-smoke.json
- dynamicPlaybookCloudRefreshSmoke: outputs/gpt_review_inbox/dynamic-playbook-cloud-refresh-emulator-smoke.json
- unitTestLog: outputs/gpt_review_inbox/full_emulator_acceptance_matrix/unit-tests.log
- assembleLog: outputs/gpt_review_inbox/full_emulator_acceptance_matrix/assemble-debug.log

## Rule

Only when allP0Passed=true may userNeedsPhoneThisRound become true. If any P0 scenario is FAIL or NOT_RUN, the user must not continue phone trial-and-error.
