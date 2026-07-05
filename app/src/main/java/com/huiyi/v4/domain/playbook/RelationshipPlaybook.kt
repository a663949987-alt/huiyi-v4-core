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
            return listOf(route("wait", "先等对方", ReplyRouteType.WAIT, "你已经回过了，先等对方。", RiskLevel.LOW))
        }
        return listOf(
            route("receive", "接住情绪", ReplyRouteType.EMPATHY, "嗯，我懂你这个意思。你先按自己的节奏来，不用急着解释清楚。", RiskLevel.LOW),
            route("steady", "稳住节奏", ReplyRouteType.STABLE, "先把眼前这件事处理好就行，我在，不用一下子想太多。", RiskLevel.LOW),
            route("light-question", "轻轻追问", ReplyRouteType.DIRECT, "你现在更在意的是事情本身，还是怕别人没理解你？", RiskLevel.LOW),
            route("warm", "轻微升温", ReplyRouteType.WARM_UP, "其实你愿意跟我说这些，我是有感觉到的。", RiskLevel.MEDIUM),
            route("withdraw", "低压撤退", ReplyRouteType.COOL_DOWN, "那我先不追着问了，你舒服一点的时候我们再慢慢说。", risk)
        )
    }

    private fun activeExpressionRoutes(
        arcPlan: PlaybookCharacterArcPlan,
        personaCorpus: UserPersonaCorpus,
        risk: RiskLevel
    ): List<ReplyRoute> {
        val arcLine = arcPlan.suggestedLine ?: personaCorpus.characterArcCards.firstOrNull()?.safeRevealLine
        val arcRoutes = if (arcLine != null && arcPlan.exists) {
            listOf(route("arc-reveal", "人物弧光", ReplyRouteType.ARC_REVEAL, arcLine, RiskLevel.MEDIUM))
        } else {
            emptyList()
        }
        return (arcRoutes + listOf(
            route("express-self", "表达我", ReplyRouteType.SELF_STORY, "我也挺认同这个。对我来说，很多事不是嘴上说得满就行，我更愿意一步一步走稳。", RiskLevel.LOW),
            route("co-create", "共创节奏", ReplyRouteType.CO_CREATION, "那我们也别急着把答案定死，可以先找一个彼此都舒服、也能长期走下去的节奏。", RiskLevel.LOW),
            route("lighten", "放轻一点", ReplyRouteType.WARM_UP, "我先不把话说重，简单点说就是：我会认真看待，也会慢慢做给你看。", RiskLevel.LOW),
            route("safe-withdraw", "撤退收口", ReplyRouteType.COOL_DOWN, "如果现在聊这个有点重，我先收一下，等你觉得合适的时候我们再继续。", risk)
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
            fallback = "只露出一点真实底色，然后回到对方的话题上"
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
        RiskLevel.HIGH -> "先停一下，把压力降下来。"
        RiskLevel.MEDIUM -> "说短一点，不要连续解释自己。"
        RiskLevel.LOW -> "保持轻一点，让对方容易接。"
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
        riskWarning = if (risk == RiskLevel.HIGH) "压力偏高，不要继续推进。" else null,
        expectedEffect = "预案缓存里的可发关系动作",
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
