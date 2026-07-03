package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisClient
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisRepository
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
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
        assertEquals("CLOUD", result.cloudTrace.decisionSource)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun CloudFailureShowsLocalFallbackNotAnalysisFailedTest() = runTest {
        val result = pipeline(
            messages = lastOtherMessages(),
            repository = CloudAnalysisRepository(relayConfig(), RecordingCloudClient("not json"))
        ).run(emptyPersona()).getOrThrow()

        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals(5, result.routes.size)
    }

    private fun pipeline(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        repository: CloudAnalysisRepository
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(messages),
        cloudAnalysisService = repository,
        appVersionName = "4.1.28",
        appVersionCode = 447
    )

    private fun relayConfig() = CloudAnalysisConfig(
        cloudEnabled = true,
        providerType = CloudProviderType.OPENAI_COMPATIBLE_RELAY,
        endpoint = "https://relay.example/v1",
        model = "gpt-5.5",
        timeoutMs = 6000,
        apiKey = "placeholder",
        relayApiKeyStoredSecurely = true
    )

    private class RecordingCloudClient(private val response: String) : CloudAnalysisClient {
        var callCount = 0
        var lastEndpoint = ""
        var lastBody = ""
        override fun postJson(endpoint: String, body: String, timeoutMs: Long, clientId: String, clientToken: String): String {
            callCount += 1
            lastEndpoint = endpoint
            lastBody = body
            return response
        }
    }

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
