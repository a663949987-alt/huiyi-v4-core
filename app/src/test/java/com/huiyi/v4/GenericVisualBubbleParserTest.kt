package com.huiyi.v4

import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import org.junit.Assert.assertEquals
import org.junit.Test

class GenericVisualBubbleParserTest {
    @Test
    fun rightSideIsMe() {
        val node = GenericVisualBubbleParser(1000).parse(listOf(VisualBubble("r", "我说的", bubbleBounds = VisualBounds(650, 0, 980, 80)))).single()

        assertEquals(Speaker.ME, node.speaker)
    }

    @Test
    fun leftSideIsOther() {
        val node = GenericVisualBubbleParser(1000).parse(listOf(VisualBubble("l", "她说的", bubbleBounds = VisualBounds(20, 0, 380, 80)))).single()

        assertEquals(Speaker.OTHER, node.speaker)
    }
}
