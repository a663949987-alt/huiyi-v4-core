package com.huiyi.v4

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportMarksLocalSampleAsLocalValidationTest {
    @Test
    fun localSampleIsMarked() {
        val result = evidenceResult(
            appPackage = "local.validation.sample",
            source = SampleSource.LOCAL_VALIDATION_SAMPLE,
            messages = listOf(textNode("1", Speaker.OTHER, "你好", 1), textNode("2", Speaker.ME, "你好", 2)),
            includeRoutes = false
        )

        val report = EvidencePackReportGenerator().buildMarkdown(result, HuiyiAccessibilityState(), generatedAt = 1)

        assertTrue(report.contains("sample_source: local_validation_sample"))
    }
}
