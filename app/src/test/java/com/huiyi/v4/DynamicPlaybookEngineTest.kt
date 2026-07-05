package com.huiyi.v4

import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.CloudPlaybookRefresher
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.PlaybookRefreshScheduler
import com.huiyi.v4.domain.playbook.RelationshipPlaybookSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicPlaybookEngineTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun LastMeNextSentenceReturnsWaitWithoutRoutesOrCloudRefreshTest() {
        val engine = DynamicPlaybookEngine()
        val result = engine.nextSentence(
            request(
                mode = DynamicPlaybookMode.NEXT_SENTENCE,
                messages = listOf(
                    textNode("other-1", Speaker.OTHER, "I will be busy later.", 1),
                    textNode("me-1", Speaker.ME, "Okay, take care of your work first.", 2)
                )
            )
        )

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecisionType)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.cloudRefreshRecommended)
        assertEquals("LOCAL_WAIT", result.decisionSource)
        assertTrue(result.oneClickImmediateResultPass)
    }

    @Test
    fun LastOtherNextSentenceUsesPassiveCacheAndStaysCleanTest() {
        val engine = DynamicPlaybookEngine()
        val req = request(
            mode = DynamicPlaybookMode.NEXT_SENTENCE,
            messages = planningMessages()
        )

        val first = engine.nextSentence(req)
        val second = engine.nextSentence(req)

        assertEquals(Speaker.OTHER, first.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.NORMAL_REPLY, first.tacticalDecisionType)
        assertEquals(5, first.routes.size)
        assertTrue(first.localFallbackUsed)
        assertTrue(second.cacheHit)
        assertEquals(5, second.routes.size)
        assertTrue(second.routes.none { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(second.routes.none { it.routeType == ReplyRouteType.SELF_STORY })
        assertChineseSendableRoutes(second.routes)
        assertTrue(second.oneClickImmediateResultPass)
    }

    @Test
    fun ExpressSelfUsesActiveExpressionCacheAndArcRevealForPlanningTest() {
        val engine = DynamicPlaybookEngine()
        val req = request(
            mode = DynamicPlaybookMode.EXPRESS_SELF,
            messages = planningMessages()
        )

        val first = engine.expressSelf(req)
        val second = engine.expressSelf(req)

        assertTrue(first.routes.size in 3..5)
        assertTrue(first.routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(first.routes.any { it.routeType == ReplyRouteType.SELF_STORY })
        assertTrue(first.routes.any { it.routeType == ReplyRouteType.CO_CREATION })
        assertTrue(first.routes.any { it.routeType == ReplyRouteType.COOL_DOWN })
        assertChineseSendableRoutes(first.routes)
        assertTrue(first.cloudRefreshRecommended)
        assertTrue(second.cacheHit)
        assertTrue(second.oneClickImmediateResultPass)
    }

    @Test
    fun CloudRefreshIsOptionalAndStaleChatResultIsDiscardedTest() = runBlocking {
        val engine = DynamicPlaybookEngine()
        val req = request(
            mode = DynamicPlaybookMode.EXPRESS_SELF,
            messages = planningMessages()
        )
        val scheduler = PlaybookRefreshScheduler(
            engine = engine,
            cloudRefresher = CloudPlaybookRefresher { _, local ->
                Result.success(local.copy(chatKey = "com.bajiao.im.liaoqi|another-chat", source = RelationshipPlaybookSource.CLOUD_ENHANCED))
            }
        )

        val outcome = scheduler.refreshNow(req) { "com.bajiao.im.liaoqi|demo-chat" }

        assertTrue(outcome.localFallbackReady)
        assertTrue(outcome.cloudAttempted)
        assertTrue(outcome.cloudSuccess)
        assertTrue(outcome.staleRefreshDiscarded)
        assertFalse(outcome.cacheReplaced)
        assertEquals("CHAT_KEY_CHANGED", outcome.discardedReason)
    }

    @Test
    fun CloudRefreshSuccessUpdatesCacheAndNextClickReadsCloudEnhancedPlaybookTest() = runBlocking {
        val engine = DynamicPlaybookEngine()
        val req = request(
            mode = DynamicPlaybookMode.NEXT_SENTENCE,
            messages = planningMessages()
        )
        val initial = engine.nextSentence(req)
        val cloudRoute = initial.routes.first().copy(
            id = "cloud-passive-1",
            name = "云端接话",
            message = "我懂你的意思，我们先按舒服的节奏慢慢来。"
        )
        val scheduler = PlaybookRefreshScheduler(
            engine = engine,
            cloudRefresher = CloudPlaybookRefresher { _, local ->
                Result.success(
                    local.copy(
                        passiveNext = listOf(cloudRoute) + local.passiveNext.drop(1),
                        source = RelationshipPlaybookSource.CLOUD_ENHANCED
                    )
                )
            }
        )

        val outcome = scheduler.refreshNow(req) { "com.bajiao.im.liaoqi|demo-chat" }
        val afterRefresh = engine.nextSentence(req)

        assertTrue(outcome.localFallbackReady)
        assertTrue(outcome.cloudAttempted)
        assertTrue(outcome.cloudSuccess)
        assertTrue(outcome.cacheReplaced)
        assertFalse(outcome.staleRefreshDiscarded)
        assertEquals("CLOUD_ENHANCED_PLAYBOOK", afterRefresh.decisionSource)
        assertEquals("我懂你的意思，我们先按舒服的节奏慢慢来。", afterRefresh.routes.first().message)
    }

    private fun request(
        mode: DynamicPlaybookMode,
        messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) = DynamicPlaybookRequest(
        mode = mode,
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "demo-chat",
        messages = messages,
        personaCorpus = persona,
        capturedAt = 1000L,
        chatWindowHash = "demo-hash"
    )

    private fun planningMessages() = listOf(
        textNode("me-1", Speaker.ME, "I hear you.", 1),
        textNode("other-1", Speaker.OTHER, "This needs real planning, future stability and responsibility.", 2)
    )

    private fun assertChineseSendableRoutes(routes: List<com.huiyi.v4.domain.model.ReplyRoute>) {
        assertTrue(routes.isNotEmpty())
        routes.forEach { route ->
            assertTrue("route name should be Chinese: ${route.name}", route.name.contains(Regex("[\\u4e00-\\u9fff]")))
            assertTrue("route message should be Chinese: ${route.message}", route.message.contains(Regex("[\\u4e00-\\u9fff]")))
            assertFalse("route message should not expose English template: ${route.message}", route.message.contains(Regex("[A-Za-z]{3,}")))
        }
    }
}
