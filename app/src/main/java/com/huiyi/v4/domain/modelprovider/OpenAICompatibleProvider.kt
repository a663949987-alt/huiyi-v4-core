package com.huiyi.v4.domain.modelprovider

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
    private val config: OpenAICompatibleConfig
) : TacticalModelProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .build()

    override suspend fun generateTacticalReply(input: TacticalPromptInput): TacticalReplyResult {
        val last = input.context.lastMessage
        require(last?.speaker != Speaker.ME) { "Last speaker is ME; live API call is forbidden." }
        require(input.context.contentCompleteness.canDeepAnalyze) { "Context is incomplete; live deep analysis is forbidden." }
        require(input.decision.decisionType != TacticalDecisionType.VOICE_SUMMARY_REQUIRED) { "Voice summary is required; live API call is forbidden." }
        require(config.apiKey.isNotBlank()) { "API key is missing." }

        val prompt = buildString {
            appendLine("你是会意 v4 Core 的关系战术 HUD。")
            appendLine("只输出战术判断和 5 条简短回复路线。")
            appendLine("当前判断：${input.decision.decisionType}")
            appendLine("上下文：")
            input.context.allMessages.takeLast(12).forEach {
                appendLine("${it.speaker}: ${it.normalizedText.orEmpty()}")
            }
        }
        val json = """
            {
              "model": "${config.model}",
              "messages": [
                {"role":"user","content":${prompt.jsonString()}}
              ]
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("API call failed: ${response.code}")
        }
        return TacticalReplyResult(
            decision = input.decision,
            routes = com.huiyi.v4.domain.tactical.ReplyRouteGenerator().generate(input.context, input.decision),
            providerName = "OpenAICompatibleProvider",
            apiCalled = true
        )
    }

    private fun String.jsonString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
