package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MessageDeliveryStatus
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.RealDeviceScenarioValidator
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LastMeWaitPriorityAndStatusMetadataFixTest {
    @Test
    fun LastMeAlwaysReturnsWaitBeforeContextRequiredTest() = runTest {
        val messages = listOf(
            textNode("unknown-1", Speaker.UNKNOWN, "ambiguous", 1),
            textNode("me-2", Speaker.ME, "ok", 2)
        )
        val result = CurrentScreenPipelineUseCase(FakeCaptureUseCase(messages)).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.apiCalled)
    }

    @Test
    fun LastMeCannotBecomeContextRequiredTest() {
        val context = ContextAssembler().assemble(listOf(textNode("me-only", Speaker.ME, "ok", 1)))
        val decision = TacticalDecisionEngine().decide(context)

        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
    }

    @Test
    fun LastMeContextCompletenessDoesNotOverrideWaitTest() {
        val context = ContextAssembler().assemble(listOf(textNode("me-only", Speaker.ME, "ok", 1).copy(contentConfidence = 40)))

        assertFalse(context.contentCompleteness.canDeepAnalyze)
        assertEquals(TacticalDecisionType.WAIT, TacticalDecisionEngine().decide(context).decisionType)
    }

    @Test
    fun LastMeTerminalStateIsWaitPanelTest() {
        val record = com.huiyi.v4.runtime.NextSentenceFlightRecordFactory.fromSuccess(
            evidenceResult(
                appPackage = "com.bajiao.im.liaoqi",
                source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
                messages = listOf(textNode("me", Speaker.ME, "ok", 1)),
                includeRoutes = false
            ).copy(waitPanelShown = true),
            com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace("s", 1L, endedAt = 2L)
        )

        assertEquals("WAIT_PANEL", record.terminalState)
        assertTrue(record.waitPanelShown)
        assertFalse(record.contextRequiredPanelShown)
    }

    @Test
    fun PreAnalysisTitleContaminationDetectedForOneTapPanelTest() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("me", Speaker.ME, "ok", 1)),
            includeRoutes = false,
            windowTitle = "下一句没有跑完，正在上传 GitHub，这次不对，发给 GPT 重试"
        )

        val validation = RealDeviceScenarioValidator.validate(result, RealDeviceScenario.LAST_ME)

        assertTrue(validation.reportWindowTitleContaminatedByPanel)
        assertEquals("PARTIAL", validation.preAnalysisSnapshotTrusted)
        assertEquals("HUIYI_OVERLAY_CONTAMINATED", validation.preAnalysisWindowTitleSource)
    }

    @Test
    fun ReadReceiptNodeIsMetadataNotMessageTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 0, 980, 80)),
                VisualBubble("read", "已读", bubbleBounds = VisualBounds(900, 90, 980, 120))
            )
        )
        val receipt = nodes.last()

        assertEquals(MetadataType.READ_RECEIPT, receipt.metadataType)
        assertEquals(Speaker.SYSTEM, receipt.speaker)
        assertFalse(receipt.isEffectiveChatMessage)
        assertEquals(MessageDeliveryStatus.READ, receipt.statusArtifact?.status)
    }

    @Test
    fun DeliveryStatusAttachedToPreviousMeMessageTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 0, 980, 80)),
                VisualBubble("read", "已读", bubbleBounds = VisualBounds(900, 90, 980, 120))
            )
        )

        assertEquals("bubble-me", nodes.last().statusArtifact?.attachedToMessageId)
    }

    @Test
    fun ReadReceiptDoesNotAffectLastSpeakerTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("other", "hello", bubbleBounds = VisualBounds(20, 0, 350, 80)),
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 100, 980, 180)),
                VisualBubble("read", "已读", bubbleBounds = VisualBounds(900, 190, 980, 220))
            )
        )

        assertEquals(Speaker.ME, LastSpeakerDecisionUseCase().decide(nodes).lastSpeaker)
    }

    @Test
    fun LastMeWithReadReceiptStillWaitsTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("other", "hello", bubbleBounds = VisualBounds(20, 0, 350, 80)),
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 100, 980, 180)),
                VisualBubble("read", "已读", bubbleBounds = VisualBounds(900, 190, 980, 220))
            )
        )
        val decision = TacticalDecisionEngine().decide(ContextAssembler().assemble(nodes))

        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
    }

    @Test
    fun LastMeWithSendFailedShowsSendFailedWarningTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 0, 980, 80)),
                VisualBubble("failed", "发送失败", bubbleBounds = VisualBounds(900, 90, 980, 120))
            )
        )

        assertEquals(MessageDeliveryStatus.SEND_FAILED, nodes.last().statusArtifact?.status)
        assertFalse(nodes.last().isEffectiveChatMessage)
    }

    @Test
    fun StatusArtifactDoesNotBecomeEffectiveMessageTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 0, 980, 80)),
                VisualBubble("icon", "✓✓", bubbleBounds = VisualBounds(900, 90, 980, 120))
            )
        )

        assertEquals(1, nodes.count { it.isEffectiveChatMessage })
        assertEquals(MetadataType.MESSAGE_STATUS_ICON, nodes.last().metadataType)
    }

    @Test
    fun EvidenceReportIncludesMessageStatusSummaryFieldsTest() {
        val nodes = statusParser().parse(
            listOf(
                VisualBubble("me", "ok", bubbleBounds = VisualBounds(650, 0, 980, 80)),
                VisualBubble("read", "已读", bubbleBounds = VisualBounds(900, 90, 980, 120))
            )
        )
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = nodes,
            includeRoutes = false
        )
        val markdown = EvidencePackReportGenerator().buildMarkdown(
            result,
            HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            scenario = RealDeviceScenario.LAST_ME
        )

        assertTrue(markdown.contains("messageStatusArtifactCount: 1"))
        assertTrue(markdown.contains("lastMeReadStatus: READ"))
        assertTrue(markdown.contains("statusArtifactsFilteredFromEffectiveMessages: true"))
    }

    private fun statusParser() = GenericVisualBubbleParser(screenWidth = 1000)

    private class FakeCaptureUseCase(
        private val messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> {
            return Result.success(
                CurrentScreenCaptureResult(
                    snapshot = CurrentScreenSnapshot(
                        appPackage = "com.bajiao.im.liaoqi",
                        windowTitle = "chat",
                        screenWidth = 1080,
                        screenHeight = 2400,
                        nodes = emptyList(),
                        capturedAt = 1L
                    ),
                    messages = messages,
                    sampleSource = SampleSource.REAL_DEVICE_ACCESSIBILITY
                )
            )
        }
    }

    private fun emptyPersona() = UserPersonaCorpus(
        id = "test",
        name = "test",
        enabled = false,
        identityCards = emptyList(),
        storyCards = emptyList(),
        styleRules = emptyList(),
        riskRules = emptyList()
    )
}
