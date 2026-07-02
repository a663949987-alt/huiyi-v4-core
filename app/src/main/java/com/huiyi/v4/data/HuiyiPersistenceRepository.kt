package com.huiyi.v4.data

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.ReplyAttempt

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

    private fun MessageContent.toStableString(): String = when (this) {
        is MessageContent.Text -> "text:$text"
        is MessageContent.Voice -> "voice:$durationSeconds:$transcriptStatus:$transcriptText:$userSummary"
        is MessageContent.Image -> "image:$descriptionStatus:$descriptionText"
        is MessageContent.Video -> "video:$durationSeconds:$descriptionStatus:$descriptionText"
        is MessageContent.Sticker -> "sticker:$meaningStatus:$meaningText"
    }
}
