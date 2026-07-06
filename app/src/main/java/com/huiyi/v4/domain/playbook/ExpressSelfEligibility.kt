package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.context.LightChatStableSnapshot
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.LastSpeakerDecision

data class ExpressSelfEligibility(
    val eligible: Boolean,
    val mode: ExpressSelfEligibilityMode,
    val blockReason: ExpressSelfBlockReason?,
    val confidence: Int,
    val source: String,
    val currentAppPackage: String?,
    val currentWindowTitleRedacted: String?,
    val targetAppSupported: Boolean,
    val snapshotTrusted: Boolean,
    val lastSpeaker: Speaker,
    val shouldReply: Boolean,
    val lastUserMessageAgeMs: Long?,
    val expressionWindowExists: Boolean,
    val coldStartAllowed: Boolean,
    val recentSelfExpressionCount: Int,
    val repeatRisk: String
) {
    val allowedReason: String
        get() = if (eligible) mode.name else "NOT_ALLOWED"

    val blockedReason: String
        get() = blockReason?.name ?: if (eligible) "NONE" else "UNKNOWN"
}

enum class ExpressSelfEligibilityMode {
    ALLOW_EXPRESS_SELF,
    ALLOW_COLD_START,
    ALLOW_ELEVATE_MEANING,
    ALLOW_GENERIC_TRIAL,
    HOLD_BACK,
    BLOCK_UNSUPPORTED_CONTEXT,
    BLOCK_UNTRUSTED_SNAPSHOT,
    BLOCK_RECENT_LAST_ME,
    BLOCK_NO_CHAT_STATE
}

enum class ExpressSelfBlockReason {
    UNSUPPORTED_APP,
    WINDOW_IS_DESKTOP_OR_LAUNCHER,
    SNAPSHOT_UNTRUSTED,
    LAST_SPEAKER_ME_TOO_RECENT,
    NO_EXPRESSION_WINDOW,
    TOO_MUCH_SELF_EXPRESSION_RECENTLY,
    REPEAT_RISK_HIGH,
    CHAT_STATE_MISSING,
    TARGET_CHAT_NOT_FOUND
}

class ExpressSelfEligibilityEvaluator {
    fun evaluate(
        request: DynamicPlaybookRequest,
        snapshot: LightChatStableSnapshot,
        lastSpeakerDecision: LastSpeakerDecision,
        playbook: RelationshipPlaybook,
        arcProgress: ArcProgressState
    ): ExpressSelfEligibility {
        val explicitTargetSupported = request.targetAppSupported
            ?: (request.appPackage.orEmpty() in supportedChatPackages)
        val parserConfidence = request.parserConfidence.coerceIn(0, 100)
        val currentPackage = request.currentAppPackage ?: request.appPackage
        val currentWindowTitle = request.currentWindowTitleRedacted ?: request.windowTitle
        val effectiveCount = snapshot.recentEffectiveMessages.count { it.speaker != Speaker.SYSTEM }
        val lastSpeaker = lastSpeakerDecision.lastSpeaker ?: Speaker.UNKNOWN
        val lastUserMessageAgeMs = request.lastUserMessageAgeMsOverride
            ?: snapshot.lastUserMessage?.createdAt
                ?.let { createdAt -> request.capturedAt - createdAt }
                ?.takeIf { it >= 0L }
        val recentSelfExpressionCount = request.recentSelfExpressionCountOverride
            ?: request.expressionLedger
                .recentEntries(request.capturedAt, windowMillis = RECENT_SELF_EXPRESSION_WINDOW_MS)
                .count {
                    it.lastExpressionLevel in setOf(
                        ExpressionLevel.ATTITUDE,
                        ExpressionLevel.ARC_REVEAL,
                        ExpressionLevel.CO_CREATION
                    )
                }
        val repeatRisk = request.repeatRiskOverride ?: playbook.expressionModeSelection?.repeatRisk?.name.orEmpty()
            .ifBlank { ExpressionRepeatRisk.LOW.name }
        val expressionMode = playbook.expressionModeSelection?.expressionMode
        val genericTrial = genericChatTrial(
            request = request,
            currentPackage = currentPackage,
            currentWindowTitle = currentWindowTitle,
            effectiveCount = effectiveCount,
            parserConfidence = parserConfidence
        )
        val targetSupported = explicitTargetSupported || genericTrial
        val expressionWindowExists = arcProgress.currentExpressionWindow.exists ||
            playbook.characterArcPlan.exists ||
            expressionMode in setOf(
                ExpressionMode.EXTEND_TOPIC,
                ExpressionMode.ELEVATE_MEANING,
                ExpressionMode.SWITCH_FACET
            )
        val chatInactiveMs = request.chatInactiveMsOverride ?: lastUserMessageAgeMs
        val longInactive = (lastUserMessageAgeMs != null && lastUserMessageAgeMs >= COLD_START_MIN_AGE_MS) ||
            (chatInactiveMs != null && chatInactiveMs >= COLD_START_MIN_AGE_MS)
        val coldStartAllowed = longInactive ||
            (expressionMode == ExpressionMode.START_TOPIC && lastSpeaker != Speaker.ME)
        val base = ExpressSelfEligibility(
            eligible = false,
            mode = ExpressSelfEligibilityMode.BLOCK_NO_CHAT_STATE,
            blockReason = ExpressSelfBlockReason.CHAT_STATE_MISSING,
            confidence = parserConfidence,
            source = if (genericTrial) "GENERIC_TRIAL" else request.preAnalysisSnapshotSource,
            currentAppPackage = currentPackage,
            currentWindowTitleRedacted = currentWindowTitle,
            targetAppSupported = targetSupported,
            snapshotTrusted = request.snapshotTrusted,
            lastSpeaker = lastSpeaker,
            shouldReply = lastSpeakerDecision.shouldReply,
            lastUserMessageAgeMs = lastUserMessageAgeMs,
            expressionWindowExists = expressionWindowExists,
            coldStartAllowed = coldStartAllowed,
            recentSelfExpressionCount = recentSelfExpressionCount,
            repeatRisk = repeatRisk
        )

        return when {
            effectiveCount == 0 -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_NO_CHAT_STATE,
                blockReason = ExpressSelfBlockReason.CHAT_STATE_MISSING
            )

            isDesktopOrPanelWindow(currentWindowTitle, currentPackage) -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT,
                blockReason = ExpressSelfBlockReason.WINDOW_IS_DESKTOP_OR_LAUNCHER
            )

            !request.snapshotTrusted -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT,
                blockReason = ExpressSelfBlockReason.SNAPSHOT_UNTRUSTED
            )

            request.preAnalysisSnapshotSource.contains("LAST_STABLE", ignoreCase = true) &&
                !currentPackage.isNullOrBlank() &&
                !request.appPackage.isNullOrBlank() &&
                currentPackage != request.appPackage -> base.copy(
                    mode = ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT,
                    blockReason = ExpressSelfBlockReason.SNAPSHOT_UNTRUSTED
                )

            !targetSupported -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_UNSUPPORTED_CONTEXT,
                blockReason = ExpressSelfBlockReason.UNSUPPORTED_APP
            )

            lastSpeaker == Speaker.UNKNOWN -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_UNTRUSTED_SNAPSHOT,
                blockReason = ExpressSelfBlockReason.TARGET_CHAT_NOT_FOUND
            )

            recentSelfExpressionCount >= RECENT_SELF_EXPRESSION_LIMIT -> base.copy(
                mode = ExpressSelfEligibilityMode.HOLD_BACK,
                blockReason = ExpressSelfBlockReason.TOO_MUCH_SELF_EXPRESSION_RECENTLY
            )

            repeatRisk.equals(ExpressionRepeatRisk.HIGH.name, ignoreCase = true) -> base.copy(
                mode = ExpressSelfEligibilityMode.HOLD_BACK,
                blockReason = ExpressSelfBlockReason.REPEAT_RISK_HIGH
            )

            lastSpeaker == Speaker.ME && !coldStartAllowed -> base.copy(
                mode = ExpressSelfEligibilityMode.BLOCK_RECENT_LAST_ME,
                blockReason = ExpressSelfBlockReason.LAST_SPEAKER_ME_TOO_RECENT
            )

            lastSpeaker == Speaker.OTHER && expressionWindowExists -> base.copy(
                eligible = true,
                mode = when {
                    genericTrial -> ExpressSelfEligibilityMode.ALLOW_GENERIC_TRIAL
                    expressionMode == ExpressionMode.ELEVATE_MEANING -> ExpressSelfEligibilityMode.ALLOW_ELEVATE_MEANING
                    else -> ExpressSelfEligibilityMode.ALLOW_EXPRESS_SELF
                },
                blockReason = null
            )

            coldStartAllowed -> base.copy(
                eligible = true,
                mode = ExpressSelfEligibilityMode.ALLOW_COLD_START,
                blockReason = null
            )

            else -> base.copy(
                mode = ExpressSelfEligibilityMode.HOLD_BACK,
                blockReason = ExpressSelfBlockReason.NO_EXPRESSION_WINDOW
            )
        }
    }

    companion object {
        const val HIGH_CONFIDENCE: Int = 85
        const val GENERIC_TRIAL_MIN_CONFIDENCE: Int = 70
        const val COLD_START_MIN_AGE_MS: Long = 30 * 60 * 1000L
        const val RECENT_SELF_EXPRESSION_LIMIT: Int = 2
        const val RECENT_SELF_EXPRESSION_WINDOW_MS: Long = 30 * 60 * 1000L

        val supportedChatPackages: Set<String> = setOf(
            "com.bajiao.im.liaoqi",
            "com.huiyi.mockchat"
        )

        fun isDesktopOrPanelWindow(title: String?, packageName: String?): Boolean {
            val joined = listOfNotNull(title, packageName).joinToString(" ")
            if (joined.isBlank()) return false
            val markers = listOf(
                "\u534e\u4e3a\u684c\u9762",
                "\u684c\u9762",
                "Launcher",
                "launcher",
                "\u4f1a\u610f\u96f7\u8fbe",
                "\u8fd9\u6b21\u4e0d\u5bf9",
                "\u6ca1\u8bfb\u5230\u5f53\u524d\u804a\u5929",
                "\u6ca1\u8bfb\u5230\u804a\u5929",
                "\u8bf7\u56de\u5230\u804a\u8d77\u804a\u5929\u7a97\u53e3",
                "\u9690\u85cf",
                "com.huawei.android.launcher"
            )
            return markers.any { joined.contains(it, ignoreCase = true) }
        }

        private fun genericChatTrial(
            request: DynamicPlaybookRequest,
            currentPackage: String?,
            currentWindowTitle: String?,
            effectiveCount: Int,
            parserConfidence: Int
        ): Boolean {
            if (request.appPackage.orEmpty() in supportedChatPackages) return false
            if (request.appPackage != "com.xiaoenai.app") return false
            if (isDesktopOrPanelWindow(currentWindowTitle, currentPackage)) return false
            if (currentWindowTitle?.contains("\u5c0f\u6069\u7231", ignoreCase = true) != true) return false
            if (effectiveCount < 3) return false
            return parserConfidence >= GENERIC_TRIAL_MIN_CONFIDENCE
        }
    }
}
