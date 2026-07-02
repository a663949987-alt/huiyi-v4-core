package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertEquals
import org.junit.Test

class RealDeviceReportRequiresNonLocalPackageTest {
    @Test
    fun localPackageFailsRealDeviceValidation() {
        val result = evidenceResult(
            appPackage = "local.validation.sample",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("1", Speaker.OTHER, "你好", 1), textNode("2", Speaker.ME, "你好", 2)),
            includeRoutes = false
        )

        assertEquals("FAIL", EvidencePackReportGenerator().overallResult(result))
    }
}
