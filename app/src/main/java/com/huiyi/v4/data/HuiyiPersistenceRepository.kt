package com.huiyi.v4.data

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.review.ChatReviewDraft

class HuiyiPersistenceRepository(
    private val dao: HuiyiDao
) {
    suspend fun saveScene(context: ChatSceneContext): Result<Unit> = runCatching {
        dao.insertMessageNodes(context.allMessages.map { it.toEntity() })
        dao.insertChatScene(
            ChatSceneEntity(
                id = context.id,
                contactId = context.contactId,
                payloadJson = "messages=${context.allMessages.size};decisionReady=true",
                completenessScore = context.contentCompleteness.score,
                createdAt = context.createdAt
            )
        )
    }

    suspend fun saveChatReviewDraft(draft: ChatReviewDraft): Result<Unit> = runCatching {
        dao.insertAppSetting(
            AppSettingEntity(
                key = "chat_review.${draft.contactKey}.${draft.id}",
                value = draft.toStorageJson(),
                updatedAt = draft.createdAt
            )
        )
        dao.insertAppSetting(
            AppSettingEntity(
                key = "chat_profile.${draft.contactKey}.latest",
                value = draft.toProfileSeedJson(),
                updatedAt = draft.createdAt
            )
        )
    }

    suspend fun saveLightListenMessages(
        appPackage: String,
        windowTitle: String?,
        messages: List<MessageNode>,
        observedAt: Long
    ): Result<Unit> = runCatching {
        if (messages.isEmpty()) return@runCatching
        val contactKey = contactKey(appPackage, windowTitle)
        dao.insertLightListenMessages(
            messages
                .sortedBy { it.createdAt }
                .map { it.toLightListenEntity(contactKey, appPackage, windowTitle, observedAt) }
        )
        dao.deleteLightListenMessagesOlderThan(observedAt - LIGHT_LISTEN_RETENTION_MS)
    }

    suspend fun lightListenTimeline(contactKey: String, limit: Int = 200): List<LightListenMessageEntity> =
        dao.getLightListenMessagesForContact(contactKey, limit)

    suspend fun saveReplyAttempt(attempt: ReplyAttempt): Result<Unit> = runCatching {
        dao.insertReplyAttempt(
            ReplyAttemptEntity(
                id = attempt.id,
                contactId = attempt.contactId,
                sceneId = attempt.sceneId,
                routeId = attempt.routeId,
                routeType = attempt.routeType.name,
                suggestedText = attempt.suggestedText,
                userAction = attempt.userAction.name,
                status = attempt.status.name,
                selectedAt = attempt.selectedAt,
                confirmedSentAt = attempt.confirmedSentAt,
                finalSentText = attempt.finalSentText
            )
        )
    }

    private fun MessageNode.toEntity(): MessageNodeEntity = MessageNodeEntity(
        id = id,
        contactId = contactId,
        speaker = speaker.name,
        contentJson = content.toStableString(),
        normalizedText = normalizedText,
        source = source.name,
        localSequence = localSequence,
        confidence = confidence,
        createdAt = createdAt,
        sceneId = sceneId
    )

    private fun MessageNode.toLightListenEntity(
        contactKey: String,
        appPackage: String,
        windowTitle: String?,
        observedAt: Long
    ): LightListenMessageEntity = LightListenMessageEntity(
        id = id,
        contactKey = contactKey,
        appPackage = appPackage,
        windowTitle = windowTitle,
        speaker = speaker.name,
        contentType = content.contentTypeName(),
        text = normalizedText,
        source = source.name,
        observedAt = observedAt,
        localSequence = localSequence,
        confidence = confidence,
        speakerConfidence = speakerConfidence,
        contentConfidence = contentConfidence,
        cloudHistoryFormatJson = toCloudHistoryFormatJson(contactKey, appPackage, windowTitle, observedAt),
        createdAt = createdAt
    )

    private fun MessageContent.toStableString(): String = when (this) {
        is MessageContent.Text -> "text:$text"
        is MessageContent.Voice -> "voice:$durationSeconds:$transcriptStatus:$transcriptText:$userSummary"
        is MessageContent.Image -> "image:$descriptionStatus:$descriptionText"
        is MessageContent.Video -> "video:$durationSeconds:$descriptionStatus:$descriptionText"
        is MessageContent.Sticker -> "sticker:$meaningStatus:$meaningText"
    }

    private fun MessageContent.contentTypeName(): String = when (this) {
        is MessageContent.Text -> "text"
        is MessageContent.Voice -> "voice"
        is MessageContent.Image -> "image"
        is MessageContent.Video -> "video"
        is MessageContent.Sticker -> "sticker"
    }

    private fun MessageNode.toCloudHistoryFormatJson(
        contactKey: String,
        appPackage: String,
        windowTitle: String?,
        observedAt: Long
    ): String = """
        {
          "schemaVersion": "huiyi-history-message-v1",
          "contactKey": "${json(contactKey)}",
          "appPackage": "${json(appPackage)}",
          "windowTitle": ${windowTitle?.let { "\"${json(it)}\"" } ?: "null"},
          "messageId": "${json(id)}",
          "speaker": "${json(speaker.name)}",
          "contentType": "${json(content.contentTypeName())}",
          "text": ${normalizedText?.let { "\"${json(it)}\"" } ?: "null"},
          "source": "${json(source.name)}",
          "authority": "AUXILIARY_TEXT_CONTEXT",
          "mayContainParserError": true,
          "cannotOverrideCurrentScreenshot": true,
          "observedAt": $observedAt,
          "createdAt": $createdAt,
          "confidence": $confidence,
          "speakerConfidence": $speakerConfidence,
          "contentConfidence": $contentConfidence
        }
    """.trimIndent()

    private fun contactKey(appPackage: String, windowTitle: String?): String =
        "${appPackage.trim()}|${windowTitle?.trim()?.replace(Regex("\\s+"), " ")?.take(80).orEmpty().ifBlank { "default" }}"

    private fun ChatReviewDraft.toStorageJson(): String = """
        {
          "id": "${json(id)}",
          "contactKey": "${json(contactKey)}",
          "contactDisplayName": "${json(contactDisplayName)}",
          "appPackage": "${json(appPackage)}",
          "windowTitle": "${json(windowTitle)}",
          "sceneId": "${json(sceneId)}",
          "createdAt": $createdAt,
          "source": "${json(source)}",
          "coCreationPoint": "${json(coCreationPoint)}",
          "userLikelyMistake": "${json(userLikelyMistake)}",
          "intensity": "${json(intensity)}",
          "riskLevel": "${json(riskLevel)}",
          "riskWarning": ${riskWarning?.let { "\"${json(it)}\"" } ?: "null"},
          "fallbackMove": ${fallbackMove?.let { "\"${json(it)}\"" } ?: "null"},
          "bestMove": "${json(bestMove)}",
          "recommendedReplyPreview": "${json(recommendedReplyPreview)}",
          "routeCount": $routeCount,
          "modelSource": "${json(modelSource)}",
          "profileHints": [${profileHints.joinToString(",") { "\"${json(it)}\"" }}]
        }
    """.trimIndent()

    private fun ChatReviewDraft.toProfileSeedJson(): String = """
        {
          "contactKey": "${json(contactKey)}",
          "contactDisplayName": "${json(contactDisplayName)}",
          "appPackage": "${json(appPackage)}",
          "lastReviewId": "${json(id)}",
          "updatedAt": $createdAt,
          "latestRiskLevel": "${json(riskLevel)}",
          "latestFallbackMove": ${fallbackMove?.let { "\"${json(it)}\"" } ?: "null"},
          "profileHints": [${profileHints.joinToString(",") { "\"${json(it)}\"" }}]
        }
    """.trimIndent()

    private fun json(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private companion object {
        const val LIGHT_LISTEN_RETENTION_MS = 14L * 24L * 60L * 60L * 1000L
    }
}
