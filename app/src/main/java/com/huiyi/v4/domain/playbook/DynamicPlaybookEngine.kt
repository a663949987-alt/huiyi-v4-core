package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.context.CharacterArcPlanner
import com.huiyi.v4.domain.context.ConversationStateCompression
import com.huiyi.v4.domain.context.ConversationStateCompressor
import com.huiyi.v4.domain.context.LightChatStableSnapshot
import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.pipeline.LastSpeakerDecision
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import kotlin.system.measureTimeMillis

enum class DynamicPlaybookMode {
    NEXT_SENTENCE,
    EXPRESS_SELF
}

data class DynamicPlaybookRequest(
    val mode: DynamicPlaybookMode,
    val appPackage: String?,
    val windowTitle: String?,
    val messages: List<MessageNode>,
    val personaCorpus: UserPersonaCorpus,
    val capturedAt: Long = System.currentTimeMillis(),
    val currentTopics: List<String> = emptyList(),
    val expressionLedger: ExpressionLedger = ExpressionLedger.empty(),
    val sessionId: String? = null,
    val chatWindowHash: String? = null,
    val targetAppSupported: Boolean? = null,
    val snapshotTrusted: Boolean = true,
    val currentAppPackage: String? = appPackage,
    val currentWindowTitleRedacted: String? = windowTitle,
    val parserConfidence: Int = 0,
    val lastUserMessageAgeMsOverride: Long? = null,
    val chatInactiveMsOverride: Long? = null,
    val recentSelfExpressionCountOverride: Int? = null,
    val repeatRiskOverride: String? = null,
    val preAnalysisSnapshotSource: String = "CURRENT_ROOT_BEFORE_PANEL"
)

data class DynamicPlaybookResult(
    val mode: DynamicPlaybookMode,
    val snapshot: LightChatStableSnapshot,
    val cacheKey: PlaybookCacheKey,
    val playbook: RelationshipPlaybook,
    val lastSpeakerDecision: LastSpeakerDecision,
    val tacticalDecisionType: TacticalDecisionType,
    val routes: List<ReplyRoute>,
    val cacheHit: Boolean,
    val localFallbackUsed: Boolean,
    val cloudRefreshRecommended: Boolean,
    val cloudRefreshAttempted: Boolean,
    val refreshTriggers: List<PlaybookRefreshTrigger>,
    val compression: ConversationStateCompression,
    val arcProgressState: ArcProgressState,
    val nextMoveType: NextMoveType,
    val panelNextAction: String,
    val latencyMs: Long,
    val decisionSource: String,
    val expressionModeSelection: ExpressionModeSelection? = null,
    val expressSelfEligibility: ExpressSelfEligibility? = null,
    val passiveRouteDisplaySource: String = "NONE",
    val localPassiveRoutesGenerated: Boolean = false,
    val localPassiveRoutesShownToUser: Boolean = false,
    val passiveWaitPanelShown: Boolean = false,
    val cloudPlaybookAvailable: Boolean = false,
    val cloudPlaybookAgeMs: Long? = null,
    val expressSelfFeedbackDefaultVisible: Boolean = false,
    val expressSelfFeedbackCollapsed: Boolean = true,
    val expressSelfDefaultRouteCount: Int = 0,
    val expressSelfPanelSimpleMode: Boolean = false,
    val expressSelfResultCacheHit: Boolean = false,
    val expressSelfRepeatClickCount: Int = 0,
    val expressSelfSameSceneStable: Boolean = true,
    val expressSelfReusedPreviousResult: Boolean = false,
    val expressSelfRepeatBlockedReason: String? = null
) {
    val oneClickImmediateResultPass: Boolean
        get() = latencyMs <= 300 && (
            tacticalDecisionType == TacticalDecisionType.WAIT ||
                tacticalDecisionType == TacticalDecisionType.PASSIVE_NOT_READY ||
                routes.isNotEmpty()
            )
}

class DynamicPlaybookEngine(
    private val playbookCache: PlaybookCache = PlaybookCache(),
    private val lightChatStateStore: LightChatStateStore = LightChatStateStore(),
    private val compressor: ConversationStateCompressor = ConversationStateCompressor(),
    private val arcPlanner: CharacterArcPlanner = CharacterArcPlanner(compressor),
    private val generator: RelationshipPlaybookGenerator = RelationshipPlaybookGenerator(),
    private val lastSpeakerDecisionUseCase: LastSpeakerDecisionUseCase = LastSpeakerDecisionUseCase(),
    private val expressSelfEligibilityEvaluator: ExpressSelfEligibilityEvaluator = ExpressSelfEligibilityEvaluator(),
    private val expressSelfResultCache: ExpressSelfResultCache = ExpressSelfResultCache(),
    private val outputQualityGate: HuiyiOutputQualityGate = HuiyiOutputQualityGate()
) {
    fun nextSentence(request: DynamicPlaybookRequest): DynamicPlaybookResult =
        resolve(request.copy(mode = DynamicPlaybookMode.NEXT_SENTENCE))

    fun expressSelf(request: DynamicPlaybookRequest): DynamicPlaybookResult =
        resolve(request.copy(mode = DynamicPlaybookMode.EXPRESS_SELF))

    fun cache(playbook: RelationshipPlaybook): PlaybookCacheKey {
        val key = PlaybookCacheKey(
            chatKey = playbook.chatKey.orEmpty().ifBlank { "unknown_chat" },
            stage = playbook.stage,
            topicHash = playbook.topicHash.ifBlank { "general" }
        )
        playbookCache.put(key, playbook)
        return key
    }

    private fun resolve(request: DynamicPlaybookRequest): DynamicPlaybookResult {
        var result: DynamicPlaybookResult? = null
        val elapsed = measureTimeMillis {
            result = resolveInternal(request)
        }
        return result!!.copy(latencyMs = elapsed)
    }

    private fun resolveInternal(request: DynamicPlaybookRequest): DynamicPlaybookResult {
        val snapshot = lightChatStateStore.buildStableSnapshot(
            appPackage = request.appPackage,
            windowTitle = request.windowTitle,
            messages = request.messages,
            capturedAt = request.capturedAt,
            nextSentenceSessionId = request.sessionId,
            preAnalysisSnapshotId = request.capturedAt.toString(),
            panelSessionId = request.sessionId,
            chatWindowHash = request.chatWindowHash,
            characterArcCards = request.personaCorpus.characterArcCards
        )
        val topics = (request.currentTopics + inferTopics(snapshot)).distinct()
        val compression = compressor.compress(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = topics,
            personaCorpus = request.personaCorpus,
            characterArcCards = request.personaCorpus.characterArcCards
        )
        val arcProgress = arcPlanner.plan(
            recentMessages = snapshot.recentEffectiveMessages,
            lastUserMessage = snapshot.lastUserMessage,
            lastOtherMessage = snapshot.lastOtherMessage,
            currentTopics = topics,
            personaCorpus = request.personaCorpus,
            characterArcCards = request.personaCorpus.characterArcCards
        )
        val localPlaybook = generator.generate(
            lightChatState = snapshot,
            compression = compression,
            arcProgressState = arcProgress,
            personaCorpus = request.personaCorpus,
            expressionLedger = request.expressionLedger,
            nowMillis = request.capturedAt
        ).copy(chatKey = playbookChatKey(request, snapshot))
        val cacheKey = PlaybookCacheKey(
            chatKey = localPlaybook.chatKey.orEmpty().ifBlank { "unknown_chat" },
            stage = localPlaybook.stage,
            topicHash = localPlaybook.topicHash.ifBlank { "general" }
        )
        val cached = playbookCache.get(cacheKey, request.capturedAt)
        val playbook = cached ?: localPlaybook.also { playbookCache.put(cacheKey, it) }
        val cacheHit = cached != null
        val lastSpeaker = lastSpeakerDecisionUseCase.decide(request.messages)
        val refreshTriggers = refreshTriggers(request, snapshot, localPlaybook, cacheHit)

        return when (request.mode) {
            DynamicPlaybookMode.NEXT_SENTENCE -> nextSentenceResult(
                request = request,
                snapshot = snapshot,
                cacheKey = cacheKey,
                playbook = playbook,
                lastSpeaker = lastSpeaker,
                cacheHit = cacheHit,
                localFallbackUsed = !cacheHit,
                refreshTriggers = refreshTriggers,
                compression = compression,
                arcProgress = arcProgress
            )

            DynamicPlaybookMode.EXPRESS_SELF -> expressSelfResult(
                request = request,
                snapshot = snapshot,
                cacheKey = cacheKey,
                playbook = playbook,
                lastSpeaker = lastSpeaker,
                cacheHit = cacheHit,
                localFallbackUsed = !cacheHit,
                refreshTriggers = refreshTriggers,
                compression = compression,
                arcProgress = arcProgress
            )
        }
    }

    private fun nextSentenceResult(
        request: DynamicPlaybookRequest,
        snapshot: LightChatStableSnapshot,
        cacheKey: PlaybookCacheKey,
        playbook: RelationshipPlaybook,
        lastSpeaker: LastSpeakerDecision,
        cacheHit: Boolean,
        localFallbackUsed: Boolean,
        refreshTriggers: List<PlaybookRefreshTrigger>,
        compression: ConversationStateCompression,
        arcProgress: ArcProgressState
    ): DynamicPlaybookResult {
        if (lastSpeaker.lastSpeaker == Speaker.ME) {
            return DynamicPlaybookResult(
                mode = DynamicPlaybookMode.NEXT_SENTENCE,
                snapshot = snapshot,
                cacheKey = cacheKey,
                playbook = playbook,
                lastSpeakerDecision = lastSpeaker,
                tacticalDecisionType = TacticalDecisionType.WAIT,
                routes = emptyList(),
                cacheHit = cacheHit,
                localFallbackUsed = false,
                cloudRefreshRecommended = false,
                cloudRefreshAttempted = false,
                refreshTriggers = emptyList(),
                compression = compression,
                arcProgressState = arcProgress,
                nextMoveType = NextMoveType.WAIT,
                panelNextAction = "WAIT",
                latencyMs = 0L,
                decisionSource = "LOCAL_WAIT",
                expressionModeSelection = null
            )
        }
        val localPassiveGenerated = playbook.passiveNext.any { it.routeType !in activeOnlyRouteTypes }
        val cloudPlaybookAvailable = playbook.source == RelationshipPlaybookSource.CLOUD_ENHANCED
        val passiveRoutes = if (cloudPlaybookAvailable) {
            val traced = playbook.passiveNext
                .filterNot { it.routeType in activeOnlyRouteTypes }
                .map { route ->
                    route.copy(
                        routeSource = HuiyiOutputQualityGate.SOURCE_CLOUD_VERIFIED_PASSIVE_NEXT,
                        generatorName = route.generatorName.ifBlank { "CloudRelationshipPlaybookMapper" },
                        modelName = route.modelName.ifBlank { playbook.cloudModelTrace.actualModel },
                        promptVersion = route.promptVersion.ifBlank { "relationship-playbook-cloud-v1" },
                        playbookId = playbook.playbookId,
                        cacheSource = "CLOUD_VERIFIED_PLAYBOOK_CACHE"
                    )
                }
            outputQualityGate.visibleRoutes(
                routes = traced,
                requestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK,
                sceneTags = request.currentTopics + inferTopics(snapshot)
            )
                .take(5)
                .mapIndexed { index, route -> route.copy(recommended = index == 0) }
        } else {
            emptyList()
        }
        val cloudPlaybookAgeMs = if (cloudPlaybookAvailable) {
            (request.capturedAt - playbook.generatedAtMillis).coerceAtLeast(0L)
        } else {
            null
        }
        return DynamicPlaybookResult(
            mode = DynamicPlaybookMode.NEXT_SENTENCE,
            snapshot = snapshot,
            cacheKey = cacheKey,
            playbook = playbook,
            lastSpeakerDecision = lastSpeaker,
            tacticalDecisionType = if (passiveRoutes.isEmpty()) TacticalDecisionType.PASSIVE_NOT_READY else TacticalDecisionType.NORMAL_REPLY,
            routes = passiveRoutes,
            cacheHit = cacheHit,
            localFallbackUsed = localFallbackUsed,
            cloudRefreshRecommended = request.messages.isNotEmpty() && lastSpeaker.lastSpeaker == Speaker.OTHER,
            cloudRefreshAttempted = false,
            refreshTriggers = refreshTriggers,
            compression = compression,
            arcProgressState = arcProgress,
            nextMoveType = NextMoveType.RECEIVE_OTHER,
            panelNextAction = if (passiveRoutes.isEmpty()) "PASSIVE_WAIT" else "RECEIVE_OTHER",
            latencyMs = 0L,
            decisionSource = if (cloudPlaybookAvailable) "CLOUD_ENHANCED_PLAYBOOK" else "PASSIVE_WAIT_FOR_CLOUD_PLAYBOOK",
            expressionModeSelection = null,
            passiveRouteDisplaySource = if (cloudPlaybookAvailable) "CLOUD_VERIFIED_PASSIVE_NEXT" else "NONE",
            localPassiveRoutesGenerated = localPassiveGenerated,
            localPassiveRoutesShownToUser = false,
            passiveWaitPanelShown = passiveRoutes.isEmpty(),
            cloudPlaybookAvailable = cloudPlaybookAvailable,
            cloudPlaybookAgeMs = cloudPlaybookAgeMs
        )
    }

    private fun expressSelfResult(
        request: DynamicPlaybookRequest,
        snapshot: LightChatStableSnapshot,
        cacheKey: PlaybookCacheKey,
        playbook: RelationshipPlaybook,
        lastSpeaker: LastSpeakerDecision,
        cacheHit: Boolean,
        localFallbackUsed: Boolean,
        refreshTriggers: List<PlaybookRefreshTrigger>,
        compression: ConversationStateCompression,
        arcProgress: ArcProgressState
    ): DynamicPlaybookResult {
        val cacheLookupKey = ExpressSelfResultCacheKey.from(
            request = request,
            playbook = playbook,
            eligibilityMode = null
        )
        expressSelfResultCache.getSameScene(cacheLookupKey, request.capturedAt)?.let { cached ->
            return cached.result.copy(
                cacheHit = true,
                latencyMs = 0L,
                expressSelfResultCacheHit = true,
                expressSelfRepeatClickCount = cached.repeatClickCount,
                expressSelfSameSceneStable = true,
                expressSelfReusedPreviousResult = true,
                expressSelfRepeatBlockedReason = null
            )
        }
        val eligibility = expressSelfEligibilityEvaluator.evaluate(
            request = request,
            snapshot = snapshot,
            lastSpeakerDecision = lastSpeaker,
            playbook = playbook,
            arcProgress = arcProgress
        )
        if (!eligibility.eligible) {
            val decisionType = when (eligibility.mode) {
                ExpressSelfEligibilityMode.HOLD_BACK,
                ExpressSelfEligibilityMode.BLOCK_RECENT_LAST_ME -> TacticalDecisionType.HOLD_BACK
                ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT -> TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED
                ExpressSelfEligibilityMode.BLOCK_UNSUPPORTED_CONTEXT,
                ExpressSelfEligibilityMode.BLOCK_NO_CHAT_STATE -> TacticalDecisionType.CHAT_WINDOW_NOT_FOUND
                else -> TacticalDecisionType.HOLD_BACK
            }
            val blocked = DynamicPlaybookResult(
                mode = DynamicPlaybookMode.EXPRESS_SELF,
                snapshot = snapshot,
                cacheKey = cacheKey,
                playbook = playbook,
                lastSpeakerDecision = lastSpeaker,
                tacticalDecisionType = decisionType,
                routes = emptyList(),
                cacheHit = cacheHit,
                localFallbackUsed = false,
                cloudRefreshRecommended = false,
                cloudRefreshAttempted = false,
                refreshTriggers = emptyList(),
                compression = compression,
                arcProgressState = arcProgress,
                nextMoveType = NextMoveType.WITHDRAW,
                panelNextAction = eligibility.mode.name,
                latencyMs = 0L,
                decisionSource = "EXPRESS_SELF_ELIGIBILITY_BLOCKED",
                expressionModeSelection = playbook.expressionModeSelection,
                expressSelfEligibility = eligibility,
                expressSelfFeedbackDefaultVisible = false,
                expressSelfFeedbackCollapsed = true,
                expressSelfDefaultRouteCount = 0,
                expressSelfPanelSimpleMode = true
            )
            if (eligibility.mode in setOf(
                    ExpressSelfEligibilityMode.HOLD_BACK,
                    ExpressSelfEligibilityMode.BLOCK_RECENT_LAST_ME
                )
            ) {
                expressSelfResultCache.put(
                    key = cacheLookupKey.copy(eligibilityMode = eligibility.mode.name),
                    result = blocked,
                    nowMillis = request.capturedAt
                )
            }
            return blocked
        }
        val selectedActiveRoutes = playbook.activeExpression
            .ifEmpty {
                generator.generate(
                    lightChatState = snapshot,
                    compression = compression,
                    arcProgressState = arcProgress,
                    personaCorpus = request.personaCorpus,
                    expressionLedger = request.expressionLedger,
                    nowMillis = request.capturedAt
                ).activeExpression
            }
        val activeRoutes = prioritizeExpressSelfRoutes(
            routes = selectedActiveRoutes,
            sceneTags = request.currentTopics + inferTopics(snapshot)
        )
            .map { route ->
                route.copy(
                    routeSource = route.routeSource.ifBlank { HuiyiOutputQualityGate.SOURCE_EXPRESS_SELF_ARC_PLANNER },
                    generatorName = route.generatorName.ifBlank { "RelationshipPlaybookGenerator" },
                    modelName = route.modelName.ifBlank {
                        if (playbook.source == RelationshipPlaybookSource.CLOUD_ENHANCED) playbook.cloudModelTrace.actualModel else ""
                    },
                    promptVersion = route.promptVersion.ifBlank { "express-self-arc-planner-v1" },
                    playbookId = playbook.playbookId,
                    cacheSource = route.cacheSource.ifBlank {
                        if (playbook.source == RelationshipPlaybookSource.CLOUD_ENHANCED) {
                            "CLOUD_ENHANCED_PLAYBOOK_CACHE"
                        } else {
                            "LOCAL_ARC_PLANNER"
                        }
                    }
                )
            }
            .let { traced ->
                outputQualityGate.visibleRoutes(
                    routes = traced,
                    requestPurpose = CloudRequestPurpose.ACTIVE_EXPRESSION,
                    sceneTags = request.currentTopics + inferTopics(snapshot)
                )
            }
            .let { visible ->
                val setResult = outputQualityGate.assessRouteSet(
                    routes = visible,
                    requestPurpose = CloudRequestPurpose.ACTIVE_EXPRESSION,
                    sceneTags = request.currentTopics + inferTopics(snapshot)
                )
                if (setResult.pass) visible else emptyList()
            }
            .take(EXPRESS_SELF_DEFAULT_ROUTE_LIMIT)
            .mapIndexed { index, route -> route.copy(recommended = index == 0) }
        val selectedMode = playbook.expressionModeSelection?.expressionMode
        val nextMove = when {
            selectedMode == ExpressionMode.HOLD_BACK -> NextMoveType.WITHDRAW
            activeRoutes.any { it.routeType == ReplyRouteType.ARC_REVEAL } -> NextMoveType.ARC_REVEAL
            activeRoutes.any { it.routeType == ReplyRouteType.CO_CREATION } -> NextMoveType.CO_CREATE_MEANING
            activeRoutes.any { it.routeType == ReplyRouteType.COOL_DOWN } -> NextMoveType.WITHDRAW
            else -> NextMoveType.EXPRESS_SELF
        }
        val result = DynamicPlaybookResult(
            mode = DynamicPlaybookMode.EXPRESS_SELF,
            snapshot = snapshot,
            cacheKey = cacheKey,
            playbook = playbook,
            lastSpeakerDecision = lastSpeaker,
            tacticalDecisionType = TacticalDecisionType.NORMAL_REPLY,
            routes = activeRoutes,
            cacheHit = cacheHit,
            localFallbackUsed = localFallbackUsed,
            cloudRefreshRecommended = request.messages.isNotEmpty(),
            cloudRefreshAttempted = false,
            refreshTriggers = refreshTriggers,
            compression = compression,
            arcProgressState = arcProgress,
            nextMoveType = nextMove,
            panelNextAction = playbook.expressionModeSelection?.panelModeLabel ?: nextMove.name,
            latencyMs = 0L,
            decisionSource = decisionSourceFor(
                cacheHit = cacheHit,
                playbook = playbook,
                cacheSource = "PLAYBOOK_CACHE_ACTIVE_EXPRESSION",
                fallbackSource = "LOCAL_PLAYBOOK_FALLBACK_ACTIVE_EXPRESSION"
            ),
            expressionModeSelection = playbook.expressionModeSelection,
            expressSelfEligibility = eligibility,
            expressSelfFeedbackDefaultVisible = false,
            expressSelfFeedbackCollapsed = true,
            expressSelfDefaultRouteCount = activeRoutes.size,
            expressSelfPanelSimpleMode = true
        )
        expressSelfResultCache.put(
            key = cacheLookupKey.copy(eligibilityMode = eligibility.mode.name),
            result = result,
            nowMillis = request.capturedAt
        )
        return result
    }

    private fun decisionSourceFor(
        cacheHit: Boolean,
        playbook: RelationshipPlaybook,
        cacheSource: String,
        fallbackSource: String
    ): String = when {
        cacheHit && playbook.source == RelationshipPlaybookSource.CLOUD_ENHANCED -> "CLOUD_ENHANCED_PLAYBOOK"
        cacheHit -> cacheSource
        else -> fallbackSource
    }

    private fun prioritizeExpressSelfRoutes(
        routes: List<ReplyRoute>,
        sceneTags: List<String>
    ): List<ReplyRoute> {
        val requiresArc = outputQualityGate.requiresPlanningGrounding(sceneTags)
        if (!requiresArc) return routes.take(EXPRESS_SELF_DEFAULT_ROUTE_LIMIT)
        val requiredTypes = listOf(
            ReplyRouteType.SELF_STORY,
            ReplyRouteType.ARC_REVEAL,
            ReplyRouteType.CO_CREATION
        )
        val required = requiredTypes.mapNotNull { type -> routes.firstOrNull { it.routeType == type } }
        val rest = routes.filterNot { route -> required.any { it.id == route.id } }
        return (required + rest).distinctBy { it.id }.take(EXPRESS_SELF_DEFAULT_ROUTE_LIMIT)
    }

    private fun refreshTriggers(
        request: DynamicPlaybookRequest,
        snapshot: LightChatStableSnapshot,
        playbook: RelationshipPlaybook,
        cacheHit: Boolean
    ): List<PlaybookRefreshTrigger> {
        val triggers = mutableListOf<PlaybookRefreshTrigger>()
        if (!cacheHit) triggers += PlaybookRefreshTrigger.CACHE_MISSING
        if (snapshot.recentEffectiveMessages.size >= 5) triggers += PlaybookRefreshTrigger.MESSAGES_CHANGED
        if (inferTopics(snapshot).any { it in arcTriggerTopics }) triggers += PlaybookRefreshTrigger.ARC_TRIGGER_TOPIC
        if (request.mode == DynamicPlaybookMode.EXPRESS_SELF) triggers += PlaybookRefreshTrigger.EXPRESS_SELF_CLICK
        if (playbook.expiresWhen.isExpired(request.capturedAt)) triggers += PlaybookRefreshTrigger.PLAYBOOK_EXPIRED
        return triggers.distinct()
    }

    private fun inferTopics(snapshot: LightChatStableSnapshot): List<String> {
        val text = snapshot.recentEffectiveMessages.joinToString(" ") { it.text.orEmpty() }.lowercase()
        return topicRules
            .filter { (_, tokens) -> tokens.any { token -> text.contains(token) } }
            .map { (topic, _) -> topic }
            .ifEmpty { listOf("ordinary") }
            .distinct()
    }

    private fun playbookChatKey(
        request: DynamicPlaybookRequest,
        snapshot: LightChatStableSnapshot
    ): String = if (request.appPackage == "com.huiyi.mockchat" && !request.chatWindowHash.isNullOrBlank()) {
        "${request.appPackage}|mock:${request.chatWindowHash}"
    } else {
        snapshot.chatKey.orEmpty().ifBlank { "${request.appPackage.orEmpty()}|default" }
    }

    private companion object {
        const val EXPRESS_SELF_DEFAULT_ROUTE_LIMIT = 5
        val activeOnlyRouteTypes = setOf(ReplyRouteType.ARC_REVEAL, ReplyRouteType.SELF_STORY, ReplyRouteType.CO_CREATION)
        val arcTriggerTopics = setOf("reality", "planning", "stability", "future", "past", "responsibility")
        val topicRules = linkedMapOf(
            "planning" to listOf("planning", "plan", "\u89c4\u5212", "\u8ba1\u5212", "\u5b89\u6392"),
            "reality" to listOf("reality", "realistic", "\u73b0\u5b9e", "\u5b9e\u9645"),
            "stability" to listOf("stable", "stability", "\u7a33\u5b9a", "\u8e0f\u5b9e"),
            "future" to listOf("future", "later", "\u672a\u6765", "\u4ee5\u540e", "\u957f\u671f"),
            "past" to listOf("past", "experience", "\u8fc7\u53bb", "\u7ecf\u5386", "\u4ee5\u524d"),
            "responsibility" to listOf("responsibility", "responsible", "\u8d23\u4efb", "\u8d1f\u8d23", "\u4e3a\u4f60\u597d"),
            "work_pressure" to listOf("work", "pressure", "busy", "\u5de5\u4f5c", "\u538b\u529b", "\u5fd9", "\u5ba2\u6237"),
            "army" to listOf("army", "soldier", "\u90e8\u961f", "\u8001\u73ed\u957f", "\u51fa\u64cd", "\u8f6c\u4e1a"),
            "kids" to listOf("kid", "child", "\u5b69\u5b50", "\u5c0f\u5b69"),
            "expression_difficulty" to listOf("express", "\u4e0d\u77e5\u9053\u600e\u4e48\u8868\u8fbe", "\u4e0d\u4f1a\u8868\u8fbe"),
            "read_no_reply" to listOf("read", "\u5df2\u8bfb", "\u672a\u8bfb", "\u221a"),
            "light_life" to listOf("eat", "clothes", "\u5403\u996d", "\u8863\u670d", "\u9001\u8863\u670d"),
            "retreat" to listOf("later", "busy", "\u7b97\u4e86", "\u4e0d\u804a", "\u665a\u70b9", "\u5fd9")
        )
    }
}
