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
    val cloudFailed: Boolean = false
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
        if (input.userRequestedDeepAnalysis) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "user_deep_analysis", "gpt-5.5")
        }
        if (!input.validatorPassed) {
            return ModelRouterDecision(ModelRouteTarget.DS_PRO, "validator_fail_try_stronger_playbook", "deepseek-v4-pro")
        }
        if (input.risk == RiskLevel.HIGH) {
            return ModelRouterDecision(ModelRouteTarget.GPT_STRONG, "high_risk_needs_strong_model", "gpt-5.5")
        }
        return ModelRouterDecision(ModelRouteTarget.DS_FLASH_PLAYBOOK, "normal_other_background_playbook", "deepseek-v4-flash")
    }
}
