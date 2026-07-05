package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.context.LightMessage
import com.huiyi.v4.domain.model.CharacterArcCard
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.Speaker

enum class ExpressionLevel {
    FACT,
    ATTITUDE,
    ARC_REVEAL,
    CO_CREATION
}

enum class OtherReaction {
    RECEIVED,
    SHIFTED,
    COLD,
    UNKNOWN
}

enum class ExpressionRepeatRisk {
    LOW,
    MEDIUM,
    HIGH
}

enum class ExpressionMode {
    START_TOPIC,
    EXTEND_TOPIC,
    ELEVATE_MEANING,
    SWITCH_FACET,
    HOLD_BACK
}

data class ExpressionLedgerEntry(
    val themeId: String,
    val themeName: String,
    val lastUsedAt: Long,
    val lastExpressionLevel: ExpressionLevel,
    val lastIntensity: InfluenceIntensity,
    val lastSurfaceLineRedacted: String,
    val otherReaction: OtherReaction,
    val repeatRisk: ExpressionRepeatRisk,
    val canReappear: Boolean,
    val suggestedNextLevel: ExpressionLevel
)

data class ExpressionLedger(
    val entries: List<ExpressionLedgerEntry> = emptyList()
) {
    fun entryFor(themeId: String): ExpressionLedgerEntry? =
        entries
            .filter { it.themeId == themeId }
            .maxByOrNull { it.lastUsedAt }

    fun recentEntries(nowMillis: Long, windowMillis: Long = 12 * 60 * 60 * 1000L): List<ExpressionLedgerEntry> =
        entries.filter { nowMillis - it.lastUsedAt in 0..windowMillis }

    companion object {
        fun empty(): ExpressionLedger = ExpressionLedger()
    }
}

data class ArcTheme(
    val themeId: String,
    val themeName: String,
    val triggerTokens: List<String>,
    val defaultNextLevel: ExpressionLevel,
    val avoidLine: String,
    val routeSeed: String
)

object FixedArcThemes {
    val all: List<ArcTheme> = listOf(
        ArcTheme(
            themeId = "steady_not_cold",
            themeName = "\u7a33\u4f46\u4e0d\u662f\u51b7",
            triggerTokens = listOf("\u7a33\u5b9a", "\u7a33", "\u8282\u594f", "stable", "stability"),
            defaultNextLevel = ExpressionLevel.ARC_REVEAL,
            avoidLine = "\u4e0d\u8981\u628a\u7a33\u5b9a\u8bf4\u6210\u51b7\u5904\u7406\uff0c\u4e5f\u4e0d\u8981\u53cd\u590d\u5f3a\u8c03\u81ea\u5df1\u591a\u7406\u6027\u3002",
            routeSeed = "\u6211\u4e0d\u662f\u6ca1\u60c5\u7eea\uff0c\u53ea\u662f\u66f4\u4e60\u60ef\u5148\u628a\u4e8b\u60c5\u7a33\u4f4f\uff0c\u518d\u6162\u6162\u628a\u8bdd\u8bf4\u6e05\u695a\u3002"
        ),
        ArcTheme(
            themeId = "serious_not_playboy",
            themeName = "\u4e0d\u82b1\u4f46\u8ba4\u771f",
            triggerTokens = listOf("\u8ba4\u771f", "\u5173\u7cfb", "\u559c\u6b22", "\u66a7\u6627", "serious"),
            defaultNextLevel = ExpressionLevel.ATTITUDE,
            avoidLine = "\u4e0d\u8981\u6025\u7740\u8868\u5fe0\u5fc3\uff0c\u4e5f\u4e0d\u8981\u628a\u8ba4\u771f\u8bf4\u6210\u538b\u529b\u3002",
            routeSeed = "\u6211\u4e0d\u662f\u90a3\u79cd\u5f88\u4f1a\u5230\u5904\u64a9\u7684\u4eba\uff0c\u4f46\u771f\u653e\u5728\u5fc3\u4e0a\u7684\u4e8b\uff0c\u6211\u4f1a\u8ba4\u771f\u5bf9\u5f85\u3002"
        ),
        ArcTheme(
            themeId = "experienced_not_miserable",
            themeName = "\u6709\u7ecf\u5386\u4f46\u4e0d\u5356\u60e8",
            triggerTokens = listOf("\u8fc7\u53bb", "\u7ecf\u5386", "\u4ee5\u524d", "\u8f6c\u4e1a", "\u90e8\u961f", "past", "experience"),
            defaultNextLevel = ExpressionLevel.ARC_REVEAL,
            avoidLine = "\u4e0d\u8981\u8bb2\u6210\u957f\u7bc7\u81ea\u6211\u8bc1\u660e\uff0c\u4e5f\u4e0d\u8981\u628a\u8fc7\u53bb\u8bb2\u6210\u5356\u60e8\u3002",
            routeSeed = "\u6211\u8fc7\u53bb\u786e\u5b9e\u7ecf\u5386\u8fc7\u4e00\u4e9b\u4e8b\uff0c\u4f46\u6211\u4e0d\u60f3\u62ff\u5b83\u6362\u540c\u60c5\uff0c\u53ea\u662f\u5b83\u8ba9\u6211\u66f4\u77e5\u9053\u4ec0\u4e48\u8981\u8ba4\u771f\u3002"
        ),
        ArcTheme(
            themeId = "responsible_not_pressuring",
            themeName = "\u6709\u8d23\u4efb\u611f\u4f46\u4e0d\u538b\u8feb",
            triggerTokens = listOf("\u8d23\u4efb", "\u8d1f\u8d23", "\u4e3a\u4f60\u597d", "\u73b0\u5b9e", "responsibility"),
            defaultNextLevel = ExpressionLevel.ARC_REVEAL,
            avoidLine = "\u4e0d\u8981\u628a\u8d23\u4efb\u611f\u53d8\u6210\u7ba1\u6559\u611f\uff0c\u4e5f\u4e0d\u8981\u7ad9\u4e0a\u53bb\u8bb2\u9053\u7406\u3002",
            routeSeed = "\u6211\u4f1a\u5728\u610f\u73b0\u5b9e\u548c\u8d23\u4efb\uff0c\u4f46\u6211\u4e0d\u60f3\u7528\u8fd9\u4e2a\u538b\u7740\u4f60\uff0c\u53ea\u662f\u60f3\u628a\u4e8b\u505a\u8e0f\u5b9e\u3002"
        ),
        ArcTheme(
            themeId = "planning_not_promise",
            themeName = "\u73b0\u5b9e\u89c4\u5212\u4f46\u4e0d\u753b\u997c",
            triggerTokens = listOf("\u89c4\u5212", "\u8ba1\u5212", "\u672a\u6765", "\u4ee5\u540e", "\u73b0\u5b9e", "\u957f\u671f", "planning", "future", "reality"),
            defaultNextLevel = ExpressionLevel.CO_CREATION,
            avoidLine = "\u4e0d\u8981\u753b\u5f88\u6ee1\u7684\u627f\u8bfa\uff0c\u4e5f\u4e0d\u8981\u91cd\u590d\u540c\u4e00\u53e5\u4e00\u6b65\u4e00\u6b65\u8d70\u7a33\u3002",
            routeSeed = "\u6211\u4e5f\u633a\u8ba4\u540c\u8fd9\u4e2a\u3002\u5f88\u591a\u4e8b\u4e0d\u662f\u5634\u4e0a\u8bf4\u6ee1\u5c31\u884c\uff0c\u6211\u66f4\u613f\u610f\u4e00\u6b65\u4e00\u6b65\u628a\u5b83\u843d\u5230\u73b0\u5b9e\u91cc\u3002"
        )
    )
}

data class ExpressionModeSelection(
    val expressionMode: ExpressionMode,
    val selectedTheme: ArcTheme,
    val selectedArcCard: CharacterArcCard?,
    val repeatRisk: ExpressionRepeatRisk,
    val suggestedDepth: com.huiyi.v4.domain.context.ArcRevealDepth,
    val reason: String
) {
    val panelModeLabel: String get() = when (expressionMode) {
        ExpressionMode.START_TOPIC -> "\u5f00\u573a"
        ExpressionMode.EXTEND_TOPIC -> "\u5ef6\u4f38"
        ExpressionMode.ELEVATE_MEANING -> "\u5347\u7ef4"
        ExpressionMode.SWITCH_FACET -> "\u6362\u4e00\u9762"
        ExpressionMode.HOLD_BACK -> "\u5148\u4e0d\u8bf4"
    }
}

class ExpressionModeSelector(
    private val themes: List<ArcTheme> = FixedArcThemes.all
) {
    fun select(
        currentTopics: List<String>,
        recentMessages: List<LightMessage>,
        arcProgressState: ArcProgressState,
        expressionLedger: ExpressionLedger,
        characterArcPlannerOutput: ArcProgressState = arcProgressState,
        nowMillis: Long = System.currentTimeMillis()
    ): ExpressionModeSelection {
        val selectedTheme = selectTheme(currentTopics, recentMessages)
        val entry = expressionLedger.entryFor(selectedTheme.themeId)
        val repeatRisk = repeatRisk(entry)
        val recentSelfExpressionCount = expressionLedger
            .recentEntries(nowMillis, windowMillis = 30 * 60 * 1000L)
            .count { it.lastExpressionLevel in setOf(ExpressionLevel.ATTITUDE, ExpressionLevel.ARC_REVEAL, ExpressionLevel.CO_CREATION) }
        val userJustExpressed = recentMessages.takeLast(2).all { it.speaker == Speaker.ME } && recentMessages.size >= 2
        val mode = when {
            recentSelfExpressionCount >= 2 || userJustExpressed -> ExpressionMode.HOLD_BACK
            entry != null && repeatRisk == ExpressionRepeatRisk.HIGH && entry.otherReaction == OtherReaction.RECEIVED -> ExpressionMode.ELEVATE_MEANING
            entry != null && repeatRisk == ExpressionRepeatRisk.HIGH -> ExpressionMode.SWITCH_FACET
            entry != null && entry.otherReaction == OtherReaction.RECEIVED -> ExpressionMode.ELEVATE_MEANING
            entry != null && entry.canReappear -> ExpressionMode.EXTEND_TOPIC
            isColdStart(recentMessages, arcProgressState) -> ExpressionMode.START_TOPIC
            characterArcPlannerOutput.currentExpressionWindow.exists -> ExpressionMode.EXTEND_TOPIC
            else -> ExpressionMode.START_TOPIC
        }
        return ExpressionModeSelection(
            expressionMode = mode,
            selectedTheme = selectedTheme,
            selectedArcCard = characterArcPlannerOutput.suggestedArcCard,
            repeatRisk = repeatRisk,
            suggestedDepth = if (mode == ExpressionMode.HOLD_BACK) {
                com.huiyi.v4.domain.context.ArcRevealDepth.LOW
            } else {
                characterArcPlannerOutput.suggestedDepth
            },
            reason = reasonFor(mode, entry, selectedTheme)
        )
    }

    private fun selectTheme(
        currentTopics: List<String>,
        recentMessages: List<LightMessage>
    ): ArcTheme {
        val text = (currentTopics + recentMessages.mapNotNull { it.text }).joinToString(" ").lowercase()
        return themes.firstOrNull { theme ->
            theme.triggerTokens.any { token -> text.contains(token.lowercase()) }
        } ?: themes.first { it.themeId == "planning_not_promise" }
    }

    private fun repeatRisk(entry: ExpressionLedgerEntry?): ExpressionRepeatRisk =
        entry?.repeatRisk ?: ExpressionRepeatRisk.LOW

    private fun isColdStart(
        recentMessages: List<LightMessage>,
        arcProgressState: ArcProgressState
    ): Boolean {
        val last = recentMessages.lastOrNull()
        return (last == null || last.speaker == Speaker.ME) &&
            (arcProgressState.currentExpressionWindow.exists || arcProgressState.unseenPersonaFacets.isNotEmpty())
    }

    private fun reasonFor(
        mode: ExpressionMode,
        entry: ExpressionLedgerEntry?,
        theme: ArcTheme
    ): String {
        val base = when (mode) {
            ExpressionMode.START_TOPIC -> "\u51b7\u573a\u65f6\u7528\u4f4e\u538b\u5f00\u573a\u9732\u51fa\u6bcd\u9898\uff1a${theme.themeName}\u3002"
            ExpressionMode.EXTEND_TOPIC -> "\u540c\u4e00\u6bcd\u9898\u6362\u573a\u666f\u5ef6\u4f38\uff0c\u4e0d\u91cd\u590d\u4e0a\u4e00\u53e5\u3002"
            ExpressionMode.ELEVATE_MEANING -> "\u5bf9\u65b9\u63a5\u4f4f\u8fc7\u8fd9\u4e2a\u6bcd\u9898\uff0c\u8fd9\u6b21\u5347\u5230\u5171\u540c\u8282\u594f\u3002"
            ExpressionMode.SWITCH_FACET -> "\u540c\u4e00\u6bcd\u9898\u91cd\u590d\u98ce\u9669\u504f\u9ad8\uff0c\u6362\u4e00\u4e2a\u8f7d\u4f53\u8868\u8fbe\u3002"
            ExpressionMode.HOLD_BACK -> "\u6700\u8fd1\u5df2\u7ecf\u8fde\u7eed\u8868\u8fbe\u81ea\u5df1\uff0c\u5148\u6536\u4f4f\uff0c\u907f\u514d\u81ea\u6211\u8f93\u51fa\u8fc7\u91cf\u3002"
        }
        val lastLine = entry?.lastSurfaceLineRedacted?.takeIf { it.isNotBlank() }
        return if (lastLine == null) base else "$base \u4e0a\u6b21\u8bf4\u8fc7\uff1a$lastLine"
    }
}
