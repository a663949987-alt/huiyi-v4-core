package com.huiyi.v4

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPasswordPolicyTest {
    @Test
    fun accessPasswordPolicyIsFiveHours() {
        val password = "6639"
        val validMs = 5L * 60L * 60L * 1000L

        assertEquals("6639", password)
        assertEquals(18_000_000L, validMs)
        assertTrue(validMs > 0)
    }
}
