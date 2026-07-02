package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.InfluenceProfile
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.RiskLevel
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
    val persistenceError: String? = null
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
            val unknownRatio = capture.messages.count { it.speaker == com.huiyi.v4.domain.model.Speaker.UNKNOWN }
                .toFloat() / capture.messages.size.coerceAtLeast(1)
            val unknownTooHigh = unknownRatio > 0.30f
            val decision = when {
                unknownTooHigh -> unknownSpeakerDecision("UNKNOWN 说话人超过 30%，不允许高置信度生成。")
                lastSpeaker.unknownSpeaker -> unknownSpeakerDecision(lastSpeaker.reason)
                else -> decisionEngine.decide(context)
            }
            val routes = if (decision.decisionType == TacticalDecisionType.WAIT || lastSpeaker.unknownSpeaker || unknownTooHigh) {
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
        situation = "说话人不确定。",
        coreInsight = reason,
        userLikelyMistake = "在没分清是谁说的时候生成回复。",
        bestMove = "切换我的气泡方向，或补充这句是谁说的。",
        avoidMoves = listOf("不要调用模型", "不要猜说话人"),
        coCreationOpportunity = null,
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        influenceProfile = InfluenceProfile(
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.MEDIUM,
            riskWarning = "说话人不确定，判断可能反向。",
            fallbackMove = "先确认这句是谁说的。"
        ),
        fallbackMove = "先确认这句是谁说的。"
    )
}
