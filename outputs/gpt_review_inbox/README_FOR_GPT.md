# Huiyi v4 GPT Review Inbox

## Current Task
- taskName: relay_model_benchmark_default_playbook_model_search
- versionName: 4.1.56
- versionCode: 475
- currentOverallResult: RELAY_MODEL_BENCHMARK_COMPLETE_NO_RUNTIME_SWITCH
- generatedAt: 2026-07-05T10:45:23+08:00
- userNeedsPhoneThisRound: false
- realDeviceSmokeResult: NOT_REQUIRED

## What Ran
- Fetched relay `/models`.
- Found 111 raw models and 68 text/chat candidates.
- Tested 10 text candidates with 20 normalized relationship-playbook samples each.
- No screenshots were sent.
- No API key is included in reports.

## Tested Models
- deepseek-v4-flash
- qwen3.5-plus
- qwen3.5-flash
- kimi-k2.5
- glm-5
- gpt-5.4
- qwen3-max
- MiniMax-M2.5
- claude-sonnet-4-6
- gemini-3-flash-official

## Result
- recommendedDefaultModel: NONE
- recommendedStrongModel: NONE
- closestStrongReference: gpt-5.4
- runtimeSwitchRecommended: false
- DS Pro runtimeEnabled: false

## Why No Switch
- deepseek-v4-flash was fast enough but unstable across 20 samples: contractPassRate 20%, responseParsedRate 45%.
- qwen3.5-plus / qwen3.5-flash / glm-5 mostly timed out.
- kimi-k2.5 handled arc routes well but contractPassRate was only 65% and avgLatencyMs was 19156.
- gpt-5.4 was the closest reference, but contractPassRate was 85%, routeCountPassRate 85%, avgLatencyMs 24473.
- MiniMax-M2.5 was promising but still below threshold: contractPassRate 80%, routeCountPassRate 85%, avgLatencyMs 20131.

## Recommended Router Policy
- LAST_ME -> LOCAL_WAIT
- UNKNOWN -> LOCAL_CONTEXT_REQUIRED
- normal OTHER -> KEEP_CURRENT_RUNTIME_MODEL
- arc/reality/planning/future -> KEEP_CURRENT_RUNTIME_MODEL
- validator fail -> KEEP_CURRENT_STRONG_MODEL
- strongModel fail -> LOCAL_FALLBACK
- timeout -> LOCAL_FALLBACK

## GPT Should Inspect
1. outputs/gpt_review_inbox/relay-available-models-for-gpt.md
2. outputs/gpt_review_inbox/relay-available-models.json
3. outputs/gpt_review_inbox/relay-model-benchmark-for-gpt.md
4. outputs/gpt_review_inbox/relay-model-benchmark.json
5. outputs/codex_to_gpt/result-manifest.json

## Delivery
- apkGeneratedThisRound: false
- lanUpdateTouchedThisRound: false
- appUiChanged: false
- userNeedsPhoneThisRound: false
