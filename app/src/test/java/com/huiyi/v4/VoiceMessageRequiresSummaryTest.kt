package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MissingContextType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceMessageRequiresSummaryTest {
    @Test
    fun lastVoiceRequiresSummary() {
        val context = ContextAssembler().assemble(
            currentScreenMessages = listOf(
                textNode("1", Speaker.OTHER, "今天有点累", 1),
                textNode("2", Speaker.ME, "我在", 2),
                voiceNode("3", Speaker.OTHER, 3)
            )
        )

        val decision = TacticalDecisionEngine().decide(context)

        assertEquals(TacticalDecisionType.VOICE_SUMMARY_REQUIRED, decision.decisionType)
        assertFalse(context.contentCompleteness.canDeepAnalyze)
        assertTrue(MissingContextType.VOICE_WITHOUT_TRANSCRIPT in context.contentCompleteness.missingTypes)
    }
}
