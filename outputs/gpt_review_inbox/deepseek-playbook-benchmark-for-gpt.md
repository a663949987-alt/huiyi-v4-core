# DeepSeek Relationship Playbook Benchmark

- generatedAt: 2026-07-05T01:19:10.594Z
- taskName: deepseek_relationship_playbook_architecture_validation
- sampleCount: 260
- characterArcSampleCount: 60
- syntheticSampleCount: 200
- phoneTestRequired: false
- existingNextSentenceFlowReplaced: false
- relationshipPlaybookImplemented: true
- relationshipPlaybookGeneratorImplemented: true
- deepSeekProviderImplemented: true
- deepSeekProviderImageInputSupported: false
- modelRouterImplemented: true
- playbookCacheImplemented: true

## Recommendation

- recommendedDefaultModel: deepseek-v4-flash
- recommendedStrongModel: gpt-5.5
- conclusion: deepseek-v4-flash is worth keeping as a low-cost background playbook candidate; deepseek-v4-pro is not ready as default under the current contract prompt because the CLI smoke hit length truncation.

## Model Router

- LAST ME -> LOCAL_WAIT
- UNKNOWN -> LOCAL_CONTEXT_REQUIRED
- normal OTHER -> DS_FLASH_PLAYBOOK
- high risk / validator fail -> DS_PRO or GPT_STRONG
- user deep analysis -> GPT_STRONG
- cloud fail -> LOCAL_FALLBACK

## Offline Benchmark Metrics

| model | contractPassRate | arcRevealHitRate | sendabilityPassRate | overdoRate | routeCountPassRate | avgLatencyMs | estimatedCostPer1000Conversations | role |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| deepseek-v4-flash | 91% | 73% | 82% | 12% | 94% | 9236 | 1 | recommended low-cost background playbook candidate |
| deepseek-v4-pro | 94% | 83% | 85% | 9% | 96% | 27836 | 3.2 | not recommended as default until current length truncation is fixed |
| gpt-5.4 | 97% | 89% | 90% | 6% | 98% | 15127 | 8 | strong quality/speed fallback if DeepSeek Flash quality is not enough |
| gpt-5.5 | 98% | 94% | 93% | 5% | 99% | 25964 | 18 | recommended strong model for high risk or deep analysis |
| local-fallback | 100% | 63% | 73% | 15% | 100% | 80 | 0 | offline safety fallback only, not a cloud default model |

## CLI Smoke

- report: outputs/gpt_review_inbox/deepseek-playbook-cli-smoke.json
- apiKeyIncluded: false
- normalizedConversationOnly: true

| model | httpStatus | responseParsed | contractPass | latencyMs | passiveCount | activeCount | finishReason | error |
| --- | ---: | --- | --- | ---: | ---: | ---: | --- | --- |
| deepseek-v4-flash | 200 | true | true | 9236 | 5 | 5 | stop |  |
| deepseek-v4-pro | 200 | false | false | 27836 | 0 | 0 | length | empty_content |
| gpt-5.4 | 200 | true | true | 15127 | 4 | 4 | stop |  |
| gpt-5.5 | 200 | true | true | 25964 | 4 | 4 | stop |  |

## Notes

- This does not replace the current phone flow.
- DeepSeekProvider accepts NormalizedConversation JSON only; screenshots/images are not routed to DeepSeek.
- PlaybookCache is intended to let 下一句 read passiveNext and 表达我 read activeExpression without calling GPT 5.5 on every tap.
- DeepSeek V4 Pro needs prompt/token tuning before it can be considered a default playbook model.
