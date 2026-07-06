package com.huiyi.v4

import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.CloudRequestPurpose
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.HuiyiOutputQualityGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplyQualityGateTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun LegacyTemplatePhraseBlockedTest() {
        val result = HuiyiOutputQualityGate().assessRoute(
            route = route(
                message = "\u90a3\u4f60\u5e0c\u671b\u6211\u73b0\u5728\u600e\u4e48\u63a5\u4f60\uff0c\u4f1a\u66f4\u8212\u670d\u4e00\u70b9\uff1f",
                source = HuiyiOutputQualityGate.SOURCE_CLOUD_VERIFIED_PASSIVE_NEXT
            ),
            requestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK
        )

        assertFalse(result.pass)
        assertEquals("LEGACY_TEMPLATE_PHRASE", result.rejectReason)
    }

    @Test
    fun RouteSourceMissingBlockedTest() {
        val result = HuiyiOutputQualityGate().assessRoute(
            route = route(message = "\u6211\u5148\u63a5\u4f4f\u4f60\u8fd9\u53e5\uff0c\u4e0d\u6025\u7740\u63a8\u8fdb\u3002", source = ""),
            requestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK
        )

        assertFalse(result.pass)
        assertEquals("ROUTE_SOURCE_MISSING", result.rejectReason)
    }

    @Test
    fun RealPlanningTransferArmyNextSentenceWaitsWithoutCloudTest() {
        val result = DynamicPlaybookEngine().nextSentence(realPlanningRequest(DynamicPlaybookMode.NEXT_SENTENCE))

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecisionType)
        assertEquals(0, result.routes.size)
        assertTrue(result.localPassiveRoutesGenerated)
        assertFalse(result.localPassiveRoutesShownToUser)
        assertTrue(result.passiveWaitPanelShown)
        assertFalse(result.cloudPlaybookAvailable)
        assertEquals("NONE", result.passiveRouteDisplaySource)
    }

    @Test
    fun RealPlanningTransferArmyExpressSelfUsesHuiyiFamiliesTest() {
        val result = DynamicPlaybookEngine().expressSelf(realPlanningRequest(DynamicPlaybookMode.EXPRESS_SELF))
        val families = result.routes.map { it.routeType }.toSet()
        val messages = result.routes.joinToString("\n") { it.message }

        assertTrue(result.routes.size in 3..5)
        assertTrue(families.contains(ReplyRouteType.SELF_STORY))
        assertTrue(families.contains(ReplyRouteType.ARC_REVEAL))
        assertTrue(families.contains(ReplyRouteType.CO_CREATION))
        assertFalse(messages.contains("\u90a3\u4f60\u5e0c\u671b\u6211\u73b0\u5728\u600e\u4e48\u63a5\u4f60"))
        assertFalse(messages.contains("\u5148\u7ed9\u4f60\u8bb0\u4e00\u7b14\u8f9b\u82e6\u5206"))
        result.routes.forEach { route ->
            assertTrue(route.routeSource.isNotBlank())
            assertTrue(route.generatorName.isNotBlank())
            assertTrue(route.promptVersion.isNotBlank())
            assertTrue(route.playbookId.isNotBlank())
            assertTrue(route.cacheSource.isNotBlank())
            assertTrue(route.qualityGatePass)
            assertEquals("", route.qualityGateRejectReason)
        }
    }

    @Test
    fun CloudEnhancedPlaybookRequiresQualityGateTest() {
        val badCloudRoutes = listOf(
            route(
                message = "\u55ef\uff0c\u6211\u61c2\u4f60\u7684\u610f\u601d\u3002\u90a3\u4f60\u73b0\u5728\u662f\u66f4\u60f3\u5148\u4f11\u606f\uff0c\u8fd8\u662f\u7ee7\u7eed\u8bf4\u8bf4\uff1f",
                source = HuiyiOutputQualityGate.SOURCE_CLOUD_ENHANCED_PLAYBOOK
            )
        )
        val result = HuiyiOutputQualityGate().assessRouteSet(
            routes = badCloudRoutes,
            requestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK,
            sceneTags = listOf("planning", "reality")
        )

        assertFalse(result.pass)
        assertEquals("LEGACY_TEMPLATE_PHRASE", result.rejectReason)
    }

    private fun realPlanningRequest(mode: DynamicPlaybookMode): DynamicPlaybookRequest = DynamicPlaybookRequest(
        mode = mode,
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "REAL_PLANNING_TRANSFER_ARMY_001",
        messages = listOf(
            textNode("me-1", Speaker.ME, "\u55ef\u597d\u7684\u3002", 1),
            textNode("other-1", Speaker.OTHER, "\u4e5f\u662f\u8d70\u4e00\u6b65\u770b\u4e00\u6b65\u3002", 2),
            textNode("me-2", Speaker.ME, "\u73b0\u5728\u5927\u73af\u5883\u4e0d\u597d\u3002", 3),
            textNode(
                "other-2",
                Speaker.OTHER,
                "\u5bf9\u7684\uff0c\u8d70\u4e00\u6b65\u770b\u4e00\u6b65\u3002\u6211\u8001\u73ed\u957f\u8ddf\u6211\u8bf4\uff0c\u8ba9\u6211\u627e\u4e00\u4e2a\u4f53\u5236\u5185\u7684\u5de5\u4f5c\u3002" +
                    "\u56e0\u4e3a\u6211\u4eec\u8f6c\u4e1a\u8fd8\u4f1a\u7ed9\u4e00\u7b14\u94b1\u3002\u7136\u540e\u6211\u4eba\u4e00\u76f4\u5728\u90e8\u961f\u91cc\uff0c\u8fd9\u51e0\u5e74\u7684\u94b1\u4e5f\u6ca1\u600e\u4e48\u52a8\uff0c" +
                    "\u672c\u6765\u6253\u7b97\u4e00\u8d77\u7528\u6765\u521b\u4e1a\u7528\u7684\u3002\u8fd9\u4e2a\u4e8b\u60c5\u4e5f\u9700\u8981\u8003\u8651\u597d\u89c4\u5212\u597d\u624d\u884c\u3002",
                4
            )
        ),
        personaCorpus = persona,
        capturedAt = 1000L,
        currentTopics = listOf("planning", "reality", "stability", "future", "army", "transfer", "responsibility"),
        chatWindowHash = "REAL_PLANNING_TRANSFER_ARMY_001",
        targetAppSupported = true,
        parserConfidence = 92
    )

    private fun route(
        message: String,
        source: String,
        type: ReplyRouteType = ReplyRouteType.STABLE
    ): ReplyRoute = ReplyRoute(
        id = "test-route",
        name = "\u6d4b\u8bd5",
        routeType = type,
        tag = "test",
        message = message,
        intensity = InfluenceIntensity.LOW,
        riskLevel = RiskLevel.LOW,
        riskWarning = null,
        expectedEffect = null,
        fallbackMove = null,
        recommended = false,
        routeSource = source,
        generatorName = "test",
        promptVersion = "test-v1",
        cacheSource = "test"
    )
}
