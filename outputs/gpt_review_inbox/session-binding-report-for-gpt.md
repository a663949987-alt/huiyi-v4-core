# Session Binding Report For GPT

## Current round
- taskName: local_state_chain_closure_before_cloud
- versionName: 4.1.23
- versionCode: 443
- currentOverallResult: CONTROLLED_LOCAL_PASS

## Current result split
- fixtureReplayResult: PASS
- mockChatBuildResult: PASS
- emulatorUiSmokeResult: NOT_RUN
- realDeviceSmokeResult: NOT_TESTED
- phoneLatestFreshness: NOT_AVAILABLE
- phoneLatestUsedForCurrentResult: false

## Session binding
- feedbackExportsBoundSession: PASS
- feedbackTriggeredNewAnalysis: false
- feedbackReCapturedCurrentRoot: false
- feedbackUsedOverlayStateAsPreAnalysis: false
- preAnalysisSnapshotFrozen: PASS
- preAnalysisSnapshotMutableAfterPanel: false
- postPanelSnapshotUsedForDecision: false
- sessionImmutableAfterTerminalState: true

## LAST ME hard gate
- lastMeHardWaitRule: PASS
- contextRequiredCanOverrideLastMe: false
- routePanelCanShowForLastMe: false
- cloudCanRunForLastMe: false
- actualLastSpeaker=ME -> decisionType=WAIT -> terminalState=WAIT_PANEL -> routeCount=0 -> cloudAttempted=false

## phone/latest
- phoneLatestVersionName: 4.1.23
- phoneLatestVersionCode: 443
- phoneLatestFreshness: NOT_AVAILABLE
- phoneLatestUsedForCurrentResult: false
- phoneLatestIgnoredReason: NO_NEW_PHONE_BUNDLE_THIS_ROUND_USER_NOT_REQUIRED_TO_TEST
- phoneBundleIncluded: false
- phoneBundleRequiredFromUser: false

## Cloud
- cloudEnabled: false
- cloudConfigured: false
- cloudAttempted: false
- decisionSource: LOCAL_WAIT / LOCAL_FALLBACK

## User testing
- userNeedsPhoneThisRound: false
- userInstruction: 本轮用户不需要手机测试。

## Tests
- LocalStateChainClosureTest: PASS
- OneTapFeedbackExportTest: PASS
- LastMeWaitPriorityAndStatusMetadataFixTest: PASS
- CloudAnalysisMvpSafetyGateTest: PASS
- SimulationFirstValidationTest: PASS
- mockChatBuildResult: PASS
- assembleDebug: PASS
