# Real Use Acceptance Report

## Basic Info

- project: Huiyi v4 Core
- taskName: real_use_package_next_sentence_express_self_playbook
- versionName: 4.1.61
- versionCode: 480
- currentOverallResult: EMULATOR_REAL_USE_PASS
- userNeedsPhoneThisRound: true
- apkPath: outputs/update_server/huiyi-v4.1.61-debug.apk
- apkSha256: A04257AFC7554DD20A7C48FECAD9A95593D8A240A9F42C17E08C691A4B237BFD
- lanUpdateAvailable: true
- latestJsonPath: outputs/update_server/latest.json
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true

## Product Structure

- floatingMenuOnlyRealUseEntries: true
- floatingMenuEntries:
  - 下一句
  - 表达我
  - 这次不对，发给 GPT
  - 隐藏
- passiveNextSentenceChanged: true
- passiveActiveStillSeparated: true
- lightListeningBaseChanged: false
- parserChanged: false
- sessionStateMachineChanged: false
- cloudCallbackChanged: false

## Next Sentence

- nextSentenceReady: true
- lastMeWaitReady: true
- lastMeCloudAttempted: false
- lastMeRouteCount: 0
- lastOtherReadsPassiveNext: true
- nextSentenceLocalFallbackImmediate: true
- nextSentenceCloudNonBlocking: true
- nextSentenceNoPersonaFeedback: true
- nextSentenceChineseRoutes: true
- nextSentenceLatencyMsEmulator: 300

## Express Self

- expressSelfReady: true
- expressSelfReadsActiveExpression: true
- expressSelfLocalFallbackImmediate: true
- expressSelfCloudNonBlocking: true
- expressSelfShowsExpressionMode: true
- expressSelfShowsCurrentTheme: true
- expressSelfShowsWhyThisCanBeSaid: true
- expressSelfShowsWhatNotToSay: true
- expressSelfChineseRoutes: true
- expressSelfLatencyMsEmulator: 300
- holdBackNoArcRevealRoute: true
- elevateMeaningHasCoCreateRoute: true
- switchFacetDoesNotRepeatLastSurfaceLineRedacted: true

## Playbook Cache / Cloud

- nextSentenceReadsPassiveNext: true
- expressSelfReadsActiveExpression: true
- localFallbackImmediate: true
- cloudEnhancementNonBlocking: true
- staleCloudRefreshDiscarded: true
- staleCloudRefreshDiscardedEvidence: DynamicPlaybookEngineTest.CloudRefreshIsOptionalAndStaleChatResultIsDiscardedTest
- emulatorCloudRefreshActuallyAttempted: false

## Corpus Validation

- corpusScenarioCount: 12
- corpusPassCount: 12
- nextSentencePassCount: 12
- expressSelfPassCount: 12
- arcRevealScenarioPassCount: 1+
- holdBackScenarioPassCount: 1+
- englishLeakCount: 0

## Emulator Smoke

- emulatorSmokeResult: PASS
- emulatorDetected: true
- emulatorSerial: emulator-5554
- huiyiInstalled: true
- mockchatInstalled: true
- accessibilityEnabled: true
- overlayPermissionGranted: true
- oneClickImmediateResultPass: true
- realUseEmulatorSmokeReport: outputs/gpt_review_inbox/real-use-emulator-smoke-for-gpt.md

## Build / Test

- app:testDebugUnitTest: PASS
- app:assembleDebug: PASS
- mockchat:assembleDebug: PASS

## User Test Gate

All required gates are satisfied:

- APK generated: true
- latest.json updated: true
- emulator smoke PASS: true
- englishLeakCount = 0: true
- Next Sentence and Express Self emulator results within 1s: true

User may test v4.1.61 on phone via LAN update or local APK install.
