package com.huiyi.v4.runtime

import com.huiyi.v4.BuildConfig
import com.huiyi.v4.domain.pipeline.redactPrivateText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

enum class OneTapGithubUploadStage {
    IDLE,
    ZIP_GENERATING,
    ZIP_GENERATED,
    PRIVACY_SCAN_RUNNING,
    PRIVACY_SCAN_FAILED,
    UPLOAD_STARTED,
    UPLOAD_SUCCESS,
    UPLOAD_FAILED,
    FALLBACK_LOCAL_ONLY
}

data class OneTapGithubUploadState(
    val stage: OneTapGithubUploadStage = OneTapGithubUploadStage.IDLE,
    val sessionId: String? = null,
    val zipPath: String? = null,
    val uploadStartedAt: Long? = null,
    val uploadEndedAt: Long? = null,
    val githubBranch: String? = null,
    val githubCommitHash: String? = null,
    val githubReviewPath: String? = null,
    val githubReviewUrl: String? = null,
    val errorCode: String? = null,
    val errorMessageRedacted: String? = null,
    val userVisibleMessage: String = ""
)

object OneTapGithubUploadErrorCode {
    const val GITHUB_UPLOAD_DISABLED = "GITHUB_UPLOAD_DISABLED"
    const val GITHUB_UPLOAD_PRIVACY_BLOCKED = "GITHUB_UPLOAD_PRIVACY_BLOCKED"
    const val GITHUB_UPLOAD_NETWORK_FAILED = "GITHUB_UPLOAD_NETWORK_FAILED"
    const val GITHUB_UPLOAD_SERVER_REJECTED = "GITHUB_UPLOAD_SERVER_REJECTED"
    const val GITHUB_UPLOAD_AUTH_FAILED = "GITHUB_UPLOAD_AUTH_FAILED"
    const val GITHUB_UPLOAD_RATE_LIMITED = "GITHUB_UPLOAD_RATE_LIMITED"
    const val GITHUB_UPLOAD_COMMIT_FAILED = "GITHUB_UPLOAD_COMMIT_FAILED"
    const val GITHUB_UPLOAD_TIMEOUT = "GITHUB_UPLOAD_TIMEOUT"
    const val GITHUB_UPLOAD_UNKNOWN_ERROR = "GITHUB_UPLOAD_UNKNOWN_ERROR"
}

data class OneTapPrivacyScan(
    val containsRawPrivateChat: Boolean,
    val containsRawScreenshot: Boolean,
    val containsApiKey: Boolean,
    val containsToken: Boolean,
    val containsKeystore: Boolean,
    val safeForPublicGitHub: Boolean,
    val rawJson: String
)

data class OneTapGithubUploadConfig(
    val endpoint: String = BuildConfig.HUIYI_REVIEW_UPLOAD_ENDPOINT,
    val clientKey: String = BuildConfig.HUIYI_REVIEW_UPLOAD_CLIENT_KEY,
    val timeoutSeconds: Long = 30
) {
    val enabled: Boolean get() = endpoint.isNotBlank()
}

object ReviewUploadEndpointResolver {
    fun resolve(
        configuredEndpoint: String,
        lanUpdateUrl: String,
        uploadPort: Int = 8791
    ): String {
        if (configuredEndpoint.isNotBlank()) return configuredEndpoint.trim()
        val trimmed = lanUpdateUrl.trim()
        if (trimmed.isBlank()) return ""
        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return ""
        val scheme = uri.scheme?.takeIf { it == "http" || it == "https" } ?: return ""
        val host = uri.host?.takeIf { it.isNotBlank() } ?: return ""
        return "$scheme://$host:$uploadPort/api/huiyi/review-upload"
    }
}

data class OneTapGithubUploadRequest(
    val zipFile: File,
    val record: NextSentenceFlightRecord,
    val privacyScan: OneTapPrivacyScan
)

data class OneTapGithubUploadResponse(
    val githubCommitHash: String,
    val githubBranch: String,
    val githubReviewPath: String,
    val githubReviewUrl: String,
    val uploadedAt: String
)

data class OneTapGithubUploadReport(
    val versionName: String,
    val versionCode: Int,
    val sessionId: String,
    val zipGenerated: Boolean,
    val privacySafeForPublicGitHub: Boolean,
    val uploadAttempted: Boolean,
    val uploadSuccess: Boolean,
    val githubBranch: String?,
    val githubCommitHash: String?,
    val githubReviewPath: String?,
    val githubReviewUrl: String?,
    val errorCode: String?,
    val fallbackLocalZipPath: String?,
    val state: OneTapGithubUploadState,
    val privacyScan: OneTapPrivacyScan
)

interface OneTapGithubUploadGateway {
    suspend fun upload(request: OneTapGithubUploadRequest): Result<OneTapGithubUploadResponse>
}

class HttpOneTapGithubUploadGateway(
    private val config: OneTapGithubUploadConfig
) : OneTapGithubUploadGateway {
    private val client = OkHttpClient.Builder()
        .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .build()

    override suspend fun upload(request: OneTapGithubUploadRequest): Result<OneTapGithubUploadResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "oneTapFeedbackZip",
                    request.zipFile.name,
                    request.zipFile.asRequestBody("application/zip".toMediaType())
                )
                .addFormDataPart("appVersionName", BuildConfig.VERSION_NAME)
                .addFormDataPart("appVersionCode", BuildConfig.VERSION_CODE.toString())
                .addFormDataPart("sessionId", request.record.sessionId)
                .addFormDataPart("manifestSummary", request.record.summaryJson())
                .addFormDataPart("privacyScan", request.privacyScan.rawJson)
                .build()
            val builder = Request.Builder()
                .url(config.endpoint)
                .post(body)
            if (config.clientKey.isNotBlank()) {
                builder.header("X-Huiyi-Client-Key", config.clientKey)
            }
            client.newCall(builder.build()).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error(errorCodeForHttp(response.code) + ": HTTP ${response.code} ${raw.redactPrivateText(500)}")
                }
                parseResponse(raw)
            }
        }
    }

    private fun errorCodeForHttp(code: Int): String = when (code) {
        401, 403 -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_AUTH_FAILED
        408, 504 -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_TIMEOUT
        429 -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_RATE_LIMITED
        409, 422 -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_COMMIT_FAILED
        else -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_SERVER_REJECTED
    }

    private fun parseResponse(raw: String): OneTapGithubUploadResponse {
        val obj = Json.parseToJsonElement(raw).jsonObject
        return OneTapGithubUploadResponse(
            githubCommitHash = obj.stringField("githubCommitHash"),
            githubBranch = obj.stringField("githubBranch"),
            githubReviewPath = obj.stringField("githubReviewPath"),
            githubReviewUrl = obj.stringField("githubReviewUrl"),
            uploadedAt = obj.stringField("uploadedAt")
        )
    }
}

class OneTapGithubUploader(
    private val config: OneTapGithubUploadConfig,
    private val gateway: OneTapGithubUploadGateway = HttpOneTapGithubUploadGateway(config)
) {
    suspend fun upload(zipFile: File, record: NextSentenceFlightRecord): OneTapGithubUploadReport {
        val privacy = OneTapPrivacyScanner.scan(zipFile)
        if (!privacy.safeForPublicGitHub) {
            return report(
                record = record,
                privacy = privacy,
                uploadAttempted = false,
                uploadSuccess = false,
                response = null,
                errorCode = OneTapGithubUploadErrorCode.GITHUB_UPLOAD_PRIVACY_BLOCKED,
                zipFile = zipFile,
                stage = OneTapGithubUploadStage.FALLBACK_LOCAL_ONLY,
                message = "此反馈包包含私密内容，不能上传公开 GitHub。已保留本地 zip。"
            )
        }
        if (!config.enabled) {
            return report(
                record = record,
                privacy = privacy,
                uploadAttempted = false,
                uploadSuccess = false,
                response = null,
                errorCode = OneTapGithubUploadErrorCode.GITHUB_UPLOAD_DISABLED,
                zipFile = zipFile,
                stage = OneTapGithubUploadStage.FALLBACK_LOCAL_ONLY,
                message = "GitHub 自动上传暂未配置，反馈包已保存，可用系统分享发送。"
            )
        }
        val started = System.currentTimeMillis()
        val result = gateway.upload(OneTapGithubUploadRequest(zipFile, record, privacy))
        return result.fold(
            onSuccess = { response ->
                report(
                    record = record,
                    privacy = privacy,
                    uploadAttempted = true,
                    uploadSuccess = true,
                    response = response,
                    errorCode = null,
                    zipFile = zipFile,
                    stage = OneTapGithubUploadStage.UPLOAD_SUCCESS,
                    message = "已上传 GitHub，GPT 可以验收。",
                    startedAt = started,
                    endedAt = System.currentTimeMillis()
                )
            },
            onFailure = { error ->
                report(
                    record = record,
                    privacy = privacy,
                    uploadAttempted = true,
                    uploadSuccess = false,
                    response = null,
                    errorCode = classifyUploadError(error),
                    zipFile = zipFile,
                    stage = OneTapGithubUploadStage.FALLBACK_LOCAL_ONLY,
                    message = "上传 GitHub 失败，但反馈包已保存，可用系统分享发送。",
                    startedAt = started,
                    endedAt = System.currentTimeMillis(),
                    errorMessage = error.message
                )
            }
        )
    }

    private fun report(
        record: NextSentenceFlightRecord,
        privacy: OneTapPrivacyScan,
        uploadAttempted: Boolean,
        uploadSuccess: Boolean,
        response: OneTapGithubUploadResponse?,
        errorCode: String?,
        zipFile: File,
        stage: OneTapGithubUploadStage,
        message: String,
        startedAt: Long? = null,
        endedAt: Long? = null,
        errorMessage: String? = null
    ): OneTapGithubUploadReport {
        val state = OneTapGithubUploadState(
            stage = stage,
            sessionId = record.sessionId,
            zipPath = zipFile.absolutePath,
            uploadStartedAt = startedAt,
            uploadEndedAt = endedAt,
            githubBranch = response?.githubBranch,
            githubCommitHash = response?.githubCommitHash,
            githubReviewPath = response?.githubReviewPath,
            githubReviewUrl = response?.githubReviewUrl,
            errorCode = errorCode,
            errorMessageRedacted = errorMessage?.redactPrivateText(500),
            userVisibleMessage = message
        )
        return OneTapGithubUploadReport(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            sessionId = record.sessionId,
            zipGenerated = zipFile.exists(),
            privacySafeForPublicGitHub = privacy.safeForPublicGitHub,
            uploadAttempted = uploadAttempted,
            uploadSuccess = uploadSuccess,
            githubBranch = response?.githubBranch,
            githubCommitHash = response?.githubCommitHash,
            githubReviewPath = response?.githubReviewPath,
            githubReviewUrl = response?.githubReviewUrl,
            errorCode = errorCode,
            fallbackLocalZipPath = if (uploadSuccess) null else zipFile.absolutePath,
            state = state,
            privacyScan = privacy
        )
    }

    private fun classifyUploadError(error: Throwable): String {
        val text = error.message.orEmpty()
        return when {
            text.contains(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_AUTH_FAILED) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_AUTH_FAILED
            text.contains(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_RATE_LIMITED) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_RATE_LIMITED
            text.contains(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_COMMIT_FAILED) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_COMMIT_FAILED
            text.contains(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_TIMEOUT) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_TIMEOUT
            text.contains("timeout", ignoreCase = true) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_TIMEOUT
            text.contains("connect", ignoreCase = true) -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_NETWORK_FAILED
            text.contains("HTTP") -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_SERVER_REJECTED
            else -> OneTapGithubUploadErrorCode.GITHUB_UPLOAD_UNKNOWN_ERROR
        }
    }
}

object OneTapPrivacyScanner {
    fun scan(zipFile: File): OneTapPrivacyScan {
        val raw = ZipFile(zipFile).use { zip ->
            val entry = zip.getEntry("metadata/privacy-scan.json")
                ?: return unsafe("metadata/privacy-scan.json missing")
            zip.getInputStream(entry).bufferedReader().use { it.readText() }
        }
        val obj = runCatching { Json.parseToJsonElement(raw).jsonObject }.getOrNull()
        return OneTapPrivacyScan(
            containsRawPrivateChat = obj.bool("containsRawPrivateChat"),
            containsRawScreenshot = obj.bool("containsRawScreenshot"),
            containsApiKey = obj.bool("containsApiKey"),
            containsToken = obj.bool("containsToken"),
            containsKeystore = obj.bool("containsKeystore"),
            safeForPublicGitHub = obj.bool("safeForPublicGitHub"),
            rawJson = raw
        )
    }

    private fun unsafe(reason: String): OneTapPrivacyScan = OneTapPrivacyScan(
        containsRawPrivateChat = true,
        containsRawScreenshot = false,
        containsApiKey = false,
        containsToken = false,
        containsKeystore = false,
        safeForPublicGitHub = false,
        rawJson = """{"safeForPublicGitHub":false,"reason":"$reason"}"""
    )
}

object OneTapGithubUploadReportGenerator {
    fun markdown(report: OneTapGithubUploadReport): String = buildString {
        appendLine("# One Tap GitHub Upload Report")
        appendLine()
        appendLine("## User visible result")
        appendLine("- shown message: ${report.state.userVisibleMessage}")
        appendLine("- upload success: ${report.uploadSuccess}")
        appendLine("- github url: ${report.githubReviewUrl ?: "none"}")
        appendLine("- fallback zip path: ${report.fallbackLocalZipPath ?: "none"}")
        appendLine()
        appendLine("## Privacy")
        appendLine("- safeForPublicGitHub: ${report.privacySafeForPublicGitHub}")
        appendLine("- containsRawPrivateChat: ${report.privacyScan.containsRawPrivateChat}")
        appendLine("- containsRawScreenshot: ${report.privacyScan.containsRawScreenshot}")
        appendLine()
        appendLine("## Upload")
        appendLine("- stage: ${report.state.stage}")
        appendLine("- branch: ${report.githubBranch ?: "none"}")
        appendLine("- commit: ${report.githubCommitHash ?: "none"}")
        appendLine("- path: ${report.githubReviewPath ?: "none"}")
        appendLine("- errorCode: ${report.errorCode ?: "none"}")
        appendLine("- githubAutoUpload: ${if (report.errorCode == OneTapGithubUploadErrorCode.GITHUB_UPLOAD_DISABLED) "NOT_AVAILABLE_GATEWAY_NOT_CONFIGURED" else report.uploadSuccess}")
        appendLine("- localZipFallback: ${report.fallbackLocalZipPath != null}")
    }

    fun json(report: OneTapGithubUploadReport): String = """
        {
          "versionName": "${report.versionName}",
          "versionCode": ${report.versionCode},
          "sessionId": "${report.sessionId}",
          "zipGenerated": ${report.zipGenerated},
          "privacySafeForPublicGitHub": ${report.privacySafeForPublicGitHub},
          "uploadAttempted": ${report.uploadAttempted},
          "uploadSuccess": ${report.uploadSuccess},
          "githubBranch": ${report.githubBranch.jsonOrNull()},
          "githubCommitHash": ${report.githubCommitHash.jsonOrNull()},
          "githubReviewPath": ${report.githubReviewPath.jsonOrNull()},
          "githubReviewUrl": ${report.githubReviewUrl.jsonOrNull()},
          "errorCode": ${report.errorCode.jsonOrNull()},
          "fallbackLocalZipPath": ${report.fallbackLocalZipPath.jsonOrNull()}
        }
    """.trimIndent()
}

private fun JsonObject?.bool(name: String): Boolean {
    return this?.get(name)?.jsonPrimitive?.booleanOrNull ?: false
}

private fun JsonObject.stringField(name: String): String {
    return get(name)?.jsonPrimitive?.content.orEmpty()
}

private fun NextSentenceFlightRecord.summaryJson(): String = """
    {
      "sessionId": "${sessionId.escapeJson()}",
      "terminalState": "${terminalState.escapeJson()}",
      "appPackage": "${appPackage.escapeJson()}",
      "actualLastSpeaker": "${actualLastSpeaker.escapeJson()}",
      "decisionType": "${decisionType.escapeJson()}",
      "routeCount": $routeCount
    }
""".trimIndent()

private fun String?.jsonOrNull(): String = this?.let { "\"${it.escapeJson()}\"" } ?: "null"

private fun String.escapeJson(): String = replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "")
