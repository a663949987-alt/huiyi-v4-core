package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudAnalysisMvpSafetyGateTest {
    @Test
    fun LastMeSkipsCloudAndReturnsWaitTest() = runTest {
        val cloud = FakeCloudService(CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://gateway.example/v1/huiyi/next-sentence/analyze"))
        val result = pipeline(lastMeMessages(), cloud).run(emptyPersona()).getOrThrow()

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertEquals("LAST_SPEAKER_ME_WAIT", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_WAIT", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertFalse(result.apiCalled)
        assertFalse(result.cloudTrace.modelCalled)
        assertTrue(result.routes.isEmpty())
        assertEquals(0, cloud.callCount)
    }

    @Test
    fun LastMeCannotBecomeContextRequiredEvenWhenCloudEnabledTest() = runTest {
        val result = pipeline(lastMeMessages(), FakeCloudService()).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertFalse(result.cloudTrace.cloudAttempted)
    }

    @Test
    fun LastOtherCallsCloudWhenEnabledTest() = runTest {
        val cloud = FakeCloudService()
        val result = pipeline(lastOtherMessages(), cloud).run(emptyPersona()).getOrThrow()

        assertEquals(1, cloud.callCount)
        assertTrue(result.cloudTrace.cloudAttempted)
        assertTrue(result.cloudTrace.cloudSuccess)
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
    }

    @Test
    fun LastOtherCloudSuccessShowsCloudRoutesTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService()).run(emptyPersona()).getOrThrow()

        assertEquals(5, result.routes.size)
        assertEquals("cloud-0", result.routes.first().id)
        assertTrue(result.apiCalled)
        assertTrue(result.cloudTrace.modelCalled)
    }

    @Test
    fun LastOtherCloudTimeoutFallsBackToLocalTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(error = CloudAnalysisException("TIMEOUT"))).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("TIMEOUT", result.cloudTrace.cloudErrorCode)
        assertEquals(5, result.routes.size)
        assertFalse(result.apiCalled)
    }

    @Test
    fun CloudSchemaInvalidFallsBackToLocalTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(error = CloudAnalysisException("SCHEMA_INVALID"))).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("SCHEMA_INVALID", result.cloudTrace.cloudErrorCode)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun CloudNotConfiguredUsesLocalRoutesTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(CloudAnalysisConfig(cloudEnabled = false, endpoint = ""))).run(emptyPersona()).getOrThrow()

        assertEquals("CLOUD_NOT_CONFIGURED", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun ApiKeyNotPresentInApkOrRepoTest() {
        assertEquals("", BuildConfig.HUIYI_API_KEY)
    }

    @Test
    fun CloudTraceWrittenToFlightRecordTest() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = lastOtherMessages()
        ).copy(
            cloudTrace = com.huiyi.v4.domain.cloud.CloudAnalysisTrace.success(
                CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://gateway.example"),
                requestId = "cloud-req",
                latencyMs = 123
            ),
            apiCalled = true
        )
        val record = NextSentenceFlightRecordFactory.fromSuccess(result, NextSentenceSessionTrace("s", 1L, endedAt = 2L))

        assertTrue(record.cloudAttempted)
        assertTrue(record.cloudSuccess)
        assertEquals("CLOUD", record.decisionSource)
        assertEquals(123L, record.cloudLatencyMs)
    }

    @Test(expected = CloudAnalysisException::class)
    fun CloudResultRouteCountMustBeFiveTest() {
        CloudTacticalDecisionMapper().parseResponse(
            """
            {
              "decisionType": "NORMAL_REPLY",
              "routes": [{"id":"r1","name":"one","routeType":"STABLE","message":"ok"}]
            }
            """.trimIndent(),
            10
        )
    }

    @Test
    fun CloudFailureDoesNotShowGenericAnalysisFailedTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(error = CloudAnalysisException("NETWORK"))).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertEquals(5, result.routes.size)
        assertEquals(TacticalDecisionType.NORMAL_REPLY, result.tacticalDecision.decisionType)
    }

    private fun pipeline(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        cloud: CloudAnalysisService?
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(messages),
        cloudAnalysisService = cloud,
        appVersionName = "test",
        appVersionCode = 1
    )

    private class FakeCaptureUseCase(
        private val messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> {
            return Result.success(
                CurrentScreenCaptureResult(
                    snapshot = CurrentScreenSnapshot(
                        appPackage = "com.bajiao.im.liaoqi",
                        windowTitle = "chat",
                        screenWidth = 1080,
                        screenHeight = 2400,
                        nodes = emptyList(),
                        capturedAt = 1L
                    ),
                    messages = messages,
                    sampleSource = SampleSource.REAL_DEVICE_ACCESSIBILITY
                )
            )
        }
    }

    private class FakeCloudService(
        override val config: CloudAnalysisConfig = CloudAnalysisConfig(
            cloudEnabled = true,
            endpoint = "https://gateway.example/v1/huiyi/next-sentence/analyze"
        ),
        private val error: Throwable? = null
    ) : CloudAnalysisService {
        var callCount = 0

        override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> {
            callCount += 1
            error?.let { return Result.failure(it) }
            val routes: List<ReplyRoute> = ReplyRouteGenerator().generate(input.context, input.localDecision)
                .mapIndexed { index, route -> route.copy(id = "cloud-$index", tag = "云端") }
            return Result.success(CloudAnalysisOutput("cloud-req", input.localDecision, routes, 42))
        }
    }

    private fun lastMeMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "hello", 1),
        textNode("me-2", Speaker.ME, "ok", 2)
    )

    private fun lastOtherMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "hello", 1),
        textNode("me-2", Speaker.ME, "I am here", 2),
        textNode("other-3", Speaker.OTHER, "today was hard", 3),
        textNode("me-4", Speaker.ME, "tell me", 4),
        textNode("other-5", Speaker.OTHER, "I need someone to listen", 5)
    )

    private fun emptyPersona() = UserPersonaCorpus(
        id = "test",
        name = "test",
        enabled = false,
        identityCards = emptyList(),
        storyCards = emptyList(),
        styleRules = emptyList(),
        riskRules = emptyList()
    )
}
