# Changed Files for GPT

## Task
- taskName: cloud_contract_mvp_with_local_safety_gate
- versionName: 4.1.24
- versionCode: 443
- currentOverallResult: CLOUD_CONTRACT_LOCAL_PASS

## Summary
- Synced GPT review inbox and Codex-to-GPT manifest to v4.1.24.
- Added OPENAI_COMPATIBLE_RELAY runtime configuration skeleton.
- Added runtime-only relay API key handling with report redaction.
- Added relay fallback reasons for missing endpoint/key.
- Added cloud report fields for providerType and relay key safety.
- Added tests for relay key redaction, missing-key fallback, LAST_ME safety, LAST_OTHER relay success, and invalid relay fallback.

## Safety
- No real key committed.
- BuildConfig cloud client token remains empty.
- Reports only expose hasRelayApiKey / relayApiKeyConfigured booleans.
