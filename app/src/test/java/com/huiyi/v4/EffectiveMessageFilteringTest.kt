package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.pipeline.ParserReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectiveMessageFilteringTest {
    @Test
    fun timeMetadataNotMe() {
        val node = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(VisualBubble("time", "10:56", bubbleBounds = VisualBounds(650, 100, 900, 160))),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        ).single()

        assertEquals(Speaker.SYSTEM, node.speaker)
        assertEquals(MetadataType.TIME, node.metadataType)
        assertFalse(node.isEffectiveChatMessage)
    }

    @Test
    fun shortDashDateMetadataNotUnknown() {
        val node = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(VisualBubble("date", "07-02", bubbleBounds = VisualBounds(450, 100, 550, 150))),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        ).single()

        assertEquals(Speaker.SYSTEM, node.speaker)
        assertEquals(MetadataType.DATE, node.metadataType)
        assertFalse(node.isEffectiveChatMessage)
    }

    @Test
    fun headerMetadataNotOther() {
        val nodes = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(
                VisualBubble("title", "白云蓝天", bubbleBounds = VisualBounds(50, 20, 300, 90)),
                VisualBubble("online", "上次在线时间07-02 18:06", bubbleBounds = VisualBounds(50, 100, 420, 170)),
                VisualBubble("msg", "孩子跟着男方的", bubbleBounds = VisualBounds(50, 300, 520, 380))
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )
        val decision = LastSpeakerDecisionUseCase().decide(nodes)

        assertEquals(MetadataType.HEADER, nodes[0].metadataType)
        assertEquals(MetadataType.ONLINE_STATUS, nodes[1].metadataType)
        assertFalse(nodes[0].isEffectiveChatMessage)
        assertFalse(nodes[1].isEffectiveChatMessage)
        assertEquals(Speaker.OTHER, decision.lastSpeaker)
    }

    @Test
    fun lastSpeakerIgnoresTimestamps() {
        val nodes = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(
                VisualBubble("other", "是啊，我离婚是10年了呀。", bubbleBounds = VisualBounds(50, 300, 540, 390)),
                VisualBubble("time", "10:59", bubbleBounds = VisualBounds(700, 420, 900, 480))
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )

        val decision = LastSpeakerDecisionUseCase().decide(nodes)

        assertEquals(Speaker.OTHER, decision.lastSpeaker)
        assertEquals("other", decision.lastEffectiveMessage?.id?.removePrefix("bubble-"))
    }

    @Test
    fun contextSummaryExcludesMetadata() {
        val nodes = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(
                VisualBubble("title", "白云蓝天", bubbleBounds = VisualBounds(50, 20, 300, 90)),
                VisualBubble("time", "10:56", bubbleBounds = VisualBounds(700, 100, 900, 160)),
                VisualBubble("other", "本来我的过去我不想再提离婚。", bubbleBounds = VisualBounds(50, 300, 650, 390))
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )
        val context = ContextAssembler().assemble(nodes)
        val summary = context.turns.joinToString(" ") { it.summary }

        assertFalse(summary.contains("白云蓝天"))
        assertFalse(summary.contains("10:56"))
        assertTrue(summary.contains("过去"))
    }

    @Test
    fun reportIncludesEffectiveMessageStats() {
        val nodes = GenericVisualBubbleParser(screenWidth = 1000).parse(
            listOf(
                VisualBubble("time", "10:56", bubbleBounds = VisualBounds(700, 100, 900, 160)),
                VisualBubble("other", "孩子跟着男方的", bubbleBounds = VisualBounds(50, 300, 520, 380))
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )
        val result = CurrentScreenCaptureResult(
            snapshot = CurrentScreenSnapshot("com.chat.real", "聊天", 1000, 2000, emptyList(), 1),
            messages = nodes,
            sampleSource = SampleSource.REAL_DEVICE_ACCESSIBILITY
        )

        val report = ParserReportGenerator().build(result)

        assertTrue(report.contains("metadataFilteredCount"))
        assertTrue(report.contains("effectiveMessageCount"))
        assertTrue(report.contains("filteredMetadataSamples"))
    }
}
