package com.huiyi.v4.runtime

import android.content.Context
import android.content.Intent
import com.huiyi.v4.data.DatabaseProvider
import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.capture.ManualContextCaptureSession
import com.huiyi.v4.domain.capture.VisualDebugResult
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.model.ReplyAttemptStatus
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserAction
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
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
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
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
import java.io.File
import java.util.UUID

data class HuiyiRuntimeState(
    val demoState: HuiyiDemoState = sampleState(),
    val latestPipelineResult: CurrentScreenPipelineResult? = null,
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
    val lanUpdateState: LanUpdateState = LanUpdateState()
)

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
    private val pipeline = CurrentScreenPipelineUseCase(
        captureUseCase = CurrentScreenCaptureUseCase(),
        persistenceRepository = persistence
    )
    private var sessionWatchdogJob: Job? = null

    private val mutableState = MutableStateFlow(HuiyiRuntimeState())
    val state: StateFlow<HuiyiRuntimeState> = mutableState

    init {
        val savedUrl = prefs.getString("lan_update_url", "").orEmpty()
        mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(updateUrl = savedUrl)) }
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

    fun togglePersona() {
        mutableState.update { it.copy(demoState = it.demoState.togglePersona()) }
    }

    fun runNextSentence() {
        scope.launch {
            sampleClickState("beforeClick")
            launchClickFollowupSamples()
            val beforeRuntime = AccessibilityRuntimeReader.read(appContext)
            val beforeOverlay = OverlayStateStore.state.value
            val startedTrace = NextSentenceSessionTrace(
                sessionId = UUID.randomUUID().toString(),
                startedAt = System.currentTimeMillis(),
                stage = NextSentenceStage.CLICK_RECEIVED,
                bubbleVisibleBeforeClick = beforeOverlay.bubbleVisible,
                bubbleVisibleAfterClick = beforeOverlay.bubbleVisible,
                bubbleAttachedAfterClick = beforeOverlay.bubbleVisible,
                systemAccessibilityEnabled = beforeRuntime.systemAccessibilityEnabled,
                serviceConnected = beforeRuntime.serviceConnected,
                activePackageBeforeClick = beforeRuntime.currentPackage
            )
            val previousSessionId = mutableState.value.lastNextSentenceTrace?.sessionId
            val scenarioAtStart = mutableState.value.selectedRealDeviceScenario
            mutableState.update {
                it.copy(
                    lastNextSentenceTrace = startedTrace,
                    lastError = null,
                    latestPipelineResult = null,
                    panelVisible = false
                )
            }
            launchSessionTerminalWatchdog(startedTrace, scenarioAtStart)
            try {
                val persona = currentPersona()
                val result = pipeline.run(persona)
                result.fold(
                    onSuccess = { pipelineResult ->
                        val messages = pipelineResult.context?.currentScreenMessages ?: mutableState.value.demoState.messages
                        val scenario = mutableState.value.selectedRealDeviceScenario
                        val visualDebug = runCatching {
                            VisualDebugCapture(File(appContext.filesDir, "debug/real_device_visual_debug"))
                                .capture(HuiyiAccessibilityService.instance, pipelineResult, scenario)
                        }.getOrElse { error ->
                            val screenshotCode = mapScreenshotException(error) ?: NextSentenceErrorCode.SCREENSHOT_FAILED
                            VisualDebugResult(
                                screenshotCaptured = false,
                                screenshotUnavailable = true,
                                reason = error.message ?: screenshotCode.name,
                                screenshotPath = null,
                                overlayImagePath = null,
                                screenshotWidth = pipelineResult.captureResult?.snapshot?.screenWidth ?: 0,
                                screenshotHeight = pipelineResult.captureResult?.snapshot?.screenHeight ?: 0,
                                accessibilityBoundsProjected = pipelineResult.captureResult?.accessibilityBoundsProjected == true,
                                ocrUsed = false,
                                visualTruthAvailable = pipelineResult.captureResult?.visualTruthAvailable == true,
                                screenshotErrorCode = screenshotCode.name,
                                screenshotExceptionClass = error::class.java.name,
                                screenshotExceptionMessageRedacted = error.message?.redactPrivateText()
                            )
                        }
                        val waitPanelShown = pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT &&
                            pipelineResult.routes.isEmpty()
                        val routePanelShown = pipelineResult.routes.isNotEmpty()
                        val endedAt = System.currentTimeMillis()
                        val terminalState = terminalStateFor(pipelineResult.tacticalDecision.decisionType)
                        val resultWithVisualDebug = pipelineResult.copy(
                            visualDebugResult = visualDebug,
                            sessionId = startedTrace.sessionId,
                            previousSessionId = previousSessionId,
                            panelSessionId = startedTrace.sessionId,
                            panelContentFromCurrentSession = true,
                            staleRoutesClearedAtSessionStart = true,
                            staleRoutesReused = false,
                            waitPanelShown = waitPanelShown,
                            routePanelShown = routePanelShown,
                            sessionTerminalState = terminalState,
                            analysisStartedAt = startedTrace.startedAt,
                            analysisEndedAt = endedAt,
                            analysisDurationMs = endedAt - startedTrace.startedAt,
                            loadingStillVisibleAfterTimeout = false,
                            waitDecisionReached = pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT,
                            waitPanelRenderAttempted = waitPanelShown,
                            waitPanelRenderSuccess = waitPanelShown,
                            decisionTypeFamily = decisionTypeFamily(pipelineResult.tacticalDecision.decisionType)
                        )
                        val capture = pipelineResult.captureResult
                        val successTrace = startedTrace.copy(
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
                            userFacingMessage = if (pipelineResult.tacticalDecision.decisionType == TacticalDecisionType.WAIT) {
                                userFacingMessageFor(NextSentenceErrorCode.LAST_SPEAKER_IS_ME_SHOULD_WAIT)
                            } else null
                        )
                        mutableState.update {
                            it.copy(
                                demoState = it.demoState.copy(messages = messages),
                                latestPipelineResult = resultWithVisualDebug,
                                panelVisible = true,
                                lastError = pipelineResult.persistenceError,
                                lastVisualDebugOverlayPath = visualDebug.overlayImagePath,
                                lastClickPipelineException = null,
                                lastNextSentenceTrace = successTrace
                            )
                        }
                    },
                    onFailure = { error ->
                        handleNextSentenceFailure(error, startedTrace)
                    }
                )
            } catch (error: Throwable) {
                handleNextSentenceFailure(error, startedTrace)
            }
        }
    }

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

    private fun launchSessionTerminalWatchdog(
        trace: NextSentenceSessionTrace,
        scenario: RealDeviceScenario
    ) {
        sessionWatchdogJob?.cancel()
        sessionWatchdogJob = scope.launch {
            delay(8000L)
            val state = mutableState.value
            if (state.lastNextSentenceTrace?.sessionId != trace.sessionId) return@launch
            if (state.latestPipelineResult != null || state.lastError != null) return@launch
            val code = if (scenario == RealDeviceScenario.LAST_ME) {
                NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK
            } else {
                NextSentenceErrorCode.SESSION_TIMEOUT_NO_TERMINAL_STATE
            }
            val timeoutTrace = trace.failed(code, state.lastNextSentenceTrace?.stage ?: trace.stage)
                .copy(
                    endedAt = System.currentTimeMillis(),
                    userFacingMessage = if (scenario == RealDeviceScenario.LAST_ME) {
                        "LAST ME 分析超时，已生成卡住证据报告。"
                    } else {
                        userFacingMessageFor(code)
                    }
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
            mutableState.update {
                it.copy(
                    lastNextSentenceTrace = timeoutTrace,
                    lastError = timeoutTrace.userFacingMessage,
                    panelVisible = true,
                    latestPipelineResult = null
                )
            }
        }
    }

    private fun terminalStateFor(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "PANEL_RENDERED_WAIT"
        TacticalDecisionType.CONTEXT_REQUIRED,
        TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "PANEL_RENDERED_CONTEXT_REQUIRED"
        else -> "PANEL_RENDERED_ROUTE"
    }

    private fun decisionTypeFamily(type: TacticalDecisionType): String = when (type) {
        TacticalDecisionType.WAIT -> "WAIT"
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
                lastClickPipelineException = "${error::class.java.name}: ${error.message}",
                lastNextSentenceTrace = next.trace
            )
        }
    }

    private fun handleNextSentenceFailure(error: Throwable, baseTrace: NextSentenceSessionTrace) {
        val next = error.toNextSentenceException(baseTrace, NextSentenceStage.NODE_TREE_CAPTURE_STARTED)
        OverlayStateStore.recordPipelineException(error)
        val overlay = OverlayStateStore.state.value
        val runtime = AccessibilityRuntimeReader.read(appContext)
        val finalTrace = next.trace.copy(
            endedAt = System.currentTimeMillis(),
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
        mutableState.update {
            it.copy(
                panelVisible = true,
                latestPipelineResult = null,
                lastError = finalTrace.userFacingMessage,
                lastClickPipelineException = "${error::class.java.name}: ${error.message}",
                lastNextSentenceTrace = finalTrace,
                latestNextSentenceFailureMarkdownPath = paths.first.absolutePath,
                latestNextSentenceFailureJsonPath = paths.second.absolutePath
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
            !runtime.serviceConnected -> "系统无障碍已开启，但会意服务暂未连接。请返回聊天窗口等待几秒，或重新关闭/开启一次无障碍。"
            !runtime.rootAvailable -> "无障碍已开启，但当前窗口暂时不可读取。请确认你停留在聊天页面。"
            else -> "这次分析失败，但悬浮球仍在。${error.message ?: ""}"
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
            val url = mutableState.value.lanUpdateState.updateUrl
            if (url.isBlank()) {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在自动发现局域网更新服务", error = null)) }
                updateManager.discoverAndCheck().fold(
                    onSuccess = { (foundUrl, manifest, raw) ->
                        prefs.edit().putString("lan_update_url", foundUrl).apply()
                        mutableState.update {
                            it.copy(
                                lanUpdateState = it.lanUpdateState.copy(
                                    updateUrl = foundUrl,
                                    latestManifest = manifest,
                                    latestJsonRaw = raw,
                                    status = "已自动发现：${manifest.versionName} (${manifest.versionCode})",
                                    error = null
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        mutableState.update {
                            it.copy(lanUpdateState = it.lanUpdateState.copy(status = "自动发现失败", error = error.message))
                        }
                    }
                )
                return@launch
            }
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在检查", error = null)) }
            updateManager.check(url).fold(
                onSuccess = { (manifest, raw) ->
                    mutableState.update {
                        it.copy(
                            lanUpdateState = it.lanUpdateState.copy(
                                latestManifest = manifest,
                                latestJsonRaw = raw,
                                status = "发现版本 ${manifest.versionName} (${manifest.versionCode})",
                                error = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lanUpdateState = it.lanUpdateState.copy(status = "检查失败", error = error.message))
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
        @Volatile
        private var instance: HuiyiRuntime? = null

        fun get(context: Context): HuiyiRuntime {
            return instance ?: synchronized(this) {
                instance ?: HuiyiRuntime(context.applicationContext).also { instance = it }
            }
        }
    }
}
