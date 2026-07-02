package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class VoiceMissingBlocksDeepAnalyzeTest {
    @Test
    fun missingVoiceBlocksDeepAnalyze() {
        val context = ContextAssembler().assemble(listOf(textNode("1", Speaker.ME, "我在", 1), voiceNode("2", Speaker.OTHER, 2)))
        val decision = TacticalDecisionEngine().decide(context)

        assertEquals(TacticalDecisionType.VOICE_SUMMARY_REQUIRED, decision.decisionType)
        assertFalse(context.contentCompleteness.canDeepAnalyze)
    }
}
