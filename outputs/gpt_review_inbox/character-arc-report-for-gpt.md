# Huiyi v4 Character Arc Report

## Basic Info

- project: Huiyi v4 Core
- taskName: persona_character_arc_reveal_hook
- versionName: 4.1.42
- versionCode: 461
- generatedAt: 2026-07-04T14:49:31+08:00
- overallResult: LOCAL_INTERFACE_PASS_NO_PHONE_REQUIRED
- userNeedsPhoneThisRound: false
- realDeviceRequiredThisRound: false

## Scope

This round adds the "Character Arc / 人物弧光" concept to the existing persona self-expression hook.

It does not rewrite Light Listening and does not change:

- parser
- session isolation
- cloud callback discard
- one-tap feedback binding
- NextSentenceSession state machine
- light-listen capture / persistence flow
- overlay UI structure

## CharacterArcCard

`CharacterArcCard` exists in:

- app/src/main/java/com/huiyi/v4/domain/model/PersonaModels.kt

Fields:

- surfaceImpression: what she currently sees on the surface
- hiddenDepth: the part of the user she has not seen yet
- contrastTension: the grounded contrast that creates character depth
- revealTrigger: chat nodes where this depth is safe to reveal
- safeRevealLine: one light expression line
- overdoRisk: what it becomes if overdone
- relatedPersonaCardIds: links back to existing persona cards

Intent:

- reveal authentic depth gradually
- do not fabricate a fake persona
- do not over-explain or self-prove
- keep the other person's topic as the main line

## NextMoveType

`NextMoveType.ARC_REVEAL` added.

Existing Lite self-expression hook remains intact:

- WAIT
- RECEIVE_OTHER
- EXPRESS_SELF
- CO_CREATE_MEANING
- ARC_REVEAL
- LIGHTEN_MOOD
- WITHDRAW

## Route Family

`ReplyRouteType.ARC_REVEAL` added.

`ReplyRoute.routeFamily` now exposes `routeType.name`, so ARC routes can be inspected as:

- routeFamily = ARC_REVEAL

Local fallback behavior:

- If lastSpeaker=OTHER
- and recent text mentions reality / planning / stability / past experience / responsibility / future
- local ReplyRouteGenerator may include at least one ARC_REVEAL route.

Cloud behavior:

- OpenAI-compatible cloud parser now reads `routeFamily`.
- `routeFamily=ARC_REVEAL` maps to `ReplyRouteType.ARC_REVEAL`.
- Cloud prompt now asks for ARC_REVEAL when safe and relevant.

## User-Facing Principle

The route is not for inventing a cooler persona. It is for letting the other person gradually see a real, three-dimensional, contrast-bearing user.

Example safe reveal:

> 我可能表达不算花，但认真起来会把事一点点做到位。

Overdo risk:

- turns into self-proof
- turns into preaching
- steals the main topic from the other person

## Validation

Command executed:

- ./gradlew.bat :app:testDebugUnitTest --tests "com.huiyi.v4.CharacterArcRevealTest" --tests "com.huiyi.v4.LightChatStateStoreTest"

Result:

- PASS

Test coverage:

- CharacterArcCard fields exposed through SelfExpressionOpportunity: PASS
- Last OTHER planning/responsibility topic adds ARC_REVEAL route family: PASS
- Cloud routeFamily=ARC_REVEAL maps to ReplyRouteType.ARC_REVEAL: PASS
- LightChatStateStore previous Lite tests: PASS

## Safety

- longTermRawChatStorage: false
- autoSend: false
- rawPrivateChatUploadedToGithub: false
- rawPrivateChatInReport: false
- fakePersonaGeneration: false
- lightListeningRewrite: false

## Result

Character Arc is now available as a minimal persona/self-expression hook. It can surface an ARC_REVEAL route when the conversation naturally reaches reality, planning, stability, past experience, responsibility, or future.
