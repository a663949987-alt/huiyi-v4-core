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
    val failureReason: String
)

object RealDeviceScenarioValidator {
    fun validate(result: CurrentScreenPipelineResult?, scenario: RealDeviceScenario): RealDeviceScenarioValidation {
        val actualLastSpeaker = result?.lastSpeakerDecision?.lastSpeaker
        val actualDecisionType = result?.tacticalDecision?.decisionType
        val actualRouteCount = result?.routes?.size ?: 0
        val effectiveMessages = result?.captureResult?.messages.orEmpty()
            .filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val lastEffective = result?.lastSpeakerDecision?.lastEffectiveMessage
        val failureReason = when {
            result == null -> "not_tested"
            result.apiCalled -> "api_called"
            effectiveMessages.isEmpty() -> "no_effective_message"
            scenario.expectedLastSpeaker != null && actualLastSpeaker != scenario.expectedLastSpeaker -> "last_speaker_mismatch"
            scenario.expectedDecisionType != null && actualDecisionType != scenario.expectedDecisionType -> "decision_type_mismatch"
            scenario.expectedRouteCount != null && actualRouteCount != scenario.expectedRouteCount -> "route_count_mismatch"
            scenario == RealDeviceScenario.LAST_OTHER && actualDecisionType in setOf(TacticalDecisionType.WAIT, TacticalDecisionType.CONTEXT_REQUIRED) -> "last_other_should_generate_routes"
            scenario == RealDeviceScenario.METADATA_TRAP && lastEffective?.metadataType != MetadataType.NONE -> "metadata_polluted_last_speaker"
            scenario == RealDeviceScenario.VOICE_LAST_OTHER && lastEffective?.content !is MessageContent.Voice -> "voice_expected"
            else -> "none"
        }
        return RealDeviceScenarioValidation(
            scenarioName = scenario.id,
            expectedLastSpeaker = scenario.expectedLastSpeaker?.name ?: "NO_FIXED_EXPECTATION",
            actualLastSpeaker = actualLastSpeaker?.name ?: "NOT_TESTED",
            expectedDecisionType = scenario.expectedDecisionType?.name ?: "NO_FIXED_EXPECTATION",
            actualDecisionType = actualDecisionType?.name ?: "NOT_TESTED",
            expectedRouteCount = scenario.expectedRouteCount?.toString() ?: "NO_FIXED_EXPECTATION",
            actualRouteCount = actualRouteCount,
            scenarioResult = if (failureReason == "none") "PASS" else if (failureReason == "not_tested") "NOT_TESTED" else "FAIL",
            failureReason = failureReason
        )
    }
}
