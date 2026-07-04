package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.CharacterArcCard
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterArcRevealTest {
    @Test
    fun CharacterArcCardFieldsAreExposedThroughSelfExpressionOpportunityTest() {
        val arcCard = CharacterArcCard(
            surfaceImpression = "看起来直接，不太会绕弯。",
            hiddenDepth = "其实很重视责任和长期稳定。",
            contrastTension = "表面硬，内里稳。",
            revealTrigger = "对方谈现实、规划、稳定、责任感、未来时。",
            safeRevealLine = "我可能表达不算花，但认真起来会把事一点点做到位。",
            overdoRisk = "说过头会变成自证或说教。",
            relatedPersonaCardIds = listOf("soldier", "transition")
        )

        val snapshot = LightChatStateStore().buildStableSnapshot(
            appPackage = "chat.app",
            windowTitle = "Alice",
            messages = listOf(
                textNode("me", Speaker.ME, "我在听", 1),
                textNode("other", Speaker.OTHER, "我还是更看重现实一点，以后的规划和责任感很重要。", 2)
            ),
            characterArcCards = listOf(arcCard)
        )

        assertTrue(snapshot.selfExpressionOpportunity.exists)
        assertEquals(NextMoveType.ARC_REVEAL, snapshot.selfExpressionOpportunity.type)
        assertEquals("ARC_REVEAL", snapshot.selfExpressionOpportunity.routeFamily)
        assertEquals(arcCard, snapshot.selfExpressionOpportunity.characterArcCard)
        assertEquals(listOf("soldier", "transition"), snapshot.selfExpressionOpportunity.matchedPersonaCardIds)
    }

    @Test
    fun LastOtherPlanningTopicAddsArcRevealRouteFamilyTest() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("me-1", Speaker.ME, "嗯，我在", 1),
                textNode("other-1", Speaker.OTHER, "我觉得现实一点也挺重要的", 2),
                textNode("me-2", Speaker.ME, "你说得对", 3),
                textNode("other-2", Speaker.OTHER, "以后还是要看规划和责任感吧。", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        assertEquals(5, routes.size)
        assertTrue(routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(routes.any { it.routeFamily == "ARC_REVEAL" })
        val arcRoute = routes.first { it.routeType == ReplyRouteType.ARC_REVEAL }
        assertEquals("让她看见你", arcRoute.panelNextAction)
        assertEquals("人物弧光", arcRoute.panelRouteLabel)
        assertTrue(arcRoute.panelPersonaFacet.orEmpty().contains("真实"))
    }

    @Test
    fun CloudRouteFamilyArcRevealMapsToArcRevealRouteTypeTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(
            arcRevealCloudResponse(),
            latencyMs = 10L,
            actualLastSpeaker = Speaker.OTHER
        )

        assertEquals(5, output.routes.size)
        assertEquals(ReplyRouteType.ARC_REVEAL, output.routes.first().routeType)
        assertEquals("ARC_REVEAL", output.routes.first().routeFamily)
    }

    private fun arcRevealCloudResponse(): String = """
        {
          "schemaVersion": 1,
          "visualLastSpeaker": "OTHER",
          "visualLastSpeakerConfidence": 92,
          "visualSpeakerEvidence": "right/left bubble visual evidence",
          "decisionType": "NORMAL_REPLY",
          "decisionTypeFamily": "REPLY_ROUTES",
          "situation": "other talks about future and responsibility",
          "coCreationPoint": {
            "exists": true,
            "type": "shared_future",
            "evidence": "future and responsibility topic",
            "meaning": "safe moment for a grounded self reveal"
          },
          "userLikelyMistake": "over explaining and proving himself",
          "bestMove": "reveal one light contrast, then return to her topic",
          "intensityPolicy": {
            "level": "MEDIUM",
            "reason": "light self reveal is allowed but should stay short"
          },
          "riskWarning": "do not overdo the self proof",
          "fallbackMove": "return to receiving her concern",
          "routes": [
            {
              "slot": "人物弧光",
              "routeFamily": "ARC_REVEAL",
              "message": "我可能表达不算花，但认真起来会把事一点点做到位。",
              "why": "shows authentic contrast without faking a persona",
              "riskLevel": "MEDIUM",
              "fallbackMove": "return to her topic if she does not pick it up"
            },
            {
              "slot": "接情绪",
              "routeFamily": "EMPATHY",
              "message": "嗯，你在意这个我能理解，现实感确实不是小事。",
              "why": "receives the concern first",
              "riskLevel": "LOW",
              "fallbackMove": "keep it simple"
            },
            {
              "slot": "共同推进",
              "routeFamily": "CO_CREATION",
              "message": "那我们可以慢慢把这些事聊具体一点，不急着下结论。",
              "why": "co-creates the next step",
              "riskLevel": "LOW",
              "fallbackMove": "slow down"
            },
            {
              "slot": "轻问一句",
              "routeFamily": "DIRECT",
              "message": "你说的稳定，最看重的是生活节奏，还是遇事的态度？",
              "why": "asks a concrete question",
              "riskLevel": "LOW",
              "fallbackMove": "receive answer"
            },
            {
              "slot": "降压撤退",
              "routeFamily": "COOL_DOWN",
              "message": "这个话题不用一下聊完，你慢慢说，我听着。",
              "why": "keeps pressure low",
              "riskLevel": "LOW",
              "fallbackMove": "wait"
            }
          ]
        }
    """.trimIndent()
}
