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
import com.huiyi.v4.domain.playbook.CloudModelTrace
import com.huiyi.v4.domain.playbook.CloudRelationshipPlaybookMapper
import com.huiyi.v4.domain.playbook.CloudRequestPurpose
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
import com.huiyi.v4.domain.playbook.RelationshipPlaybookSource
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
            ModelRouteTarget.GPT_STRONG,
            router.route(ModelRouterInput(lastSpeaker = Speaker.OTHER, risk = RiskLevel.LOW, validatorPassed = false)).target
        )
    }

    @Test
    fun ModelTraceRecordsActualModelTest() {
        val decision = ModelRouter().route(
            ModelRouterInput(
                lastSpeaker = Speaker.OTHER,
                risk = RiskLevel.LOW,
                requestPurpose = CloudRequestPurpose.ARC_REVEAL,
                configuredStrongModel = "gpt-5.4"
            )
        )

        val trace = CloudModelTrace.fromDecision(
            decision = decision,
            requestedModel = "deepseek-v4-flash",
            requestPurpose = CloudRequestPurpose.ARC_REVEAL
        ).withValidation("PASS", cacheWriteAllowed = true)

        assertEquals("deepseek-v4-flash", trace.requestedModel)
        assertEquals("gpt-5.4", trace.selectedModel)
        assertEquals("gpt-5.4", trace.actualModel)
        assertEquals(ModelRouteTarget.GPT_STRONG, trace.routeTarget)
        assertEquals(CloudRequestPurpose.ARC_REVEAL, trace.requestPurpose)
        assertEquals("PASS", trace.cloudContractValidationResult)
        assertTrue(trace.playbookCacheWriteAllowed)
    }

    @Test
    fun ExpressSelfDoesNotDefaultToDsFlashWhenArcRevealRequiredTest() {
        val decision = ModelRouter().route(
            ModelRouterInput(
                lastSpeaker = Speaker.OTHER,
                risk = RiskLevel.LOW,
                requestPurpose = CloudRequestPurpose.ARC_REVEAL,
                configuredStrongModel = "gpt-5.4"
            )
        )

        assertEquals(ModelRouteTarget.GPT_STRONG, decision.target)
        assertEquals("gpt-5.4", decision.model)
        assertFalse(decision.model == "deepseek-v4-flash")
    }

    @Test
    fun DsProDisabledInRuntimeTest() {
        val decision = ModelRouter().route(
            ModelRouterInput(
                lastSpeaker = Speaker.OTHER,
                risk = RiskLevel.LOW,
                validatorPassed = false,
                configuredStrongModel = "gpt-5.4"
            )
        )

        assertEquals(ModelRouteTarget.GPT_STRONG, decision.target)
        assertEquals("gpt-5.4", decision.model)
        assertFalse(decision.model == "deepseek-v4-pro")
    }

    @Test
    fun PassivePlaybookMayUseDsFlashCheapDraftTest() {
        val decision = ModelRouter().route(
            ModelRouterInput(
                lastSpeaker = Speaker.OTHER,
                risk = RiskLevel.LOW,
                requestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK
            )
        )

        assertEquals(ModelRouteTarget.DS_FLASH_PLAYBOOK, decision.target)
        assertEquals("deepseek-v4-flash", decision.model)
    }

    @Test
    fun ArcRevealUsesStrongOrConfiguredModelTest() {
        val decision = ModelRouter().route(
            ModelRouterInput(
                lastSpeaker = Speaker.OTHER,
                risk = RiskLevel.LOW,
                requestPurpose = CloudRequestPurpose.ARC_REVEAL,
                configuredStrongModel = "gpt-5.4"
            )
        )

        assertEquals(ModelRouteTarget.GPT_STRONG, decision.target)
        assertEquals("gpt-5.4", decision.model)
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
                model = DeepSeekPlaybookModel.V4_FLASH.modelId
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
    fun CloudRelationshipPlaybookMapperParsesOpenAiResponseIntoCloudEnhancedPlaybookTest() {
        val persona = DefaultPersonaCorpus.soldier()
        val snapshot = planningSnapshot(lastSpeaker = Speaker.OTHER)
        val compression = ConversationStateCompressor().compress(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = listOf("planning", "future"),
            personaCorpus = persona
        )
        val arcProgress = CharacterArcPlanner().plan(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = compression.currentTopics,
            personaCorpus = persona
        )
        val local = RelationshipPlaybookGenerator().generate(snapshot, compression, arcProgress, personaCorpus = persona, nowMillis = 1000L)
        val cloudJson = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\"stage\":\"TRUST_BUILDING\",\"currentFrame\":\"planning / future\",\"passiveNext\":[{\"slot\":\"接住现实感\",\"routeFamily\":\"EMPATHY\",\"message\":\"我懂，你不是随便说说，是想把这事真的想清楚。\",\"why\":\"接住对方的现实感\",\"riskLevel\":\"LOW\",\"fallbackMove\":\"先顺着聊\"},{\"slot\":\"稳住节奏\",\"routeFamily\":\"STABLE\",\"message\":\"那我们就按舒服的节奏来，不急着一下子定死。\",\"why\":\"降低压力\",\"riskLevel\":\"LOW\",\"fallbackMove\":\"转轻一点\"},{\"slot\":\"轻问一句\",\"routeFamily\":\"DIRECT\",\"message\":\"你现在更在意的是规划本身，还是怕走到后面不稳定？\",\"why\":\"轻问关键点\",\"riskLevel\":\"LOW\",\"fallbackMove\":\"不追问\"}],\"activeExpression\":[{\"slot\":\"低压表达\",\"routeFamily\":\"EXPRESS_SELF\",\"message\":\"我也挺认同这个。对我来说，很多事不是嘴上说满就行，能一步一步走稳更重要。\",\"why\":\"表达底色\",\"riskLevel\":\"LOW\",\"fallbackMove\":\"说短一点\"},{\"slot\":\"人物弧光\",\"routeFamily\":\"ARC_REVEAL\",\"message\":\"我可能不太会把话说得很漂亮，但真到要负责的时候，我会更愿意把事落到实处。\",\"why\":\"让对方看见稳定反差\",\"riskLevel\":\"MEDIUM\",\"fallbackMove\":\"露一点就收\"},{\"slot\":\"共创升维\",\"routeFamily\":\"CO_CREATE\",\"message\":\"那我们也可以先找一个彼此都舒服、也能长期走下去的节奏。\",\"why\":\"形成共同节奏\",\"riskLevel\":\"LOW\",\"fallbackMove\":\"回到轻松话题\"}],\"characterArcPlan\":{\"exists\":true,\"nextMoveType\":\"ARC_REVEAL\",\"suggestedFacet\":\"稳定但不冷\",\"suggestedLine\":\"我更愿意把事落到实处。\",\"overdoRisk\":\"别讲成自我证明\",\"triggerTopics\":[\"planning\",\"future\"]},\"next2StepBranches\":[],\"risk\":\"LOW\",\"fallback\":\"如果她没接住，就先降压收口。\",\"expiresWhen\":[\"topic_changed\",\"playbook_ttl_expired\"]}"
                  }
                }
              ]
            }
        """.trimIndent()

        val mapped = CloudRelationshipPlaybookMapper().parseResponse(cloudJson, local, nowMillis = 2000L)

        assertEquals(RelationshipPlaybookSource.CLOUD_ENHANCED, mapped.source)
        assertEquals(3, mapped.passiveNext.size)
        assertTrue(mapped.passiveNext.all { it.message.contains(Regex("[\\u4e00-\\u9fff]")) })
        assertTrue(mapped.activeExpression.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(mapped.activeExpression.any { it.routeType == ReplyRouteType.CO_CREATION })
    }

    @Test
    fun ModelBenchmarkUsesArcAndSyntheticSamplesAndDisablesDeepSeekFlashForArcRuntimeTest() {
        val report = ModelBenchmark().run(
            cliLatencyOverrides = mapOf(
                BenchmarkCandidateModel.DS_V4_FLASH to 9236,
                BenchmarkCandidateModel.DS_V4_PRO to 27836,
                BenchmarkCandidateModel.GPT_5_4 to 15127
            )
        )

        assertEquals(260, report.sampleCount)
        assertEquals(60, report.characterArcSampleCount)
        assertEquals(200, report.syntheticSampleCount)
        assertEquals("gpt-5.4", report.recommendedDefaultModel)
        assertEquals("gpt-5.5", report.recommendedStrongModel)
        assertEquals(20, report.modelMetrics.getValue(BenchmarkCandidateModel.DS_V4_FLASH).contractPassRate)
        assertEquals(0, report.modelMetrics.getValue(BenchmarkCandidateModel.DS_V4_FLASH).arcRevealHitRate)
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
