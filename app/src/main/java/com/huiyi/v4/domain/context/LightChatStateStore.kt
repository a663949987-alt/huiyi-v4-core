package com.huiyi.v4.domain.context

import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageDeliveryStatus
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker

enum class NextMoveType {
    WAIT,
    RECEIVE_OTHER,
    EXPRESS_SELF,
    CO_CREATE_MEANING,
    LIGHTEN_MOOD,
    WITHDRAW
}

data class SelfExpressionOpportunity(
    val exists: Boolean,
    val type: NextMoveType?,
    val matchedPersonaCardIds: List<String>,
    val reason: String?,
    val suggestedIntensity: InfluenceIntensity,
    val risk: RiskLevel
)

data class LightChatMessageSummary(
    val id: String,
    val speaker: Speaker,
    val text: String?,
    val contentType: String,
    val source: String,
    val localSequence: Long,
    val createdAt: Long,
    val messageStatus: MessageDeliveryStatus
)

data class LightChatMessageStatusMetadata(
    val id: String,
    val metadataType: MetadataType?,
    val deliveryStatus: MessageDeliveryStatus,
    val readStatus: MessageDeliveryStatus,
    val attachedToMessageId: String?,
    val reason: String?
)

data class LightChatSessionBinding(
    val nextSentenceSessionId: String?,
    val preAnalysisSnapshotId: String?,
    val panelSessionId: String?,
    val chatPackage: String?,
    val chatWindowHash: String?
)

data class LightChatSafetyFlags(
    val longTermRawChatStorage: Boolean = false,
    val autoSend: Boolean = false,
    val rawPrivateChatUploadedToGithub: Boolean = false
)

data class LightChatStableSnapshot(
    val appPackage: String?,
    val windowTitle: String?,
    val chatKey: String?,
    val capturedAt: Long,
    val recentEffectiveMessages: List<LightChatMessageSummary>,
    val lastUserMessage: LightChatMessageSummary?,
    val lastOtherMessage: LightChatMessageSummary?,
    val messageStatusMetadata: List<LightChatMessageStatusMetadata>,
    val sessionBinding: LightChatSessionBinding,
    val selfExpressionOpportunity: SelfExpressionOpportunity,
    val safetyFlags: LightChatSafetyFlags = LightChatSafetyFlags()
)

class LightChatStateStore(
    private val maxRecentEffectiveMessages: Int = 12
) {
    fun buildStableSnapshot(
        appPackage: String?,
        windowTitle: String?,
        messages: List<MessageNode>,
        capturedAt: Long = System.currentTimeMillis(),
        nextSentenceSessionId: String? = null,
        preAnalysisSnapshotId: String? = null,
        panelSessionId: String? = null,
        chatWindowHash: String? = null,
        matchedPersonaCardIds: List<String> = emptyList()
    ): LightChatStableSnapshot {
        val effective = messages
            .filter { it.isEffectiveChatMessage && (it.speaker == Speaker.ME || it.speaker == Speaker.OTHER) }
            .sortedWith(compareBy<MessageNode> { it.finalVisualOrder ?: Int.MAX_VALUE }.thenBy { it.localSequence })
        val recent = effective.takeLast(maxRecentEffectiveMessages.coerceIn(6, 12))
        val lastEffective = effective.lastOrNull()

        return LightChatStableSnapshot(
            appPackage = appPackage,
            windowTitle = windowTitle,
            chatKey = chatKey(appPackage, windowTitle),
            capturedAt = capturedAt,
            recentEffectiveMessages = recent.map { it.toSummary() },
            lastUserMessage = effective.lastOrNull { it.speaker == Speaker.ME }?.toSummary(),
            lastOtherMessage = effective.lastOrNull { it.speaker == Speaker.OTHER }?.toSummary(),
            messageStatusMetadata = messages
                .filter { it.hasMessageStatusMetadata() }
                .map { it.toStatusMetadata() },
            sessionBinding = LightChatSessionBinding(
                nextSentenceSessionId = nextSentenceSessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                panelSessionId = panelSessionId,
                chatPackage = appPackage,
                chatWindowHash = chatWindowHash
            ),
            selfExpressionOpportunity = selfExpressionOpportunityFor(
                lastEffective = lastEffective,
                matchedPersonaCardIds = matchedPersonaCardIds
            )
        )
    }

    private fun selfExpressionOpportunityFor(
        lastEffective: MessageNode?,
        matchedPersonaCardIds: List<String>
    ): SelfExpressionOpportunity {
        if (lastEffective?.speaker != Speaker.OTHER) {
            return SelfExpressionOpportunity(
                exists = false,
                type = null,
                matchedPersonaCardIds = emptyList(),
                reason = "last_speaker_not_other",
                suggestedIntensity = InfluenceIntensity.LOW,
                risk = RiskLevel.LOW
            )
        }

        val text = lastEffective.normalizedText.orEmpty()
        val matchedTopic = expressionTopics.firstOrNull { text.contains(it, ignoreCase = true) }
        if (matchedTopic == null) {
            return SelfExpressionOpportunity(
                exists = false,
                type = NextMoveType.RECEIVE_OTHER,
                matchedPersonaCardIds = matchedPersonaCardIds,
                reason = "no_planning_reality_stability_or_future_topic",
                suggestedIntensity = InfluenceIntensity.LOW,
                risk = RiskLevel.LOW
            )
        }

        val type = if (matchedTopic in coCreationTopics) {
            NextMoveType.CO_CREATE_MEANING
        } else {
            NextMoveType.EXPRESS_SELF
        }
        return SelfExpressionOpportunity(
            exists = true,
            type = type,
            matchedPersonaCardIds = matchedPersonaCardIds,
            reason = "last_other_mentions_$matchedTopic",
            suggestedIntensity = InfluenceIntensity.MEDIUM,
            risk = RiskLevel.LOW
        )
    }

    private fun MessageNode.toSummary(): LightChatMessageSummary = LightChatMessageSummary(
        id = id,
        speaker = speaker,
        text = normalizedText,
        contentType = when (content) {
            is MessageContent.Text -> "Text"
            is MessageContent.Voice -> "Voice"
            is MessageContent.Image -> "Image"
            is MessageContent.Video -> "Video"
            is MessageContent.Sticker -> "Sticker"
        },
        source = source.name,
        localSequence = localSequence,
        createdAt = createdAt,
        messageStatus = when {
            attachedReadStatus != MessageDeliveryStatus.NONE -> attachedReadStatus
            attachedDeliveryStatus != MessageDeliveryStatus.NONE -> attachedDeliveryStatus
            else -> MessageDeliveryStatus.NONE
        }
    )

    private fun MessageNode.hasMessageStatusMetadata(): Boolean =
        metadataType in statusMetadataTypes ||
            statusArtifact != null ||
            attachedDeliveryStatus != MessageDeliveryStatus.NONE ||
            attachedReadStatus != MessageDeliveryStatus.NONE

    private fun MessageNode.toStatusMetadata(): LightChatMessageStatusMetadata = LightChatMessageStatusMetadata(
        id = statusArtifact?.id ?: id,
        metadataType = metadataType,
        deliveryStatus = statusArtifact?.status ?: attachedDeliveryStatus,
        readStatus = attachedReadStatus,
        attachedToMessageId = statusArtifact?.attachedToMessageId,
        reason = statusArtifact?.reason ?: speakerReason
    )

    private fun chatKey(appPackage: String?, windowTitle: String?): String? {
        val pkg = appPackage?.trim().orEmpty()
        if (pkg.isBlank()) return null
        val title = windowTitle?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.take(80)
            .orEmpty()
            .ifBlank { "default" }
        return "$pkg|$title"
    }

    private companion object {
        val statusMetadataTypes = setOf(
            MetadataType.READ_RECEIPT,
            MetadataType.DELIVERY_STATUS,
            MetadataType.SEND_STATUS,
            MetadataType.MESSAGE_STATUS_ICON
        )
        val expressionTopics = listOf(
            "planning",
            "reality",
            "stable",
            "stability",
            "future",
            "plan",
            "\u89c4\u5212",
            "\u73b0\u5b9e",
            "\u7a33\u5b9a",
            "\u672a\u6765",
            "\u4ee5\u540e",
            "\u5b89\u6392",
            "\u957f\u671f"
        )
        val coCreationTopics = setOf(
            "planning",
            "future",
            "plan",
            "\u89c4\u5212",
            "\u672a\u6765",
            "\u4ee5\u540e",
            "\u5b89\u6392",
            "\u957f\u671f"
        )
    }
}
