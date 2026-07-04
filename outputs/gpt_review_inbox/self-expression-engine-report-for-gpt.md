# Huiyi v4 Self Expression Engine Report

## Basic Info

- project: Huiyi v4 Core
- taskName: persona_character_arc_reveal_hook
- versionName: 4.1.42
- versionCode: 461
- generatedAt: 2026-07-04T15:03:28+08:00
- currentOverallResult: LOCAL_INTERFACE_PASS_NO_PHONE_REQUIRED
- userNeedsPhoneThisRound: false

## Current Minimum Capability

- SelfExpressionOpportunity exists: true
- NextMoveType.ARC_REVEAL exists: true
- ReplyRouteType.ARC_REVEAL exists: true
- planning / reality / stability / future can trigger ARC_REVEAL: true
- past experience / responsibility can trigger ARC_REVEAL: true
- cloud routeFamily=ARC_REVEAL can map correctly: true
- no light listening rewrite: true
- no parser rewrite: true
- no session state machine rewrite: true
- no auto send: true
- no raw private chat in GitHub: true

## Result Panel Reserved Fields

`ReplyRoute` exposes lightweight display fields and the floating result panel now renders them:

- panelNextAction
  - 接住她
  - 表达我
  - 让她看见你
  - 共创
  - 撤退

- panelPersonaFacet
  - describes which side of the user this line reveals
  - for ARC_REVEAL: true, authentic, contrast-bearing, able-to-follow-through side

- panelRouteLabel
  - ARC_REVEAL route label: 人物弧光

Floating panel behavior:

- If routeFamily=ARC_REVEAL, route label shows: 人物弧光
- It shows: 本轮动作：让她看见你
- It shows: 这句话展示了你的哪一面
- It shows: 不要说过头
- If the route set contains ARC_REVEAL, panel top shows: 本轮动作：让她看见你
- The panel structure was not rewritten.

## Character Arc Intent

The purpose is not to create a fake persona. The purpose is to let the chat partner gradually see a real, more dimensional user:

- surface impression
- hidden depth
- contrast tension
- safe reveal timing
- safe reveal line
- overdo risk

## Trigger Boundary

ARC_REVEAL is allowed when:

- lastSpeaker = OTHER
- topic touches planning / reality / stability / future / past experience / responsibility
- the reveal is short and grounded

ARC_REVEAL is not allowed to:

- override LAST ME wait behavior
- trigger auto-send
- invent fake identity
- become long self-proof
- steal the other person's main topic

## Tests

Executed:

- ./gradlew.bat :app:testDebugUnitTest --tests "com.huiyi.v4.CharacterArcRevealTest" --tests "com.huiyi.v4.LightChatStateStoreTest"

Result:

- PASS

Covered:

- CharacterArcCard fields exposed through SelfExpressionOpportunity
- NextMoveType.ARC_REVEAL exposed
- ReplyRouteType.ARC_REVEAL exposed
- planning / reality / stability / future topic triggers local ARC_REVEAL route
- cloud routeFamily=ARC_REVEAL maps correctly
- result panel visible text exists for ARC_REVEAL
- MockChat-style visual bubble fixture exposes ARC_REVEAL panel fields

## Safety

- longTermRawChatStorage: false
- autoSend: false
- rawPrivateChatUploadedToGithub: false
- rawPrivateChatInReport: false
- lightListeningRewrite: false
- parserRewrite: false
- sessionStateMachineRewrite: false

## Result

Self-expression engine minimum capability is ready as a small interface hook. The result panel now has visible ARC_REVEAL copy for the immediate product experience without rewriting Light Listening or session flow.
