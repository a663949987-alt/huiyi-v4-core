# Character Arc Planner Report

## Basic Info
- project: Huiyi v4 Core
- taskName: character_arc_planner_state_compressor
- versionName: 4.1.56
- versionCode: 475
- generatedAt: 2026-07-04T22:36:13+08:00
- currentOverallResult: LOCAL_UNIT_PASS_NO_PHONE_REQUIRED
- userNeedsPhoneThisRound: false

## Scope
- Added ConversationStateCompressor.
- Added ArcProgressState.
- Added CharacterArcPlanner.
- Preserved existing LightChatStateStore.
- Did not rewrite parser.
- Did not rewrite session isolation.
- Did not rewrite cloud callback handling.
- Did not change passive Next Sentence behavior.

## Inputs Supported
- recent 6-12 LightMessage summaries.
- lastUserMessage.
- lastOtherMessage.
- currentTopics.
- SelfPersonaCorpus / UserPersonaCorpus.
- CharacterArcCards.

## Outputs Produced
- seenPersonaFacets.
- unseenPersonaFacets.
- recentlyExpressedFacets.
- currentExpressionWindow.
- suggestedArcCard.
- suggestedDepth: LOW / MEDIUM.
- overdoRisk.

## Runtime Wiring
- CharacterArcPlanner is wired only into the Express Self path.
- Passive Next Sentence does not call CharacterArcPlanner.
- If Express Self has a valid expression window and a suggested CharacterArcCard, the ARC_REVEAL route can use the card safeRevealLine.
- If no valid expression window exists, the existing local fallback remains available.

## Safety
- longTermRawChatStorage: false
- autoSend: false
- rawPrivateChatUploadedToGithub: false
- raw long private chat persisted by this module: false
- cloud callback changed: false
- parser changed: false
- LightChatStateStore rewritten: false

## Tests
- CharacterArcPlannerTest: PASS
- PassiveActiveSplitTest: PASS
- command: :app:testDebugUnitTest --tests com.huiyi.v4.CharacterArcPlannerTest --tests com.huiyi.v4.PassiveActiveSplitTest

## GPT Review Focus
- Check that the planner only consumes LightMessage summaries.
- Check that recently expressed persona facets lower suggestedDepth.
- Check that expression windows close when the last user message is newer than the last other message.
- Check that passive Next Sentence remains clean and does not show persona feedback.
