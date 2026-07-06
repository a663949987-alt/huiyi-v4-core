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
import com.huiyi.v4.domain.playbook.ExpressSelfEligibilityEvaluator
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
                currentAppPackage = "com.huawei.android.launcher",
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
    fun XiaoenaiWindowTitleIsNotDesktopTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "小恩爱",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "小恩爱",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(false, ExpressSelfEligibilityEvaluator.isDesktopOrPanelWindow("小恩爱", "com.xiaoenai.app"))
        assertEquals(true, result.expressSelfEligibility?.targetAppSupported)
        assertEquals("GENERIC_TRIAL", result.expressSelfEligibility?.source)
    }

    @Test
    fun XiaoenaiGenericTrialAllowsStableChatTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "小恩爱",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "小恩爱",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.ALLOW_GENERIC_TRIAL, result.expressSelfEligibility?.mode)
        assertTrue(result.routes.isNotEmpty())
    }

    @Test
    fun GenericTrialBlocksLowConfidenceTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "小恩爱",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "小恩爱",
                targetAppSupported = false,
                parserConfidence = 55,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(false, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNSUPPORTED_CONTEXT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.LOW_GENERIC_CONFIDENCE, result.expressSelfEligibility?.blockReason)
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
                currentAppPackage = "com.huawei.android.launcher",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                messages = recentLastMeMessages()
            )
        )
        val eligibility = result.expressSelfEligibility

        assertEquals(false, eligibility?.eligible)
        assertEquals(false, eligibility?.targetAppSupported)
        assertEquals("\u534e\u4e3a\u684c\u9762", eligibility?.currentWindowTitleRedacted)
        assertEquals("com.huawei.android.launcher", eligibility?.currentAppPackage)
        assertEquals(Speaker.ME, eligibility?.lastSpeaker)
        assertEquals(false, eligibility?.shouldReply)
        assertEquals("WINDOW_IS_DESKTOP_OR_LAUNCHER", eligibility?.blockedReason)
    }

    @Test
    fun V4161BugFixtureBlocksHuaweiDesktopLastMeRoutesTest() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                currentAppPackage = "com.huawei.android.launcher",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                messages = recentLastMeMessages(),
                lastUserMessageAgeMsOverride = 20_000L
            )
        )

        assertEquals(false, result.expressSelfEligibility?.targetAppSupported)
        assertEquals("\u534e\u4e3a\u684c\u9762", result.expressSelfEligibility?.currentWindowTitleRedacted)
        assertEquals(Speaker.ME, result.expressSelfEligibility?.lastSpeaker)
        assertEquals(false, result.expressSelfEligibility?.shouldReply)
        assertEquals(false, result.expressSelfEligibility?.eligible)
        assertEquals(false, result.routes.isNotEmpty())
        assertNotEquals(TacticalDecisionType.NORMAL_REPLY, result.tacticalDecisionType)
        assertEquals(0, result.routes.size)
        assertEquals(false, result.cloudRefreshRecommended)
    }

    @Test
    fun V4165_XIAOENAI_HUAWEI_DESKTOP_EXPRESS_SELF_BLOCK() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                currentAppPackage = "com.huawei.android.launcher",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                messages = recentLastMeMessages(),
                lastUserMessageAgeMsOverride = 20_000L
            )
        )

        assertEquals(false, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER, result.expressSelfEligibility?.blockReason)
        assertEquals(0, result.routes.size)
        assertFalse(result.cloudRefreshRecommended)
    }

    @Test
    fun V4165_XIAOENAI_NORMAL_CHAT_GENERIC_TRIAL() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u5c0f\u6069\u7231",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "\u5c0f\u6069\u7231",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.ALLOW_GENERIC_TRIAL, result.expressSelfEligibility?.mode)
        assertEquals("GENERIC_TRIAL", result.expressSelfEligibility?.source)
        assertTrue(result.routes.isNotEmpty())
    }

    @Test
    fun XIAOENAI_DESKTOP_BLOCK() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER, result.expressSelfEligibility?.blockReason)
        assertEquals(0, result.routes.size)
        assertFalse(result.cloudRefreshRecommended)
    }

    @Test
    fun XIAOENAI_DESKTOP_LAST_STABLE_SNAPSHOT_DOES_NOT_ANALYZE() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                parserConfidence = 82,
                preAnalysisSnapshotSource = "LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL",
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER, result.expressSelfEligibility?.blockReason)
        assertEquals(0, result.routes.size)
        assertFalse(result.cloudRefreshRecommended)
    }

    @Test
    fun V4166_HUAWEI_DESKTOP_WITH_LAUNCHER_PACKAGE_STILL_BLOCKS() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u534e\u4e3a\u684c\u9762",
                currentAppPackage = "com.huawei.android.launcher",
                currentWindowTitleRedacted = "\u534e\u4e3a\u684c\u9762",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertBlocked(result)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT, result.expressSelfEligibility?.mode)
        assertEquals(ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER, result.expressSelfEligibility?.blockReason)
    }

    @Test
    fun XIAOENAI_NORMAL_CHAT_LAST_OTHER_NEXT_SENTENCE_GENERIC_TRIAL_READY() {
        val result = engine().nextSentence(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u5c0f\u6069\u7231",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "\u5c0f\u6069\u7231",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecisionType)
        assertTrue(result.passiveWaitPanelShown)
        assertTrue(result.cloudRefreshRecommended)
        assertEquals(0, result.routes.size)
    }

    @Test
    fun XIAOENAI_NORMAL_CHAT_EXPRESS_SELF_PLANNING_GENERIC_TRIAL() {
        val result = engine().expressSelf(
            request(
                appPackage = "com.xiaoenai.app",
                windowTitle = "\u5c0f\u6069\u7231",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitleRedacted = "\u5c0f\u6069\u7231",
                targetAppSupported = false,
                parserConfidence = 82,
                messages = stableXiaoenaiPlanningMessages()
            )
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertEquals(ExpressSelfEligibilityMode.ALLOW_GENERIC_TRIAL, result.expressSelfEligibility?.mode)
        assertEquals("GENERIC_TRIAL", result.expressSelfEligibility?.source)
        assertTrue(result.routes.isNotEmpty())
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

    private fun stableXiaoenaiPlanningMessages() = listOf(
        textNode("me-1", Speaker.ME, "我明白你的意思。", 1),
        textNode("other-1", Speaker.OTHER, "这个事情还是要考虑现实和规划。", 2),
        textNode("me-2", Speaker.ME, "嗯，我也觉得不能只靠嘴上说。", 3),
        textNode("other-2", Speaker.OTHER, "稳定和以后也挺重要的。", 4)
    )

    private fun assertBlocked(result: com.huiyi.v4.domain.playbook.DynamicPlaybookResult) {
        assertEquals(false, result.expressSelfEligibility?.eligible)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.cloudRefreshRecommended)
        assertNotEquals(TacticalDecisionType.NORMAL_REPLY, result.tacticalDecisionType)
    }
}
