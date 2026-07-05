# DeepSeek V4 Pro vs GPT-5.4 Speed Summary

- generatedAt: 2026-07-05T09:07:00+08:00
- relayBaseUrl: https://toapis.com/v1
- apiKeyIncluded: false
- promptEncoding: UTF-8
- input: same Chinese chat context + same JSON output contract
- comparedModels: gpt-5.4, deepseek-v4-pro

## Model Id Check

- gpt-5.4: available
- deepseek-v4-pro: available

## Drop-In Test At 700 Max Tokens

This test used the same input and same JSON response contract.

- gpt-5.4: ok 3/3, parseOk 3/3, routeCountOk 3/3, avgLatencyMs 8406, min 7163, max 9266
- deepseek-v4-pro: ok 3/3, parseOk 1/3, routeCountOk 1/3, avgLatencyMs 13269, min 6710, max 16910

Finding:

- deepseek-v4-pro often spent the token budget on reasoning and returned finishReason=length, so it was not stable at 700 max tokens.

## App-Like Test At 1200 Max Tokens

The current Android provider uses max_tokens=1200. This is the most relevant speed check for app integration.

- gpt-5.4: ok 3/3, parseOk 3/3, routeCountOk 3/3
- gpt-5.4 latenciesMs: 12324, 7155, 10803
- gpt-5.4 avgLatencyMs: 10094
- gpt-5.4 reasoningTokens: 224, 126, 257

- deepseek-v4-pro: ok 3/3, parseOk 3/3, routeCountOk 3/3
- deepseek-v4-pro latenciesMs: 18186, 21090, 13844
- deepseek-v4-pro avgLatencyMs: 17707
- deepseek-v4-pro reasoningTokens: 696, 948, 525

Finding:

- deepseek-v4-pro became stable at 1200 max tokens, but it was slower and used far more reasoning tokens.
- On this relay path and prompt, deepseek-v4-pro was about 7.6 seconds slower on average than gpt-5.4.

## Recommendation

- Do not switch the mainline from gpt-5.4 to deepseek-v4-pro for speed.
- If DeepSeek is added later, use it as an experimental provider behind a model router, not as the default.
- For a DeepSeek speed candidate, deepseek-v4-flash exists in the relay model list and should be tested separately.
