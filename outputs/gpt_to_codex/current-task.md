# GPT -> Codex Current Task

taskStatus: PENDING
project: Huiyi v4 Core
taskName: cloud_contract_mvp_with_local_safety_gate
versionName: 4.1.24
versionCode: 443
createdBy: GPT
userNeedsPhoneThisRound: false
realDeviceRequiredThisRound: false
cloudRealEndpointRequiredThisRound: false

## Goals
- Sync GPT review inbox to v4.1.24 cloud contract MVP.
- Support OPENAI_COMPATIBLE_RELAY runtime configuration skeleton.
- Keep relay API key runtime-only and redacted from reports.
- Preserve LAST_ME / UNKNOWN / unsupported-app local safety gates.
- Fallback locally when endpoint or runtime relay key is missing.

## Non Goals
- no real API key
- no real cloud endpoint required
- no phone testing
- no automatic sending

## Required Reports
- outputs/gpt_review_inbox/cloud-contract-mvp-report-for-gpt.md
- outputs/gpt_review_inbox/cloud-contract-mvp-report.json
- outputs/codex_to_gpt/result-manifest.json
