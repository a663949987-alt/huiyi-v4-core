# Passive Active UX Cleanup Report

## Basic Info

- taskName: no_local_passive_routes_and_express_self_simplify
- versionName: 4.1.64
- versionCode: 483
- generatedAt: 2026-07-06T09:39:26+08:00
- currentOverallResult: EMULATOR_PASS_NO_PHONE_REQUIRED
- userNeedsPhoneThisRound: false

## Scope

- Do not show local fallback reply routes in 下一句.
- Keep LAST_ME as local WAIT.
- If LAST_OTHER has no cloud/cache verified playbook, show a passive wait panel instead of local routes.
- Simplify 表达我 panel and hide calibration feedback by default.
- Stabilize repeat clicks on 表达我 with result reuse.
- Add Xiaoenai generic trial handling for `com.xiaoenai.app` / `小恩爱`.

## Key Results

- nextSentenceCloudOnly: true
- passiveWaitPanelImplemented: true
- passiveWaitPanelShown: true
- passiveWaitPanelTitle: 先等一下
- passiveWaitPanelDecisionType: PASSIVE_NOT_READY
- passiveWaitPanelTerminalState: PASSIVE_WAIT_PANEL
- localPassiveRoutesGenerated: true
- localPassiveRoutesShownToUser: false
- nextSentenceRouteCountWhenNoCloudPlaybook: 0
- nextSentenceHasNoPersonaFeedback: true
- lastMeLocalWaitPreserved: true

## Express Self

- expressSelfPanelSimpleMode: true
- expressSelfFeedbackDefaultVisible: false
- expressSelfFeedbackCollapsed: true
- expressSelfDefaultRouteCountMaxThree: true
- expressSelfRepeatClickStable: true
- expressSelfResultCacheImplemented: true
- expressSelfRepeatDoesNotBecomeSnapshotUntrusted: true

## Xiaoenai

- xiaoenaiPackage: com.xiaoenai.app
- xiaoenaiWindowTitle: 小恩爱
- xiaoenaiHandled: GENERIC_TRIAL
- xiaoenaiNotDesktopOrLauncher: true

## Verification

- unitTests: PASS
- appAssembleDebug: PASS
- mockchatAssembleDebug: PASS
- emulatorSmoke: PASS
- emulatorSerial: emulator-5554
- emulatorSmokeReport: outputs/gpt_review_inbox/passive-active-ux-cleanup-emulator-smoke-for-gpt.md
- emulatorSmokeJson: outputs/gpt_review_inbox/passive-active-ux-cleanup-emulator-smoke.json
- screenshotsPath: outputs/gpt_review_inbox/passive_active_ux_cleanup_emulator_smoke

## Notes For GPT

- This round is not an APK release round.
- User should not test phone yet for this task.
- The important behavior change is that 下一句 no longer exposes local passive routes when cloud/cache playbook is unavailable.
- 表达我 remains available for active self-expression and is now visually lighter by default.
