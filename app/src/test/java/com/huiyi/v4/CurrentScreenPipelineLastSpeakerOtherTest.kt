package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentScreenPipelineLastSpeakerOtherTest {
    @Test
    fun leftSideLastMessageGeneratesFiveRoutes() {
        val messages = listOf(
            textNode("1", Speaker.OTHER, "今天有点累", 1),
            textNode("2", Speaker.ME, "我在", 2),
            textNode("3", Speaker.OTHER, "我真的有点撑不住", 3),
            textNode("4", Speaker.ME, "你慢慢说", 4),
            textNode("5", Speaker.OTHER, "嗯，就是想有人听我说", 5)
        )
        val context = ContextAssembler().assemble(messages)
        val lastSpeaker = LastSpeakerDecisionUseCase().decide(messages)
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        assertTrue(lastSpeaker.shouldReply)
        assertEquals(5, routes.size)
    }
}
