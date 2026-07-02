package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus

data class LastSpeakerDecision(
    val lastEffectiveMessage: MessageNode?,
    val lastSpeaker: Speaker?,
    val shouldReply: Boolean,
    val reason: String,
    val requiresVoiceSummary: Boolean = false,
    val unknownSpeaker: Boolean = false
)

class LastSpeakerDecisionUseCase {
    fun decide(messages: List<MessageNode>): LastSpeakerDecision {
        val last = messages.lastOrNull { it.speaker != Speaker.SYSTEM }
            ?: return LastSpeakerDecision(null, null, false, "当前屏幕未识别到聊天消息。")
        val voiceMissing = last.content is MessageContent.Voice && last.content.transcriptStatus == TranscriptStatus.MISSING
        return when {
            voiceMissing -> LastSpeakerDecision(last, last.speaker, false, "最后一条是未转写语音，需要先补摘要。", requiresVoiceSummary = true)
            last.speaker == Speaker.ME -> LastSpeakerDecision(last, Speaker.ME, false, "最后一句是你发的，先等她回，不要继续补话。")
            last.speaker == Speaker.OTHER -> LastSpeakerDecision(last, Speaker.OTHER, true, "最后一句是对方，可以生成下一句。")
            else -> LastSpeakerDecision(last, Speaker.UNKNOWN, false, "我还没分清这句是谁说的，请切换我的气泡方向或补充。", unknownSpeaker = true)
        }
    }
}
