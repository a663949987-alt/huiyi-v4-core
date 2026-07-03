package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.DescriptionStatus
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.MetadataType
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
    val parentBounds: VisualBounds? = null,
    val ancestorBoundsChain: List<VisualBounds> = emptyList(),
    val confidence: Int = 85
)

class GenericVisualBubbleParser(
    private val screenWidth: Int = 1080,
    private val meOnRight: Boolean = true
) {
    private val metadataFilter = MetadataMessageFilter()

    fun parse(bubbles: List<VisualBubble>, source: MessageSource = MessageSource.ACCESSIBILITY_CURRENT_SCREEN): List<MessageNode> {
        return bubbles.mapIndexed { rawIndex, bubble -> rawIndex to bubble }
            .sortedWith(compareBy<Pair<Int, VisualBubble>> { it.second.visualTop() }.thenBy { it.second.visualLeft() })
            .mapIndexed { visualIndex, (rawIndex, bubble) ->
            val selectedBounds = bubble.bubbleBounds
                ?: bubble.rowBounds?.takeUnless { it.isFullWidthRow() }
                ?: bubble.contentBounds
                ?: bubble.textBounds
                ?: bubble.parentBounds?.takeUnless { it.isFullWidthRow() }
                ?: bubble.avatarBounds
            val rawText = bubble.text.orEmpty()
            val actualText = rawText.actualReplyText()
            val metadataType = metadataFilter.classify(actualText)
            val isMetadata = metadataType != MetadataType.NONE
            val sideDecision = inferSide(bubble)
            val inferredSide = sideDecision.side
            val speaker = if (isMetadata) {
                Speaker.SYSTEM
            } else if (sideDecision.side == "unknown") {
                Speaker.UNKNOWN
            } else {
                val rightSide = sideDecision.side == "right"
                when {
                    rightSide && meOnRight -> Speaker.ME
                    rightSide && !meOnRight -> Speaker.OTHER
                    !rightSide && meOnRight -> Speaker.OTHER
                    else -> Speaker.ME
                }
            }
            val isVoice = !isMetadata && (
                rawText.contains("璇煶") ||
                    rawText.contains("语音") ||
                    rawText.contains("绉?") ||
                    rawText.contains("秒")
                )
            val isSticker = !isMetadata && (
                rawText.contains("[sticker]") ||
                    rawText.contains("表情包") ||
                    rawText.contains("贴纸")
                )
            val isImage = !isMetadata && !isSticker && (
                rawText.contains("[image]") ||
                    rawText.contains("图片") ||
                    rawText.contains("鍥剧墖")
                )
            val sideMarginBounds = selectedBounds ?: bubble.textBounds ?: bubble.parentBounds
            MessageNode(
                id = "bubble-${bubble.id}",
                contactId = null,
                speaker = speaker,
                content = when {
                    isVoice -> MessageContent.Voice(
                        durationSeconds = rawText.filter(Char::isDigit).toIntOrNull(),
                        transcriptStatus = TranscriptStatus.MISSING,
                        transcriptText = null,
                        userSummary = null
                    )
                    isImage -> MessageContent.Image(
                        descriptionStatus = DescriptionStatus.MISSING,
                        descriptionText = null
                    )
                    isSticker -> MessageContent.Sticker(
                        meaningStatus = DescriptionStatus.MISSING,
                        meaningText = null
                    )
                    else -> MessageContent.Text(actualText)
                },
                normalizedText = when {
                    isVoice || isImage || isSticker -> null
                    else -> actualText
                },
                source = source,
                localSequence = visualIndex.toLong(),
                confidence = bubble.confidence,
                speakerConfidence = when {
                    isMetadata -> 100
                    speaker == Speaker.UNKNOWN -> 30
                    else -> 82
                },
                contentConfidence = bubble.confidence,
                bounds = selectedBounds,
                pageIndex = 0,
                createdAt = System.currentTimeMillis() + visualIndex,
                sceneId = null,
                speakerReason = when {
                    isMetadata -> when (metadataType) {
                        MetadataType.TIME,
                        MetadataType.DATE -> "time_metadata"
                        MetadataType.ONLINE_STATUS -> "online_status_metadata"
                        MetadataType.UI_CONTROL -> "ui_control_metadata"
                        MetadataType.SYSTEM_NOTICE -> "system_notice_metadata"
                        else -> "header_metadata"
                    }
                    sideDecision.side == "unknown" -> "ambiguous_center_bounds"
                    sideDecision.side == "right" -> sideDecision.reason
                    sideDecision.side == "left" -> sideDecision.reason
                    else -> "unknown_visual_bounds"
                },
                parserName = "GenericVisualBubbleParser",
                isEffectiveChatMessage = !isMetadata && speaker in setOf(Speaker.ME, Speaker.OTHER),
                metadataType = metadataType,
                rowBounds = bubble.rowBounds,
                textBounds = bubble.textBounds,
                inferredSide = inferredSide,
                parentBounds = bubble.parentBounds,
                bubbleBounds = bubble.bubbleBounds,
                ancestorBoundsChain = bubble.ancestorBoundsChain,
                unknownReason = if (!isMetadata && sideDecision.side == "unknown") sideDecision.reason else null,
                rawNodeOrder = rawIndex + 1,
                finalVisualOrder = visualIndex + 1,
                sideMarginLeft = sideMarginBounds?.left,
                sideMarginRight = sideMarginBounds?.let { screenWidth - it.right },
                finalDecisionSource = if (isMetadata) "metadata_filter" else sideDecision.reason,
                possibleSpeakerConflict = speaker == Speaker.OTHER && actualText.hasPossibleSpeakerConflict()
            )
        }
    }

    private fun VisualBubble.visualTop(): Int = listOfNotNull(textBounds, bubbleBounds, rowBounds, parentBounds, avatarBounds)
        .minOfOrNull { it.top } ?: Int.MAX_VALUE

    private fun VisualBubble.visualLeft(): Int = listOfNotNull(textBounds, bubbleBounds, rowBounds, parentBounds, avatarBounds)
        .minOfOrNull { it.left } ?: Int.MAX_VALUE

    private fun inferSide(bubble: VisualBubble): SideDecision {
        val candidateBounds = listOfNotNull(
            bubble.bubbleBounds,
            bubble.rowBounds?.takeUnless { it.isFullWidthRow() },
            bubble.contentBounds,
            bubble.parentBounds?.takeUnless { it.isFullWidthRow() },
            bubble.textBounds,
            bubble.avatarBounds
        ).distinct()
        for (bounds in candidateBounds) {
            val side = bounds.edgeSide()
            if (side != "unknown") return SideDecision(side, "bubble_edge_$side")
        }
        val best = candidateBounds.firstOrNull() ?: return SideDecision("unknown", "missing_visual_bounds")
        return SideDecision("unknown", best.unknownReason())
    }

    private fun VisualBounds.edgeSide(): String {
        val edgeDeltaStrong = screenWidth * 0.10f
        val edgeDeltaWeak = screenWidth * 0.06f
        val sideMarginWeak = screenWidth * 0.08f
        val leftMargin = left.toFloat()
        val rightMargin = (screenWidth - right).toFloat()
        return when {
            rightMargin + edgeDeltaStrong < leftMargin -> "right"
            leftMargin + edgeDeltaStrong < rightMargin -> "left"
            rightMargin + edgeDeltaWeak < leftMargin && rightMargin <= sideMarginWeak -> "right"
            leftMargin + edgeDeltaWeak < rightMargin && leftMargin <= sideMarginWeak -> "left"
            else -> "unknown"
        }
    }

    private fun VisualBounds.isFullWidthRow(): Boolean {
        val widthRatio = (right - left).toFloat() / screenWidth.coerceAtLeast(1)
        return widthRatio >= 0.86f
    }

    private fun VisualBounds.unknownReason(): String {
        val leftMargin = left
        val rightMargin = screenWidth - right
        val widthRatio = (right - left).toFloat() / screenWidth.coerceAtLeast(1)
        return "ambiguous_center_or_balanced_bounds leftMargin=$leftMargin rightMargin=$rightMargin widthRatio=${"%.2f".format(widthRatio)}"
    }

    private fun String.actualReplyText(): String {
        val marker = "实际回复："
        val index = indexOf(marker)
        return if (index >= 0) substring(index + marker.length).trim() else this
    }

    private fun String.hasPossibleSpeakerConflict(): Boolean {
        val markers = listOf("我早上出操", "我进入机要室", "我查寝", "我今天战士们聚餐", "我们单位", "我不能用手机")
        return markers.any { contains(it) }
    }

    private data class SideDecision(
        val side: String,
        val reason: String
    )
}
