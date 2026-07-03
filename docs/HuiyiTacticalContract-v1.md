# HuiyiTacticalContract v1

Status: TODO only. This contract is not enabled in the app yet.

## Foundation

Huiyi assumes that a relationship is not built by chatting itself. A relationship is built when two people use chat to co-create shared meaning that did not previously exist.

Huiyi is a relationship tactics HUD, not an auto-reply bot.

## Local Safety Gates

- Cloud analysis may only process LAST OTHER scenes.
- LAST ME must stay local.
- LAST ME must return WAIT, zero routes, and no cloud/model call.
- UNKNOWN speaker must not trigger high-confidence route generation.
- Voice without transcript must require a user summary before deep analysis.

## Required Cloud Output

Cloud output must include:

- `situation`
- `coCreationPoint`
- `userLikelyMistake`
- `intensityPolicy`
- `riskWarning`
- `fallbackMove`
- `routes[5]`

Each route must be concise, user-sendable, and safe under the local risk policy.

## Cloud Must Not

Cloud output must not contain:

- PUA
- coercion
- begging
- pressure tactics
- excessive escalation
- auto-send behavior
- promises on behalf of the user
- treating the relationship as a script exploit
- ignoring shared meaning

## Local Validation

Every cloud response must pass a local `CloudTacticalResponseValidator` before it can be shown.

The validator must reject:

- non-JSON or invalid schema
- missing required fields
- route count not equal to 5
- blank route text
- unsafe manipulation
- LAST ME cloud responses
- UNKNOWN speaker cloud responses
- responses that override local WAIT or context-required safety gates

## Current App Status

This contract is documentation only in v4.1.21. The app keeps cloud analysis disabled until this contract is implemented and tested.
