# Cloud Model Trace Routing Report

## Basic

- project: huiyi-v4
- taskName: cloud_model_trace_and_ds_runtime_guard
- versionName: 4.1.71
- versionCode: 490
- generatedAt: 2026-07-06T18:12:00+08:00
- currentOverallResult: LOCAL_ROUTING_PASS_NO_PHONE_REQUIRED
- userNeedsPhoneThisRound: false

## What Changed

- CloudModelTrace added for playbook cloud refresh requests.
- Reports now expose actualCloudModelUsed, passiveNextModelUsed, expressSelfModelUsed, arcRevealModelUsed, deepAnalysisModelUsed.
- ModelRouter no longer routes validator fail to deepseek-v4-pro.
- deepseek-v4-flash remains available only as cheap draft for PASSIVE_PLAYBOOK.
- ACTIVE_EXPRESSION and ARC_REVEAL route to gpt-5.4 or configured strong model.
- DS Flash playbook cache writes are allowed only after strict cloud contract validation PASS.

## Runtime Model Policy

- LAST_ME -> LOCAL_WAIT
- UNKNOWN -> LOCAL_CONTEXT_REQUIRED
- PASSIVE_PLAYBOOK normal OTHER -> deepseek-v4-flash cheap draft
- ACTIVE_EXPRESSION -> gpt-5.4 / configuredStrongModel
- ARC_REVEAL -> gpt-5.4 / configuredStrongModel
- DEEP_ANALYSIS -> gpt-5.5
- validator fail -> GPT_STRONG or LOCAL_FALLBACK
- deepseek-v4-pro runtimeEnabled=false

## Required Fields

- requestedModel: implemented
- selectedModel: implemented
- providerType: implemented
- routeReason: implemented
- routeTarget: implemented
- requestPurpose: implemented
- cloudContractValidationResult: implemented
- playbookCacheWriteAllowed: implemented
- playbookCacheWriteBlockedReason: implemented

## Model Report Fields

- actualCloudModelUsed: implemented
- passiveNextModelUsed: implemented
- expressSelfModelUsed: implemented
- arcRevealModelUsed: implemented
- deepAnalysisModelUsed: implemented

## DS Flash / DS Pro Guard

- dsFlashCheapDraftOnly: true
- dsFlashAllowedForPassivePlaybook: true
- dsFlashDefaultForActiveExpression: false
- dsFlashDefaultForArcReveal: false
- dsProRuntimeDisabled: true
- validatorFailRoutesToDsPro: false

## Tests

- ModelTraceRecordsActualModelTest: PASS
- ExpressSelfDoesNotDefaultToDsFlashWhenArcRevealRequiredTest: PASS
- DsProDisabledInRuntimeTest: PASS
- PassivePlaybookMayUseDsFlashCheapDraftTest: PASS
- ArcRevealUsesStrongOrConfiguredModelTest: PASS
- app:testDebugUnitTest: PASS
- app:assembleDebug: PASS

## Safety Scan

- apiKeyExposedInReports: false
- relayApiKeyValueCommitted: false
- secretScanResult: PASS_RUNTIME_VARIABLES_ONLY

## Notes For GPT

The latest benchmark no longer recommends deepseek-v4-flash as the character arc main model. It is intentionally limited to passive playbook cheap draft usage, and its output cannot update PlaybookCache unless the strict cloud playbook parser/validator passes.
