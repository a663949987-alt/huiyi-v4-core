package com.huiyi.v4

import com.huiyi.v4.domain.capture.LiaoqiRealParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiaoqiRealParserTest {
    @Test
    fun usesParentBubbleBoundsForRightSideMe() {
        val nodes = LiaoqiRealParser(screenWidth = 1080).parse(
            listOf(
                VisualBubble(
                    id = "me",
                    text = "人消失了啊……",
                    textBounds = VisualBounds(620, 1200, 900, 1260),
                    parentBounds = VisualBounds(520, 1180, 1030, 1290),
                    rowBounds = VisualBounds(0, 1160, 1080, 1310),
                    bubbleBounds = VisualBounds(620, 1200, 900, 1260)
                )
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )

        assertEquals(Speaker.ME, nodes.single().speaker)
        assertEquals("LiaoqiRealParser", nodes.single().parserName)
    }

    @Test
    fun filtersLiaoqiDateBeforeLastSpeakerDecision() {
        val nodes = LiaoqiRealParser(screenWidth = 1080).parse(
            listOf(
                VisualBubble("date", "07-02", textBounds = VisualBounds(480, 100, 560, 145), parentBounds = VisualBounds(430, 90, 650, 155)),
                VisualBubble("me", "人消失了啊……", textBounds = VisualBounds(620, 300, 900, 360), parentBounds = VisualBounds(520, 280, 1030, 390))
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )

        assertEquals(MetadataType.DATE, nodes.first().metadataType)
        assertFalse(nodes.first().isEffectiveChatMessage)
        assertEquals(Speaker.ME, LastSpeakerDecisionUseCase().decide(nodes).lastSpeaker)
    }

    @Test
    fun flagsPossibleSpeakerConflictWithoutChangingSpeaker() {
        val node = LiaoqiRealParser(screenWidth = 1080).parse(
            listOf(
                VisualBubble(
                    id = "other",
                    text = "我进入机要室工作，晚上查寝。",
                    textBounds = VisualBounds(80, 400, 620, 470),
                    parentBounds = VisualBounds(40, 380, 700, 500)
                )
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        ).single()

        assertEquals(Speaker.OTHER, node.speaker)
        assertTrue(node.possibleSpeakerConflict)
    }

    @Test
    fun ambiguousWideBubbleDoesNotFallBackToLeftTextInset() {
        val node = LiaoqiRealParser(screenWidth = 1080).parse(
            listOf(
                VisualBubble(
                    id = "wide",
                    text = "long relationship paragraph",
                    textBounds = VisualBounds(205, 1573, 748, 2205),
                    parentBounds = VisualBounds(150, 1548, 896, 2230),
                    rowBounds = VisualBounds(0, 1536, 1080, 2242),
                    bubbleBounds = VisualBounds(150, 1548, 896, 2230),
                    ancestorBoundsChain = listOf(
                        VisualBounds(0, 0, 1080, 2412),
                        VisualBounds(0, 1536, 1080, 2242),
                        VisualBounds(150, 1548, 896, 2230)
                    )
                )
            ),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        ).single()

        assertEquals(Speaker.UNKNOWN, node.speaker)
        assertTrue(node.isEffectiveChatMessage)
        assertTrue(node.speakerReason.orEmpty().contains("liaoqi_ambiguous_bubble_bounds"))
    }
}
