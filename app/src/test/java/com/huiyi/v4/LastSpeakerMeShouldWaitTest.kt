package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class LastSpeakerMeShouldWaitTest {
    @Test
    fun lastSpeakerMeReturnsWait() {
        val context = ContextAssembler().assemble(
            currentScreenMessages = listOf(
                textNode("1", Speaker.OTHER, "今天有点累", 1),
                textNode("2", Speaker.ME, "那你先休息，我晚点再找你", 2)
            )
        )

        val decision = TacticalDecisionEngine().decide(context)

        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
    }
}
