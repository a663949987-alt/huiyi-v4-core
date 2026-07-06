package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisClient
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisRepository
import com.huiyi.v4.domain.cloud.CloudVisualEvidence
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.SampleSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PreconfiguredCloudRealUseMvpTest {
    @Test
    fun PreconfiguredCloudConfigLoadsFromLocalIgnoredFileTest() {
        assertTrue(BuildConfig.HUIYI_RELAY_MODEL.isNotBlank())
        assertEquals(
            BuildConfig.HUIYI_RELAY_BASE_URL.isNotBlank() && BuildConfig.HUIYI_RELAY_API_KEY.isNotBlank(),
            BuildConfig.HUIYI_RELAY_CONFIGURED_FOR_BUILD
        )
    }

    @Test
    fun RelayApiKeyNotCommittedToRepoTest() {
        val tracked = runGit("ls-files")
        assertFalse(tracked.contains("huiyi-cloud.properties"))
        assertFalse(tracked.contains(".env.local"))
        tracked.lineSequence()
            .filter { it.endsWith(".kt") || it.endsWith(".kts") || it.endsWith(".md") || it.endsWith(".json") }
            .forEach { path ->
                val file = File(path)
                if (file.exists()) assertFalse(file.readText().contains(BuildConfig.HUIYI_RELAY_API_KEY))
            }
    }

    @Test
    fun RelayApiKeyNotWrittenToOutputsTest() {
        File("outputs").walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in setOf("md", "json", "txt") }
            .forEach { file ->
                assertFalse(file.path, file.readText(Charsets.UTF_8).contains(BuildConfig.HUIYI_RELAY_API_KEY))
            }
    }

    @Test
    fun BaseUrlJoinDoesNotDuplicateV1Test() {
        assertEquals("https://api.example.com/v1/chat/completions", RelayEndpointBuilder.chatCompletionsUrl("https://api.example.com/v1"))
        assertEquals("https://api.example.com/v1/chat/completions", RelayEndpointBuilder.chatCompletionsUrl("https://api.example.com/v1/"))
        assertEquals("https://api.example.com/v1/chat/completions", RelayEndpointBuilder.chatCompletionsUrl("https://api.example.com/v1/chat/completions"))
    }

    @Test
    fun OpenAICompatibleProviderParsesChoicesMessageContentTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(openAiCompletion(validCloudContract()), 12L, Speaker.OTHER)

        assertEquals(TacticalDecisionType.NORMAL_REPLY, output.decision.decisionType)
        assertEquals(5, output.routes.size)
        assertEquals("stable", output.routes.first().name)
    }

    @Test
    fun OpenAICompatibleProviderStripsJsonCodeFenceTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(openAiCompletion("```json\n${validCloudContract()}\n```"), 12L, Speaker.OTHER)

        assertEquals(TacticalDecisionType.NORMAL_REPLY, output.decision.decisionType)
        assertEquals(5, output.routes.size)
    }

    @Test
    fun RelayResponseParsedIntoHuiyiContractTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(openAiCompletion(validCloudContract()), 12L, Speaker.OTHER)

        assertEquals("cloud-req", output.cloudRequestId)
        assertEquals("Receive the pressure without pushing.", output.decision.bestMove)
    }

    @Test
    fun NumericCloudSlotsAreMappedToStrategyRouteTypesTest() {
        val output = CloudTacticalDecisionMapper().parseResponse(openAiCompletion(numericSlotCloudContract()), 12L, Speaker.OTHER)

        assertEquals(com.huiyi.v4.domain.model.ReplyRouteType.EMPATHY, output.routes[0].routeType)
        assertEquals(com.huiyi.v4.domain.model.ReplyRouteType.STABLE, output.routes[1].routeType)
        assertEquals(com.huiyi.v4.domain.model.ReplyRouteType.DIRECT, output.routes[2].routeType)
        assertEquals(com.huiyi.v4.domain.model.ReplyRouteType.WARM_UP, output.routes[3].routeType)
        assertEquals(com.huiyi.v4.domain.model.ReplyRouteType.COOL_DOWN, output.routes[4].routeType)
    }

    @Test(expected = CloudAnalysisException::class)
    fun RelayInvalidJsonFallsBackToLocalTest() {
        CloudTacticalDecisionMapper().parseResponse(openAiCompletion("not json"), 12L, Speaker.OTHER)
    }

    @Test
    fun LastMeSkipsCloudEvenWhenRelayConfiguredTest() = runTest {
        val client = RecordingCloudClient(openAiCompletion(validCloudContract()))
        val result = pipeline(
            messages = lastMeMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals(0, client.callCount)
        assertTrue(result.routes.isEmpty())
    }

    @Test
    fun LastOtherUsesRelayWhenConfiguredTest() = runTest {
        val client = RecordingCloudClient(openAiCompletion(validCloudContract()))
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(1, client.callCount)
        assertEquals("https://relay.example/v1/chat/completions", client.lastEndpoint)
        assertTrue(client.lastBody.contains("\"messages\""))
        assertTrue(client.lastBody.contains("\"model\":\"gpt-5.4\""))
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertEquals("gpt-5.4", result.cloudTrace.cloudPrimaryModel)
        assertEquals("gpt-5.4", result.cloudTrace.cloudFinalModel)
        assertFalse(result.cloudTrace.cloudEscalated)
        assertEquals("PASS", result.cloudTrace.cloudQualityGateResult)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun LightListenBackfillIsIncludedInCloudPayloadWithoutChangingCurrentLastSpeakerTest() = runTest {
        val client = RecordingCloudClient(openAiCompletion(validCloudContract()))
        val backfill = listOf(
            textNode("light-1", Speaker.OTHER, "Earlier context from light listen", -20)
                .copy(source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN),
            textNode("light-2", Speaker.ME, "My earlier reply from light listen", -19)
                .copy(source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN)
        )
        val result = pipeline(
            messages = listOf(
                textNode("current-1", Speaker.ME, "Current screen me", 1),
                textNode("current-2", Speaker.OTHER, "Current screen other", 2)
            ),
            repository = CloudAnalysisRepository(relayConfig(), client),
            lightListenContextProvider = { backfill }
        ).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(2, result.lightListenBackfillCount)
        assertTrue(result.lightListenUsed)
        assertTrue(client.lastBody.contains("Earlier context from light listen"))
        assertTrue(client.lastBody.contains("ACCESSIBILITY_LIGHT_LISTEN"))
        assertTrue(client.lastBody.contains("lastSpeakerStillBasedOnCurrentScreenOnly"))
        assertTrue(client.lastBody.contains("lightListenBackfillCount"))
    }

    @Test
    fun LowQualityPrimaryModelEscalatesTo55Test() = runTest {
        val client = RecordingCloudClient(
            openAiCompletion(aiLikeCloudContract()),
            openAiCompletion(validCloudContract())
        )
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(2, client.callCount)
        assertTrue(client.bodies[0].contains("\"model\":\"gpt-5.4\""))
        assertTrue(client.bodies[1].contains("\"model\":\"gpt-5.5\""))
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudEscalated)
        assertEquals("gpt-5.4", result.cloudTrace.cloudPrimaryModel)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals("QUALITY_GATE_AI_LIKE_PHRASE", result.cloudTrace.cloudEscalationReason)
        assertEquals("PASS", result.cloudTrace.cloudQualityGateResult)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun ContractInvalidPrimaryModelEscalatesTo55Test() = runTest {
        val client = RecordingCloudClient(
            "not json",
            openAiCompletion(validCloudContract())
        )
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(2, client.callCount)
        assertTrue(client.bodies[0].contains("\"model\":\"gpt-5.4\""))
        assertTrue(client.bodies[1].contains("\"model\":\"gpt-5.5\""))
        assertTrue(result.cloudTrace.cloudEscalated)
        assertEquals("CLOUD_SCHEMA_INVALID", result.cloudTrace.cloudEscalationReason)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun VisualEvidenceUses55PrimaryToAvoidFirstClick54VisionFailureTest() = runTest {
        val client = RecordingCloudClient(openAiCompletion(validCloudContract()))
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client),
            visualEvidenceProvider = { fakeVisualEvidence() }
        ).run(emptyPersona()).getOrThrow()

        assertEquals(1, client.callCount)
        assertTrue(client.lastBody.contains("\"model\":\"gpt-5.5\""))
        assertTrue(client.lastBody.contains("\"image_url\""))
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertEquals("gpt-5.5", result.cloudTrace.cloudPrimaryModel)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertFalse(result.cloudTrace.cloudEscalated)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun UnifiedEvidencePackageSendsCurrentScreenshotAndRecentCheckpointsWithAuthorityLabelsTest() = runTest {
        val client = RecordingCloudClient(openAiCompletion(validCloudContract()))
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client),
            visualEvidenceProvider = { fakeVisualEvidence(role = "CURRENT_SCREENSHOT") },
            recentVisualEvidenceProvider = {
                listOf(
                    fakeVisualEvidence(role = "RECENT_VISUAL_CHECKPOINT", source = "event_triggered_visual_checkpoint:one"),
                    fakeVisualEvidence(role = "RECENT_VISUAL_CHECKPOINT", source = "event_triggered_visual_checkpoint:two")
                )
            }
        ).run(emptyPersona()).getOrThrow()

        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertTrue(client.lastBody.contains("huiyi-evidence-v1"))
        assertTrue(client.lastBody.contains("CURRENT_SCREENSHOT"))
        assertTrue(client.lastBody.contains("RECENT_VISUAL_CHECKPOINT_1"))
        assertTrue(client.lastBody.contains("RECENT_VISUAL_CHECKPOINT_2"))
        assertTrue(client.lastBody.contains("cannotOverrideCurrentScreenshot"))
        assertEquals(3, Regex("data:image/png;base64").findAll(client.lastBody).count())
        assertTrue(client.lastBody.contains("\"model\":\"gpt-5.5\""))
    }

    @Test
    fun ModelUnavailablePrimaryEscalatesTo55BeforeLocalFallbackTest() = runTest {
        val client = ScriptedCloudClient(
            CloudAnalysisException("HTTP_404"),
            openAiCompletion(validCloudContract())
        )
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(2, client.callCount)
        assertTrue(client.bodies[0].contains("\"model\":\"gpt-5.4\""))
        assertTrue(client.bodies[1].contains("\"model\":\"gpt-5.5\""))
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudEscalated)
        assertEquals("HTTP_404", result.cloudTrace.cloudEscalationReason)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun TimeoutPrimaryEscalatesTo55BeforeLocalFallbackTest() = runTest {
        val client = ScriptedCloudClient(
            CloudAnalysisException("TIMEOUT"),
            openAiCompletion(validCloudContract())
        )
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(2, client.callCount)
        assertTrue(client.bodies[0].contains("\"model\":\"gpt-5.4\""))
        assertTrue(client.bodies[1].contains("\"model\":\"gpt-5.5\""))
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudEscalated)
        assertEquals("TIMEOUT", result.cloudTrace.cloudEscalationReason)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun TimeoutPrimaryAndEscalationFailureStillReports55AttemptTest() = runTest {
        val client = ScriptedCloudClient(
            CloudAnalysisException("TIMEOUT"),
            CloudAnalysisException("TIMEOUT")
        )
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), client)
        ).run(emptyPersona()).getOrThrow()

        assertEquals(2, client.callCount)
        assertEquals("PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudFallbackUsed)
        assertEquals("TIMEOUT", result.cloudTrace.cloudErrorCode)
        assertTrue(result.cloudTrace.cloudNetworkFailureVisibleToUser)
        assertTrue(result.cloudTrace.cloudEscalated)
        assertEquals("TIMEOUT", result.cloudTrace.cloudEscalationReason)
        assertEquals("gpt-5.4", result.cloudTrace.cloudPrimaryModel)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecision.decisionType)
        assertEquals(0, result.routes.size)
    }

    @Test
    fun VisualEvidenceTimeoutUsesConfiguredLongTimeoutAndReports55AttemptTest() = runTest {
        val client = ScriptedCloudClient(CloudAnalysisException("TIMEOUT"))
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(timeoutMs = 90_000L), client),
            visualEvidenceProvider = { fakeVisualEvidence() }
        ).run(emptyPersona()).getOrThrow()

        assertEquals(1, client.callCount)
        assertEquals(90_000L, client.timeouts.single())
        assertTrue(client.bodies.single().contains("\"model\":\"gpt-5.5\""))
        assertEquals("PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudFallbackUsed)
        assertEquals("TIMEOUT", result.cloudTrace.cloudErrorCode)
        assertFalse(result.cloudTrace.cloudEscalated)
        assertEquals("gpt-5.5", result.cloudTrace.cloudPrimaryModel)
        assertEquals("gpt-5.5", result.cloudTrace.cloudFinalModel)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecision.decisionType)
        assertEquals(0, result.routes.size)
    }

    @Test
    fun CloudFailureShowsLocalFallbackNotAnalysisFailedTest() = runTest {
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), RecordingCloudClient("not json"))
        ).run(emptyPersona()).getOrThrow()

        assertEquals("PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK", result.cloudTrace.decisionSource)
        assertFalse(result.cloudTrace.cloudFallbackUsed)
        assertEquals(TacticalDecisionType.PASSIVE_NOT_READY, result.tacticalDecision.decisionType)
        assertEquals(0, result.routes.size)
    }

    private fun pipeline(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        repository: CloudAnalysisRepository,
        visualEvidenceProvider: (suspend () -> CloudVisualEvidence?)? = null,
        recentVisualEvidenceProvider: ((CurrentScreenCaptureResult) -> List<CloudVisualEvidence>)? = null,
        lightListenContextProvider: ((CurrentScreenCaptureResult) -> List<com.huiyi.v4.domain.model.MessageNode>)? = null
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(messages),
        cloudAnalysisService = repository,
        visualEvidenceProvider = visualEvidenceProvider,
        recentVisualEvidenceProvider = recentVisualEvidenceProvider,
        lightListenContextProvider = lightListenContextProvider,
        appVersionName = "4.1.28",
        appVersionCode = 447
    )

    private fun relayConfig(timeoutMs: Long = 6000L) = CloudAnalysisConfig(
        cloudEnabled = true,
        providerType = CloudProviderType.OPENAI_COMPATIBLE_RELAY,
        endpoint = "https://relay.example/v1",
        model = "gpt-5.5",
        timeoutMs = timeoutMs,
        apiKey = "placeholder",
        relayApiKeyStoredSecurely = true
    )

    private class RecordingCloudClient(private vararg val responses: String) : CloudAnalysisClient {
        var callCount = 0
        var lastEndpoint = ""
        var lastBody = ""
        val bodies = mutableListOf<String>()
        override fun postJson(endpoint: String, body: String, timeoutMs: Long, clientId: String, clientToken: String): String {
            callCount += 1
            lastEndpoint = endpoint
            lastBody = body
            bodies += body
            return responses.getOrElse(callCount - 1) { responses.last() }
        }
    }

    private class ScriptedCloudClient(private vararg val outcomes: Any) : CloudAnalysisClient {
        var callCount = 0
        val bodies = mutableListOf<String>()
        val timeouts = mutableListOf<Long>()

        override fun postJson(endpoint: String, body: String, timeoutMs: Long, clientId: String, clientToken: String): String {
            callCount += 1
            bodies += body
            timeouts += timeoutMs
            return when (val outcome = outcomes.getOrElse(callCount - 1) { outcomes.last() }) {
                is Throwable -> throw outcome
                is String -> outcome
                else -> error("unsupported_outcome")
            }
        }
    }

    private fun fakeVisualEvidence(
        role: String = "CURRENT_SCREENSHOT",
        source: String = "unit_test"
    ) = CloudVisualEvidence(
        imageBase64 = "ZmFrZQ==",
        mimeType = "image/png",
        width = 360,
        height = 760,
        source = source,
        capturedAt = 123L,
        role = role
    )

    private class FakeCaptureUseCase(
        private val messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> = Result.success(
            CurrentScreenCaptureResult(
                snapshot = CurrentScreenSnapshot(
                    appPackage = "com.bajiao.im.liaoqi",
                    windowTitle = "聊起",
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

    private fun lastMeMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "今天有点累", 1),
        textNode("me-2", Speaker.ME, "你先休息一下", 2)
    )

    private fun lastOtherMessages() = listOf(
        textNode("me-1", Speaker.ME, "怎么了", 1),
        textNode("other-2", Speaker.OTHER, "今天事情太多了，想缓一缓", 2)
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

    private fun openAiCompletion(content: String): String {
        val escaped = content
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
        return """{"id":"chatcmpl-test","choices":[{"message":{"role":"assistant","content":"$escaped"}}]}"""
    }

    private fun validCloudContract(): String = """
        {
          "cloudRequestId": "cloud-req",
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
          "bestMove": "Receive the pressure without pushing.",
          "intensityPolicy": {
            "level": "LOW",
            "reason": "Low pressure is safer here."
          },
          "riskWarning": "",
          "fallbackMove": "Keep it light and let her answer when ready.",
          "routes": [
            {"slot":"stable","message":"Sounds like today really took a lot out of you. Slow down first.","why":"Receives the pressure without pushing.","riskLevel":"LOW","fallbackMove":"Keep waiting calmly."},
            {"slot":"light","message":"Tonight do not force yourself. Eat something easy.","why":"Adds daily-life care.","riskLevel":"LOW","fallbackMove":"Move back to light care."},
            {"slot":"question","message":"Was it too many things, or did someone make you tired?","why":"Asks one gentle clarifying question.","riskLevel":"LOW","fallbackMove":"Stop if she does not expand."},
            {"slot":"daily_life","message":"I am here. Talk slowly when you are done being busy.","why":"Keeps presence without pressure.","riskLevel":"LOW","fallbackMove":"Wait."},
            {"slot":"warmer","message":"That sounds rough. Put yourself first tonight.","why":"Warmer but still safe.","riskLevel":"LOW","fallbackMove":"Return to low intensity."}
          ]
        }
    """.trimIndent()

    private fun aiLikeCloudContract(): String = validCloudContract()
        .replace(
            "Sounds like today really took a lot out of you. Slow down first.",
            "I understand your feelings and suggest you communicate clearly about this situation."
        )

    private fun numericSlotCloudContract(): String = validCloudContract()
        .replace("\"slot\":\"stable\"", "\"slot\":\"1\"")
        .replace("\"slot\":\"light\"", "\"slot\":\"2\"")
        .replace("\"slot\":\"question\"", "\"slot\":\"3\"")
        .replace("\"slot\":\"daily_life\"", "\"slot\":\"4\"")
        .replace("\"slot\":\"warmer\"", "\"slot\":\"5\"")
        .replace("\"why\":\"Warmer but still safe.\"", "\"why\":\"Option five.\"")

    private fun runGit(args: String): String {
        val command = listOf("git") + args.split(" ")
        return ProcessBuilder(command)
            .directory(File("."))
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
    }
}
