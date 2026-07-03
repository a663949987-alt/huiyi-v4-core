package com.huiyi.v4.domain.model

data class VisualBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerX: Int get() = (left + right) / 2
}

enum class MessageSource {
    ACCESSIBILITY_CURRENT_SCREEN,
    ACCESSIBILITY_CONTEXT_BACKFILL,
    ACCESSIBILITY_LIGHT_LISTEN,
    SCREENSHOT_OCR,
    MANUAL_TEXT,
    MANUAL_VOICE_SUMMARY,
    NOTIFICATION_PREVIEW,
    IME_USER_INPUT,
    MOCK
}

sealed class MessageContent {
    data class Text(val text: String) : MessageContent()

    data class Voice(
        val durationSeconds: Int?,
        val transcriptStatus: TranscriptStatus,
        val transcriptText: String?,
        val userSummary: String?
    ) : MessageContent()

    data class Image(
        val descriptionStatus: DescriptionStatus,
        val descriptionText: String?
    ) : MessageContent()

    data class Video(
        val durationSeconds: Int?,
        val descriptionStatus: DescriptionStatus,
        val descriptionText: String?
    ) : MessageContent()

    data class Sticker(
        val meaningStatus: DescriptionStatus,
        val meaningText: String?
    ) : MessageContent()
}

enum class TranscriptStatus {
    MISSING,
    APP_TRANSCRIBED,
    USER_SUMMARY,
    ASR_TRANSCRIBED
}

enum class DescriptionStatus {
    MISSING,
    USER_SUMMARY,
    MODEL_DESCRIBED
}

data class MessageNode(
    val id: String,
    val contactId: String?,
    val speaker: Speaker,
    val content: MessageContent,
    val normalizedText: String?,
    val source: MessageSource,
    val localSequence: Long,
    val confidence: Int,
    val speakerConfidence: Int,
    val contentConfidence: Int,
    val bounds: VisualBounds?,
    val pageIndex: Int?,
    val createdAt: Long,
    val sceneId: String?,
    val speakerReason: String? = null,
    val parserName: String? = null,
    val isEffectiveChatMessage: Boolean = true,
    val metadataType: MetadataType? = MetadataType.NONE,
    val rowBounds: VisualBounds? = null,
    val textBounds: VisualBounds? = null,
    val inferredSide: String? = null,
    val parentBounds: VisualBounds? = null,
    val bubbleBounds: VisualBounds? = null,
    val ancestorBoundsChain: List<VisualBounds> = emptyList(),
    val unknownReason: String? = null,
    val rawNodeOrder: Int? = null,
    val finalVisualOrder: Int? = null,
    val sideMarginLeft: Int? = null,
    val sideMarginRight: Int? = null,
    val finalDecisionSource: String? = null,
    val possibleSpeakerConflict: Boolean = false,
    val visualDebugBoxDrawn: Boolean = false,
    val projectedBox: VisualBounds? = null,
    val accessibilitySide: String? = null,
    val visualProjectedSide: String? = null,
    val visualConflict: Boolean = false,
    val visualConflictReason: String? = null,
    val visualSpeakerFallbackUsed: Boolean = false
)

enum class Speaker {
    ME,
    OTHER,
    SYSTEM,
    UNKNOWN
}

enum class MetadataType {
    NONE,
    TIME,
    DATE,
    HEADER,
    ONLINE_STATUS,
    UI_CONTROL,
    SYSTEM_NOTICE,
    UNKNOWN_METADATA
}

data class Turn(
    val id: String,
    val speaker: Speaker,
    val messageIds: List<String>,
    val summary: String,
    val startedAt: Long,
    val endedAt: Long,
    val turnType: TurnType
)

enum class TurnType {
    SMALL_TALK,
    FACT_EXCHANGE,
    EMOTION,
    VULNERABILITY,
    RELATIONSHIP_TEST,
    BOUNDARY,
    DAILY_CARE,
    UNKNOWN
}

data class CoCreationOpportunity(
    val exists: Boolean,
    val type: CoCreationType,
    val unfinishedMeaning: String?,
    val bestMove: String?,
    val avoidMoves: List<String>,
    val confidence: Int
)

enum class CoCreationType {
    SHARED_UNDERSTANDING,
    SHARED_LANGUAGE,
    SHARED_RULE,
    SHARED_EXPECTATION,
    DAILY_RHYTHM,
    REPAIR_MOMENT,
    NO_OPPORTUNITY
}

data class ChatSceneContext(
    val id: String,
    val contactId: String?,
    val currentScreenMessages: List<MessageNode>,
    val backfillMessages: List<MessageNode>,
    val recentMemoryMessages: List<MessageNode>,
    val turns: List<Turn>,
    val lastReplyAttempts: List<ReplyAttempt>,
    val lastOutcomes: List<ReplyOutcome>,
    val userPersonaContext: UserPersonaContext?,
    val contentCompleteness: ContentCompleteness,
    val coCreationOpportunity: CoCreationOpportunity?,
    val createdAt: Long
) {
    val allMessages: List<MessageNode>
        get() = (backfillMessages + currentScreenMessages + recentMemoryMessages)
            .distinctBy { it.id }
            .sortedBy { it.localSequence }

    val effectiveMessages: List<MessageNode>
        get() = allMessages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }

    val lastMessage: MessageNode?
        get() = effectiveMessages.lastOrNull()
}

data class ContentCompleteness(
    val score: Int,
    val canDeepAnalyze: Boolean,
    val missingTypes: List<MissingContextType>,
    val reason: String
)

enum class MissingContextType {
    PREVIOUS_TURN_MISSING,
    VOICE_WITHOUT_TRANSCRIPT,
    IMAGE_WITHOUT_DESCRIPTION,
    UNKNOWN_SPEAKER,
    OCR_LOW_CONFIDENCE,
    NOT_ENOUGH_MESSAGES,
    REFERENTIAL_CONTEXT_MISSING
}

data class TacticalDecision(
    val decisionType: TacticalDecisionType,
    val situation: String,
    val coreInsight: String,
    val userLikelyMistake: String?,
    val bestMove: String,
    val avoidMoves: List<String>,
    val coCreationOpportunity: CoCreationOpportunity?,
    val shouldUseUserStory: Boolean,
    val selectedStoryCardIds: List<String>,
    val influenceProfile: InfluenceProfile,
    val fallbackMove: String?
)

enum class TacticalDecisionType {
    NORMAL_REPLY,
    EMPATHY_FIRST,
    HUIYI_MOMENT,
    COOL_DOWN,
    WARM_UP,
    REPAIR,
    BOUNDARY_RESPECT,
    WAIT,
    PUSH_LIGHTLY,
    PUSH_NOT_NOW,
    VOICE_SUMMARY_REQUIRED,
    CONTEXT_REQUIRED
}

data class InfluenceProfile(
    val intensity: InfluenceIntensity,
    val riskLevel: RiskLevel,
    val riskWarning: String?,
    val fallbackMove: String?
)

enum class InfluenceIntensity {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class ReplyRoute(
    val id: String,
    val name: String,
    val routeType: ReplyRouteType,
    val tag: String,
    val message: String,
    val intensity: InfluenceIntensity,
    val riskLevel: RiskLevel,
    val riskWarning: String?,
    val expectedEffect: String?,
    val fallbackMove: String?,
    val recommended: Boolean
)

enum class ReplyRouteType {
    STABLE,
    EMPATHY,
    CO_CREATION,
    COOL_DOWN,
    WARM_UP,
    SELF_STORY,
    REPAIR,
    WAIT,
    DIRECT
}

data class ReplyAttempt(
    val id: String,
    val contactId: String?,
    val sceneId: String,
    val routeId: String,
    val routeType: ReplyRouteType,
    val suggestedText: String,
    val userAction: UserAction,
    val status: ReplyAttemptStatus,
    val selectedAt: Long,
    val confirmedSentAt: Long?,
    val finalSentText: String?
)

enum class UserAction {
    COPIED,
    INSERTED,
    IGNORED,
    EDITED
}

enum class ReplyAttemptStatus {
    PENDING,
    CONFIRMED_SENT,
    NOT_SENT,
    UNKNOWN
}

data class ReplyOutcome(
    val id: String,
    val attemptId: String,
    val contactId: String?,
    val replied: Boolean,
    val replyDelaySeconds: Long?,
    val replyLength: Int?,
    val hasQuestion: Boolean,
    val hasSelfDisclosure: Boolean,
    val hasWarmSignal: Boolean,
    val hasPullbackSignal: Boolean,
    val outcomeScore: Int,
    val outcomeLabel: String
)
