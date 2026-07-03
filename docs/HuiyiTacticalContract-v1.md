# HuiyiTacticalContract v1

## 1. Product Foundation

Huiyi is a relationship tactical HUD. It is not an auto-reply bot, not a profile report, and not a tool that sends messages for the user.

The foundation is:

Relationship is not created by chat itself. It is created when two people use chat to co-create shared meaning that did not exist before.

Cloud analysis may improve tactical content, but it must stay inside this product contract.

## 2. Input Boundary

The client sends only the current safe tactical context:

- current effective chat messages
- local last speaker decision
- local decision before cloud
- app package
- message delivery/read status
- privacy metadata

The cloud must not decide who spoke last. LastSpeakerDecision is local truth.

## 3. Output Schema

```json
{
  "schemaVersion": 1,
  "decisionType": "NORMAL_REPLY | EMPATHY_FIRST | CONTEXT_REQUIRED",
  "decisionTypeFamily": "REPLY_ROUTES | CONTEXT_REQUIRED",
  "situation": "",
  "coCreationPoint": {
    "exists": true,
    "type": "",
    "evidence": "",
    "meaning": ""
  },
  "userLikelyMistake": "",
  "bestMove": "",
  "intensityPolicy": {
    "level": "LOW | MEDIUM | HIGH",
    "reason": ""
  },
  "riskWarning": "",
  "fallbackMove": "",
  "routes": [
    {
      "slot": "stable | light | question | daily_life | warmer",
      "message": "",
      "why": "",
      "riskLevel": "LOW | MEDIUM | HIGH",
      "fallbackMove": ""
    }
  ]
}
```

## 4. Required Fields

- `schemaVersion` must be `1`.
- `decisionType` must be `NORMAL_REPLY`, `EMPATHY_FIRST`, or `CONTEXT_REQUIRED`.
- `decisionTypeFamily` must be `REPLY_ROUTES` or `CONTEXT_REQUIRED`.
- `coCreationPoint` must exist and include `exists`, `type`, `evidence`, and `meaning`.
- `userLikelyMistake`, `bestMove`, `intensityPolicy.reason`, and `fallbackMove` must be non-empty.
- `riskWarning` must exist. It may be empty only when risk is low.
- `NORMAL_REPLY` and `EMPATHY_FIRST` must return exactly 5 unique routes.
- `CONTEXT_REQUIRED` must not return routes.

## 5. Forbidden Output

Cloud output must not:

- imply automatic sending
- claim a message has already been sent
- override local `LAST_ME -> WAIT`
- use PUA, coercion, begging, manipulation, or pressure
- escalate intensity beyond the local safety gate
- generate routes when the local speaker is `ME` or `UNKNOWN`

## 6. Local Safety Gate

The local app always decides whether cloud may run.

- `ME`: local `WAIT`, routeCount `0`, cloudAttempted `false`, decisionSource `LOCAL_WAIT`.
- `UNKNOWN`: no cloud, decisionSource `LOCAL_FALLBACK` or local controlled fail.
- unsupported app: no cloud, cloudSkippedReason `UNSUPPORTED_APP`.
- `OTHER`: cloud may run only when `cloudEnabled=true` and endpoint is configured.

## 7. Cloud Failure Fallback

If cloud is disabled, not configured, times out, returns invalid schema, or violates this contract:

- invalid cloud content is not shown
- local routes remain available when local route generation is safe
- `decisionSource=LOCAL_FALLBACK`
- `cloudFallbackUsed=true` only when cloud was attempted
- the UI must not show a generic failure when local fallback exists

## 8. Validator Rules

The validator enforces:

- schema version and legal enums
- required tactical fields
- required co-creation point
- exactly 5 unique routes for reply decisions
- no routes for context-required decisions
- non-empty route message and why
- legal risk and intensity values
- no auto-send implication
- no coercive or manipulative language
- no conflict with local last speaker safety gate

## 9. Golden Replay Cases

1. LAST_ME with cloud enabled:
   `cloudAttempted=false`, `decisionType=WAIT`, `decisionSource=LOCAL_WAIT`.

2. LAST_OTHER with cloud disabled:
   `cloudAttempted=false`, `decisionSource=LOCAL_FALLBACK`, `routeCount=5`.

3. LAST_OTHER with valid cloud response:
   `cloudAttempted=true`, `cloudSuccess=true`, `decisionSource=CLOUD`, `routeCount=5`.

4. Invalid cloud schema:
   `cloudSuccess=false`, `cloudFallbackUsed=true`, `decisionSource=LOCAL_FALLBACK`.

5. Manipulative cloud output:
   validator fails, invalid content is not shown, local fallback is used.
