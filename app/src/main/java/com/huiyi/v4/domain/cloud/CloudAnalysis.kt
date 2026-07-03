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
import kotlinx.serialization.json.add
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

object HuiyiTacticalContract {
    const val VERSION = "HuiyiTacticalContract-v1"
    const val SCHEMA_VERSION = 1
}

object CloudProviderType {
    const val OPENAI_COMPATIBLE_RELAY = "OPENAI_COMPATIBLE_RELAY"
}

data class CloudRuntimeSettings(
    val cloudEnabled: Boolean = false,
    val providerType: String = CloudProviderType.OPENAI_COMPATIBLE_RELAY,
    val baseUrl: String = "",
    val model: String = "gpt-5.5",
    val timeoutMs: Long = 6000,
    val relayApiKeyConfigured: Boolean = false,
    val relayApiKeyStoredSecurely: Boolean = false,
    val relaySecureStorageAvailable: Boolean = false,
    val relayApiKeyStorageMode: String = "DEBUG_ONLY_INSECURE_STORAGE"
) {
    val relayBaseUrlConfigured: Boolean get() = baseUrl.isNotBlank()
}

data class CloudAnalysisConfig(
    val cloudEnabled: Boolean = false,
    val providerType: String = CloudProviderType.OPENAI_COMPATIBLE_RELAY,
    val endpoint: String = "",
    val model: String = "gpt-5.5",
    val timeoutMs: Long = 6000,
    val privateMode: Boolean = false,
    val fallbackToLocal: Boolean = true,
    val clientId: String = "",
    val apiKey: String = "",
    val relayApiKeyStoredSecurely: Boolean = false
) {
    val endpointConfigured: Boolean get() = endpoint.isNotBlank()
    val relayApiKeyConfigured: Boolean get() = apiKey.isNotBlank()
    val configuredAndEnabled: Boolean get() = cloudEnabled && endpointConfigured && relayApiKeyConfigured && relayApiKeyStoredSecurely
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
    val apiCalled: Boolean = false,
    val cloudContractVersion: String = HuiyiTacticalContract.VERSION,
    val cloudContractValidationResult: String = "NOT_RUN",
    val providerType: String = CloudProviderType.OPENAI_COMPATIBLE_RELAY,
    val relayBaseUrlConfigured: Boolean = false,
    val relayApiKeyConfigured: Boolean = false,
    val relayApiKeyStoredSecurely: Boolean = false,
    val relayApiKeyExposedInRepo: Boolean = false,
    val relayApiKeyExposedInApk: Boolean = false
) {
    companion object {
        fun skipped(config: CloudAnalysisConfig, reason: String, decisionSource: String): CloudAnalysisTrace = CloudAnalysisTrace(
            cloudEnabled = config.cloudEnabled,
            endpointConfigured = config.endpointConfigured,
            cloudSkippedReason = reason,
            decisionSource = decisionSource,
            cloudContractValidationResult = "NOT_RUN",
            providerType = config.providerType,
            relayBaseUrlConfigured = config.endpointConfigured,
            relayApiKeyConfigured = config.relayApiKeyConfigured,
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely
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
            apiCalled = true,
            cloudContractValidationResult = "PASS",
            providerType = config.providerType,
            relayBaseUrlConfigured = config.endpointConfigured,
            relayApiKeyConfigured = config.relayApiKeyConfigured,
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely
        )

        fun fallback(
            config: CloudAnalysisConfig,
            errorCode: String,
            latencyMs: Long?,
            validationResult: String = "NOT_RUN"
        ): CloudAnalysisTrace = CloudAnalysisTrace(
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
            apiCalled = false,
            cloudContractValidationResult = validationResult,
            providerType = config.providerType,
            relayBaseUrlConfigured = config.endpointConfigured,
            relayApiKeyConfigured = config.relayApiKeyConfigured,
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely
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
            .post(body.toRequestBody("application/json".toMediaType()))
        if (clientId.isNotBlank()) requestBuilder.header("X-Huiyi-Client-Id", clientId)
        if (clientToken.isNotBlank()) requestBuilder.header("Authorization", "Bearer $clientToken")
        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) error("SERVER_ERROR:${response.code}")
            return response.body?.string() ?: error("CLOUD_SCHEMA_INVALID:empty_body")
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
                clientToken = config.apiKey
            )
        } catch (error: java.net.SocketTimeoutException) {
            throw CloudAnalysisException("TIMEOUT", error)
        } catch (error: java.io.IOException) {
            throw CloudAnalysisException("NETWORK", error)
        } catch (error: IllegalStateException) {
            throw CloudAnalysisException(error.message?.substringBefore(":") ?: "SERVER_ERROR", error)
        }
        mapper.parseResponse(responseJson, System.currentTimeMillis() - startedAt, input.lastSpeakerDecision.lastSpeaker)
    }
}

class CloudAnalysisException(val code: String, cause: Throwable? = null) : RuntimeException(code, cause)

class CloudTacticalDecisionMapper(
    private val contractValidator: CloudTacticalResponseValidator = CloudTacticalResponseValidator()
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun buildRequest(input: CloudAnalysisInput, config: CloudAnalysisConfig): String {
        val messages = input.context.currentScreenMessages
            .filter { it.isEffectiveChatMessage && it.speaker in setOf(Speaker.ME, Speaker.OTHER) }
            .takeLast(12)
        val root = buildJsonObject {
            put("schemaVersion", HuiyiTacticalContract.SCHEMA_VERSION)
            put("contractVersion", HuiyiTacticalContract.VERSION)
            put("sessionId", input.sessionId)
            put("appVersionName", input.appVersionName)
            put("appVersionCode", input.appVersionCode)
            put("targetAppPackage", input.capture.snapshot.appPackage.orEmpty())
            put("providerType", config.providerType)
            put("model", config.model)
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
            put("messageStatus", buildJsonObject {
                put("lastMeDeliveryStatus", input.lastMeDeliveryStatus)
                put("lastMeReadStatus", input.lastMeReadStatus)
            })
            put("localSafetyGate", buildJsonObject {
                put("meNeverCallsCloud", true)
                put("unknownNeverCallsCloud", true)
                put("unsupportedAppNeverCallsCloud", true)
                put("cloudCannotOverrideLastMeWait", true)
            })
            put("privacy", buildJsonObject {
                put("privateMode", config.privateMode)
                put("redactedForPublicLog", true)
            })
        }
        return root.toString()
    }

    fun parseResponse(responseJson: String, latencyMs: Long, actualLastSpeaker: Speaker? = Speaker.OTHER): CloudAnalysisOutput {
        val root = runCatching { extractContractJson(responseJson) }
            .getOrElse { throw CloudAnalysisException("CLOUD_SCHEMA_INVALID", it) }
        contractValidator.validate(root, actualLastSpeaker).getOrElse { throw it }
        val decisionType = root.string("decisionType").toDecisionType()
        val routes = root["routes"]?.jsonArray.orEmpty().mapIndexed { index, element ->
            element.toRoute(index)
        }
        val decision = TacticalDecision(
            decisionType = decisionType,
            situation = root.string("situation").ifBlank { "cloud analysis" },
            coreInsight = coCreationSummary(root["coCreationPoint"]?.jsonObject),
            userLikelyMistake = root.string("userLikelyMistake"),
            bestMove = root.string("bestMove").ifBlank { routes.firstOrNull()?.message.orEmpty() },
            avoidMoves = root["avoidMoves"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
            coCreationOpportunity = null,
            shouldUseUserStory = false,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(
                intensity = root["intensityPolicy"]?.jsonObject?.string("level").orEmpty().toIntensity(),
                riskLevel = routes.firstOrNull()?.riskLevel ?: RiskLevel.LOW,
                riskWarning = root.string("riskWarning").ifBlank { null },
                fallbackMove = root.string("fallbackMove")
            ),
            fallbackMove = root.string("fallbackMove")
        )
        return CloudAnalysisOutput(root.string("cloudRequestId").ifBlank { UUID.randomUUID().toString() }, decision, routes, latencyMs)
    }

    fun extractContractJson(responseJson: String): JsonObject {
        val parsed = json.parseToJsonElement(responseJson)
        val direct = parsed.jsonObject
        val completionContent = direct["choices"]?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.contentOrNull
        return if (completionContent.isNullOrBlank()) {
            direct
        } else {
            json.parseToJsonElement(stripJsonFence(completionContent)).jsonObject
        }
    }

    private fun stripJsonFence(content: String): String {
        val trimmed = content.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun JsonElement.toRoute(index: Int): ReplyRoute {
        val obj = jsonObject
        val risk = obj.string("riskLevel").toRisk()
        return ReplyRoute(
            id = obj.string("id").ifBlank { "cloud-route-$index" },
            name = obj.string("slot").ifBlank { obj.string("name").ifBlank { "route-${index + 1}" } },
            routeType = obj.string("routeType").toRouteType(),
            tag = "cloud",
            message = obj.string("message").trim(),
            intensity = InfluenceIntensity.LOW,
            riskLevel = risk,
            riskWarning = obj.string("riskWarning").ifBlank { null },
            expectedEffect = obj.string("why"),
            fallbackMove = obj.string("fallbackMove"),
            recommended = index == 0
        )
    }

    private fun coCreationSummary(point: JsonObject?): String {
        if (point == null) return ""
        val type = point.string("type")
        val evidence = point.string("evidence")
        val meaning = point.string("meaning")
        return listOf(type, evidence, meaning).filter { it.isNotBlank() }.joinToString(" / ")
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
        "EMPATHY" -> ReplyRouteType.EMPATHY
        "WARM_UP" -> ReplyRouteType.WARM_UP
        "WITHDRAW" -> ReplyRouteType.COOL_DOWN
        "LIGHT" -> ReplyRouteType.STABLE
        "REPAIR" -> ReplyRouteType.REPAIR
        "DIRECT" -> ReplyRouteType.DIRECT
        else -> ReplyRouteType.STABLE
    }
}

open class CloudTacticalResponseValidator {
    fun validate(root: JsonObject, actualLastSpeaker: Speaker? = Speaker.OTHER): Result<Unit> = runCatching {
        if (actualLastSpeaker == Speaker.ME) throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
        if (actualLastSpeaker == Speaker.UNKNOWN) throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
        if (root["schemaVersion"]?.jsonPrimitive?.intOrNull != HuiyiTacticalContract.SCHEMA_VERSION) {
            throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        }
        val decisionType = root.string("decisionType")
        if (decisionType !in allowedDecisionTypes) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        val family = root.string("decisionTypeFamily")
        if (family !in allowedFamilies) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (decisionType == "CONTEXT_REQUIRED" && family != "CONTEXT_REQUIRED") {
            throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
        }
        if (decisionType in replyDecisionTypes && family != "REPLY_ROUTES") {
            throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
        }

        val coCreationPoint = root["coCreationPoint"]?.jsonObject ?: throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (coCreationPoint["exists"]?.jsonPrimitive?.booleanOrNull == null) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (coCreationPoint.string("type").isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (coCreationPoint.string("evidence").isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (coCreationPoint.string("meaning").isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        requireNonBlank(root, "userLikelyMistake")
        requireNonBlank(root, "bestMove")
        root["intensityPolicy"]?.jsonObject?.let { intensity ->
            if (intensity.string("level") !in allowedIntensity) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            requireNonBlank(intensity, "reason")
        } ?: throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (!root.containsKey("riskWarning")) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        requireNonBlank(root, "fallbackMove")

        val routes = root["routes"]?.jsonArray ?: throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (decisionType in replyDecisionTypes && routes.size != 5) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
        if (decisionType == "CONTEXT_REQUIRED" && routes.isNotEmpty()) throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")

        val seenMessages = mutableSetOf<String>()
        routes.forEach { element ->
            val route = element.jsonObject
            val message = route.string("message").trim()
            val why = route.string("why").trim()
            if (message.isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            if (why.isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            if (route.string("riskLevel").ifBlank { "LOW" } !in allowedRisk) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
            if (!seenMessages.add(message)) throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
            validateSafetyText(message)
            validateSafetyText(why)
            validateSafetyText(route.string("fallbackMove"))
        }
        validateSafetyText(root.string("userLikelyMistake"))
        validateSafetyText(root.string("bestMove"))
        validateSafetyText(root.string("fallbackMove"))
    }

    private fun requireNonBlank(root: JsonObject, name: String) {
        if (root.string(name).isBlank()) throw CloudAnalysisException("CLOUD_SCHEMA_INVALID")
    }

    private fun validateSafetyText(text: String) {
        val normalized = text.lowercase()
        if (bannedFragments.any { normalized.contains(it) }) {
            throw CloudAnalysisException("CLOUD_CONTRACT_VIOLATION")
        }
    }

    private fun JsonObject.string(name: String): String = this[name]?.jsonPrimitive?.contentOrNull.orEmpty()

    private companion object {
        val allowedDecisionTypes = setOf("NORMAL_REPLY", "EMPATHY_FIRST", "CONTEXT_REQUIRED")
        val replyDecisionTypes = setOf("NORMAL_REPLY", "EMPATHY_FIRST")
        val allowedFamilies = setOf("REPLY_ROUTES", "CONTEXT_REQUIRED")
        val allowedIntensity = setOf("LOW", "MEDIUM", "HIGH")
        val allowedRisk = setOf("LOW", "MEDIUM", "HIGH")
        val bannedFragments = listOf(
            "auto send",
            "autosend",
            "sent for you",
            "i sent it",
            "pua",
            "manipulate",
            "force her",
            "force him",
            "must reply",
            "自动发送",
            "替你发送",
            "我帮你发",
            "逼迫",
            "跪舔",
            "拿捏",
            "操控",
            "不回就是"
        )
    }
}

class HuiyiTacticalContractValidator : CloudTacticalResponseValidator()
