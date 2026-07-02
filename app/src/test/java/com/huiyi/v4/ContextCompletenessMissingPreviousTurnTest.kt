package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.MissingContextType
import com.huiyi.v4.domain.model.Speaker
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextCompletenessMissingPreviousTurnTest {
    @Test
    fun missingPreviousTurnIsMarked() {
        val context = ContextAssembler().assemble(listOf(textNode("1", Speaker.OTHER, "你以后会不会又不回我", 1)))

        assertTrue(MissingContextType.PREVIOUS_TURN_MISSING in context.contentCompleteness.missingTypes)
    }
}
