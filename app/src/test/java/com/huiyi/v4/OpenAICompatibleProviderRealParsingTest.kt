package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.CloudTacticalResponseValidator
import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.modelprovider.OpenAICompatibleConfig
import com.huiyi.v4.domain.modelprovider.OpenAICompatibleProvider
import com.huiyi.v4.domain.modelprovider.TacticalPromptInput
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class OpenAICompatibleProviderRealParsingTest {
    private val server = MockWebServer()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun OpenAICompatibleProviderParsesSuccessfulRelayResponseTest() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(validOpenAiChatCompletion()))
        server.start()

        val result = provider(server.url("/v1").toString()).generateTacticalReply(promptInput())
        val request = server.takeRequest()

        assertEquals("/v1/chat/completions", request.path)
        assertEquals("Bearer placeholder", request.getHeader("Authorization"))
        assertEquals(TacticalDecisionType.NORMAL_REPLY, result.decision.decisionType)
        assertEquals(5, result.routes.size)
        assertEquals("stable", result.routes.first().name)
        assertEquals("Sounds like today really took a lot out of you. Slow down first.", result.routes.first().message)
    }

    @Test
    fun OpenAICompatibleProviderReportsHttp401Test() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"unauthorized"}"""))
        server.start()

        val error = catchCloudError { provider(server.url("/v1").toString()).generateTacticalReply(promptInput()) }

        assertEquals("HTTP_401", error.code)
    }

    @Test
    fun OpenAICompatibleProviderReportsHttp404Test() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"not_found"}"""))
        server.start()

        val error = catchCloudError { provider(server.url("/v1").toString()).generateTacticalReply(promptInput()) }

        assertEquals("HTTP_404", error.code)
    }

    @Test
    fun OpenAICompatibleProviderReportsHttp5xxTest() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"server"}"""))
        server.start()

        val error = catchCloudError { provider(server.url("/v1").toString()).generateTacticalReply(promptInput()) }

        assertEquals("HTTP_5XX", error.code)
    }

    @Test
    fun OpenAICompatibleProviderReportsDnsFailureTest() = runTest {
        val client = OkHttpClient.Builder()
            .addInterceptor { throw UnknownHostException("relay.invalid") }
            .build()

        val error = catchCloudError { provider("https://relay.invalid/v1", client).generateTacticalReply(promptInput()) }

        assertEquals("NETWORK", error.code)
        assertEquals("DNS_FAILED", error.likelyCause)
    }

    @Test
    fun OpenAICompatibleProviderReportsTlsFailureTest() = runTest {
        val client = OkHttpClient.Builder()
            .addInterceptor { throw SSLException("certificate rejected") }
            .build()

        val error = catchCloudError { provider("https://relay.example/v1", client).generateTacticalReply(promptInput()) }

        assertEquals("NETWORK", error.code)
        assertEquals("TLS_FAILED", error.likelyCause)
    }

    @Test
    fun BaseUrlJoinDoesNotDuplicateV1Test() {
        assertEquals("https://api.example.com/v1/chat/completions", RelayEndpointBuilder.chatCompletionsUrl("https://api.example.com/v1"))
        assertEquals("https://api.example.com/v1/chat/completions", RelayEndpointBuilder.chatCompletionsUrl("https://api.example.com/v1/chat/completions"))
    }

    @Test
    fun RelaySmokeContractValidatorPassTest() {
        val root = CloudTacticalDecisionMapper().extractContractJson(validOpenAiChatCompletion(fenced = true))
        val result = CloudTacticalResponseValidator().validate(root, Speaker.OTHER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun RelayInvalidJsonFallsBackToLocalTest() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"choices":[{"message":{"content":"not json"}}]}"""))
        server.start()

        val error = catchCloudError { provider(server.url("/v1").toString()).generateTacticalReply(promptInput()) }

        assertEquals("CLOUD_SCHEMA_INVALID", error.code)
    }

    @Test
    fun RelayContractViolationFallsBackToLocalTest() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(validOpenAiChatCompletion().replace("Slow down first.", "pua manipulate force her to reply.")))
        server.start()

        val error = catchCloudError { provider(server.url("/v1").toString()).generateTacticalReply(promptInput()) }

        assertEquals("CLOUD_CONTRACT_VIOLATION", error.code)
    }

    private fun provider(baseUrl: String, client: OkHttpClient = OkHttpClient()): OpenAICompatibleProvider = OpenAICompatibleProvider(
        OpenAICompatibleConfig(
            baseUrl = baseUrl,
            apiKey = "placeholder",
            model = "gpt-5.5",
            timeoutSeconds = 6
        ),
        client = client
    )

    private suspend fun catchCloudError(block: suspend () -> Unit): CloudAnalysisException {
        val error = runCatching { block() }.exceptionOrNull()
        assertNotNull(error)
        return error as CloudAnalysisException
    }

    private fun promptInput(): TacticalPromptInput {
        val context = ContextAssembler().assemble(
            currentScreenMessages = listOf(
                textNode("other-1", Speaker.OTHER, "hello", 1),
                textNode("me-2", Speaker.ME, "I am here", 2),
                textNode("other-3", Speaker.OTHER, "today was hard", 3),
                textNode("me-4", Speaker.ME, "tell me", 4),
                textNode("other-5", Speaker.OTHER, "I need someone to listen", 5)
            ),
            userPersonaCorpus = UserPersonaCorpus(
                id = "test",
                name = "test",
                enabled = false,
                identityCards = emptyList(),
                storyCards = emptyList(),
                styleRules = emptyList(),
                riskRules = emptyList()
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        return TacticalPromptInput(context, decision)
    }

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

    private fun validOpenAiChatCompletion(fenced: Boolean = false): String {
        val content = if (fenced) "```json\n${validCloudResponse()}\n```" else validCloudResponse()
        val escaped = content
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
        return """{"id":"chatcmpl-test","choices":[{"message":{"role":"assistant","content":"$escaped"}}]}"""
    }
}
