package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.context.ConversationStateCompression
import com.huiyi.v4.domain.context.LightChatStableSnapshot
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.UserPersonaCorpus

enum class RelationshipStage {
    EARLY_CONTACT,
    WARMING_UP,
    TRUST_BUILDING,
    PRESSURE_REPAIR,
    BOUNDARY_PAUSE
}

enum class RelationshipPlaybookSource {
    LOCAL_FALLBACK,
    CLOUD_ENHANCED
}

data class RelationshipPlaybook(
    val stage: RelationshipStage,
    val currentFrame: String,
    val passiveNext: List<ReplyRoute>,
    val activeExpression: List<ReplyRoute>,
    val characterArcPlan: PlaybookCharacterArcPlan,
    val next2StepBranches: List<PlaybookBranch>,
    val risk: RiskLevel,
    val fallback: String,
    val expiresWhen: PlaybookExpiry,
    val playbookId: String = "playbook-${nowId()}",
    val chatKey: String? = null,
    val topicHash: String = "",
    val generatedAtMillis: Long = System.currentTimeMillis(),
    val source: RelationshipPlaybookSource = RelationshipPlaybookSource.LOCAL_FALLBACK
)

data class PlaybookCharacterArcPlan(
    val exists: Boolean,
    val nextMoveType: NextMoveType,
    val suggestedFacet: String?,
    val suggestedLine: String?,
    val overdoRisk: String?,
    val triggerTopics: List<String>
)

data class PlaybookBranch(
    val id: String,
    val condition: String,
    val passiveRoute: ReplyRoute,
    val expressRoute: ReplyRoute?,
    val fallback: String
)

data class PlaybookExpiry(
    val expiresAtMillis: Long,
    val expiresWhen: List<String>
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean = nowMillis >= expiresAtMillis
}

class RelationshipPlaybookGenerator {
    fun generate(
        lightChatState: LightChatStableSnapshot,
        compression: ConversationStateCompression,
        arcProgressState: ArcProgressState,
        characterArcPlannerOutput: ArcProgressState = arcProgressState,
        personaCorpus: UserPersonaCorpus,
        nowMillis: Long = System.currentTimeMillis()
    ): RelationshipPlaybook {
        val stage = inferStage(lightChatState, compression)
        val risk = inferRisk(lightChatState, compression)
        val arcPlan = characterArcPlan(characterArcPlannerOutput)
        val passiveRoutes = passiveRoutes(lightChatState, risk)
        val activeRoutes = activeExpressionRoutes(arcPlan, personaCorpus, risk)
        val branches = nextBranches(passiveRoutes, activeRoutes, risk)

        return RelationshipPlaybook(
            stage = stage,
            currentFrame = currentFrame(stage, compression),
            passiveNext = passiveRoutes,
            activeExpression = activeRoutes,
            characterArcPlan = arcPlan,
            next2StepBranches = branches,
            risk = risk,
            fallback = fallbackFor(risk),
            expiresWhen = PlaybookExpiry(
                expiresAtMillis = nowMillis + 10 * 60 * 1000,
                expiresWhen = listOf("stage_changed", "topic_changed", "last_speaker_changed", "playbook_ttl_expired")
            ),
            chatKey = lightChatState.chatKey,
            topicHash = topicHash(compression),
            generatedAtMillis = nowMillis,
            source = RelationshipPlaybookSource.LOCAL_FALLBACK
        )
    }

    private fun inferStage(
        lightChatState: LightChatStableSnapshot,
        compression: ConversationStateCompression
    ): RelationshipStage {
        val text = compression.recentMessages.joinToString(" ") { it.text.orEmpty() }.lowercase()
        return when {
            pressureWords.any { text.contains(it) } -> RelationshipStage.PRESSURE_REPAIR
            lightChatState.lastUserMessage != null &&
                lightChatState.lastOtherMessage != null &&
                lightChatState.lastUserMessage.localSequence > lightChatState.lastOtherMessage.localSequence -> RelationshipStage.BOUNDARY_PAUSE
            compression.expressionTriggerTopics.isNotEmpty() -> RelationshipStage.TRUST_BUILDING
            warmWords.any { text.contains(it) } -> RelationshipStage.WARMING_UP
            else -> RelationshipStage.EARLY_CONTACT
        }
    }

    private fun inferRisk(
        lightChatState: LightChatStableSnapshot,
        compression: ConversationStateCompression
    ): RiskLevel {
        val text = compression.recentMessages.joinToString(" ") { it.text.orEmpty() }.lowercase()
        return when {
            lightChatState.lastUserMessage != null &&
                lightChatState.lastOtherMessage != null &&
                lightChatState.lastUserMessage.localSequence > lightChatState.lastOtherMessage.localSequence -> RiskLevel.MEDIUM
            highRiskWords.any { text.contains(it) } -> RiskLevel.HIGH
            pressureWords.any { text.contains(it) } -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private fun characterArcPlan(arcProgressState: ArcProgressState): PlaybookCharacterArcPlan {
        val window = arcProgressState.currentExpressionWindow
        val card = arcProgressState.suggestedArcCard
        return PlaybookCharacterArcPlan(
            exists = window.exists && card != null,
            nextMoveType = window.nextMoveType,
            suggestedFacet = card?.hiddenDepth,
            suggestedLine = card?.safeRevealLine,
            overdoRisk = arcProgressState.overdoRisk,
            triggerTopics = window.triggerTopics
        )
    }

    private fun passiveRoutes(lightChatState: LightChatStableSnapshot, risk: RiskLevel): List<ReplyRoute> {
        val lastSpeaker = lightChatState.recentEffectiveMessages.lastOrNull()?.speaker
        if (lastSpeaker == Speaker.ME) {
            return listOf(route("wait", "Wait", ReplyRouteType.WAIT, "You already replied. Wait for the other person.", RiskLevel.LOW))
        }
        return listOf(
            route("receive", "Receive", ReplyRouteType.EMPATHY, "I get it, no rush. Say it at your pace.", RiskLevel.LOW),
            route("steady", "Steady", ReplyRouteType.STABLE, "Handle what is in front of you first. I am here.", RiskLevel.LOW),
            route("light-question", "Light question", ReplyRouteType.DIRECT, "Is it the situation itself, or being misunderstood?", RiskLevel.LOW),
            route("warm", "Warmer", ReplyRouteType.WARM_UP, "I actually like that you still told me this.", RiskLevel.MEDIUM),
            route("withdraw", "Withdraw", ReplyRouteType.COOL_DOWN, "Then I will not press. We can talk when it feels easier.", risk)
        )
    }

    private fun activeExpressionRoutes(
        arcPlan: PlaybookCharacterArcPlan,
        personaCorpus: UserPersonaCorpus,
        risk: RiskLevel
    ): List<ReplyRoute> {
        val arcLine = arcPlan.suggestedLine ?: personaCorpus.characterArcCards.firstOrNull()?.safeRevealLine
        val arcRoutes = if (arcLine != null && arcPlan.exists) {
            listOf(route("arc-reveal", "Character arc", ReplyRouteType.ARC_REVEAL, arcLine, RiskLevel.MEDIUM))
        } else {
            emptyList()
        }
        return (arcRoutes + listOf(
            route("express-self", "Express self", ReplyRouteType.SELF_STORY, "I may not say it perfectly, but I do take this seriously.", RiskLevel.LOW),
            route("co-create", "Co-create", ReplyRouteType.CO_CREATION, "Maybe we do not force an answer tonight. We find a pace that both of us can hold.", RiskLevel.LOW),
            route("lighten", "Lighten", ReplyRouteType.WARM_UP, "Then I will keep it simple: take a breath first, I am not going anywhere.", RiskLevel.LOW),
            route("safe-withdraw", "Withdraw", ReplyRouteType.COOL_DOWN, "If this is too much now, I will pause here.", risk)
        )).take(5).mapIndexed { index, item -> item.copy(recommended = index == 0) }
    }

    private fun nextBranches(
        passiveRoutes: List<ReplyRoute>,
        activeRoutes: List<ReplyRoute>,
        risk: RiskLevel
    ): List<PlaybookBranch> = listOf(
        PlaybookBranch(
            id = "other_softens",
            condition = "other replies with warmth or more detail",
            passiveRoute = passiveRoutes.first(),
            expressRoute = activeRoutes.firstOrNull { it.routeType == ReplyRouteType.ARC_REVEAL },
            fallback = "keep one small self-expression, then return to her topic"
        ),
        PlaybookBranch(
            id = "other_pulls_back",
            condition = "other becomes short, busy, or avoidant",
            passiveRoute = passiveRoutes.last(),
            expressRoute = activeRoutes.lastOrNull(),
            fallback = fallbackFor(risk)
        )
    )

    private fun currentFrame(stage: RelationshipStage, compression: ConversationStateCompression): String {
        val topics = compression.currentTopics.ifEmpty { compression.expressionTriggerTopics }
        return "${stage.name.lowercase()} / ${topics.take(3).joinToString(", ").ifBlank { "low_pressure_chat" }}"
    }

    private fun fallbackFor(risk: RiskLevel): String = when (risk) {
        RiskLevel.HIGH -> "pause and lower pressure"
        RiskLevel.MEDIUM -> "keep it short; avoid explaining yourself twice"
        RiskLevel.LOW -> "stay light and keep the next move easy to answer"
    }

    private fun topicHash(compression: ConversationStateCompression): String =
        (compression.currentTopics + compression.expressionTriggerTopics)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString("|")
            .ifBlank { "general" }

    private fun route(
        id: String,
        name: String,
        type: ReplyRouteType,
        message: String,
        risk: RiskLevel
    ): ReplyRoute = ReplyRoute(
        id = "playbook-$id",
        name = name,
        routeType = type,
        tag = "playbook",
        message = message,
        intensity = if (risk == RiskLevel.LOW) InfluenceIntensity.LOW else InfluenceIntensity.MEDIUM,
        riskLevel = risk,
        riskWarning = if (risk == RiskLevel.HIGH) "high pressure; do not push" else null,
        expectedEffect = "precomputed relationship move",
        fallbackMove = fallbackFor(risk),
        recommended = false
    )

    private companion object {
        val pressureWords = listOf(
            "pressure",
            "forced",
            "busy",
            "tired",
            "push",
            "\u6025",
            "\u7d2f",
            "\u538b\u529b",
            "\u9000",
            "\u70e6"
        )
        val highRiskWords = listOf(
            "angry",
            "divorce",
            "child",
            "fight",
            "break up",
            "\u79bb\u5a5a",
            "\u5b69\u5b50",
            "\u5435",
            "\u5206\u624b"
        )
        val warmWords = listOf(
            "miss",
            "like",
            "care",
            "\u60f3\u4f60",
            "\u559c\u6b22",
            "\u5728\u610f"
        )
    }
}

private fun nowId(): String = System.currentTimeMillis().toString(36)
