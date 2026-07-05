# GPT Review Inbox

- taskName: dynamic_playbook_cloud_refresh_emulator_smoke
- versionName: 4.1.61
- versionCode: 480
- currentOverallResult: EMULATOR_DYNAMIC_PLAYBOOK_CLOUD_REFRESH_PASS
- userNeedsPhoneThisRound: false
- apkPath: outputs/update_server/huiyi-v4.1.61-debug.apk
- apkSha256: DEE518C42E53087700C5AC579EBDC664037A4DACB79FB3DFA43993E00D4E3209
- lanUpdateAvailable: true
- latestJsonPath: outputs/update_server/latest.json
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- dynamicPlaybookCloudRefreshSmokeResult: PASS
- emulatorDetected: true
- emulatorSerial: emulator-5554
- localPlaybookFirstResultPass: true
- passiveNextLatencyMs: 300
- activeExpressionLatencyMs: 300
- cloudRefreshAttempted: true
- cloudRefreshSuccess: true
- cloudContractValidationResult: PASS
- playbookCacheUpdatedFromCloud: true
- nextClickReadsCloudEnhancedPlaybook: true
- staleCloudRefreshDiscarded: true
- staleDiscardReason: CHAT_KEY_CHANGED
- lastMeWaitPass: true
- lastMeCloudAttempted: false

## Main Reports

- outputs/gpt_review_inbox/dynamic-playbook-cloud-refresh-emulator-smoke-for-gpt.md
- outputs/gpt_review_inbox/dynamic-playbook-cloud-refresh-emulator-smoke.json
- outputs/update_server/latest.json

## Evidence

- outputs/gpt_review_inbox/dynamic_playbook_cloud_refresh_emulator_smoke/

## Test Evidence

- app:testDebugUnitTest DeepSeekRelationshipPlaybookTest + DynamicPlaybookEngineTest: PASS
- app:assembleDebug: PASS
- dynamic playbook cloud refresh emulator smoke: PASS
