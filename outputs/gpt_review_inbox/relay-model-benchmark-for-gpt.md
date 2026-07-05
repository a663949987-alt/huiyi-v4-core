# Relay Model Benchmark

- generatedAt: 2026-07-05T02:45:23.456Z
- taskName: relay_model_benchmark_default_playbook_model_search
- completed: true
- apiKeyIncluded: false
- availableModelCount: 68
- testedModels: deepseek-v4-flash, qwen3.5-plus, qwen3.5-flash, kimi-k2.5, glm-5, gpt-5.4, qwen3-max, MiniMax-M2.5, claude-sonnet-4-6, gemini-3-flash-official
- completedModels: deepseek-v4-flash, qwen3.5-plus, qwen3.5-flash, kimi-k2.5, glm-5, gpt-5.4, qwen3-max, MiniMax-M2.5, claude-sonnet-4-6, gemini-3-flash-official
- sampleCountPerModel: 20
- recommendedDefaultModel: NONE
- recommendedStrongModel: NONE
- closestStrongReference: gpt-5.4

## Per Model Metrics

| model | runtimeEnabled | contractPassRate | routeCountPassRate | sendabilityPassRate | arcRevealHitRate | expressSelfHitRate | coCreateHitRate | overdoRate | allReceiveRoutesRate | allQuestionRoutesRate | avgLatencyMs | costScore | disabledReasons |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| deepseek-v4-flash | false | 20% | 45% | 5% | 0% | 0% | 0% | 0% | 0% | 0% | 10577 | 1 | finishReason=length; responseParsedRate<90; contractPassRate<90 |
| qwen3.5-plus | false | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 35007 | 2 | responseParsedRate<90; contractPassRate<90; avgLatencyMs>25000 |
| qwen3.5-flash | false | 0% | 30% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 33441 | 1.2 | responseParsedRate<90; contractPassRate<90; avgLatencyMs>25000 |
| kimi-k2.5 | false | 65% | 90% | 75% | 100% | 100% | 100% | 5% | 0% | 0% | 19156 | 2.5 | contractPassRate<90 |
| glm-5 | false | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 35006 | 2 | responseParsedRate<90; contractPassRate<90; avgLatencyMs>25000 |
| gpt-5.4 | false | 85% | 85% | 80% | 80% | 80% | 80% | 5% | 0% | 0% | 24473 | 8 | responseParsedRate<90; contractPassRate<90 |
| qwen3-max | false | 10% | 100% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 16289 | 3.5 | contractPassRate<90 |
| MiniMax-M2.5 | false | 80% | 85% | 60% | 80% | 80% | 80% | 0% | 0% | 0% | 20131 | 2.2 | responseParsedRate<90; contractPassRate<90 |
| claude-sonnet-4-6 | false | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 19258 | 12 | finishReason=length; responseParsedRate<90; contractPassRate<90 |
| gemini-3-flash-official | false | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 0% | 6809 | 2 | finishReason=length; responseParsedRate<90; contractPassRate<90 |

## Arc Trigger Smoke

- sample: B01 / planning-reality arc trigger
- evaluatedModel: deepseek-v4-flash
- result: {"contractPass":false,"characterArcOpportunityExists":false,"arcRevealHit":false,"expressSelfHit":false,"coCreateHit":false,"allReceiveRoutes":false,"allQuestionRoutes":false}

## Disabled Models

- deepseek-v4-flash: finishReason=length, responseParsedRate<90, contractPassRate<90
- qwen3.5-plus: responseParsedRate<90, contractPassRate<90, avgLatencyMs>25000
- qwen3.5-flash: responseParsedRate<90, contractPassRate<90, avgLatencyMs>25000
- kimi-k2.5: contractPassRate<90
- glm-5: responseParsedRate<90, contractPassRate<90, avgLatencyMs>25000
- gpt-5.4: responseParsedRate<90, contractPassRate<90
- qwen3-max: contractPassRate<90
- MiniMax-M2.5: responseParsedRate<90, contractPassRate<90
- claude-sonnet-4-6: finishReason=length, responseParsedRate<90, contractPassRate<90
- gemini-3-flash-official: finishReason=length, responseParsedRate<90, contractPassRate<90
- deepseek-v4-pro: previous_smoke_finishReason=length, previous_smoke_responseParsed=false, not_allowed_in_runtime_this_round

## Recommended Router Policy

- LAST_ME -> LOCAL_WAIT
- UNKNOWN -> LOCAL_CONTEXT_REQUIRED
- normal OTHER -> KEEP_CURRENT_RUNTIME_MODEL
- arc/reality/planning/future -> KEEP_CURRENT_RUNTIME_MODEL
- validator fail -> KEEP_CURRENT_STRONG_MODEL
- strongModel fail -> LOCAL_FALLBACK
- timeout -> LOCAL_FALLBACK

## Notes

- DS Pro is not allowed into runtime in this round.
- No screenshots were sent; every request used NormalizedConversation JSON.
- This benchmark does not change App UI and does not require phone testing.
