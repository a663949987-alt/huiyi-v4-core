package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
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
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine

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
    val cloudTrace: CloudAnalysisTrace = CloudAnalysisTrace()
)

class CurrentScreenPipelineUseCase(
    private val captureUseCase: CurrentScreenCaptureUseCase,
    private val contextAssembler: ContextAssembler = ContextAssembler(),
    private val lastSpeakerDecisionUseCase: LastSpeakerDecisionUseCase = LastSpeakerDecisionUseCase(),
    private val decisionEngine: TacticalDecisionEngine = TacticalDecisionEngine(),
    private val routeGenerator: ReplyRouteGenerator = ReplyRouteGenerator(),
    private val persistenceRepository: HuiyiPersistenceRepository? = null,
    private val cloudAnalysisService: CloudAnalysisService? = null,
    private val appVersionName: String = "",
    private val appVersionCode: Int = 0
) {
    suspend fun run(userPersonaCorpus: UserPersonaCorpus): Result<CurrentScreenPipelineResult> {
        return captureUseCase.capture().mapCatching { capture ->
            val lastSpeaker = lastSpeakerDecisionUseCase.decide(capture.messages)
            val context = contextAssembler.assemble(
                currentScreenMessages = capture.messages,
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
            val decision = when {
                lastSpeaker.lastSpeaker == Speaker.ME -> decisionEngine.decide(context)
                unknownTooHigh -> unknownSpeakerDecision("UNKNOWN 说话人超过 30%，不允许高置信度生成。")
                hasVisualConflict -> unknownSpeakerDecision("当前屏幕存在 Accessibility 与视觉投影冲突，需要先看 visual debug 图。")
                hasUnknownChatNode -> unknownSpeakerDecision("当前屏幕存在边界不清的聊天气泡，不允许高置信度生成。")
                hasMissingVisualLastMessage -> unknownSpeakerDecision("最后一条是未描述的图片/表情，需要先补充画面含义。")
                lastSpeaker.unknownSpeaker -> unknownSpeakerDecision(lastSpeaker.reason)
                else -> decisionEngine.decide(context)
            }
            val localRoutes = if (
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
                capture = capture,
                context = context,
                lastSpeaker = lastSpeaker,
                localDecision = decision,
                localRoutes = localRoutes,
                targetSupported = targetSupported
            )
            val persistenceError = persistenceRepository?.saveScene(context)?.exceptionOrNull()?.message
            CurrentScreenPipelineResult(
                captureResult = capture,
                context = context,
                lastSpeakerDecision = lastSpeaker,
                tacticalDecision = cloudResult.decision,
                routes = cloudResult.routes,
                apiCalled = cloudResult.trace.apiCalled,
                persistenceError = persistenceError,
                cloudTrace = cloudResult.trace
            )
        }
    }

    private suspend fun maybeAnalyzeWithCloud(
        capture: CurrentScreenCaptureResult,
        context: ChatSceneContext,
        lastSpeaker: LastSpeakerDecision,
        localDecision: TacticalDecision,
        localRoutes: List<ReplyRoute>,
        targetSupported: Boolean
    ): CloudPipelineResult {
        val service = cloudAnalysisService
        val config = service?.config ?: CloudAnalysisConfig(cloudEnabled = false)
        if (lastSpeaker.lastSpeaker == Speaker.ME) {
            return CloudPipelineResult(localDecision, emptyList(), CloudAnalysisTrace.skipped(config, "LAST_SPEAKER_ME_WAIT", "LOCAL_WAIT"))
        }
        if (lastSpeaker.lastSpeaker == Speaker.UNKNOWN || lastSpeaker.unknownSpeaker) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "LAST_SPEAKER_UNKNOWN", "LOCAL_FALLBACK"))
        }
        if (!targetSupported) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "UNSUPPORTED_APP", "LOCAL_FALLBACK"))
        }
        if (service == null || !config.configuredAndEnabled) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "CLOUD_NOT_CONFIGURED", "LOCAL_FALLBACK"))
        }
        if (lastSpeaker.lastSpeaker != Speaker.OTHER || localRoutes.size != 5) {
            return CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.skipped(config, "LOCAL_CONTEXT_REQUIRED", "LOCAL_FALLBACK"))
        }
        val startedAt = System.currentTimeMillis()
        return service.analyze(
            CloudAnalysisInput(
                sessionId = java.util.UUID.randomUUID().toString(),
                appVersionName = appVersionName,
                appVersionCode = appVersionCode,
                capture = capture,
                context = context,
                lastSpeakerDecision = lastSpeaker,
                localDecision = localDecision
            )
        ).fold(
            onSuccess = { output ->
                CloudPipelineResult(output.decision, output.routes, CloudAnalysisTrace.success(config, output.cloudRequestId, output.latencyMs))
            },
            onFailure = { error ->
                val code = (error as? CloudAnalysisException)?.code ?: "NETWORK"
                CloudPipelineResult(localDecision, localRoutes, CloudAnalysisTrace.fallback(config, code, System.currentTimeMillis() - startedAt))
            }
        )
    }

    private data class CloudPipelineResult(
        val decision: TacticalDecision,
        val routes: List<ReplyRoute>,
        val trace: CloudAnalysisTrace
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
}
