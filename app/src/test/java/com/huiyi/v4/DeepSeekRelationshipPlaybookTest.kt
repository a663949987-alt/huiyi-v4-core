package com.huiyi.v4

import com.huiyi.v4.domain.context.CharacterArcPlanner
import com.huiyi.v4.domain.context.ConversationStateCompressor
import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.BenchmarkCandidateModel
import com.huiyi.v4.domain.playbook.DeepSeekPlaybookModel
import com.huiyi.v4.domain.playbook.DeepSeekProvider
import com.huiyi.v4.domain.playbook.DeepSeekProviderConfig
import com.huiyi.v4.domain.playbook.ModelBenchmark
import com.huiyi.v4.domain.playbook.ModelRouteTarget
import com.huiyi.v4.domain.playbook.ModelRouter
import com.huiyi.v4.domain.playbook.ModelRouterInput
import com.huiyi.v4.domain.playbook.NormalizedConversationJson
import com.huiyi.v4.domain.playbook.PlaybookCache
import com.huiyi.v4.domain.playbook.PlaybookCacheKey
import com.huiyi.v4.domain.playbook.RelationshipPlaybookGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekRelationshipPlaybookTest {
    @Test
    fun RelationshipPlaybookGeneratorBuildsPassiveActiveAndArcRoutesTest() {
        val persona = DefaultPersonaCorpus.soldier()
        val snapshot = planningSnapshot(lastSpeaker = Speaker.OTHER)
        val compression = ConversationStateCompressor().compress(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = listOf("reality", "future", "stability", "responsibility"),
            personaCorpus = persona
        )
        val arcProgress = CharacterArcPlanner().plan(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = compression.currentTopics,
            personaCorpus = persona
        )

        val playbook = RelationshipPlaybookGenerator().generate(
            lightChatState = snapshot,
            compression = compression,
            arcProgressState = arcProgress,
            personaCorpus = persona,
            nowMillis = 1000L
        )

        assertEquals(5, playbook.passiveNext.size)
        assertTrue(playbook.activeExpression.size in 3..5)
        assertTrue(playbook.characterArcPlan.exists)
        assertEquals(NextMoveType.ARC_REVEAL, playbook.characterArcPlan.nextMoveType)
        assertTrue(playbook.activeExpression.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertEquals(listOf("stage_changed", "topic_changed", "last_speaker_changed", "playbook_ttl_expired"), playbook.expiresWhen.expiresWhen)
    }

    @Test
    fun ModelRouterFollowsSafetyAndCostRulesTest() {
        val router = ModelRouter()

        assertEquals(
            ModelRouteTarget.LOCAL_WAIT,
            router.route(ModelRouterInput(lastSpeaker = Speaker.ME, risk = RiskLevel.LOW)).target
        )
        assertEquals(
            ModelRouteTarget.LOCAL_CONTEXT_REQUIRED,
            router.route(ModelRouterInput(lastSpeaker = Speaker.UNKNOWN, risk = RiskLevel.LOW)).target
        )
        assertEquals(
            ModelRouteTarget.DS_FLASH_PLAYBOOK,
            router.route(ModelRouterInput(lastSpeaker = Speaker.OTHER, risk = RiskLevel.LOW)).target
        )
        assertEquals(
            "deepseek-v4-flash",
            router.route(ModelRouterInput(lastSpeaker = Speaker.OTHER, risk = RiskLevel.LOW)).model
        )
        assertEquals(
            ModelRouteTarget.GPT_STRONG,
            router.route(ModelRouterInput(lastSpeaker = Speaker.OTHER, risk = RiskLevel.HIGH)).target
        )
        assertEquals(
            ModelRouteTarget.DS_PRO,
            router.route(ModelRouterInput(lastSpeaker = Speaker.OTHER, risk = RiskLevel.LOW, validatorPassed = false)).target
        )
    }

    @Test
    fun PlaybookCacheServesPassiveAndActiveUntilExpiredTest() {
        val persona = DefaultPersonaCorpus.soldier()
        val snapshot = planningSnapshot(lastSpeaker = Speaker.OTHER)
        val compression = ConversationStateCompressor().compress(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = listOf("future"),
            personaCorpus = persona
        )
        val arcProgress = CharacterArcPlanner().plan(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = compression.currentTopics,
            personaCorpus = persona
        )
        val playbook = RelationshipPlaybookGenerator().generate(snapshot, compression, arcProgress, personaCorpus = persona, nowMillis = 1000L)
        val key = PlaybookCacheKey(chatKey = "liaoqi:demo", stage = playbook.stage, topicHash = "future")
        val cache = PlaybookCache()

        cache.put(key, playbook)

        assertEquals(5, cache.passiveNext(key, nowMillis = 2000L).size)
        assertTrue(cache.activeExpression(key, nowMillis = 2000L).isNotEmpty())
        assertFalse(cache.needsRefresh(key, stage = playbook.stage, topicHash = "future", nowMillis = 2000L))
        assertTrue(cache.needsRefresh(key, stage = playbook.stage, topicHash = "future", nowMillis = 1000L + 10 * 60 * 1000))
    }

    @Test
    fun DeepSeekProviderUsesOpenAiCompatibleTextOnlyRequestTest() {
        val provider = DeepSeekProvider(
            DeepSeekProviderConfig(
                baseUrl = "https://toapis.com/v1",
                apiKey = "runtime-only",
                model = DeepSeekPlaybookModel.V4_FLASH
            )
        )

        val body = provider.buildRequestBody(
            NormalizedConversationJson("""{"messages":[{"speaker":"OTHER","text":"future matters"}]}""")
        )

        assertTrue(body.contains("deepseek-v4-flash"))
        assertTrue(body.contains("chat") || body.contains("messages"))
        assertFalse(body.contains("image_url"))
        assertFalse(body.contains("imageBase64"))

        val imageResult = provider.generate(NormalizedConversationJson("""{"imageBase64":"abc"}"""))
        assertTrue(imageResult.isFailure)
    }

    @Test
    fun ModelBenchmarkUsesArcAndSyntheticSamplesAndRecommendsDeepSeekFlashTest() {
        val report = ModelBenchmark().run(
            cliLatencyOverrides = mapOf(
                BenchmarkCandidateModel.DS_V4_FLASH to 4200,
                BenchmarkCandidateModel.DS_V4_PRO to 17707,
                BenchmarkCandidateModel.GPT_5_4 to 10094
            )
        )

        assertEquals(260, report.sampleCount)
        assertEquals(60, report.characterArcSampleCount)
        assertEquals(200, report.syntheticSampleCount)
        assertEquals("deepseek-v4-flash", report.recommendedDefaultModel)
        assertEquals("gpt-5.5", report.recommendedStrongModel)
        assertTrue(report.modelMetrics.getValue(BenchmarkCandidateModel.DS_V4_FLASH).contractPassRate >= 90)
        assertTrue(report.modelMetrics.getValue(BenchmarkCandidateModel.GPT_5_4).sendabilityPassRate > report.modelMetrics.getValue(BenchmarkCandidateModel.DS_V4_FLASH).sendabilityPassRate)
        assertTrue(ModelBenchmark().json(report, "2026-07-05T00:00:00+08:00").contains("estimatedCostPer1000Conversations"))
    }

    private fun planningSnapshot(lastSpeaker: Speaker) = LightChatStateStore().buildStableSnapshot(
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "relationship planning",
        messages = if (lastSpeaker == Speaker.ME) {
            listOf(
                textNode("other-1", Speaker.OTHER, "I care about future and stability.", 1),
                textNode("me-1", Speaker.ME, "I hear you. I also want a steady pace.", 2)
            )
        } else {
            listOf(
                textNode("me-1", Speaker.ME, "I hear you.", 1),
                textNode("other-1", Speaker.OTHER, "I care about future and stability, not just words.", 2)
            )
        },
        characterArcCards = DefaultPersonaCorpus.soldier().characterArcCards
    )
}
