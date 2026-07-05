package com.huiyi.v4.runtime

import android.content.Context
import android.content.Intent
import android.util.Log
import com.huiyi.v4.data.DatabaseProvider
import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.cloud.CloudRuntimeSettings
import com.huiyi.v4.domain.capture.ManualContextCaptureSession
import com.huiyi.v4.domain.capture.VisualDebugResult
import com.huiyi.v4.domain.context.ArcRevealDepth
import com.huiyi.v4.domain.context.CharacterArcPlanner
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.InfluenceProfile
import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.model.ReplyAttemptStatus
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserAction
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.persona.CharacterArcActiveSampler
import com.huiyi.v4.domain.persona.CharacterArcCandidate
import com.huiyi.v4.domain.persona.CharacterArcPreferenceProfile
import com.huiyi.v4.domain.persona.CharacterArcPreferenceRecord
import com.huiyi.v4.domain.persona.CharacterArcPreferenceStore
import com.huiyi.v4.domain.persona.CharacterArcReviewItem
import com.huiyi.v4.domain.persona.CharacterArcUserFeedback
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.LateCloudPipelineResult
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.pipeline.LastSpeakerAcceptanceReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceCaptureSource
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceFailureReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.ParserReportGenerator
import com.huiyi.v4.domain.pipeline.RealDeviceReviewBundleGenerator
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.RealDeviceTestIntent
import com.huiyi.v4.domain.pipeline.ReplyAttemptFactory
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.accessibility.LastStableForeignWindowSnapshot
import com.huiyi.v4.accessibility.AccessibilityRuntimeReader
import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.accessibility.accessibilityRuntimeMessage
import com.huiyi.v4.floating.OverlayStateStore
import com.huiyi.v4.domain.pipeline.EvidencePackFiles
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.domain.pipeline.toNextSentenceException
import com.huiyi.v4.domain.pipeline.userFacingMessageFor
import com.huiyi.v4.domain.pipeline.mapScreenshotException
import com.huiyi.v4.domain.pipeline.redactPrivateText
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.DynamicPlaybookResult
import com.huiyi.v4.domain.playbook.PlaybookRefreshScheduler
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import com.huiyi.v4.ui.HuiyiDemoState
import com.huiyi.v4.ui.sampleState
import com.huiyi.v4.update.LanUpdateManager
import com.huiyi.v4.update.LanUpdateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class HuiyiRuntimeState(
    val demoState: HuiyiDemoState = sampleState(),
    val latestPipelineResult: CurrentScreenPipelineResult? = null,
    val floatingPanelMode: FloatingPanelMode = FloatingPanelMode.NEXT_SENTENCE,
    val panelVisible: Boolean = false,
    val lastError: String? = null,
    val lastDebugExportPath: String? = null,
    val lastEvidenceJsonPath: String? = null,
    val lastPublicExportPath: String? = null,
    val selectedRealDeviceScenario: RealDeviceScenario = RealDeviceScenario.AUTO_FROM_SCREEN,
    val showParserDiagnostics: Boolean = false,
    val lastDebugCorrection: String? = null,
    val lastVisualDebugOverlayPath: String? = null,
    val clickDiagnostics: List<AccessibilityClickSample> = emptyList(),
    val lastClickPipelineException: String? = null,
    val lastNextSentenceTrace: NextSentenceSessionTrace? = null,
    val latestNextSentenceFailureMarkdownPath: String? = null,
    val latestNextSentenceFailureJsonPath: String? = null,
    val latestPhoneGptReviewBundlePath: String? = null,
    val latestOneTapFeedbackBundlePath: String? = null,
    val latestOneTapGithubUploadReportPath: String? = null,
    val oneTapGithubUploadState: OneTapGithubUploadState = OneTapGithubUploadState(),
    val latestFlightRecord: NextSentenceFlightRecord? = null,
    val recentFlightRecords: List<NextSentenceFlightRecord> = emptyList(),
    val lanUpdateState: LanUpdateState = LanUpdateState(),
    val cloudSettings: CloudRuntimeSettings = CloudRuntimeSettings(),
    val cloudSettingsTestStatus: String = "NOT_TESTED",
    val nextSentenceUiState: NextSentenceUiState = NextSentenceUiState.IDLE,
    val characterArcPreferenceProfile: CharacterArcPreferenceProfile = CharacterArcPreferenceProfile()
)

enum class NextSentenceUiState {
    IDLE,
    CLICK_ACK,
    LOADING_CAPTURE,
    LOADING_CLOUD,
    RESULT,
    CONTROLLED_FAIL,
    TIMEOUT
}

data class NextSentenceClickAck(
    val clickReceivedAt: Long = System.currentTimeMillis(),
    val clickAckShownAt: Long? = null,
    val clickAckVisible: Boolean = false,
    val panelVisibleBeforeClick: Boolean = false,
    val panelVisibleAfterClick: Boolean = false,
    val bubbleVisibleAfterClick: Boolean = true
) {
    val clickAckLatencyMs: Long?
        get() = clickAckShownAt?.let { it - clickReceivedAt }
}

data class AccessibilityClickSample(
    val label: String,
    val capturedAt: Long,
    val runtimeState: AccessibilityRuntimeState,
    val overlayState: com.huiyi.v4.floating.OverlayRuntimeState
)

class HuiyiRuntime private constructor(
    private val appContext: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistence = HuiyiPersistenceRepository(DatabaseProvider.get(appContext).huiyiDao())
    private val updateManager = LanUpdateManager(appContext)
    private val prefs = appContext.getSharedPreferences("huiyi-runtime", Context.MODE_PRIVATE)
    private val cloudSettingsRepository = RuntimeCloudSettingsRepository.create(appContext)
    private val characterArcPreferenceStore = CharacterArcPreferenceStore(
        File(appContext.filesDir, "character_arc/preferences.jsonl")
    )
    private val pipeline = CurrentScreenPipelineUseCase(
        captureUseCase = CurrentScreenCaptureUseCase(),
        persistenceRepository = persistence,
        cloudAnalysisService = RuntimeCloudAnalysisService(cloudSettingsRepository),
        visualEvidenceProvider = {
            VisualCloudScreenshotCapture().capture(HuiyiAccessibilityService.instance)
        },
        recentVisualEvidenceProvider = { capture ->
            HuiyiAccessibilityService.instance?.recentVisualCheckpointsFor(capture.snapshot).orEmpty()
        },
        lightListenContextProvider = { capture ->
            HuiyiAccessibilityService.instance?.lightListenBackfillFor(capture.snapshot, capture.messages).orEmpty()
        },
        appVersionName = BuildConfig.VERSION_NAME,
        appVersionCode = BuildConfig.VERSION_CODE,
        lateCloudScope = scope,
        onLateCloudResult = { lateResult -> handleLateCloudResult(lateResult) }
    )
    private val dynamicPlaybookEngine = DynamicPlaybookEngine()
    private val playbookRefreshScheduler = PlaybookRefreshScheduler(dynamicPlaybookEngine)
    private var sessionWatchdogJob: Job? = null
    private var activeNextSentenceJob: Job? = null
    @Volatile
    private var activeNextSentenceSessionId: String? = null

    private val mutableState = MutableStateFlow(HuiyiRuntimeState())
    val state: StateFlow<HuiyiRuntimeState> = mutableState

    init {
        val savedUrl = prefs.getString("lan_update_url", "").orEmpty()
        val initialUrl = savedUrl.ifBlank { BuildConfig.HUIYI_UPDATE_BASE_URL }
        mutableState.update {
            it.copy(
                lanUpdateState = it.lanUpdateState.copy(updateUrl = initialUrl),
                cloudSettings = cloudSettingsRepository.load(),
                characterArcPreferenceProfile = characterArcPreferenceStore.buildProfile()
            )
        }
    }

    fun saveCloudSettings(
        cloudEnabled: Boolean,
        providerType: String,
        baseUrl: String,
        model: String,
        timeoutMs: Long,
        apiKeyInput: String?
    ) {
        val saved = cloudSettingsRepository.save(
            cloudEnabled = cloudEnabled,
            providerType = providerType.ifBlank { CloudProviderType.OPENAI_COMPATIBLE_RELAY },
            baseUrl = baseUrl,
            model = model,
            timeoutMs = timeoutMs,
            apiKeyInput = apiKeyInput
        )
        mutableState.update {
            it.copy(
                cloudSettings = saved,
                cloudSettingsTestStatus = "SAVED_${saved.relayApiKeyStorageMode}"
            )
        }
    }

    fun testCloudSettings() {
        val settings = cloudSettingsRepository.load()
        val status = when {
            !settings.cloudEnabled -> "CLOUD_DISABLED"
            !settings.relayBaseUrlConfigured -> "CLOUD_NOT_CONFIGURED"
            !settings.relayApiKeyConfigured -> "RELAY_API_KEY_MISSING"
            else -> "READY_FOR_RELAY_RUNTIME_CALL"
        }
        mutableState.update { it.copy(cloudSettings = settings, cloudSettingsTestStatus = status) }
    }

    fun setPanelVisible(visible: Boolean) {
        mutableState.update { it.copy(panelVisible = visible) }
    }

    fun setRealDeviceScenario(scenario: RealDeviceScenario) {
        mutableState.update { it.copy(selectedRealDeviceScenario = scenario) }
    }

    fun toggleParserDiagnostics() {
        mutableState.update { it.copy(showParserDiagnostics = !it.showParserDiagnostics) }
    }

    fun markOverlayPanelShown() {
        mutableState.update { state ->
            val result = state.latestPipelineResult
            state.copy(
                latestPipelineResult = result?.copy(
                    overlayShownInTargetApp = true,
                    foregroundPackageWhenPanelShown = result.captureResult?.snapshot?.appPackage,
                    huiyiActivityOpened = false,
                    userStayedInChatApp = true,
                    resultShownAsOverlay = true,
                    mainActivityOpened = false,
                    panelSessionId = result.sessionId,
                    panelContentFromCurrentSession = result.panelSessionId == null || result.panelSessionId == result.sessionId
                )
            )
        }
    }

    private fun handleLateCloudResult(lateResult: LateCloudPipelineResult) {
        scope.launch {
            val state = mutableState.value
            val current = state.latestPipelineResult
            val trace = state.lastNextSentenceTrace
            lateCloudDiscardReason(lateResult, current, trace)?.let { reason ->
                Log.i(
                    LOG_TAG,
                    "late_cloud_result_discarded sessionId=${lateResult.sessionId} " +
                        "activeSessionId=$activeNextSentenceSessionId reason=$reason"
                )
                return@launch
            }
            if (current == null || trace == null) return@launch
            val endedAt = System.currentTimeMillis()
            val terminalState = terminalStateFor(lateResult.decision.decisionType)
            val updatedCloudTrace = lateResult.trace.withSessionBinding(
                activeSessionId = lateResult.sessionId,
                preAnalysisSnapshotId = lateResult.preAnalysisSnapshotId,
                chatPackage = lateResult.chatPackage,
                chatWindowHash = lateResult.chatWindowHash,
                cloudRequestSessionId = lateResult.trace.cloudRequestSessionId ?: lateResult.sessionId,
                cloudResponseSessionId = lateResult.trace.cloudResponseSessionId ?: lateResult.sessionId,
                panelRenderedSessionId = lateResult.sessionId,
                oneClickOneTerminalPanel = true
            )
            val upgradedResult = current.copy(
                tacticalDecision = lateResult.decision,
                routes = lateResult.routes,
                apiCalled = updatedCloudTrace.apiCalled,
                waitPanelShown = false,
                routePanelShown = lateResult.routes.isNotEmpty(),
                sessionTerminalState = terminalState,
                analysisEndedAt = endedAt,
                analysisDurationMs = if (current.analysisStartedAt > 0L) {
                    endedAt - current.analysisStartedAt
                } else {
                    current.analysisDurationMs
                },
                cloudTrace = updatedCloudTrace
            )
            val updatedTrace = trace.copy(
                endedAt = endedAt,
                stage = NextSentenceStage.ROUTES_GENERATED,
                decisionType = lateResult.decision.decisionType.name,
                routeCount = lateResult.routes.size,
                apiCalled = updatedCloudTrace.apiCalled,
                terminalState = terminalState,
                panelShown = true,
                panelAttached = true,
                panelRenderSuccess = true,
                userFacingMessage = null
            )
            writeLatestSessionTraceReports(updatedTrace)
            val flightRecord = NextSentenceFlightRecordFactory.fromSuccess(upgradedResult, updatedTrace)
            mutableState.update { latest ->
                val latestCurrent = latest.latestPipelineResult
                lateCloudDiscardReason(lateResult, latestCurrent, latest.lastNextSentenceTrace)?.let {
                    latest
                } ?: latest.copy(
                    latestPipelineResult = upgradedResult,
                    panelVisible = true,
                    lastError = null,
                    nextSentenceUiState = NextSentenceUiState.RESULT,
                    lastNextSentenceTrace = updatedTrace,
                    latestFlightRecord = flightRecord,
                    recentFlightRecords = (latest.recentFlightRecords + flightRecord).takeLast(10)
                )
            }
            Log.i(
                LOG_TAG,
                "late_cloud_result_applied sessionId=${lateResult.sessionId} " +
                    "decisionSource=${updatedCloudTrace.decisionSource} routes=${lateResult.routes.size}"
            )
        }
    }

    private fun lateCloudDiscardReason(
        lateResult: LateCloudPipelineResult,
        current: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?
    ): String? {
        if (activeNextSentenceSessionId != lateResult.sessionId) return "STALE_SESSION"
        if (trace?.sessionId != lateResult.sessionId) return "TRACE_SESSION_MISMATCH"
        if (current == null) return "NO_PANEL_TO_UPGRADE"
        if (current.sessionId != lateResult.sessionId) return "PIPELINE_SESSION_MISMATCH"
        if (current.panelSessionId != null && current.panelSessionId != lateResult.sessionId) {
            return "PANEL_SESSION_MISMATCH"
        }
        if (current.tacticalDecision.decisionType == TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED ||
            current.tacticalDecision.decisionType == TacticalDecisionType.CHAT_WINDOW_NOT_FOUND
        ) {
            return "PRE_ANALYSIS_CONTAMINATED"
        }
        val currentTrace = current.cloudTrace
        val currentSnapshotId = currentTrace.preAnalysisSnapshotId.orEmpty()
        if (currentSnapshotId.isNotBlank() && currentSnapshotId != lateResult.preAnalysisSnapshotId) {
            return "SNAPSHOT_CHANGED"
        }
        val currentPackage = currentTrace.chatPackage.orEmpty()
        if (currentPackage.isNotBlank() && currentPackage != lateResult.chatPackage) {
            return "CHAT_PACKAGE_CHANGED"
        }
        val currentWindowHash = currentTrace.chatWindowHash.orEmpty()
        if (currentWindowHash.isNotBlank() && currentWindowHash != lateResult.chatWindowHash) {
            return "CHAT_WINDOW_CHANGED"
        }
        val foregroundPackage = HuiyiAccessibilityService.state.value.currentPackage
        if (foregroundPackage != null &&
            foregroundPackage != lateResult.chatPackage &&
            foregroundPackage != appContext.packageName
        ) {
            val stableSnapshotMatch = currentSnapshotId.isNotBlank() &&
                currentSnapshotId == lateResult.preAnalysisSnapshotId
            val stableWindowMatch = currentPackage.isNotBlank() &&
                currentPackage == lateResult.chatPackage &&
                currentWindowHash.isNotBlank() &&
                currentWindowHash == lateResult.chatWindowHash
            if (!stableSnapshotMatch && !stableWindowMatch) {
                return "FOREGROUND_PACKAGE_CHANGED"
            }
        }
        return null
    }

    fun togglePersona() {
        mutableState.update { it.copy(demoState = it.demoState.togglePersona()) }
    }

    fun runNextSentence(clickAck: NextSentenceClickAck = NextSentenceClickAck()): String {
        resumePendingCloudSessionIfAny()?.let { return it }
        val sessionId = UUID.randomUUID().toString()
        val beforeRuntime = AccessibilityRuntimeReader.read(appContext)
        val beforeOverlay = OverlayStateStore.state.value
        val startedTrace = NextSentenceSessionTrace(
            sessionId = sessionId,
            startedAt = clickAck.clickReceivedAt,
            stage = if (clickAck.clickAckVisible) NextSentenceStage.CLICK_ACK_SHOWN else NextSentenceStage.CLICK_RECEIVED,
            clickReceivedAt = clickAck.clickReceivedAt,
            clickAckShownAt = clickAck.clickAckShownAt,
            clickAckLatencyMs = clickAck.clickAckLatencyMs,
            clickAckVisible = clickAck.clickAckVisible,
            runNextSentenceEntered = true,
            sessionCreated = true,
            terminalState = null,
            panelShown = false,
            panelVisibleBeforeClick = clickAck.panelVisibleBeforeClick,
            panelVisibleAfterClick = clickAck.panelVisibleAfterClick,
            bubbleVisibleBeforeClick = beforeOverlay.bubbleVisible,
            bubbleVisibleAfterClick = clickAck.bubbleVisibleAfterClick,
            bubbleAttachedAfterClick = clickAck.bubbleVisibleAfterClick,
            systemAccessibilityEnabled = beforeRuntime.systemAccessibilityEnabled,
            serviceConnected = beforeRuntime.serviceConnected,
            activePackageBeforeClick = beforeRuntime.currentPackage,
            activeWindowTitleAtClick = beforeRuntime.currentWindowTitle,
            rootAvailableAtClick = beforeRuntime.rootAvailable
        )
        activeNextSentenceJob?.cancel()
        sessionWatchdogJob?.cancel()
        activeNextSentenceSessionId = sessionId
        val previousSessionId = mutableState.value.lastNextSentenceTrace?.sessionId
        val scenarioAtStart = mutableState.value.selectedRealDeviceScenario
        mutableState.update {
            it.copy(
                lastNextSentenceTrace = startedTrace,
                lastError = null,
                latestPipelineResult = null,
                floatingPanelMode = FloatingPanelMode.NEXT_SENTENCE,
                panelVisible = false,
                nextSentenceUiState = if (clickAck.clickAckVisible) NextSentenceUiState.CLICK_ACK else NextSentenceUiState.LOADING_CAPTURE
            )
        }
        writeLatestSessionTraceReports(startedTrace)
        activeNextSentenceJob = scope.launch {
            sampleClickState("beforeClick")
            launchClickFollowupSamples()
            val captureStartingTrace = startedTrace.copy(stage = NextSentenceStage.CAPTURE_STARTING)
            mutableState.update { state ->
                if (state.lastNextSentenceTrace?.sessionId == sessionId) {
                    state.copy(
                        lastNextSentenceTrace = captureStartingTrace,
                        nextSentenceUiState = NextSentenceUiState.LOADING_CAPTURE
                    )
                } else {
                    state
                }
            }
            writeLatestSessionTraceReports(captureStartingTrace)
            launchSessionTerminalWatchdogV2(startedTrace, scenarioAtStart)
            var traceForThisRun = captureStartingTrace
            try {
                if (tryRenderDynamicPlaybookFromStableSnapshot(
                        mode = DynamicPlaybookMode.NEXT_SENTENCE,
                        sessionId = sessionId,
                        trace = captureStartingTrace,
                        previousSessionId = previousSessionId
                    )
                ) {
                    return@launch
                }
                traceForThisRun = waitForAccessibilityConnection(captureStartingTrace)
                mutableState.update { state ->
                    if (state.lastNextSentenceTrace?.sessionId == sessionId) {
                        state.copy(lastNextSentenceTrace = traceForThisRun)
                    } else {
                        state
                    }
                }
                writeLatestSessionTraceReports(traceForThisRun)
                val persona = currentPersona()
                val result = pipeline.run(persona, sessionId = traceForThisRun.sessionId)
                ensureActive()
                result.fold(
                    onSuccess = { pipelineResult ->
                        if (!isActiveNextSentenceSession(traceForThisRun.sessionId)) {
                            Log.i(
                                LOG_TAG,
                                "next_sentence_discarded sessionId=${traceForThisRun.sessionId} " +
                                    "activeSessionId=$activeNextSentenceSessionId reason=STALE_SESSION"
                            )
                            return@fold
                        }
                        val preRenderState = mutableState.value
                        if (preRenderState.latestPipelineResult != null || preRenderState.lastError != null) {
                            Log.i(
                                LOG_TAG,
                                "next_sentence_discarded sessionId=${traceForThisRun.sessionId} " +
                                    "activeSessionId=$activeNextSentenceSessionId reason=PANEL_SESSION_MISMATCH"
                            )
                            return@fold
                        }
                        Log.i(
                            LOG_TAG,
                            "next_sentence_success package=${pipelineResult.captureResult?.snapshot?.appPackage} " +
                                "sessionId=${traceForThisRun.sessionId} " +
                                "activeSessionId=$activeNextSentenceSessionId " +
                                "cloudRequestSessionId=${pipelineResult.cloudTrace.cloudRequestSessionId} " +
                                "cloudResponseSessionId=${pipelineResult.cloudTrace.cloudResponseSessionId} " +
                                "preAnalysisSnapshotId=${pipelineResult.cloudTrace.preAnalysisSnapshotId} " +
                                "chatPackage=${pipelineResult.cloudTrace.chatPackage} " +
                                "chatWindowHash=${pipelineResult.cloudTrace.chatWindowHash} " +
                                "captureSource=${pipelineResult.captureResult?.captureSource} " +
                                "lastSpeaker=${pipelineResult.lastSpeakerDecision.lastSpeaker} " +
                                "decision=${pipelineResult.tacticalDecision.decisionType} " +
                                "routes=${pipelineResult.routes.size} " +
                                "cloudAttempted=${pipelineResult.cloudTrace.cloudAttempted} " +
                                "cloudSuccess=${pipelineResult.cloudTrace.cloudSuccess} " +
                                "cloudErrorCode=${pipelineResult.cloudTrace.cloudErrorCode} " +
                                "cloudValidation=${pipelineResult.cloudTrace.cloudContractValidationResult} " +
                                "cloudLikelyCause=${pipelineResult.cloudTrace.cloudFailureLikelyCause} " +
                                "decisionSource=${pipelineResult.cloudTrace.decisionSource}"
                        )
                        val messages = pipelineResult.context?.currentScreenMessages ?: mutableState.value.demoState.messages
                        val scenario = mutableState.value.selectedRealDeviceScenario
                        val visualDebug = VisualDebugResult(
                            screenshotCaptured = false,
                            screenshotUnavailable = true,
                            reason = "REAL_USE_FAST_PATH",
                            screenshotPath = null,
                            overlayImagePath = null,
                            screenshotWidth = pipelineResult.captureResult?.snapshot?.screenWidth ?: 0,
                            screenshotHeight = pipelineResult.captureResult?.snapshot?.screenHeight ?: 0,
                            accessibilityBoundsProjected = pipelineResult.captureResult?.accessibilityBoundsProjected == true,
                            ocrUsed = false,
                            visualTruthAvailable = pipelineResult.captureResult?.visualTruthAvailable == true,
                            screenshotErrorCode = null,
                            screenshotExceptionClass = null,
                            screenshotExceptionMessageRedacted = null
                        )
                        val waitPanelShown = pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT &&
                            pipelineResult.routes.isEmpty()
                        val routePanelShown = pipelineResult.routes.isNotEmpty()
                        val endedAt = System.currentTimeMillis()
                        val terminalState = terminalStateFor(pipelineResult.tacticalDecision.decisionType)
                        val resultWithVisualDebug = pipelineResult.copy(
                            visualDebugResult = visualDebug,
                            sessionId = traceForThisRun.sessionId,
                            previousSessionId = previousSessionId,
                            panelSessionId = traceForThisRun.sessionId,
                            panelContentFromCurrentSession = true,
                            staleRoutesClearedAtSessionStart = true,
                            staleRoutesReused = false,
                            waitPanelShown = waitPanelShown,
                            routePanelShown = routePanelShown,
                            sessionTerminalState = terminalState,
                            analysisStartedAt = traceForThisRun.startedAt,
                            analysisEndedAt = endedAt,
                            analysisDurationMs = endedAt - traceForThisRun.startedAt,
                            loadingStillVisibleAfterTimeout = false,
                            waitDecisionReached = pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT,
                            waitPanelRenderAttempted = waitPanelShown,
                            waitPanelRenderSuccess = waitPanelShown,
                            decisionTypeFamily = decisionTypeFamily(pipelineResult.tacticalDecision.decisionType),
                            cloudTrace = pipelineResult.cloudTrace.withSessionBinding(
                                activeSessionId = traceForThisRun.sessionId,
                                preAnalysisSnapshotId = pipelineResult.cloudTrace.preAnalysisSnapshotId.orEmpty(),
                                chatPackage = pipelineResult.cloudTrace.chatPackage.orEmpty(),
                                chatWindowHash = pipelineResult.cloudTrace.chatWindowHash.orEmpty(),
                                cloudRequestSessionId = pipelineResult.cloudTrace.cloudRequestSessionId,
                                cloudResponseSessionId = pipelineResult.cloudTrace.cloudResponseSessionId,
                                panelRenderedSessionId = traceForThisRun.sessionId,
                                oneClickOneTerminalPanel = true
                            )
                        )
                        val capture = pipelineResult.captureResult
                        val successTrace = traceForThisRun.copy(
                            endedAt = endedAt,
                            stage = NextSentenceStage.ROUTES_GENERATED,
                            activePackageAtCaptureStart = capture?.currentRootPackageAtCapture,
                            activePackageAfterRootRetry = capture?.snapshot?.appPackage,
                            rootAvailableFirstTry = capture?.rootAvailableFirstTry ?: true,
                            rootRetryCount = capture?.rootRetryCount ?: 0,
                            rootAvailableAfterRetry = capture?.rootAvailableAfterRetry ?: true,
                            rootPackageName = capture?.snapshot?.appPackage,
                            rootWindowTitle = capture?.snapshot?.windowTitle,
                            rootIsOwnOverlay = capture?.rootIsOwnOverlay ?: false,
                            rootIsSystemUi = capture?.rootIsSystemUi ?: false,
                            rootIsTargetChatApp = capture?.snapshot?.appPackage !in setOf(null, appContext.packageName, "com.android.systemui"),
                            captureSource = capture?.captureSource ?: NextSentenceCaptureSource.CURRENT_ROOT,
                            primaryCapturePath = if (capture?.captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) {
                                "LAST_STABLE_CHAT_SNAPSHOT"
                            } else {
                                "NODE_TREE"
                            },
                            nodeTreeAttempted = true,
                            nodeTreeSuccess = capture?.captureSource == NextSentenceCaptureSource.CURRENT_ROOT,
                            fallbackSnapshotAttempted = true,
                            fallbackSnapshotSuccess = capture?.usedFallbackSnapshot == true,
                            usedFallbackSnapshot = capture?.usedFallbackSnapshot ?: false,
                            lastStableSnapshotAgeMs = capture?.lastStableSnapshotAgeMs,
                            lastStableSnapshotPackage = capture?.lastStableSnapshotPackage,
                            rawNodeCount = capture?.rawNodeCount ?: 0,
                            visibleTextCount = capture?.visibleTextCount ?: 0,
                            parsedMessageCount = capture?.messages?.size ?: 0,
                            effectiveMessageCount = capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: 0,
                            lastEffectiveSpeaker = pipelineResult.lastSpeakerDecision.lastSpeaker,
                            decisionType = pipelineResult.tacticalDecision.decisionType.name,
                            routeCount = pipelineResult.routes.size,
                            apiCalled = pipelineResult.apiCalled,
                            screenshotAttempted = true,
                            screenshotSuccess = visualDebug.screenshotCaptured,
                            screenshotAvailable = visualDebug.screenshotCaptured,
                            screenshotCapabilityDeclared = false,
                            screenshotErrorCode = visualDebug.screenshotErrorCode?.let { NextSentenceErrorCode.valueOf(it) },
                            screenshotExceptionClass = visualDebug.screenshotExceptionClass,
                            screenshotExceptionMessageRedacted = visualDebug.screenshotExceptionMessageRedacted,
                            secondaryErrorCode = visualDebug.screenshotErrorCode?.let { NextSentenceErrorCode.valueOf(it) },
                            panelAttached = true,
                            panelRenderSuccess = true,
                            serviceConnected = true,
                            serviceReconnectSucceeded = traceForThisRun.serviceReconnectAttempted || traceForThisRun.serviceReconnectSucceeded,
                            terminalState = terminalState,
                            panelShown = true,
                            userFacingMessage = if (pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT) {
                                "你已经回过了，先等对方。"
                            } else null
                        )
                        writeLatestSessionTraceReports(successTrace)
                        val flightRecord = NextSentenceFlightRecordFactory.fromSuccess(resultWithVisualDebug, successTrace)
                        mutableState.update {
                            if (it.lastNextSentenceTrace?.sessionId != traceForThisRun.sessionId ||
                                activeNextSentenceSessionId != traceForThisRun.sessionId
                            ) {
                                Log.i(
                                    LOG_TAG,
                                    "next_sentence_discarded sessionId=${traceForThisRun.sessionId} " +
                                        "stateSessionId=${it.lastNextSentenceTrace?.sessionId} " +
                                        "activeSessionId=$activeNextSentenceSessionId reason=STALE_SESSION"
                                )
                                it
                            } else it.copy(
                                demoState = it.demoState.copy(messages = messages),
                                latestPipelineResult = resultWithVisualDebug,
                                floatingPanelMode = FloatingPanelMode.NEXT_SENTENCE,
                                panelVisible = true,
                                lastError = null,
                                nextSentenceUiState = NextSentenceUiState.RESULT,
                                lastVisualDebugOverlayPath = visualDebug.overlayImagePath,
                                lastClickPipelineException = null,
                                lastNextSentenceTrace = successTrace,
                                latestFlightRecord = flightRecord,
                                recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                            )
                        }
                    },
                    onFailure = { error ->
                        handleNextSentenceFailure(error, traceForThisRun)
                    }
                )
            } catch (cancelled: CancellationException) {
                Log.i(
                    LOG_TAG,
                    "next_sentence_discarded sessionId=${startedTrace.sessionId} " +
                        "activeSessionId=$activeNextSentenceSessionId reason=STALE_SESSION"
                )
            } catch (error: Throwable) {
                handleNextSentenceFailure(error, (error as? com.huiyi.v4.domain.pipeline.NextSentenceException)?.trace ?: traceForThisRun)
            }
        }
        return sessionId
    }

    fun runExpressSelf(clickAck: NextSentenceClickAck = NextSentenceClickAck()): String {
        val sessionId = UUID.randomUUID().toString()
        val beforeRuntime = AccessibilityRuntimeReader.read(appContext)
        val beforeOverlay = OverlayStateStore.state.value
        val startedTrace = NextSentenceSessionTrace(
            sessionId = sessionId,
            startedAt = clickAck.clickReceivedAt,
            stage = if (clickAck.clickAckVisible) NextSentenceStage.CLICK_ACK_SHOWN else NextSentenceStage.CLICK_RECEIVED,
            clickReceivedAt = clickAck.clickReceivedAt,
            clickAckShownAt = clickAck.clickAckShownAt,
            clickAckLatencyMs = clickAck.clickAckLatencyMs,
            clickAckVisible = clickAck.clickAckVisible,
            runNextSentenceEntered = true,
            sessionCreated = true,
            terminalState = null,
            panelShown = false,
            panelVisibleBeforeClick = clickAck.panelVisibleBeforeClick,
            panelVisibleAfterClick = clickAck.panelVisibleAfterClick,
            bubbleVisibleBeforeClick = beforeOverlay.bubbleVisible,
            bubbleVisibleAfterClick = clickAck.bubbleVisibleAfterClick,
            bubbleAttachedAfterClick = clickAck.bubbleVisibleAfterClick,
            systemAccessibilityEnabled = beforeRuntime.systemAccessibilityEnabled,
            serviceConnected = beforeRuntime.serviceConnected,
            activePackageBeforeClick = beforeRuntime.currentPackage,
            activeWindowTitleAtClick = beforeRuntime.currentWindowTitle,
            rootAvailableAtClick = beforeRuntime.rootAvailable
        )
        activeNextSentenceJob?.cancel()
        sessionWatchdogJob?.cancel()
        activeNextSentenceSessionId = sessionId
        mutableState.update {
            it.copy(
                lastNextSentenceTrace = startedTrace,
                lastError = null,
                latestPipelineResult = null,
                floatingPanelMode = FloatingPanelMode.EXPRESS_SELF,
                panelVisible = false,
                nextSentenceUiState = if (clickAck.clickAckVisible) NextSentenceUiState.CLICK_ACK else NextSentenceUiState.LOADING_CAPTURE
            )
        }
        writeLatestSessionTraceReports(startedTrace)
        activeNextSentenceJob = scope.launch {
            val captureTrace = startedTrace.copy(stage = NextSentenceStage.CAPTURE_STARTING)
            mutableState.update { state ->
                if (state.lastNextSentenceTrace?.sessionId == sessionId) {
                    state.copy(
                        lastNextSentenceTrace = captureTrace,
                        nextSentenceUiState = NextSentenceUiState.LOADING_CAPTURE
                    )
                } else {
                    state
                }
            }
            try {
                if (tryRenderDynamicPlaybookFromStableSnapshot(
                        mode = DynamicPlaybookMode.EXPRESS_SELF,
                        sessionId = sessionId,
                        trace = captureTrace,
                        previousSessionId = null
                    )
                ) {
                    return@launch
                }
                val checkedTrace = waitForAccessibilityConnection(captureTrace)
                val capture = withContext(Dispatchers.Default) { CurrentScreenCaptureUseCase().capture().getOrThrow() }
                ensureActive()
                val messages = capture.messages.ifEmpty { mutableState.value.demoState.messages }
                val persona = currentPersona()
                val context = ContextAssembler().assemble(messages, userPersonaCorpus = persona)
                val lastSpeaker = LastSpeakerDecisionUseCase().decide(messages)
                val baseDecision = TacticalDecisionEngine().decide(context)
                val expressDecision = baseDecision.copy(
                    decisionType = TacticalDecisionType.NORMAL_REPLY,
                    bestMove = "主动表达自己的底色，但只露出一点，不把聊天变成自我汇报。",
                    shouldUseUserStory = true,
                    fallbackMove = "如果对方接不住，立刻回到接住她和低压力聊天。"
                )
                val generatedRoutes = ReplyRouteGenerator().generate(context, expressDecision)
                val lightSnapshot = LightChatStateStore().buildStableSnapshot(
                    appPackage = capture.snapshot.appPackage,
                    windowTitle = capture.snapshot.windowTitle,
                    messages = messages,
                    capturedAt = capture.snapshot.capturedAt,
                    characterArcCards = persona.characterArcCards
                )
                val arcProgress = CharacterArcPlanner().plan(
                    recentMessages = lightSnapshot.recentEffectiveMessages,
                    lastUserMessage = lightSnapshot.lastUserMessage,
                    lastOtherMessage = lightSnapshot.lastOtherMessage,
                    currentTopics = emptyList(),
                    personaCorpus = persona,
                    characterArcCards = persona.characterArcCards
                )
                val plannedArcRoute = arcProgress.suggestedArcCard
                    ?.takeIf { arcProgress.currentExpressionWindow.exists }
                    ?.let { card ->
                        ReplyRoute(
                            id = "express-self-planned-arc-reveal",
                            name = "\u4eba\u7269\u5f27\u5149",
                            routeType = ReplyRouteType.ARC_REVEAL,
                            tag = "ARC_REVEAL",
                            message = card.safeRevealLine,
                            intensity = if (arcProgress.suggestedDepth == ArcRevealDepth.LOW) {
                                InfluenceIntensity.LOW
                            } else {
                                InfluenceIntensity.MEDIUM
                            },
                            riskLevel = RiskLevel.MEDIUM,
                            riskWarning = arcProgress.overdoRisk,
                            expectedEffect = card.hiddenDepth,
                            fallbackMove = card.overdoRisk,
                            recommended = false
                        )
                    }
                val routes = expressSelfRoutes(
                    routes = if (plannedArcRoute != null) listOf(plannedArcRoute) + generatedRoutes else generatedRoutes
                )
                val endedAt = System.currentTimeMillis()
                val result = CurrentScreenPipelineResult(
                    captureResult = capture,
                    context = context,
                    lastSpeakerDecision = lastSpeaker,
                    tacticalDecision = expressDecision,
                    routes = routes,
                    apiCalled = false,
                    sessionId = sessionId,
                    panelSessionId = sessionId,
                    panelContentFromCurrentSession = true,
                    staleRoutesClearedAtSessionStart = true,
                    staleRoutesReused = false,
                    routePanelShown = routes.isNotEmpty(),
                    sessionTerminalState = "EXPRESS_SELF_PANEL",
                    analysisStartedAt = startedTrace.startedAt,
                    analysisEndedAt = endedAt,
                    analysisDurationMs = endedAt - startedTrace.startedAt,
                    decisionTypeFamily = "EXPRESS_SELF",
                    expressSelfArcProgressState = arcProgress,
                    cloudTrace = CloudAnalysisTrace(
                        activeSessionId = sessionId,
                        cloudRequestSessionId = null,
                        cloudResponseSessionId = null,
                        preAnalysisSnapshotId = capture.snapshot.capturedAt.toString(),
                        chatPackage = capture.snapshot.appPackage.orEmpty(),
                        chatWindowHash = capture.snapshot.windowTitle.orEmpty(),
                        cloudAttempted = false,
                        cloudSkippedReason = "EXPRESS_SELF_LOCAL_FALLBACK",
                        decisionSource = "LOCAL_EXPRESS_SELF",
                        panelRenderedSessionId = sessionId
                    )
                )
                val successTrace = checkedTrace.copy(
                    endedAt = endedAt,
                    stage = NextSentenceStage.ROUTES_GENERATED,
                    activePackageAtCaptureStart = capture.currentRootPackageAtCapture,
                    activePackageAfterRootRetry = capture.snapshot.appPackage,
                    rootAvailableFirstTry = capture.rootAvailableFirstTry,
                    rootRetryCount = capture.rootRetryCount,
                    rootAvailableAfterRetry = capture.rootAvailableAfterRetry,
                    rootPackageName = capture.snapshot.appPackage,
                    rootWindowTitle = capture.snapshot.windowTitle,
                    parsedMessageCount = capture.messages.size,
                    effectiveMessageCount = capture.messages.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM },
                    lastEffectiveSpeaker = lastSpeaker.lastSpeaker,
                    decisionType = expressDecision.decisionType.name,
                    routeCount = routes.size,
                    apiCalled = false,
                    panelAttached = true,
                    panelRenderSuccess = true,
                    terminalState = "EXPRESS_SELF_PANEL",
                    panelShown = true,
                    userFacingMessage = "表达我"
                )
                writeLatestSessionTraceReports(successTrace)
                val flightRecord = NextSentenceFlightRecordFactory.fromSuccess(result, successTrace)
                mutableState.update {
                    if (it.lastNextSentenceTrace?.sessionId != sessionId ||
                        activeNextSentenceSessionId != sessionId
                    ) {
                        it
                    } else {
                        it.copy(
                            demoState = it.demoState.copy(messages = messages),
                            latestPipelineResult = result,
                            floatingPanelMode = FloatingPanelMode.EXPRESS_SELF,
                            panelVisible = true,
                            lastError = null,
                            nextSentenceUiState = NextSentenceUiState.RESULT,
                            lastNextSentenceTrace = successTrace,
                            latestFlightRecord = flightRecord,
                            recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                Log.i(LOG_TAG, "express_self_discarded sessionId=${startedTrace.sessionId} activeSessionId=$activeNextSentenceSessionId reason=STALE_SESSION")
            } catch (error: Throwable) {
                val message = error.message?.takeIf { it.isNotBlank() }
                    ?: userFacingMessageFor(NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT)
                val failedTrace = startedTrace.failed(NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT, NextSentenceStage.CAPTURE_STARTING)
                    .copy(
                        endedAt = System.currentTimeMillis(),
                        userFacingMessage = message,
                        terminalState = "CONTROLLED_FAIL",
                        panelShown = true
                    )
                val flightRecord = NextSentenceFlightRecordFactory.fromFailure(failedTrace)
                mutableState.update {
                    if (it.lastNextSentenceTrace?.sessionId != sessionId ||
                        activeNextSentenceSessionId != sessionId
                    ) {
                        it
                    } else {
                        it.copy(
                            latestPipelineResult = null,
                            floatingPanelMode = FloatingPanelMode.EXPRESS_SELF,
                            panelVisible = true,
                            lastError = message,
                            nextSentenceUiState = NextSentenceUiState.CONTROLLED_FAIL,
                            lastNextSentenceTrace = failedTrace,
                            latestFlightRecord = flightRecord,
                            recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                        )
                    }
                }
                writeLatestSessionTraceReports(failedTrace)
            }
        }
        return sessionId
    }

    private fun expressSelfRoutes(routes: List<ReplyRoute>): List<ReplyRoute> {
        val activeTypes = setOf(
            ReplyRouteType.ARC_REVEAL,
            ReplyRouteType.SELF_STORY,
            ReplyRouteType.CO_CREATION,
            ReplyRouteType.COOL_DOWN
        )
        val active = routes.filter { it.routeType in activeTypes }
        val withArc = if (active.none { it.routeType == ReplyRouteType.ARC_REVEAL }) {
            listOf(fallbackArcRevealRoute()) + active
        } else {
            active
        }
        return (withArc + routes)
            .distinctBy { it.id }
            .take(5)
            .mapIndexed { index, route -> route.copy(recommended = index == 0) }
    }

    private fun tryRenderDynamicPlaybookFromStableSnapshot(
        mode: DynamicPlaybookMode,
        sessionId: String,
        trace: NextSentenceSessionTrace,
        previousSessionId: String?
    ): Boolean {
        val stable = HuiyiAccessibilityService.instance?.lastStableChatSnapshot() ?: return false
        val messages = stable.normalizedMessages
            .filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        if (messages.isEmpty()) return false
        val persona = currentPersona()
        val request = DynamicPlaybookRequest(
            mode = mode,
            appPackage = stable.packageName,
            windowTitle = stable.windowTitle,
            messages = messages,
            personaCorpus = persona,
            capturedAt = stable.capturedAt,
            sessionId = sessionId,
            chatWindowHash = stable.nodesHash
        )
        val dynamicResult = when (mode) {
            DynamicPlaybookMode.NEXT_SENTENCE -> dynamicPlaybookEngine.nextSentence(request)
            DynamicPlaybookMode.EXPRESS_SELF -> dynamicPlaybookEngine.expressSelf(request)
        }
        if (mode == DynamicPlaybookMode.NEXT_SENTENCE &&
            dynamicResult.tacticalDecisionType != TacticalDecisionType.WAIT &&
            dynamicResult.routes.isEmpty()
        ) {
            return false
        }
        renderDynamicPlaybookResult(
            stable = stable,
            request = request,
            dynamicResult = dynamicResult,
            sessionId = sessionId,
            trace = trace,
            previousSessionId = previousSessionId
        )
        playbookRefreshScheduler.refreshInBackground(scope, request) {
            HuiyiAccessibilityService.instance?.lastStableChatSnapshot()?.stableChatKey()
        }
        return true
    }

    private fun renderDynamicPlaybookResult(
        stable: LastStableForeignWindowSnapshot,
        request: DynamicPlaybookRequest,
        dynamicResult: DynamicPlaybookResult,
        sessionId: String,
        trace: NextSentenceSessionTrace,
        previousSessionId: String?
    ) {
        val endedAt = System.currentTimeMillis()
        val mode = request.mode
        val persona = request.personaCorpus
        val capture = CurrentScreenCaptureResult(
            snapshot = stable.snapshot,
            messages = stable.normalizedMessages,
            sampleSource = if (stable.packageName == "com.huiyi.mockchat") {
                SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
            } else {
                SampleSource.REAL_DEVICE_ACCESSIBILITY
            },
            parserName = "DynamicPlaybookStableSnapshot",
            captureSource = NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
            usedFallbackSnapshot = true,
            lastStableSnapshotAgeMs = endedAt - stable.capturedAt,
            lastStableSnapshotPackage = stable.packageName,
            currentRootPackageAtCapture = stable.packageName,
            rootAvailableFirstTry = true,
            rootAvailableAfterRetry = true,
            rawNodeCount = stable.nodeCount,
            visibleTextCount = stable.visibleTextCount
        )
        val context = ContextAssembler().assemble(stable.normalizedMessages, userPersonaCorpus = persona)
        val decision = dynamicPlaybookDecision(dynamicResult)
        val terminalState = when {
            mode == DynamicPlaybookMode.EXPRESS_SELF -> "EXPRESS_SELF_PANEL"
            decision.decisionType == TacticalDecisionType.WAIT -> "WAIT_PANEL"
            dynamicResult.routes.isNotEmpty() -> "ROUTE_PANEL"
            else -> terminalStateFor(decision.decisionType)
        }
        val settings = mutableState.value.cloudSettings
        val result = CurrentScreenPipelineResult(
            captureResult = capture,
            context = context,
            lastSpeakerDecision = dynamicResult.lastSpeakerDecision,
            tacticalDecision = decision,
            routes = dynamicResult.routes,
            apiCalled = false,
            sessionId = sessionId,
            previousSessionId = previousSessionId,
            panelSessionId = sessionId,
            panelContentFromCurrentSession = true,
            staleRoutesClearedAtSessionStart = true,
            staleRoutesReused = false,
            waitPanelShown = decision.decisionType == TacticalDecisionType.WAIT,
            routePanelShown = dynamicResult.routes.isNotEmpty(),
            sessionTerminalState = terminalState,
            analysisStartedAt = trace.startedAt,
            analysisEndedAt = endedAt,
            analysisDurationMs = dynamicResult.latencyMs,
            loadingStillVisibleAfterTimeout = false,
            waitDecisionReached = decision.decisionType == TacticalDecisionType.WAIT,
            waitPanelRenderAttempted = decision.decisionType == TacticalDecisionType.WAIT,
            waitPanelRenderSuccess = decision.decisionType == TacticalDecisionType.WAIT,
            decisionTypeFamily = if (mode == DynamicPlaybookMode.EXPRESS_SELF) {
                "EXPRESS_SELF"
            } else {
                decisionTypeFamily(decision.decisionType)
            },
            lightListenBackfillCount = stable.normalizedMessages.size,
            lightListenUsed = true,
            expressSelfArcProgressState = if (mode == DynamicPlaybookMode.EXPRESS_SELF) dynamicResult.arcProgressState else null,
            cloudTrace = CloudAnalysisTrace(
                activeSessionId = sessionId,
                preAnalysisSnapshotId = stable.capturedAt.toString(),
                chatPackage = stable.packageName,
                chatWindowHash = stable.nodesHash,
                cloudEnabled = settings.cloudEnabled,
                endpointConfigured = settings.relayBaseUrlConfigured,
                cloudAttempted = false,
                cloudSkippedReason = when {
                    decision.decisionType == TacticalDecisionType.WAIT -> "LAST_SPEAKER_ME_WAIT"
                    dynamicResult.cloudRefreshRecommended -> "CLOUD_REFRESH_BACKGROUND_OPTIONAL"
                    else -> "PLAYBOOK_CACHE_OR_LOCAL_FALLBACK"
                },
                decisionSource = dynamicResult.decisionSource,
                providerType = settings.providerType,
                relayBaseUrlConfigured = settings.relayBaseUrlConfigured,
                relayApiKeyConfigured = settings.relayApiKeyConfigured,
                relayApiKeyStoredSecurely = settings.relayApiKeyStoredSecurely,
                cloudPrimaryModel = settings.model,
                cloudFinalModel = settings.model,
                panelRenderedSessionId = sessionId
            )
        )
        val successTrace = trace.copy(
            endedAt = endedAt,
            stage = NextSentenceStage.ROUTES_GENERATED,
            activePackageAtCaptureStart = stable.packageName,
            activePackageAfterRootRetry = stable.packageName,
            rootAvailableFirstTry = true,
            rootRetryCount = 0,
            rootAvailableAfterRetry = true,
            rootPackageName = stable.packageName,
            rootWindowTitle = stable.windowTitle,
            rootIsOwnOverlay = false,
            rootIsSystemUi = false,
            rootIsTargetChatApp = true,
            captureSource = NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
            primaryCapturePath = "LAST_STABLE_CHAT_SNAPSHOT",
            nodeTreeAttempted = false,
            nodeTreeSuccess = false,
            fallbackSnapshotAttempted = true,
            fallbackSnapshotSuccess = true,
            usedFallbackSnapshot = true,
            lastStableSnapshotAgeMs = endedAt - stable.capturedAt,
            lastStableSnapshotPackage = stable.packageName,
            rawNodeCount = stable.nodeCount,
            visibleTextCount = stable.visibleTextCount,
            parsedMessageCount = stable.normalizedMessages.size,
            effectiveMessageCount = stable.normalizedMessages.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM },
            lastEffectiveSpeaker = dynamicResult.lastSpeakerDecision.lastSpeaker,
            decisionType = decision.decisionType.name,
            routeCount = dynamicResult.routes.size,
            apiCalled = false,
            panelAttached = true,
            panelRenderSuccess = true,
            terminalState = terminalState,
            panelShown = true,
            userFacingMessage = if (decision.decisionType == TacticalDecisionType.WAIT) {
                "\u4f60\u5df2\u7ecf\u56de\u8fc7\u4e86\uff0c\u5148\u7b49\u5bf9\u65b9\u3002"
            } else {
                null
            }
        )
        writeLatestSessionTraceReports(successTrace)
        val flightRecord = NextSentenceFlightRecordFactory.fromSuccess(result, successTrace)
        mutableState.update {
            if (it.lastNextSentenceTrace?.sessionId != sessionId || activeNextSentenceSessionId != sessionId) {
                it
            } else {
                it.copy(
                    demoState = it.demoState.copy(messages = stable.normalizedMessages),
                    latestPipelineResult = result,
                    floatingPanelMode = if (mode == DynamicPlaybookMode.EXPRESS_SELF) {
                        FloatingPanelMode.EXPRESS_SELF
                    } else {
                        FloatingPanelMode.NEXT_SENTENCE
                    },
                    panelVisible = true,
                    lastError = null,
                    nextSentenceUiState = NextSentenceUiState.RESULT,
                    lastNextSentenceTrace = successTrace,
                    latestFlightRecord = flightRecord,
                    recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                )
            }
        }
    }

    private fun dynamicPlaybookDecision(result: DynamicPlaybookResult): TacticalDecision {
        val wait = result.tacticalDecisionType == TacticalDecisionType.WAIT
        return TacticalDecision(
            decisionType = result.tacticalDecisionType,
            situation = if (wait) {
                "last_speaker_me_wait"
            } else {
                "dynamic_playbook_${result.mode.name.lowercase()}"
            },
            coreInsight = if (wait) {
                "LAST_ME safety gate remains highest priority."
            } else {
                "Use cached relationship playbook first; refresh cloud in the background."
            },
            userLikelyMistake = if (wait) {
                "Adding another message before the other person replies."
            } else {
                "Waiting for cloud before showing usable wording."
            },
            bestMove = if (wait) {
                "\u4f60\u5df2\u7ecf\u56de\u8fc7\u4e86\uff0c\u5148\u7b49\u5bf9\u65b9\u3002"
            } else {
                result.routes.firstOrNull()?.message ?: "Use the local playbook fallback."
            },
            avoidMoves = if (wait) {
                listOf("do not call cloud", "do not show routes")
            } else {
                listOf("do not block on cloud", "do not mix active persona feedback into passive panel")
            },
            coCreationOpportunity = null,
            shouldUseUserStory = result.mode == DynamicPlaybookMode.EXPRESS_SELF,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(
                intensity = if (result.mode == DynamicPlaybookMode.EXPRESS_SELF) InfluenceIntensity.MEDIUM else InfluenceIntensity.LOW,
                riskLevel = result.playbook.risk,
                riskWarning = result.playbook.characterArcPlan.overdoRisk,
                fallbackMove = result.playbook.fallback
            ),
            fallbackMove = result.playbook.fallback
        )
    }

    private fun LastStableForeignWindowSnapshot.stableChatKey(): String =
        "${packageName}|${windowTitle?.trim()?.replace(Regex("\\s+"), " ")?.take(80).orEmpty().ifBlank { "default" }}"

    private fun fallbackArcRevealRoute(): ReplyRoute = ReplyRoute(
        id = "express-self-arc-reveal",
        name = "人物弧光",
        routeType = ReplyRouteType.ARC_REVEAL,
        tag = "ARC_REVEAL",
        message = "我可能不是特别会讲漂亮话，但认真起来会把事情一点点做到位。",
        intensity = InfluenceIntensity.MEDIUM,
        riskLevel = RiskLevel.MEDIUM,
        riskWarning = "不要讲成长篇自我证明，露出一点真实感就收住。",
        expectedEffect = "让对方看到真实、稳定、有反差的一面。",
        fallbackMove = "如果对方没有接住，就回到接住她的情绪，不继续展开自己。",
        recommended = false
    )

    private fun resumePendingCloudSessionIfAny(): String? {
        val currentState = mutableState.value
        val currentResult = currentState.latestPipelineResult
        if (!NextSentencePendingCloudSessionPolicy.shouldResumePendingSession(
                result = currentResult,
                activeSessionId = activeNextSentenceSessionId
            )
        ) {
            return null
        }
        val sessionId = currentResult?.sessionId ?: return null
        val updatedTrace = currentState.lastNextSentenceTrace
            ?.takeIf { it.sessionId == sessionId }
            ?.copy(
                panelShown = true,
                panelAttached = true,
                panelRenderSuccess = true,
                userFacingMessage = "云端还在分析，结果回来会自动刷新。"
            )
        mutableState.update { latest ->
            if (!NextSentencePendingCloudSessionPolicy.shouldResumePendingSession(
                    result = latest.latestPipelineResult,
                    activeSessionId = activeNextSentenceSessionId
                )
            ) {
                latest
            } else {
                latest.copy(
                    floatingPanelMode = FloatingPanelMode.NEXT_SENTENCE,
                    panelVisible = true,
                    lastError = null,
                    nextSentenceUiState = NextSentenceUiState.LOADING_CLOUD,
                    lastNextSentenceTrace = updatedTrace ?: latest.lastNextSentenceTrace
                )
            }
        }
        updatedTrace?.let(::writeLatestSessionTraceReports)
        Log.i(LOG_TAG, "pending_cloud_session_resumed sessionId=$sessionId")
        return sessionId
    }

    private suspend fun waitForAccessibilityConnection(trace: NextSentenceSessionTrace): NextSentenceSessionTrace {
        var runtime = AccessibilityRuntimeReader.read(appContext)
        if (!runtime.systemAccessibilityEnabled) {
            val checkedTrace = trace.copy(
                stage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED,
                systemAccessibilityEnabled = false,
                serviceConnected = false,
                rootAvailableAtClick = false,
                accessibilityRuntimeCategory = runtime.category.name
            )
            throw com.huiyi.v4.domain.pipeline.NextSentenceException(
                code = NextSentenceErrorCode.ACCESSIBILITY_SYSTEM_DISABLED,
                failedStage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED,
                trace = checkedTrace.failed(
                    NextSentenceErrorCode.ACCESSIBILITY_SYSTEM_DISABLED,
                    NextSentenceStage.ACCESSIBILITY_STATE_CHECKED
                )
            )
        }

        var waitedMs = 0L
        var attemptedReconnect = false
        if (!runtime.serviceConnected) {
            attemptedReconnect = true
            for (delayMs in ACCESSIBILITY_SERVICE_RECONNECT_DELAYS_MS) {
                delay(delayMs)
                waitedMs += delayMs
                runtime = AccessibilityRuntimeReader.read(appContext)
                if (runtime.serviceConnected) break
            }
        }

        val checkedTrace = trace.copy(
            stage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED,
            systemAccessibilityEnabled = runtime.systemAccessibilityEnabled,
            serviceConnected = runtime.serviceConnected,
            rootAvailableAtClick = runtime.rootAvailable,
            activePackageAtCaptureStart = runtime.currentPackage,
            activeWindowTitleAtClick = trace.activeWindowTitleAtClick ?: runtime.currentWindowTitle,
            serviceReconnectAttempted = attemptedReconnect,
            serviceReconnectWaitMs = waitedMs,
            serviceReconnectSucceeded = attemptedReconnect && runtime.serviceConnected,
            accessibilityRuntimeCategory = runtime.category.name
        )

        if (!runtime.serviceConnected) {
            throw com.huiyi.v4.domain.pipeline.NextSentenceException(
                code = NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED,
                failedStage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED,
                trace = checkedTrace.failed(
                    NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED,
                    NextSentenceStage.ACCESSIBILITY_STATE_CHECKED
                )
            )
        }

        return checkedTrace
    }

    private fun isActiveNextSentenceSession(sessionId: String): Boolean =
        activeNextSentenceSessionId == sessionId &&
            mutableState.value.lastNextSentenceTrace?.sessionId == sessionId

    fun runLastMeAcceptanceTestAndExport() {
        setRealDeviceScenario(RealDeviceScenario.LAST_ME)
        runNextSentence()
        scope.launch {
            waitForSessionTerminal()
            exportLastMeAcceptanceBundle()
        }
    }

    fun runLastOtherAcceptanceTestAndExport() {
        setRealDeviceScenario(RealDeviceScenario.LAST_OTHER)
        runNextSentence()
        scope.launch {
            waitForSessionTerminal()
            exportLastOtherAcceptanceBundle()
        }
    }

    private suspend fun waitForSessionTerminal() {
        repeat(18) {
            delay(500L)
            val state = mutableState.value
            if (state.latestPipelineResult != null || state.lastError != null) return
        }
    }

    private fun launchSessionTerminalWatchdogV2(
        trace: NextSentenceSessionTrace,
        scenario: RealDeviceScenario
    ) {
        sessionWatchdogJob?.cancel()
        sessionWatchdogJob = scope.launch {
            delay(95_000L)
            val state = mutableState.value
            if (state.lastNextSentenceTrace?.sessionId != trace.sessionId ||
                activeNextSentenceSessionId != trace.sessionId
            ) return@launch
            if (state.latestPipelineResult != null || state.lastError != null) return@launch
            val code = if (scenario == RealDeviceScenario.LAST_ME) {
                NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK
            } else {
                NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT
            }
            val timeoutTrace = trace.failed(code, state.lastNextSentenceTrace?.stage ?: trace.stage)
                .copy(
                    endedAt = System.currentTimeMillis(),
                    stage = NextSentenceStage.TIMEOUT,
                    terminalState = "TIMEOUT_PANEL",
                    panelShown = true,
                    panelAttached = true,
                    panelRenderSuccess = true,
                    userFacingMessage = "这次没有跑完，已保存诊断。"
                )
            if (scenario == RealDeviceScenario.LAST_ME) {
                writeScenarioAcceptanceBundle(
                    scenario = RealDeviceScenario.LAST_ME,
                    testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
                    folderName = "last-me",
                    filePrefix = "last-me-real-device-report",
                    result = null,
                    trace = timeoutTrace
                )
            }
            val flightRecord = NextSentenceFlightRecordFactory.fromFailure(timeoutTrace)
            mutableState.update {
                if (it.lastNextSentenceTrace?.sessionId != trace.sessionId ||
                    activeNextSentenceSessionId != trace.sessionId
                ) {
                    it
                } else {
                    it.copy(
                        lastNextSentenceTrace = timeoutTrace,
                        lastError = timeoutTrace.userFacingMessage,
                        panelVisible = true,
                        nextSentenceUiState = NextSentenceUiState.TIMEOUT,
                        latestPipelineResult = null,
                        latestFlightRecord = flightRecord,
                        recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                    )
                }
            }
            writeLatestSessionTraceReports(timeoutTrace)
            if (activeNextSentenceSessionId == trace.sessionId) {
                activeNextSentenceSessionId = null
            }
        }
    }

    private fun launchSessionTerminalWatchdog(
        trace: NextSentenceSessionTrace,
        scenario: RealDeviceScenario
    ) {
        sessionWatchdogJob?.cancel()
        sessionWatchdogJob = scope.launch {
            delay(8000L)
            val state = mutableState.value
            if (state.lastNextSentenceTrace?.sessionId != trace.sessionId ||
                activeNextSentenceSessionId != trace.sessionId
            ) return@launch
            if (state.latestPipelineResult != null || state.lastError != null) return@launch
            val code = if (scenario == RealDeviceScenario.LAST_ME) {
                NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK
            } else {
                NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT
            }
            val timeoutTrace = trace.failed(code, state.lastNextSentenceTrace?.stage ?: trace.stage)
                .copy(
                    endedAt = System.currentTimeMillis(),
                    userFacingMessage = "没读到当前聊天，请回到聊起聊天窗口再点一次“下一句”。"
                )
            if (scenario == RealDeviceScenario.LAST_ME) {
                writeScenarioAcceptanceBundle(
                    scenario = RealDeviceScenario.LAST_ME,
                    testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
                    folderName = "last-me",
                    filePrefix = "last-me-real-device-report",
                    result = null,
                    trace = timeoutTrace
                )
            }
            val flightRecord = NextSentenceFlightRecordFactory.fromFailure(timeoutTrace)
            if (activeNextSentenceSessionId == trace.sessionId) {
                activeNextSentenceSessionId = null
            }
            mutableState.update {
                if (it.lastNextSentenceTrace?.sessionId != trace.sessionId ||
                    activeNextSentenceSessionId != trace.sessionId
                ) it else it.copy(
                    lastNextSentenceTrace = timeoutTrace,
                    lastError = timeoutTrace.userFacingMessage,
                    panelVisible = true,
                    latestPipelineResult = null,
                    latestFlightRecord = flightRecord,
                    recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
                )
            }
        }
    }

    private fun terminalStateFor(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT_PANEL"
        TacticalDecisionType.CHAT_WINDOW_NOT_FOUND,
        TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED -> "CONTROLLED_FAIL"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "CONTEXT_REQUIRED_PANEL"
        else -> "ROUTE_PANEL"
    }

    private fun decisionTypeFamily(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT"
        TacticalDecisionType.CHAT_WINDOW_NOT_FOUND,
        TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED -> "CONTROLLED_FAIL"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "CONTEXT_REQUIRED"
        TacticalDecisionType.NORMAL_REPLY,
        TacticalDecisionType.EMPATHY_FIRST -> "REPLY_ROUTES"
        else -> "REPLY_ROUTES"
    }

    fun showOverlayError(error: Throwable) {
        val trace = mutableState.value.lastNextSentenceTrace
            ?: NextSentenceSessionTrace(UUID.randomUUID().toString(), System.currentTimeMillis())
        val next = error.toNextSentenceException(trace, NextSentenceStage.PANEL_RENDERING)
        mutableState.update {
            it.copy(
                panelVisible = true,
                latestPipelineResult = null,
                lastError = next.trace.userFacingMessage ?: next.message,
                nextSentenceUiState = NextSentenceUiState.CONTROLLED_FAIL,
                lastClickPipelineException = "${error::class.java.name}: ${error.message}",
                lastNextSentenceTrace = next.trace
            )
        }
        writeLatestSessionTraceReports(next.trace.copy(panelShown = true, terminalState = "CONTROLLED_FAIL_PANEL"))
    }

    private fun handleNextSentenceFailure(error: Throwable, baseTrace: NextSentenceSessionTrace) {
        if (!isActiveNextSentenceSession(baseTrace.sessionId)) {
            Log.i(
                LOG_TAG,
                "next_sentence_discarded sessionId=${baseTrace.sessionId} " +
                    "activeSessionId=$activeNextSentenceSessionId reason=STALE_SESSION"
            )
            return
        }
        val next = error.toNextSentenceException(baseTrace, NextSentenceStage.NODE_TREE_CAPTURE_STARTED)
        Log.w(
            LOG_TAG,
            "next_sentence_failure code=${next.code} stage=${next.failedStage} " +
                "rootPackage=${next.trace.rootPackageName} activePackage=${next.trace.activePackageAtCaptureStart} " +
                "lastStablePackage=${next.trace.lastStableSnapshotPackage} " +
                "lastStableAgeMs=${next.trace.lastStableSnapshotAgeMs}"
        )
        OverlayStateStore.recordPipelineException(error)
        val overlay = OverlayStateStore.state.value
        val runtime = AccessibilityRuntimeReader.read(appContext)
        val finalTrace = next.trace.copy(
            endedAt = System.currentTimeMillis(),
            terminalState = "CONTROLLED_FAIL_PANEL",
            panelShown = true,
            panelAttached = true,
            panelRenderSuccess = true,
            bubbleVisibleAfterClick = overlay.bubbleVisible,
            bubbleAttachedAfterClick = overlay.bubbleVisible,
            bubbleVisibleAfterFailure = overlay.bubbleVisible,
            systemAccessibilityEnabled = runtime.systemAccessibilityEnabled,
            serviceConnected = runtime.serviceConnected,
            permissionMissingMessageShown = next.code == NextSentenceErrorCode.ACCESSIBILITY_SYSTEM_DISABLED,
            activePackageBeforeClick = baseTrace.activePackageBeforeClick ?: runtime.currentPackage,
            rootPackageBeforeFailureUi = runtime.currentPackage,
            pipelineExceptionClass = next.trace.pipelineExceptionClass ?: error::class.java.name,
            pipelineExceptionMessageRedacted = next.trace.pipelineExceptionMessageRedacted ?: error.message?.redactPrivateText(),
            userFacingMessage = next.trace.userFacingMessage ?: userFacingMessageFor(next.code)
        )
        val paths = writeLatestFailureReports(finalTrace)
        writeLatestSessionTraceReports(finalTrace)
        val flightRecord = NextSentenceFlightRecordFactory.fromFailure(finalTrace)
        mutableState.update {
            if (it.lastNextSentenceTrace?.sessionId != baseTrace.sessionId ||
                activeNextSentenceSessionId != baseTrace.sessionId
            ) it else it.copy(
                panelVisible = true,
                latestPipelineResult = null,
                lastError = finalTrace.userFacingMessage,
                nextSentenceUiState = NextSentenceUiState.CONTROLLED_FAIL,
                lastClickPipelineException = "${error::class.java.name}: ${error.message}",
                lastNextSentenceTrace = finalTrace,
                latestNextSentenceFailureMarkdownPath = paths.first.absolutePath,
                latestNextSentenceFailureJsonPath = paths.second.absolutePath,
                latestFlightRecord = flightRecord,
                recentFlightRecords = (it.recentFlightRecords + flightRecord).takeLast(10)
            )
        }
    }

    private fun writeLatestFailureReports(trace: NextSentenceSessionTrace): Pair<File, File> {
        val generator = NextSentenceFailureReportGenerator()
        val dir = File(appContext.filesDir, "debug")
        dir.mkdirs()
        val markdown = File(dir, "latest-next-sentence-failure.md")
        val json = File(dir, "latest-next-sentence-failure.json")
        val markdownText = generator.buildMarkdown(trace)
        val jsonText = generator.buildJson(trace)
        markdown.writeText(markdownText, Charsets.UTF_8)
        json.writeText(jsonText, Charsets.UTF_8)
        val exporter = PublicDownloadExporter(appContext)
        exporter.exportText("latest-next-sentence-failure.md", markdownText, relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("latest-next-sentence-failure.md", markdownText, subDirectory = "debug/review") }
        exporter.exportText("latest-next-sentence-failure.json", jsonText, "application/json", relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("latest-next-sentence-failure.json", jsonText, subDirectory = "debug/review") }
        return markdown to json
    }

    private fun writeLatestSessionTraceReports(trace: NextSentenceSessionTrace) {
        val dir = File(appContext.filesDir, "debug/next_sentence").apply { mkdirs() }
        File(dir, "latest-next-sentence-session-for-gpt.md")
            .writeText(buildLatestSessionTraceMarkdown(trace), Charsets.UTF_8)
        File(dir, "latest-next-sentence-session.json")
            .writeText(buildLatestSessionTraceJson(trace), Charsets.UTF_8)
        File(dir, "no-reaction-diagnostic-report-for-gpt.md")
            .writeText(buildNoReactionDiagnosticMarkdown(trace), Charsets.UTF_8)
        File(dir, "no-reaction-diagnostic-report.json")
            .writeText(buildNoReactionDiagnosticJson(trace), Charsets.UTF_8)
    }

    private fun buildLatestSessionTraceMarkdown(trace: NextSentenceSessionTrace): String = buildString {
        appendLine("# Latest Next Sentence Session")
        appendLine()
        appendLine("- versionName: ${BuildConfig.VERSION_NAME}")
        appendLine("- versionCode: ${BuildConfig.VERSION_CODE}")
        appendLine("- sessionId: ${trace.sessionId}")
        appendLine("- stage: ${trace.stage}")
        appendLine("- terminalState: ${trace.terminalState ?: "none"}")
        appendLine("- clickReceivedAt: ${trace.clickReceivedAt ?: "none"}")
        appendLine("- clickAckShownAt: ${trace.clickAckShownAt ?: "none"}")
        appendLine("- clickAckLatencyMs: ${trace.clickAckLatencyMs ?: "none"}")
        appendLine("- clickAckVisible: ${trace.clickAckVisible}")
        appendLine("- runNextSentenceEntered: ${trace.runNextSentenceEntered}")
        appendLine("- sessionCreated: ${trace.sessionCreated}")
        appendLine("- activePackageAtClick: ${trace.activePackageBeforeClick ?: "unknown"}")
        appendLine("- activeWindowTitleAtClick: ${trace.activeWindowTitleAtClick ?: "unknown"}")
        appendLine("- rootAvailableAtClick: ${trace.rootAvailableAtClick}")
        appendLine("- systemAccessibilityEnabled: ${trace.systemAccessibilityEnabled}")
        appendLine("- serviceConnected: ${trace.serviceConnected}")
        appendLine("- accessibilityRuntimeCategory: ${trace.accessibilityRuntimeCategory ?: "unknown"}")
        appendLine("- serviceReconnectAttempted: ${trace.serviceReconnectAttempted}")
        appendLine("- serviceReconnectWaitMs: ${trace.serviceReconnectWaitMs}")
        appendLine("- serviceReconnectSucceeded: ${trace.serviceReconnectSucceeded}")
        appendLine("- errorCode: ${trace.errorCode ?: NextSentenceErrorCode.NONE}")
        appendLine("- cloudAttempted: ${trace.apiCalled}")
        appendLine("- panelShown: ${trace.panelShown || trace.panelAttached}")
        appendLine("- userFacingMessage: ${trace.userFacingMessage ?: "none"}")
    }

    private fun buildLatestSessionTraceJson(trace: NextSentenceSessionTrace): String = linkedMapOf<String, Any?>(
        "versionName" to BuildConfig.VERSION_NAME,
        "versionCode" to BuildConfig.VERSION_CODE,
        "sessionId" to trace.sessionId,
        "clickReceivedAt" to trace.clickReceivedAt,
        "clickAckShownAt" to trace.clickAckShownAt,
        "clickAckLatencyMs" to trace.clickAckLatencyMs,
        "clickAckVisible" to trace.clickAckVisible,
        "runNextSentenceEntered" to trace.runNextSentenceEntered,
        "sessionCreated" to trace.sessionCreated,
        "stage" to trace.stage.name,
        "terminalState" to trace.terminalState,
        "errorCode" to trace.errorCode?.name,
        "cloudAttempted" to trace.apiCalled,
        "panelShown" to (trace.panelShown || trace.panelAttached),
        "activePackageAtClick" to trace.activePackageBeforeClick,
        "activeWindowTitleAtClick" to trace.activeWindowTitleAtClick,
        "rootAvailableAtClick" to trace.rootAvailableAtClick,
        "systemAccessibilityEnabled" to trace.systemAccessibilityEnabled,
        "serviceConnected" to trace.serviceConnected,
        "accessibilityRuntimeCategory" to trace.accessibilityRuntimeCategory,
        "serviceReconnectAttempted" to trace.serviceReconnectAttempted,
        "serviceReconnectWaitMs" to trace.serviceReconnectWaitMs,
        "serviceReconnectSucceeded" to trace.serviceReconnectSucceeded
    ).toSimpleJson()

    private fun buildNoReactionDiagnosticMarkdown(trace: NextSentenceSessionTrace): String = buildString {
        appendLine("# No Reaction Diagnostic Report")
        appendLine()
        appendLine("- versionName: ${BuildConfig.VERSION_NAME}")
        appendLine("- versionCode: ${BuildConfig.VERSION_CODE}")
        appendLine("- scenarioName: real_device_next_sentence_no_reaction")
        appendLine("- clickReceived: ${trace.clickReceivedAt != null}")
        appendLine("- clickAckVisible: ${trace.clickAckVisible}")
        appendLine("- runNextSentenceEntered: ${trace.runNextSentenceEntered}")
        appendLine("- sessionCreated: ${trace.sessionCreated}")
        appendLine("- latestSessionId: ${trace.sessionId}")
        appendLine("- stage: ${trace.stage}")
        appendLine("- terminalState: ${trace.terminalState ?: "none"}")
        appendLine("- panelVisibleBeforeClick: ${trace.panelVisibleBeforeClick}")
        appendLine("- panelVisibleAfterClick: ${trace.panelVisibleAfterClick}")
        appendLine("- bubbleVisibleAfterClick: ${trace.bubbleVisibleAfterClick}")
        appendLine("- activePackageAtClick: ${trace.activePackageBeforeClick ?: "unknown"}")
        appendLine("- windowTitleAtClickRedacted: ${trace.activeWindowTitleAtClick?.redactPrivateText() ?: "unknown"}")
        appendLine("- accessibilityConnected: ${trace.serviceConnected}")
        appendLine("- rootAvailableAtClick: ${trace.rootAvailableAtClick}")
        appendLine("- accessibilityRuntimeCategory: ${trace.accessibilityRuntimeCategory ?: "unknown"}")
        appendLine("- serviceReconnectAttempted: ${trace.serviceReconnectAttempted}")
        appendLine("- serviceReconnectWaitMs: ${trace.serviceReconnectWaitMs}")
        appendLine("- serviceReconnectSucceeded: ${trace.serviceReconnectSucceeded}")
        appendLine("- exceptionClass: ${trace.exceptionClass ?: trace.pipelineExceptionClass ?: "none"}")
        appendLine("- exceptionMessageRedacted: ${trace.exceptionMessageRedacted ?: trace.pipelineExceptionMessageRedacted ?: "none"}")
    }

    private fun buildNoReactionDiagnosticJson(trace: NextSentenceSessionTrace): String = linkedMapOf<String, Any?>(
        "versionName" to BuildConfig.VERSION_NAME,
        "versionCode" to BuildConfig.VERSION_CODE,
        "scenarioName" to "real_device_next_sentence_no_reaction",
        "clickReceived" to (trace.clickReceivedAt != null),
        "clickAckVisible" to trace.clickAckVisible,
        "runNextSentenceEntered" to trace.runNextSentenceEntered,
        "sessionCreated" to trace.sessionCreated,
        "latestSessionId" to trace.sessionId,
        "stage" to trace.stage.name,
        "terminalState" to trace.terminalState,
        "panelVisibleBeforeClick" to trace.panelVisibleBeforeClick,
        "panelVisibleAfterClick" to trace.panelVisibleAfterClick,
        "bubbleVisibleAfterClick" to trace.bubbleVisibleAfterClick,
        "activePackageAtClick" to trace.activePackageBeforeClick.orEmpty(),
        "windowTitleAtClickRedacted" to trace.activeWindowTitleAtClick.orEmpty().redactPrivateText(),
        "accessibilityConnected" to trace.serviceConnected,
        "rootAvailableAtClick" to trace.rootAvailableAtClick,
        "accessibilityRuntimeCategory" to trace.accessibilityRuntimeCategory,
        "serviceReconnectAttempted" to trace.serviceReconnectAttempted,
        "serviceReconnectWaitMs" to trace.serviceReconnectWaitMs,
        "serviceReconnectSucceeded" to trace.serviceReconnectSucceeded,
        "exceptionClass" to (trace.exceptionClass ?: trace.pipelineExceptionClass),
        "exceptionMessageRedacted" to (trace.exceptionMessageRedacted ?: trace.pipelineExceptionMessageRedacted)
    ).toSimpleJson()

    private fun Map<String, Any?>.toSimpleJson(): String =
        entries.joinToString(prefix = "{\n", postfix = "\n}", separator = ",\n") { (key, value) ->
            "  \"${jsonEscape(key)}\": ${jsonValue(value)}"
        }

    private fun jsonValue(value: Any?): String = when (value) {
        null -> "null"
        is Boolean -> value.toString()
        is Number -> value.toString()
        else -> "\"${jsonEscape(value.toString())}\""
    }

    private fun jsonEscape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    fun applyVoiceSummary(summary: String) {
        val old = mutableState.value
        val updatedState = old.demoState.withVoiceSummary(summary)
        val context = ContextAssembler().assemble(updatedState.messages, userPersonaCorpus = currentPersona())
        val decision = TacticalDecisionEngine().decide(context)
        val routes = if (decision.decisionType.name == "WAIT") emptyList() else ReplyRouteGenerator().generate(context, decision)
        val result = CurrentScreenPipelineResult(
            captureResult = old.latestPipelineResult?.captureResult,
            context = context,
            lastSpeakerDecision = old.latestPipelineResult?.lastSpeakerDecision
                ?: com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase().decide(updatedState.messages),
            tacticalDecision = decision,
            routes = routes,
            apiCalled = false
        )
        mutableState.update {
            it.copy(
                demoState = updatedState,
                latestPipelineResult = result,
                panelVisible = true,
                lastError = null
            )
        }
    }

    fun createCopiedAttempt(route: ReplyRoute) {
        val sceneId = mutableState.value.latestPipelineResult?.context?.id ?: "local-scene"
        val attempt = ReplyAttemptFactory().copied(
            route = route,
            sceneId = sceneId,
            contactId = mutableState.value.latestPipelineResult?.context?.contactId
        )
        scope.launch {
            val error = persistence.saveReplyAttempt(attempt).exceptionOrNull()?.message
            mutableState.update { it.copy(lastError = error) }
        }
    }

    fun soloCharacterArcReviewItems(limit: Int = 20): List<CharacterArcReviewItem> {
        return CharacterArcActiveSampler().selectForInitialReview(reviewLimit = limit.coerceIn(1, 20))
    }

    fun recordCharacterArcRouteFeedback(route: ReplyRoute, feedback: CharacterArcUserFeedback) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                characterArcPreferenceStore.recordFeedback(
                    CharacterArcPreferenceRecord.fromRoute(route, feedback)
                )
            }
            refreshCharacterArcPreferenceProfile(result.exceptionOrNull()?.message)
        }
    }

    fun recordSoloCharacterArcFeedback(
        item: CharacterArcReviewItem,
        candidate: CharacterArcCandidate?,
        feedback: CharacterArcUserFeedback,
        note: String?
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                characterArcPreferenceStore.recordFeedback(
                    CharacterArcPreferenceRecord.fromSoloReview(item, candidate, feedback, note)
                )
            }
            refreshCharacterArcPreferenceProfile(result.exceptionOrNull()?.message)
        }
    }

    private suspend fun refreshCharacterArcPreferenceProfile(error: String?) {
        val profile = withContext(Dispatchers.IO) {
            characterArcPreferenceStore.buildProfile()
        }
        mutableState.update {
            it.copy(
                characterArcPreferenceProfile = profile,
                lastError = error
            )
        }
    }

    fun applyLastMessageCorrection(correctedSpeaker: Speaker?) {
        val state = mutableState.value
        val result = state.latestPipelineResult ?: return
        val capture = result.captureResult ?: return
        val lastEffectiveId = result.lastSpeakerDecision.lastEffectiveMessage?.id ?: return
        val updatedMessages = capture.messages.map { message ->
            if (message.id != lastEffectiveId) {
                message
            } else if (correctedSpeaker == null) {
                message.copy(
                    speaker = Speaker.SYSTEM,
                    isEffectiveChatMessage = false,
                    metadataType = com.huiyi.v4.domain.model.MetadataType.SYSTEM_NOTICE,
                    speakerReason = "debug_correction_not_chat_message",
                    finalDecisionSource = "debug_correction_not_chat_message"
                )
            } else {
                message.copy(
                    speaker = correctedSpeaker,
                    isEffectiveChatMessage = true,
                    metadataType = com.huiyi.v4.domain.model.MetadataType.NONE,
                    speakerReason = "debug_correction_${correctedSpeaker.name.lowercase()}",
                    inferredSide = if (correctedSpeaker == Speaker.ME) "right" else "left",
                    finalDecisionSource = "debug_correction"
                )
            }
        }
        val updatedCapture = capture.copy(messages = updatedMessages)
        val context = ContextAssembler().assemble(updatedMessages, userPersonaCorpus = currentPersona())
        val lastSpeaker = LastSpeakerDecisionUseCase().decide(updatedMessages)
        val decision = TacticalDecisionEngine().decide(context)
        val routes = if (decision.decisionType in setOf(TacticalDecisionType.WAIT, TacticalDecisionType.CONTEXT_REQUIRED) || lastSpeaker.unknownSpeaker) {
            emptyList()
        } else {
            ReplyRouteGenerator().generate(context, decision)
        }
        mutableState.update {
            it.copy(
                latestPipelineResult = result.copy(
                    captureResult = updatedCapture,
                    context = context,
                    lastSpeakerDecision = lastSpeaker,
                    tacticalDecision = decision,
                    routes = routes,
                    userCorrectionProvided = true,
                    correctedLastSpeaker = correctedSpeaker,
                    correctedMessageId = lastEffectiveId
                ),
                demoState = it.demoState.copy(messages = updatedMessages),
                lastDebugCorrection = correctedSpeaker?.name ?: "NOT_CHAT_MESSAGE"
            )
        }
    }

    fun exportParserReport(): File? {
        val capture = mutableState.value.latestPipelineResult?.captureResult ?: return null
        val text = ParserReportGenerator().build(capture)
        val exported = PublicDownloadExporter(appContext)
            .exportText("current-screen-parser-report-for-gpt.md", text)
            .getOrElse { PublicDownloadExporter(appContext).fallbackToPrivate("current-screen-parser-report-for-gpt.md", text) }
        mutableState.update {
            it.copy(
                lastDebugExportPath = exported.privateFallbackFile?.absolutePath,
                lastPublicExportPath = exported.displayPath
            )
        }
        return exported.privateFallbackFile
    }

    fun exportRealDeviceEvidencePack(): Pair<File, File>? {
        val result = mutableState.value.latestPipelineResult ?: return null
        val generator = EvidencePackReportGenerator()
        val now = System.currentTimeMillis()
        val scenario = mutableState.value.selectedRealDeviceScenario
        val resultWithVisualDebug = result
        val markdown = generator.buildMarkdown(resultWithVisualDebug, HuiyiAccessibilityService.state.value, now, scenario)
        val json = generator.buildJson(resultWithVisualDebug, HuiyiAccessibilityService.state.value, now, scenario)
        val exporter = PublicDownloadExporter(appContext)
        val markdownExport = exporter.exportText("real-device-current-screen-report-for-gpt.md", markdown)
            .getOrElse { exporter.fallbackToPrivate("real-device-current-screen-report-for-gpt.md", markdown) }
        val jsonExport = exporter.exportText("real-device-current-screen-report.json", json, "application/json")
            .getOrElse { exporter.fallbackToPrivate("real-device-current-screen-report.json", json) }
        exportVisualDebugOverlay(exporter, resultWithVisualDebug)
        val files = EvidencePackReportGenerator()
            .writeTo(File(appContext.filesDir, "debug"), resultWithVisualDebug, HuiyiAccessibilityService.state.value, scenario)
            .getOrNull()
            ?: EvidencePackFiles(
                markdown = markdownExport.privateFallbackFile ?: File(appContext.filesDir, "debug/real-device-current-screen-report-for-gpt.md"),
                json = jsonExport.privateFallbackFile ?: File(appContext.filesDir, "debug/real-device-current-screen-report.json")
            )
        mutableState.update {
            it.copy(
                lastDebugExportPath = files.markdown.absolutePath,
                lastEvidenceJsonPath = files.json.absolutePath,
                lastPublicExportPath = markdownExport.displayPath + " / " + jsonExport.displayPath
            )
        }
        return files.markdown to files.json
    }

    fun exportRealDeviceReviewBundle(): List<String> {
        val content = RealDeviceReviewBundleGenerator().build(
            latestResult = mutableState.value.latestPipelineResult,
            accessibilityState = HuiyiAccessibilityService.state.value,
            generatedAt = System.currentTimeMillis(),
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            ownAppPackage = BuildConfig.APPLICATION_ID,
            scenario = mutableState.value.selectedRealDeviceScenario
        )
        val exporter = PublicDownloadExporter(appContext)
        val files = listOf(
            "huiyi-v4-review-for-gpt.md" to content.reviewMarkdown,
            "real-device-smoke-report-for-gpt.md" to content.smokeMarkdown,
            "real-device-current-screen-report-for-gpt.md" to content.currentScreenMarkdown,
            "real-device-current-screen-report.json" to content.currentScreenJson
        )
        val displayPaths = files.map { (fileName, text) ->
            val mimeType = if (fileName.endsWith(".json")) "application/json" else "text/markdown"
            exporter.exportText(fileName, text, mimeType, relativePath = "Huiyi/review")
                .getOrElse { exporter.fallbackToPrivate(fileName, text, subDirectory = "debug/review") }
                .displayPath
        }
        val overlayPath = mutableState.value.latestPipelineResult?.visualDebugResult?.let { visualDebug ->
            visualDebug.overlayImagePath?.let { exportVisualDebugOverlay(exporter, mutableState.value.latestPipelineResult)?.displayPath }
        }
        mutableState.update {
            it.copy(
                lastDebugExportPath = "realDeviceSmoke=${content.realDeviceSmokeResult}; overall=${content.overallResult}",
                lastEvidenceJsonPath = displayPaths.firstOrNull { path -> path.endsWith("real-device-current-screen-report.json") },
                lastPublicExportPath = (displayPaths + listOfNotNull(overlayPath)).joinToString(" / "),
                lastError = if (content.realDeviceSmokeResult == "NOT_TESTED") content.failReason else null
            )
        }
        return displayPaths
    }

    fun exportLastMeAcceptanceBundle(): List<String> = exportScenarioAcceptanceBundle(
        scenario = RealDeviceScenario.LAST_ME,
        testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
        folderName = "last-me",
        filePrefix = "last-me-real-device-report"
    )

    fun exportLastOtherAcceptanceBundle(): List<String> = exportScenarioAcceptanceBundle(
        scenario = RealDeviceScenario.LAST_OTHER,
        testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_OTHER,
        folderName = "last-other",
        filePrefix = "last-other-real-device-report"
    )

    private fun exportScenarioAcceptanceBundle(
        scenario: RealDeviceScenario,
        testIntent: RealDeviceTestIntent,
        folderName: String,
        filePrefix: String
    ): List<String> {
        return writeScenarioAcceptanceBundle(
            scenario = scenario,
            testIntent = testIntent,
            folderName = folderName,
            filePrefix = filePrefix,
            result = mutableState.value.latestPipelineResult,
            trace = mutableState.value.lastNextSentenceTrace
        )
    }

    private fun writeScenarioAcceptanceBundle(
        scenario: RealDeviceScenario,
        testIntent: RealDeviceTestIntent,
        folderName: String,
        filePrefix: String,
        result: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?
    ): List<String> {
        val report = LastSpeakerAcceptanceReportGenerator().build(
            result = result,
            trace = trace,
            accessibilityState = AccessibilityRuntimeReader.read(appContext),
            scenario = scenario,
            testIntent = testIntent,
            generatedAt = System.currentTimeMillis(),
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE
        )
        val markdown = report.markdown
        val json = report.json
        val privateDir = File(appContext.filesDir, "debug/review/$folderName").apply { mkdirs() }
        val mdFile = File(privateDir, "$filePrefix-for-gpt.md")
        val jsonFile = File(privateDir, "$filePrefix.json")
        mdFile.writeText(markdown, Charsets.UTF_8)
        jsonFile.writeText(json, Charsets.UTF_8)
        val exporter = PublicDownloadExporter(appContext)
        val publicMd = exporter.exportText("$filePrefix-for-gpt.md", markdown, relativePath = "Huiyi/review/$folderName")
            .getOrElse { exporter.fallbackToPrivate("$filePrefix-for-gpt.md", markdown, subDirectory = "debug/review/$folderName") }
        val publicJson = exporter.exportText("$filePrefix.json", json, "application/json", relativePath = "Huiyi/review/$folderName")
            .getOrElse { exporter.fallbackToPrivate("$filePrefix.json", json, subDirectory = "debug/review/$folderName") }
        mutableState.update {
            it.copy(
                lastDebugExportPath = mdFile.absolutePath,
                lastPublicExportPath = "${publicMd.displayPath} / ${publicJson.displayPath}"
            )
        }
        return listOf(publicMd.displayPath, publicJson.displayPath)
    }

    fun exportPhoneGptReviewBundle() {
        scope.launch {
            val exporter = PhoneGptReviewBundleExporter(appContext)
            exporter.export(
                latestResult = mutableState.value.latestPipelineResult,
                selectedScenario = mutableState.value.selectedRealDeviceScenario,
                latestSessionId = mutableState.value.lastNextSentenceTrace?.sessionId
            ).fold(
                onSuccess = { output ->
                    runCatching {
                        appContext.startActivity(
                            Intent.createChooser(output.shareIntent, "分享 GPT 验收包")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    mutableState.update {
                        it.copy(
                            latestPhoneGptReviewBundlePath = output.zipFile.absolutePath,
                            lastDebugExportPath = output.zipFile.absolutePath,
                            lastPublicExportPath = output.publicCopyPath ?: output.displayPath,
                            lastError = "GPT 验收包已生成，你只需要上传这个 zip。"
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lastError = "GPT 验收包导出失败：${error.message}")
                    }
                }
            )
        }
    }

    fun exportOneTapFeedback(correctionLastSpeaker: String = "NONE") {
        scope.launch {
            mutableState.update {
                it.copy(
                    oneTapGithubUploadState = OneTapGithubUploadState(
                        stage = OneTapGithubUploadStage.ZIP_GENERATING,
                        sessionId = it.latestFlightRecord?.sessionId,
                        userVisibleMessage = "正在生成反馈包..."
                    ),
                    lastError = "正在生成反馈包...",
                    panelVisible = true
                )
            }
            val state = mutableState.value
            val panelSessionId = state.latestPipelineResult?.panelSessionId
                ?: state.latestPipelineResult?.sessionId
                ?: state.latestFlightRecord?.panelSessionId
            val target = OneTapFeedbackZipContract.selectTargetRecord(
                panelSessionId = panelSessionId,
                lastCompletedSessionId = state.latestFlightRecord?.sessionId,
                latest = state.latestFlightRecord,
                records = state.recentFlightRecords
            )
            if (target == null) {
                mutableState.update {
                    it.copy(
                        oneTapGithubUploadState = OneTapGithubUploadState(
                            stage = OneTapGithubUploadStage.UPLOAD_FAILED,
                            errorCode = OneTapGithubUploadErrorCode.GITHUB_UPLOAD_UNKNOWN_ERROR,
                            errorMessageRedacted = "NO_TARGET_SESSION_FOR_FEEDBACK",
                            userVisibleMessage = "没有找到刚才那次分析记录，请先点一次下一句。"
                        ),
                        lastError = "没有找到刚才那次分析记录，请先点一次下一句。",
                        panelVisible = true
                    )
                }
                return@launch
            }
            val targetRecord = target.first
            val exportSource = target.second
            val feedbackTargetSessionId = panelSessionId?.takeIf { it.isNotBlank() } ?: targetRecord.sessionId
            val targetResult = state.latestPipelineResult?.takeIf {
                it.sessionId == targetRecord.sessionId || it.panelSessionId == targetRecord.sessionId
            }
            val exporter = OneTapFeedbackExporter(appContext)
            exporter.export(
                latestRecord = targetRecord,
                recentRecords = state.recentFlightRecords,
                latestResult = targetResult,
                latestTrace = state.lastNextSentenceTrace,
                feedback = UserFeedbackMark(
                    markedWrong = true,
                    userCorrectionLastSpeaker = correctionLastSpeaker
                ),
                feedbackClickedAt = System.currentTimeMillis(),
                feedbackTargetSessionId = feedbackTargetSessionId,
                feedbackExportSource = exportSource
            ).fold(
                onSuccess = { output ->
                    mutableState.update {
                        it.copy(
                            latestOneTapFeedbackBundlePath = output.zipFile.absolutePath,
                            lastDebugExportPath = output.zipFile.absolutePath,
                            lastPublicExportPath = output.publicCopyPath ?: output.displayPath,
                            oneTapGithubUploadState = OneTapGithubUploadState(
                                stage = OneTapGithubUploadStage.PRIVACY_SCAN_RUNNING,
                                sessionId = output.record.sessionId,
                                zipPath = output.zipFile.absolutePath,
                                userVisibleMessage = "正在检查隐私安全..."
                            ),
                            lastError = "正在检查隐私安全...",
                            panelVisible = true
                        )
                    }
                    val uploadConfig = OneTapGithubUploadConfig(
                        endpoint = ReviewUploadEndpointResolver.resolve(
                            configuredEndpoint = BuildConfig.HUIYI_REVIEW_UPLOAD_ENDPOINT,
                            lanUpdateUrl = mutableState.value.lanUpdateState.updateUrl
                        )
                    )
                    mutableState.update {
                        it.copy(
                            oneTapGithubUploadState = OneTapGithubUploadState(
                                stage = if (uploadConfig.enabled) OneTapGithubUploadStage.UPLOAD_STARTED else OneTapGithubUploadStage.FALLBACK_LOCAL_ONLY,
                                sessionId = output.record.sessionId,
                                zipPath = output.zipFile.absolutePath,
                                userVisibleMessage = if (uploadConfig.enabled) "正在上传 GitHub..." else "GitHub 自动上传暂未配置，准备本地保底..."
                            ),
                            lastError = if (uploadConfig.enabled) "正在上传 GitHub..." else "GitHub 自动上传暂未配置，准备本地保底...",
                            panelVisible = true
                        )
                    }
                    val report = withContext(Dispatchers.IO) {
                        OneTapGithubUploader(uploadConfig).upload(output.zipFile, output.record)
                    }
                    val reportPaths = writeOneTapGithubUploadReport(report)
                    val shouldShareFallback = !report.uploadSuccess
                    if (shouldShareFallback) {
                        runCatching {
                            appContext.startActivity(
                                Intent.createChooser(output.shareIntent, "分享一键反馈包")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                    val finalMessage = buildOneTapGithubUserMessage(report, output.publicCopyPath ?: output.displayPath)
                    mutableState.update {
                        it.copy(
                            latestOneTapFeedbackBundlePath = output.zipFile.absolutePath,
                            lastDebugExportPath = output.zipFile.absolutePath,
                            lastPublicExportPath = output.publicCopyPath ?: output.displayPath,
                            latestOneTapGithubUploadReportPath = reportPaths.first.absolutePath,
                            oneTapGithubUploadState = report.state,
                            lastError = finalMessage,
                            panelVisible = true
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(
                            oneTapGithubUploadState = OneTapGithubUploadState(
                                stage = OneTapGithubUploadStage.UPLOAD_FAILED,
                                errorCode = OneTapGithubUploadErrorCode.GITHUB_UPLOAD_UNKNOWN_ERROR,
                                errorMessageRedacted = error.message?.redactPrivateText(),
                                userVisibleMessage = "反馈包生成失败。"
                            ),
                            lastError = "反馈包生成失败：${error.message?.redactPrivateText()}",
                            panelVisible = true
                        )
                    }
                }
            )
        }
    }

    private fun buildOneTapGithubUserMessage(
        report: OneTapGithubUploadReport,
        fallbackDisplayPath: String
    ): String = if (report.uploadSuccess) {
        """
            已上传 GitHub，GPT 可以验收。
            branch: ${report.githubBranch}
            commit: ${report.githubCommitHash}
            path: ${report.githubReviewPath}
            url: ${report.githubReviewUrl}
        """.trimIndent()
    } else {
        """
            上传 GitHub 失败，但反馈包已保存。
            errorCode: ${report.errorCode}
            本地 zip: $fallbackDisplayPath
            你可以使用刚弹出的系统分享面板发送 zip。
        """.trimIndent()
    }

    private fun writeOneTapGithubUploadReport(report: OneTapGithubUploadReport): Pair<File, File> {
        val markdown = OneTapGithubUploadReportGenerator.markdown(report)
        val json = OneTapGithubUploadReportGenerator.json(report)
        val dir = File(appContext.filesDir, "debug/review").apply { mkdirs() }
        val mdFile = File(dir, "one-tap-github-upload-report-for-gpt.md")
        val jsonFile = File(dir, "one-tap-github-upload-report.json")
        mdFile.writeText(markdown, Charsets.UTF_8)
        jsonFile.writeText(json, Charsets.UTF_8)
        val exporter = PublicDownloadExporter(appContext)
        exporter.exportText("one-tap-github-upload-report-for-gpt.md", markdown, relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("one-tap-github-upload-report-for-gpt.md", markdown, subDirectory = "debug/review") }
        exporter.exportText("one-tap-github-upload-report.json", json, "application/json", relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("one-tap-github-upload-report.json", json, subDirectory = "debug/review") }
        return mdFile to jsonFile
    }

    fun exportClickDiagnosticReports(): List<String> {
        val clickReport = buildClickDiagnosticReport()
        val overlayReport = buildOverlayRuntimeReport()
        val exporter = PublicDownloadExporter(appContext)
        val click = exporter.exportText("accessibility-click-diagnostic-report-for-gpt.md", clickReport, relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("accessibility-click-diagnostic-report-for-gpt.md", clickReport, subDirectory = "debug/review") }
        val overlay = exporter.exportText("overlay-runtime-report-for-gpt.md", overlayReport, relativePath = "Huiyi/review")
            .getOrElse { exporter.fallbackToPrivate("overlay-runtime-report-for-gpt.md", overlayReport, subDirectory = "debug/review") }
        mutableState.update { it.copy(lastPublicExportPath = click.displayPath + " / " + overlay.displayPath) }
        return listOf(click.displayPath, overlay.displayPath)
    }

    private fun exportVisualDebugOverlay(
        exporter: PublicDownloadExporter,
        result: CurrentScreenPipelineResult?
    ): ExportedTextFile? {
        val path = result?.visualDebugResult?.overlayImagePath ?: return null
        val file = File(path)
        if (!file.exists()) return null
        return exporter.exportBinary(
            fileName = "current_screen_overlay.png",
            bytes = file.readBytes(),
            mimeType = "image/png",
            relativePath = "Huiyi/review/real_device_visual_debug"
        ).getOrNull()
    }

    fun exportTextDebug(name: String, text: String): File {
        val file = File(appContext.filesDir, "debug/$name")
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        mutableState.update { it.copy(lastDebugExportPath = file.absolutePath) }
        return file
    }

    private fun sampleClickState(label: String) {
        val sample = AccessibilityClickSample(
            label = label,
            capturedAt = System.currentTimeMillis(),
            runtimeState = AccessibilityRuntimeReader.read(appContext),
            overlayState = OverlayStateStore.state.value
        )
        mutableState.update { it.copy(clickDiagnostics = (it.clickDiagnostics + sample).takeLast(20)) }
    }

    private fun launchClickFollowupSamples() {
        listOf(100L to "afterClick_100ms", 500L to "afterClick_500ms", 1000L to "afterClick_1000ms", 3000L to "afterClick_3000ms").forEach { (ms, label) ->
            scope.launch {
                delay(ms)
                sampleClickState(label)
            }
        }
    }

    private fun friendlyPipelineError(error: Throwable): String {
        val runtime = AccessibilityRuntimeReader.read(appContext)
        return when {
            !runtime.systemAccessibilityEnabled -> "无障碍未开启，请前往系统设置开启。"
            !runtime.serviceConnected -> "系统无障碍已开启，但会意服务暂未连接。请回到聊起聊天窗口等几秒，再点一次“下一句”。"
            !runtime.rootAvailable -> "没读到当前聊天，请确认你停留在聊起聊天窗口。"
            else -> "没读到当前聊天，请回到聊起聊天窗口再点一次“下一句”。${error.message ?: ""}"
        }
    }

    private fun buildClickDiagnosticReport(): String {
        val samples = mutableState.value.clickDiagnostics
        val runtime = AccessibilityRuntimeReader.read(appContext)
        val overlay = OverlayStateStore.state.value
        val wrongPermissionTextShown = mutableState.value.lastError == "无障碍没有权限"
        return buildString {
            appendLine("# Accessibility Click Diagnostic Report")
            appendLine()
            appendLine("- generatedAt: ${System.currentTimeMillis()}")
            appendLine("- systemAccessibilityEnabled: ${runtime.systemAccessibilityEnabled}")
            appendLine("- serviceConnected: ${runtime.serviceConnected}")
            appendLine("- rootAvailable: ${runtime.rootAvailable}")
            appendLine("- runtimeCategory: ${runtime.category}")
            appendLine("- runtimeMessage: ${accessibilityRuntimeMessage(runtime)}")
            appendLine("- system_enabled_but_service_not_connected: ${runtime.systemAccessibilityEnabled && !runtime.serviceConnected}")
            appendLine("- overlayVisible: ${runtime.overlayVisible}")
            appendLine("- floatingServiceRunning: ${runtime.floatingServiceRunning}")
            appendLine("- windowManagerException: ${overlay.lastWindowManagerException ?: "none"}")
            appendLine("- pipelineException: ${mutableState.value.lastClickPipelineException ?: overlay.lastPipelineException ?: "none"}")
            appendLine("- floatingBubbleDisappearReason: ${overlay.removeViewReason ?: "none"}")
            appendLine("- wrongNoPermissionTextShown: $wrongPermissionTextShown")
            appendLine()
            appendLine("## Samples")
            samples.forEach { sample ->
                appendLine("- ${sample.label}")
                appendLine("  capturedAt: ${sample.capturedAt}")
                appendLine("  systemAccessibilityEnabled: ${sample.runtimeState.systemAccessibilityEnabled}")
                appendLine("  serviceConnected: ${sample.runtimeState.serviceConnected}")
                appendLine("  rootAvailable: ${sample.runtimeState.rootAvailable}")
                appendLine("  currentPackage: ${sample.runtimeState.currentPackage ?: "unknown"}")
                appendLine("  currentWindowTitle: ${sample.runtimeState.currentWindowTitle ?: "unknown"}")
                appendLine("  overlayVisible: ${sample.runtimeState.overlayVisible}")
                appendLine("  floatingServiceRunning: ${sample.runtimeState.floatingServiceRunning}")
                appendLine("  activeServiceInstanceId: ${sample.runtimeState.activeServiceInstanceId ?: "none"}")
                appendLine("  lastError: ${sample.runtimeState.lastError ?: "none"}")
            }
        }
    }

    private fun buildOverlayRuntimeReport(): String {
        val overlay = OverlayStateStore.state.value
        val runtime = AccessibilityRuntimeReader.read(appContext)
        return buildString {
            appendLine("# Overlay Runtime Report")
            appendLine()
            appendLine("- generatedAt: ${System.currentTimeMillis()}")
            appendLine("- bubbleVisible: ${overlay.bubbleVisible}")
            appendLine("- resultPanelVisible: ${overlay.resultPanelVisible}")
            appendLine("- errorPanelVisible: ${overlay.errorPanelVisible}")
            appendLine("- lastPanelType: ${overlay.lastPanelType ?: "none"}")
            appendLine("- lastBubbleClickAt: ${overlay.lastBubbleClickAt ?: "none"}")
            appendLine("- lastPanelShownAt: ${overlay.lastPanelShownAt ?: "none"}")
            appendLine("- lastPanelDismissedAt: ${overlay.lastPanelDismissedAt ?: "none"}")
            appendLine("- lastOverlayError: ${overlay.lastOverlayError ?: "none"}")
            appendLine("- lastWindowManagerException: ${overlay.lastWindowManagerException ?: "none"}")
            appendLine("- lastWindowManagerStackTrace: ${overlay.lastWindowManagerStackTrace ?: "none"}")
            appendLine("- addViewSuccess: ${overlay.addViewSuccess}")
            appendLine("- removeViewReason: ${overlay.removeViewReason ?: "none"}")
            appendLine("- floatingServiceRunning: ${overlay.floatingServiceRunning}")
            appendLine("- lastPipelineException: ${overlay.lastPipelineException ?: "none"}")
            appendLine("- serviceStoppedByUser: ${overlay.serviceStoppedByUser}")
            appendLine("- overlayPermissionState: ${android.provider.Settings.canDrawOverlays(appContext)}")
            appendLine("- currentForegroundPackage: ${runtime.currentPackage ?: "unknown"}")
            appendLine("- targetPackage: ${appContext.packageName}")
        }
    }

    fun setLanUpdateUrl(url: String) {
        prefs.edit().putString("lan_update_url", url).apply()
        mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(updateUrl = url, error = null)) }
    }

    fun checkLanUpdate() {
        scope.launch {
            val configuredUrl = mutableState.value.lanUpdateState.updateUrl.ifBlank { BuildConfig.HUIYI_UPDATE_BASE_URL }
            suspend fun applyFound(url: String, manifest: com.huiyi.v4.domain.model.UpdateManifest, raw: String, status: String) {
                prefs.edit().putString("lan_update_url", url).apply()
                mutableState.update {
                    it.copy(
                        lanUpdateState = it.lanUpdateState.copy(
                            updateUrl = url,
                            latestManifest = manifest,
                            latestJsonRaw = raw,
                            status = status,
                            error = null
                        )
                    )
                }
            }

            if (configuredUrl.isNotBlank()) {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(updateUrl = configuredUrl, status = "正在检查局域网更新", error = null)) }
                updateManager.check(configuredUrl).fold(
                    onSuccess = { (manifest, raw) ->
                        applyFound(configuredUrl, manifest, raw, "发现版本 ${manifest.versionName} (${manifest.versionCode})")
                    },
                    onFailure = { firstError ->
                        val fallbackUrl = BuildConfig.HUIYI_UPDATE_BASE_URL
                        if (fallbackUrl.isNotBlank() && fallbackUrl != configuredUrl) {
                            updateManager.check(fallbackUrl).fold(
                                onSuccess = { (manifest, raw) ->
                                    applyFound(fallbackUrl, manifest, raw, "固定地址发现版本 ${manifest.versionName} (${manifest.versionCode})")
                                },
                                onFailure = { fallbackError ->
                                    mutableState.update {
                                        it.copy(
                                            lanUpdateState = it.lanUpdateState.copy(
                                                status = "检查失败",
                                                error = "${firstError.message}; fallback ${fallbackUrl}: ${fallbackError.message}"
                                            )
                                        )
                                    }
                                }
                            )
                        } else {
                            mutableState.update {
                                it.copy(lanUpdateState = it.lanUpdateState.copy(status = "检查失败", error = firstError.message))
                            }
                        }
                    }
                )
                return@launch
            }

            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在自动发现局域网更新服务", error = null)) }
            updateManager.discoverAndCheck().fold(
                onSuccess = { (foundUrl, manifest, raw) ->
                    applyFound(foundUrl, manifest, raw, "已自动发现：${manifest.versionName} (${manifest.versionCode})")
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lanUpdateState = it.lanUpdateState.copy(status = "自动发现失败", error = error.message))
                    }
                }
            )
        }
    }
    fun downloadLanUpdate() {
        scope.launch {
            val updateState = mutableState.value.lanUpdateState
            val manifest = updateState.latestManifest
            if (manifest == null) {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "请先检查更新", error = null)) }
                return@launch
            }
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在下载", error = null)) }
            updateManager.download(updateState.updateUrl, manifest).fold(
                onSuccess = { file ->
                    mutableState.update {
                        it.copy(
                            lanUpdateState = it.lanUpdateState.copy(
                                downloadedApkPath = file.absolutePath,
                                status = "下载完成，等待安装确认",
                                error = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lanUpdateState = it.lanUpdateState.copy(status = "下载失败", error = error.message))
                    }
                }
            )
        }
    }

    fun openDownloadedUpdateInstaller() {
        val path = mutableState.value.lanUpdateState.downloadedApkPath
        if (path.isNullOrBlank()) {
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "还没有下载 APK")) }
            return
        }
        updateManager.openInstaller(File(path)).fold(
            onSuccess = {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "已打开系统安装确认")) }
            },
            onFailure = { error ->
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "打开安装失败", error = error.message)) }
            }
        )
    }

    private fun currentPersona(): UserPersonaCorpus = DefaultPersonaCorpus.soldier(mutableState.value.demoState.personaEnabled)

    companion object {
        private const val LOG_TAG = "HuiyiRuntime"
        private val ACCESSIBILITY_SERVICE_RECONNECT_DELAYS_MS = longArrayOf(250L, 500L, 750L, 1_000L)

        @Volatile
        private var instance: HuiyiRuntime? = null

        fun get(context: Context): HuiyiRuntime {
            return instance ?: synchronized(this) {
                instance ?: HuiyiRuntime(context.applicationContext).also { instance = it }
            }
        }
    }
}
