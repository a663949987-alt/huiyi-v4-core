package com.huiyi.v4

import com.huiyi.v4.domain.context.BackfillController
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import org.junit.Assert.assertTrue
import org.junit.Test

class BackfillStopsWhenEnoughContextTest {
    @Test
    fun stopsWhenCompletenessEnough() {
        val messages = (1..8).map {
            textNode("$it", if (it % 2 == 0) Speaker.ME else Speaker.OTHER, "有效消息$it", it.toLong())
        }
        val context = ContextAssembler().assemble(messages)

        assertTrue(BackfillController().shouldStop(context, pagesRead = 1))
    }
}
