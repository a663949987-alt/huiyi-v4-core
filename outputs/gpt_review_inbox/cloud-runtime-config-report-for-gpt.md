# Cloud Runtime Config Report for GPT

## Basic Info
- project: Huiyi v4 Core
- taskName: relay_secure_runtime_config_and_response_parsing_mvp
- versionName: 4.1.25
- versionCode: 444
- generatedAt: 2026-07-03 20:47:56 +0800
- currentOverallResult: CLOUD_RUNTIME_CONFIG_LOCAL_PASS
- userNeedsPhoneThisRound: false
- realDeviceRequiredThisRound: false
- realDeviceSmokeResult: NOT_TESTED
- cloudRealEndpointRequiredThisRound: false

## Required Fields
- relaySettingsUiImplemented: true
- providerType: OPENAI_COMPATIBLE_RELAY
- relayBaseUrlConfigured: false
- relayApiKeyConfigured: false
- relayApiKeyStoredSecurely: true
- relaySecureStorageImplementation: ANDROID_KEYSTORE_ENCRYPTED_SHARED_PREFERENCES
- relaySecureStorageRuntimeSmoke: NOT_TESTED_NO_PHONE_THIS_ROUND
- relayApiKeyExposedInRepo: false
- relayApiKeyExposedInApk: false
- cloudEnabledDefault: false
- cloudCanBeEnabledOnlyWhenSecureKeyStorage: true
- relayResponseParsed: true
- localFallbackOnInvalidCloud: true
- lastMeSkipsCloud: true
- unknownSpeakerSkipsCloud: true
- unsupportedAppSkipsCloud: true
- lastOtherCloudPathReady: true

## Behavior Contract
- LAST ME: local WAIT, routeCount=0, cloudAttempted=false.
- UNKNOWN: local fallback / blocked; cloudAttempted=false.
- Unsupported app: unsupported prompt path; cloudAttempted=false.
- LAST OTHER with secure endpoint/key configured: shows cloud analysis path and validates HuiyiTacticalContract v1 before display.
- Invalid cloud JSON or contract violation: cloudSuccess=false, cloudFallbackUsed=true, decisionSource=LOCAL_FALLBACK.

## Tests
- RelayApiKeyRequiresSecureStorageTest: PASS
- RelayApiKeyNotWrittenToRepoOrOutputsTest: PASS
- RelaySettingsRedactsApiKeyInReportsTest: PASS
- LastMeSkipsRelayCloudTest: PASS
- UnknownSpeakerSkipsRelayCloudTest: PASS
- LastOtherUsesRelayWhenConfiguredTest: PASS
- RelayResponseParsedIntoHuiyiContractTest: PASS
- RelayInvalidJsonFallsBackToLocalTest: PASS
- RelayContractViolationFallsBackToLocalTest: PASS
- RelaySuccessShowsCloudDecisionSourceTest: PASS
- OpenAICompatibleProviderDoesNotDiscardResponseTest: PASS
- :app:testDebugUnitTest: PASS
- :app:assembleDebug: PASS
- :mockchat:assembleDebug: PASS

## Security Notes
- No real API key was used.
- API key is runtime input only.
- API key is not written to source, BuildConfig, outputs, review bundle, or logcat by this implementation.
- Reports only expose relayApiKeyConfigured=true/false.
