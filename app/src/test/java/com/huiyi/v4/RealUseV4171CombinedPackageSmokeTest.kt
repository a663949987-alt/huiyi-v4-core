package com.huiyi.v4

import com.huiyi.v4.domain.app.ChatAppProfileDetectionInput
import com.huiyi.v4.domain.app.ChatAppProfileDetector
import com.huiyi.v4.domain.app.ChatAppSupportLevel
import com.huiyi.v4.domain.app.UnsupportedAppAdaptationExporter
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealUseV4171CombinedPackageSmokeTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun NextSentenceLastMeWaitsAndSkipsCloudTest() {
        val result = engine().nextSentence(
            request(
                messages = listOf(
                    textNode("other-1", Speaker.OTHER, "我先去忙一下", 1),
                    textNode("me-1", Speaker.ME, "好，你先忙", 2)
                )
            )
        )

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecisionType)
        assertEquals("LOCAL_WAIT", result.decisionSource)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.cloudRefreshRecommended)
        assertFalse(result.cloudRefreshAttempted)
    }

    @Test
    fun NextSentenceLastOtherShowsNoLocalPassiveRoutesTest() {
        val result = engine().nextSentence(request(messages = planningMessages()))

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecisionType)
        assertTrue(result.localPassiveRoutesGenerated)
        assertFalse(result.localPassiveRoutesShownToUser)
        assertTrue(result.passiveWaitPanelShown)
        assertEquals(0, result.routes.size)
        assertEquals("PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK", result.decisionSource)
    }

    @Test
    fun ExpressSelfPlanningStabilityShowsArcRevealTest() {
        val result = engine().expressSelf(
            request(
                mode = DynamicPlaybookMode.EXPRESS_SELF,
                messages = planningMessages(),
                currentTopics = listOf("planning", "reality", "stability", "future")
            )
        )

        assertEquals(true, result.expressSelfEligibility?.eligible)
        assertTrue(result.routes.size in 1..3)
        assertTrue(result.routes.any { it.routeType == ReplyRouteType.ARC_REVEAL })
        assertTrue(result.expressSelfPanelSimpleMode)
        assertFalse(result.expressSelfFeedbackDefaultVisible)
        assertTrue(result.expressSelfFeedbackCollapsed)
    }

    @Test
    fun ExpressSelfRecentLastMeHoldsBackTest() {
        val result = engine().expressSelf(
            request(
                mode = DynamicPlaybookMode.EXPRESS_SELF,
                messages = listOf(
                    textNode("other-1", Speaker.OTHER, "这个事情还是要规划好", 1),
                    textNode("me-1", Speaker.ME, "我也觉得要一步一步走稳", 2)
                ),
                capturedAt = 10_000L,
                lastUserMessageAgeMsOverride = 20_000L
            )
        )

        assertEquals(TacticalDecisionType.HOLD_BACK, result.tacticalDecisionType)
        assertEquals(ExpressSelfEligibilityMode.BLOCK_RECENT_LAST_ME, result.expressSelfEligibility?.mode)
        assertEquals(0, result.routes.size)
        assertFalse(result.cloudRefreshRecommended)
    }

    @Test
    fun XiaoenaiNormalChatUsesGenericTrialAndDesktopBlocksTest() {
        val normal = detect(
            appPackage = "com.xiaoenai.app",
            windowTitle = "小恩爱",
            messages = planningMessages(),
            parserConfidence = 86
        )
        val desktop = detect(
            appPackage = "com.xiaoenai.app",
            windowTitle = "华为桌面",
            currentAppPackage = "com.huawei.android.launcher",
            currentWindowTitle = "华为桌面",
            messages = planningMessages(),
            parserConfidence = 86
        )

        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, normal.supportLevel)
        assertTrue(normal.targetAppSupported)
        assertEquals("GENERIC_TRIAL", normal.source)
        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, desktop.supportLevel)
        assertFalse(desktop.targetAppSupported)
    }

    @Test
    fun MockChatMainstreamProfilesUseGenericTrialTest() {
        listOf("wechat_like", "qq_like", "redbook_like", "dating_like").forEach { profile ->
            val result = detect(
                appPackage = "com.huiyi.mockchat",
                windowTitle = profile,
                messages = planningMessagesWithReadStatus(),
                parserConfidence = 88
            )

            assertEquals(profile, ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, result.supportLevel)
            assertTrue(profile, result.targetAppSupported)
        }
    }

    @Test
    fun WebviewLowAccessibilityExportsRedactedAdaptationPackTest() {
        val messages = listOf(
            textNode("unknown-1", Speaker.UNKNOWN, "真实私聊内容一", 1).copy(speakerConfidence = 28),
            textNode("unknown-2", Speaker.UNKNOWN, "真实私聊内容二", 2).copy(speakerConfidence = 28)
        )
        val result = detect(
            appPackage = "com.example.webview.chat",
            windowTitle = "WebView Chat",
            messages = messages,
            parserConfidence = 45
        )
        val pack = UnsupportedAppAdaptationExporter().build(
            appPackage = "com.example.webview.chat",
            windowTitle = "WebView Chat",
            messages = messages,
            reasonWhyUnsupported = result.reason
        )

        assertEquals(ChatAppSupportLevel.LEVEL_1_UNSUPPORTED_WITH_ADAPTATION_PACK, result.supportLevel)
        assertTrue(result.shouldGenerateAdaptationPack)
        assertFalse(pack.screenshotIncluded)
        assertFalse(pack.rawPrivateChatIncluded)
        assertTrue(pack.visibleTextRedacted.all { it.startsWith("[redacted:") })
    }

    @Test
    fun LauncherAndHuiyiOverlayBlockBeforeAnalysisTest() {
        val launcher = detect(
            appPackage = "com.huawei.android.launcher",
            windowTitle = "华为桌面",
            messages = planningMessages(),
            parserConfidence = 90
        )
        val overlay = detect(
            appPackage = "com.bajiao.im.liaoqi",
            windowTitle = "会意雷达 这次不对，发给 GPT 隐藏",
            currentAppPackage = "com.huiyi.v4",
            currentWindowTitle = "会意雷达 这次不对，发给 GPT 隐藏",
            messages = planningMessages(),
            parserConfidence = 90
        )

        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, launcher.supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, overlay.supportLevel)
        assertFalse(launcher.targetAppSupported)
        assertFalse(overlay.targetAppSupported)
    }

    @Test
    fun MessageStatusMetadataDoesNotChangeLastEffectiveSpeakerTest() {
        val messages = listOf(
            textNode("other-1", Speaker.OTHER, "那你先忙", 1),
            textNode("me-1", Speaker.ME, "好，忙完我再说", 2),
            textNode("status-1", Speaker.SYSTEM, "已读", 3).copy(
                isEffectiveChatMessage = false,
                metadataType = MetadataType.READ_RECEIPT
            ),
            textNode("status-2", Speaker.SYSTEM, "✓", 4).copy(
                isEffectiveChatMessage = false,
                metadataType = MetadataType.DELIVERY_STATUS
            )
        )
        val decision = LastSpeakerDecisionUseCase().decide(messages)

        assertEquals(Speaker.ME, decision.lastSpeaker)
        assertFalse(decision.shouldReply)
    }

    private fun engine(): DynamicPlaybookEngine = DynamicPlaybookEngine()

    private fun request(
        mode: DynamicPlaybookMode = DynamicPlaybookMode.NEXT_SENTENCE,
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        currentTopics: List<String> = emptyList(),
        capturedAt: Long = 1000L,
        lastUserMessageAgeMsOverride: Long? = null
    ): DynamicPlaybookRequest = DynamicPlaybookRequest(
        mode = mode,
        appPackage = "com.huiyi.mockchat",
        windowTitle = "wechat_like",
        messages = messages,
        personaCorpus = persona,
        capturedAt = capturedAt,
        currentTopics = currentTopics,
        chatWindowHash = "v4171-combined-smoke",
        targetAppSupported = true,
        parserConfidence = 88,
        lastUserMessageAgeMsOverride = lastUserMessageAgeMsOverride
    )

    private fun detect(
        appPackage: String,
        windowTitle: String,
        currentAppPackage: String = appPackage,
        currentWindowTitle: String = windowTitle,
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        parserConfidence: Int
    ) = ChatAppProfileDetector.detect(
        ChatAppProfileDetectionInput(
            appPackage = appPackage,
            windowTitle = windowTitle,
            currentAppPackage = currentAppPackage,
            currentWindowTitle = currentWindowTitle,
            messages = messages,
            parserConfidence = parserConfidence
        )
    )

    private fun planningMessages() = listOf(
        textNode("me-1", Speaker.ME, "我明白你的意思。", 1),
        textNode("other-1", Speaker.OTHER, "这个事情还是要考虑现实和规划。", 2),
        textNode("me-2", Speaker.ME, "嗯，我也觉得不能只靠嘴上说。", 3),
        textNode("other-2", Speaker.OTHER, "稳定和以后也挺重要的。", 4)
    )

    private fun planningMessagesWithReadStatus() = planningMessages() + listOf(
        textNode("status-1", Speaker.SYSTEM, "已读", 5).copy(
            isEffectiveChatMessage = false,
            metadataType = MetadataType.READ_RECEIPT
        ),
        textNode("status-2", Speaker.SYSTEM, "送达", 6).copy(
            isEffectiveChatMessage = false,
            metadataType = MetadataType.DELIVERY_STATUS
        )
    )
}
