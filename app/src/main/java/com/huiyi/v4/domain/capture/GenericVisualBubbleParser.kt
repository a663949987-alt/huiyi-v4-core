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
    private val metadataFilter = MetadataMessageFilter()

    fun parse(bubbles: List<VisualBubble>, source: MessageSource = MessageSource.ACCESSIBILITY_CURRENT_SCREEN): List<MessageNode> {
        return bubbles.mapIndexed { index, bubble ->
            val selectedBounds = bubble.avatarBounds
                ?: bubble.bubbleBounds
                ?: bubble.rowBounds
                ?: bubble.contentBounds
                ?: bubble.textBounds
            val metadataType = metadataFilter.classify(bubble.text)
            val isMetadata = metadataType != com.huiyi.v4.domain.model.MetadataType.NONE
            val inferredSide = selectedBounds?.let {
                when {
                    it.isAmbiguousHorizontalPosition() -> "unknown"
                    it.centerX >= screenWidth / 2 -> "right"
                    else -> "left"
                }
            } ?: "unknown"
            val speaker = if (isMetadata) {
                Speaker.SYSTEM
            } else selectedBounds?.let { bounds ->
                if (bounds.isAmbiguousHorizontalPosition()) return@let Speaker.UNKNOWN
                val rightSide = bounds.centerX >= screenWidth / 2
                when {
                    rightSide && meOnRight -> Speaker.ME
                    rightSide && !meOnRight -> Speaker.OTHER
                    !rightSide && meOnRight -> Speaker.OTHER
                    else -> Speaker.ME
                }
            } ?: Speaker.UNKNOWN
            val isVoice = !isMetadata && (bubble.text?.contains("语音") == true || bubble.text?.contains("秒") == true)
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
                speakerConfidence = when {
                    isMetadata -> 100
                    speaker == Speaker.UNKNOWN -> 30
                    else -> 82
                },
                contentConfidence = bubble.confidence,
                bounds = selectedBounds,
                pageIndex = 0,
                createdAt = System.currentTimeMillis() + index,
                sceneId = null,
                speakerReason = when {
                    isMetadata -> when (metadataType) {
                        com.huiyi.v4.domain.model.MetadataType.TIME,
                        com.huiyi.v4.domain.model.MetadataType.DATE -> "time_metadata"
                        com.huiyi.v4.domain.model.MetadataType.ONLINE_STATUS -> "online_status_metadata"
                        com.huiyi.v4.domain.model.MetadataType.UI_CONTROL -> "ui_control_metadata"
                        com.huiyi.v4.domain.model.MetadataType.SYSTEM_NOTICE -> "system_notice_metadata"
                        else -> "header_metadata"
                    }
                    selectedBounds?.isAmbiguousHorizontalPosition() == true -> "ambiguous_center_bounds"
                    selectedBounds != null -> if (selectedBounds.centerX >= screenWidth / 2) "bubble_edge_right" else "bubble_edge_left"
                    else -> "unknown_visual_bounds"
                },
                parserName = "GenericVisualBubbleParser",
                isEffectiveChatMessage = !isMetadata && speaker in setOf(Speaker.ME, Speaker.OTHER),
                metadataType = metadataType,
                rowBounds = bubble.rowBounds,
                textBounds = bubble.textBounds,
                inferredSide = inferredSide
            )
        }
    }

    private fun VisualBounds.isAmbiguousHorizontalPosition(): Boolean {
        val centerBand = screenWidth * 0.10f
        val distanceFromCenter = kotlin.math.abs(centerX - screenWidth / 2)
        val widthRatio = (right - left).toFloat() / screenWidth.coerceAtLeast(1)
        return distanceFromCenter <= centerBand && widthRatio < 0.55f
    }
}
