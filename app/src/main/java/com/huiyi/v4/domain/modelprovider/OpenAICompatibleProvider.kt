package com.huiyi.v4.domain.modelprovider

import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.HuiyiTacticalContract
import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

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
              "max_tokens": 1200,
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
        val responseBody = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw CloudAnalysisException(httpErrorCode(response.code))
                response.body?.string() ?: throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            }
        } catch (error: CloudAnalysisException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw CloudAnalysisException("NETWORK", error, "TIMEOUT", requestActuallySent = true)
        } catch (error: UnknownHostException) {
            throw CloudAnalysisException("NETWORK", error, "DNS_FAILED", requestActuallySent = true)
        } catch (error: SSLException) {
            throw CloudAnalysisException("NETWORK", error, "TLS_FAILED", requestActuallySent = true)
        } catch (error: IllegalArgumentException) {
            throw CloudAnalysisException("NETWORK", error, "BASE_URL_INVALID", requestActuallySent = false)
        } catch (error: IOException) {
            val message = error.message.orEmpty()
            val likelyCause = when {
                message.contains("CLEARTEXT", ignoreCase = true) -> "BASE_URL_INVALID"
                message.contains("timeout", ignoreCase = true) -> "TIMEOUT"
                else -> "UNKNOWN"
            }
            throw CloudAnalysisException("NETWORK", error, likelyCause, requestActuallySent = true)
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
    private fun httpErrorCode(statusCode: Int): String = when (statusCode) {
        401 -> "HTTP_401"
        403 -> "HTTP_403"
        404 -> "HTTP_404"
        429 -> "HTTP_429"
        in 500..599 -> "HTTP_5XX"
        else -> "HTTP_$statusCode"
    }
}
