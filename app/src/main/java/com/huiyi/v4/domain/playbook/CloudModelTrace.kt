package com.huiyi.v4.domain.playbook

enum class CloudRequestPurpose {
    PASSIVE_PLAYBOOK,
    ACTIVE_EXPRESSION,
    ARC_REVEAL,
    DEEP_ANALYSIS
}

data class CloudModelTrace(
    val requestedModel: String = "",
    val selectedModel: String = "",
    val providerType: String = "OPENAI_COMPATIBLE_RELAY",
    val routeReason: String = "",
    val routeTarget: ModelRouteTarget = ModelRouteTarget.LOCAL_FALLBACK,
    val requestPurpose: CloudRequestPurpose = CloudRequestPurpose.PASSIVE_PLAYBOOK,
    val cloudContractValidationResult: String = "NOT_RUN",
    val playbookCacheWriteAllowed: Boolean = false,
    val playbookCacheWriteBlockedReason: String = "NOT_ATTEMPTED"
) {
    val actualModel: String get() = selectedModel.ifBlank { requestedModel }

    fun withValidation(result: String, cacheWriteAllowed: Boolean, blockedReason: String = ""): CloudModelTrace = copy(
        cloudContractValidationResult = result,
        playbookCacheWriteAllowed = cacheWriteAllowed,
        playbookCacheWriteBlockedReason = if (cacheWriteAllowed) "" else blockedReason.ifBlank { "VALIDATION_NOT_PASS" }
    )

    fun blocked(reason: String): CloudModelTrace = copy(
        playbookCacheWriteAllowed = false,
        playbookCacheWriteBlockedReason = reason
    )

    companion object {
        fun local(reason: String = "LOCAL_ONLY"): CloudModelTrace = CloudModelTrace(
            routeReason = reason,
            playbookCacheWriteBlockedReason = reason
        )

        fun fromDecision(
            decision: ModelRouterDecision,
            requestedModel: String,
            requestPurpose: CloudRequestPurpose,
            providerType: String = "OPENAI_COMPATIBLE_RELAY"
        ): CloudModelTrace = CloudModelTrace(
            requestedModel = requestedModel,
            selectedModel = decision.model.orEmpty(),
            providerType = providerType,
            routeReason = decision.reason,
            routeTarget = decision.target,
            requestPurpose = requestPurpose,
            cloudContractValidationResult = "NOT_RUN",
            playbookCacheWriteAllowed = false,
            playbookCacheWriteBlockedReason = "VALIDATION_NOT_RUN"
        )
    }
}

class CloudPlaybookRefreshException(
    message: String,
    val modelTrace: CloudModelTrace,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
