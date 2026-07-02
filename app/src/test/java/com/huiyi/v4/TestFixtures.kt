package com.huiyi.v4

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus

fun textNode(id: String, speaker: Speaker, text: String, sequence: Long = id.hashCode().toLong()): MessageNode = MessageNode(
    id = id,
    contactId = "test-contact",
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
    createdAt = sequence,
    sceneId = "test-scene"
)

fun voiceNode(id: String, speaker: Speaker, sequence: Long = id.hashCode().toLong()): MessageNode = MessageNode(
    id = id,
    contactId = "test-contact",
    speaker = speaker,
    content = MessageContent.Voice(18, TranscriptStatus.MISSING, null, null),
    normalizedText = null,
    source = MessageSource.MOCK,
    localSequence = sequence,
    confidence = 80,
    speakerConfidence = 90,
    contentConfidence = 50,
    bounds = null,
    pageIndex = null,
    createdAt = sequence,
    sceneId = "test-scene"
)
