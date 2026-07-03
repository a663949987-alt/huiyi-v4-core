package com.huiyi.v4.domain.cloud

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.InfluenceProfile
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.LastSpeakerDecision
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CloudAnalysisConfig(
    val cloudEnabled: Boolean = true,
    val endpoint: String = "",
    val timeoutMs: Long = 6000,
    val privateMode: Boolean = false,
    val fallbackToLocal: Boolean = true,
    val clientId: String = "",
    val clientToken: String = ""
) {
    val endpointConfigured: Boolean get() = endpoint.isNotBlank()
    val configuredAndEnabled: Boolean get() = cloudEnabled && endpointConfigured
}

data class CloudAnalysisTrace(
    val cloudEnabled: Boolean = false,
    val endpointConfigured: Boolean = false,
    val cloudAttempted: Boolean = false,
    val cloudSkippedReason: String? = "CLOUD_NOT_CONFIGURED",
    val cloudRequestId: String? = null,
    val cloudSuccess: Boolean = false,
    val cloudLatencyMs: Long? = null,
    val cloudErrorCode: String? = null,
    val cloudFallbackUsed: Boolean = false,
    val decisionSource: String = "LOCAL_FALLBACK",
    val modelCalled: Boolean = false,
    val apiCalled: Boolean = false
) {
    companion object {
        fun skipped(config: CloudAnalysisConfig, reason: String, decisionSource: String): CloudAnalysisTrace = CloudAnalysisTrace(
            cloudEnabled = config.cloudEnabled,
            endpointConfigured = config.endpointConfigured,
            cloudSkippedReason = reason,
            decisionSource = decisionSource
        )

        fun success(config: CloudAnalysisConfig, requestId: String?, latencyMs: Long): CloudAnalysisTrace = CloudAnalysisTrace(
            cloudEnabled = config.cloudEnabled,
            endpointConfigured = config.endpointConfigured,
            cloudAttempted = true,
            cloudSkippedReason = null,
            cloudRequestId = requestId,
            cloudSuccess = true,
            cloudLatencyMs = latencyMs,
            cloudErrorCode = null,
            cloudFallbackUsed = false,
            decisionSource = "CLOUD",
            modelCalled = true,
            apiCalled = true
        )

        fun fallback(config: CloudAnalysisConfig, errorCode: String, latencyMs: Long?): CloudAnalysisTrace = CloudAnalysisTrace(
            cloudEnabled = config.cloudEnabled,
            endpointConfigured = config.endpointConfigured,
            cloudAttempted = true,
            cloudSkippedReason = null,
            cloudSuccess = false,
            cloudLatencyMs = latencyMs,
            cloudErrorCode = errorCode,
            cloudFallbackUsed = true,
            decisionSource = "LOCAL_FALLBACK",
            modelCalled = false,
            apiCalled = false
        )
    }
}

data class CloudAnalysisInput(
    val sessionId: String,
    val appVersionName: String,
    val appVersionCode: Int,
    val capture: CurrentScreenCaptureResult,
    val context: ChatSceneContext,
    val lastSpeakerDecision: LastSpeakerDecision,
    val localDecision: TacticalDecision,
    val lastMeDeliveryStatus: String = "NONE",
    val lastMeReadStatus: String = "NONE"
)

data class CloudAnalysisOutput(
    val cloudRequestId: String?,
    val decision: TacticalDecision,
    val routes: List<ReplyRoute>,
    val latencyMs: Long
)

interface CloudAnalysisService {
    val config: CloudAnalysisConfig
    suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput>
}

interface CloudAnalysisClient {
    fun postJson(endpoint: String, body: String, timeoutMs: Long, clientId: String, clientToken: String): String
}

class OkHttpCloudAnalysisClient : CloudAnalysisClient {
    override fun postJson(endpoint: String, body: String, timeoutMs: Long, clientId: String, clientToken: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val requestBuilder = Request.Builder()
            .url(endpoint.trimEnd('/'))
            .header("X-Huiyi-Client-Id", clientId)
            .post(body.toRequestBody("application/json".toMediaType()))
        if (clientToken.isNotBlank()) requestBuilder.header("X-Huiyi-Client-Token", clientToken)
        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) error("SERVER_ERROR:${response.code}")
            return response.body?.string() ?: error("SCHEMA_INVALID:empty_body")
        }
    }
}

class CloudAnalysisRepository(
    override val config: CloudAnalysisConfig,
    private val client: CloudAnalysisClient = OkHttpCloudAnalysisClient(),
    private val mapper: CloudTacticalDecisionMapper = CloudTacticalDecisionMapper()
) : CloudAnalysisService {
    override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> = runCatching {
        val startedAt = System.currentTimeMillis()
        val requestJson = mapper.buildRequest(input, config)
        val responseJson = try {
            client.postJson(
                endpoint = config.endpoint,
                body = requestJson,
                timeoutMs = config.timeoutMs,
                clientId = config.clientId,
                clientToken = config.clientToken
            )
        } catch (error: java.net.SocketTimeoutException) {
            throw CloudAnalysisException("TIMEOUT", error)
        } catch (error: java.io.IOException) {
            throw CloudAnalysisException("NETWORK", error)
        } catch (error: IllegalStateException) {
            throw CloudAnalysisException(error.message?.substringBefore(":") ?: "SERVER_ERROR", error)
        }
        mapper.parseResponse(responseJson, System.currentTimeMillis() - startedAt)
    }
}

class CloudAnalysisException(val code: String, cause: Throwable? = null) : RuntimeException(code, cause)

class CloudTacticalDecisionMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun buildRequest(input: CloudAnalysisInput, config: CloudAnalysisConfig): String {
        val messages = input.context.currentScreenMessages
            .filter { it.isEffectiveChatMessage && it.speaker in setOf(Speaker.ME, Speaker.OTHER) }
            .takeLast(12)
        val root = buildJsonObject {
            put("schemaVersion", 1)
            put("sessionId", input.sessionId)
            put("appVersionName", input.appVersionName)
            put("appVersionCode", input.appVersionCode)
            put("targetAppPackage", input.capture.snapshot.appPackage.orEmpty())
            put("localDecision", buildJsonObject {
                put("actualLastSpeaker", input.lastSpeakerDecision.lastSpeaker?.name ?: "UNKNOWN")
                put("decisionTypeBeforeCloud", input.localDecision.decisionType.name)
                put("contextCompletenessScore", input.context.contentCompleteness.score)
                put("canDeepAnalyze", input.context.contentCompleteness.canDeepAnalyze)
            })
            put("conversation", buildJsonObject {
                put("messages", buildJsonArray {
                    messages.forEach { message ->
                        add(buildJsonObject {
                            put("id", message.id)
                            put("speaker", message.speaker.name)
                            put("textRedactedOrRawDependingOnPrivacyMode", payloadText(message.content, config.privateMode))
                            put("isEffectiveChatMessage", message.isEffectiveChatMessage)
                        })
                    }
                })
                put("lastEffectiveMessageId", input.lastSpeakerDecision.lastEffectiveMessage?.id.orEmpty())
                put("lastEffectiveSpeaker", input.lastSpeakerDecision.lastSpeaker?.name ?: "UNKNOWN")
            })
            put("userPersona", buildJsonObject {
                put("knownTags", JsonArray(emptyList()))
                put("relationshipGoal", "")
                put("stylePreference", "")
            })
            put("messageStatus", buildJsonObject {
                put("lastMeDeliveryStatus", input.lastMeDeliveryStatus)
                put("lastMeReadStatus", input.lastMeReadStatus)
            })
            put("privacy", buildJsonObject {
                put("privateMode", config.privateMode)
                put("redactedForPublicLog", true)
            })
        }
        return root.toString()
    }

    fun parseResponse(responseJson: String, latencyMs: Long): CloudAnalysisOutput {
        val root = json.parseToJsonElement(responseJson).jsonObject
        val decisionType = root.string("decisionType").toDecisionType()
        val routes = root["routes"]?.jsonArray.orEmpty().mapIndexed { index, element ->
            element.toRoute(index)
        }
        if (decisionType in setOf(TacticalDecisionType.NORMAL_REPLY, TacticalDecisionType.EMPATHY_FIRST) && routes.size != 5) {
            throw CloudAnalysisException("SCHEMA_INVALID")
        }
        if (routes.any { it.message.isBlank() || it.message.length > 160 }) {
            throw CloudAnalysisException("SCHEMA_INVALID")
        }
        val decision = TacticalDecision(
            decisionType = decisionType,
            situation = root.string("situation").ifBlank { "cloud analysis" },
            coreInsight = root.string("coreInsight"),
            userLikelyMistake = root.string("userLikelyMistake").ifBlank { null },
            bestMove = root.string("bestMove").ifBlank { routes.firstOrNull()?.message.orEmpty() },
            avoidMoves = root["avoidMoves"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
            coCreationOpportunity = null,
            shouldUseUserStory = false,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(
                intensity = root.string("influenceIntensity").toIntensity(),
                riskLevel = root.string("riskLevel").toRisk(),
                riskWarning = root.string("riskWarning").ifBlank { null },
                fallbackMove = root.string("fallbackMove").ifBlank { null }
            ),
            fallbackMove = root.string("fallbackMove").ifBlank { null }
        )
        return CloudAnalysisOutput(root.string("cloudRequestId").ifBlank { UUID.randomUUID().toString() }, decision, routes, latencyMs)
    }

    private fun JsonElement.toRoute(index: Int): ReplyRoute {
        val obj = jsonObject
        val risk = obj.string("riskLevel").toRisk()
        return ReplyRoute(
            id = obj.string("id").ifBlank { "cloud-route-$index" },
            name = obj.string("name").ifBlank { "路线${index + 1}" },
            routeType = obj.string("routeType").toRouteType(),
            tag = "云端",
            message = obj.string("message").trim(),
            intensity = InfluenceIntensity.LOW,
            riskLevel = risk,
            riskWarning = obj.string("riskWarning").ifBlank { null },
            expectedEffect = obj.string("why").ifBlank { null },
            fallbackMove = obj.string("fallbackMove").ifBlank { null },
            recommended = index == 0
        )
    }

    private fun payloadText(content: MessageContent, privateMode: Boolean): String = when (content) {
        is MessageContent.Text -> if (privateMode) content.text.take(500) else content.text.take(120)
        is MessageContent.Voice -> "[voice:${content.transcriptStatus}]"
        is MessageContent.Image -> "[image:${content.descriptionStatus}]"
        is MessageContent.Video -> "[video]"
        is MessageContent.Sticker -> "[sticker:${content.meaningStatus}]"
    }

    private fun JsonObject.string(name: String): String = this[name]?.jsonPrimitive?.contentOrNull.orEmpty()
    private fun String.toDecisionType(): TacticalDecisionType = runCatching { TacticalDecisionType.valueOf(ifBlank { "NORMAL_REPLY" }) }.getOrDefault(TacticalDecisionType.NORMAL_REPLY)
    private fun String.toRisk(): RiskLevel = runCatching { RiskLevel.valueOf(ifBlank { "LOW" }) }.getOrDefault(RiskLevel.LOW)
    private fun String.toIntensity(): InfluenceIntensity = runCatching { InfluenceIntensity.valueOf(ifBlank { "LOW" }) }.getOrDefault(InfluenceIntensity.LOW)
    private fun String.toRouteType(): ReplyRouteType = when (uppercase()) {
        "CO_CREATION" -> ReplyRouteType.CO_CREATION
        "WARM_UP" -> ReplyRouteType.WARM_UP
        "WITHDRAW" -> ReplyRouteType.COOL_DOWN
        "LIGHT" -> ReplyRouteType.STABLE
        "REPAIR" -> ReplyRouteType.REPAIR
        else -> ReplyRouteType.STABLE
    }
}
