package com.huiyi.v4

import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.panel.RoutePanelDisplayText
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.ExpressionLedger
import com.huiyi.v4.domain.playbook.ExpressionLedgerEntry
import com.huiyi.v4.domain.playbook.ExpressionLevel
import com.huiyi.v4.domain.playbook.ExpressionRepeatRisk
import com.huiyi.v4.domain.playbook.OtherReaction
import com.huiyi.v4.floating.FloatingPanelSplitPolicy
import com.huiyi.v4.runtime.FloatingPanelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealUsePackageAcceptanceTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun FloatingMenuContainsOnlyRealUseEntriesTest() {
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
    fun NextSentenceLastMeWaitsWithoutRoutesOrCloudTest() {
        val result = DynamicPlaybookEngine().nextSentence(
            request(
                messages = listOf(
                    textNode("other-1", Speaker.OTHER, "\u6211\u7b49\u4e0b\u8fd8\u8981\u5fd9\u4e00\u4e0b", 1),
                    textNode("me-1", Speaker.ME, "\u597d\uff0c\u4f60\u5148\u5fd9\uff0c\u6211\u5148\u4e0d\u6253\u6270\u4f60", 2)
                )
            )
        )

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecisionType)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.cloudRefreshRecommended)
        assertEquals("LOCAL_WAIT", result.decisionSource)
    }

    @Test
    fun NextSentenceLastOtherDoesNotExposeLocalPassiveRoutesTest() {
        val result = DynamicPlaybookEngine().nextSentence(
            request(
                messages = listOf(
                    textNode("me-1", Speaker.ME, "\u6211\u542c\u7740", 1),
                    textNode("other-1", Speaker.OTHER, "\u6211\u665a\u70b9\u8fd8\u8981\u53bb\u5904\u7406\u4e00\u4e0b\u5de5\u4f5c", 2)
                )
            )
        )

        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecisionType)
        assertTrue(result.routes.isEmpty())
        assertTrue(result.localPassiveRoutesGenerated)
        assertFalse(result.localPassiveRoutesShownToUser)
        assertTrue(result.passiveWaitPanelShown)
        assertEquals("PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK", result.decisionSource)
        assertTrue(result.routes.none { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertFalse(FloatingPanelSplitPolicy.showsPersonaFeedback(FloatingPanelMode.NEXT_SENTENCE))
        assertFalse(FloatingPanelSplitPolicy.showsCharacterArcDetails(FloatingPanelMode.NEXT_SENTENCE))
    }

    @Test
    fun ExpressSelfShowsLedgerFieldsAndChineseActiveRoutesTest() {
        val result = DynamicPlaybookEngine().expressSelf(
            request(
                messages = listOf(
                    textNode("me-1", Speaker.ME, "\u6211\u660e\u767d", 1),
                    textNode("other-1", Speaker.OTHER, "\u8fd9\u4e2a\u4e8b\u60c5\u4e5f\u9700\u8981\u8003\u8651\u597d\u89c4\u5212\u597d\u624d\u884c", 2)
                ),
                currentTopics = listOf("planning", "reality", "future")
            )
        )
        val summary = RoutePanelDisplayText.expressSelfSummaryLines(result.arcProgressState, result.routes)

        assertTrue(result.routes.size in 1..3)
        assertChineseRoutes(result.routes)
        assertTrue(result.expressSelfPanelSimpleMode)
        assertFalse(result.expressSelfFeedbackDefaultVisible)
        assertTrue(result.expressSelfFeedbackCollapsed)
        assertTrue(result.expressSelfDefaultRouteCount <= 3)
        assertTrue(summary.any { it.startsWith("\u8868\u8fbe\u6a21\u5f0f\uff1a") })
        assertTrue(summary.any { it.startsWith("\u5f53\u524d\u6bcd\u9898\uff1a") })
        assertTrue(summary.any { it.startsWith("\u4e3a\u4ec0\u4e48\u8fd9\u6b21\u53ef\u4ee5\u8bf4\uff1a") })
        assertTrue(summary.any { it.startsWith("\u8fd9\u6b21\u522b\u600e\u4e48\u8bf4\uff1a") })
        assertTrue(result.routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(result.routes.any { it.routeType == ReplyRouteType.SELF_STORY })
    }

    @Test
    fun CorpusSmokeHasNoEnglishTemplateLeakTest() {
        val scenarios = listOf(
            "last ME wait" to listOf(
                textNode("other-1", Speaker.OTHER, "\u6211\u5148\u5fd9\u4e00\u4e0b", 1),
                textNode("me-1", Speaker.ME, "\u597d\uff0c\u4f60\u5148\u5fd9", 2)
            ),
            "normal last OTHER" to listOf(textNode("other-1", Speaker.OTHER, "\u597d\uff0c\u6211\u8fc7\u4f1a\u4e5f\u53bb\u5403\u996d\u4e86", 1)),
            "planning stability" to listOf(textNode("other-1", Speaker.OTHER, "\u8fd9\u4e2a\u4e8b\u60c5\u9700\u8981\u597d\u597d\u89c4\u5212", 1)),
            "future" to listOf(textNode("other-1", Speaker.OTHER, "\u4ee5\u540e\u8fd8\u662f\u8981\u770b\u957f\u671f\u600e\u4e48\u8d70", 1)),
            "past experience" to listOf(textNode("other-1", Speaker.OTHER, "\u4eca\u5929\u4f60\u628a\u8fc7\u53bb\u7ecf\u5386\u5c55\u793a\u7ed9\u6211\u4e86", 1)),
            "responsibility" to listOf(textNode("other-1", Speaker.OTHER, "\u8001\u73ed\u957f\u90fd\u662f\u4e3a\u4f60\u597d\u554a", 1)),
            "expression difficulty" to listOf(textNode("other-1", Speaker.OTHER, "\u6211\u662f\u4e0d\u77e5\u9053\u600e\u4e48\u8868\u8fbe", 1)),
            "work pressure" to listOf(textNode("other-1", Speaker.OTHER, "\u6211\u53bb\u7ed9\u5ba2\u6237\u9001\u8863\u670d\u53bb\u4e86", 1)),
            "topic shift" to listOf(textNode("other-1", Speaker.OTHER, "\u5148\u4e0d\u8bf4\u8fd9\u4e2a\u4e86\uff0c\u6362\u4e2a\u8bdd\u9898", 1)),
            "cold chat start topic" to listOf(textNode("me-1", Speaker.ME, "\u6211\u521a\u5fd9\u5b8c", 1)),
            "repeated theme elevate" to listOf(textNode("other-1", Speaker.OTHER, "\u73b0\u5b9e\u548c\u89c4\u5212\u8fd8\u662f\u5f97\u518d\u770b\u770b", 1)),
            "over expression hold back" to listOf(
                textNode("me-1", Speaker.ME, "\u6211\u521a\u521a\u8868\u8fbe\u8fc7\u81ea\u5df1", 1),
                textNode("me-2", Speaker.ME, "\u6211\u8fd8\u60f3\u518d\u8868\u8fbe\u4e00\u4e0b", 2)
            )
        )
        val engine = DynamicPlaybookEngine()
        var englishLeakCount = 0
        var nextSentencePassCount = 0
        var expressSelfPassCount = 0
        var arcRevealScenarioPassCount = 0
        var holdBackScenarioPassCount = 0

        scenarios.forEach { (name, messages) ->
            val ledger = if (name == "over expression hold back") overExpressionLedger() else ExpressionLedger.empty()
            val next = engine.nextSentence(request(messages = messages, expressionLedger = ledger))
            val express = engine.expressSelf(request(messages = messages, expressionLedger = ledger))
            englishLeakCount += englishLeakCount(next.routes + express.routes)
            if (
                next.tacticalDecisionType == TacticalDecisionType.WAIT ||
                next.tacticalDecisionType == TacticalDecisionType.PASSIVE_NOT_READY ||
                next.routes.size in 3..5
            ) nextSentencePassCount += 1
            if (express.routes.size in 1..5 || express.expressSelfEligibility?.eligible == false) expressSelfPassCount += 1
            if (express.routes.any { it.routeType == ReplyRouteType.ARC_REVEAL }) arcRevealScenarioPassCount += 1
            if (express.nextMoveType.name == "WITHDRAW") holdBackScenarioPassCount += 1
        }

        assertEquals(12, scenarios.size)
        assertEquals(12, nextSentencePassCount)
        assertEquals(12, expressSelfPassCount)
        assertTrue(arcRevealScenarioPassCount >= 1)
        assertTrue(holdBackScenarioPassCount >= 1)
        assertEquals(0, englishLeakCount)
    }

    private fun request(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        currentTopics: List<String> = emptyList(),
        expressionLedger: ExpressionLedger = ExpressionLedger.empty()
    ) = DynamicPlaybookRequest(
        mode = DynamicPlaybookMode.NEXT_SENTENCE,
        appPackage = "com.huiyi.mockchat",
        windowTitle = "real-use-fixture",
        messages = messages,
        personaCorpus = persona,
        capturedAt = 1000L,
        currentTopics = currentTopics,
        expressionLedger = expressionLedger,
        chatWindowHash = "real-use-fixture"
    )

    private fun overExpressionLedger(): ExpressionLedger = ExpressionLedger(
        entries = listOf(
            ledgerEntry(lastUsedAt = 900L, lastExpressionLevel = ExpressionLevel.ATTITUDE),
            ledgerEntry(lastUsedAt = 950L, lastExpressionLevel = ExpressionLevel.ARC_REVEAL)
        )
    )

    private fun ledgerEntry(
        lastUsedAt: Long,
        lastExpressionLevel: ExpressionLevel
    ) = ExpressionLedgerEntry(
        themeId = "planning_not_promise",
        themeName = "\u73b0\u5b9e\u89c4\u5212\u4f46\u4e0d\u753b\u997c",
        lastUsedAt = lastUsedAt,
        lastExpressionLevel = lastExpressionLevel,
        lastIntensity = InfluenceIntensity.LOW,
        lastSurfaceLineRedacted = "\u6211\u66f4\u4e60\u60ef\u4e00\u6b65\u4e00\u6b65\u8d70\u7a33",
        otherReaction = OtherReaction.UNKNOWN,
        repeatRisk = ExpressionRepeatRisk.LOW,
        canReappear = true,
        suggestedNextLevel = ExpressionLevel.ARC_REVEAL
    )

    private fun assertChineseRoutes(routes: List<ReplyRoute>) {
        routes.forEach { route ->
            assertTrue(route.name.contains(Regex("[\\u4e00-\\u9fff]")))
            assertTrue(route.message.contains(Regex("[\\u4e00-\\u9fff]")))
        }
        assertEquals(0, englishLeakCount(routes))
    }

    private fun englishLeakCount(routes: List<ReplyRoute>): Int {
        val forbidden = listOf(
            "I get it",
            "no rush",
            "I am here",
            "handle what is in front of you"
        )
        return routes.sumOf { route ->
            forbidden.count { token ->
                route.name.contains(token, ignoreCase = true) ||
                    route.message.contains(token, ignoreCase = true)
            }
        }
    }
}
