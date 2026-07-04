package com.huiyi.v4.domain.review

import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.TacticalDecision

data class ChatReviewDraft(
    val id: String,
    val contactKey: String,
    val contactDisplayName: String,
    val appPackage: String,
    val windowTitle: String,
    val sceneId: String,
    val createdAt: Long,
    val source: String,
    val coCreationPoint: String,
    val userLikelyMistake: String,
    val intensity: String,
    val riskLevel: String,
    val riskWarning: String?,
    val fallbackMove: String?,
    val bestMove: String,
    val recommendedReplyPreview: String,
    val routeCount: Int,
    val modelSource: String,
    val profileHints: List<String>
)

object ChatReviewMemoryBuilder {
    fun build(
        context: ChatSceneContext,
        appPackage: String,
        windowTitle: String,
        decision: TacticalDecision,
        routes: List<ReplyRoute>,
        cloudTrace: CloudAnalysisTrace,
        now: Long = System.currentTimeMillis()
    ): ChatReviewDraft {
        val displayName = windowTitle.takeIf { it.isNotBlank() } ?: appPackage.ifBlank { "unknown" }
        val contactKey = context.contactId
            ?: stableContactKey(appPackage = appPackage, displayName = displayName)
        val recommended = routes.firstOrNull { it.recommended } ?: routes.firstOrNull()
        return ChatReviewDraft(
            id = "review-$now",
            contactKey = contactKey,
            contactDisplayName = displayName,
            appPackage = appPackage,
            windowTitle = windowTitle,
            sceneId = context.id,
            createdAt = now,
            source = "next_sentence_cloud_or_local",
            coCreationPoint = decision.coreInsight.orEmpty(),
            userLikelyMistake = decision.userLikelyMistake.orEmpty(),
            intensity = decision.influenceProfile.intensity.name,
            riskLevel = decision.influenceProfile.riskLevel.name,
            riskWarning = decision.influenceProfile.riskWarning,
            fallbackMove = decision.fallbackMove ?: decision.influenceProfile.fallbackMove,
            bestMove = decision.bestMove.orEmpty(),
            recommendedReplyPreview = recommended?.message.orEmpty(),
            routeCount = routes.size,
            modelSource = cloudTrace.cloudFinalModel.ifBlank { cloudTrace.decisionSource },
            profileHints = profileHints(decision)
        )
    }

    private fun stableContactKey(appPackage: String, displayName: String): String {
        val raw = "${appPackage.ifBlank { "unknown" }}:${displayName.ifBlank { "unknown" }}"
        return "contact-" + raw.hashCode().toUInt().toString(16)
    }

    private fun profileHints(decision: TacticalDecision): List<String> {
        val hints = mutableListOf<String>()
        decision.coreInsight?.takeIf { it.isNotBlank() }?.let { hints += "interaction_pattern:$it" }
        decision.userLikelyMistake?.takeIf { it.isNotBlank() }?.let { hints += "user_risk:$it" }
        decision.influenceProfile.riskWarning?.takeIf { it.isNotBlank() }?.let { hints += "red_flag:$it" }
        decision.fallbackMove?.takeIf { it.isNotBlank() }?.let { hints += "safe_fallback:$it" }
        return hints.take(8)
    }
}
