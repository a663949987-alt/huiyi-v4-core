package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import com.huiyi.v4.floating.FloatingPanelSplitPolicy
import com.huiyi.v4.runtime.FloatingPanelMode
import com.huiyi.v4.runtime.NextSentencePendingCloudSessionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassiveActiveSplitTest {
    @Test
    fun NextSentencePanelDoesNotShowPersonaFeedbackTest() {
        assertFalse(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun NextSentencePanelDoesNotShowLikeMeUnlikeMeTest() {
        val forbidden = FloatingPanelSplitPolicy.personaFeedbackLabels
        assertTrue("像我" in forbidden)
        assertTrue("不像我" in forbidden)
        assertFalse(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun NextSentencePanelHasExpressSelfEntryTest() {
        assertTrue(FloatingPanelSplitPolicy.showsExpressSelfEntry(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun ExpressSelfPanelShowsCharacterArcTest() {
        assertTrue(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.EXPRESS_SELF))
        assertTrue("人物弧光" in FloatingPanelSplitPolicy.characterArcDetailLabels)
    }

    @Test
    fun ExpressSelfPanelShowsLikeMeFeedbackTest() {
        assertTrue(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.EXPRESS_SELF))
        assertTrue("像我" in FloatingPanelSplitPolicy.personaFeedbackLabels)
    }

    @Test
    fun PassiveNextSentenceLocalFallbackShowsImmediatelyTest() {
        val context = passiveLastOtherContext()
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        assertEquals(Speaker.OTHER, context.lastMessage?.speaker)
        assertEquals(5, routes.size)
        assertFalse(FloatingPanelSplitPolicy.blocksLocalRoutesWhileCloudPending(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun PassiveCloudTimeoutDoesNotBlockLocalRoutesTest() {
        val cloudTrace = CloudAnalysisTrace(
            cloudErrorCode = NextSentencePendingCloudSessionPolicy.SOFT_TIMEOUT_PENDING,
            decisionSource = "LOCAL_FALLBACK"
        )

        assertEquals("本地建议", FloatingPanelSplitPolicy.titleForNextSentence(cloudTrace))
        assertFalse(FloatingPanelSplitPolicy.blocksLocalRoutesWhileCloudPending(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun LastMeNextSentenceStillWaitsTest() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("other-1", Speaker.OTHER, "today was stressful", 1),
                textNode("me-1", Speaker.ME, "I hear you", 2),
                textNode("other-2", Speaker.OTHER, "I just need some space", 3),
                textNode("me-2", Speaker.ME, "ok, I will wait for you", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)

        assertEquals(Speaker.ME, context.lastMessage?.speaker)
        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
    }

    @Test
    fun ActiveExpressSelfCanUseArcRevealTest() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("me-1", Speaker.ME, "I am listening", 1),
                textNode("other-1", Speaker.OTHER, "I care about reality and planning", 2),
                textNode("me-2", Speaker.ME, "that makes sense", 3),
                textNode("other-2", Speaker.OTHER, "future and responsibility matter to me", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        assertTrue(routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.EXPRESS_SELF))
    }

    private fun passiveLastOtherContext() = ContextAssembler().assemble(
        listOf(
            textNode("me-1", Speaker.ME, "I am here", 1),
            textNode("other-1", Speaker.OTHER, "today was busy", 2),
            textNode("me-2", Speaker.ME, "take it slowly", 3),
            textNode("other-2", Speaker.OTHER, "ok, I just want a simple chat", 4)
        )
    )
}
