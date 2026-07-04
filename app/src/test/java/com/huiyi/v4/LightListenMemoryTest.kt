package com.huiyi.v4

import com.huiyi.v4.domain.context.LightListenMemory
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LightListenMemoryTest {
    @Test
    fun LightListenKeepsRollingBackfillAndExcludesCurrentScreenDuplicatesTest() {
        val memory = LightListenMemory(maxMessagesPerChat = 4, maxAgeMs = 10_000L)
        val first = textNode("m1", Speaker.OTHER, "Earlier context", 1)
        val second = textNode("m2", Speaker.ME, "My previous reply", 2)
        val current = textNode("m3", Speaker.OTHER, "Current visible reply", 3)

        memory.ingest("chat.app", "Alice", listOf(first, second), capturedAt = 1_000L)
        memory.ingest("chat.app", "Alice", listOf(second, current), capturedAt = 2_000L)

        val backfill = memory.backfillFor(
            appPackage = "chat.app",
            windowTitle = "Alice",
            currentMessages = listOf(current),
            maxCount = 10,
            now = 2_500L
        )

        assertEquals(listOf("Earlier context", "My previous reply"), backfill.map { it.normalizedText })
        assertTrue(backfill.all { it.source == MessageSource.ACCESSIBILITY_LIGHT_LISTEN })
        assertTrue(backfill.all { it.localSequence < 0 })
    }

    @Test
    fun LightListenExpiresOldWindowsTest() {
        val memory = LightListenMemory(maxMessagesPerChat = 4, maxAgeMs = 500L)

        memory.ingest("chat.app", "Alice", listOf(textNode("m1", Speaker.OTHER, "Too old", 1)), capturedAt = 1_000L)

        val backfill = memory.backfillFor(
            appPackage = "chat.app",
            windowTitle = "Alice",
            currentMessages = emptyList(),
            now = 2_000L
        )

        assertTrue(backfill.isEmpty())
    }
}
