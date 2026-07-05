package com.huiyi.v4

import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.ExpressionLedger
import com.huiyi.v4.domain.playbook.ExpressSelfBlockReason
import com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressSelfEligibilityTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun ExpressSelfBlocksUnsupportedAppTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                targetAppSupported = false,
                parserConfidence = 40,
                messages = planningOtherMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNSUPPORTED_CONTEXT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.UNSUPPORTED_APP, result.expressSelfEligibility?.blockReason)
    }

    @Test
    fun ExpressSelfBlocksHuaweiDesktopSnapshotTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                messages = recentLastMeMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER, result.expressSelfEligibility?.blockReason)
    }

    @Test
    fun ExpressSelfBlocksUntrustedLastStableSnapshotTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.bajiao.im.liaoqi",
                currentAppPackage = "com.huawei.android.launcher",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                preAnalysisSnapshotSource = "LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL",
                messages = planningOtherMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
    }

    @Test
    fun ExpressSelfBlocksRecentLastMeWithoutColdStartTest() {
        val result = engine().expressSelf(
            request(
                messages = recentLastMeMessages(),
                capturedAt = 10_000L,
                lastUserMessageAgeMsOverride = 20_000L
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_RECENT_LAST_ME, result.expressSelfEligibility?.mode)
        assertEquals(TacticalDecisionType.HOLD_BACK, result.tacticalDecisionType)
    }

    @Test
    fun ExpressSelfHoldBackAfterRecentSelfExpressionTest() {
        val result = engine().expressSelf(
            request(
                messages = planningOtherMessages(),
                recentSelfExpressionCountOverride = 2
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.HOLD_BACK, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.TOO_MUCH_SELF_EXPRESSION_RECENTLY, result.expressSelfEligibility?.blockReason)
    }

    @Test
    fun ExpressSelfAllowsColdStartAfterLongInactiveTest() {
        val result = engine().expressSelf(
            request(
                messages = recentLastMeMessages(),
                capturedAt = 31 * 60 * 1000L,
                lastUserMessageAgeMsOverride = 31 * 60 * 1000L
            )
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.ALLOW_COLD_START, result.expressSelfEligibility?.mode)
        assertTrue(result.routes.size in 3..5)
    }

    @Test
    fun ExpressSelfAllowsArcRevealWhenOtherPlanningWindowTest() {
        val result = engine().expressSelf(
            request(messages = planningOtherMessages())
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertTrue(
            result.expressSelfEligibility?.mode in setOf(
                ExpressSelfEligibilityMode.ALLOW_EXPRESS_SELF,
                ExpressSelfEligibilityMode.ALLOW_ELEVATE_MEANING
            )
        )
        assertTrue(result.routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
    }

    @Test
    fun ExpressSelfDoesNotReturnNormalReplyWhenBlockedTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                targetAppSupported = false,
                parserConfidence = 40,
                messages = recentLastMeMessages()
            )
        )

        assertNotEquals(TacticalDecisionType.NORMAL_REPLY, result.tacticalDecisionType)
    }

    @Test
    fun ExpressSelfBlockedDoesNotShowRoutePanelTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                targetAppSupported = false,
                parserConfidence = 40,
                messages = recentLastMeMessages()
            )
        )

        assertEquals(0, result.routes.size)
        assertFalse(result.cloudRefreshRecommended)
    }

    @Test
    fun ExpressSelfEligibilityReportFieldsTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                messages = recentLastMeMessages()
            )
        )
        val eligibility = result.expressSelfEligibility

        assertEquals(false, eligibility?.eligible)
        assertEquals(false, eligibility?.targetAppSupported)
        assertEquals("\u534e\u4e3a\u684c\u9762", eligibility?.currentWindowTitleRedacted)
        assertEquals(Speaker.ME, eligibility?.lastSpeaker)
        assertEquals(false, eligibility?.shouldReply)
        assertEquals("WINDOW_IS_DESKTOP_OR_LAUNCHER", eligibility?.blockedReason)
    }

    private fun engine() = DynamicPlaybookEngine()

    private fun request(
        appPackage: String = "com.bajiao.im.liaoqi",
        windowTitle: String = "demo-chat",
        currentAppPackage: String? = appPackage,
        currentWindowTitleRedacted: String? = windowTitle,
        targetAppSupported: Boolean? = appPackage == "com.bajiao.im.liaoqi" || appPackage == "com.huiyi.mockchat",
        snapshotTrusted: Boolean = true,
        parserConfidence: Int = 100,
        capturedAt: Long = 100_000L,
        lastUserMessageAgeMsOverride: Long? = null,
        recentSelfExpressionCountOverride: Int? = null,
        preAnalysisSnapshotSource: String = "CURRENT_ROOT_BEFORE_PANEL",
        messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) = DynamicPlaybookRequest(
        mode = DynamicPlaybookMode.EXPRESS_SELF,
        appPackage = appPackage,
        windowTitle = windowTitle,
        messages = messages,
        personaCorpus = persona,
        capturedAt = capturedAt,
        currentTopics = listOf("planning", "reality", "stability", "future"),
        expressionLedger = ExpressionLedger.empty(),
        sessionId = "test-session",
        chatWindowHash = "demo-hash",
        targetAppSupported = targetAppSupported,
        snapshotTrusted = snapshotTrusted,
        currentAppPackage = currentAppPackage,
        currentWindowTitleRedacted = currentWindowTitleRedacted,
        parserConfidence = parserConfidence,
        lastUserMessageAgeMsOverride = lastUserMessageAgeMsOverride,
        recentSelfExpressionCountOverride = recentSelfExpressionCountOverride,
        preAnalysisSnapshotSource = preAnalysisSnapshotSource
    )

    private fun planningOtherMessages() = listOf(
        textNode("me-1", Speaker.ME, "I hear you.", 1),
        textNode("other-1", Speaker.OTHER, "This needs real planning, future stability and responsibility.", 2)
    )

    private fun recentLastMeMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "I care about stable reality.", 1),
        textNode("me-1", Speaker.ME, "I prefer to make real plans step by step.", 2)
    )

    private fun assertBlocked(result: com.huiyi.v4.domain.playbook.DynamicPlaybookResult) {
        assertEquals(false, result.expressSelfEligibility?.eligible)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.cloudRefreshRecommended)
        assertNotEquals(TacticalDecisionType.NORMAL_REPLY, result.tacticalDecisionType)
    }
}
