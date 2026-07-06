package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker

enum class ModelRouteTarget {
    LOCAL_WAIT,
    LOCAL_CONTEXT_REQUIRED,
    DS_FLASH_PLAYBOOK,
    DS_PRO,
    GPT_STRONG,
    LOCAL_FALLBACK
}

data class ModelRouterInput(
    val lastSpeaker: Speaker,
    val risk: RiskLevel,
    val validatorPassed: Boolean = true,
    val userRequestedDeepAnalysis: Boolean = false,
    val cloudFailed: Boolean = false,
    val requestPurpose: CloudRequestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK,
    val configuredCheapDraftModel: String = "deepseek-v4-flash",
    val configuredStrongModel: String = "gpt-5.4",
    val deepAnalysisModel: String = "gpt-5.5",
    val dsFlashArcRuntimeEnabled: Boolean = false
)

data class ModelRouterDecision(
    val target: ModelRouteTarget,
    val reason: String,
    val model: String? = null
)

class ModelRouter {
    fun route(input: ModelRouterInput): ModelRouterDecision {
        if (input.lastSpeaker == Speaker.ME) {
            return ModelRouterDecision(ModelRouteTarget.LOCAL_WAIT, "last_speaker_me", null)
        }
        if (input.lastSpeaker == Speaker.UNKNOWN || input.lastSpeaker == Speaker.SYSTEM) {
            return ModelRouterDecision(ModelRouteTarget.LOCAL_CONTEXT_REQUIRED, "last_speaker_unknown", null)
        }
        if (input.cloudFailed) {
            return ModelRouterDecision(ModelRouteTarget.LOCAL_FALLBACK, "cloud_fail", null)
        }
        if (input.userRequestedDeepAnalysis || input.requestPurpose == CloudRequestPurpose.DEEP_ANALYSIS) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "user_deep_analysis", input.deepAnalysisModel)
        }
        if (!input.validatorPassed) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "validator_fail_use_strong_model", input.configuredStrongModel)
        }
        if (input.risk == RiskLevel.HIGH) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "high_risk_needs_strong_model", input.deepAnalysisModel)
        }
        if (input.requestPurpose == CloudRequestPurpose.ACTIVE_EXPRESSION) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "active_expression_needs_sendable_model", input.configuredStrongModel)
        }
        if (input.requestPurpose == CloudRequestPurpose.ARC_REVEAL) {
            return if (input.dsFlashArcRuntimeEnabled) {
                ModelRouterDecision(ModelRouteTarget.DS_FLASH_PLAYBOOK, "arc_reveal_ds_flash_benchmark_enabled", input.configuredCheapDraftModel)
            } else {
                ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "arc_reveal_needs_strong_model", input.configuredStrongModel)
            }
        }
        return ModelRouterDecision(ModelRouteTarget.DS_FLASH_PLAYBOOK, "normal_other_background_playbook", input.configuredCheapDraftModel)
    }
}
