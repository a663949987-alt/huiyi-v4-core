package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LastSpeakerMeNoRoutesNoApiTest {
    @Test
    fun meLastHasNoRoutesNoApi() {
        val result = evidenceResult(
            appPackage = "com.chat.real",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("1", Speaker.OTHER, "你好", 1), textNode("2", Speaker.ME, "我刚发的", 2)),
            includeRoutes = false,
            apiCalled = false
        )

        assertFalse(result.apiCalled)
        assertTrue(result.routes.isEmpty())
        assertEquals("PASS", EvidencePackReportGenerator().overallResult(result))
    }
}
