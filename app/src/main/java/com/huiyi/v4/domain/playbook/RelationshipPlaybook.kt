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
    val expressionLedger: ExpressionLedger = ExpressionLedger.empty(),
    val expressionModeSelection: ExpressionModeSelection? = null,
    val arcThemes: List<ArcTheme> = FixedArcThemes.all,
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

class RelationshipPlaybookGenerator(
    private val expressionModeSelector: ExpressionModeSelector = ExpressionModeSelector()
) {
    fun generate(
        lightChatState: LightChatStableSnapshot,
        compression: ConversationStateCompression,
        arcProgressState: ArcProgressState,
        characterArcPlannerOutput: ArcProgressState = arcProgressState,
        personaCorpus: UserPersonaCorpus,
        expressionLedger: ExpressionLedger = ExpressionLedger.empty(),
        nowMillis: Long = System.currentTimeMillis()
    ): RelationshipPlaybook {
        val stage = inferStage(lightChatState, compression)
        val risk = inferRisk(lightChatState, compression)
        val arcPlan = characterArcPlan(characterArcPlannerOutput)
        val expressionModeSelection = expressionModeSelector.select(
            currentTopics = compression.currentTopics + compression.expressionTriggerTopics,
            recentMessages = compression.recentMessages,
            arcProgressState = arcProgressState,
            expressionLedger = expressionLedger,
            characterArcPlannerOutput = characterArcPlannerOutput,
            nowMillis = nowMillis
        )
        val passiveRoutes = passiveRoutes(lightChatState, risk)
        val activeRoutes = activeExpressionRoutes(
            arcPlan = arcPlan,
            personaCorpus = personaCorpus,
            risk = risk,
            selection = expressionModeSelection,
            ledger = expressionLedger
        )
        val branches = nextBranches(passiveRoutes, activeRoutes, risk)

        return RelationshipPlaybook(
            stage = stage,
            currentFrame = currentFrame(stage, compression),
            passiveNext = passiveRoutes,
            activeExpression = activeRoutes,
            characterArcPlan = arcPlan,
            expressionLedger = expressionLedger,
            expressionModeSelection = expressionModeSelection,
            arcThemes = FixedArcThemes.all,
            next2StepBranches = branches,
            risk = risk,
            fallback = fallbackFor(risk),
            expiresWhen = PlaybookExpiry(
                expiresAtMillis = nowMillis + 10 * 60 * 1000,
                expiresWhen = listOf("stage_changed", "topic_changed", "last_speaker_changed", "playbook_ttl_expired")
            ),
            chatKey = lightChatState.chatKey,
            topicHash = topicHash(compression, expressionLedger),
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
            return listOf(route("wait", "\u5148\u7b49\u5bf9\u65b9", ReplyRouteType.WAIT, "\u4f60\u5df2\u7ecf\u56de\u8fc7\u4e86\uff0c\u5148\u7b49\u5bf9\u65b9\u3002", RiskLevel.LOW))
        }
        return listOf(
            route("receive", "\u63a5\u4f4f\u60c5\u7eea", ReplyRouteType.EMPATHY, "\u55ef\uff0c\u6211\u61c2\u4f60\u8fd9\u4e2a\u610f\u601d\u3002\u4f60\u5148\u6309\u81ea\u5df1\u7684\u8282\u594f\u6765\uff0c\u4e0d\u7528\u6025\u7740\u89e3\u91ca\u6e05\u695a\u3002", RiskLevel.LOW),
            route("steady", "\u7a33\u4f4f\u8282\u594f", ReplyRouteType.STABLE, "\u5148\u628a\u773c\u524d\u8fd9\u4ef6\u4e8b\u5904\u7406\u597d\u5c31\u884c\uff0c\u6211\u5728\uff0c\u4e0d\u7528\u4e00\u4e0b\u5b50\u60f3\u592a\u591a\u3002", RiskLevel.LOW),
            route("light-question", "\u8f7b\u8f7b\u8ffd\u95ee", ReplyRouteType.DIRECT, "\u4f60\u73b0\u5728\u66f4\u5728\u610f\u7684\u662f\u4e8b\u60c5\u672c\u8eab\uff0c\u8fd8\u662f\u6015\u522b\u4eba\u6ca1\u7406\u89e3\u4f60\uff1f", RiskLevel.LOW),
            route("warm", "\u8f7b\u5fae\u5347\u6e29", ReplyRouteType.WARM_UP, "\u5176\u5b9e\u4f60\u613f\u610f\u8ddf\u6211\u8bf4\u8fd9\u4e9b\uff0c\u6211\u662f\u6709\u611f\u89c9\u5230\u7684\u3002", RiskLevel.MEDIUM),
            route("withdraw", "\u4f4e\u538b\u64a4\u9000", ReplyRouteType.COOL_DOWN, "\u90a3\u6211\u5148\u4e0d\u8ffd\u7740\u95ee\u4e86\uff0c\u4f60\u8212\u670d\u4e00\u70b9\u7684\u65f6\u5019\u6211\u4eec\u518d\u6162\u6162\u8bf4\u3002", risk)
        )
    }

    private fun activeExpressionRoutes(
        arcPlan: PlaybookCharacterArcPlan,
        personaCorpus: UserPersonaCorpus,
        risk: RiskLevel,
        selection: ExpressionModeSelection,
        ledger: ExpressionLedger
    ): List<ReplyRoute> {
        val entry = ledger.entryFor(selection.selectedTheme.themeId)
        if (selection.expressionMode == ExpressionMode.HOLD_BACK) {
            return listOf(
                route(
                    id = "hold-back",
                    name = "\u64a4\u9000",
                    type = ReplyRouteType.COOL_DOWN,
                    message = "\u8fd9\u4e2a\u70b9\u6211\u5148\u4e0d\u5f80\u81ea\u5df1\u8eab\u4e0a\u8bb2\u4e86\uff0c\u4f60\u521a\u521a\u90a3\u4e2a\u611f\u53d7\u6211\u5148\u63a5\u4f4f\u3002",
                    risk = risk,
                    selection = selection
                )
            ).mapIndexed { index, item -> item.copy(recommended = index == 0) }
        }

        val seedLine = selection.selectedArcCard?.safeRevealLine
            ?: arcPlan.suggestedLine
            ?: personaCorpus.characterArcCards.firstOrNull()?.safeRevealLine
            ?: selection.selectedTheme.routeSeed
        val arcLine = avoidRepeated(
            candidate = seedLine,
            entry = entry,
            fallback = selection.selectedTheme.routeSeed
        )
        val lowPressureLine = avoidRepeated(
            candidate = "\u6211\u4e5f\u633a\u8ba4\u540c\u8fd9\u4e2a\u3002\u5bf9\u6211\u6765\u8bf4\uff0c\u6709\u4e9b\u4e8b\u4e0d\u662f\u5634\u4e0a\u8bf4\u5f97\u6ee1\u5c31\u884c\uff0c\u6211\u66f4\u613f\u610f\u6162\u6162\u628a\u5b83\u843d\u5230\u73b0\u5b9e\u91cc\u3002",
            entry = entry,
            fallback = "\u6211\u8ba4\u540c\u8981\u770b\u73b0\u5b9e\u3002\u6211\u4e0d\u592a\u559c\u6b22\u628a\u8bdd\u8bf4\u5f97\u5f88\u6ee1\uff0c\u4f46\u771f\u8be5\u505a\u7684\u4e8b\uff0c\u6211\u4f1a\u4e00\u70b9\u70b9\u843d\u5230\u5b9e\u5904\u3002"
        )
        val coCreateLine = if (selection.expressionMode == ExpressionMode.ELEVATE_MEANING) {
            "\u90a3\u6211\u4eec\u4e5f\u4e0d\u7528\u6025\u7740\u7ed9\u5b9a\u8bba\uff0c\u53ef\u4ee5\u5148\u627e\u4e00\u4e2a\u5f7c\u6b64\u90fd\u8212\u670d\u3001\u4e5f\u80fd\u957f\u671f\u8d70\u4e0b\u53bb\u7684\u5171\u540c\u8282\u594f\u3002"
        } else {
            "\u90a3\u6211\u4eec\u5148\u522b\u628a\u7b54\u6848\u5b9a\u6b7b\uff0c\u53ef\u4ee5\u4e00\u8fb9\u770b\u73b0\u5b9e\uff0c\u4e00\u8fb9\u627e\u5f7c\u6b64\u90fd\u8212\u670d\u7684\u8282\u594f\u3002"
        }

        return listOf(
            route(
                id = "start-topic",
                name = "\u8f7b\u5f00\u573a",
                type = ReplyRouteType.WARM_UP,
                message = "\u4f60\u521a\u521a\u8bf4\u7684\u8fd9\u4e2a\u70b9\uff0c\u6211\u6709\u70b9\u80fd\u7406\u89e3\u3002\u6211\u60f3\u8f7b\u8f7b\u63a5\u4e00\u53e5\uff0c\u4e0d\u8bf4\u91cd\u3002",
                risk = RiskLevel.LOW,
                selection = selection
            ),
            route(
                id = "low-pressure-expression",
                name = "\u4f4e\u538b\u8868\u8fbe",
                type = ReplyRouteType.SELF_STORY,
                message = lowPressureLine,
                risk = RiskLevel.LOW,
                selection = selection
            ),
            route(
                id = "arc-reveal",
                name = "\u4eba\u7269\u5f27\u5149",
                type = ReplyRouteType.ARC_REVEAL,
                message = arcLine,
                risk = RiskLevel.MEDIUM,
                selection = selection
            ),
            route(
                id = "co-create",
                name = "\u5171\u521b\u5347\u7ef4",
                type = ReplyRouteType.CO_CREATION,
                message = coCreateLine,
                risk = RiskLevel.LOW,
                selection = selection
            ),
            route(
                id = "safe-withdraw",
                name = "\u64a4\u9000",
                type = ReplyRouteType.COOL_DOWN,
                message = "\u5982\u679c\u73b0\u5728\u804a\u8fd9\u4e2a\u6709\u70b9\u91cd\uff0c\u6211\u5148\u6536\u4e00\u4e0b\uff0c\u4e0d\u7ed9\u4f60\u538b\u529b\u3002",
                risk = risk,
                selection = selection
            )
        ).mapIndexed { index, item -> item.copy(recommended = index == 0) }
    }

    private fun avoidRepeated(
        candidate: String,
        entry: ExpressionLedgerEntry?,
        fallback: String
    ): String {
        val lastLine = entry?.lastSurfaceLineRedacted?.trim().orEmpty()
        return if (lastLine.isNotBlank() && candidate.contains(lastLine)) fallback else candidate
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
            fallback = "\u53ea\u9732\u51fa\u4e00\u70b9\u771f\u5b9e\u5e95\u8272\uff0c\u7136\u540e\u56de\u5230\u5bf9\u65b9\u7684\u8bdd\u9898\u4e0a"
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
        RiskLevel.HIGH -> "\u5148\u505c\u4e00\u4e0b\uff0c\u628a\u538b\u529b\u964d\u4e0b\u6765\u3002"
        RiskLevel.MEDIUM -> "\u8bf4\u77ed\u4e00\u70b9\uff0c\u4e0d\u8981\u8fde\u7eed\u89e3\u91ca\u81ea\u5df1\u3002"
        RiskLevel.LOW -> "\u4fdd\u6301\u8f7b\u4e00\u70b9\uff0c\u8ba9\u5bf9\u65b9\u5bb9\u6613\u63a5\u3002"
    }

    private fun topicHash(
        compression: ConversationStateCompression,
        expressionLedger: ExpressionLedger
    ): String {
        val topicPart = (compression.currentTopics + compression.expressionTriggerTopics)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString("|")
            .ifBlank { "general" }
        val ledgerPart = expressionLedger.entries
            .sortedByDescending { it.lastUsedAt }
            .take(3)
            .joinToString(",") { "${it.themeId}:${it.lastExpressionLevel}:${it.otherReaction}:${it.repeatRisk}" }
            .ifBlank { "empty" }
        return "$topicPart|ledger:$ledgerPart"
    }

    private fun route(
        id: String,
        name: String,
        type: ReplyRouteType,
        message: String,
        risk: RiskLevel,
        selection: ExpressionModeSelection? = null
    ): ReplyRoute = ReplyRoute(
        id = "playbook-$id",
        name = name,
        routeType = type,
        tag = "playbook",
        message = message,
        intensity = if (risk == RiskLevel.LOW) InfluenceIntensity.LOW else InfluenceIntensity.MEDIUM,
        riskLevel = risk,
        riskWarning = if (risk == RiskLevel.HIGH) "\u538b\u529b\u504f\u9ad8\uff0c\u4e0d\u8981\u7ee7\u7eed\u63a8\u8fdb\u3002" else null,
        expectedEffect = "\u9884\u6848\u7f13\u5b58\u91cc\u7684\u53ef\u53d1\u5173\u7cfb\u52a8\u4f5c",
        fallbackMove = fallbackFor(risk),
        recommended = false,
        panelExpressionMode = selection?.panelModeLabel,
        panelArcTheme = selection?.selectedTheme?.themeName,
        panelModeReason = selection?.reason,
        panelAvoidLine = selection?.selectedTheme?.avoidLine
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
