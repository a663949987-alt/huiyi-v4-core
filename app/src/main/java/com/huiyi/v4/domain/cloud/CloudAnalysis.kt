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
import javax.net.ssl.SSLException

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
    val model: String = "gpt-5.4",
    val timeoutMs: Long = 20000,
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
    val model: String = "gpt-5.4",
    val timeoutMs: Long = 20000,
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
    val activeSessionId: String? = null,
    val cloudRequestSessionId: String? = null,
    val cloudResponseSessionId: String? = null,
    val preAnalysisSnapshotId: String? = null,
    val chatPackage: String? = null,
    val chatWindowHash: String? = null,
    val cloudResponseDiscarded: Boolean = false,
    val cloudResponseDiscardedReason: String? = null,
    val activeSessionChangedDuringCloud: Boolean = false,
    val panelRenderedSessionId: String? = null,
    val oneClickOneTerminalPanel: Boolean = true,
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
    val relayApiKeyExposedInApk: Boolean = false,
    val cloudNetworkFailureVisibleToUser: Boolean = false,
    val cloudRequestActuallySent: Boolean = false,
    val cloudFailureLikelyCause: String = "NONE",
    val cloudPrimaryModel: String = "",
    val cloudFinalModel: String = "",
    val cloudEscalated: Boolean = false,
    val cloudEscalationReason: String? = null,
    val cloudQualityGateResult: String = "NOT_RUN",
    val cloudQualityScore: Int? = null,
    val cloudQualityIssues: List<String> = emptyList(),
    val cloudPrimaryLatencyMs: Long? = null,
    val cloudTotalLatencyMs: Long? = null
) {
    fun withSessionBinding(
        activeSessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String,
        cloudRequestSessionId: String? = this.cloudRequestSessionId,
        cloudResponseSessionId: String? = this.cloudResponseSessionId,
        panelRenderedSessionId: String? = this.panelRenderedSessionId,
        cloudResponseDiscarded: Boolean = this.cloudResponseDiscarded,
        cloudResponseDiscardedReason: String? = this.cloudResponseDiscardedReason,
        activeSessionChangedDuringCloud: Boolean = this.activeSessionChangedDuringCloud,
        oneClickOneTerminalPanel: Boolean = this.oneClickOneTerminalPanel
    ): CloudAnalysisTrace = copy(
        activeSessionId = activeSessionId,
        cloudRequestSessionId = cloudRequestSessionId,
        cloudResponseSessionId = cloudResponseSessionId,
        preAnalysisSnapshotId = preAnalysisSnapshotId,
        chatPackage = chatPackage,
        chatWindowHash = chatWindowHash,
        panelRenderedSessionId = panelRenderedSessionId,
        cloudResponseDiscarded = cloudResponseDiscarded,
        cloudResponseDiscardedReason = cloudResponseDiscardedReason,
        activeSessionChangedDuringCloud = activeSessionChangedDuringCloud,
        oneClickOneTerminalPanel = oneClickOneTerminalPanel
    )

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
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely,
            cloudPrimaryModel = config.model,
            cloudFinalModel = config.model
        )

        fun success(config: CloudAnalysisConfig, output: CloudAnalysisOutput): CloudAnalysisTrace = CloudAnalysisTrace(
            cloudEnabled = config.cloudEnabled,
            endpointConfigured = config.endpointConfigured,
            cloudAttempted = true,
            cloudSkippedReason = null,
            cloudRequestId = output.cloudRequestId,
            cloudSuccess = true,
            cloudLatencyMs = output.latencyMs,
            cloudErrorCode = null,
            cloudFallbackUsed = false,
            decisionSource = "CLOUD",
            modelCalled = true,
            apiCalled = true,
            cloudContractValidationResult = "PASS",
            providerType = config.providerType,
            relayBaseUrlConfigured = config.endpointConfigured,
            relayApiKeyConfigured = config.relayApiKeyConfigured,
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely,
            cloudRequestActuallySent = true,
            cloudPrimaryModel = output.primaryModel.ifBlank { config.model },
            cloudFinalModel = output.modelUsed.ifBlank { config.model },
            cloudEscalated = output.escalatedFromModel != null,
            cloudEscalationReason = output.escalationReason,
            cloudQualityGateResult = output.qualityGateResult,
            cloudQualityScore = output.qualityScore,
            cloudQualityIssues = output.qualityIssues,
            cloudPrimaryLatencyMs = output.primaryLatencyMs,
            cloudTotalLatencyMs = output.latencyMs
        )

        fun fallback(
            config: CloudAnalysisConfig,
            errorCode: String,
            latencyMs: Long?,
            validationResult: String = "NOT_RUN",
            failureLikelyCause: String = "UNKNOWN",
            requestActuallySent: Boolean = true,
            primaryModel: String = config.model,
            finalModel: String = config.model,
            escalated: Boolean = false,
            escalationReason: String? = null,
            qualityGateResult: String = "NOT_RUN",
            qualityScore: Int? = null,
            qualityIssues: List<String> = emptyList(),
            primaryLatencyMs: Long? = null
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
            relayApiKeyStoredSecurely = config.relayApiKeyStoredSecurely,
            cloudNetworkFailureVisibleToUser = errorCode == "NETWORK",
            cloudRequestActuallySent = requestActuallySent,
            cloudFailureLikelyCause = if (errorCode == "NETWORK") failureLikelyCause else "NONE",
            cloudPrimaryModel = primaryModel,
            cloudFinalModel = finalModel,
            cloudEscalated = escalated,
            cloudEscalationReason = escalationReason,
            cloudQualityGateResult = qualityGateResult,
            cloudQualityScore = qualityScore,
            cloudQualityIssues = qualityIssues,
            cloudPrimaryLatencyMs = primaryLatencyMs,
            cloudTotalLatencyMs = latencyMs
        )
    }
}

data class CloudAnalysisInput(
    val sessionId: String,
    val preAnalysisSnapshotId: String = "",
    val chatPackage: String = "",
    val chatWindowHash: String = "",
    val appVersionName: String,
    val appVersionCode: Int,
    val capture: CurrentScreenCaptureResult,
    val context: ChatSceneContext,
    val lastSpeakerDecision: LastSpeakerDecision,
    val localDecision: TacticalDecision,
    val visualEvidence: CloudVisualEvidence? = null,
    val recentVisualEvidence: List<CloudVisualEvidence> = emptyList(),
    val lastMeDeliveryStatus: String = "NONE",
    val lastMeReadStatus: String = "NONE"
)

data class CloudVisualEvidence(
    val imageBase64: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val source: String,
    val capturedAt: Long = 0L,
    val role: String = "CURRENT_SCREENSHOT"
)

data class CloudAnalysisOutput(
    val sessionId: String,
    val preAnalysisSnapshotId: String,
    val chatPackage: String,
    val chatWindowHash: String,
    val cloudRequestId: String?,
    val decision: TacticalDecision,
    val routes: List<ReplyRoute>,
    val latencyMs: Long,
    val modelUsed: String = "",
    val primaryModel: String = "",
    val escalatedFromModel: String? = null,
    val escalationReason: String? = null,
    val qualityGateResult: String = "NOT_RUN",
    val qualityScore: Int? = null,
    val qualityIssues: List<String> = emptyList(),
    val primaryLatencyMs: Long? = null
)

data class CloudReplyQualityAssessment(
    val result: String,
    val score: Int,
    val issues: List<String>
) {
    val passed: Boolean get() = result == "PASS"

    companion object {
        fun pass(score: Int = 100, issues: List<String> = emptyList()) =
            CloudReplyQualityAssessment("PASS", score, issues)

        fun fail(score: Int, issues: List<String>) =
            CloudReplyQualityAssessment("FAIL", score, issues)
    }
}

class CloudReplyQualityGate {
    fun assess(output: CloudAnalysisOutput): CloudReplyQualityAssessment {
        if (output.routes.isEmpty()) return CloudReplyQualityAssessment.pass()

        var score = 100
        val issues = mutableListOf<String>()
        val messages = output.routes.map { it.message.trim() }
        val averageLength = messages.map { it.length }.average().takeIf { !it.isNaN() } ?: 0.0

        if (messages.any { it.isBlank() }) {
            score -= 40
            issues += "EMPTY_ROUTE_MESSAGE"
        }
        if (messages.distinctBy { it.lowercase() }.size != messages.size) {
            score -= 35
            issues += "DUPLICATE_ROUTE_MESSAGE"
        }
        if (messages.firstOrNull().orEmpty().length > 58) {
            score -= 18
            issues += "FIRST_REPLY_TOO_LONG"
        }
        if (averageLength > 72.0) {
            score -= 18
            issues += "VERBOSE_ROUTES"
        }
        if (messages.any { message -> aiLikeFragments.any { message.contains(it, ignoreCase = true) } }) {
            score -= 35
            issues += "AI_LIKE_PHRASE"
        }
        if (messages.any { message -> explanatoryFragments.any { message.contains(it, ignoreCase = true) } }) {
            score -= 12
            issues += "EXPLANATORY_REPLY_TEXT"
        }
        if (output.routes.none { it.riskLevel in setOf(RiskLevel.MEDIUM, RiskLevel.HIGH) }) {
            score -= 5
            issues += "NO_RELATION_PROGRESS_ROUTE"
        }

        val normalizedScore = score.coerceIn(0, 100)
        val hardFail = issues.any { it in hardFailIssues }
        return if (!hardFail && normalizedScore >= 75) {
            CloudReplyQualityAssessment.pass(normalizedScore, issues)
        } else {
            CloudReplyQualityAssessment.fail(normalizedScore, issues)
        }
    }

    private companion object {
        val hardFailIssues = setOf("EMPTY_ROUTE_MESSAGE", "DUPLICATE_ROUTE_MESSAGE", "AI_LIKE_PHRASE")
        val aiLikeFragments = listOf(
            "as an ai",
            "i understand your feelings",
            "i understand how you feel",
            "you can try",
            "i suggest",
            "it is recommended",
            "communication is important",
            "我理解你的感受",
            "我理解你的心情",
            "建议你",
            "你可以尝试",
            "作为一个ai",
            "作为ai",
            "沟通很重要"
        )
        val explanatoryFragments = listOf(
            "because ",
            "therefore ",
            "this reply",
            "this can",
            "the reason",
            "这样可以",
            "因为这样",
            "这句话"
        )
    }
}

class CloudModelRoutingPolicy {
    fun primaryModel(config: CloudAnalysisConfig, visualEvidenceAttached: Boolean = false): String {
        if (visualEvidenceAttached) {
            val configured = config.model.trim()
            return if (configured.contains("5.5", ignoreCase = true)) configured else "gpt-5.5"
        }
        val configured = config.model.trim()
        return when {
            configured.contains("5.4", ignoreCase = true) -> configured
            configured.contains("5.3", ignoreCase = true) -> configured
            else -> "gpt-5.4"
        }
    }

    fun escalationModel(config: CloudAnalysisConfig, primaryModel: String): String? {
        val configured = config.model.trim()
        val candidate = if (configured.contains("5.5", ignoreCase = true)) configured else "gpt-5.5"
        return candidate.takeUnless { it.equals(primaryModel, ignoreCase = true) }
    }

    fun primaryTimeoutMs(config: CloudAnalysisConfig, visualEvidenceAttached: Boolean = false): Long =
        if (visualEvidenceAttached) {
            escalationTimeoutMs(config)
        } else {
            config.timeoutMs.coerceIn(1_000L, 10_000L)
        }

    fun escalationTimeoutMs(config: CloudAnalysisConfig): Long =
        config.timeoutMs.coerceIn(8_000L, 32_000L)

    fun shouldEscalateError(error: CloudAnalysisException): Boolean {
        if (error.code == "TIMEOUT") return true
        if (error.code == "NETWORK" && error.likelyCause == "TIMEOUT") return true
        return error.code in setOf(
            "CLOUD_SCHEMA_INVALID",
            "CLOUD_CONTRACT_VIOLATION",
            "CLOUD_QUALITY_GATE_FAILED",
            "HTTP_400",
            "HTTP_404",
            "HTTP_429",
            "HTTP_422",
            "HTTP_5XX",
            "SERVER_ERROR"
        )
    }
}

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
            if (!response.isSuccessful) throw CloudAnalysisException(httpErrorCode(response.code))
            return response.body?.string() ?: error("CLOUD_SCHEMA_INVALID:empty_body")
        }
    }

    private fun httpErrorCode(statusCode: Int): String = when (statusCode) {
        401 -> "HTTP_401"
        403 -> "HTTP_403"
        404 -> "HTTP_404"
        429 -> "HTTP_429"
        in 500..599 -> "HTTP_5XX"
        else -> "HTTP_$statusCode"
    }
}

object RelayEndpointBuilder {
    fun chatCompletionsUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/chat/completions")) {
            trimmed
        } else {
            "$trimmed/chat/completions"
        }
    }
}

class CloudAnalysisRepository(
    override val config: CloudAnalysisConfig,
    private val client: CloudAnalysisClient = OkHttpCloudAnalysisClient(),
    private val mapper: CloudTacticalDecisionMapper = CloudTacticalDecisionMapper(),
    private val qualityGate: CloudReplyQualityGate = CloudReplyQualityGate(),
    private val modelRoutingPolicy: CloudModelRoutingPolicy = CloudModelRoutingPolicy()
) : CloudAnalysisService {
    override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> = runCatching {
        val totalStartedAt = System.currentTimeMillis()
        val visualEvidenceAttached = input.visualEvidence != null || input.recentVisualEvidence.isNotEmpty()
        val primaryModel = modelRoutingPolicy.primaryModel(config, visualEvidenceAttached)
        val escalationModel = modelRoutingPolicy.escalationModel(config, primaryModel)
        val primaryConfig = config.copy(
            model = primaryModel,
            timeoutMs = modelRoutingPolicy.primaryTimeoutMs(config, visualEvidenceAttached)
        )
        val primaryOutput = try {
            callModel(input, primaryConfig, primaryModel = primaryModel, totalStartedAt = totalStartedAt)
        } catch (error: CloudAnalysisException) {
            if (escalationModel != null && modelRoutingPolicy.shouldEscalateError(error)) {
                return@runCatching callEscalationModel(
                    input = input,
                    primaryModel = primaryModel,
                    escalationModel = escalationModel,
                    reason = error.code,
                    totalStartedAt = totalStartedAt,
                    primaryLatencyMs = null
                )
            }
            throw error
        }
        val quality = qualityGate.assess(primaryOutput)
        if (!quality.passed && escalationModel != null) {
            val escalationIssue = quality.issues.firstOrNull {
                it in setOf("AI_LIKE_PHRASE", "DUPLICATE_ROUTE_MESSAGE", "EMPTY_ROUTE_MESSAGE")
            } ?: quality.issues.firstOrNull().orEmpty().ifBlank { "FAILED" }
            return@runCatching callEscalationModel(
                input = input,
                primaryModel = primaryModel,
                escalationModel = escalationModel,
                reason = "QUALITY_GATE_$escalationIssue",
                totalStartedAt = totalStartedAt,
                primaryLatencyMs = primaryOutput.latencyMs
            )
        }
        primaryOutput.copy(
            primaryModel = primaryModel,
            qualityGateResult = quality.result,
            qualityScore = quality.score,
            qualityIssues = quality.issues,
            primaryLatencyMs = primaryOutput.latencyMs,
            latencyMs = System.currentTimeMillis() - totalStartedAt
        )
    }

    private fun callEscalationModel(
        input: CloudAnalysisInput,
        primaryModel: String,
        escalationModel: String,
        reason: String,
        totalStartedAt: Long,
        primaryLatencyMs: Long?
    ): CloudAnalysisOutput {
        val escalationConfig = config.copy(
            model = escalationModel,
            timeoutMs = modelRoutingPolicy.escalationTimeoutMs(config)
        )
        val output = callModel(
            input = input,
            effectiveConfig = escalationConfig,
            primaryModel = primaryModel,
            totalStartedAt = totalStartedAt
        )
        val quality = qualityGate.assess(output)
        if (!quality.passed) throw CloudAnalysisException("CLOUD_QUALITY_GATE_FAILED")
        return output.copy(
            primaryModel = primaryModel,
            escalatedFromModel = primaryModel,
            escalationReason = reason,
            qualityGateResult = quality.result,
            qualityScore = quality.score,
            qualityIssues = quality.issues,
            primaryLatencyMs = primaryLatencyMs,
            latencyMs = System.currentTimeMillis() - totalStartedAt
        )
    }

    private fun callModel(
        input: CloudAnalysisInput,
        effectiveConfig: CloudAnalysisConfig,
        primaryModel: String,
        totalStartedAt: Long
    ): CloudAnalysisOutput {
        val callStartedAt = System.currentTimeMillis()
        val requestJson = if (effectiveConfig.providerType == CloudProviderType.OPENAI_COMPATIBLE_RELAY) {
            mapper.buildOpenAiChatCompletionsRequest(input, effectiveConfig)
        } else {
            mapper.buildRequest(input, effectiveConfig)
        }
        val endpoint = if (effectiveConfig.providerType == CloudProviderType.OPENAI_COMPATIBLE_RELAY) {
            RelayEndpointBuilder.chatCompletionsUrl(effectiveConfig.endpoint)
        } else {
            effectiveConfig.endpoint
        }
        val responseJson = postJson(endpoint, requestJson, effectiveConfig)
        return mapper.parseResponse(
            responseJson = responseJson,
            latencyMs = System.currentTimeMillis() - callStartedAt,
            actualLastSpeaker = input.lastSpeakerDecision.lastSpeaker,
            sessionId = input.sessionId,
            preAnalysisSnapshotId = input.preAnalysisSnapshotId,
            chatPackage = input.chatPackage,
            chatWindowHash = input.chatWindowHash
        ).copy(
            modelUsed = effectiveConfig.model,
            primaryModel = primaryModel,
            latencyMs = System.currentTimeMillis() - totalStartedAt
        )
    }

    private fun postJson(endpoint: String, requestJson: String, effectiveConfig: CloudAnalysisConfig): String {
        return try {
            client.postJson(
                endpoint = endpoint,
                body = requestJson,
                timeoutMs = effectiveConfig.timeoutMs,
                clientId = effectiveConfig.clientId,
                clientToken = effectiveConfig.apiKey
            )
        } catch (error: java.net.SocketTimeoutException) {
            throw CloudAnalysisException("TIMEOUT", error)
        } catch (error: java.net.UnknownHostException) {
            throw CloudAnalysisException("NETWORK", error, "DNS_FAILED", requestActuallySent = true)
        } catch (error: SSLException) {
            throw CloudAnalysisException("NETWORK", error, "TLS_FAILED", requestActuallySent = true)
        } catch (error: CloudAnalysisException) {
            throw error
        } catch (error: java.io.IOException) {
            val message = error.message.orEmpty()
            val likelyCause = when {
                message.contains("CLEARTEXT", ignoreCase = true) -> "BASE_URL_INVALID"
                message.contains("timeout", ignoreCase = true) -> "TIMEOUT"
                else -> "UNKNOWN"
            }
            throw CloudAnalysisException("NETWORK", error, likelyCause, requestActuallySent = true)
        } catch (error: IllegalArgumentException) {
            throw CloudAnalysisException("NETWORK", error, "BASE_URL_INVALID", requestActuallySent = false)
        } catch (error: IllegalStateException) {
            throw CloudAnalysisException(error.message?.substringBefore(":") ?: "SERVER_ERROR", error)
        }
    }
}

class CloudAnalysisException(
    val code: String,
    cause: Throwable? = null,
    val likelyCause: String = "UNKNOWN",
    val requestActuallySent: Boolean = true
) : RuntimeException(code, cause)

class CloudTacticalDecisionMapper(
    private val contractValidator: CloudTacticalResponseValidator = CloudTacticalResponseValidator()
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun buildRequest(input: CloudAnalysisInput, config: CloudAnalysisConfig): String {
        return buildContractPayload(input, config).toString()
    }

    fun buildOpenAiChatCompletionsRequest(input: CloudAnalysisInput, config: CloudAnalysisConfig): String {
        val payload = buildContractPayload(input, config).toString()
        val contractInstruction = """
            Return one JSON object only. No markdown, no prose, no code fence.
            The JSON object must satisfy HuiyiTacticalContract-v1 exactly.
            Required top-level fields:
            schemaVersion=1,
            visualLastSpeaker=ME|OTHER|UNKNOWN,
            visualLastSpeakerConfidence=0..100,
            visualSpeakerEvidence,
            decisionType=NORMAL_REPLY or EMPATHY_FIRST,
            decisionTypeFamily=REPLY_ROUTES,
            situation,
            coCreationPoint { exists:boolean, type:string, evidence:string, meaning:string },
            userLikelyMistake,
            bestMove,
            intensityPolicy { level:LOW|MEDIUM|HIGH, reason:string },
            riskWarning,
            fallbackMove,
            routes exactly 5 items.
            Each route must have slot, message, why, riskLevel=LOW|MEDIUM|HIGH, fallbackMove.
            routeFamily is optional. Allowed values include EMPATHY, STABLE, CO_CREATION, WARM_UP, COOL_DOWN, DIRECT, ARC_REVEAL.
            If the other person talks about reality, planning, stability, past experience, responsibility, or future, and the user's authentic contrast/depth can be safely shown, include at least one routeFamily=ARC_REVEAL route.
            ARC_REVEAL must reveal a real, grounded contrast in the user; it must not invent a fake persona or over-explain.
            Every required string except riskWarning must be non-empty.
            The 5 route messages must be unique, safe, low-pressure, and must not auto-send anything.
            route.message is shown directly to the user as copyable chat text.
            Keep route.message short, concrete, and human. Do not put analysis, advice, or meta reasoning in route.message.
            Put reasoning only in why / coCreationPoint / userLikelyMistake.
            Avoid AI-like wording such as "I understand your feelings", "I suggest", "you can try", or generic communication advice.
            route.slot must name a user-facing strategy direction, such as 接情绪, 升温, 轻松接话, 轻问一句, 共同推进, 修复关系, 降压撤退, or 直接确认.
            Evidence priority is strict:
            1. CURRENT_SCREENSHOT image is highest authority and decides the current visible last speaker.
            2. RECENT_VISUAL_CHECKPOINT images provide previous visible context only.
            3. ACCESSIBILITY_LIGHT_LISTEN messages are auxiliary text backfill and may contain parser errors.
            Never let recent checkpoints or light-listen text override CURRENT_SCREENSHOT for visualLastSpeaker.
            If an image is provided, visually inspect the chat bubbles first. visualLastSpeaker is authoritative for this response.
            If visualLastSpeaker is ME, return decisionType=CONTEXT_REQUIRED, decisionTypeFamily=CONTEXT_REQUIRED, routes=[].
            If visualLastSpeaker is UNKNOWN, return decisionType=CONTEXT_REQUIRED, decisionTypeFamily=CONTEXT_REQUIRED, routes=[].
            Only return 5 routes when visualLastSpeaker is OTHER.
        """.trimIndent()
        val userText = "Analyze this Huiyi input. If an image is attached, use the CURRENT_SCREENSHOT to decide visualLastSpeaker before writing routes. Use recent visual checkpoints and light-listen text only as context.\n$payload"
        val visualItems = buildJsonArray {
            add(buildJsonObject {
                put("type", "text")
                put("text", userText)
            })
            input.visualEvidence?.let { evidence ->
                add(buildJsonObject {
                    put("type", "text")
                    put("text", "CURRENT_SCREENSHOT: highest authority. Decide current visible last speaker from this image.")
                })
                add(evidence.toImageContent(detail = "high"))
            }
            input.recentVisualEvidence.takeLast(2).forEachIndexed { index, evidence ->
                add(buildJsonObject {
                    put("type", "text")
                    put("text", "RECENT_VISUAL_CHECKPOINT_${index + 1}: previous visible context only. Do not override CURRENT_SCREENSHOT last speaker.")
                })
                add(evidence.toImageContent(detail = "low"))
            }
        }
        return buildJsonObject {
            put("model", config.model)
            put("temperature", if (config.model.contains("5.4", ignoreCase = true)) 0.35 else 0.25)
            put("max_tokens", 900)
            put("response_format", buildJsonObject {
                put("type", "json_object")
            })
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", contractInstruction)
                })
                add(buildJsonObject {
                    put("role", "user")
                    if (input.visualEvidence == null && input.recentVisualEvidence.isEmpty()) {
                        put("content", userText)
                    } else {
                        put("content", visualItems)
                    }
                })
            })
        }.toString()
    }

    private fun CloudVisualEvidence.toImageContent(detail: String): JsonObject = buildJsonObject {
        put("type", "image_url")
        put("image_url", buildJsonObject {
            put("url", "data:$mimeType;base64,$imageBase64")
            put("detail", detail)
        })
    }

    private fun buildContractPayload(input: CloudAnalysisInput, config: CloudAnalysisConfig): JsonObject {
        val messages = input.context.allMessages
            .filter { it.isEffectiveChatMessage && it.speaker in setOf(Speaker.ME, Speaker.OTHER) }
            .takeLast(24)
        val currentScreenIds = input.context.currentScreenMessages.mapTo(mutableSetOf()) { it.id }
        val lightListenCount = input.context.backfillMessages.count { it.source.name == "ACCESSIBILITY_LIGHT_LISTEN" }
        return buildJsonObject {
            put("schemaVersion", HuiyiTacticalContract.SCHEMA_VERSION)
            put("contractVersion", HuiyiTacticalContract.VERSION)
            put("sessionId", input.sessionId)
            put("appVersionName", input.appVersionName)
            put("appVersionCode", input.appVersionCode)
            put("targetAppPackage", input.capture.snapshot.appPackage.orEmpty())
            put("providerType", config.providerType)
            put("model", config.model)
            put("evidencePackage", buildJsonObject {
                put("schemaVersion", "huiyi-evidence-v1")
                put("currentVisualEvidence", buildJsonObject {
                    put("attached", input.visualEvidence != null)
                    put("authority", "HIGHEST_CURRENT_SCREEN")
                    put("purpose", "decide_current_visible_last_speaker")
                    put("cannotBeOverriddenByBackfill", true)
                    put("source", input.visualEvidence?.source.orEmpty())
                })
                put("recentVisualCheckpoints", buildJsonObject {
                    put("count", input.recentVisualEvidence.size)
                    put("authority", "HIGH_CONTEXT_ONLY")
                    put("purpose", "recover_previous_visible_context")
                    put("cannotOverrideCurrentScreenshot", true)
                    put("maxSent", 2)
                })
                put("lightListenBackfill", buildJsonObject {
                    put("count", lightListenCount)
                    put("authority", "AUXILIARY_TEXT_CONTEXT")
                    put("mayContainParserError", true)
                    put("cannotOverrideCurrentScreenshot", true)
                    put("cannotOverrideCurrentLastSpeaker", true)
                    put("persistedLocallyForTimeline", true)
                })
            })
            put("localDecision", buildJsonObject {
                put("actualLastSpeaker", input.lastSpeakerDecision.lastSpeaker?.name ?: "UNKNOWN")
                put("decisionTypeBeforeCloud", input.localDecision.decisionType.name)
                put("contextCompletenessScore", input.context.contentCompleteness.score)
                put("canDeepAnalyze", input.context.contentCompleteness.canDeepAnalyze)
                put("visualEvidenceAttached", input.visualEvidence != null)
                put("recentVisualCheckpointCount", input.recentVisualEvidence.size)
            })
            put("conversation", buildJsonObject {
                put("currentScreenMessageCount", input.context.currentScreenMessages.size)
                put("backfillMessageCount", input.context.backfillMessages.size)
                put("lightListenBackfillCount", lightListenCount)
                put("lastSpeakerStillBasedOnCurrentScreenOnly", true)
                put("messages", buildJsonArray {
                    messages.forEach { message ->
                        add(buildJsonObject {
                            put("id", message.id)
                            put("speaker", message.speaker.name)
                            put("source", message.source.name)
                            put("isCurrentScreen", message.id in currentScreenIds)
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
                put("imageNotWrittenToPublicReport", input.visualEvidence != null)
            })
        }
    }

    fun parseResponse(
        responseJson: String,
        latencyMs: Long,
        actualLastSpeaker: Speaker? = Speaker.OTHER,
        sessionId: String = "",
        preAnalysisSnapshotId: String = "",
        chatPackage: String = "",
        chatWindowHash: String = ""
    ): CloudAnalysisOutput {
        val root = runCatching { extractContractJson(responseJson) }
            .getOrElse { throw CloudAnalysisException("CLOUD_SCHEMA_INVALID", it) }
        val visualLastSpeaker = root.string("visualLastSpeaker").toSpeakerOrNull()
        if (visualLastSpeaker == Speaker.ME) {
            return CloudAnalysisOutput(
                sessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                cloudRequestId = root.string("cloudRequestId").ifBlank { UUID.randomUUID().toString() },
                decision = visualWaitDecision(root),
                routes = emptyList(),
                latencyMs = latencyMs
            )
        }
        if (visualLastSpeaker == Speaker.UNKNOWN) {
            return CloudAnalysisOutput(
                sessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                cloudRequestId = root.string("cloudRequestId").ifBlank { UUID.randomUUID().toString() },
                decision = visualUnknownDecision(root),
                routes = emptyList(),
                latencyMs = latencyMs
            )
        }
        contractValidator.validate(root, visualLastSpeaker ?: actualLastSpeaker).getOrElse { throw it }
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
        return CloudAnalysisOutput(
            sessionId = sessionId,
            preAnalysisSnapshotId = preAnalysisSnapshotId,
            chatPackage = chatPackage,
            chatWindowHash = chatWindowHash,
            cloudRequestId = root.string("cloudRequestId").ifBlank { UUID.randomUUID().toString() },
            decision = decision,
            routes = routes,
            latencyMs = latencyMs
        )
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
        val routeName = obj.string("slot").ifBlank { obj.string("name").ifBlank { "route-${index + 1}" } }
        val routeTypeText = listOf(
            obj.string("routeFamily"),
            obj.string("routeType"),
            routeName,
            obj.string("why"),
            obj.string("message")
        ).firstOrNull { it.isNotBlank() && !it.isGenericRouteName() }.orEmpty()
        return ReplyRoute(
            id = obj.string("id").ifBlank { "cloud-route-$index" },
            name = routeName,
            routeType = routeTypeText.toRouteType(index),
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

    private fun visualWaitDecision(root: JsonObject): TacticalDecision = TacticalDecision(
        decisionType = TacticalDecisionType.WAIT,
        situation = root.string("visualSpeakerEvidence").ifBlank { "visual cloud judged last speaker as ME" },
        coreInsight = "Visual cloud judged the latest effective message is from the user.",
        userLikelyMistake = "Continuing to send more before the other person replies.",
        bestMove = "You already replied. Wait for the other person.",
        avoidMoves = listOf("do not generate reply routes", "do not keep explaining"),
        coCreationOpportunity = null,
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        influenceProfile = InfluenceProfile(
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.LOW,
            riskWarning = null,
            fallbackMove = "Wait before sending another message."
        ),
        fallbackMove = "Wait before sending another message."
    )

    private fun visualUnknownDecision(root: JsonObject): TacticalDecision = TacticalDecision(
        decisionType = TacticalDecisionType.CONTEXT_REQUIRED,
        situation = root.string("visualSpeakerEvidence").ifBlank { "visual cloud could not safely identify the last speaker" },
        coreInsight = "Visual cloud could not safely identify who sent the latest effective message.",
        userLikelyMistake = "Generating a reply before speaker identity is clear.",
        bestMove = "Tap the latest chat area and try again.",
        avoidMoves = listOf("do not generate reply routes", "do not guess speaker identity"),
        coCreationOpportunity = null,
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        influenceProfile = InfluenceProfile(
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.MEDIUM,
            riskWarning = "speaker_unknown",
            fallbackMove = "Try again on a cleaner chat screen."
        ),
        fallbackMove = "Try again on a cleaner chat screen."
    )

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
    private fun String.toRouteType(index: Int = -1): ReplyRouteType = when {
        contains("ARC_REVEAL", ignoreCase = true) || contains("character_arc", ignoreCase = true) || contains("人物弧光") || contains("底色反差") -> ReplyRouteType.ARC_REVEAL
        contains("接情绪") || contains("情绪") || contains("empathy", ignoreCase = true) -> ReplyRouteType.EMPATHY
        contains("升温") || contains("暧昧") || contains("warm", ignoreCase = true) || contains("flirt", ignoreCase = true) -> ReplyRouteType.WARM_UP
        contains("轻松") || contains("生活") || contains("light", ignoreCase = true) || contains("daily", ignoreCase = true) -> ReplyRouteType.STABLE
        contains("轻问") || contains("问一句") || contains("question", ignoreCase = true) -> ReplyRouteType.DIRECT
        contains("共同") || contains("推进") || contains("co_creation", ignoreCase = true) || contains("co-creation", ignoreCase = true) -> ReplyRouteType.CO_CREATION
        contains("修复") || contains("repair", ignoreCase = true) -> ReplyRouteType.REPAIR
        contains("撤退") || contains("降压") || contains("withdraw", ignoreCase = true) || contains("fallback", ignoreCase = true) -> ReplyRouteType.COOL_DOWN
        uppercase() == "CO_CREATION" -> ReplyRouteType.CO_CREATION
        uppercase() == "EMPATHY" -> ReplyRouteType.EMPATHY
        uppercase() == "WARM_UP" -> ReplyRouteType.WARM_UP
        uppercase() == "ARC_REVEAL" -> ReplyRouteType.ARC_REVEAL
        uppercase() == "WITHDRAW" -> ReplyRouteType.COOL_DOWN
        uppercase() == "LIGHT" -> ReplyRouteType.STABLE
        uppercase() == "REPAIR" -> ReplyRouteType.REPAIR
        uppercase() == "DIRECT" -> ReplyRouteType.DIRECT
        else -> index.defaultRouteType()
    }

    private fun Int.defaultRouteType(): ReplyRouteType = when (this) {
        0 -> ReplyRouteType.EMPATHY
        1 -> ReplyRouteType.STABLE
        2 -> ReplyRouteType.DIRECT
        3 -> ReplyRouteType.WARM_UP
        4 -> ReplyRouteType.COOL_DOWN
        else -> ReplyRouteType.STABLE
    }

    private fun String.isGenericRouteName(): Boolean {
        val normalized = trim().lowercase()
        return normalized.isBlank() ||
            normalized.matches(Regex("""\d+""")) ||
            normalized.matches(Regex("""route[-_\s]*\d+""")) ||
            normalized in setOf("stable", "reply", "option", "answer")
    }

    private fun String.toRouteTypeLegacy(): ReplyRouteType = when (uppercase()) {
        "CO_CREATION" -> ReplyRouteType.CO_CREATION
        "EMPATHY" -> ReplyRouteType.EMPATHY
        "WARM_UP" -> ReplyRouteType.WARM_UP
        "ARC_REVEAL" -> ReplyRouteType.ARC_REVEAL
        "WITHDRAW" -> ReplyRouteType.COOL_DOWN
        "LIGHT" -> ReplyRouteType.STABLE
        "REPAIR" -> ReplyRouteType.REPAIR
        "DIRECT" -> ReplyRouteType.DIRECT
        else -> ReplyRouteType.STABLE
    }
    private fun String.toSpeakerOrNull(): Speaker? = when (uppercase()) {
        "ME" -> Speaker.ME
        "OTHER" -> Speaker.OTHER
        "UNKNOWN" -> Speaker.UNKNOWN
        else -> null
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
