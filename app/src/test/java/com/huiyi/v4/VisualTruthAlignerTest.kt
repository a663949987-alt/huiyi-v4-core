package com.huiyi.v4

import com.huiyi.v4.domain.capture.VisualTruthAligner
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
}
