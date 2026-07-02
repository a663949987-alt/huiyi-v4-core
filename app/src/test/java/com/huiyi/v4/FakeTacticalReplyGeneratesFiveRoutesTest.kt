package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.modelprovider.FakeModelProvider
import com.huiyi.v4.domain.modelprovider.TacticalPromptInput
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FakeTacticalReplyGeneratesFiveRoutesTest {
    @Test
    fun fakeProviderReturnsFiveRoutes() = runTest {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("1", Speaker.ME, "今天怎么样", 1),
                textNode("2", Speaker.OTHER, "今天店里很忙，有点累", 2),
                textNode("3", Speaker.ME, "辛苦了", 3),
                textNode("4", Speaker.OTHER, "嗯，确实有点撑不住", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val result = FakeModelProvider().generateTacticalReply(TacticalPromptInput(context, decision))

        assertEquals(5, result.routes.size)
        assertFalse(result.apiCalled)
    }
}
