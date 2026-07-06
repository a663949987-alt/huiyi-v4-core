package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.cloud.RelayEndpointBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

enum class DeepSeekPlaybookModel(val modelId: String) {
    V4_FLASH("deepseek-v4-flash"),
    V4_PRO("deepseek-v4-pro"),
    GPT_5_4("gpt-5.4"),
    GPT_5_5("gpt-5.5");

    companion object {
        fun fromModelId(modelId: String): DeepSeekPlaybookModel =
            entries.firstOrNull { it.modelId.equals(modelId.trim(), ignoreCase = true) } ?: GPT_5_4
    }
}

data class NormalizedConversationJson(
    val json: String
)

data class DeepSeekProviderConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String = DeepSeekPlaybookModel.GPT_5_4.modelId,
    val timeoutMs: Long = 20_000L
)

data class DeepSeekProviderResult(
    val httpStatus: Int,
    val latencyMs: Long,
    val responseBody: String
)

class DeepSeekProvider(
    private val config: DeepSeekProviderConfig,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        .build()
) {
    fun buildRequestBody(input: NormalizedConversationJson): String = """
        {
          "model": "${config.model}",
          "max_tokens": 3500,
          "temperature": 0.25,
          "response_format": {"type": "json_object"},
          "messages": [
            {
              "role": "system",
              "content": "You are Huiyi RelationshipPlaybookGenerator. Return strict compact JSON only. Required fields: stage,currentFrame,passiveNext,activeExpression,characterArcPlan,next2StepBranches,risk,fallback,expiresWhen. passiveNext and activeExpression must each include exactly 3 short Chinese routes. Each route needs slot,message,routeFamily,why,riskLevel,fallbackMove. message <= 34 Chinese chars; why <= 18 chars; fallbackMove <= 18 chars. For planning/reality/stability/future/responsibility topics, activeExpression must include routeFamily=ARC_REVEAL and CO_CREATE. next2StepBranches can be an empty array. No images. Use only the normalized conversation JSON."
            },
            {
              "role": "user",
              "content": ${input.json.jsonString()}
            }
          ]
        }
    """.trimIndent()

    fun generate(input: NormalizedConversationJson): Result<DeepSeekProviderResult> = runCatching {
        require(config.apiKey.isNotBlank()) { "api_key_missing" }
        require(!input.json.contains("imageBase64", ignoreCase = true)) { "deepseek_playbook_accepts_normalized_conversation_only" }
        val startedAt = System.currentTimeMillis()
        val request = Request.Builder()
            .url(RelayEndpointBuilder.chatCompletionsUrl(config.baseUrl))
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(buildRequestBody(input).toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            DeepSeekProviderResult(
                httpStatus = response.code,
                latencyMs = System.currentTimeMillis() - startedAt,
                responseBody = response.body?.string().orEmpty()
            )
        }
    }

    private fun String.jsonString(): String =
        "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
