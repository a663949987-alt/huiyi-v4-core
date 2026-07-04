package com.huiyi.v4

import com.huiyi.v4.domain.capture.VisualTruthAligner
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualTruthAlignerTest {
    @Test
    fun unknownRightProjectedBoxUsesVisualSpeakerFallback() {
        val message = textNode("1", Speaker.UNKNOWN, "我是不知道怎么表达", 1).copy(
            speakerConfidence = 30,
            bubbleBounds = VisualBounds(650, 1000, 1030, 1100),
            inferredSide = "unknown"
        )

        val aligned = VisualTruthAligner(screenWidth = 1080).align(listOf(message)).messages.single()

        assertEquals(Speaker.ME, aligned.speaker)
        assertEquals("right", aligned.visualProjectedSide)
        assertEquals(75, aligned.speakerConfidence)
        assertTrue(aligned.visualSpeakerFallbackUsed)
    }

    @Test
    fun visualProjectedLastOtherBecomesEffectiveForLastSpeaker() {
        val previousMe = textNode("me", Speaker.ME, "current environment is not good", 14).copy(
            bubbleBounds = VisualBounds(336, 1839, 904, 1975),
            inferredSide = "right",
            isEffectiveChatMessage = true
        )
        val visualOther = textNode("other-visual", Speaker.UNKNOWN, "let us see if we have this fate", 15).copy(
            speakerConfidence = 30,
            bubbleBounds = VisualBounds(180, 2060, 810, 2258),
            inferredSide = "unknown",
            isEffectiveChatMessage = false
        )

        val aligned = VisualTruthAligner(screenWidth = 1084).align(listOf(previousMe, visualOther)).messages
        val last = LastSpeakerDecisionUseCase().decide(aligned)

        assertEquals(Speaker.OTHER, aligned.last().speaker)
        assertTrue(aligned.last().isEffectiveChatMessage)
        assertEquals(Speaker.OTHER, last.lastSpeaker)
        assertEquals("other-visual", last.lastEffectiveMessage?.id)
    }
}
