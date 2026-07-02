package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.ParserReportGenerator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GenericParserReportTest {
    @Test
    fun writesParserReport() {
        val snapshot = CurrentScreenSnapshot(
            appPackage = "com.chat.demo",
            windowTitle = "聊天",
            screenWidth = 1000,
            screenHeight = 2000,
            nodes = listOf(
                ScreenNodeSnapshot("n1", "你好", null, "TextView", null, VisualBounds(20, 100, 300, 180), true, 1, 0)
            ),
            capturedAt = 1
        )
        val messages = GenericVisualBubbleParser(1000).parse(
            listOf(VisualBubble("1", "你好", bubbleBounds = VisualBounds(20, 100, 300, 180))),
            MessageSource.ACCESSIBILITY_CURRENT_SCREEN
        )
        val result = CurrentScreenCaptureResult(snapshot, messages)
        val file = File.createTempFile("current-screen-parser-report-for-gpt", ".md")

        ParserReportGenerator().writeTo(file, result).getOrThrow()

        assertTrue(file.readText().contains("parsedMessageCount: 1"))
        assertTrue(file.readText().contains("[m001]"))
    }
}
