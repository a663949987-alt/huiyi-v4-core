package com.huiyi.v4

import com.huiyi.v4.domain.context.ArcRevealDepth
import com.huiyi.v4.domain.context.CharacterArcPlanner
import com.huiyi.v4.domain.context.LightChatMessageSummary
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.CharacterArcCard
import com.huiyi.v4.domain.model.IdentityCard
import com.huiyi.v4.domain.model.MessageDeliveryStatus
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.floating.FloatingPanelSplitPolicy
import com.huiyi.v4.runtime.FloatingPanelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterArcPlannerTest {
    @Test
    fun PlannerSuggestsUnseenArcCardForRealityPlanningWindowTest() {
        val persona = personaCorpus()
        val lastOther = lightMessage(
            id = "other-2",
            speaker = Speaker.OTHER,
            text = "Future and responsibility matter to me.",
            sequence = 4
        )

        val state = CharacterArcPlanner().plan(
            recentMessages = listOf(
                lightMessage("me-1", Speaker.ME, "I hear you.", 1),
                lightMessage("other-1", Speaker.OTHER, "I want something stable.", 2),
                lightMessage("me-2", Speaker.ME, "That makes sense.", 3),
                lastOther
            ),
            lastUserMessage = lightMessage("me-2", Speaker.ME, "That makes sense.", 3),
            lastOtherMessage = lastOther,
            currentTopics = listOf("future", "responsibility"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )

        assertTrue(state.currentExpressionWindow.exists)
        assertEquals(NextMoveType.ARC_REVEAL, state.currentExpressionWindow.nextMoveType)
        assertEquals(persona.characterArcCards.first(), state.suggestedArcCard)
        assertEquals(ArcRevealDepth.MEDIUM, state.suggestedDepth)
        assertTrue("responsibility-core" in state.unseenPersonaFacets)
        assertEquals("Do not turn this into a resume speech.", state.overdoRisk)
    }

    @Test
    fun RecentlyExpressedFacetLowersDepthTest() {
        val persona = personaCorpus()
        val lastUser = lightMessage(
            id = "me-2",
            speaker = Speaker.ME,
            text = "I care about responsibility and discipline.",
            sequence = 3
        )
        val lastOther = lightMessage(
            id = "other-2",
            speaker = Speaker.OTHER,
            text = "I do care about future planning.",
            sequence = 4
        )

        val state = CharacterArcPlanner().plan(
            recentMessages = listOf(
                lightMessage("other-1", Speaker.OTHER, "I want stable energy.", 1),
                lightMessage("me-1", Speaker.ME, "I understand.", 2),
                lastUser,
                lastOther
            ),
            lastUserMessage = lastUser,
            lastOtherMessage = lastOther,
            currentTopics = listOf("future"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )

        assertTrue(state.currentExpressionWindow.exists)
        assertTrue("responsibility-core" in state.recentlyExpressedFacets)
        assertEquals(ArcRevealDepth.LOW, state.suggestedDepth)
    }

    @Test
    fun LastUserAfterLastOtherClosesExpressionWindowTest() {
        val persona = personaCorpus()
        val lastOther = lightMessage("other-1", Speaker.OTHER, "Future feels important.", 1)
        val lastUser = lightMessage("me-1", Speaker.ME, "I get it, let us go slowly.", 2)

        val state = CharacterArcPlanner().plan(
            recentMessages = listOf(lastOther, lastUser),
            lastUserMessage = lastUser,
            lastOtherMessage = lastOther,
            currentTopics = listOf("future"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )

        assertFalse(state.currentExpressionWindow.exists)
        assertEquals("last_user_message_after_last_other", state.currentExpressionWindow.reason)
        assertEquals(ArcRevealDepth.LOW, state.suggestedDepth)
        assertEquals(null, state.suggestedArcCard)
    }

    @Test
    fun CompressorKeepsOnlyRecentLightMessagesAndDoesNotExposeOldRawChatTest() {
        val persona = personaCorpus(
            extraIdentityCard = IdentityCard(
                id = "old-secret-facet",
                title = "Old secret facet",
                summary = "Only old chat mentions this",
                values = listOf("secret-old-raw-chat"),
                bestFor = emptyList(),
                avoidWhen = emptyList(),
                risk = "Do not expose"
            )
        )
        val messages = (1..14).map { index ->
            val text = if (index == 1) "secret-old-raw-chat" else "neutral recent message $index"
            lightMessage("m$index", if (index % 2 == 0) Speaker.ME else Speaker.OTHER, text, index.toLong())
        }
        val lastOther = messages.last { it.speaker == Speaker.OTHER }

        val state = CharacterArcPlanner().plan(
            recentMessages = messages,
            lastUserMessage = messages.last { it.speaker == Speaker.ME },
            lastOtherMessage = lastOther,
            currentTopics = listOf("future"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )

        assertFalse("old-secret-facet" in state.seenPersonaFacets)
        assertFalse(state.toString().contains("secret-old-raw-chat"))
    }

    @Test
    fun PlannerDoesNotChangePassiveNextSentencePanelPolicyTest() {
        val persona = personaCorpus()
        val lastOther = lightMessage("other-1", Speaker.OTHER, "I think about stability.", 1)

        val state = CharacterArcPlanner().plan(
            recentMessages = listOf(lastOther),
            lastUserMessage = null,
            lastOtherMessage = lastOther,
            currentTopics = listOf("stability"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )

        assertNotNull(state.suggestedArcCard)
        assertFalse(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.NEXT_SENTENCE))
        assertFalse(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.NEXT_SENTENCE))
        assertFalse(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.EXPRESS_SELF))
    }

    private fun lightMessage(
        id: String,
        speaker: Speaker,
        text: String,
        sequence: Long
    ): LightChatMessageSummary = LightChatMessageSummary(
        id = id,
        speaker = speaker,
        text = text,
        contentType = "Text",
        source = "test",
        localSequence = sequence,
        createdAt = sequence,
        messageStatus = MessageDeliveryStatus.NONE
    )

    private fun personaCorpus(extraIdentityCard: IdentityCard? = null): UserPersonaCorpus {
        val identityCards = listOf(
            IdentityCard(
                id = "responsibility-core",
                title = "Responsibility",
                summary = "steady responsibility",
                values = listOf("responsibility", "discipline", "stable"),
                bestFor = listOf("future", "planning"),
                avoidWhen = emptyList(),
                risk = "May sound heavy"
            )
        ) + listOfNotNull(extraIdentityCard)
        return UserPersonaCorpus(
            id = "test-persona",
            name = "Test persona",
            enabled = true,
            identityCards = identityCards,
            storyCards = emptyList(),
            styleRules = emptyList(),
            riskRules = emptyList(),
            characterArcCards = listOf(
                CharacterArcCard(
                    surfaceImpression = "Quiet",
                    hiddenDepth = "Steady responsibility",
                    contrastTension = "Quiet outside, reliable inside",
                    revealTrigger = "future responsibility",
                    safeRevealLine = "I may not talk big, but I take steady things seriously.",
                    overdoRisk = "Do not turn this into a resume speech.",
                    relatedPersonaCardIds = listOf("responsibility-core")
                )
            )
        )
    }
}
