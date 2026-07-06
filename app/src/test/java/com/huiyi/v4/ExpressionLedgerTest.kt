package com.huiyi.v4

import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.panel.RoutePanelDisplayText
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.ExpressionLedger
import com.huiyi.v4.domain.playbook.ExpressionLedgerEntry
import com.huiyi.v4.domain.playbook.ExpressionLevel
import com.huiyi.v4.domain.playbook.ExpressionMode
import com.huiyi.v4.domain.playbook.ExpressionRepeatRisk
import com.huiyi.v4.domain.playbook.FixedArcThemes
import com.huiyi.v4.domain.playbook.OtherReaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionLedgerTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun FixedArcThemesStaySmallAndNamedTest() {
        assertEquals(5, FixedArcThemes.all.size)
        assertEquals(
            listOf(
                "steady_not_cold",
                "serious_not_playboy",
                "experienced_not_miserable",
                "responsible_not_pressuring",
                "planning_not_promise"
            ),
            FixedArcThemes.all.map { it.themeId }
        )
    }

    @Test
    fun ColdStartExpressionCanStartTopicTest() {
        val result = DynamicPlaybookEngine().expressSelf(
            request(
                messages = listOf(textNode("me-1", Speaker.ME, "I have been quiet for a bit.", 1)),
                ledger = ExpressionLedger.empty(),
                capturedAt = 31 * 60 * 1000L,
                lastUserMessageAgeMsOverride = 31 * 60 * 1000L
            )
        )

        assertEquals(ExpressionMode.START_TOPIC, result.expressionModeSelection?.expressionMode)
        assertEquals("\u5f00\u573a", result.panelNextAction)
        assertTrue(
            result.routes.joinToString { "${it.id}:${it.name}:${it.routeType}:${it.qualityGateRejectReason}" },
            result.routes.any { it.routeType == ReplyRouteType.WARM_UP || it.id.contains("start-topic") }
        )
        assertTrue(result.routes.size in 3..5)
    }

    @Test
    fun RepeatedThemeBlocksLowLevelRepetitionTest() {
        val repeatedLine = "\u6211\u66f4\u4e60\u60ef\u4e00\u6b65\u4e00\u6b65\u8d70\u7a33"
        val result = DynamicPlaybookEngine().expressSelf(
            request(
                messages = planningMessages(),
                ledger = ExpressionLedger(
                    entries = listOf(
                        ledgerEntry(
                            lastSurfaceLineRedacted = repeatedLine,
                            repeatRisk = ExpressionRepeatRisk.HIGH,
                            otherReaction = OtherReaction.UNKNOWN,
                            canReappear = true
                        )
                    )
                )
            )
        )

        assertEquals(ExpressionMode.SWITCH_FACET, result.expressionModeSelection?.expressionMode)
        assertEquals(com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode.HOLD_BACK, result.expressSelfEligibility?.mode)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.routes.any { it.message.contains(repeatedLine) })
    }

    @Test
    fun RepeatedThemeCanElevateAfterOtherReceivedTest() {
        val result = DynamicPlaybookEngine().expressSelf(
            request(
                messages = planningMessages(),
                ledger = ExpressionLedger(
                    entries = listOf(
                        ledgerEntry(
                            repeatRisk = ExpressionRepeatRisk.HIGH,
                            otherReaction = OtherReaction.RECEIVED,
                            suggestedNextLevel = ExpressionLevel.CO_CREATION
                        )
                    )
                )
            )
        )

        assertEquals(ExpressionMode.ELEVATE_MEANING, result.expressionModeSelection?.expressionMode)
        assertEquals(com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode.HOLD_BACK, result.expressSelfEligibility?.mode)
        assertTrue(result.routes.isEmpty())
    }

    @Test
    fun OverExpressionGuardHoldsBackTest() {
        val result = DynamicPlaybookEngine().expressSelf(
            request(
                messages = listOf(
                    textNode("me-1", Speaker.ME, "I care about this.", 1),
                    textNode("me-2", Speaker.ME, "I want to explain myself again.", 2)
                ),
                ledger = ExpressionLedger(
                    entries = listOf(
                        ledgerEntry(lastUsedAt = 900L, lastExpressionLevel = ExpressionLevel.ATTITUDE),
                        ledgerEntry(lastUsedAt = 950L, lastExpressionLevel = ExpressionLevel.ARC_REVEAL)
                    )
                )
            )
        )

        assertEquals(ExpressionMode.HOLD_BACK, result.expressionModeSelection?.expressionMode)
        assertEquals(NextMoveType.WITHDRAW, result.nextMoveType)
        assertEquals(com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode.HOLD_BACK.name, result.panelNextAction)
        val summary = RoutePanelDisplayText.expressSelfSummaryLines(result.arcProgressState, result.routes)
        assertTrue(summary.isEmpty() || summary.none { it.contains("\u4eba\u7269\u5f27\u5149") })
        assertEquals(0, result.routes.size)
        assertTrue(result.routes.none { it.routeType == ReplyRouteType.ARC_REVEAL })
    }

    private fun request(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        ledger: ExpressionLedger,
        capturedAt: Long = 1000L,
        lastUserMessageAgeMsOverride: Long? = null
    ) = DynamicPlaybookRequest(
        mode = DynamicPlaybookMode.EXPRESS_SELF,
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "demo-chat",
        messages = messages,
        personaCorpus = persona,
        capturedAt = capturedAt,
        currentTopics = listOf("planning", "future", "reality"),
        expressionLedger = ledger,
        chatWindowHash = "demo-hash",
        lastUserMessageAgeMsOverride = lastUserMessageAgeMsOverride
    )

    private fun planningMessages() = listOf(
        textNode("me-1", Speaker.ME, "I hear you.", 1),
        textNode("other-1", Speaker.OTHER, "This needs planning for the future.", 2)
    )

    private fun ledgerEntry(
        lastUsedAt: Long = 900L,
        lastExpressionLevel: ExpressionLevel = ExpressionLevel.ARC_REVEAL,
        lastSurfaceLineRedacted: String = "\u6211\u66f4\u4e60\u60ef\u4e00\u6b65\u4e00\u6b65\u8d70\u7a33",
        otherReaction: OtherReaction = OtherReaction.UNKNOWN,
        repeatRisk: ExpressionRepeatRisk = ExpressionRepeatRisk.LOW,
        canReappear: Boolean = true,
        suggestedNextLevel: ExpressionLevel = ExpressionLevel.ARC_REVEAL
    ) = ExpressionLedgerEntry(
        themeId = "planning_not_promise",
        themeName = "\u73b0\u5b9e\u89c4\u5212\u4f46\u4e0d\u753b\u997c",
        lastUsedAt = lastUsedAt,
        lastExpressionLevel = lastExpressionLevel,
        lastIntensity = InfluenceIntensity.LOW,
        lastSurfaceLineRedacted = lastSurfaceLineRedacted,
        otherReaction = otherReaction,
        repeatRisk = repeatRisk,
        canReappear = canReappear,
        suggestedNextLevel = suggestedNextLevel
    )
}
