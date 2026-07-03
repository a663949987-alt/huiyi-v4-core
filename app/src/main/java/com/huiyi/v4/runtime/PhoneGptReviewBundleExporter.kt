package com.huiyi.v4.runtime

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.RealDeviceReviewBundleGenerator
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.redactPrivateText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class PhoneGptReviewBundleSummary(
    val bundleVersion: Int,
    val generatedAt: Long,
    val appVersionName: String,
    val appVersionCode: Int,
    val currentOverallResult: String,
    val realDeviceFunctionalSmoke: String,
    val lastMeRealDeviceResult: String,
    val lastOtherRealDeviceResult: String,
    val latestScenarioName: String?,
    val latestActualLastSpeaker: String?,
    val latestDecisionType: String?,
    val latestRouteCount: Int?,
    val latestFailureFreshness: String,
    val latestFailureUsedForCurrentOverallResult: Boolean,
    val privacy: PhoneBundlePrivacySummary,
    val files: List<PhoneBundleFileEntry>,
    val staleReports: List<PhoneBundleStaleReport>
)

data class PhoneBundlePrivacySummary(
    val containsRawPrivateChat: Boolean,
    val containsRawScreenshot: Boolean,
    val containsApiKey: Boolean,
    val containsToken: Boolean,
    val containsKeystore: Boolean,
    val safeForPublicGitHub: Boolean
)

data class PhoneBundleFileEntry(
    val path: String,
    val exists: Boolean,
    val required: Boolean,
    val freshness: String,
    val description: String
)

data class PhoneBundleStaleReport(
    val path: String,
    val reason: String
)

data class PhoneGptReviewBundleExport(
    val zipFile: File,
    val displayPath: String,
    val publicCopyPath: String?,
    val summary: PhoneGptReviewBundleSummary,
    val shareIntent: Intent
)

data class PhoneGptReviewBundleInput(
    val appVersionName: String,
    val appVersionCode: Int,
    val packageName: String,
    val buildType: String,
    val gitCommit: String = "",
    val gitBranch: String = "",
    val targetChatAppPackage: String = "NOT_TESTED",
    val windowTitleRedacted: String = "NOT_TESTED",
    val latestSessionId: String = "none",
    val generatedAt: Long = System.currentTimeMillis(),
    val currentReviewMarkdown: String?,
    val currentScreenMarkdown: String?,
    val currentScreenJson: String?,
    val lastMeMarkdown: String?,
    val lastMeJson: String?,
    val lastOtherMarkdown: String?,
    val lastOtherJson: String?,
    val latestFailureMarkdown: String?,
    val latestFailureJson: String?,
    val diagnostics: Map<String, String> = emptyMap(),
    val visualOverlayPng: File? = null,
    val staleReports: Map<String, String> = emptyMap(),
    val includePrivateChat: Boolean = false
)

class PhoneGptReviewBundleExporter(
    private val context: Context
) {
    fun export(
        latestResult: CurrentScreenPipelineResult?,
        selectedScenario: RealDeviceScenario,
        latestSessionId: String?
    ): Result<PhoneGptReviewBundleExport> = runCatching {
        val now = System.currentTimeMillis()
        val content = RealDeviceReviewBundleGenerator().build(
            latestResult = latestResult,
            accessibilityState = HuiyiAccessibilityService.state.value,
            generatedAt = now,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            ownAppPackage = BuildConfig.APPLICATION_ID,
            scenario = selectedScenario
        )
        val debug = File(context.filesDir, "debug")
        val reviewDir = File(debug, "review")
        val input = PhoneGptReviewBundleInput(
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE,
            packageName = BuildConfig.APPLICATION_ID,
            buildType = BuildConfig.BUILD_TYPE,
            targetChatAppPackage = latestResult?.captureResult?.snapshot?.appPackage ?: "NOT_TESTED",
            windowTitleRedacted = latestResult?.captureResult?.snapshot?.windowTitle?.redactPrivateText() ?: "NOT_TESTED",
            latestSessionId = latestSessionId ?: "none",
            generatedAt = now,
            currentReviewMarkdown = content.reviewMarkdown,
            currentScreenMarkdown = content.currentScreenMarkdown,
            currentScreenJson = content.currentScreenJson,
            lastMeMarkdown = File(reviewDir, "last-me/last-me-real-device-report-for-gpt.md").readIfExists(),
            lastMeJson = File(reviewDir, "last-me/last-me-real-device-report.json").readIfExists(),
            lastOtherMarkdown = File(reviewDir, "last-other/last-other-real-device-report-for-gpt.md").readIfExists(),
            lastOtherJson = File(reviewDir, "last-other/last-other-real-device-report.json").readIfExists(),
            latestFailureMarkdown = File(debug, "latest-next-sentence-failure.md").readIfExists()
                ?: File(reviewDir, "latest-next-sentence-failure.md").readIfExists(),
            latestFailureJson = File(debug, "latest-next-sentence-failure.json").readIfExists()
                ?: File(reviewDir, "latest-next-sentence-failure.json").readIfExists(),
            diagnostics = diagnosticsFrom(reviewDir),
            visualOverlayPng = latestResult?.visualDebugResult?.overlayImagePath?.let(::File)?.takeIf { it.exists() },
            staleReports = emptyMap(),
            includePrivateChat = false
        )
        val fileName = "huiyi-phone-gpt-review-v${BuildConfig.VERSION_NAME}-${timestamp(now)}.zip"
        val outDir = File(context.filesDir, "exports/gpt_review_bundles").apply { mkdirs() }
        val zip = File(outDir, fileName)
        val summary = PhoneGptReviewBundleBuilder().build(input, zip)
        val publicCopy = PublicDownloadExporter(context).exportBinary(
            fileName = fileName,
            bytes = zip.readBytes(),
            mimeType = "application/zip",
            relativePath = "Huiyi"
        ).getOrNull()
        PhoneGptReviewBundleExport(
            zipFile = zip,
            displayPath = zip.absolutePath,
            publicCopyPath = publicCopy?.displayPath,
            summary = summary,
            shareIntent = buildShareIntent(context, zip)
        )
    }

    private fun diagnosticsFrom(reviewDir: File): Map<String, String> {
        val names = listOf(
            "accessibility-click-diagnostic-report-for-gpt.md",
            "real-device-overlay-accessibility-report-for-gpt.md",
            "real-device-overlay-accessibility-report.json",
            "parser-empty-diagnostics-for-gpt.md",
            "parser-empty-diagnostics.json"
        )
        return names.mapNotNull { name ->
            File(reviewDir, name).readIfExists()?.let { name to it }
        }.toMap()
    }

    companion object {
        fun buildShareIntent(context: Context, file: File): Intent {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            return Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        fun timestamp(timeMillis: Long): String {
            return SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(timeMillis))
        }
    }
}

class PhoneGptReviewBundleBuilder {
    fun build(input: PhoneGptReviewBundleInput, zipFile: File): PhoneGptReviewBundleSummary {
        zipFile.parentFile?.mkdirs()
        val entries = mutableListOf<PhoneBundleFileEntry>()
        val stale = input.staleReports.map { PhoneBundleStaleReport("stale/${it.key}", "Older generatedAt than current report") }
        val latestFailureFreshness = latestFailureFreshness(input)
        val currentFailureMarkdown = if (latestFailureFreshness == "CURRENT") input.latestFailureMarkdown else null
        val currentFailureJson = if (latestFailureFreshness == "CURRENT") input.latestFailureJson else null
        val staleFailureReports = buildMap {
            putAll(input.staleReports)
            if (latestFailureFreshness.startsWith("STALE")) {
                input.latestFailureMarkdown?.let { put("old-latest-next-sentence-failure.md", it) }
                input.latestFailureJson?.let { put("old-latest-next-sentence-failure.json", it) }
            }
        }
        val staleAll = stale + if (latestFailureFreshness.startsWith("STALE")) {
            listOf(PhoneBundleStaleReport("stale/old-latest-next-sentence-failure.md", latestFailureFreshness))
        } else {
            emptyList()
        }
        val currentOverall = parseField(input.currentReviewMarkdown.orEmpty(), "currentOverallResult")
            ?: parseField(input.currentScreenMarkdown.orEmpty(), "currentOverallResult")
            ?: parseField(input.currentReviewMarkdown.orEmpty(), "overall_result")
            ?: "NOT_TESTED"
        val functionalSmoke = parseField(input.currentReviewMarkdown.orEmpty(), "realDeviceFunctionalSmoke")
            ?: parseField(input.currentScreenMarkdown.orEmpty(), "realDeviceFunctionalSmoke")
            ?: "NOT_TESTED"
        val lastMeResult = parseField(input.lastMeMarkdown.orEmpty(), "lastMeResult")
            ?: parseField(input.lastMeMarkdown.orEmpty(), "scenarioResult")
            ?: parseField(input.lastMeMarkdown.orEmpty(), "overall_result")
            ?: "NOT_TESTED"
        val lastOtherResult = parseField(input.lastOtherMarkdown.orEmpty(), "lastOtherRealDeviceResult")
            ?: parseField(input.lastOtherMarkdown.orEmpty(), "scenarioResult")
            ?: parseField(input.lastOtherMarkdown.orEmpty(), "overall_result")
            ?: "NOT_TESTED"
        val privacy = PhoneBundlePrivacySummary(
            containsRawPrivateChat = input.includePrivateChat,
            containsRawScreenshot = false,
            containsApiKey = false,
            containsToken = false,
            containsKeystore = false,
            safeForPublicGitHub = !input.includePrivateChat
        )
        val summary = PhoneGptReviewBundleSummary(
            bundleVersion = 1,
            generatedAt = input.generatedAt,
            appVersionName = input.appVersionName,
            appVersionCode = input.appVersionCode,
            currentOverallResult = currentOverall,
            realDeviceFunctionalSmoke = functionalSmoke,
            lastMeRealDeviceResult = lastMeResult,
            lastOtherRealDeviceResult = lastOtherResult,
            latestScenarioName = parseField(input.currentScreenMarkdown.orEmpty(), "scenarioName"),
            latestActualLastSpeaker = parseField(input.currentScreenMarkdown.orEmpty(), "actualLastSpeaker"),
            latestDecisionType = parseField(input.currentScreenMarkdown.orEmpty(), "actualDecisionType")
                ?: parseField(input.currentScreenMarkdown.orEmpty(), "decisionType"),
            latestRouteCount = parseField(input.currentScreenMarkdown.orEmpty(), "actualRouteCount")?.toIntOrNull()
                ?: parseField(input.currentScreenMarkdown.orEmpty(), "routeCount")?.toIntOrNull(),
            latestFailureFreshness = latestFailureFreshness,
            latestFailureUsedForCurrentOverallResult = false,
            privacy = privacy,
            files = entries,
            staleReports = staleAll
        )
        val readme = writeReadme(input, summary)
        val manifest = writeManifest(input, summary)
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            addText(zip, "README_FOR_GPT.md", readme, entries, true, "CURRENT", "Phone bundle README")
            addText(zip, "phone-gpt-review-manifest.json", manifest, entries, true, "CURRENT", "Machine-readable manifest")
            addText(zip, "current/huiyi-v4-review-for-gpt.md", input.currentReviewMarkdown, entries, true, "CURRENT", "Main current review report")
            addText(zip, "current/real-device-current-screen-report-for-gpt.md", input.currentScreenMarkdown, entries, true, "CURRENT", "Current screen markdown")
            addText(zip, "current/real-device-current-screen-report.json", input.currentScreenJson, entries, true, "CURRENT", "Current screen JSON")
            addText(zip, "last-me/last-me-real-device-report-for-gpt.md", input.lastMeMarkdown ?: placeholder("last_me"), entries, true, freshness(input.lastMeMarkdown), "Last ME report")
            addText(zip, "last-me/last-me-real-device-report.json", input.lastMeJson ?: placeholderJson("last_me"), entries, true, freshness(input.lastMeJson), "Last ME JSON")
            addText(zip, "last-other/last-other-real-device-report-for-gpt.md", input.lastOtherMarkdown ?: placeholder("last_other"), entries, true, freshness(input.lastOtherMarkdown), "Last OTHER report")
            addText(zip, "last-other/last-other-real-device-report.json", input.lastOtherJson ?: placeholderJson("last_other"), entries, true, freshness(input.lastOtherJson), "Last OTHER JSON")
            addText(zip, "failure/latest-next-sentence-failure.md", currentFailureMarkdown ?: placeholder("latest_failure"), entries, false, freshness(currentFailureMarkdown), "Latest failure markdown")
            addText(zip, "failure/latest-next-sentence-failure.json", currentFailureJson ?: placeholderJson("latest_failure"), entries, false, freshness(currentFailureJson), "Latest failure JSON")
            listOf(
                "accessibility-click-diagnostic-report-for-gpt.md",
                "real-device-overlay-accessibility-report-for-gpt.md",
                "real-device-overlay-accessibility-report.json",
                "parser-empty-diagnostics-for-gpt.md",
                "parser-empty-diagnostics.json"
            ).forEach { name ->
                addText(zip, "diagnostics/$name", input.diagnostics[name] ?: placeholder(name), entries, false, freshness(input.diagnostics[name]), "Diagnostic report")
            }
            val visualIndex = buildString {
                appendLine("# Visual Debug Index")
                appendLine()
                appendLine("- current_screen_overlay.png: ${if (input.visualOverlayPng?.exists() == true) "exists" else "NOT_AVAILABLE"}")
                appendLine("- containsRawScreenshot: false")
            }
            if (input.visualOverlayPng?.exists() == true) {
                addBinary(zip, "visual/current_screen_overlay.png", input.visualOverlayPng.readBytes(), entries, false, "CURRENT", "Accessibility overlay debug image")
            } else {
                addText(zip, "visual/current_screen_overlay.png", "NOT_AVAILABLE", entries, false, "NOT_AVAILABLE", "Accessibility overlay debug image placeholder")
            }
            addText(zip, "visual/visual-debug-index.md", visualIndex, entries, true, "CURRENT", "Visual debug index")
            addText(zip, "stale/README_STALE_REPORTS.md", staleReadme(staleAll), entries, true, "CURRENT", "Stale report explanation")
            staleFailureReports.forEach { (name, text) ->
                addText(zip, "stale/$name", text, entries, false, "STALE", "Stale historical report")
            }
            addText(zip, "metadata/export-log.txt", exportLog(input, summary), entries, true, "CURRENT", "Export log")
            addText(zip, "metadata/file-list.txt", entries.joinToString("\n") { it.path }, entries, true, "CURRENT", "File list")
            addText(zip, "metadata/privacy-scan.json", privacyJson(privacy), entries, true, "CURRENT", "Privacy scan")
        }
        return summary.copy(files = entries.toList())
    }

    private fun addText(
        zip: ZipOutputStream,
        path: String,
        text: String?,
        entries: MutableList<PhoneBundleFileEntry>,
        required: Boolean,
        freshness: String,
        description: String
    ) {
        val exists = text != null && !text.startsWith("# NOT_AVAILABLE") && !text.contains("\"result\":\"NOT_TESTED\"")
        val safeText = (text ?: placeholder(path)).safeForPublic()
        zip.putNextEntry(ZipEntry(path))
        zip.write(safeText.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
        entries += PhoneBundleFileEntry(path, exists, required, freshness, description)
    }

    private fun addBinary(
        zip: ZipOutputStream,
        path: String,
        bytes: ByteArray,
        entries: MutableList<PhoneBundleFileEntry>,
        required: Boolean,
        freshness: String,
        description: String
    ) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(bytes)
        zip.closeEntry()
        entries += PhoneBundleFileEntry(path, true, required, freshness, description)
    }

    private fun writeReadme(input: PhoneGptReviewBundleInput, summary: PhoneGptReviewBundleSummary): String = buildString {
        appendLine("# Huiyi v4 Phone GPT Review Bundle")
        appendLine()
        appendLine("## Bundle basic")
        appendLine("- bundleVersion: ${summary.bundleVersion}")
        appendLine("- appVersionName: ${summary.appVersionName}")
        appendLine("- appVersionCode: ${summary.appVersionCode}")
        appendLine("- generatedAt: ${summary.generatedAt}")
        appendLine("- source: PHONE_APP_EXPORT")
        appendLine("- packageName: ${input.packageName}")
        appendLine("- targetChatAppPackage: ${input.targetChatAppPackage}")
        appendLine("- latestSessionId: ${input.latestSessionId}")
        appendLine("- staleReportCount: ${summary.staleReports.size}")
        appendLine()
        appendLine("## Current conclusion")
        appendLine("- currentOverallResult: ${summary.currentOverallResult}")
        appendLine("- realDeviceFunctionalSmoke: ${summary.realDeviceFunctionalSmoke}")
        appendLine("- lastMeRealDeviceResult: ${summary.lastMeRealDeviceResult}")
        appendLine("- lastOtherRealDeviceResult: ${summary.lastOtherRealDeviceResult}")
        appendLine("- latestScenarioName: ${summary.latestScenarioName ?: "NOT_TESTED"}")
        appendLine("- latestActualLastSpeaker: ${summary.latestActualLastSpeaker ?: "NOT_TESTED"}")
        appendLine("- latestDecisionType: ${summary.latestDecisionType ?: "NOT_TESTED"}")
        appendLine("- latestRouteCount: ${summary.latestRouteCount ?: 0}")
        appendLine("- latestFailureFreshness: ${summary.latestFailureFreshness}")
        appendLine("- latestFailureUsedForCurrentOverallResult: ${summary.latestFailureUsedForCurrentOverallResult}")
        appendLine("- permissionFalseAlarmObserved: unknown")
        appendLine("- screenshotFailureBlocksMainPath: ${parseField(input.currentScreenMarkdown.orEmpty(), "screenshotFailureBlocksMainPath") ?: "unknown"}")
        appendLine("- mainActivityOpened: ${parseField(input.currentScreenMarkdown.orEmpty(), "mainActivityOpened") ?: "unknown"}")
        appendLine("- overlayShownInTargetApp: ${parseField(input.currentScreenMarkdown.orEmpty(), "overlayShownInTargetApp") ?: "unknown"}")
        appendLine()
        appendLine("## What GPT should inspect first")
        appendLine("1. current/huiyi-v4-review-for-gpt.md")
        appendLine("2. current/real-device-current-screen-report-for-gpt.md")
        appendLine("3. last-me/last-me-real-device-report-for-gpt.md")
        appendLine("4. last-other/last-other-real-device-report-for-gpt.md")
        appendLine("5. phone-gpt-review-manifest.json")
        appendLine()
        appendLine("## Last ME summary")
        appendScenarioSummary(input.lastMeMarkdown, "ME")
        appendLine()
        appendLine("## Last OTHER summary")
        appendScenarioSummary(input.lastOtherMarkdown, "OTHER")
        appendLine()
        appendLine("## Latest failure summary")
        appendLine("- hasLatestFailure: ${input.latestFailureMarkdown != null || input.latestFailureJson != null}")
        appendLine("- errorCode: ${parseField(input.latestFailureMarkdown.orEmpty(), "errorCode") ?: "NOT_AVAILABLE"}")
        appendLine("- failedStage: ${parseField(input.latestFailureMarkdown.orEmpty(), "failedStage") ?: "NOT_AVAILABLE"}")
        appendLine("- exceptionClass: ${parseField(input.latestFailureMarkdown.orEmpty(), "exceptionClass") ?: "NOT_AVAILABLE"}")
        appendLine("- exceptionMessageRedacted: ${parseField(input.latestFailureMarkdown.orEmpty(), "exceptionMessageRedacted") ?: "NOT_AVAILABLE"}")
        appendLine()
        appendLine("## Privacy")
        appendLine("- containsRawPrivateChat: ${summary.privacy.containsRawPrivateChat}")
        appendLine("- containsRawScreenshot: ${summary.privacy.containsRawScreenshot}")
        appendLine("- containsApiKey: ${summary.privacy.containsApiKey}")
        appendLine("- containsToken: ${summary.privacy.containsToken}")
        appendLine("- containsKeystore: ${summary.privacy.containsKeystore}")
        appendLine("- safeForPublicGitHub: ${summary.privacy.safeForPublicGitHub}")
        appendLine()
        appendLine("## User note")
        appendLine("- 用户只需要把这个 zip 上传给 GPT。")
        appendLine("- 不需要再单独上传 md/json。")
        appendLine("- 如果这个 zip 被提交到公开 GitHub，必须 safeForPublicGitHub=true。")
    }

    private fun StringBuilder.appendScenarioSummary(text: String?, asserted: String) {
        appendLine("- tested: ${text != null}")
        appendLine("- userAssertedLastSpeaker: $asserted")
        appendLine("- actualLastSpeaker: ${parseField(text.orEmpty(), "actualLastSpeaker") ?: "NOT_TESTED"}")
        appendLine("- decisionType: ${parseField(text.orEmpty(), "actualDecisionType") ?: parseField(text.orEmpty(), "decisionType") ?: "NOT_TESTED"}")
        appendLine("- routeCount: ${parseField(text.orEmpty(), "actualRouteCount") ?: parseField(text.orEmpty(), "routeCount") ?: "0"}")
        appendLine("- waitPanelShown: ${parseField(text.orEmpty(), "waitPanelShown") ?: "false"}")
        appendLine("- routePanelShown: ${parseField(text.orEmpty(), "routePanelShown") ?: "false"}")
        appendLine("- staleRoutesReused: ${parseField(text.orEmpty(), "staleRoutesReused") ?: "false"}")
        appendLine("- failureCategory: ${parseField(text.orEmpty(), "failureCategory") ?: "NOT_TESTED"}")
        appendLine("- failureReason: ${parseField(text.orEmpty(), "failureReason") ?: "NOT_TESTED"}")
    }

    private fun writeManifest(input: PhoneGptReviewBundleInput, summary: PhoneGptReviewBundleSummary): String {
        return """
            {
              "project": "huiyi-v4",
              "bundleType": "PHONE_GPT_REVIEW_BUNDLE",
              "bundleVersion": ${summary.bundleVersion},
              "generatedAt": "${summary.generatedAt}",
              "source": "PHONE_APP_EXPORT",
              "app": {
                "versionName": "${input.appVersionName}",
                "versionCode": ${input.appVersionCode},
                "packageName": "${input.packageName}",
                "buildType": "${input.buildType}",
                "gitCommit": "${input.gitCommit}",
                "gitBranch": "${input.gitBranch}"
              },
              "target": {
                "chatAppPackage": "${input.targetChatAppPackage}",
                "windowTitleRedacted": "${escapeJson(input.windowTitleRedacted)}"
              },
              "currentStatus": {
                "currentOverallResult": "${summary.currentOverallResult}",
                "realDeviceFunctionalSmoke": "${summary.realDeviceFunctionalSmoke}",
                "latestScenarioName": "${summary.latestScenarioName ?: ""}",
                "latestActualLastSpeaker": "${summary.latestActualLastSpeaker ?: ""}",
                "latestDecisionType": "${summary.latestDecisionType ?: ""}",
                "latestRouteCount": ${summary.latestRouteCount ?: 0},
                "overlayShownInTargetApp": "${parseField(input.currentScreenMarkdown.orEmpty(), "overlayShownInTargetApp") ?: "unknown"}",
                "mainActivityOpened": "${parseField(input.currentScreenMarkdown.orEmpty(), "mainActivityOpened") ?: "unknown"}",
                "permissionFalseAlarmObserved": "unknown",
                "screenshotFailureBlocksMainPath": "${parseField(input.currentScreenMarkdown.orEmpty(), "screenshotFailureBlocksMainPath") ?: "unknown"}"
              },
              "latestFailureFreshness": "${summary.latestFailureFreshness}",
              "latestFailureUsedForCurrentOverallResult": ${summary.latestFailureUsedForCurrentOverallResult},
              "lastMe": ${scenarioJson(input.lastMeMarkdown, "ME", summary.lastMeRealDeviceResult)},
              "lastOther": ${scenarioJson(input.lastOtherMarkdown, "OTHER", summary.lastOtherRealDeviceResult)},
              "latestFailure": {
                "exists": ${input.latestFailureMarkdown != null || input.latestFailureJson != null},
                "errorCode": "${parseField(input.latestFailureMarkdown.orEmpty(), "errorCode") ?: ""}",
                "failedStage": "${parseField(input.latestFailureMarkdown.orEmpty(), "failedStage") ?: ""}",
                "exceptionClass": "${parseField(input.latestFailureMarkdown.orEmpty(), "exceptionClass") ?: ""}",
                "exceptionMessageRedacted": "${parseField(input.latestFailureMarkdown.orEmpty(), "exceptionMessageRedacted") ?: ""}"
              },
              "files": [${mandatoryManifestFiles().joinToString(",") { """{"path":"$it","exists":true,"required":true,"freshness":"CURRENT","description":"Phone bundle file"}""" }}],
              "staleReports": [${summary.staleReports.joinToString(",") { """{"path":"${it.path}","reason":"${it.reason}"}""" }}],
              "privacy": ${privacyJson(summary.privacy)}
            }
        """.trimIndent()
    }

    private fun scenarioJson(text: String?, asserted: String, result: String): String {
        return """
            {
              "exists": ${text != null},
              "result": "$result",
              "testIntent": "$asserted",
              "userAssertedLastSpeaker": "$asserted",
              "actualLastSpeaker": "${parseField(text.orEmpty(), "actualLastSpeaker") ?: ""}",
              "decisionType": "${parseField(text.orEmpty(), "actualDecisionType") ?: parseField(text.orEmpty(), "decisionType") ?: ""}",
              "routeCount": ${parseField(text.orEmpty(), "actualRouteCount")?.toIntOrNull() ?: parseField(text.orEmpty(), "routeCount")?.toIntOrNull() ?: 0},
              "waitPanelShown": ${parseField(text.orEmpty(), "waitPanelShown") == "true"},
              "routePanelShown": ${parseField(text.orEmpty(), "routePanelShown") == "true"},
              "chosenCaptureSource": "${parseField(text.orEmpty(), "chosenCaptureSource") ?: parseField(text.orEmpty(), "captureSource") ?: ""}",
              "postSendSettleAttempted": ${parseField(text.orEmpty(), "attempted") == "true"},
              "staleSnapshotSuspected": false,
              "staleRoutesReused": ${parseField(text.orEmpty(), "staleRoutesReused") == "true"},
              "failureCategory": "${parseField(text.orEmpty(), "failureCategory") ?: ""}",
              "failureReason": "${parseField(text.orEmpty(), "failureReason") ?: ""}"
            }
        """.trimIndent()
    }

    private fun privacyJson(privacy: PhoneBundlePrivacySummary): String {
        return """
            {
              "containsRawPrivateChat": ${privacy.containsRawPrivateChat},
              "containsRawScreenshot": ${privacy.containsRawScreenshot},
              "containsApiKey": ${privacy.containsApiKey},
              "containsToken": ${privacy.containsToken},
              "containsKeystore": ${privacy.containsKeystore},
              "safeForPublicGitHub": ${privacy.safeForPublicGitHub}
            }
        """.trimIndent()
    }

    private fun exportLog(input: PhoneGptReviewBundleInput, summary: PhoneGptReviewBundleSummary): String = buildString {
        appendLine("bundleVersion=${summary.bundleVersion}")
        appendLine("generatedAt=${summary.generatedAt}")
        appendLine("source=PHONE_APP_EXPORT")
        appendLine("appVersion=${input.appVersionName} (${input.appVersionCode})")
        appendLine("currentOverallResult=${summary.currentOverallResult}")
        appendLine("latestFailureFreshness=${summary.latestFailureFreshness}")
        appendLine("latestFailureUsedForCurrentOverallResult=${summary.latestFailureUsedForCurrentOverallResult}")
        appendLine("safeForPublicGitHub=${summary.privacy.safeForPublicGitHub}")
    }

    private fun staleReadme(stale: List<PhoneBundleStaleReport>): String = buildString {
        appendLine("# Stale Reports")
        appendLine()
        appendLine("- staleReportCount: ${stale.size}")
        appendLine("- stale reports are historical evidence only and must not affect currentOverallResult.")
        stale.forEach { appendLine("- ${it.path}: ${it.reason}") }
    }

    private fun placeholder(name: String): String = """
        # NOT_AVAILABLE

        - reportName: $name
        - result: NOT_TESTED
        - reason: NOT_GENERATED_ON_PHONE
    """.trimIndent()

    private fun placeholderJson(name: String): String = """{"reportName":"$name","result":"NOT_TESTED","reason":"NOT_GENERATED_ON_PHONE"}"""

    private fun freshness(text: String?): String = if (text == null) "NOT_AVAILABLE" else "CURRENT"

    private fun latestFailureFreshness(input: PhoneGptReviewBundleInput): String {
        val text = input.latestFailureJson.orEmpty() + "\n" + input.latestFailureMarkdown.orEmpty()
        if (text.isBlank()) return "NOT_AVAILABLE"
        val failureVersionCode = parseField(text, "versionCode")?.toIntOrNull()
        if (failureVersionCode != null && failureVersionCode < input.appVersionCode) return "STALE_OLD_VERSION"
        val failureVersionName = parseField(text, "versionName")
        if (!failureVersionName.isNullOrBlank() && failureVersionName != input.appVersionName) return "STALE_OLD_VERSION"
        if (text.contains("4.1.8b", ignoreCase = true) && text.contains("SecurityException", ignoreCase = true)) {
            return "STALE_OLD_VERSION"
        }
        return "CURRENT"
    }

    private fun parseField(text: String, key: String): String? {
        val escaped = Regex.escape(key)
        val markdown = Regex("(?m)^\\s*-\\s*$escaped\\s*:\\s*(.+)$").find(text)?.groupValues?.getOrNull(1)
        if (markdown != null) return markdown.trim().trim('"')
        val json = Regex("(?m)\"$escaped\"\\s*:\\s*\"?([^\",\\n}]+)\"?").find(text)?.groupValues?.getOrNull(1)
        return json?.trim()?.trim('"')
    }

    private fun String.safeForPublic(): String {
        return redactPrivateText(100_000)
            .replace(Regex("(?m)^(\\s*-?\\s*(text|message|lastEffectiveMessageText|lastEffectiveMessagePreview|currentSceneSummary|coreInsight|situation|bestMove)\\s*[:=]\\s*).+$"), "$1[REDACTED_PRIVATE_CHAT]")
            .replace(Regex("\"(text|message|lastEffectiveMessageText|lastEffectiveMessagePreview|currentSceneSummary|coreInsight|situation|bestMove)\"\\s*:\\s*\"(?:\\\\.|[^\"])*\""), "\"$1\":\"[REDACTED_PRIVATE_CHAT]\"")
            .lines()
            .joinToString("\n") { line ->
                if (line.startsWith("| ") && !line.contains("---") && !line.contains("rawNodeOrder")) {
                    val parts = line.split("|").toMutableList()
                    if (parts.size > 4) parts[3] = " [REDACTED_PRIVATE_CHAT] "
                    parts.joinToString("|")
                } else {
                    line
                }
            }
    }

    private fun escapeJson(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")

    private fun mandatoryManifestFiles(): List<String> = listOf(
        "README_FOR_GPT.md",
        "phone-gpt-review-manifest.json",
        "current/huiyi-v4-review-for-gpt.md",
        "current/real-device-current-screen-report-for-gpt.md",
        "current/real-device-current-screen-report.json",
        "last-me/last-me-real-device-report-for-gpt.md",
        "last-me/last-me-real-device-report.json",
        "last-other/last-other-real-device-report-for-gpt.md",
        "last-other/last-other-real-device-report.json",
        "failure/latest-next-sentence-failure.md",
        "failure/latest-next-sentence-failure.json",
        "metadata/privacy-scan.json"
    )
}

private fun File.readIfExists(): String? = takeIf { it.exists() && it.isFile }?.readText(Charsets.UTF_8)

fun ZipFile.containsEntry(name: String): Boolean = getEntry(name) != null
