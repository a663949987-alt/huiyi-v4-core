package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus
import java.util.UUID

class ManualContextCaptureSession {
    fun createText(text: String, speaker: Speaker, sequence: Long): MessageNode = MessageNode(
        id = UUID.randomUUID().toString(),
        contactId = null,
        speaker = speaker,
        content = MessageContent.Text(text),
        normalizedText = text,
        source = MessageSource.MANUAL_TEXT,
        localSequence = sequence,
        confidence = 100,
        speakerConfidence = 100,
        contentConfidence = 100,
        bounds = null,
        pageIndex = null,
        createdAt = System.currentTimeMillis(),
        sceneId = null
    )

    fun summarizeVoice(original: MessageNode, summary: String): MessageNode {
        val voice = original.content as? MessageContent.Voice ?: return original
        return original.copy(
            content = voice.copy(
                transcriptStatus = TranscriptStatus.USER_SUMMARY,
                userSummary = summary
            ),
            normalizedText = summary,
            source = MessageSource.MANUAL_VOICE_SUMMARY,
            contentConfidence = 92
        )
    }
}
