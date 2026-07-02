package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus

class ScreenshotOcrParser(
    private val screenWidth: Int = 1080
) {
    fun parse(lines: List<VisualTextLine>, sceneId: String? = null): List<MessageNode> {
        return lines.mapIndexed { index, line ->
            val speaker = when {
                isSystemLine(line) -> Speaker.SYSTEM
                line.bounds.centerX >= screenWidth / 2 -> Speaker.ME
                else -> Speaker.OTHER
            }
            val isVoice = line.text.contains("语音") || line.text.contains("秒")
            MessageNode(
                id = "ocr-${line.id}",
                contactId = null,
                speaker = speaker,
                content = if (isVoice) {
                    MessageContent.Voice(
                        durationSeconds = line.text.filter(Char::isDigit).toIntOrNull(),
                        transcriptStatus = TranscriptStatus.MISSING,
                        transcriptText = null,
                        userSummary = null
                    )
                } else {
                    MessageContent.Text(line.text)
                },
                normalizedText = if (isVoice) null else line.text.trim(),
                source = MessageSource.SCREENSHOT_OCR,
                localSequence = index.toLong(),
                confidence = line.confidence,
                speakerConfidence = if (speaker == Speaker.SYSTEM) 70 else 82,
                contentConfidence = line.confidence,
                bounds = line.bounds,
                pageIndex = 0,
                createdAt = System.currentTimeMillis() + index,
                sceneId = sceneId,
                speakerReason = if (speaker == Speaker.ME) "右侧气泡" else if (speaker == Speaker.OTHER) "左侧气泡" else "居中系统文本",
                parserName = "ScreenshotOcrParser"
            )
        }
    }

    private fun isSystemLine(line: VisualTextLine): Boolean {
        val text = line.text.trim()
        val centered = kotlin.math.abs(line.bounds.centerX - screenWidth / 2) < screenWidth / 10
        return centered && text.length <= 12 && (text.contains(":") || text.contains("昨天") || text.contains("今天"))
    }
}
