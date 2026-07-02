package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentScreenPipelineLastSpeakerMeTest {
    @Test
    fun rightSideLastMessageWaitsAndGeneratesNoRoutes() {
        val messages = listOf(
            textNode("1", Speaker.OTHER, "今天有点累", 1),
            textNode("2", Speaker.ME, "那你先休息，我晚点再找你", 2)
        )
        val context = ContextAssembler().assemble(messages)
        val lastSpeaker = LastSpeakerDecisionUseCase().decide(messages)
        val decision = TacticalDecisionEngine().decide(context)
        val routes = if (decision.decisionType == TacticalDecisionType.WAIT) emptyList() else ReplyRouteGenerator().generate(context, decision)

        assertFalse(lastSpeaker.shouldReply)
        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
        assertTrue(routes.isEmpty())
    }
}
