package com.huiyi.v4

import com.huiyi.v4.domain.capture.ManualContextCaptureSession
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSummaryReanalysisTest {
    @Test
    fun summaryUnlocksReanalysis() {
        val updatedVoice = ManualContextCaptureSession().summarizeVoice(
            original = voiceNode("voice", Speaker.OTHER, 4),
            summary = "她说今天店里很忙，有点累。"
        )
        val context = ContextAssembler().assemble(
            listOf(
                textNode("1", Speaker.OTHER, "今天太忙了", 1),
                textNode("2", Speaker.ME, "我在", 2),
                textNode("3", Speaker.OTHER, "你听一下", 3),
                updatedVoice
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        assertNotEquals(TacticalDecisionType.VOICE_SUMMARY_REQUIRED, decision.decisionType)
        assertTrue(routes.isNotEmpty())
    }
}
