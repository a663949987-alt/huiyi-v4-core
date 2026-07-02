package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ReplyAttemptStatus
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.UserAction
import com.huiyi.v4.domain.pipeline.ReplyAttemptFactory
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ClipboardCopyCreatesReplyAttemptTest {
    @Test
    fun copiedRouteCreatesPendingAttempt() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("1", Speaker.ME, "你今天怎么样", 1),
                textNode("2", Speaker.OTHER, "有点累", 2),
                textNode("3", Speaker.ME, "我在", 3),
                textNode("4", Speaker.OTHER, "想有人听我说", 4)
            )
        )
        val route = ReplyRouteGenerator().generate(context, TacticalDecisionEngine().decide(context)).first()
        val attempt = ReplyAttemptFactory().copied(route, context.id, context.contactId, now = 10)

        assertEquals(UserAction.COPIED, attempt.userAction)
        assertEquals(ReplyAttemptStatus.PENDING, attempt.status)
        assertEquals(route.message, attempt.suggestedText)
    }
}
