package com.huiyi.v4.domain.context

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.CoCreationOpportunity
import com.huiyi.v4.domain.model.CoCreationType
import com.huiyi.v4.domain.model.ContentCompleteness
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MissingContextType
import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.model.ReplyOutcome
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.Turn
import com.huiyi.v4.domain.model.TurnType
import com.huiyi.v4.domain.model.UserPersonaContext
import com.huiyi.v4.domain.model.UserPersonaCorpus
import java.util.UUID

class ContextAssembler {
    fun assemble(
        currentScreenMessages: List<MessageNode>,
        contextBackfillMessages: List<MessageNode> = emptyList(),
        recentMemoryMessages: List<MessageNode> = emptyList(),
        lastReplyAttempts: List<ReplyAttempt> = emptyList(),
        lastOutcomes: List<ReplyOutcome> = emptyList(),
        userPersonaCorpus: UserPersonaCorpus? = null
    ): ChatSceneContext {
        val merged = (contextBackfillMessages + currentScreenMessages + recentMemoryMessages)
            .distinctBy { it.id }
            .sortedBy { it.localSequence }
        val completeness = calculateCompleteness(merged)
        val turns = mergeTurns(merged)
        val opportunity = detectCoCreation(merged)

        return ChatSceneContext(
            id = "scene-${UUID.randomUUID()}",
            contactId = merged.firstOrNull { it.contactId != null }?.contactId,
            currentScreenMessages = currentScreenMessages,
            backfillMessages = contextBackfillMessages,
            recentMemoryMessages = recentMemoryMessages,
            turns = turns,
            lastReplyAttempts = lastReplyAttempts,
            lastOutcomes = lastOutcomes,
            userPersonaContext = userPersonaCorpus?.let {
                UserPersonaContext(it.id, it.enabled, it.name)
            },
            contentCompleteness = completeness,
            coCreationOpportunity = opportunity,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun calculateCompleteness(messages: List<MessageNode>): ContentCompleteness {
        val missing = mutableListOf<MissingContextType>()
        val effective = messages.filter { it.speaker != Speaker.SYSTEM }
        if (effective.size < 4) missing += MissingContextType.NOT_ENOUGH_MESSAGES
        if (effective.firstOrNull()?.speaker == Speaker.OTHER && effective.size <= 5) {
            missing += MissingContextType.PREVIOUS_TURN_MISSING
        }
        if (messages.any { it.speaker == Speaker.UNKNOWN }) missing += MissingContextType.UNKNOWN_SPEAKER
        if (messages.any { it.contentConfidence < 60 }) missing += MissingContextType.OCR_LOW_CONFIDENCE
        if (messages.any { it.content is MessageContent.Voice && it.content.transcriptStatus == com.huiyi.v4.domain.model.TranscriptStatus.MISSING }) {
            missing += MissingContextType.VOICE_WITHOUT_TRANSCRIPT
        }
        val score = (100 - missing.distinct().size * 18).coerceIn(30, 100)
        return ContentCompleteness(
            score = score,
            canDeepAnalyze = score >= 75 && MissingContextType.VOICE_WITHOUT_TRANSCRIPT !in missing,
            missingTypes = missing.distinct(),
            reason = if (missing.isEmpty()) "当前上下文足够判断。" else "缺少：" + missing.distinct().joinToString()
        )
    }

    private fun mergeTurns(messages: List<MessageNode>): List<Turn> {
        if (messages.isEmpty()) return emptyList()
        val turns = mutableListOf<Turn>()
        var bucket = mutableListOf<MessageNode>()
        for (message in messages) {
            if (bucket.isNotEmpty() && bucket.last().speaker != message.speaker) {
                turns += bucket.toTurn()
                bucket = mutableListOf()
            }
            bucket += message
        }
        if (bucket.isNotEmpty()) turns += bucket.toTurn()
        return turns
    }

    private fun List<MessageNode>.toTurn(): Turn = Turn(
        id = "turn-${first().id}",
        speaker = first().speaker,
        messageIds = map { it.id },
        summary = joinToString(" ") { it.normalizedText.orEmpty() }.take(80),
        startedAt = first().createdAt,
        endedAt = last().createdAt,
        turnType = detectTurnType(joinToString(" ") { it.normalizedText.orEmpty() })
    )

    private fun detectTurnType(text: String): TurnType = when {
        listOf("难过", "累", "委屈", "不开心").any(text::contains) -> TurnType.VULNERABILITY
        listOf("以后", "认真", "负责", "安全感").any(text::contains) -> TurnType.RELATIONSHIP_TEST
        listOf("别", "不聊", "算了").any(text::contains) -> TurnType.BOUNDARY
        else -> TurnType.UNKNOWN
    }

    private fun detectCoCreation(messages: List<MessageNode>): CoCreationOpportunity {
        val text = messages.takeLast(8).joinToString(" ") { it.normalizedText.orEmpty() }
        val exists = listOf("我们", "以后", "一起", "习惯", "约定").any(text::contains)
        return CoCreationOpportunity(
            exists = exists,
            type = if (exists) CoCreationType.SHARED_EXPECTATION else CoCreationType.NO_OPPORTUNITY,
            unfinishedMeaning = if (exists) "双方正在试探一种共同节奏。" else null,
            bestMove = if (exists) "把共同期待落到一个小而具体的动作。" else null,
            avoidMoves = if (exists) listOf("不要空口承诺", "不要急着证明自己") else emptyList(),
            confidence = if (exists) 72 else 45
        )
    }
}
