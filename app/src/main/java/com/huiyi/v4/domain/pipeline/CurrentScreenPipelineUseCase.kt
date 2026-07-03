package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.data.HuiyiPersistenceRepository
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
    val mainActivityOpened: Boolean = false
)

class CurrentScreenPipelineUseCase(
    private val captureUseCase: CurrentScreenCaptureUseCase,
    private val contextAssembler: ContextAssembler = ContextAssembler(),
    private val lastSpeakerDecisionUseCase: LastSpeakerDecisionUseCase = LastSpeakerDecisionUseCase(),
    private val decisionEngine: TacticalDecisionEngine = TacticalDecisionEngine(),
    private val routeGenerator: ReplyRouteGenerator = ReplyRouteGenerator(),
    private val persistenceRepository: HuiyiPersistenceRepository? = null
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
            val hasMissingVisualLastMessage = lastSpeaker.lastEffectiveMessage?.content.let {
                it is MessageContent.Image || it is MessageContent.Sticker
            }
            val decision = when {
                unknownTooHigh -> unknownSpeakerDecision("UNKNOWN 说话人超过 30%，不允许高置信度生成。")
                hasUnknownChatNode -> unknownSpeakerDecision("当前屏幕存在边界不清的聊天气泡，不允许高置信度生成。")
                hasMissingVisualLastMessage -> unknownSpeakerDecision("最后一条是未描述的图片/表情，需要先补充画面含义。")
                lastSpeaker.unknownSpeaker -> unknownSpeakerDecision(lastSpeaker.reason)
                else -> decisionEngine.decide(context)
            }
            val routes = if (
                decision.decisionType == TacticalDecisionType.WAIT ||
                decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED ||
                lastSpeaker.unknownSpeaker ||
                unknownTooHigh ||
                hasUnknownChatNode ||
                hasMissingVisualLastMessage
            ) {
                emptyList()
            } else {
                routeGenerator.generate(context, decision)
            }
            val persistenceError = persistenceRepository?.saveScene(context)?.exceptionOrNull()?.message
            CurrentScreenPipelineResult(
                captureResult = capture,
                context = context,
                lastSpeakerDecision = lastSpeaker,
                tacticalDecision = decision,
                routes = routes,
                apiCalled = false,
                persistenceError = persistenceError
            )
        }
    }

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
