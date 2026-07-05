package com.huiyi.v4

import com.huiyi.v4.domain.context.CharacterArcPlanner
import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.panel.RoutePanelDisplayText
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import com.huiyi.v4.floating.FloatingPanelSplitPolicy
import com.huiyi.v4.runtime.FloatingPanelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressSelfUiLoopTest {
    @Test
    fun FloatingMenuHasNextSentenceAndExpressSelfButtonsTest() {
        assertEquals(
            listOf(
                "\u4e0b\u4e00\u53e5",
                "\u8868\u8fbe\u6211",
                "\u8fd9\u6b21\u4e0d\u5bf9\uff0c\u53d1\u7ed9 GPT",
                "\u9690\u85cf"
            ),
            FloatingPanelSplitPolicy.mainMenuLabels
        )
    }

    @Test
    fun NextSentencePanelStaysPassiveAndCleanTest() {
        assertFalse(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.NEXT_SENTENCE))
        assertFalse(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.NEXT_SENTENCE))
        assertTrue(FloatingPanelSplitPolicy.showsExpressSelfEntry(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun ExpressSelfPlannerBuildsArcRevealPanelForRealityPlanningFixtureTest() {
        val persona = DefaultPersonaCorpus.soldier()
        val messages = listOf(
            textNode("me-1", Speaker.ME, "I am listening.", 1),
            textNode("other-1", Speaker.OTHER, "I care about stable reality, future and responsibility.", 2)
        )
        val snapshot = LightChatStateStore().buildStableSnapshot(
            appPackage = "com.huiyi.mockchat",
            windowTitle = "mock-last-other",
            messages = messages,
            characterArcCards = persona.characterArcCards
        )
        val arcProgress = CharacterArcPlanner().plan(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = listOf("reality", "stable", "future", "responsibility"),
            personaCorpus = persona,
            characterArcCards = persona.characterArcCards
        )
        val context = ContextAssembler().assemble(messages, userPersonaCorpus = persona)
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)
        val summary = RoutePanelDisplayText.expressSelfSummaryLines(arcProgress, routes)

        assertTrue(arcProgress.currentExpressionWindow.exists)
        assertEquals(NextMoveType.ARC_REVEAL, arcProgress.currentExpressionWindow.nextMoveType)
        assertTrue(routes.size in 3..5)
        assertTrue(routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(summary.any { it.startsWith("\u672c\u8f6e\u52a8\u4f5c\uff1a") })
        assertTrue(summary.any { it.startsWith("\u5979\u7ed9\u7684\u7a97\u53e3\uff1a") })
        assertTrue(summary.any { it.startsWith("\u9002\u5408\u9732\u51fa\u7684\u4f60\uff1a") })
        assertTrue(summary.any { it.startsWith("\u5efa\u8bae\u53e5\uff1a") })
        assertTrue(summary.any { it.startsWith("\u522b\u8bf4\u8fc7\u5934\uff1a") })
    }
}
