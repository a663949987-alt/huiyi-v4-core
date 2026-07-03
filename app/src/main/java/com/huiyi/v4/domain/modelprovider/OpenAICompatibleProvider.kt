package com.huiyi.v4.domain.modelprovider

import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.HuiyiTacticalContract
import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class OpenAICompatibleConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val timeoutSeconds: Long
)

class OpenAICompatibleProvider(
    private val config: OpenAICompatibleConfig,
    private val mapper: CloudTacticalDecisionMapper = CloudTacticalDecisionMapper(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .build()
) : TacticalModelProvider {
    override suspend fun generateTacticalReply(input: TacticalPromptInput): TacticalReplyResult {
        val last = input.context.lastMessage
        require(last?.speaker != Speaker.ME) { "Last speaker is ME; live API call is forbidden." }
        require(input.context.contentCompleteness.canDeepAnalyze) { "Context is incomplete; live deep analysis is forbidden." }
        require(input.decision.decisionType != TacticalDecisionType.VOICE_SUMMARY_REQUIRED) { "Voice summary is required; live API call is forbidden." }
        require(config.apiKey.isNotBlank()) { "API key is missing." }

        val prompt = buildString {
            appendLine("You are Huiyi v4 relationship tactical HUD.")
            appendLine("Return only ${HuiyiTacticalContract.VERSION} JSON.")
            appendLine("Do not decide last speaker. Local last speaker is ${last?.speaker}.")
            appendLine("Current local decision: ${input.decision.decisionType}.")
            appendLine("Required fields: schemaVersion, decisionType, decisionTypeFamily, situation, coCreationPoint, userLikelyMistake, bestMove, intensityPolicy, riskWarning, fallbackMove, routes[5].")
            appendLine("Recent context:")
            input.context.allMessages.takeLast(12).forEach {
                appendLine("${it.speaker}: ${it.normalizedText.orEmpty()}")
            }
        }
        val json = """
            {
              "model": "${config.model.jsonEscape()}",
              "messages": [
                {"role":"user","content":${prompt.jsonString()}}
              ]
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(RelayEndpointBuilder.chatCompletionsUrl(config.baseUrl))
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        val responseBody = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("API call failed: ${response.code}")
            response.body?.string() ?: error("API call failed: empty body")
        }
        val parsed = mapper.parseResponse(responseBody, latencyMs = 0L, actualLastSpeaker = last?.speaker)
        return TacticalReplyResult(
            decision = parsed.decision,
            routes = parsed.routes,
            providerName = "OpenAICompatibleProvider",
            apiCalled = true
        )
    }

    private fun String.jsonString(): String = "\"" + jsonEscape().replace("\n", "\\n") + "\""
    private fun String.jsonEscape(): String = replace("\\", "\\\\").replace("\"", "\\\"")
}
