# Express Self Eligibility Emulator Smoke

- taskName: expressSelfEligibility_and_hold_back_fix
- versionName: 4.1.66
- versionCode: 485
- generatedAt: 2026-07-06T11:10:40
- emulatorDetected: True
- emulatorSerial: emulator-5554
- huiyiInstalled: True
- mockchatInstalled: True
- accessibilityEnabled: True
- overlayPermissionGranted: True
- unitTestsResult: PASS
- fixtureTestsResult: PASS
- expressSelfUnsupportedBlocked: True
- expressSelfRecentLastMeHoldBack: True
- expressSelfColdStartAllowed: True
- expressSelfPlanningArcReveal: True
- v4161BugFixtureFixed: True
- userNeedsPhoneThisRound: false
- finalOverallResult: PASS
- reason: EXPRESS_SELF_ELIGIBILITY_EMULATOR_AND_FIXTURE_PASS

## Evidence

- screenshotsPath: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke
- logcatPath: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/express_self_eligibility_logcat.txt
- lastMeSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/last_me_express-session.json
- lastOtherSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/last_other_express-session.json
- desktopSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/desktop_express-session.json

## Scenario Assertions

- LAST_ME just sent -> terminalState: HOLD_BACK_PANEL, decisionType: HOLD_BACK, routeCount: 0, cloudAttempted: False, eligibilityMode: BLOCK_RECENT_LAST_ME
- LAST_OTHER planning/stability -> terminalState: EXPRESS_SELF_PANEL, routeTypesCsv: WARM_UP,SELF_STORY,ARC_REVEAL, routeCount: 3, eligibilityMode: ALLOW_EXPRESS_SELF
- launcher/desktop -> terminalState: CONTROLLED_FAIL_PANEL, decisionType: PRE_ANALYSIS_CONTAMINATED, routeCount: 0, cloudAttempted: False
- cold chat long inactive -> assertionSource: ENGINE_FIXTURE_LONG_INACTIVE, result: True
