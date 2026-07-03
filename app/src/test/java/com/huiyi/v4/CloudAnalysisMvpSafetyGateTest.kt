package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudTacticalResponseValidator
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudAnalysisMvpSafetyGateTest {
    @Test
    fun LastMeSkipsCloudAndReturnsWaitTest() = runTest {
        val cloud = FakeCloudService(CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://gateway.example/v1/huiyi/next-sentence/analyze", apiKey = "dummy-runtime-input"))
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
    fun LastMeContextOrderingCannotOverrideWaitTest() = runTest {
        val messages = listOf(
            textNode("other-sequence-late", Speaker.OTHER, "need more context", 99),
            textNode("me-visual-last", Speaker.ME, "I already replied", 1).copy(contentConfidence = 40)
        )
        val cloud = FakeCloudService()
        val result = pipeline(messages, cloud).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(Speaker.OTHER, result.context?.lastMessage?.speaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertEquals("WAIT", result.tacticalDecision.decisionType.name)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.apiCalled)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals("LAST_SPEAKER_ME_WAIT", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_WAIT", result.cloudTrace.decisionSource)
        assertEquals(0, cloud.callCount)
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
        val result = pipeline(lastOtherMessages(), FakeCloudService(error = CloudAnalysisException("CLOUD_SCHEMA_INVALID"))).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("CLOUD_SCHEMA_INVALID", result.cloudTrace.cloudErrorCode)
        assertEquals("FAIL", result.cloudTrace.cloudContractValidationResult)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun CloudContractViolationFallsBackToLocalTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(error = CloudAnalysisException("CLOUD_CONTRACT_VIOLATION"))).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("CLOUD_CONTRACT_VIOLATION", result.cloudTrace.cloudErrorCode)
        assertEquals("FAIL", result.cloudTrace.cloudContractValidationResult)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun LastOtherCloudNotConfiguredFallsBackToLocalTest() = runTest {
        val result = pipeline(lastOtherMessages(), FakeCloudService(CloudAnalysisConfig(cloudEnabled = false, endpoint = ""))).run(emptyPersona()).getOrThrow()

        assertEquals("CLOUD_NOT_CONFIGURED", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun RelayApiKeyMissingFallsBackToLocalTest() = runTest {
        val result = pipeline(
            lastOtherMessages(),
            FakeCloudService(CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = ""))
        ).run(emptyPersona()).getOrThrow()

        assertEquals("RELAY_API_KEY_MISSING", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertFalse(result.cloudTrace.relayApiKeyConfigured)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun LastMeSkipsRelayCloudTest() = runTest {
        val cloud = FakeCloudService(CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = "dummy-runtime-input"))
        val result = pipeline(lastMeMessages(), cloud).run(emptyPersona()).getOrThrow()

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertEquals("LAST_SPEAKER_ME_WAIT", result.cloudTrace.cloudSkippedReason)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals(0, cloud.callCount)
    }

    @Test
    fun LastOtherUsesRelayWhenConfiguredTest() = runTest {
        val cloud = FakeCloudService(CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = "dummy-runtime-input"))
        val result = pipeline(lastOtherMessages(), cloud).run(emptyPersona()).getOrThrow()

        assertTrue(result.cloudTrace.cloudAttempted)
        assertTrue(result.cloudTrace.relayBaseUrlConfigured)
        assertTrue(result.cloudTrace.relayApiKeyConfigured)
        assertEquals("OPENAI_COMPATIBLE_RELAY", result.cloudTrace.providerType)
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
    }

    @Test
    fun RelayInvalidResponseFallsBackToLocalTest() = runTest {
        val result = pipeline(
            lastOtherMessages(),
            FakeCloudService(
                CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = "dummy-runtime-input"),
                error = CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            )
        ).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("CLOUD_SCHEMA_INVALID", result.cloudTrace.cloudErrorCode)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun UnknownSpeakerSkipsCloudTest() = runTest {
        val cloud = FakeCloudService()
        val result = pipeline(listOf(textNode("unknown", Speaker.UNKNOWN, "not sure", 1)), cloud).run(emptyPersona()).getOrThrow()

        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals("LAST_SPEAKER_UNKNOWN", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertEquals(0, cloud.callCount)
    }

    @Test
    fun UnsupportedAppSkipsCloudTest() = runTest {
        val cloud = FakeCloudService()
        val result = pipeline(lastOtherMessages(), cloud, appPackage = "com.unsupported.chat").run(emptyPersona()).getOrThrow()

        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals("UNSUPPORTED_APP", result.cloudTrace.cloudSkippedReason)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertEquals(0, cloud.callCount)
    }

    @Test
    fun ApiKeyNotPresentInApkOrRepoTest() {
        assertEquals("", BuildConfig.HUIYI_API_KEY)
        assertEquals("", BuildConfig.HUIYI_CLOUD_ANALYSIS_CLIENT_TOKEN)
    }

    @Test
    fun RelayApiKeyNotWrittenToRepoOrOutputsTest() {
        val trace = com.huiyi.v4.domain.cloud.CloudAnalysisTrace.skipped(
            CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = "dummy-runtime-input"),
            reason = "LAST_SPEAKER_ME_WAIT",
            decisionSource = "LOCAL_WAIT"
        )

        assertTrue(trace.relayApiKeyConfigured)
        assertFalse(trace.relayApiKeyExposedInRepo)
        assertFalse(trace.relayApiKeyExposedInApk)
        assertEquals("", BuildConfig.HUIYI_CLOUD_ANALYSIS_CLIENT_TOKEN)
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
        assertEquals("HuiyiTacticalContract-v1", record.cloudContractVersion)
        assertEquals("PASS", record.cloudContractValidationResult)
    }

    @Test
    fun CloudSettingsRedactsApiKeyInReportsTest() {
        val record = NextSentenceFlightRecordFactory.fromSuccess(
            evidenceResult(
                appPackage = "com.bajiao.im.liaoqi",
                source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
                messages = lastOtherMessages()
            ).copy(
                cloudTrace = com.huiyi.v4.domain.cloud.CloudAnalysisTrace.skipped(
                    CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example/v1", apiKey = "dummy-runtime-input"),
                    reason = "RELAY_API_KEY_MISSING",
                    decisionSource = "LOCAL_FALLBACK"
                )
            ),
            NextSentenceSessionTrace("redacted-relay-key", 1L, endedAt = 2L)
        )

        assertTrue(record.relayApiKeyConfigured)
        assertFalse(record.relayApiKeyExposedInRepo)
        assertFalse(record.relayApiKeyExposedInApk)
        assertEquals("OPENAI_COMPATIBLE_RELAY", record.providerType)
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
    fun LastOtherCloudSuccessMapsToRoutePanelTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(validCloudResponse(), 10L, Speaker.OTHER)

        assertEquals(TacticalDecisionType.NORMAL_REPLY, output.decision.decisionType)
        assertEquals(5, output.routes.size)
        assertEquals("stable", output.routes.first().name)
    }

    @Test
    fun CloudContractRequiresCoCreationPointTest() {
        val invalid = validCloudResponse().replace("\"coCreationPoint\"", "\"missingCoCreationPoint\"")
        val result = CloudTacticalResponseValidator().validate(Json.parseToJsonElement(invalid).jsonObject, Speaker.OTHER)

        assertTrue(result.isFailure)
        assertEquals("CLOUD_SCHEMA_INVALID", (result.exceptionOrNull() as CloudAnalysisException).code)
    }

    @Test
    fun CloudContractRejectsManipulativeOutputTest() {
        val invalid = validCloudResponse().replace("接住她现在的状态", "pua manipulate force her to reply")
        val result = CloudTacticalResponseValidator().validate(Json.parseToJsonElement(invalid).jsonObject, Speaker.OTHER)

        assertTrue(result.isFailure)
        assertEquals("CLOUD_CONTRACT_VIOLATION", (result.exceptionOrNull() as CloudAnalysisException).code)
    }

    @Test
    fun GithubUploadIsNotCloudAnalysisTest() {
        val record = NextSentenceFlightRecordFactory.fromSuccess(
            evidenceResult(
                appPackage = "com.bajiao.im.liaoqi",
                source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
                messages = lastOtherMessages()
            ).copy(
                cloudTrace = com.huiyi.v4.domain.cloud.CloudAnalysisTrace.skipped(
                    CloudAnalysisConfig(cloudEnabled = false, endpoint = ""),
                    reason = "CLOUD_NOT_CONFIGURED",
                    decisionSource = "LOCAL_FALLBACK"
                )
            ),
            NextSentenceSessionTrace("github-upload-is-not-cloud", 1L, endedAt = 2L)
        ).withFeedbackTrace(
            clickedAt = 3L,
            targetSessionId = "github-upload-is-not-cloud",
            exportSource = "ONE_TAP_FEEDBACK"
        )

        assertFalse(record.cloudAttempted)
        assertEquals("CLOUD_NOT_CONFIGURED", record.cloudSkippedReason)
        assertTrue(record.feedbackTargetSessionFound)
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
        cloud: CloudAnalysisService?,
        appPackage: String = "com.bajiao.im.liaoqi"
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(messages, appPackage),
        cloudAnalysisService = cloud,
        appVersionName = "test",
        appVersionCode = 1
    )

    private class FakeCaptureUseCase(
        private val messages: List<com.huiyi.v4.domain.model.MessageNode>,
        private val appPackage: String
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> {
            return Result.success(
                CurrentScreenCaptureResult(
                    snapshot = CurrentScreenSnapshot(
                        appPackage = appPackage,
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
            endpoint = "https://gateway.example/v1/huiyi/next-sentence/analyze",
            apiKey = "dummy-runtime-input"
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

    private fun validCloudResponse(): String = """
        {
          "schemaVersion": 1,
          "decisionType": "NORMAL_REPLY",
          "decisionTypeFamily": "REPLY_ROUTES",
          "situation": "The other person is sharing pressure.",
          "coCreationPoint": {
            "exists": true,
            "type": "daily_rhythm",
            "evidence": "She said today was hard.",
            "meaning": "Create a small shared safe rhythm."
          },
          "userLikelyMistake": "Replying too hard or changing topic too fast.",
          "bestMove": "接住她现在的状态",
          "intensityPolicy": {
            "level": "LOW",
            "reason": "Low pressure is safer here."
          },
          "riskWarning": "",
          "fallbackMove": "Keep it light and let her answer when ready.",
          "routes": [
            {"slot":"stable","message":"听起来今天确实有点累，你先缓一缓。","why":"Receives the pressure without pushing.","riskLevel":"LOW","fallbackMove":"Keep waiting calmly."},
            {"slot":"light","message":"那今晚先别硬撑，吃点舒服的。","why":"Adds daily-life care.","riskLevel":"LOW","fallbackMove":"Move back to light care."},
            {"slot":"question","message":"是事情多，还是人让你累？","why":"Asks one gentle clarifying question.","riskLevel":"LOW","fallbackMove":"Stop if she does not expand."},
            {"slot":"daily_life","message":"我在，等你忙完慢慢说。","why":"Keeps presence without pressure.","riskLevel":"LOW","fallbackMove":"Wait."},
            {"slot":"warmer","message":"辛苦啦，今天先把自己放第一位。","why":"Warmer but still safe.","riskLevel":"LOW","fallbackMove":"Return to low intensity."}
          ]
        }
    """.trimIndent()

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
