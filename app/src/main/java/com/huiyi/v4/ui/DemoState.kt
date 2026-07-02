package com.huiyi.v4.ui

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TranscriptStatus
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine

data class HuiyiDemoState(
    val personaEnabled: Boolean,
    val messages: List<MessageNode>
) {
    val persona = DefaultPersonaCorpus.soldier(personaEnabled)
    val context = ContextAssembler().assemble(messages, userPersonaCorpus = persona)
    val decision: TacticalDecision = TacticalDecisionEngine().decide(context)
    val routes = ReplyRouteGenerator().generate(context, decision)
    val voiceMessages = context.allMessages.filter { it.content is MessageContent.Voice }

    fun togglePersona(): HuiyiDemoState = copy(personaEnabled = !personaEnabled)

    fun withVoiceSummary(summary: String): HuiyiDemoState {
        val updated = messages.map {
            val voice = it.content as? MessageContent.Voice
            if (voice != null && voice.transcriptStatus == TranscriptStatus.MISSING) {
                it.copy(
                    content = voice.copy(transcriptStatus = TranscriptStatus.USER_SUMMARY, userSummary = summary),
                    normalizedText = summary,
                    source = MessageSource.MANUAL_VOICE_SUMMARY,
                    contentConfidence = 92
                )
            } else {
                it
            }
        }
        return copy(messages = updated)
    }
}

fun sampleState(): HuiyiDemoState = HuiyiDemoState(
    personaEnabled = true,
    messages = listOf(
        sampleText("m1", Speaker.OTHER, "今天店里真的好忙，有点累", 1),
        sampleText("m2", Speaker.ME, "那你先喝点水，别一直硬撑", 2),
        sampleVoice("m3", Speaker.OTHER, 18, 3)
    )
)

private fun sampleText(id: String, speaker: Speaker, text: String, sequence: Long) = MessageNode(
    id = id,
    contactId = "demo-contact",
    speaker = speaker,
    content = MessageContent.Text(text),
    normalizedText = text,
    source = MessageSource.MOCK,
    localSequence = sequence,
    confidence = 100,
    speakerConfidence = 100,
    contentConfidence = 100,
    bounds = null,
    pageIndex = null,
    createdAt = System.currentTimeMillis() + sequence,
    sceneId = "demo-scene"
)

private fun sampleVoice(id: String, speaker: Speaker, seconds: Int, sequence: Long) = MessageNode(
    id = id,
    contactId = "demo-contact",
    speaker = speaker,
    content = MessageContent.Voice(seconds, TranscriptStatus.MISSING, null, null),
    normalizedText = null,
    source = MessageSource.MOCK,
    localSequence = sequence,
    confidence = 88,
    speakerConfidence = 92,
    contentConfidence = 50,
    bounds = null,
    pageIndex = null,
    createdAt = System.currentTimeMillis() + sequence,
    sceneId = "demo-scene"
)
