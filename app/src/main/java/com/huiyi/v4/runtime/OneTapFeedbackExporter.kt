package com.huiyi.v4.runtime

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceCaptureSource
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.redactPrivateText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class UserFeedbackMark(
    val markedWrong: Boolean = false,
    val userCorrectionLastSpeaker: String = "NONE",
    val userNote: String = ""
)

data class NextSentenceFlightRecord(
    val sessionId: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMs: Long,
    val terminalState: String,
    val appPackage: String,
    val windowTitlePreAnalysisRedacted: String,
    val targetAppSupported: Boolean,
    val adapterName: String,
    val parserName: String,
    val preAnalysisSnapshotCaptured: Boolean,
    val postPanelSnapshotCaptured: Boolean,
    val postPanelContaminationDetected: Boolean,
    val actualLastSpeaker: String,
    val decisionType: String,
    val decisionTypeFamily: String,
    val routeCount: Int,
    val waitPanelShown: Boolean,
    val routePanelShown: Boolean,
    val contextRequiredPanelShown: Boolean,
    val loadingStillVisible: Boolean,
    val apiCalled: Boolean,
    val modelCalled: Boolean,
    val captureSource: String,
    val usedFallbackSnapshot: Boolean,
    val fallbackSnapshotAgeMs: Long?,
    val staleSnapshotSuspected: Boolean,
    val previousSessionId: String,
    val panelSessionId: String,
    val panelContentFromCurrentSession: Boolean,
    val staleRoutesReused: Boolean,
    val permissionMissingMessageShown: Boolean,
    val mainActivityOpened: Boolean,
    val overlayShownInTargetApp: Boolean,
    val errorCode: String?,
    val failedStage: String?,
    val exceptionClass: String?,
    val exceptionMessageRedacted: String?,
    val userFeedback: UserFeedbackMark = UserFeedbackMark()
) {
    fun withFeedback(feedback: UserFeedbackMark): NextSentenceFlightRecord = copy(userFeedback = feedback)
}

data class OneTapFeedbackExport(
    val zipFile: File,
    val displayPath: String,
    val publicCopyPath: String?,
    val shareIntent: Intent
)

object OneTapFeedbackZipContract {
    val requiredPaths: List<String> = listOf(
        "README_FOR_GPT.md",
        "one-tap-feedback-manifest.json",
        "latest-session/next-sentence-flight-record.json",
        "latest-session/next-sentence-flight-record-for-gpt.md",
        "current-screen/real-device-current-screen-report-for-gpt.md",
        "current-screen/real-device-current-screen-report.json",
        "recent-sessions/session-index.md",
        "diagnostics/accessibility-click-diagnostic-report-for-gpt.md",
        "diagnostics/overlay-accessibility-report-for-gpt.md",
        "diagnostics/parser-empty-diagnostics-for-gpt.md",
        "visual/current_screen_overlay.png",
        "visual/visual-debug-index.md",
        "stale/README_STALE_REPORTS.md",
        "metadata/file-list.txt",
        "metadata/privacy-scan.json",
        "metadata/app-build-info.json"
    )
}

object NextSentenceFlightRecordFactory {
    fun fromSuccess(
        result: CurrentScreenPipelineResult,
        trace: NextSentenceSessionTrace
    ): NextSentenceFlightRecord {
        val capture = result.captureResult
        val decisionType = result.tacticalDecision.decisionType
        return NextSentenceFlightRecord(
            sessionId = result.sessionId ?: trace.sessionId,
            startedAt = result.analysisStartedAt.takeIf { it > 0L } ?: trace.startedAt,
            endedAt = result.analysisEndedAt.takeIf { it > 0L } ?: trace.endedAt ?: System.currentTimeMillis(),
            durationMs = result.analysisDurationMs.takeIf { it > 0L } ?: ((trace.endedAt ?: System.currentTimeMillis()) - trace.startedAt),
            terminalState = terminalStateFor(decisionType),
            appPackage = capture?.snapshot?.appPackage ?: "unknown",
            windowTitlePreAnalysisRedacted = capture?.snapshot?.windowTitle?.redactPrivateText(120) ?: "unknown",
            targetAppSupported = capture?.snapshot?.appPackage == "com.bajiao.im.liaoqi" || capture?.snapshot?.appPackage == "com.huiyi.mockchat",
            adapterName = if (capture?.snapshot?.appPackage == "com.bajiao.im.liaoqi") "LiaoqiRealParser" else "GenericVisualBubbleParser",
            parserName = capture?.parserName ?: "NONE",
            preAnalysisSnapshotCaptured = capture != null,
            postPanelSnapshotCaptured = result.resultShownAsOverlay,
            postPanelContaminationDetected = false,
            actualLastSpeaker = result.lastSpeakerDecision.lastSpeaker?.name ?: "UNKNOWN",
            decisionType = decisionType.name,
            decisionTypeFamily = decisionTypeFamily(decisionType),
            routeCount = result.routes.size,
            waitPanelShown = result.waitPanelShown || decisionType == TacticalDecisionType.WAIT,
            routePanelShown = result.routePanelShown || result.routes.isNotEmpty(),
            contextRequiredPanelShown = decisionType in setOf(TacticalDecisionType.CONTEXT_REQUIRED, TacticalDecisionType.VOICE_SUMMARY_REQUIRED),
            loadingStillVisible = false,
            apiCalled = result.apiCalled,
            modelCalled = false,
            captureSource = capture?.captureSource?.name ?: NextSentenceCaptureSource.NONE.name,
            usedFallbackSnapshot = capture?.usedFallbackSnapshot == true,
            fallbackSnapshotAgeMs = capture?.lastStableSnapshotAgeMs,
            staleSnapshotSuspected = capture?.usedFallbackSnapshot == true && (capture.lastStableSnapshotAgeMs ?: 0L) > 2000L,
            previousSessionId = result.previousSessionId.orEmpty(),
            panelSessionId = result.panelSessionId.orEmpty(),
            panelContentFromCurrentSession = result.panelContentFromCurrentSession,
            staleRoutesReused = result.staleRoutesReused,
            permissionMissingMessageShown = false,
            mainActivityOpened = result.mainActivityOpened,
            overlayShownInTargetApp = result.overlayShownInTargetApp,
            errorCode = null,
            failedStage = null,
            exceptionClass = null,
            exceptionMessageRedacted = null
        )
    }

    fun fromFailure(trace: NextSentenceSessionTrace): NextSentenceFlightRecord {
        val terminal = when (trace.errorCode) {
            NextSentenceErrorCode.SESSION_TIMEOUT_NO_TERMINAL_STATE,
            NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK -> "TIMEOUT"
            NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND,
            NextSentenceErrorCode.ONLY_NON_CHAT_TEXT_FOUND -> "UNSUPPORTED_APP"
            else -> "CONTROLLED_FAIL"
        }
        return NextSentenceFlightRecord(
            sessionId = trace.sessionId,
            startedAt = trace.startedAt,
            endedAt = trace.endedAt ?: System.currentTimeMillis(),
            durationMs = (trace.endedAt ?: System.currentTimeMillis()) - trace.startedAt,
            terminalState = terminal,
            appPackage = trace.rootPackageName ?: trace.activePackageBeforeClick ?: "unknown",
            windowTitlePreAnalysisRedacted = trace.rootWindowTitle?.redactPrivateText(120) ?: "unknown",
            targetAppSupported = trace.rootIsTargetChatApp,
            adapterName = "NONE",
            parserName = "NONE",
            preAnalysisSnapshotCaptured = trace.parsedMessageCount > 0,
            postPanelSnapshotCaptured = false,
            postPanelContaminationDetected = false,
            actualLastSpeaker = trace.lastEffectiveSpeaker?.name ?: "UNKNOWN",
            decisionType = trace.decisionType ?: "ERROR",
            decisionTypeFamily = "ERROR",
            routeCount = trace.routeCount,
            waitPanelShown = false,
            routePanelShown = false,
            contextRequiredPanelShown = false,
            loadingStillVisible = terminal == "TIMEOUT",
            apiCalled = trace.apiCalled,
            modelCalled = false,
            captureSource = trace.captureSource.name,
            usedFallbackSnapshot = trace.usedFallbackSnapshot,
            fallbackSnapshotAgeMs = trace.lastStableSnapshotAgeMs,
            staleSnapshotSuspected = trace.usedFallbackSnapshot && (trace.lastStableSnapshotAgeMs ?: 0L) > 2000L,
            previousSessionId = "",
            panelSessionId = "",
            panelContentFromCurrentSession = false,
            staleRoutesReused = false,
            permissionMissingMessageShown = trace.permissionMissingMessageShown,
            mainActivityOpened = false,
            overlayShownInTargetApp = false,
            errorCode = trace.errorCode?.name,
            failedStage = trace.failedStage?.name,
            exceptionClass = trace.pipelineExceptionClass ?: trace.exceptionClass,
            exceptionMessageRedacted = trace.pipelineExceptionMessageRedacted ?: trace.exceptionMessageRedacted
        )
    }

    private fun terminalStateFor(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT_PANEL"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "CONTEXT_REQUIRED_PANEL"
        else -> "ROUTE_PANEL"
    }

    private fun decisionTypeFamily(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "CONTEXT_REQUIRED"
        TacticalDecisionType.NORMAL_REPLY,
        TacticalDecisionType.EMPATHY_FIRST -> "REPLY_ROUTES"
        else -> "REPLY_ROUTES"
    }
}

class OneTapFeedbackExporter(
    private val context: Context
) {
    fun export(
        latestRecord: NextSentenceFlightRecord?,
        recentRecords: List<NextSentenceFlightRecord>,
        latestResult: CurrentScreenPipelineResult?,
        latestTrace: NextSentenceSessionTrace?,
        feedback: UserFeedbackMark = UserFeedbackMark(markedWrong = true)
    ): Result<OneTapFeedbackExport> = runCatching {
        val now = System.currentTimeMillis()
        val record = (latestRecord ?: latestTrace?.let { NextSentenceFlightRecordFactory.fromFailure(it) } ?: placeholderRecord(now))
            .withFeedback(feedback)
        val fileName = "huiyi-one-tap-feedback-v${BuildConfig.VERSION_NAME}-${timestamp(now)}.zip"
        val outDir = File(context.filesDir, "exports/one_tap_feedback").apply { mkdirs() }
        val zip = File(outDir, fileName)
        ZipOutputStream(zip.outputStream()).use { stream ->
            addText(stream, "README_FOR_GPT.md", readme(record))
            addText(stream, "one-tap-feedback-manifest.json", manifest(record, now))
            addText(stream, "latest-session/next-sentence-flight-record.json", recordJson(record))
            addText(stream, "latest-session/next-sentence-flight-record-for-gpt.md", recordMarkdown(record))
            val currentReports = currentScreenReports(latestResult)
            addText(stream, "current-screen/real-device-current-screen-report-for-gpt.md", currentReports.first)
            addText(stream, "current-screen/real-device-current-screen-report.json", currentReports.second)
            addText(stream, "recent-sessions/session-index.md", sessionIndex(recentRecords, record))
            (recentRecords + record).takeLast(10).forEach { item ->
                addText(stream, "recent-sessions/session-${item.sessionId}.json", recordJson(item))
            }
            addText(stream, "diagnostics/accessibility-click-diagnostic-report-for-gpt.md", notAvailable("accessibility click diagnostics"))
            addText(stream, "diagnostics/overlay-accessibility-report-for-gpt.md", notAvailable("overlay diagnostics"))
            addText(stream, "diagnostics/parser-empty-diagnostics-for-gpt.md", notAvailable("parser diagnostics"))
            addText(stream, "visual/current_screen_overlay.png", "NOT_AVAILABLE")
            addText(stream, "visual/visual-debug-index.md", visualIndex(latestResult))
            addText(stream, "stale/README_STALE_REPORTS.md", "# Stale Reports\n\n- staleReportCount: 0\n")
            addText(stream, "metadata/file-list.txt", fixedFileList())
            addText(stream, "metadata/privacy-scan.json", privacyJson())
            addText(stream, "metadata/app-build-info.json", appBuildInfo(now))
            if (record.terminalState == "UNSUPPORTED_APP") {
                addText(stream, "unsupported-app-adaptation-report.json", unsupportedJson(record))
                addText(stream, "unsupported-app-adaptation-report-for-gpt.md", unsupportedMarkdown(record))
            }
        }
        val publicCopy = PublicDownloadExporter(context).exportBinary(
            fileName = fileName,
            bytes = zip.readBytes(),
            mimeType = "application/zip",
            relativePath = "Huiyi"
        ).getOrNull()
        OneTapFeedbackExport(
            zipFile = zip,
            displayPath = zip.absolutePath,
            publicCopyPath = publicCopy?.displayPath,
            shareIntent = buildShareIntent(zip)
        )
    }

    private fun currentScreenReports(result: CurrentScreenPipelineResult?): Pair<String, String> {
        if (result == null) return notAvailable("current screen") to """{"result":"NOT_AVAILABLE"}"""
        val markdown = EvidencePackReportGenerator().buildMarkdown(result, HuiyiAccessibilityService.state.value)
        val json = EvidencePackReportGenerator().buildJson(result, HuiyiAccessibilityService.state.value)
        return markdown.safeForPublic() to json.safeForPublic()
    }

    private fun readme(record: NextSentenceFlightRecord): String = buildString {
        appendLine("# Huiyi One Tap Feedback")
        appendLine()
        appendLine("- bundleType: ONE_TAP_FEEDBACK")
        appendLine("- appVersionName: ${BuildConfig.VERSION_NAME}")
        appendLine("- appVersionCode: ${BuildConfig.VERSION_CODE}")
        appendLine("- latestSessionId: ${record.sessionId}")
        appendLine("- terminalState: ${record.terminalState}")
        appendLine("- quickConclusion: ${quickConclusion(record)}")
        appendLine()
        appendLine("GPT should inspect `latest-session/next-sentence-flight-record.json` first.")
    }

    private fun manifest(record: NextSentenceFlightRecord, generatedAt: Long): String = """
        {
          "project": "huiyi-v4",
          "bundleType": "ONE_TAP_FEEDBACK",
          "generatedAt": "$generatedAt",
          "appVersionName": "${BuildConfig.VERSION_NAME}",
          "appVersionCode": ${BuildConfig.VERSION_CODE},
          "latestSession": {
            "sessionId": "${escape(record.sessionId)}",
            "terminalState": "${record.terminalState}",
            "appPackage": "${escape(record.appPackage)}",
            "targetAppSupported": ${record.targetAppSupported},
            "adapterName": "${record.adapterName}",
            "actualLastSpeaker": "${record.actualLastSpeaker}",
            "decisionType": "${record.decisionType}",
            "decisionTypeFamily": "${record.decisionTypeFamily}",
            "routeCount": ${record.routeCount},
            "waitPanelShown": ${record.waitPanelShown},
            "routePanelShown": ${record.routePanelShown},
            "loadingStillVisible": ${record.loadingStillVisible},
            "errorCode": "${record.errorCode ?: ""}",
            "failedStage": "${record.failedStage ?: ""}",
            "userMarkedWrong": ${record.userFeedback.markedWrong},
            "userCorrectionLastSpeaker": "${record.userFeedback.userCorrectionLastSpeaker}"
          },
          "quickConclusion": {
            "status": "${quickStatus(record)}",
            "humanReadable": "${escape(quickConclusion(record))}"
          },
          "privacy": ${privacyJson()},
          "staleReports": []
        }
    """.trimIndent()

    private fun recordMarkdown(record: NextSentenceFlightRecord): String = buildString {
        appendLine("# Next Sentence Flight Record")
        appendLine()
        appendLine("- sessionId: ${record.sessionId}")
        appendLine("- terminalState: ${record.terminalState}")
        appendLine("- appPackage: ${record.appPackage}")
        appendLine("- actualLastSpeaker: ${record.actualLastSpeaker}")
        appendLine("- decisionType: ${record.decisionType}")
        appendLine("- decisionTypeFamily: ${record.decisionTypeFamily}")
        appendLine("- routeCount: ${record.routeCount}")
        appendLine("- waitPanelShown: ${record.waitPanelShown}")
        appendLine("- routePanelShown: ${record.routePanelShown}")
        appendLine("- loadingStillVisible: ${record.loadingStillVisible}")
        appendLine("- captureSource: ${record.captureSource}")
        appendLine("- usedFallbackSnapshot: ${record.usedFallbackSnapshot}")
        appendLine("- staleRoutesReused: ${record.staleRoutesReused}")
        appendLine("- errorCode: ${record.errorCode ?: "none"}")
        appendLine("- userMarkedWrong: ${record.userFeedback.markedWrong}")
        appendLine("- userCorrectionLastSpeaker: ${record.userFeedback.userCorrectionLastSpeaker}")
    }

    private fun recordJson(record: NextSentenceFlightRecord): String = """
        {
          "sessionId": "${escape(record.sessionId)}",
          "startedAt": ${record.startedAt},
          "endedAt": ${record.endedAt},
          "durationMs": ${record.durationMs},
          "terminalState": "${record.terminalState}",
          "appPackage": "${escape(record.appPackage)}",
          "windowTitlePreAnalysisRedacted": "${escape(record.windowTitlePreAnalysisRedacted)}",
          "targetAppSupported": ${record.targetAppSupported},
          "adapterName": "${record.adapterName}",
          "parserName": "${record.parserName}",
          "preAnalysisSnapshotCaptured": ${record.preAnalysisSnapshotCaptured},
          "postPanelSnapshotCaptured": ${record.postPanelSnapshotCaptured},
          "postPanelContaminationDetected": ${record.postPanelContaminationDetected},
          "actualLastSpeaker": "${record.actualLastSpeaker}",
          "decisionType": "${record.decisionType}",
          "decisionTypeFamily": "${record.decisionTypeFamily}",
          "routeCount": ${record.routeCount},
          "waitPanelShown": ${record.waitPanelShown},
          "routePanelShown": ${record.routePanelShown},
          "contextRequiredPanelShown": ${record.contextRequiredPanelShown},
          "loadingStillVisible": ${record.loadingStillVisible},
          "apiCalled": ${record.apiCalled},
          "modelCalled": ${record.modelCalled},
          "captureSource": "${record.captureSource}",
          "usedFallbackSnapshot": ${record.usedFallbackSnapshot},
          "fallbackSnapshotAgeMs": ${record.fallbackSnapshotAgeMs ?: "null"},
          "staleSnapshotSuspected": ${record.staleSnapshotSuspected},
          "previousSessionId": "${escape(record.previousSessionId)}",
          "panelSessionId": "${escape(record.panelSessionId)}",
          "panelContentFromCurrentSession": ${record.panelContentFromCurrentSession},
          "staleRoutesReused": ${record.staleRoutesReused},
          "permissionMissingMessageShown": ${record.permissionMissingMessageShown},
          "mainActivityOpened": ${record.mainActivityOpened},
          "overlayShownInTargetApp": ${record.overlayShownInTargetApp},
          "errorCode": ${record.errorCode?.let { "\"${escape(it)}\"" } ?: "null"},
          "failedStage": ${record.failedStage?.let { "\"${escape(it)}\"" } ?: "null"},
          "exceptionClass": ${record.exceptionClass?.let { "\"${escape(it)}\"" } ?: "null"},
          "exceptionMessageRedacted": ${record.exceptionMessageRedacted?.let { "\"${escape(it)}\"" } ?: "null"},
          "userFeedback": {
            "markedWrong": ${record.userFeedback.markedWrong},
            "userCorrectionLastSpeaker": "${record.userFeedback.userCorrectionLastSpeaker}",
            "userNote": "${escape(record.userFeedback.userNote)}"
          }
        }
    """.trimIndent()

    private fun quickStatus(record: NextSentenceFlightRecord): String = when {
        record.userFeedback.markedWrong -> "USER_MARKED_WRONG"
        record.terminalState == "UNSUPPORTED_APP" -> "UNSUPPORTED_APP"
        record.terminalState == "TIMEOUT" -> "TIMEOUT"
        record.terminalState == "CONTROLLED_FAIL" -> "CONTROLLED_FAIL"
        record.terminalState in setOf("ROUTE_PANEL", "WAIT_PANEL", "CONTEXT_REQUIRED_PANEL") -> "PASS"
        else -> "UNKNOWN"
    }

    private fun quickConclusion(record: NextSentenceFlightRecord): String = when {
        record.userFeedback.markedWrong && record.userFeedback.userCorrectionLastSpeaker != "NONE" ->
            "User marked this result wrong; correction last speaker is ${record.userFeedback.userCorrectionLastSpeaker}, system saw ${record.actualLastSpeaker}."
        record.terminalState == "WAIT_PANEL" -> "last ME entered WAIT panel with zero routes."
        record.terminalState == "ROUTE_PANEL" -> "reply routes were shown, routeCount=${record.routeCount}."
        record.terminalState == "TIMEOUT" -> "session timed out without a terminal state."
        record.terminalState == "UNSUPPORTED_APP" -> "current chat app is not adapted yet."
        else -> "session exported for review."
    }

    private fun sessionIndex(records: List<NextSentenceFlightRecord>, latest: NextSentenceFlightRecord): String = buildString {
        appendLine("# Recent Sessions")
        (records + latest).takeLast(10).forEach {
            appendLine("- ${it.sessionId}: ${it.terminalState} ${it.appPackage} ${it.actualLastSpeaker}")
        }
    }

    private fun placeholderRecord(now: Long): NextSentenceFlightRecord = NextSentenceFlightRecord(
        sessionId = "no-session",
        startedAt = now,
        endedAt = now,
        durationMs = 0,
        terminalState = "CONTROLLED_FAIL",
        appPackage = "unknown",
        windowTitlePreAnalysisRedacted = "unknown",
        targetAppSupported = false,
        adapterName = "NONE",
        parserName = "NONE",
        preAnalysisSnapshotCaptured = false,
        postPanelSnapshotCaptured = false,
        postPanelContaminationDetected = false,
        actualLastSpeaker = "UNKNOWN",
        decisionType = "ERROR",
        decisionTypeFamily = "ERROR",
        routeCount = 0,
        waitPanelShown = false,
        routePanelShown = false,
        contextRequiredPanelShown = false,
        loadingStillVisible = false,
        apiCalled = false,
        modelCalled = false,
        captureSource = "NONE",
        usedFallbackSnapshot = false,
        fallbackSnapshotAgeMs = null,
        staleSnapshotSuspected = false,
        previousSessionId = "",
        panelSessionId = "",
        panelContentFromCurrentSession = false,
        staleRoutesReused = false,
        permissionMissingMessageShown = false,
        mainActivityOpened = false,
        overlayShownInTargetApp = false,
        errorCode = "NO_SESSION",
        failedStage = "NONE",
        exceptionClass = null,
        exceptionMessageRedacted = null
    )

    private fun visualIndex(result: CurrentScreenPipelineResult?): String = buildString {
        appendLine("# Visual Debug Index")
        appendLine()
        appendLine("- current_screen_overlay.png: ${if (result?.visualDebugResult?.overlayImagePath != null) "exists_on_device_private_path" else "NOT_AVAILABLE"}")
        appendLine("- containsRawScreenshot: false")
    }

    private fun unsupportedJson(record: NextSentenceFlightRecord): String = """{"appPackage":"${escape(record.appPackage)}","unsupportedReason":"NO_APP_PROFILE"}"""

    private fun unsupportedMarkdown(record: NextSentenceFlightRecord): String = "# Unsupported App Adaptation\n\n- appPackage: ${record.appPackage}\n- unsupportedReason: NO_APP_PROFILE\n"

    private fun notAvailable(name: String): String = "# NOT_AVAILABLE\n\n- reportName: $name\n"

    private fun privacyJson(): String = """{"containsRawPrivateChat":false,"containsRawScreenshot":false,"containsApiKey":false,"containsToken":false,"containsKeystore":false,"safeForPublicGitHub":true}"""

    private fun appBuildInfo(now: Long): String = """{"versionName":"${BuildConfig.VERSION_NAME}","versionCode":${BuildConfig.VERSION_CODE},"generatedAt":$now}"""

    private fun fixedFileList(): String = """
        ${OneTapFeedbackZipContract.requiredPaths.joinToString("\n")}
        recent-sessions/session-*.json
    """.trimIndent()

    private fun addText(zip: ZipOutputStream, path: String, text: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(text.safeForPublic().toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildShareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun String.safeForPublic(): String = redactPrivateText(100_000)
        .replace(Regex("(?m)^(\\s*-?\\s*(text|message|lastEffectiveMessageText|currentSceneSummary|coreInsight|situation|bestMove)\\s*[:=]\\s*).+$"), "$1[REDACTED_PRIVATE_CHAT]")
        .replace(Regex("\"(text|message|lastEffectiveMessageText|currentSceneSummary|coreInsight|situation|bestMove)\"\\s*:\\s*\"(?:\\\\.|[^\"])*\""), "\"$1\":\"[REDACTED_PRIVATE_CHAT]\"")

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")

    private fun timestamp(timeMillis: Long): String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(timeMillis))
}
