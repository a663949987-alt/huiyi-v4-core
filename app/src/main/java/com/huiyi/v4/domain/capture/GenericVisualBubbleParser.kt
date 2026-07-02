package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus
import com.huiyi.v4.domain.model.VisualBounds

data class VisualBubble(
    val id: String,
    val text: String?,
    val avatarBounds: VisualBounds? = null,
    val bubbleBounds: VisualBounds? = null,
    val rowBounds: VisualBounds? = null,
    val contentBounds: VisualBounds? = null,
    val textBounds: VisualBounds? = null,
    val confidence: Int = 85
)

class GenericVisualBubbleParser(
    private val screenWidth: Int = 1080,
    private val meOnRight: Boolean = true
) {
    fun parse(bubbles: List<VisualBubble>, source: MessageSource = MessageSource.ACCESSIBILITY_CURRENT_SCREEN): List<MessageNode> {
        return bubbles.mapIndexed { index, bubble ->
            val selectedBounds = bubble.avatarBounds
                ?: bubble.bubbleBounds
                ?: bubble.rowBounds
                ?: bubble.contentBounds
                ?: bubble.textBounds
            val speaker = selectedBounds?.let { bounds ->
                val rightSide = bounds.centerX >= screenWidth / 2
                when {
                    rightSide && meOnRight -> Speaker.ME
                    rightSide && !meOnRight -> Speaker.OTHER
                    !rightSide && meOnRight -> Speaker.OTHER
                    else -> Speaker.ME
                }
            } ?: Speaker.UNKNOWN
            val isVoice = bubble.text?.contains("语音") == true || bubble.text?.contains("秒") == true
            MessageNode(
                id = "bubble-${bubble.id}",
                contactId = null,
                speaker = speaker,
                content = if (isVoice) {
                    MessageContent.Voice(
                        durationSeconds = bubble.text?.filter(Char::isDigit)?.toIntOrNull(),
                        transcriptStatus = TranscriptStatus.MISSING,
                        transcriptText = null,
                        userSummary = null
                    )
                } else {
                    MessageContent.Text(bubble.text.orEmpty())
                },
                normalizedText = if (isVoice) null else bubble.text,
                source = source,
                localSequence = index.toLong(),
                confidence = bubble.confidence,
                speakerConfidence = if (speaker == Speaker.UNKNOWN) 30 else 82,
                contentConfidence = bubble.confidence,
                bounds = selectedBounds,
                pageIndex = 0,
                createdAt = System.currentTimeMillis() + index,
                sceneId = null,
                speakerReason = selectedBounds?.let { if (it.centerX >= screenWidth / 2) "右侧气泡" else "左侧气泡" } ?: "缺少可用边界",
                parserName = "GenericVisualBubbleParser"
            )
        }
    }
}
