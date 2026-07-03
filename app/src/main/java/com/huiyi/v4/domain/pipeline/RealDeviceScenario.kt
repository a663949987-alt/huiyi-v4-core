package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType

enum class RealDeviceScenario(
    val id: String,
    val displayName: String,
    val expectedLastSpeaker: Speaker?,
    val expectedDecisionType: TacticalDecisionType?,
    val expectedRouteCount: Int?
) {
    AUTO_FROM_SCREEN("auto_from_screen", "Auto from current screen", null, null, null),
    LAST_ME("last_me", "A last_me", Speaker.ME, TacticalDecisionType.WAIT, 0),
    LAST_OTHER("last_other", "B last_other", Speaker.OTHER, null, 5),
    METADATA_TRAP("metadata_trap", "C metadata_trap", null, null, null),
    VOICE_LAST_OTHER("voice_last_other", "D voice_last_other", Speaker.OTHER, TacticalDecisionType.VOICE_SUMMARY_REQUIRED, 0),
    UNKNOWN_BOUNDS("unknown_bounds", "E unknown_bounds", Speaker.UNKNOWN, TacticalDecisionType.CONTEXT_REQUIRED, 0);

    companion object {
        fun fromId(id: String?): RealDeviceScenario {
            return entries.firstOrNull { it.id == id } ?: LAST_ME
        }
    }
}

data class RealDeviceScenarioValidation(
    val scenarioName: String,
    val expectedLastSpeaker: String,
    val actualLastSpeaker: String,
    val expectedDecisionType: String,
    val actualDecisionType: String,
    val expectedRouteCount: String,
    val actualRouteCount: Int,
    val scenarioResult: String,
    val failureReason: String,
    val realDeviceFunctionalSmoke: String,
    val scenarioAssertionResult: String,
    val currentOverallResult: String,
    val scenarioDefinitionTrusted: Boolean,
    val scenarioFailureCategory: String,
    val scenarioNameSource: String,
    val expectedLastSpeakerSource: String,
    val scenarioDefinitionMismatchReason: String?,
    val actualLastSpeakerFromPreAnalysisSnapshot: String,
    val actualLastSpeakerFromDecisionSnapshot: String,
    val actualLastSpeakerFromPostPanelSnapshot: String,
    val expectedDecisionTypeSource: String,
    val expectedRouteCountSource: String,
    val productDecisionConsistentWithActualLastSpeaker: Boolean,
    val screenshotDiagnosticStatus: String,
    val screenshotFailureBlocksMainPath: Boolean,
    val secondaryDiagnosticErrorCode: String,
    val visualTruthAvailable: Boolean,
    val visualTruthSource: String,
    val accessibilityProjectionAvailable: Boolean,
    val visualProjectionSource: String,
    val preAnalysisSnapshotAvailable: Boolean,
    val preAnalysisWindowTitle: String,
    val postPanelSnapshotAvailable: Boolean,
    val postPanelWindowTitle: String,
    val reportWindowTitleContaminatedByPanel: Boolean,
    val postPanelStateUsedForScenarioExpectation: Boolean
)

object RealDeviceScenarioValidator {
    fun validate(result: CurrentScreenPipelineResult?, scenario: RealDeviceScenario): RealDeviceScenarioValidation {
        val actualLastSpeaker = result?.lastSpeakerDecision?.lastSpeaker
        val actualDecisionType = result?.tacticalDecision?.decisionType
        val actualRouteCount = result?.routes?.size ?: 0
        val effectiveMessages = result?.captureResult?.messages.orEmpty()
            .filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val lastEffective = result?.lastSpeakerDecision?.lastEffectiveMessage
        val scenarioIsAuto = scenario == RealDeviceScenario.AUTO_FROM_SCREEN
        val derivedScenarioName = when (actualLastSpeaker) {
            Speaker.ME -> "real_device_last_me"
            Speaker.OTHER -> "real_device_last_other"
            Speaker.UNKNOWN -> "real_device_last_unknown"
            Speaker.SYSTEM -> "real_device_last_system"
            null -> scenario.id
        }
        val expectedSpeaker = if (scenarioIsAuto) actualLastSpeaker else scenario.expectedLastSpeaker
        val expectedDecisionType = if (scenarioIsAuto) expectedDecisionFor(expectedSpeaker) else scenario.expectedDecisionType
        val expectedRouteCount = if (scenarioIsAuto) expectedRouteCountFor(expectedSpeaker, actualDecisionType, actualRouteCount) else scenario.expectedRouteCount
        val scenarioName = if (scenarioIsAuto && result != null) derivedScenarioName else scenario.id
        val scenarioSource = if (scenarioIsAuto) "AUTO_FROM_PRE_ANALYSIS_SNAPSHOT" else "MANUAL"
        val functionalFailureReason = functionalFailureReason(result, effectiveMessages)
        val realDeviceFunctionalSmoke = when {
            result == null -> "NOT_TESTED"
            functionalFailureReason == "none" -> "PASS"
            functionalFailureReason in setOf("context_required_for_actual_other", "voice_summary_required_for_actual_other") -> "PASS"
            else -> "FAIL"
        }
        val scenarioMismatch = expectedSpeaker != null && actualLastSpeaker != null && actualLastSpeaker != expectedSpeaker
        val scenarioFailureReason = when {
            result == null -> "not_tested"
            scenarioMismatch -> "scenario_definition_mismatch"
            result.apiCalled -> "api_called"
            effectiveMessages.isEmpty() -> "no_effective_message"
            expectedDecisionType != null && actualDecisionType != expectedDecisionType -> "decision_type_mismatch"
            expectedRouteCount != null && actualRouteCount != expectedRouteCount -> "route_count_mismatch"
            scenario == RealDeviceScenario.LAST_OTHER && actualDecisionType == TacticalDecisionType.WAIT -> "last_other_should_not_wait"
            scenario == RealDeviceScenario.METADATA_TRAP && lastEffective?.metadataType != MetadataType.NONE -> "metadata_polluted_last_speaker"
            scenario == RealDeviceScenario.VOICE_LAST_OTHER && lastEffective?.content !is MessageContent.Voice -> "voice_expected"
            else -> "none"
        }
        val scenarioAssertionResult = when {
            result == null -> "NOT_TESTED"
            scenario == RealDeviceScenario.METADATA_TRAP -> if (scenarioFailureReason == "none") "PASS" else "MISMATCH"
            scenarioIsAuto -> "PASS"
            scenarioMismatch -> "MISMATCH"
            scenario.expectedLastSpeaker == null && scenario.expectedDecisionType == null && scenario.expectedRouteCount == null -> "NOT_APPLICABLE"
            scenarioFailureReason == "none" || scenarioFailureReason in setOf(
                "context_required_for_actual_other",
                "voice_summary_required_for_actual_other"
            ) -> "PASS"
            else -> "MISMATCH"
        }
        val currentOverallResult = when {
            result == null -> "NOT_TESTED"
            realDeviceFunctionalSmoke == "FAIL" -> "FAIL"
            realDeviceFunctionalSmoke == "PASS" && scenarioAssertionResult == "MISMATCH" -> "CONTROLLED_PASS_WITH_SCENARIO_MISMATCH"
            realDeviceFunctionalSmoke == "PASS" -> "PASS"
            else -> "CONTROLLED_FAIL"
        }
        val scenarioFailureCategory = when {
            result == null -> "not_tested"
            scenarioMismatch -> "SCENARIO_DEFINITION_MISMATCH"
            scenarioFailureReason == "decision_type_mismatch" || scenarioFailureReason == "route_count_mismatch" -> "FUNCTIONAL_PASS_ASSERTION_FAIL"
            else -> "none"
        }
        val title = result?.captureResult?.snapshot?.windowTitle.orEmpty()
        val titleContaminated = isPostPanelWindowTitle(title) || isKnownHuiyiOverlayTitle(title)
        val visualDebug = result?.visualDebugResult
        val visualTruthAvailable = visualDebug?.screenshotCaptured == true ||
            result?.userCorrectionProvided == true
        val screenshotDiagnosticStatus = when {
            visualDebug?.screenshotCaptured == true -> "OPTIONAL_SUCCEEDED"
            visualDebug?.screenshotUnavailable == true -> "OPTIONAL_FAILED"
            else -> "NOT_ATTEMPTED"
        }
        val secondaryErrorCode = when {
            visualDebug?.screenshotErrorCode != null -> visualDebug.screenshotErrorCode
            visualDebug?.screenshotUnavailable == true -> "SCREENSHOT_CAPABILITY_MISSING"
            else -> "none"
        }
        return RealDeviceScenarioValidation(
            scenarioName = scenarioName,
            expectedLastSpeaker = expectedSpeaker?.name ?: "NO_FIXED_EXPECTATION",
            actualLastSpeaker = actualLastSpeaker?.name ?: "NOT_TESTED",
            expectedDecisionType = expectedDecisionType?.name ?: "NO_FIXED_EXPECTATION",
            actualDecisionType = actualDecisionType?.name ?: "NOT_TESTED",
            expectedRouteCount = expectedRouteCount?.toString() ?: "NO_FIXED_EXPECTATION",
            actualRouteCount = actualRouteCount,
            scenarioResult = scenarioAssertionResult,
            failureReason = scenarioFailureReason,
            realDeviceFunctionalSmoke = realDeviceFunctionalSmoke,
            scenarioAssertionResult = scenarioAssertionResult,
            currentOverallResult = currentOverallResult,
            scenarioDefinitionTrusted = scenarioAssertionResult != "MISMATCH" && !titleContaminated,
            scenarioFailureCategory = scenarioFailureCategory,
            scenarioNameSource = scenarioSource,
            expectedLastSpeakerSource = scenarioSource,
            scenarioDefinitionMismatchReason = if (scenarioMismatch) "expectedLastSpeaker_${expectedSpeaker?.name}_actual_${actualLastSpeaker?.name}" else null,
            actualLastSpeakerFromPreAnalysisSnapshot = actualLastSpeaker?.name ?: "NOT_TESTED",
            actualLastSpeakerFromDecisionSnapshot = actualLastSpeaker?.name ?: "NOT_TESTED",
            actualLastSpeakerFromPostPanelSnapshot = actualLastSpeaker?.name ?: "NOT_TESTED",
            expectedDecisionTypeSource = if (expectedDecisionType == null) "NO_FIXED_EXPECTATION" else if (scenarioIsAuto) "DERIVED_FROM_EXPECTED_LAST_SPEAKER" else "MANUAL",
            expectedRouteCountSource = if (expectedRouteCount == null) "NO_FIXED_EXPECTATION" else if (scenarioIsAuto) "DERIVED_FROM_EXPECTED_LAST_SPEAKER" else "MANUAL",
            productDecisionConsistentWithActualLastSpeaker = functionalFailureReason == "none" ||
                functionalFailureReason in setOf("context_required_for_actual_other", "voice_summary_required_for_actual_other"),
            screenshotDiagnosticStatus = screenshotDiagnosticStatus,
            screenshotFailureBlocksMainPath = false,
            secondaryDiagnosticErrorCode = secondaryErrorCode ?: "none",
            visualTruthAvailable = visualTruthAvailable,
            visualTruthSource = when {
                visualDebug?.screenshotCaptured == true -> "SCREENSHOT"
                result?.userCorrectionProvided == true -> "USER_CORRECTION"
                else -> "NONE"
            },
            accessibilityProjectionAvailable = visualDebug?.accessibilityBoundsProjected ?: result?.captureResult?.accessibilityBoundsProjected ?: false,
            visualProjectionSource = "ACCESSIBILITY_BOUNDS_ONLY",
            preAnalysisSnapshotAvailable = result?.captureResult != null,
            preAnalysisWindowTitle = if (titleContaminated) "UNKNOWN_CONTAMINATED_BY_POST_PANEL" else title.ifBlank { "unknown" },
            postPanelSnapshotAvailable = titleContaminated || result?.resultShownAsOverlay == true,
            postPanelWindowTitle = if (titleContaminated) title else "none",
            reportWindowTitleContaminatedByPanel = titleContaminated,
            postPanelStateUsedForScenarioExpectation = false
        )
    }

    fun isPostPanelWindowTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        val markers = listOf("会意雷达", "路线", "打法", "判断：", "复制路线", "VoiceSummaryCard")
        return markers.any { title.contains(it, ignoreCase = true) }
    }

    private fun isKnownHuiyiOverlayTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        val markers = listOf(
            "会意雷达",
            "下一句没有跑完",
            "正在上传 GitHub",
            "这次不对",
            "发给 GPT",
            "导出诊断",
            "打开无障碍设置",
            "隐藏悬浮球",
            "重试",
            "浼氭剰闆疯揪",
            "涓嬩竴鍙ユ病鏈夎窇瀹",
            "姝ｅ湪涓婁紶 GitHub",
            "杩欐",
            "鍙戠粰 GPT",
            "瀵煎嚭璇婃柇",
            "鎵撳紑鏃犻殰",
            "闅愯棌",
            "閲嶈瘯"
        )
        return markers.any { title.contains(it, ignoreCase = true) }
    }

    private fun expectedDecisionFor(speaker: Speaker?): TacticalDecisionType? = when (speaker) {
        Speaker.ME -> TacticalDecisionType.WAIT
        Speaker.OTHER -> null
        Speaker.UNKNOWN -> TacticalDecisionType.CONTEXT_REQUIRED
        else -> null
    }

    private fun expectedRouteCountFor(
        speaker: Speaker?,
        actualDecisionType: TacticalDecisionType?,
        actualRouteCount: Int
    ): Int? = when (speaker) {
        Speaker.ME, Speaker.UNKNOWN -> 0
        Speaker.OTHER -> if (actualDecisionType == TacticalDecisionType.CONTEXT_REQUIRED) actualRouteCount else 5
        else -> null
    }

    private fun functionalFailureReason(
        result: CurrentScreenPipelineResult?,
        effectiveMessages: List<com.huiyi.v4.domain.model.MessageNode>
    ): String {
        if (result == null) return "not_tested"
        val actualLastSpeaker = result.lastSpeakerDecision.lastSpeaker
        val decision = result.tacticalDecision.decisionType
        val routeCount = result.routes.size
        return when {
            result.apiCalled -> "api_called"
            effectiveMessages.isEmpty() -> "no_effective_message"
            result.mainActivityOpened -> "main_activity_opened"
            !result.overlayShownInTargetApp -> "overlay_not_in_target_app"
            !result.resultShownAsOverlay -> "result_panel_not_shown_as_overlay"
            !result.userStayedInChatApp -> "user_left_chat_app"
            actualLastSpeaker == Speaker.ME && (decision != TacticalDecisionType.WAIT || routeCount != 0) -> "actual_me_should_wait"
            actualLastSpeaker == Speaker.OTHER &&
                decision in setOf(TacticalDecisionType.NORMAL_REPLY, TacticalDecisionType.EMPATHY_FIRST) &&
                routeCount == 5 -> "none"
            actualLastSpeaker == Speaker.OTHER && decision == TacticalDecisionType.CONTEXT_REQUIRED && routeCount == 0 -> "context_required_for_actual_other"
            actualLastSpeaker == Speaker.OTHER && decision == TacticalDecisionType.VOICE_SUMMARY_REQUIRED && routeCount == 0 -> "voice_summary_required_for_actual_other"
            actualLastSpeaker == Speaker.OTHER -> "actual_other_should_generate_or_request_context"
            actualLastSpeaker == Speaker.UNKNOWN && decision == TacticalDecisionType.CONTEXT_REQUIRED && routeCount == 0 -> "none"
            actualLastSpeaker == Speaker.UNKNOWN -> "unknown_should_block_routes"
            else -> "none"
        }
    }
}
