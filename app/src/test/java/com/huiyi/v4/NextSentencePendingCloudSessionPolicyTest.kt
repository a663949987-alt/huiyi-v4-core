package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.runtime.NextSentencePendingCloudSessionPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextSentencePendingCloudSessionPolicyTest {
    @Test
    fun SoftTimeoutPendingSameActiveSessionCanResumePanelWithoutNewAnalysisTest() {
        val result = pendingResult(sessionId = "s1")

        assertTrue(
            NextSentencePendingCloudSessionPolicy.shouldResumePendingSession(
                result = result,
                activeSessionId = "s1"
            )
        )
    }

    @Test
    fun SoftTimeoutPendingDifferentSessionDoesNotResumeTest() {
        val result = pendingResult(sessionId = "s1")

        assertFalse(
            NextSentencePendingCloudSessionPolicy.shouldResumePendingSession(
                result = result,
                activeSessionId = "s2"
            )
        )
    }

    @Test
    fun NonPendingCloudResultStartsFreshAnalysisTest() {
        val result = pendingResult(sessionId = "s1").copy(
            cloudTrace = CloudAnalysisTrace.fallback(
                config = config(),
                errorCode = "TIMEOUT",
                latencyMs = 12_000L,
                requestActuallySent = true
            )
        )

        assertFalse(
            NextSentencePendingCloudSessionPolicy.shouldResumePendingSession(
                result = result,
                activeSessionId = "s1"
            )
        )
    }

    private fun pendingResult(sessionId: String) = evidenceResult(
        appPackage = "com.bajiao.im.liaoqi",
        source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
        messages = listOf(
            textNode("me", Speaker.ME, "我在", 1),
            textNode("other", Speaker.OTHER, "那你怎么看？", 2)
        ),
        includeRoutes = false,
        apiCalled = true
    ).copy(
        sessionId = sessionId,
        panelSessionId = sessionId,
        cloudTrace = CloudAnalysisTrace.fallback(
            config = config(),
            errorCode = NextSentencePendingCloudSessionPolicy.SOFT_TIMEOUT_PENDING,
            latencyMs = 12_000L,
            validationResult = "PENDING",
            failureLikelyCause = "PENDING",
            requestActuallySent = true
        ).copy(
            apiCalled = true,
            modelCalled = true,
            cloudFailureLikelyCause = "PENDING"
        )
    )

    private fun config() = CloudAnalysisConfig(
        cloudEnabled = true,
        endpoint = "https://relay.example/v1",
        apiKey = "placeholder",
        relayApiKeyStoredSecurely = true,
        model = "gpt-5.4"
    )
}
