package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.DescriptionStatus
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus
import com.huiyi.v4.domain.model.VisualBounds

class LiaoqiRealParser(
    private val screenWidth: Int = 1080,
    private val meOnRight: Boolean = true
) {
    private val metadataFilter = MetadataMessageFilter()

    fun parse(bubbles: List<VisualBubble>, source: MessageSource = MessageSource.ACCESSIBILITY_CURRENT_SCREEN): List<MessageNode> {
        return bubbles.mapIndexed { rawIndex, bubble -> rawIndex to bubble }
            .sortedWith(compareBy<Pair<Int, VisualBubble>> { it.second.visualTop() }.thenBy { it.second.visualLeft() })
            .mapIndexed { visualIndex, (rawIndex, bubble) ->
                parseBubble(rawIndex, visualIndex, bubble, source)
            }
    }

    private fun parseBubble(
        rawIndex: Int,
        visualIndex: Int,
        bubble: VisualBubble,
        source: MessageSource
    ): MessageNode {
        val rawText = bubble.text.orEmpty()
        val actualText = rawText.actualReplyText()
        val metadataType = metadataFilter.classify(actualText)
        val isMetadata = metadataType != MetadataType.NONE
        val rowBounds = chooseRowBounds(bubble)
        val bubbleBounds = chooseBubbleBounds(bubble)
        val sideDecision = if (isMetadata) SideDecision("system", "metadata_filter") else inferSide(bubble, bubbleBounds, rowBounds)
        val speaker = when {
            isMetadata -> Speaker.SYSTEM
            sideDecision.side == "unknown" -> Speaker.UNKNOWN
            sideDecision.side == "right" && meOnRight -> Speaker.ME
            sideDecision.side == "right" -> Speaker.OTHER
            sideDecision.side == "left" && meOnRight -> Speaker.OTHER
            else -> Speaker.ME
        }
        val isVoice = !isMetadata && (rawText.contains("语音") || rawText.contains("秒"))
        val isSticker = !isMetadata && (rawText.contains("[sticker]") || rawText.contains("表情包") || rawText.contains("贴纸"))
        val isImage = !isMetadata && !isSticker && (rawText.contains("[image]") || rawText.contains("图片"))
        val sideMarginBounds = bubbleBounds ?: rowBounds ?: bubble.textBounds
        return MessageNode(
            id = "liaoqi-${bubble.id}",
            contactId = null,
            speaker = speaker,
            content = when {
                isVoice -> MessageContent.Voice(
                    durationSeconds = rawText.filter(Char::isDigit).toIntOrNull(),
                    transcriptStatus = TranscriptStatus.MISSING,
                    transcriptText = null,
                    userSummary = null
                )
                isImage -> MessageContent.Image(DescriptionStatus.MISSING, null)
                isSticker -> MessageContent.Sticker(DescriptionStatus.MISSING, null)
                else -> MessageContent.Text(actualText)
            },
            normalizedText = if (isVoice || isImage || isSticker) null else actualText,
            source = source,
            localSequence = visualIndex.toLong(),
            confidence = bubble.confidence,
            speakerConfidence = when {
                isMetadata -> 100
                speaker == Speaker.UNKNOWN -> 30
                sideDecision.reason.startsWith("liaoqi_avatar") -> 88
                sideDecision.reason.startsWith("liaoqi_bubble") -> 84
                else -> 78
            },
            contentConfidence = bubble.confidence,
            bounds = bubbleBounds ?: rowBounds ?: bubble.textBounds,
            pageIndex = 0,
            createdAt = System.currentTimeMillis() + visualIndex,
            sceneId = null,
            speakerReason = when {
                isMetadata -> metadataReason(metadataType)
                speaker == Speaker.UNKNOWN -> sideDecision.reason
                else -> sideDecision.reason
            },
            parserName = "LiaoqiRealParser",
            isEffectiveChatMessage = !isMetadata && speaker != Speaker.SYSTEM,
            metadataType = metadataType,
            rowBounds = rowBounds,
            textBounds = bubble.textBounds,
            inferredSide = sideDecision.side,
            parentBounds = bubble.parentBounds,
            bubbleBounds = bubbleBounds,
            ancestorBoundsChain = bubble.ancestorBoundsChain,
            unknownReason = if (!isMetadata && speaker == Speaker.UNKNOWN) sideDecision.reason else null,
            rawNodeOrder = rawIndex + 1,
            finalVisualOrder = visualIndex + 1,
            sideMarginLeft = sideMarginBounds?.left,
            sideMarginRight = sideMarginBounds?.let { screenWidth - it.right },
            finalDecisionSource = sideDecision.reason,
            possibleSpeakerConflict = speaker == Speaker.OTHER && actualText.hasPossibleSpeakerConflict()
        )
    }

    private fun inferSide(bubble: VisualBubble, bubbleBounds: VisualBounds?, rowBounds: VisualBounds?): SideDecision {
        val avatarSide = bubble.avatarBounds?.edgeSide()
        if (avatarSide == "left" || avatarSide == "right") return SideDecision(avatarSide, "liaoqi_avatar_$avatarSide")

        val primaryBounds = listOfNotNull(
            bubbleBounds,
            bubble.parentBounds?.takeUnless { it.isFullWidthRow() },
            bubble.ancestorBoundsChain.asReversed().firstOrNull { it.chatBubbleLike() },
            rowBounds?.takeUnless { it.isFullWidthRow() }
        ).distinct()
        primaryBounds.forEach { bounds ->
            val side = bounds.edgeSide()
            if (side == "left" || side == "right") return SideDecision(side, "liaoqi_bubble_edge_$side")
        }
        if (primaryBounds.isNotEmpty()) {
            val best = primaryBounds.first()
            return SideDecision("unknown", "liaoqi_ambiguous_bubble_bounds ${best.unknownReason()}")
        }

        val textBounds = bubble.textBounds
        if (textBounds != null) {
            val side = textBounds.edgeSide()
            if (side == "left" || side == "right") return SideDecision(side, "liaoqi_text_edge_$side")
        }

        return SideDecision("unknown", (bubbleBounds ?: rowBounds ?: textBounds)?.unknownReason() ?: "missing_visual_bounds")
    }

    private fun chooseRowBounds(bubble: VisualBubble): VisualBounds? {
        return bubble.ancestorBoundsChain.asReversed().firstOrNull { it.isFullWidthRow() && it.height() in 36..420 }
            ?: bubble.parentBounds
            ?: bubble.rowBounds
    }

    private fun chooseBubbleBounds(bubble: VisualBubble): VisualBounds? {
        val candidates = buildList {
            addAll(bubble.ancestorBoundsChain.asReversed())
            add(bubble.parentBounds)
            add(bubble.contentBounds)
            add(bubble.bubbleBounds)
            add(bubble.textBounds)
        }.filterNotNull().distinct()
        return candidates.firstOrNull { it.chatBubbleLike() }
            ?: candidates.firstOrNull { !it.isFullWidthRow() && it.width() >= 32 && it.height() >= 16 }
            ?: bubble.textBounds
    }

    private fun VisualBounds.chatBubbleLike(): Boolean {
        val widthRatio = width().toFloat() / screenWidth.coerceAtLeast(1)
        return widthRatio in 0.12f..0.78f && width() >= 56 && height() >= 24
    }

    private fun VisualBounds.edgeSide(): String {
        val leftMargin = left
        val rightMargin = screenWidth - right
        val strongDelta = (screenWidth * 0.08f).toInt()
        return when {
            rightMargin + strongDelta < leftMargin -> "right"
            leftMargin + strongDelta < rightMargin -> "left"
            else -> "unknown"
        }
    }

    private fun VisualBounds.isFullWidthRow(): Boolean = width().toFloat() / screenWidth.coerceAtLeast(1) >= 0.84f

    private fun VisualBounds.width(): Int = right - left

    private fun VisualBounds.height(): Int = bottom - top

    private fun VisualBounds.unknownReason(): String {
        val leftMargin = left
        val rightMargin = screenWidth - right
        val widthRatio = width().toFloat() / screenWidth.coerceAtLeast(1)
        return "liaoqi_ambiguous_bounds leftMargin=$leftMargin rightMargin=$rightMargin widthRatio=${"%.2f".format(widthRatio)}"
    }

    private fun VisualBubble.visualTop(): Int = listOfNotNull(textBounds, bubbleBounds, parentBounds, rowBounds)
        .minOfOrNull { it.top } ?: Int.MAX_VALUE

    private fun VisualBubble.visualLeft(): Int = listOfNotNull(textBounds, bubbleBounds, parentBounds, rowBounds)
        .minOfOrNull { it.left } ?: Int.MAX_VALUE

    private fun metadataReason(metadataType: MetadataType?): String = when (metadataType) {
        MetadataType.TIME -> "time_metadata"
        MetadataType.DATE -> "date_metadata"
        MetadataType.ONLINE_STATUS -> "online_status_metadata"
        MetadataType.UI_CONTROL -> "ui_control_metadata"
        MetadataType.SYSTEM_NOTICE -> "system_notice_metadata"
        else -> "header_metadata"
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
