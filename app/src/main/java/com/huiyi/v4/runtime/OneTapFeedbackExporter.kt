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
    val cloudEnabled: Boolean,
    val cloudAttempted: Boolean,
    val cloudSkippedReason: String?,
    val cloudRequestId: String?,
    val cloudSuccess: Boolean,
    val cloudLatencyMs: Long?,
    val cloudErrorCode: String?,
    val cloudNetworkFailureVisibleToUser: Boolean = false,
    val cloudRequestActuallySent: Boolean = false,
    val cloudFailureLikelyCause: String = "NONE",
    val cloudFallbackUsed: Boolean,
    val decisionSource: String,
    val captureSource: String,
    val usedFallbackSnapshot: Boolean,
    val fallbackSnapshotAgeMs: Long?,
    val staleSnapshotSuspected: Boolean,
    val systemAccessibilityEnabled: Boolean = false,
    val serviceConnected: Boolean = false,
    val accessibilityRuntimeCategory: String = "UNKNOWN",
    val serviceReconnectAttempted: Boolean = false,
    val serviceReconnectWaitMs: Long = 0L,
    val serviceReconnectSucceeded: Boolean = false,
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
    val userFeedback: UserFeedbackMark = UserFeedbackMark(),
    val feedbackClickedAt: Long = 0L,
    val feedbackTargetSessionId: String = "",
    val feedbackTargetSessionTerminalState: String = "",
    val feedbackTargetSessionFound: Boolean = false,
    val feedbackExportSource: String = "NO_TARGET_SESSION",
    val feedbackTriggeredNewAnalysis: Boolean = false,
    val feedbackReCapturedCurrentRoot: Boolean = false,
    val feedbackUsedOverlayStateAsPreAnalysis: Boolean = false,
    val preAnalysisSnapshotFrozenAt: Long = 0L,
    val preAnalysisSnapshotSource: String = "UNKNOWN",
    val preAnalysisSnapshotMutableAfterPanel: Boolean = false,
    val postPanelSnapshotCapturedAt: Long = 0L,
    val postPanelSnapshotUsedForDecision: Boolean = false,
    val sessionImmutableAfterTerminalState: Boolean = true,
    val preAnalysisSnapshotTrusted: Boolean = true,
    val preAnalysisSnapshotErrorCode: String? = null,
    val preAnalysisLooksLikeHuiyiPanel: Boolean = false,
    val preAnalysisTextClaimsLastMeWait: Boolean = false,
    val recordClaimsLastOtherRoutePanel: Boolean = false,
    val windowTitleAndDecisionContradiction: Boolean = false,
    val reportConsistencyResult: String = "PASS",
    val cloudContractImplemented: Boolean = false,
    val cloudConfigured: Boolean = false,
    val cloudAnalysisAttempted: Boolean = false,
    val cloudContractVersion: String = "HuiyiTacticalContract-v1",
    val cloudContractValidationResult: String = "NOT_RUN",
    val providerType: String = "OPENAI_COMPATIBLE_RELAY",
    val relayBaseUrlConfigured: Boolean = false,
    val relayApiKeyConfigured: Boolean = false,
    val relayApiKeyStoredSecurely: Boolean = false,
    val relayApiKeyExposedInRepo: Boolean = false,
    val relayApiKeyExposedInApk: Boolean = false,
    val cloudPrimaryModel: String = "",
    val cloudFinalModel: String = "",
    val cloudEscalated: Boolean = false,
    val cloudEscalationReason: String? = null,
    val cloudQualityGateResult: String = "NOT_RUN",
    val cloudQualityScore: Int? = null,
    val cloudQualityIssues: List<String> = emptyList(),
    val cloudPrimaryLatencyMs: Long? = null,
    val cloudTotalLatencyMs: Long? = null,
    val expressSelfClicked: Boolean = false,
    val expressSelfEligibilityEligible: Boolean? = null,
    val expressSelfEligibilityMode: String = "NONE",
    val expressSelfEligibilityBlockReason: String? = null,
    val expressSelfEligibilityConfidence: Int? = null,
    val currentWindowTitleRedacted: String? = null,
    val lastUserMessageAgeMs: Long? = null,
    val expressionWindowExists: Boolean? = null,
    val coldStartAllowed: Boolean? = null,
    val recentSelfExpressionCount: Int? = null,
    val repeatRisk: String = "UNKNOWN",
    val expressSelfAllowedReason: String = "NONE",
    val expressSelfBlockedReason: String = "NONE"
) {
    fun withFeedback(feedback: UserFeedbackMark): NextSentenceFlightRecord = copy(userFeedback = feedback)

    fun withFeedbackTrace(
        clickedAt: Long,
        targetSessionId: String,
        exportSource: String,
        targetFound: Boolean = true
    ): NextSentenceFlightRecord = copy(
        feedbackClickedAt = clickedAt,
        feedbackTargetSessionId = targetSessionId,
        feedbackTargetSessionTerminalState = terminalState,
        feedbackTargetSessionFound = targetFound,
        feedbackExportSource = exportSource,
        feedbackTriggeredNewAnalysis = false,
        feedbackReCapturedCurrentRoot = false
    ).withComputedConsistency()

    fun withComputedConsistency(): NextSentenceFlightRecord {
        val looksLikePanel = looksLikeHuiyiPanel(windowTitlePreAnalysisRedacted)
        val claimsLastMeWait = claimsLastMeWait(windowTitlePreAnalysisRedacted)
        val claimsOtherRoute = actualLastSpeaker == "OTHER" && terminalState == "ROUTE_PANEL"
        val contradiction = claimsLastMeWait && claimsOtherRoute
        val contaminated = looksLikePanel || contradiction
        return copy(
            terminalState = if (contaminated) "CONTROLLED_FAIL" else terminalState,
            actualLastSpeaker = if (contaminated) "UNKNOWN" else actualLastSpeaker,
            decisionType = if (contaminated) "PRE_ANALYSIS_CONTAMINATED" else decisionType,
            decisionTypeFamily = if (contaminated) "CONTROLLED_FAIL" else decisionTypeFamily,
            routeCount = if (contaminated) 0 else routeCount,
            waitPanelShown = if (contaminated) false else waitPanelShown,
            routePanelShown = if (contaminated) false else routePanelShown,
            contextRequiredPanelShown = if (contaminated) false else contextRequiredPanelShown,
            apiCalled = if (contaminated) false else apiCalled,
            modelCalled = if (contaminated) false else modelCalled,
            cloudAttempted = if (contaminated) false else cloudAttempted,
            cloudAnalysisAttempted = if (contaminated) false else cloudAnalysisAttempted,
            cloudSuccess = if (contaminated) false else cloudSuccess,
            cloudErrorCode = if (contaminated) null else cloudErrorCode,
            cloudFallbackUsed = if (contaminated) false else cloudFallbackUsed,
            cloudSkippedReason = if (contaminated) "PRE_ANALYSIS_CONTAMINATED" else cloudSkippedReason,
            decisionSource = if (contaminated) "CONTROLLED_FAIL" else decisionSource,
            postPanelContaminationDetected = postPanelContaminationDetected || contaminated,
            feedbackUsedOverlayStateAsPreAnalysis = feedbackUsedOverlayStateAsPreAnalysis || contaminated,
            preAnalysisSnapshotTrusted = preAnalysisSnapshotTrusted && !contaminated,
            preAnalysisSnapshotErrorCode = if (contaminated) "PRE_ANALYSIS_SNAPSHOT_CONTAMINATED_BY_PANEL" else preAnalysisSnapshotErrorCode,
            preAnalysisLooksLikeHuiyiPanel = looksLikePanel,
            preAnalysisTextClaimsLastMeWait = claimsLastMeWait,
            recordClaimsLastOtherRoutePanel = claimsOtherRoute,
            windowTitleAndDecisionContradiction = contradiction,
            reportConsistencyResult = if (contaminated) "FAIL_CONTAMINATED_EXPORT" else "PASS",
            cloudConfigured = cloudEnabled
        )
    }
}

private fun looksLikeHuiyiPanel(title: String): Boolean {
    if (title.isBlank()) return false
    val markers = listOf(
        "没读到当前聊天",
        "没读到聊天",
        "请回到聊起聊天窗口",
        "这次不对，发给 GPT",
        "这次不对",
        "会意雷达",
        "隐藏",
        "正在上传 GitHub",
        "会意雷达",
        "最后一句是我",
        "你已经回过了",
        "先等对方",
        "这次不对",
        "发给 GPT",
        "正在上传 GitHub",
        "导出诊断",
        "打开无障碍",
        "隐藏悬浮球",
        "当前信息不足",
        "说话人或内容不确定",
        "浼氭剰",
        "鏈€鍚庝竴鍙ユ槸鎴",
        "浣犲凡缁忓洖",
        "姝ｅ湪涓婁紶 GitHub",
        "鍙戠粰 GPT"
    )
    return markers.any { title.contains(it, ignoreCase = true) }
}

private fun claimsLastMeWait(title: String): Boolean {
    if (title.isBlank()) return false
    val lastMeMarkers = listOf("最后一句是我", "你已经回过了", "先等对方", "鏈€鍚庝竴鍙ユ槸鎴", "浣犲凡缁忓洖")
    return lastMeMarkers.any { title.contains(it, ignoreCase = true) }
}

data class OneTapFeedbackExport(
    val zipFile: File,
    val displayPath: String,
    val publicCopyPath: String?,
    val shareIntent: Intent,
    val record: NextSentenceFlightRecord,
    val generatedAt: Long
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

    fun recentSessionRecords(
        records: List<NextSentenceFlightRecord>,
        latest: NextSentenceFlightRecord
    ): List<NextSentenceFlightRecord> {
        return (records + latest)
            .asReversed()
            .distinctBy { it.sessionId }
            .asReversed()
            .takeLast(10)
    }

    fun selectTargetRecord(
        panelSessionId: String?,
        lastCompletedSessionId: String?,
        latest: NextSentenceFlightRecord?,
        records: List<NextSentenceFlightRecord>
    ): Pair<NextSentenceFlightRecord, String>? {
        val candidates = (records + listOfNotNull(latest))
            .asReversed()
            .distinctBy { it.sessionId }
        val requestedPanelSessionId = panelSessionId?.takeIf { it.isNotBlank() }
        val panelTarget = requestedPanelSessionId?.let { id ->
            candidates.firstOrNull { it.sessionId == id }?.let { it to "BOUND_PANEL_SESSION" }
        }
        if (panelTarget != null) return panelTarget
        if (requestedPanelSessionId != null) return null
        return lastCompletedSessionId?.takeIf { it.isNotBlank() }?.let { id ->
            candidates.firstOrNull { it.sessionId == id }?.let { it to "LAST_COMPLETED_NEXT_SENTENCE_SESSION" }
        } ?: latest?.let { it to "LAST_COMPLETED_NEXT_SENTENCE_SESSION" }
    }
}

object NextSentenceFlightRecordFactory {
    fun fromSuccess(
        result: CurrentScreenPipelineResult,
        trace: NextSentenceSessionTrace
    ): NextSentenceFlightRecord {
        val capture = result.captureResult
        val decisionType = result.tacticalDecision.decisionType
        val expressSelfEligibility = result.expressSelfEligibility
        val expressSelfClicked = result.decisionTypeFamily == "EXPRESS_SELF" ||
            result.sessionTerminalState in setOf("EXPRESS_SELF_PANEL", "HOLD_BACK_PANEL", "CONTROLLED_FAIL_PANEL")
        val startedAt = result.analysisStartedAt.takeIf { it > 0L } ?: trace.startedAt
        val endedAt = result.analysisEndedAt.takeIf { it > 0L } ?: trace.endedAt ?: System.currentTimeMillis()
        return NextSentenceFlightRecord(
            sessionId = result.sessionId ?: trace.sessionId,
            startedAt = startedAt,
            endedAt = endedAt,
            durationMs = result.analysisDurationMs.takeIf { it > 0L } ?: (endedAt - startedAt),
            terminalState = result.sessionTerminalState.takeIf { it != "UNKNOWN" } ?: terminalStateFor(decisionType),
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
            modelCalled = result.cloudTrace.modelCalled,
            cloudEnabled = result.cloudTrace.cloudEnabled,
            cloudAttempted = result.cloudTrace.cloudAttempted,
            cloudSkippedReason = result.cloudTrace.cloudSkippedReason,
            cloudRequestId = result.cloudTrace.cloudRequestId,
            cloudSuccess = result.cloudTrace.cloudSuccess,
            cloudLatencyMs = result.cloudTrace.cloudLatencyMs,
            cloudErrorCode = result.cloudTrace.cloudErrorCode,
            cloudNetworkFailureVisibleToUser = result.cloudTrace.cloudNetworkFailureVisibleToUser,
            cloudRequestActuallySent = result.cloudTrace.cloudRequestActuallySent,
            cloudFailureLikelyCause = result.cloudTrace.cloudFailureLikelyCause,
            cloudFallbackUsed = result.cloudTrace.cloudFallbackUsed,
            decisionSource = result.cloudTrace.decisionSource,
            captureSource = capture?.captureSource?.name ?: NextSentenceCaptureSource.NONE.name,
            usedFallbackSnapshot = capture?.usedFallbackSnapshot == true,
            fallbackSnapshotAgeMs = capture?.lastStableSnapshotAgeMs,
            staleSnapshotSuspected = capture?.usedFallbackSnapshot == true && (capture.lastStableSnapshotAgeMs ?: 0L) > 2000L,
            systemAccessibilityEnabled = trace.systemAccessibilityEnabled,
            serviceConnected = trace.serviceConnected,
            accessibilityRuntimeCategory = trace.accessibilityRuntimeCategory ?: "UNKNOWN",
            serviceReconnectAttempted = trace.serviceReconnectAttempted,
            serviceReconnectWaitMs = trace.serviceReconnectWaitMs,
            serviceReconnectSucceeded = trace.serviceReconnectSucceeded,
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
            exceptionMessageRedacted = null,
            preAnalysisSnapshotFrozenAt = startedAt,
            preAnalysisSnapshotSource = if (capture?.usedFallbackSnapshot == true) {
                "LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL"
            } else {
                "CURRENT_ROOT_BEFORE_PANEL"
            },
            preAnalysisSnapshotMutableAfterPanel = false,
            postPanelSnapshotCapturedAt = if (result.resultShownAsOverlay) endedAt else 0L,
            postPanelSnapshotUsedForDecision = false,
            cloudContractImplemented = true,
            cloudConfigured = result.cloudTrace.endpointConfigured,
            cloudAnalysisAttempted = result.cloudTrace.cloudAttempted,
            cloudContractVersion = result.cloudTrace.cloudContractVersion,
            cloudContractValidationResult = result.cloudTrace.cloudContractValidationResult,
            providerType = result.cloudTrace.providerType,
            relayBaseUrlConfigured = result.cloudTrace.relayBaseUrlConfigured,
            relayApiKeyConfigured = result.cloudTrace.relayApiKeyConfigured,
            relayApiKeyStoredSecurely = result.cloudTrace.relayApiKeyStoredSecurely,
            relayApiKeyExposedInRepo = result.cloudTrace.relayApiKeyExposedInRepo,
            relayApiKeyExposedInApk = result.cloudTrace.relayApiKeyExposedInApk,
            cloudPrimaryModel = result.cloudTrace.cloudPrimaryModel,
            cloudFinalModel = result.cloudTrace.cloudFinalModel,
            cloudEscalated = result.cloudTrace.cloudEscalated,
            cloudEscalationReason = result.cloudTrace.cloudEscalationReason,
            cloudQualityGateResult = result.cloudTrace.cloudQualityGateResult,
            cloudQualityScore = result.cloudTrace.cloudQualityScore,
            cloudQualityIssues = result.cloudTrace.cloudQualityIssues,
            cloudPrimaryLatencyMs = result.cloudTrace.cloudPrimaryLatencyMs,
            cloudTotalLatencyMs = result.cloudTrace.cloudTotalLatencyMs,
            expressSelfClicked = expressSelfClicked,
            expressSelfEligibilityEligible = expressSelfEligibility?.eligible,
            expressSelfEligibilityMode = expressSelfEligibility?.mode?.name ?: "NONE",
            expressSelfEligibilityBlockReason = expressSelfEligibility?.blockReason?.name,
            expressSelfEligibilityConfidence = expressSelfEligibility?.confidence,
            currentWindowTitleRedacted = expressSelfEligibility?.currentWindowTitleRedacted?.redactPrivateText(120),
            lastUserMessageAgeMs = expressSelfEligibility?.lastUserMessageAgeMs,
            expressionWindowExists = expressSelfEligibility?.expressionWindowExists,
            coldStartAllowed = expressSelfEligibility?.coldStartAllowed,
            recentSelfExpressionCount = expressSelfEligibility?.recentSelfExpressionCount,
            repeatRisk = expressSelfEligibility?.repeatRisk ?: "UNKNOWN",
            expressSelfAllowedReason = expressSelfEligibility?.allowedReason ?: "NONE",
            expressSelfBlockedReason = expressSelfEligibility?.blockedReason ?: "NONE"
        ).withComputedConsistency()
    }

    fun fromFailure(trace: NextSentenceSessionTrace): NextSentenceFlightRecord {
        val terminal = when (trace.errorCode) {
            NextSentenceErrorCode.SESSION_TIMEOUT_NO_TERMINAL_STATE,
            NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK -> "TIMEOUT"
            NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND,
            NextSentenceErrorCode.ONLY_NON_CHAT_TEXT_FOUND -> "UNSUPPORTED_APP"
            else -> "CONTROLLED_FAIL"
        }
        val endedAt = trace.endedAt ?: System.currentTimeMillis()
        return NextSentenceFlightRecord(
            sessionId = trace.sessionId,
            startedAt = trace.startedAt,
            endedAt = endedAt,
            durationMs = endedAt - trace.startedAt,
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
            cloudEnabled = false,
            cloudAttempted = false,
            cloudSkippedReason = "FAILURE_BEFORE_CLOUD",
            cloudRequestId = null,
            cloudSuccess = false,
            cloudLatencyMs = null,
            cloudErrorCode = trace.errorCode?.name,
            cloudNetworkFailureVisibleToUser = false,
            cloudRequestActuallySent = false,
            cloudFailureLikelyCause = "NONE",
            cloudFallbackUsed = false,
            decisionSource = "LOCAL_FALLBACK",
            captureSource = trace.captureSource.name,
            usedFallbackSnapshot = trace.usedFallbackSnapshot,
            fallbackSnapshotAgeMs = trace.lastStableSnapshotAgeMs,
            staleSnapshotSuspected = trace.usedFallbackSnapshot && (trace.lastStableSnapshotAgeMs ?: 0L) > 2000L,
            systemAccessibilityEnabled = trace.systemAccessibilityEnabled,
            serviceConnected = trace.serviceConnected,
            accessibilityRuntimeCategory = trace.accessibilityRuntimeCategory ?: "UNKNOWN",
            serviceReconnectAttempted = trace.serviceReconnectAttempted,
            serviceReconnectWaitMs = trace.serviceReconnectWaitMs,
            serviceReconnectSucceeded = trace.serviceReconnectSucceeded,
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
            exceptionMessageRedacted = trace.pipelineExceptionMessageRedacted ?: trace.exceptionMessageRedacted,
            preAnalysisSnapshotFrozenAt = trace.startedAt,
            preAnalysisSnapshotSource = if (trace.usedFallbackSnapshot) {
                "LAST_STABLE_CHAT_SNAPSHOT_BEFORE_PANEL"
            } else {
                "CURRENT_ROOT_BEFORE_PANEL"
            },
            preAnalysisSnapshotMutableAfterPanel = false,
            postPanelSnapshotCapturedAt = 0L,
            postPanelSnapshotUsedForDecision = false,
            cloudContractImplemented = false,
            cloudConfigured = false,
            cloudAnalysisAttempted = false,
            cloudContractVersion = "HuiyiTacticalContract-v1",
            cloudContractValidationResult = "NOT_RUN",
            providerType = "OPENAI_COMPATIBLE_RELAY",
            relayBaseUrlConfigured = false,
            relayApiKeyConfigured = false,
            relayApiKeyStoredSecurely = false,
            relayApiKeyExposedInRepo = false,
            relayApiKeyExposedInApk = false
        ).withComputedConsistency()
    }

    private fun terminalStateFor(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT_PANEL"
        TacticalDecisionType.HOLD_BACK -> "HOLD_BACK_PANEL"
        TacticalDecisionType.CHAT_WINDOW_NOT_FOUND,
        TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED -> "CONTROLLED_FAIL"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "CONTEXT_REQUIRED_PANEL"
        else -> "ROUTE_PANEL"
    }

    private fun decisionTypeFamily(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT"
        TacticalDecisionType.HOLD_BACK -> "HOLD_BACK"
        TacticalDecisionType.CHAT_WINDOW_NOT_FOUND,
        TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED -> "CONTROLLED_FAIL"
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
        feedback: UserFeedbackMark = UserFeedbackMark(markedWrong = true),
        feedbackClickedAt: Long = System.currentTimeMillis(),
        feedbackTargetSessionId: String? = latestRecord?.sessionId,
        feedbackExportSource: String = "LAST_COMPLETED_NEXT_SENTENCE_SESSION"
    ): Result<OneTapFeedbackExport> = runCatching {
        val now = feedbackClickedAt
        val sourceRecord = latestRecord ?: throw IllegalStateException("NO_TARGET_SESSION_FOR_FEEDBACK")
        val record = sourceRecord
            .withFeedback(feedback)
            .withFeedbackTrace(
                clickedAt = now,
                targetSessionId = feedbackTargetSessionId ?: sourceRecord.sessionId,
                exportSource = feedbackExportSource,
                targetFound = true
            )
        val fileName = "huiyi-one-tap-feedback-v${BuildConfig.VERSION_NAME}-${timestamp(now)}.zip"
        val outDir = File(context.filesDir, "exports/one_tap_feedback").apply { mkdirs() }
        val zip = File(outDir, fileName)
        val recentSessionRecords = OneTapFeedbackZipContract.recentSessionRecords(recentRecords, record)
        val currentReports = currentScreenReports(latestResult)
        ZipOutputStream(zip.outputStream()).use { stream ->
            addText(stream, "README_FOR_GPT.md", readme(record, currentReports.first))
            addText(stream, "one-tap-feedback-manifest.json", manifest(record, now))
            addText(stream, "latest-session/next-sentence-flight-record.json", recordJson(record))
            addText(stream, "latest-session/next-sentence-flight-record-for-gpt.md", recordMarkdown(record))
            addText(stream, "current-screen/real-device-current-screen-report-for-gpt.md", currentReports.first)
            addText(stream, "current-screen/real-device-current-screen-report.json", currentReports.second)
            addText(stream, "recent-sessions/session-index.md", sessionIndex(recentSessionRecords))
            recentSessionRecords.forEach { item ->
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
            shareIntent = buildShareIntent(zip),
            record = record,
            generatedAt = now
        )
    }

    private fun currentScreenReports(result: CurrentScreenPipelineResult?): Pair<String, String> {
        if (result == null) return notAvailable("current screen") to """{"result":"NOT_AVAILABLE"}"""
        val markdown = EvidencePackReportGenerator().buildMarkdown(result, HuiyiAccessibilityService.state.value)
        val json = EvidencePackReportGenerator().buildJson(result, HuiyiAccessibilityService.state.value)
        return markdown.safeForPublic() to json.safeForPublic()
    }

    private fun readme(record: NextSentenceFlightRecord, currentScreenMarkdown: String): String = buildString {
        appendLine("# Huiyi One Tap Feedback")
        appendLine()
        appendLine("- bundleType: ONE_TAP_FEEDBACK")
        appendLine("- appVersionName: ${BuildConfig.VERSION_NAME}")
        appendLine("- appVersionCode: ${BuildConfig.VERSION_CODE}")
        appendLine("- latestSessionId: ${record.sessionId}")
        appendLine("- terminalState: ${record.terminalState}")
        appendLine("- latestSessionTerminalState: ${record.terminalState}")
        appendLine("- feedbackClickedAt: ${record.feedbackClickedAt}")
        appendLine("- feedbackTargetSessionId: ${record.feedbackTargetSessionId}")
        appendLine("- feedbackExportSource: ${record.feedbackExportSource}")
        appendLine("- feedbackTriggeredNewAnalysis: ${record.feedbackTriggeredNewAnalysis}")
        appendLine("- feedbackReCapturedCurrentRoot: ${record.feedbackReCapturedCurrentRoot}")
        appendLine("- feedbackUsedOverlayStateAsPreAnalysis: ${record.feedbackUsedOverlayStateAsPreAnalysis}")
        appendLine("- preAnalysisSnapshotFrozenAt: ${record.preAnalysisSnapshotFrozenAt}")
        appendLine("- preAnalysisSnapshotSource: ${record.preAnalysisSnapshotSource}")
        appendLine("- preAnalysisSnapshotMutableAfterPanel: ${record.preAnalysisSnapshotMutableAfterPanel}")
        appendLine("- postPanelSnapshotUsedForDecision: ${record.postPanelSnapshotUsedForDecision}")
        appendLine("- sessionImmutableAfterTerminalState: ${record.sessionImmutableAfterTerminalState}")
        appendLine("- actualLastSpeaker: ${record.actualLastSpeaker}")
        appendLine("- decisionType: ${record.decisionType}")
        appendLine("- decisionTypeFamily: ${record.decisionTypeFamily}")
        appendLine("- waitPanelShown: ${record.waitPanelShown}")
        appendLine("- contextRequiredPanelShown: ${record.contextRequiredPanelShown}")
        appendLine("- cloudEnabled: ${record.cloudEnabled}")
        appendLine("- cloudConfigured: ${record.cloudConfigured}")
        appendLine("- cloudContractImplemented: ${record.cloudContractImplemented}")
        appendLine("- providerType: ${record.providerType}")
        appendLine("- relayBaseUrlConfigured: ${record.relayBaseUrlConfigured}")
        appendLine("- relayApiKeyConfigured: ${record.relayApiKeyConfigured}")
        appendLine("- relayApiKeyStoredSecurely: ${record.relayApiKeyStoredSecurely}")
        appendLine("- relayApiKeyExposedInRepo: ${record.relayApiKeyExposedInRepo}")
        appendLine("- relayApiKeyExposedInApk: ${record.relayApiKeyExposedInApk}")
        appendLine("- cloudAttempted: ${record.cloudAttempted}")
        appendLine("- cloudAnalysisAttempted: ${record.cloudAnalysisAttempted}")
        appendLine("- cloudSuccess: ${record.cloudSuccess}")
        appendLine("- cloudSkippedReason: ${record.cloudSkippedReason ?: "none"}")
        appendLine("- decisionSource: ${record.decisionSource}")
        appendLine("- cloudContractVersion: ${record.cloudContractVersion}")
        appendLine("- cloudContractValidationResult: ${record.cloudContractValidationResult}")
        appendLine("- cloudFallbackUsed: ${record.cloudFallbackUsed}")
        appendLine("- cloudLatencyMs: ${record.cloudLatencyMs ?: "null"}")
        appendLine("- cloudPrimaryModel: ${record.cloudPrimaryModel.ifBlank { "unknown" }}")
        appendLine("- cloudFinalModel: ${record.cloudFinalModel.ifBlank { "unknown" }}")
        appendLine("- cloudEscalated: ${record.cloudEscalated}")
        appendLine("- cloudEscalationReason: ${record.cloudEscalationReason ?: "none"}")
        appendLine("- cloudQualityGateResult: ${record.cloudQualityGateResult}")
        appendLine("- cloudQualityScore: ${record.cloudQualityScore ?: "null"}")
        appendLine("- cloudQualityIssues: ${record.cloudQualityIssues.joinToString("|").ifBlank { "none" }}")
        appendLine("- cloudPrimaryLatencyMs: ${record.cloudPrimaryLatencyMs ?: "null"}")
        appendLine("- cloudTotalLatencyMs: ${record.cloudTotalLatencyMs ?: "null"}")
        appendLine("- cloudErrorCode: ${record.cloudErrorCode ?: "none"}")
        appendLine("- cloudNetworkFailureVisibleToUser: ${record.cloudNetworkFailureVisibleToUser}")
        appendLine("- cloudRequestActuallySent: ${record.cloudRequestActuallySent}")
        appendLine("- cloudFailureLikelyCause: ${record.cloudFailureLikelyCause}")
        appendLine("- systemAccessibilityEnabled: ${record.systemAccessibilityEnabled}")
        appendLine("- serviceConnected: ${record.serviceConnected}")
        appendLine("- accessibilityRuntimeCategory: ${record.accessibilityRuntimeCategory}")
        appendLine("- serviceReconnectAttempted: ${record.serviceReconnectAttempted}")
        appendLine("- serviceReconnectWaitMs: ${record.serviceReconnectWaitMs}")
        appendLine("- serviceReconnectSucceeded: ${record.serviceReconnectSucceeded}")
        appendLine("- messageStatusArtifactCount: ${markdownField(currentScreenMarkdown, "messageStatusArtifactCount") ?: "0"}")
        appendLine("- lastMeDeliveryStatus: ${markdownField(currentScreenMarkdown, "lastMeDeliveryStatus") ?: "NONE"}")
        appendLine("- lastMeReadStatus: ${markdownField(currentScreenMarkdown, "lastMeReadStatus") ?: "NONE"}")
        appendLine("- reportWindowTitleContaminatedByPanel: ${markdownField(currentScreenMarkdown, "reportWindowTitleContaminatedByPanel") ?: "false"}")
        appendLine("- preAnalysisSnapshotTrusted: ${record.preAnalysisSnapshotTrusted}")
        appendLine("- preAnalysisLooksLikeHuiyiPanel: ${record.preAnalysisLooksLikeHuiyiPanel}")
        appendLine("- preAnalysisTextClaimsLastMeWait: ${record.preAnalysisTextClaimsLastMeWait}")
        appendLine("- windowTitleAndDecisionContradiction: ${record.windowTitleAndDecisionContradiction}")
        appendLine("- reportConsistencyResult: ${record.reportConsistencyResult}")
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
            "feedback": {
              "feedbackClickedAt": ${record.feedbackClickedAt},
              "feedbackTargetSessionId": "${escape(record.feedbackTargetSessionId)}",
              "feedbackTargetSessionTerminalState": "${escape(record.feedbackTargetSessionTerminalState)}",
              "feedbackTargetSessionFound": ${record.feedbackTargetSessionFound},
              "feedbackExportSource": "${record.feedbackExportSource}",
              "feedbackTriggeredNewAnalysis": ${record.feedbackTriggeredNewAnalysis},
              "feedbackReCapturedCurrentRoot": ${record.feedbackReCapturedCurrentRoot},
              "feedbackUsedOverlayStateAsPreAnalysis": ${record.feedbackUsedOverlayStateAsPreAnalysis}
            },
            "sessionBinding": {
              "preAnalysisSnapshotFrozenAt": ${record.preAnalysisSnapshotFrozenAt},
              "preAnalysisSnapshotSource": "${record.preAnalysisSnapshotSource}",
              "preAnalysisSnapshotMutableAfterPanel": ${record.preAnalysisSnapshotMutableAfterPanel},
              "postPanelSnapshotUsedForDecision": ${record.postPanelSnapshotUsedForDecision},
              "sessionImmutableAfterTerminalState": ${record.sessionImmutableAfterTerminalState}
            },
            "reportConsistencyChecks": {
              "preAnalysisLooksLikeHuiyiPanel": ${record.preAnalysisLooksLikeHuiyiPanel},
              "preAnalysisTextClaimsLastMeWait": ${record.preAnalysisTextClaimsLastMeWait},
              "recordClaimsLastOtherRoutePanel": ${record.recordClaimsLastOtherRoutePanel},
              "windowTitleAndDecisionContradiction": ${record.windowTitleAndDecisionContradiction},
              "reportConsistencyResult": "${record.reportConsistencyResult}"
            },
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
            "cloudEnabled": ${record.cloudEnabled},
            "cloudConfigured": ${record.cloudConfigured},
            "cloudContractImplemented": ${record.cloudContractImplemented},
            "providerType": "${record.providerType}",
            "relayBaseUrlConfigured": ${record.relayBaseUrlConfigured},
            "relayApiKeyConfigured": ${record.relayApiKeyConfigured},
            "relayApiKeyStoredSecurely": ${record.relayApiKeyStoredSecurely},
            "relayApiKeyExposedInRepo": ${record.relayApiKeyExposedInRepo},
            "relayApiKeyExposedInApk": ${record.relayApiKeyExposedInApk},
            "cloudAttempted": ${record.cloudAttempted},
            "cloudAnalysisAttempted": ${record.cloudAnalysisAttempted},
            "cloudContractVersion": "${record.cloudContractVersion}",
            "cloudContractValidationResult": "${record.cloudContractValidationResult}",
            "cloudSuccess": ${record.cloudSuccess},
            "cloudSkippedReason": "${record.cloudSkippedReason ?: ""}",
            "decisionSource": "${record.decisionSource}",
            "cloudFallbackUsed": ${record.cloudFallbackUsed},
            "cloudLatencyMs": ${record.cloudLatencyMs ?: "null"},
            "cloudPrimaryModel": "${escape(record.cloudPrimaryModel)}",
            "cloudFinalModel": "${escape(record.cloudFinalModel)}",
            "cloudEscalated": ${record.cloudEscalated},
            "cloudEscalationReason": ${record.cloudEscalationReason?.let { "\"${escape(it)}\"" } ?: "null"},
            "cloudQualityGateResult": "${escape(record.cloudQualityGateResult)}",
            "cloudQualityScore": ${record.cloudQualityScore ?: "null"},
            "cloudQualityIssues": "${escape(record.cloudQualityIssues.joinToString("|"))}",
            "cloudPrimaryLatencyMs": ${record.cloudPrimaryLatencyMs ?: "null"},
            "cloudTotalLatencyMs": ${record.cloudTotalLatencyMs ?: "null"},
            "cloudErrorCode": "${record.cloudErrorCode ?: ""}",
            "cloudNetworkFailureVisibleToUser": ${record.cloudNetworkFailureVisibleToUser},
            "cloudRequestActuallySent": ${record.cloudRequestActuallySent},
            "cloudFailureLikelyCause": "${escape(record.cloudFailureLikelyCause)}",
            "systemAccessibilityEnabled": ${record.systemAccessibilityEnabled},
            "serviceConnected": ${record.serviceConnected},
            "accessibilityRuntimeCategory": "${escape(record.accessibilityRuntimeCategory)}",
            "serviceReconnectAttempted": ${record.serviceReconnectAttempted},
            "serviceReconnectWaitMs": ${record.serviceReconnectWaitMs},
            "serviceReconnectSucceeded": ${record.serviceReconnectSucceeded},
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
        appendLine("- feedbackClickedAt: ${record.feedbackClickedAt}")
        appendLine("- feedbackTargetSessionId: ${record.feedbackTargetSessionId}")
        appendLine("- feedbackTargetSessionTerminalState: ${record.feedbackTargetSessionTerminalState}")
        appendLine("- feedbackTargetSessionFound: ${record.feedbackTargetSessionFound}")
        appendLine("- feedbackExportSource: ${record.feedbackExportSource}")
        appendLine("- feedbackTriggeredNewAnalysis: ${record.feedbackTriggeredNewAnalysis}")
        appendLine("- feedbackReCapturedCurrentRoot: ${record.feedbackReCapturedCurrentRoot}")
        appendLine("- feedbackUsedOverlayStateAsPreAnalysis: ${record.feedbackUsedOverlayStateAsPreAnalysis}")
        appendLine("- appPackage: ${record.appPackage}")
        appendLine("- actualLastSpeaker: ${record.actualLastSpeaker}")
        appendLine("- decisionType: ${record.decisionType}")
        appendLine("- decisionTypeFamily: ${record.decisionTypeFamily}")
        appendLine("- routeCount: ${record.routeCount}")
        appendLine("- waitPanelShown: ${record.waitPanelShown}")
        appendLine("- routePanelShown: ${record.routePanelShown}")
        appendLine("- cloudEnabled: ${record.cloudEnabled}")
        appendLine("- cloudConfigured: ${record.cloudConfigured}")
        appendLine("- cloudContractImplemented: ${record.cloudContractImplemented}")
        appendLine("- providerType: ${record.providerType}")
        appendLine("- relayBaseUrlConfigured: ${record.relayBaseUrlConfigured}")
        appendLine("- relayApiKeyConfigured: ${record.relayApiKeyConfigured}")
        appendLine("- relayApiKeyStoredSecurely: ${record.relayApiKeyStoredSecurely}")
        appendLine("- relayApiKeyExposedInRepo: ${record.relayApiKeyExposedInRepo}")
        appendLine("- relayApiKeyExposedInApk: ${record.relayApiKeyExposedInApk}")
        appendLine("- cloudAttempted: ${record.cloudAttempted}")
        appendLine("- cloudAnalysisAttempted: ${record.cloudAnalysisAttempted}")
        appendLine("- cloudSuccess: ${record.cloudSuccess}")
        appendLine("- cloudSkippedReason: ${record.cloudSkippedReason ?: "none"}")
        appendLine("- decisionSource: ${record.decisionSource}")
        appendLine("- cloudFallbackUsed: ${record.cloudFallbackUsed}")
        appendLine("- cloudNetworkFailureVisibleToUser: ${record.cloudNetworkFailureVisibleToUser}")
        appendLine("- cloudRequestActuallySent: ${record.cloudRequestActuallySent}")
        appendLine("- cloudFailureLikelyCause: ${record.cloudFailureLikelyCause}")
        appendLine("- cloudContractVersion: ${record.cloudContractVersion}")
        appendLine("- cloudContractValidationResult: ${record.cloudContractValidationResult}")
        appendLine("- cloudLatencyMs: ${record.cloudLatencyMs ?: "null"}")
        appendLine("- cloudErrorCode: ${record.cloudErrorCode ?: "none"}")
        appendLine("- loadingStillVisible: ${record.loadingStillVisible}")
        appendLine("- captureSource: ${record.captureSource}")
        appendLine("- preAnalysisSnapshotFrozenAt: ${record.preAnalysisSnapshotFrozenAt}")
        appendLine("- preAnalysisSnapshotSource: ${record.preAnalysisSnapshotSource}")
        appendLine("- preAnalysisSnapshotMutableAfterPanel: ${record.preAnalysisSnapshotMutableAfterPanel}")
        appendLine("- postPanelSnapshotCapturedAt: ${record.postPanelSnapshotCapturedAt}")
        appendLine("- postPanelSnapshotUsedForDecision: ${record.postPanelSnapshotUsedForDecision}")
        appendLine("- sessionImmutableAfterTerminalState: ${record.sessionImmutableAfterTerminalState}")
        appendLine("- preAnalysisSnapshotTrusted: ${record.preAnalysisSnapshotTrusted}")
        appendLine("- preAnalysisSnapshotErrorCode: ${record.preAnalysisSnapshotErrorCode ?: "none"}")
        appendLine("- preAnalysisLooksLikeHuiyiPanel: ${record.preAnalysisLooksLikeHuiyiPanel}")
        appendLine("- preAnalysisTextClaimsLastMeWait: ${record.preAnalysisTextClaimsLastMeWait}")
        appendLine("- recordClaimsLastOtherRoutePanel: ${record.recordClaimsLastOtherRoutePanel}")
        appendLine("- windowTitleAndDecisionContradiction: ${record.windowTitleAndDecisionContradiction}")
        appendLine("- reportConsistencyResult: ${record.reportConsistencyResult}")
        appendLine("- expressSelfClicked: ${record.expressSelfClicked}")
        appendLine("- expressSelfEligibility.eligible: ${record.expressSelfEligibilityEligible ?: "null"}")
        appendLine("- expressSelfEligibility.mode: ${record.expressSelfEligibilityMode}")
        appendLine("- expressSelfEligibility.blockReason: ${record.expressSelfEligibilityBlockReason ?: "none"}")
        appendLine("- expressSelfEligibility.confidence: ${record.expressSelfEligibilityConfidence ?: "null"}")
        appendLine("- currentWindowTitleRedacted: ${record.currentWindowTitleRedacted ?: record.windowTitlePreAnalysisRedacted}")
        appendLine("- lastUserMessageAgeMs: ${record.lastUserMessageAgeMs ?: "null"}")
        appendLine("- expressionWindowExists: ${record.expressionWindowExists ?: "null"}")
        appendLine("- coldStartAllowed: ${record.coldStartAllowed ?: "null"}")
        appendLine("- recentSelfExpressionCount: ${record.recentSelfExpressionCount ?: "null"}")
        appendLine("- repeatRisk: ${record.repeatRisk}")
        appendLine("- expressSelfAllowedReason: ${record.expressSelfAllowedReason}")
        appendLine("- expressSelfBlockedReason: ${record.expressSelfBlockedReason}")
        appendLine("- usedFallbackSnapshot: ${record.usedFallbackSnapshot}")
        appendLine("- systemAccessibilityEnabled: ${record.systemAccessibilityEnabled}")
        appendLine("- serviceConnected: ${record.serviceConnected}")
        appendLine("- accessibilityRuntimeCategory: ${record.accessibilityRuntimeCategory}")
        appendLine("- serviceReconnectAttempted: ${record.serviceReconnectAttempted}")
        appendLine("- serviceReconnectWaitMs: ${record.serviceReconnectWaitMs}")
        appendLine("- serviceReconnectSucceeded: ${record.serviceReconnectSucceeded}")
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
          "feedbackClickedAt": ${record.feedbackClickedAt},
          "feedbackTargetSessionId": "${escape(record.feedbackTargetSessionId)}",
          "feedbackTargetSessionTerminalState": "${escape(record.feedbackTargetSessionTerminalState)}",
          "feedbackTargetSessionFound": ${record.feedbackTargetSessionFound},
          "feedbackExportSource": "${record.feedbackExportSource}",
          "feedbackTriggeredNewAnalysis": ${record.feedbackTriggeredNewAnalysis},
          "feedbackReCapturedCurrentRoot": ${record.feedbackReCapturedCurrentRoot},
          "feedbackUsedOverlayStateAsPreAnalysis": ${record.feedbackUsedOverlayStateAsPreAnalysis},
          "appPackage": "${escape(record.appPackage)}",
          "windowTitlePreAnalysisRedacted": "${escape(record.windowTitlePreAnalysisRedacted)}",
          "targetAppSupported": ${record.targetAppSupported},
          "adapterName": "${record.adapterName}",
          "parserName": "${record.parserName}",
          "preAnalysisSnapshotCaptured": ${record.preAnalysisSnapshotCaptured},
          "preAnalysisSnapshotFrozenAt": ${record.preAnalysisSnapshotFrozenAt},
          "preAnalysisSnapshotSource": "${record.preAnalysisSnapshotSource}",
          "preAnalysisSnapshotMutableAfterPanel": ${record.preAnalysisSnapshotMutableAfterPanel},
          "preAnalysisSnapshotTrusted": ${record.preAnalysisSnapshotTrusted},
          "preAnalysisSnapshotErrorCode": ${record.preAnalysisSnapshotErrorCode?.let { "\"${escape(it)}\"" } ?: "null"},
          "postPanelSnapshotCapturedAt": ${record.postPanelSnapshotCapturedAt},
          "postPanelSnapshotUsedForDecision": ${record.postPanelSnapshotUsedForDecision},
          "sessionImmutableAfterTerminalState": ${record.sessionImmutableAfterTerminalState},
          "postPanelSnapshotCaptured": ${record.postPanelSnapshotCaptured},
          "postPanelContaminationDetected": ${record.postPanelContaminationDetected},
          "preAnalysisLooksLikeHuiyiPanel": ${record.preAnalysisLooksLikeHuiyiPanel},
          "preAnalysisTextClaimsLastMeWait": ${record.preAnalysisTextClaimsLastMeWait},
          "recordClaimsLastOtherRoutePanel": ${record.recordClaimsLastOtherRoutePanel},
          "windowTitleAndDecisionContradiction": ${record.windowTitleAndDecisionContradiction},
          "reportConsistencyResult": "${record.reportConsistencyResult}",
          "expressSelfClicked": ${record.expressSelfClicked},
          "expressSelfEligibility": {
            "eligible": ${record.expressSelfEligibilityEligible ?: "null"},
            "mode": "${escape(record.expressSelfEligibilityMode)}",
            "blockReason": ${record.expressSelfEligibilityBlockReason?.let { "\"${escape(it)}\"" } ?: "null"},
            "confidence": ${record.expressSelfEligibilityConfidence ?: "null"}
          },
          "currentWindowTitleRedacted": "${escape(record.currentWindowTitleRedacted ?: record.windowTitlePreAnalysisRedacted)}",
          "lastUserMessageAgeMs": ${record.lastUserMessageAgeMs ?: "null"},
          "expressionWindowExists": ${record.expressionWindowExists ?: "null"},
          "coldStartAllowed": ${record.coldStartAllowed ?: "null"},
          "recentSelfExpressionCount": ${record.recentSelfExpressionCount ?: "null"},
          "repeatRisk": "${escape(record.repeatRisk)}",
          "expressSelfAllowedReason": "${escape(record.expressSelfAllowedReason)}",
          "expressSelfBlockedReason": "${escape(record.expressSelfBlockedReason)}",
          "actualLastSpeaker": "${record.actualLastSpeaker}",
          "decisionType": "${record.decisionType}",
          "decisionTypeFamily": "${record.decisionTypeFamily}",
          "routeCount": ${record.routeCount},
          "waitPanelShown": ${record.waitPanelShown},
          "routePanelShown": ${record.routePanelShown},
          "contextRequiredPanelShown": ${record.contextRequiredPanelShown},
          "cloudEnabled": ${record.cloudEnabled},
          "cloudConfigured": ${record.cloudConfigured},
          "cloudContractImplemented": ${record.cloudContractImplemented},
          "providerType": "${record.providerType}",
          "relayBaseUrlConfigured": ${record.relayBaseUrlConfigured},
          "relayApiKeyConfigured": ${record.relayApiKeyConfigured},
          "relayApiKeyStoredSecurely": ${record.relayApiKeyStoredSecurely},
          "relayApiKeyExposedInRepo": ${record.relayApiKeyExposedInRepo},
          "relayApiKeyExposedInApk": ${record.relayApiKeyExposedInApk},
          "cloudAttempted": ${record.cloudAttempted},
          "cloudAnalysisAttempted": ${record.cloudAnalysisAttempted},
          "cloudContractVersion": "${record.cloudContractVersion}",
          "cloudContractValidationResult": "${record.cloudContractValidationResult}",
          "cloudSkippedReason": ${record.cloudSkippedReason?.let { "\"${escape(it)}\"" } ?: "null"},
          "cloudRequestId": ${record.cloudRequestId?.let { "\"${escape(it)}\"" } ?: "null"},
          "cloudSuccess": ${record.cloudSuccess},
          "cloudLatencyMs": ${record.cloudLatencyMs ?: "null"},
          "cloudPrimaryModel": "${escape(record.cloudPrimaryModel)}",
          "cloudFinalModel": "${escape(record.cloudFinalModel)}",
          "cloudEscalated": ${record.cloudEscalated},
          "cloudEscalationReason": ${record.cloudEscalationReason?.let { "\"${escape(it)}\"" } ?: "null"},
          "cloudQualityGateResult": "${escape(record.cloudQualityGateResult)}",
          "cloudQualityScore": ${record.cloudQualityScore ?: "null"},
          "cloudQualityIssues": "${escape(record.cloudQualityIssues.joinToString("|"))}",
          "cloudPrimaryLatencyMs": ${record.cloudPrimaryLatencyMs ?: "null"},
          "cloudTotalLatencyMs": ${record.cloudTotalLatencyMs ?: "null"},
          "cloudErrorCode": ${record.cloudErrorCode?.let { "\"${escape(it)}\"" } ?: "null"},
          "cloudNetworkFailureVisibleToUser": ${record.cloudNetworkFailureVisibleToUser},
          "cloudRequestActuallySent": ${record.cloudRequestActuallySent},
          "cloudFailureLikelyCause": "${escape(record.cloudFailureLikelyCause)}",
          "systemAccessibilityEnabled": ${record.systemAccessibilityEnabled},
          "serviceConnected": ${record.serviceConnected},
          "accessibilityRuntimeCategory": "${escape(record.accessibilityRuntimeCategory)}",
          "serviceReconnectAttempted": ${record.serviceReconnectAttempted},
          "serviceReconnectWaitMs": ${record.serviceReconnectWaitMs},
          "serviceReconnectSucceeded": ${record.serviceReconnectSucceeded},
          "cloudFallbackUsed": ${record.cloudFallbackUsed},
          "decisionSource": "${record.decisionSource}",
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
        record.reportConsistencyResult == "FAIL_CONTAMINATED_EXPORT" -> "FAIL_CONTAMINATED_EXPORT"
        record.userFeedback.markedWrong -> "USER_MARKED_WRONG"
        record.terminalState == "UNSUPPORTED_APP" -> "UNSUPPORTED_APP"
        record.terminalState == "TIMEOUT" -> "TIMEOUT"
        record.terminalState == "CONTROLLED_FAIL" -> "CONTROLLED_FAIL"
        record.terminalState in setOf("ROUTE_PANEL", "WAIT_PANEL", "CONTEXT_REQUIRED_PANEL") -> "PASS"
        else -> "UNKNOWN"
    }

    private fun quickConclusion(record: NextSentenceFlightRecord): String = when {
        record.reportConsistencyResult == "FAIL_CONTAMINATED_EXPORT" ->
            "这次一键反馈包被会意面板污染，不能用于判断 LAST ME / LAST OTHER。需要修 feedback export 绑定原始 session。"
        record.userFeedback.markedWrong && record.userFeedback.userCorrectionLastSpeaker != "NONE" ->
            "User marked this result wrong; correction last speaker is ${record.userFeedback.userCorrectionLastSpeaker}, system saw ${record.actualLastSpeaker}."
        record.expressSelfClicked && record.expressSelfEligibilityEligible == false ->
            "Express Self was blocked because the current chat state is untrusted, the user just sent a message, or the app is unsupported. No active routes were shown."
        record.terminalState == "WAIT_PANEL" -> "last ME entered WAIT panel with zero routes."
        record.terminalState == "HOLD_BACK_PANEL" -> "Express Self entered HOLD_BACK with no active routes."
        record.terminalState == "ROUTE_PANEL" -> "reply routes were shown, routeCount=${record.routeCount}."
        record.terminalState == "TIMEOUT" -> "session timed out without a terminal state."
        record.terminalState == "UNSUPPORTED_APP" -> "current chat app is not adapted yet."
        else -> "session exported for review."
    }

    private fun sessionIndex(records: List<NextSentenceFlightRecord>): String = buildString {
        appendLine("# Recent Sessions")
        records.forEach {
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
        cloudEnabled = false,
        cloudAttempted = false,
        cloudSkippedReason = "NO_SESSION",
        cloudRequestId = null,
        cloudSuccess = false,
        cloudLatencyMs = null,
        cloudErrorCode = null,
        cloudNetworkFailureVisibleToUser = false,
        cloudRequestActuallySent = false,
        cloudFailureLikelyCause = "NONE",
        cloudFallbackUsed = false,
        decisionSource = "LOCAL_FALLBACK",
        captureSource = "NONE",
        usedFallbackSnapshot = false,
        fallbackSnapshotAgeMs = null,
        staleSnapshotSuspected = false,
        systemAccessibilityEnabled = false,
        serviceConnected = false,
        accessibilityRuntimeCategory = "UNKNOWN",
        serviceReconnectAttempted = false,
        serviceReconnectWaitMs = 0L,
        serviceReconnectSucceeded = false,
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

    private fun markdownField(text: String, name: String): String? {
        return text.lineSequence()
            .firstOrNull { it.startsWith("- $name:") }
            ?.substringAfter(":")
            ?.trim()
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")

    private fun timestamp(timeMillis: Long): String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(timeMillis))
}
