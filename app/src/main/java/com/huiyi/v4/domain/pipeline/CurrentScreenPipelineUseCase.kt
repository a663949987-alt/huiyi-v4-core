package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.cloud.CloudVisualEvidence
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.InfluenceProfile
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.capture.VisualDebugResult
import com.huiyi.v4.domain.review.ChatReviewMemoryBuilder
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val DEFAULT_LATE_CLOUD_SOFT_TIMEOUT_MS = 12_000L

data class CurrentScreenPipelineResult(
    val captureResult: CurrentScreenCaptureResult?,
    val context: ChatSceneContext?,
    val lastSpeakerDecision: LastSpeakerDecision,
    val tacticalDecision: TacticalDecision,
    val routes: List<ReplyRoute>,
    val apiCalled: Boolean,
    val persistenceError: String? = null,
    val overlayShownInTargetApp: Boolean = false,
    val foregroundPackageWhenPanelShown: String? = null,
    val huiyiActivityOpened: Boolean = false,
    val userStayedInChatApp: Boolean = false,
    val resultShownAsOverlay: Boolean = false,
    val mainActivityOpened: Boolean = false,
    val visualDebugResult: VisualDebugResult? = null,
    val userCorrectionProvided: Boolean = false,
    val correctedLastSpeaker: Speaker? = null,
    val correctedMessageId: String? = null,
    val sessionId: String? = null,
    val previousSessionId: String? = null,
    val panelSessionId: String? = null,
    val panelContentFromCurrentSession: Boolean = true,
    val staleRoutesClearedAtSessionStart: Boolean = true,
    val staleRoutesReused: Boolean = false,
    val waitPanelShown: Boolean = false,
    val routePanelShown: Boolean = false,
    val sessionTerminalState: String = "UNKNOWN",
    val analysisStartedAt: Long = 0L,
    val analysisEndedAt: Long = 0L,
    val analysisDurationMs: Long = 0L,
    val loadingStillVisibleAfterTimeout: Boolean = false,
    val lastObservedStageBeforeTimeout: String = "NONE",
    val timeoutErrorCode: String = "NONE",
    val waitDecisionReached: Boolean = false,
    val waitPanelRenderAttempted: Boolean = false,
    val waitPanelRenderSuccess: Boolean = false,
    val decisionTypeFamily: String = "UNKNOWN",
    val lightListenBackfillCount: Int = 0,
    val lightListenUsed: Boolean = false,
    val cloudTrace: CloudAnalysisTrace = CloudAnalysisTrace()
)

data class LateCloudPipelineResult(
    val sessionId: String,
    val preAnalysisSnapshotId: String,
    val chatPackage: String,
    val chatWindowHash: String,
    val decision: TacticalDecision,
    val routes: List<ReplyRoute>,
    val trace: CloudAnalysisTrace
)

class CurrentScreenPipelineUseCase(
    private val captureUseCase: CurrentScreenCaptureUseCase,
    private val contextAssembler: ContextAssembler = ContextAssembler(),
    private val lastSpeakerDecisionUseCase: LastSpeakerDecisionUseCase = LastSpeakerDecisionUseCase(),
    private val decisionEngine: TacticalDecisionEngine = TacticalDecisionEngine(),
    private val routeGenerator: ReplyRouteGenerator = ReplyRouteGenerator(),
    private val persistenceRepository: HuiyiPersistenceRepository? = null,
    private val cloudAnalysisService: CloudAnalysisService? = null,
    private val visualEvidenceProvider: (suspend () -> CloudVisualEvidence?)? = null,
    private val recentVisualEvidenceProvider: ((CurrentScreenCaptureResult) -> List<CloudVisualEvidence>)? = null,
    private val lightListenContextProvider: ((CurrentScreenCaptureResult) -> List<com.huiyi.v4.domain.model.MessageNode>)? = null,
    private val appVersionName: String = "",
    private val appVersionCode: Int = 0,
    private val lateCloudScope: CoroutineScope? = null,
    private val lateCloudSoftTimeoutMs: Long = DEFAULT_LATE_CLOUD_SOFT_TIMEOUT_MS,
    private val onLateCloudResult: ((LateCloudPipelineResult) -> Unit)? = null
) {
    suspend fun run(
        userPersonaCorpus: UserPersonaCorpus,
        sessionId: String = java.util.UUID.randomUUID().toString()
    ): Result<CurrentScreenPipelineResult> {
        return captureUseCase.capture().mapCatching { capture ->
            val preAnalysisSnapshotId = NextSentenceSnapshotIdentity.snapshotId(capture.snapshot)
            val chatPackage = capture.snapshot.appPackage.orEmpty()
            val chatWindowHash = NextSentenceSnapshotIdentity.chatWindowHash(capture.snapshot)
            val contamination = PreAnalysisContaminationGuard.inspect(capture)
            if (contamination.contaminated) {
                return@mapCatching contaminatedResult(
                    capture = capture,
                    reason = contamination.reason,
                    sessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash
                )
            }
            val lastSpeaker = lastSpeakerDecisionUseCase.decide(capture.messages)
            val lightListenBackfill = lightListenContextProvider?.invoke(capture)
                .orEmpty()
                .filter { it.isEffectiveChatMessage && it.speaker in setOf(Speaker.ME, Speaker.OTHER) }
            val recentVisualEvidence = recentVisualEvidenceProvider?.invoke(capture).orEmpty()
            val context = contextAssembler.assemble(
                currentScreenMessages = capture.messages,
                contextBackfillMessages = lightListenBackfill,
                userPersonaCorpus = userPersonaCorpus
            )
            val unknownRatio = capture.messages.count { it.speaker == Speaker.UNKNOWN }
                .toFloat() / capture.messages.size.coerceAtLeast(1)
            val unknownTooHigh = unknownRatio > 0.30f
            val hasUnknownChatNode = capture.messages.any {
                it.speaker == Speaker.UNKNOWN && it.metadataType == MetadataType.NONE
            }
            val hasVisualConflict = capture.messages.any { it.visualConflict }
            val hasMissingVisualLastMessage = lastSpeaker.lastEffectiveMessage?.content.let {
                it is MessageContent.Image || it is MessageContent.Sticker
            }
            val isLiaoqiRealUse = capture.snapshot.appPackage == "com.bajiao.im.liaoqi"
            val packageName = capture.snapshot.appPackage.orEmpty()
            val visualCloudAllowed = packageName.isNotBlank() &&
                packageName !in setOf("com.huiyi.v4", "com.android.systemui")
            val forceLastOtherRoutes = isLiaoqiRealUse && lastSpeaker.lastSpeaker == Speaker.OTHER
            val decision = when {
                lastSpeaker.lastSpeaker == Speaker.ME -> lastMeWaitDecision()
                forceLastOtherRoutes -> decisionEngine.decide(context)
                unknownTooHigh -> unknownSpeakerDecision("UNKNOWN 说话人超过 30%，不允许高置信度生成。")
                hasVisualConflict -> unknownSpeakerDecision("当前屏幕存在 Accessibility 与视觉投影冲突，需要先看 visual debug 图。")
                hasUnknownChatNode -> unknownSpeakerDecision("当前屏幕存在边界不清的聊天气泡，不允许高置信度生成。")
                hasMissingVisualLastMessage -> unknownSpeakerDecision("最后一条是未描述的图片/表情，需要先补充画面含义。")
                lastSpeaker.unknownSpeaker -> unknownSpeakerDecision(lastSpeaker.reason)
                else -> decisionEngine.decide(context)
            }
            val localRoutes = if (forceLastOtherRoutes) {
                routeGenerator.generate(context, decision)
            } else if (
                decision.decisionType == TacticalDecisionType.WAIT ||
                decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED ||
                lastSpeaker.unknownSpeaker ||
                unknownTooHigh ||
                hasVisualConflict ||
                hasUnknownChatNode ||
                hasMissingVisualLastMessage
            ) {
                emptyList()
            } else {
                routeGenerator.generate(context, decision)
            }
            val targetSupported = capture.snapshot.appPackage in setOf("com.bajiao.im.liaoqi", "com.huiyi.mockchat")
            val cloudResult = maybeAnalyzeWithCloud(
                sessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                capture = capture,
                context = context,
                lastSpeaker = lastSpeaker,
                localDecision = decision,
                localRoutes = localRoutes,
                targetSupported = targetSupported,
                visualCloudAllowed = visualCloudAllowed,
                recentVisualEvidence = recentVisualEvidence
            )
            val persistenceError = persistenceRepository?.saveScene(context)?.exceptionOrNull()?.message
            val reviewError = persistenceRepository?.saveChatReviewDraft(
                ChatReviewMemoryBuilder.build(
                    context = context,
                    appPackage = capture.snapshot.appPackage.orEmpty(),
                    windowTitle = capture.snapshot.windowTitle.orEmpty(),
                    decision = cloudResult.decision,
                    routes = cloudResult.routes,
                    cloudTrace = cloudResult.trace
                )
            )?.exceptionOrNull()?.message
            val combinedPersistenceError = listOfNotNull(persistenceError, reviewError)
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
            CurrentScreenPipelineResult(
                captureResult = capture,
                context = context,
                lastSpeakerDecision = lastSpeaker,
                tacticalDecision = cloudResult.decision,
                routes = cloudResult.routes,
                apiCalled = cloudResult.trace.apiCalled,
                persistenceError = combinedPersistenceError,
                sessionId = sessionId,
                lightListenBackfillCount = lightListenBackfill.size,
                lightListenUsed = lightListenBackfill.isNotEmpty(),
                cloudTrace = cloudResult.trace.withSessionBinding(
                    activeSessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    panelRenderedSessionId = sessionId
                )
            )
        }
    }

    private suspend fun maybeAnalyzeWithCloud(
        sessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String,
        capture: CurrentScreenCaptureResult,
        context: ChatSceneContext,
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        targetSupported: Boolean,
        visualCloudAllowed: Boolean,
        recentVisualEvidence: List<CloudVisualEvidence>
    ): CloudPipelineResult {
        val service = cloudAnalysisService
        val config = service?.config ?: CloudAnalysisConfig(cloudEnabled = false)
        if (lastSpeaker.lastSpeaker == Speaker.ME) {
            return CloudPipelineResult(lastMeWaitDecision(), emptyList(), CloudAnalysisTrace.skipped(config, "LAST_SPEAKER_ME_WAIT", "LOCAL_WAIT"))
        }
        if (lastSpeaker.lastSpeaker == Speaker.UNKNOWN || lastSpeaker.unknownSpeaker) {
            val visualEvidence = visualEvidenceForCloud(config, visualCloudAllowed)
            if (visualEvidence == null || service == null) {
                return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "LAST_SPEAKER_UNKNOWN", "LOCAL_FALLBACK"))
            }
            return analyzeWithCloud(
                service = service,
                config = config,
                sessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                capture = capture,
                context = context,
                lastSpeaker = lastSpeaker,
                localDecision = localDecision,
                localRoutes = localRoutes,
                visualEvidence = visualEvidence,
                recentVisualEvidence = recentVisualEvidence
            )
        }
        if (!targetSupported) {
            val visualEvidence = visualEvidenceForCloud(config, visualCloudAllowed)
            if (visualEvidence != null && service != null) {
                return analyzeWithCloud(
                    service = service,
                    config = config,
                    sessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    capture = capture,
                    context = context,
                    lastSpeaker = lastSpeaker,
                    localDecision = localDecision,
                    localRoutes = localRoutes,
                    visualEvidence = visualEvidence,
                    recentVisualEvidence = recentVisualEvidence
                )
            }
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "UNSUPPORTED_APP", "LOCAL_FALLBACK"))
        }
        if (service == null || !config.cloudEnabled || !config.endpointConfigured) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "CLOUD_NOT_CONFIGURED", "LOCAL_FALLBACK"))
        }
        if (!config.relayApiKeyConfigured) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "RELAY_API_KEY_MISSING", "LOCAL_FALLBACK"))
        }
        if (!config.relayApiKeyStoredSecurely) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "RELAY_API_KEY_INSECURE_STORAGE", "LOCAL_FALLBACK"))
        }
        if (lastSpeaker.lastSpeaker != Speaker.OTHER || localRoutes.size != 5) {
            val visualEvidence = visualEvidenceForCloud(config, visualCloudAllowed)
            if (visualEvidence != null) {
                return analyzeWithCloud(
                    service = service,
                    config = config,
                    sessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    capture = capture,
                    context = context,
                    lastSpeaker = lastSpeaker,
                    localDecision = localDecision,
                    localRoutes = localRoutes,
                    visualEvidence = visualEvidence,
                    recentVisualEvidence = recentVisualEvidence
                )
            }
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "LOCAL_CONTEXT_REQUIRED", "LOCAL_FALLBACK"))
        }
        val visualEvidence = visualEvidenceForCloud(config, visualCloudAllowed)
        return analyzeWithCloud(
            service = service,
            config = config,
            sessionId = sessionId,
            preAnalysisSnapshotId = preAnalysisSnapshotId,
            chatPackage = chatPackage,
            chatWindowHash = chatWindowHash,
            capture = capture,
            context = context,
            lastSpeaker = lastSpeaker,
            localDecision = localDecision,
            localRoutes = localRoutes,
            visualEvidence = visualEvidence,
            recentVisualEvidence = recentVisualEvidence
        )
    }

    private suspend fun visualEvidenceForCloud(config: CloudAnalysisConfig, allowed: Boolean): CloudVisualEvidence? {
        if (!allowed || !config.configuredAndEnabled) return null
        return visualEvidenceProvider?.invoke()
    }

    private suspend fun analyzeWithCloud(
        service: CloudAnalysisService,
        config: CloudAnalysisConfig,
        sessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String,
        capture: CurrentScreenCaptureResult,
        context: ChatSceneContext,
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        visualEvidence: CloudVisualEvidence?,
        recentVisualEvidence: List<CloudVisualEvidence>
    ): CloudPipelineResult {
        val startedAt = System.currentTimeMillis()
        val input = CloudAnalysisInput(
            sessionId = sessionId,
            preAnalysisSnapshotId = preAnalysisSnapshotId,
            chatPackage = chatPackage,
            chatWindowHash = chatWindowHash,
            appVersionName = appVersionName,
            appVersionCode = appVersionCode,
            capture = capture,
            context = context,
            lastSpeakerDecision = lastSpeaker,
            localDecision = localDecision,
            visualEvidence = visualEvidence,
            recentVisualEvidence = recentVisualEvidence
        )
        val lateScope = lateCloudScope
        val lateCallback = onLateCloudResult
        if (lateScope != null && lateCallback != null && lateCloudSoftTimeoutMs > 0L) {
            val cloudResponse = CompletableDeferred<Result<CloudAnalysisOutput>>()
            lateScope.launch {
                val serviceResult = try {
                    service.analyze(input)
                } catch (error: CancellationException) {
                    cloudResponse.cancel(error)
                    return@launch
                } catch (error: Throwable) {
                    Result.failure(error)
                }
                cloudResponse.complete(serviceResult)
            }
            val earlyResult = withTimeoutOrNull(lateCloudSoftTimeoutMs) {
                cloudResponse.await()
            }
            if (earlyResult != null) {
                return cloudPipelineResultFromServiceResult(
                    serviceResult = earlyResult,
                    config = config,
                    sessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    lastSpeaker = lastSpeaker,
                    localDecision = localDecision,
                    localRoutes = localRoutes,
                    context = context,
                    startedAt = startedAt
                )
            }
            lateScope.launch {
                val lateResult = try {
                    cloudResponse.await()
                } catch (error: CancellationException) {
                    return@launch
                } catch (error: Throwable) {
                    Result.failure(error)
                }
                val latePipelineResult = cloudPipelineResultFromServiceResult(
                    serviceResult = lateResult,
                    config = config,
                    sessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    lastSpeaker = lastSpeaker,
                    localDecision = localDecision,
                    localRoutes = localRoutes,
                    context = context,
                    startedAt = startedAt
                )
                if (latePipelineResult.trace.cloudAttempted &&
                    latePipelineResult.trace.cloudErrorCode != "SOFT_TIMEOUT_PENDING"
                ) {
                    lateCallback(
                        LateCloudPipelineResult(
                            sessionId = sessionId,
                            preAnalysisSnapshotId = preAnalysisSnapshotId,
                            chatPackage = chatPackage,
                            chatWindowHash = chatWindowHash,
                            decision = latePipelineResult.decision,
                            routes = latePipelineResult.routes,
                            trace = latePipelineResult.trace
                        )
                    )
                }
            }
            return softTimeoutPendingResult(
                config = config,
                sessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                lastSpeaker = lastSpeaker,
                localDecision = localDecision,
                localRoutes = localRoutes,
                context = context,
                startedAt = startedAt
            )
        }
        return cloudPipelineResultFromServiceResult(
            serviceResult = service.analyze(input),
            config = config,
            sessionId = sessionId,
            preAnalysisSnapshotId = preAnalysisSnapshotId,
            chatPackage = chatPackage,
            chatWindowHash = chatWindowHash,
            lastSpeaker = lastSpeaker,
            localDecision = localDecision,
            localRoutes = localRoutes,
            context = context,
            startedAt = startedAt
        )
    }

    private fun cloudPipelineResultFromServiceResult(
        serviceResult: Result<CloudAnalysisOutput>,
        config: CloudAnalysisConfig,
        sessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String,
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        context: ChatSceneContext,
        startedAt: Long
    ): CloudPipelineResult {
        return serviceResult.fold(
            onSuccess = { output ->
                CloudPipelineResult(
                    output.decision,
                    output.routes,
                    CloudAnalysisTrace.success(config, output)
                        .withSessionBinding(
                            activeSessionId = sessionId,
                            preAnalysisSnapshotId = preAnalysisSnapshotId,
                            chatPackage = chatPackage,
                            chatWindowHash = chatWindowHash,
                            cloudRequestSessionId = sessionId,
                            cloudResponseSessionId = output.sessionId
                        )
                )
            },
            onFailure = { error ->
                val cloudError = error as? CloudAnalysisException
                val code = cloudError?.code ?: "NETWORK"
                val likelyCause = cloudError?.likelyCause ?: "UNKNOWN"
                val requestActuallySent = cloudError?.requestActuallySent ?: true
                val validationResult = if (code in setOf("CLOUD_SCHEMA_INVALID", "CLOUD_CONTRACT_VIOLATION", "SCHEMA_INVALID")) {
                    "FAIL"
                } else {
                    "NOT_RUN"
                }
                val fallbackDecision = cloudFailureFallbackDecision(lastSpeaker, localDecision, localRoutes, code)
                val fallbackRoutes = if (fallbackDecision === localDecision) {
                    localRoutes
                } else {
                    routeGenerator.generate(context, fallbackDecision)
                }
                CloudPipelineResult(
                    fallbackDecision,
                    fallbackRoutes,
                    CloudAnalysisTrace.fallback(
                        config = config,
                        errorCode = code,
                        latencyMs = System.currentTimeMillis() - startedAt,
                        validationResult = validationResult,
                        failureLikelyCause = likelyCause,
                        requestActuallySent = requestActuallySent,
                        primaryModel = cloudError?.primaryModel ?: config.model,
                        finalModel = cloudError?.finalModel ?: config.model,
                        escalated = cloudError?.escalated ?: false,
                        escalationReason = cloudError?.escalationReason,
                        primaryLatencyMs = cloudError?.primaryLatencyMs
                    ).withSessionBinding(
                        activeSessionId = sessionId,
                        preAnalysisSnapshotId = preAnalysisSnapshotId,
                        chatPackage = chatPackage,
                        chatWindowHash = chatWindowHash,
                        cloudRequestSessionId = sessionId,
                        cloudResponseSessionId = sessionId
                    )
                )
            }
        )
    }

    private fun softTimeoutPendingResult(
        config: CloudAnalysisConfig,
        sessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String,
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        context: ChatSceneContext,
        startedAt: Long
    ): CloudPipelineResult {
        val fallbackDecision = cloudFailureFallbackDecision(lastSpeaker, localDecision, localRoutes, "SOFT_TIMEOUT_PENDING")
        val fallbackRoutes = if (fallbackDecision === localDecision) {
            localRoutes
        } else {
            routeGenerator.generate(context, fallbackDecision)
        }
        return CloudPipelineResult(
            fallbackDecision,
            fallbackRoutes,
            CloudAnalysisTrace.fallback(
                config = config,
                errorCode = "SOFT_TIMEOUT_PENDING",
                latencyMs = System.currentTimeMillis() - startedAt,
                validationResult = "PENDING",
                failureLikelyCause = "PENDING",
                requestActuallySent = true,
                primaryModel = config.model,
                finalModel = config.model
            ).copy(
                modelCalled = true,
                apiCalled = true,
                cloudNetworkFailureVisibleToUser = false,
                cloudFailureLikelyCause = "PENDING"
            ).withSessionBinding(
                activeSessionId = sessionId,
                preAnalysisSnapshotId = preAnalysisSnapshotId,
                chatPackage = chatPackage,
                chatWindowHash = chatWindowHash,
                cloudRequestSessionId = sessionId,
                cloudResponseSessionId = null
            )
        )
    }

    private fun cloudFailureFallbackDecision(
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        errorCode: String
    ): TacticalDecision {
        if (localRoutes.isNotEmpty()) return localDecision
        if (lastSpeaker.lastSpeaker != Speaker.OTHER || lastSpeaker.unknownSpeaker) return localDecision
        return TacticalDecision(
            decisionType = TacticalDecisionType.NORMAL_REPLY,
            situation = if (errorCode == "TIMEOUT") {
                "Cloud timed out; using local fallback."
            } else {
                "Cloud unavailable; using local fallback."
            },
            coreInsight = "A clean LAST_OTHER screen must not end with zero routes after cloud failure.",
            userLikelyMistake = "Waiting indefinitely for cloud or closing the panel because no result appears.",
            bestMove = "Give a short low-pressure reply first.",
            avoidMoves = listOf("do not keep loading", "do not show analysis failed", "do not route ME or UNKNOWN"),
            coCreationOpportunity = null,
            shouldUseUserStory = false,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(
                intensity = InfluenceIntensity.LOW,
                riskLevel = RiskLevel.LOW,
                riskWarning = if (errorCode == "TIMEOUT") "cloud_timeout_local_fallback" else "cloud_unavailable_local_fallback",
                fallbackMove = "Retry cloud later."
            ),
            fallbackMove = "Retry cloud later."
        )
    }

    private data class CloudPipelineResult(
        val decision: TacticalDecision,
        val routes: List<ReplyRoute>,
        val trace: CloudAnalysisTrace
    )

    private fun lastMeWaitDecision(): TacticalDecision = TacticalDecision(
        decisionType = TacticalDecisionType.WAIT,
        situation = "最后一句是我。",
        coreInsight = "LAST ME 已经成立，必须先等对方，不能进入上下文不足或云端分析。",
        userLikelyMistake = "继续补话、解释或追问会稀释表达。",
        bestMove = "你已经回过了，先等对方。",
        avoidMoves = listOf("不要追问", "不要追加解释", "不要调用云端分析"),
        coCreationOpportunity = null,
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        influenceProfile = InfluenceProfile(
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.LOW,
            riskWarning = null,
            fallbackMove = "等对方回来再接。"
        ),
        fallbackMove = "如果很久没回，再发一条轻生活关心。"
    )

    private fun unknownSpeakerDecision(reason: String): TacticalDecision = TacticalDecision(
        decisionType = TacticalDecisionType.CONTEXT_REQUIRED,
        situation = "说话人或内容不确定。",
        coreInsight = reason,
        userLikelyMistake = "在没有分清内容来源或含义时生成回复。",
        bestMove = "先补充这条内容的含义，再决定下一句。",
        avoidMoves = listOf("不要调用模型", "不要猜内容", "不要强行深聊"),
        coCreationOpportunity = null,
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        influenceProfile = InfluenceProfile(
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.MEDIUM,
            riskWarning = reason,
            fallbackMove = "先确认这条内容是什么意思。"
        ),
        fallbackMove = "先确认这条内容是什么意思。"
    )

    private fun contaminatedResult(
        capture: CurrentScreenCaptureResult,
        reason: String,
        sessionId: String,
        preAnalysisSnapshotId: String,
        chatPackage: String,
        chatWindowHash: String
    ): CurrentScreenPipelineResult {
        val config = cloudAnalysisService?.config ?: CloudAnalysisConfig(cloudEnabled = false)
        val decision = TacticalDecision(
            decisionType = TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED,
            situation = "没有读到干净聊天页。",
            coreInsight = reason,
            userLikelyMistake = "当前采样像是会意自己的面板，继续分析会污染判断。",
            bestMove = "没读到聊天页，请点一下聊起聊天窗口后再试。",
            avoidMoves = listOf("不要生成路线", "不要调用云端", "不要使用本地 fallback 路线"),
            coCreationOpportunity = null,
            shouldUseUserStory = false,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(
                intensity = InfluenceIntensity.LOW,
                riskLevel = RiskLevel.MEDIUM,
                riskWarning = reason,
                fallbackMove = "回到聊起聊天窗口后再点下一句。"
            ),
            fallbackMove = "回到聊起聊天窗口后再点下一句。"
        )
        return CurrentScreenPipelineResult(
            captureResult = capture,
            context = null,
            lastSpeakerDecision = LastSpeakerDecision(
                lastEffectiveMessage = null,
                lastSpeaker = null,
                shouldReply = false,
                reason = reason,
                unknownSpeaker = true
            ),
            tacticalDecision = decision,
            routes = emptyList(),
            apiCalled = false,
            sessionId = sessionId,
            sessionTerminalState = "CONTROLLED_FAIL",
            routePanelShown = false,
            waitPanelShown = false,
            cloudTrace = CloudAnalysisTrace.skipped(config, "PRE_ANALYSIS_CONTAMINATED", "CONTROLLED_FAIL")
                .withSessionBinding(
                    activeSessionId = sessionId,
                    preAnalysisSnapshotId = preAnalysisSnapshotId,
                    chatPackage = chatPackage,
                    chatWindowHash = chatWindowHash,
                    panelRenderedSessionId = sessionId
                )
        )
    }
}

data class PreAnalysisContaminationResult(
    val contaminated: Boolean,
    val reason: String = ""
)

object PreAnalysisContaminationGuard {
    private val markers = listOf(
        "没读到当前聊天",
        "没读到聊天",
        "请回到聊起聊天窗口",
        "这次不对，发给 GPT",
        "这次不对",
        "会意雷达",
        "隐藏",
        "云端未就绪",
        "本地建议",
        "会意云端分析",
        "正在上传 GitHub",
        "Huiyi Radar",
        "send to GPT",
        "浼氭剰",
        "杩欐涓嶅",
        "闅愯棌",
        "娌¤鍒"
    )

    fun inspect(capture: CurrentScreenCaptureResult): PreAnalysisContaminationResult {
        val title = capture.snapshot.windowTitle.orEmpty()
        val titleHit = markers.firstOrNull { title.contains(it, ignoreCase = true) }
        if (titleHit != null) {
            return PreAnalysisContaminationResult(true, "preAnalysis window title looks like Huiyi panel: $titleHit")
        }
        val text = capture.snapshot.nodes.asSequence()
            .mapNotNull { it.readableText }
            .plus(capture.messages.asSequence().mapNotNull { it.normalizedText })
            .joinToString(" ")
            .take(1000)
        val textHit = markers.firstOrNull { text.contains(it, ignoreCase = true) }
        return if (textHit != null) {
            PreAnalysisContaminationResult(true, "preAnalysis node text looks like Huiyi panel: $textHit")
        } else {
            PreAnalysisContaminationResult(false)
        }
    }
}
