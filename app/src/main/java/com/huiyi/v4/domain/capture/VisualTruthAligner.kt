package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds

data class VisualTruthAlignmentResult(
    val messages: List<MessageNode>,
    val accessibilityBoundsProjected: Boolean,
    val ocrUsed: Boolean,
    val visualTruthAvailable: Boolean,
    val conflictCount: Int,
    val visualSpeakerFallbackCount: Int
)

class VisualTruthAligner(
    private val screenWidth: Int
) {
    fun align(
        messages: List<MessageNode>,
        ocrLines: List<VisualTextLine> = emptyList(),
        ocrUsed: Boolean = false
    ): VisualTruthAlignmentResult {
        val aligned = messages.map { message ->
            val projected = message.bubbleBounds ?: message.rowBounds?.takeUnless { it.isFullWidthRow() } ?: message.textBounds ?: message.bounds
            val visualSide = projected?.projectedSide()
            val accessibilitySide = message.accessibilitySideFromParser()
            val conflict = accessibilitySide in setOf("left", "right") &&
                visualSide in setOf("left", "right") &&
                accessibilitySide != visualSide
            val conflictReason = when {
                conflict -> "accessibility_visual_mismatch"
                projected == null -> "visual_projection_unavailable"
                else -> null
            }
            message.copy(
                visualDebugBoxDrawn = projected != null,
                projectedBox = projected,
                accessibilitySide = accessibilitySide,
                visualProjectedSide = visualSide ?: "unknown",
                visualConflict = conflict,
                visualConflictReason = conflictReason
            )
        }.let { VisualSpeakerFallback(screenWidth).apply(it) }
        return VisualTruthAlignmentResult(
            messages = aligned,
            accessibilityBoundsProjected = aligned.any { it.projectedBox != null },
            ocrUsed = ocrUsed && ocrLines.isNotEmpty(),
            visualTruthAvailable = aligned.any { it.visualProjectedSide in setOf("left", "right") },
            conflictCount = aligned.count { it.visualConflict },
            visualSpeakerFallbackCount = aligned.count { it.visualSpeakerFallbackUsed }
        )
    }

    private fun MessageNode.accessibilitySideFromParser(): String {
        inferredSide?.takeIf { it in setOf("left", "right", "unknown") }?.let { return it }
        return when (speaker) {
            Speaker.ME -> "right"
            Speaker.OTHER -> "left"
            Speaker.SYSTEM -> "system"
            Speaker.UNKNOWN -> "unknown"
        }
    }

    private fun VisualBounds.projectedSide(): String {
        val leftMargin = left
        val rightMargin = screenWidth - right
        val strongDelta = (screenWidth * 0.08f).toInt()
        return when {
            rightMargin + strongDelta < leftMargin -> "right"
            leftMargin + strongDelta < rightMargin -> "left"
            else -> "unknown"
        }
    }

    private fun VisualBounds.isFullWidthRow(): Boolean {
        return (right - left).toFloat() / screenWidth.coerceAtLeast(1) >= 0.84f
    }
}

class VisualSpeakerFallback(
    private val screenWidth: Int
) {
    fun apply(messages: List<MessageNode>): List<MessageNode> {
        return messages.map { message ->
            val visualSide = message.visualProjectedSide
            val noConflict = !message.visualConflict
            when {
                message.speakerConfidence >= 85 && noConflict -> message
                message.speaker == Speaker.UNKNOWN && visualSide == "right" -> message.visualSpeaker(Speaker.ME, "visual_projected_right")
                message.speaker == Speaker.UNKNOWN && visualSide == "left" -> message.visualSpeaker(Speaker.OTHER, "visual_projected_left")
                message.speaker == Speaker.OTHER && message.possibleSpeakerConflict && visualSide == "right" ->
                    message.visualSpeaker(Speaker.ME, "visual_projected_right_semantic_conflict")
                else -> message
            }
        }
    }

    private fun MessageNode.visualSpeaker(speaker: Speaker, reason: String): MessageNode {
        return copy(
            speaker = speaker,
            speakerConfidence = 75,
            inferredSide = if (speaker == Speaker.ME) "right" else "left",
            speakerReason = reason,
            finalDecisionSource = reason,
            visualSpeakerFallbackUsed = true,
            visualConflict = visualConflict || possibleSpeakerConflict,
            visualConflictReason = visualConflictReason ?: if (possibleSpeakerConflict) "parser_side_conflict" else null
        )
    }
}
