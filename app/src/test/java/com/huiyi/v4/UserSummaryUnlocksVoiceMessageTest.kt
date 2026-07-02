package com.huiyi.v4

import com.huiyi.v4.domain.capture.ManualContextCaptureSession
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSummaryUnlocksVoiceMessageTest {
    @Test
    fun userSummaryUpdatesVoice() {
        val original = voiceNode("voice", Speaker.OTHER, 1)
        val updated = ManualContextCaptureSession().summarizeVoice(original, "她说今天店里很忙，有点累。")
        val voice = updated.content as MessageContent.Voice
        val context = ContextAssembler().assemble(listOf(textNode("1", Speaker.ME, "我在", 0), updated))

        assertEquals(TranscriptStatus.USER_SUMMARY, voice.transcriptStatus)
        assertEquals("她说今天店里很忙，有点累。", voice.userSummary)
        assertTrue(context.allMessages.last().normalizedText!!.contains("店里"))
    }
}
